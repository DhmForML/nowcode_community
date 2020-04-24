package com.newcode.community.community.controller.interceptor;

import com.newcode.community.community.annotation.LoginRequired;
import com.newcode.community.community.entity.User;
import com.newcode.community.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if(handler instanceof HandlerMethod) {   //handler就是调用的请求方式
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            if(loginRequired != null && hostHolder.getUsers() == null){     //如果这个方法使用了LoginRequired注解,
                try {                                                       // 但是该用户还没登录，就重定向到主页
                    response.sendRedirect(request.getContextPath() + "/login");
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
