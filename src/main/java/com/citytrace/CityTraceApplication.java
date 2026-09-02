package com.citytrace;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;

@MapperScan("com.citytrace.mapper")
@EnableRetry
@EnableKafka
@SpringBootApplication
public class CityTraceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CityTraceApplication.class, args);
    }

}
