package com.swpu.yosmart;

import org.mybatis.spring.annotation.MapperScan;
import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("com.swpu.yosmart.mapper")
public class YosmartApplication {

	public static void main(String[] args) {
		SpringApplication.run(YosmartApplication.class, args);
	}
}
