package com.newcode.community.community.controller;

import com.newcode.community.community.entity.DiscussPost;
import com.newcode.community.community.entity.Page;
import com.newcode.community.community.service.ElasticsearchService;
import com.newcode.community.community.service.LikeService;
import com.newcode.community.community.service.UserService;
import com.newcode.community.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    ElasticsearchService elasticsearchService;

    @Autowired
    UserService userService;

    @Autowired
    LikeService likeService;

    @RequestMapping(value = "/search",method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){

        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticsearchService.searchDiscussPost(keyword,page.getCurrent()-1,page.getLimit());

        //聚合数据
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(searchResult != null){
            for (DiscussPost post : searchResult){
                Map<String,Object> map = new HashMap<>();
                //帖子
                map.put("post",post);
                //作者
                map.put("user",userService.findUserById(post.getUserId()));
                //点赞数量
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
                discussPosts.add(map);
            }
        }

        //分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());

        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);  //其实可以不用存，因为参数里已经存了

        return "/site/search";

    }

}
