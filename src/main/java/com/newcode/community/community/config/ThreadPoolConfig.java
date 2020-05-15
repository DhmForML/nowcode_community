package com.newcode.community.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling               //启用spring内置的schedule注解
@EnableAsync                    //启用异步注解
public class ThreadPoolConfig {

}
