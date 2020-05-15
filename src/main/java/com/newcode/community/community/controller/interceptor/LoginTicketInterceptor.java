package com.newcode.community.community.controller.interceptor;

import com.newcode.community.community.entity.LoginTicket;
import com.newcode.community.community.entity.User;
import com.newcode.community.community.service.UserService;
import com.newcode.community.community.util.CookieUtil;
import com.newcode.community.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //从cookie中获取凭证
        String ticket = CookieUtil.getValue(request,"ticket");
        if(ticket != null){
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查凭证是否有效
            if((loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date()))){
                //根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有用户信息
                if(user != null){
                    hostHolder.setUsers(user);
                    //构建用户认证的结果，并存入SecurityContext,以便于Security进行授权
                    Authentication authentication =  new UsernamePasswordAuthenticationToken(
                            user,user.getPassword(),userService.getAuthorities(user.getId()));
                    SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
                }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        //获取用户信息
        User user = hostHolder.getUsers();
        //通过ModelAndView把用户信息传给模板
         if(user != null && modelAndView != null){
             modelAndView.addObject("loginUser",user);
         }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        //清除掉用户信息
        hostHolder.clear();
        SecurityContextHolder.clearContext();
    }
}
