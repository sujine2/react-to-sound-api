package org.sujine.reacttosoundapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public class ReactToSoundApiApplication extends SpringBootServletInitializer {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ReactToSoundApiApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(ReactToSoundApiApplication.class, args);
	}

}
