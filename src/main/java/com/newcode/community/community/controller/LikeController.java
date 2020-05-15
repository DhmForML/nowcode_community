package com.newcode.community.community.controller;

import com.newcode.community.community.annotation.LoginRequired;
import com.newcode.community.community.entity.Event;
import com.newcode.community.community.entity.User;
import com.newcode.community.community.event.EventProducer;
import com.newcode.community.community.service.LikeService;
import com.newcode.community.community.service.ScoreService;
import com.newcode.community.community.util.CommunityConstant;
import com.newcode.community.community.util.CommunityUtil;
import com.newcode.community.community.util.HostHolder;
import com.newcode.community.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    ScoreService scoreService;

    @LoginRequired
    @RequestMapping(value = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int postId){
        User user = hostHolder.getUsers();

        //点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);

        //返回的结果
        Map<String,Object> map = new HashMap<>();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType,entityId);
        //点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(),entityType,entityId);
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        if(likeStatus == 1){    //触发点赞事件
            Event event = new Event()
                        .setTopic(TOPIC_LIKE)
                        .setUserId(user.getId())
                        .setEntityType(entityType)
                        .setEntityId(entityId)
                        .setEntityUserId(entityUserId)
                        .setData("postId",postId);
            eventProducer.fireEvent(event);
        }

        if(entityType == ENTITY_TYPE_POST){
            //计算帖子分数
            scoreService.addPostScore(entityId);
        }

        return CommunityUtil.getJSONString(0,null,map);
    }

}
