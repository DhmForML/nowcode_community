package com.newcode.community.community.controller.interceptor;

import com.newcode.community.community.entity.User;
import com.newcode.community.community.service.DataService;
import com.newcode.community.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    DataService dataService;

    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //统计UV
        dataService.recordUV(request.getRemoteHost());

        //统计DAU
        User user = hostHolder.getUsers();
        if(user != null){
            dataService.recordDAU(user.getId());
        }
        return true;
    }
}
