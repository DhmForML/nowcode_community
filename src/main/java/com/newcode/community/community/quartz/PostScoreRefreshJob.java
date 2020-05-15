package com.newcode.community.community.quartz;

import com.newcode.community.community.entity.DiscussPost;
import com.newcode.community.community.entity.Event;
import com.newcode.community.community.event.EventProducer;
import com.newcode.community.community.service.DiscussPostService;
import com.newcode.community.community.service.ElasticsearchService;
import com.newcode.community.community.service.LikeService;
import com.newcode.community.community.util.CommunityConstant;
import com.newcode.community.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job,CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    EventProducer eventProducer = new EventProducer();

    @Autowired
    LikeService likeService;

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    RedisTemplate redisTemplate;

    //牛客纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败！",e);         //因为抛出异常后，后续代码可以继续执行，可以保证epoch能被初始化
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0){
            logger.info("[任务取消] 没有需要刷新的帖子！");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子的分数：" + operations.size());
        while (operations.size() > 0){
            this.refresh((Integer) operations.pop());
        }
    }

    private void refresh(int postId){
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if(post == null){
            logger.error("该帖子不存在: id = " + postId);
            return;
        }

        //是否精华
        boolean wonderful = post.getStatus() == 1;

        //评论数量
        int commentCount = post.getCommentCount();

        //点赞数量
        int likeCount = (int) likeService.findEntityLikeCount(ENTITY_TYPE_POST,postId);

        //计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;

        //分数 = 权重 + 距离天数
        double score = Math.log10(Math.max(w,1)) +
                (post.getCreateTime().getTime() - epoch.getTime())/(1000 * 3600 * 24);
        discussPostService.updateScore(postId,score);

        //触发帖子发布事件，同步保存数据
        Event event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(SYSTEM_USER_ID)
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(postId);
        eventProducer.fireEvent(event);

    }
}
