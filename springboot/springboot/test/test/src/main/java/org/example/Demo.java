package org.example;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yufengyang
 * @Package org.example
 * @date ${DATE} ${TIME}
 * @school hnist
 */
@SpringBootApplication
@MapperScan("org.example.mapper")
public class Demo {
    public static void main(String[] args) {
        SpringApplication.run(Demo.class,args);
    }
}