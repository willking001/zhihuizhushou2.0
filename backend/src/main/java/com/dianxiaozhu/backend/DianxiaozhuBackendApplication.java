package com.dianxiaozhu.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
public class DianxiaozhuBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DianxiaozhuBackendApplication.class, args);
	}

}