package com.newcode.community.community.controller;

import com.newcode.community.community.annotation.LoginRequired;
import com.newcode.community.community.entity.Event;
import com.newcode.community.community.entity.Page;
import com.newcode.community.community.entity.User;
import com.newcode.community.community.event.EventProducer;
import com.newcode.community.community.service.FollowService;
import com.newcode.community.community.service.UserService;
import com.newcode.community.community.util.CommunityConstant;
import com.newcode.community.community.util.CommunityUtil;
import com.newcode.community.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    UserService userService;

    @Autowired
    FollowService followService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    EventProducer eventProducer;

    @LoginRequired
    @RequestMapping(value = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUsers();

        followService.follow(user.getId(),entityType,entityId);

        //触发关注事件
        Event event = new Event()
                    .setTopic(TOPIC_FOLLOW)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"已关注！");
    }

    @LoginRequired
    @RequestMapping(value = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.getUsers();
        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注");
    }

    @RequestMapping(value = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new IllegalArgumentException("用户不存在！");
        }
        model.addAttribute("user",user);
        page.setRows((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));
        page.setLimit(5);
        page.setPath("/followees/" + userId);

        List<Map<String,Object>> userList = followService.findFollowees(userId,page.getOffset(),page.getLimit());
        if(userList != null){
            for(Map<String,Object> map : userList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/followee";
    }

    @RequestMapping(value = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new IllegalArgumentException("用户不存在！");
        }
        model.addAttribute("user",user);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));
        page.setLimit(5);
        page.setPath("/followees/" + userId);

        List<Map<String,Object>> userList = followService.findFollowers(userId,page.getOffset(),page.getLimit());
        if(userList != null){
            for(Map<String,Object> map : userList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/follower";
    }



    private boolean hasFollowed(int userId){
        if(hostHolder.getUsers() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUsers().getId(),ENTITY_TYPE_USER,userId);
    }

}
