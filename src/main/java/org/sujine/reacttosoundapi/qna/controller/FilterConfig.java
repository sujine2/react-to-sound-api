package org.sujine.reacttosoundapi.qna.controller;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.ApplicationModule;
import org.sujine.reacttosoundapi.jwt.JwtFilter;

@Configuration
class FilterConfig {

    @Bean
    FilterRegistrationBean<JwtFilter> jwtFilter() {
        FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtFilter());
        registrationBean.addUrlPatterns("/aks", "/history");
        return registrationBean;
    }
}
