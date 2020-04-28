package com.newcode.community.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {

    //定义切点,第一个*表示返回值 第一个.*表示所有类 第二个.*表示所有方法 （..）表示所有参数
    @Pointcut("execution(* com.newcode.community.community.service.*.*(..))")
    public void pointcut(){

    }

    //定义前置通知
    @Before("pointcut()")
    public void before(){
        System.out.println("before...");
    }

    //定义后置通知
    @After("pointcut()")
    public void after(){
        System.out.println("after...");
    }

    //定义返回后通知
    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning...");
    }

    //定义异常后通知
    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing...");
    }

    //定义环绕通知
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("aroundBefore...");
        Object obj = joinPoint.proceed();
        System.out.println("aroundAfter...");
        return obj;
    }
}
