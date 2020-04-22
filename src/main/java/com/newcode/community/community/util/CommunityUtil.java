package com.newcode.community.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    public static String md5(String key){
        if (StringUtils.isBlank(key)){ //判断密码是否为空，
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());      //使用MD5进行加密
    }
}
