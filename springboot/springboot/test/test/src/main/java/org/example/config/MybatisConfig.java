package org.example.config;

import org.apache.ibatis.plugin.Interceptor;
import org.example.plugin.Paginationincepter;
import org.example.plugin2.Paginationincepter2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yufengyang
 * @Package org.example.config
 * @date 2025/8/21 3:11
 * @school hnist
 */
@Configuration
public class MybatisConfig {
    @Bean
    public Interceptor paginationInterceptor(){
        return new Paginationincepter();
    }
    @Bean
    public Interceptor paginationInterceptor2(){return new Paginationincepter2(); }
}
