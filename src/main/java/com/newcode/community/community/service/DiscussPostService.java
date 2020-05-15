package com.newcode.community.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.newcode.community.community.dao.DiscussPostMapper;
import com.newcode.community.community.entity.DiscussPost;
import com.newcode.community.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    SensitiveFilter sensitiveFilter;

    @Autowired
    RedisTemplate redisTemplate;


    //Caffeine核心接口：Cache,LoadingCache,AsyncLoadingCache

    //帖子列表和缓存
    private LoadingCache<String,List<DiscussPost>> postListCache;

    //帖子总数缓存
    private LoadingCache<Integer,Integer> postRowsCache;

    @PostConstruct
    public void init(){
        //初始化帖子列表
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if(key == null || key.length() == 0){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        String[] params = key.split(":");
                        if(params == null || params.length != 2){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);
                        //二级缓存：redis->mysql
//                        String redisKey = key;
//                        if(redisTemplate.hasKey(redisKey)){
//                            return redisTemplate.opsForList().range(redisKey,0,-1);
//                        }
//                        List<DiscussPost> postList = discussPostMapper.selectDiscussPosts(0,offset,limit,1);
//                        redisTemplate.opsForList().leftPushAll(redisKey,postList);
//                        redisTemplate.expire(redisKey,360,TimeUnit.SECONDS);
                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                    }
                });

        postRowsCache = Caffeine.newBuilder()
                    .maximumSize(maxSize)
                    .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                    .build(new CacheLoader<Integer, Integer>() {
                        @Override
                        public Integer load(@NonNull Integer key) throws Exception {
                            logger.debug("load post rows from DB.");
                            return discussPostMapper.selectDiscussPostRows(key);
                        }
                    });
    }

    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,int orderMode){
        if(userId == 0 && orderMode == 1){
            return postListCache.get(offset + ":" + limit);
        }
        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    public int findDiscussPostRows(int userId){
        if(userId == 0){
            return postRowsCache.get(userId);
        }
        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int insertDiscussPost(DiscussPost discussPost){
        if(discussPost == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle())); //对用户可能输入的Html标签进行转义
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    //更新状态
    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id,status);
    }
    //更新类型
    public int updateType(int id,int type){
        return discussPostMapper.updateType(id,type);
    }

    //更新分数
    public int updateScore(int id,double score){
        return discussPostMapper.updateScore(id,score);
    }

}
