package com.newcode.community.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution(* com.newcode.community.community.service.*.*(..))")
    public void pointcut(){

    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        //实现记录用户登录的日志
        //用户[1.2.3.4]，在[xxx]，访问了[com.newcode.community.community.service.xxx()]
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();        //获取ip地址
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());    //格式化日期
        //获取访问的路径和方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s],在[%s],访问了[%s]",ip,now,target));
    }
}
