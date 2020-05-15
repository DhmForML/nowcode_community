package com.newcode.community.community.service;

import com.newcode.community.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ScoreService {

    @Autowired
    RedisTemplate redisTemplate;

    public void addPostScore(int postId){
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,postId);
    }

}
