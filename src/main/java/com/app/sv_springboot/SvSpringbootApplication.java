package com.app.sv_springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class SvSpringbootApplication extends SpringBootServletInitializer {
//public class SvSpringbootApplication extends ServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SvSpringbootApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SvSpringbootApplication.class, args);
	}

}
