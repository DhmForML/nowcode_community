package com.newcode.community.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)             //说明这个注解是用在方法上的
@Retention(RetentionPolicy.RUNTIME)     //程序运行时这个注解才起作用
public @interface LoginRequired {

}
