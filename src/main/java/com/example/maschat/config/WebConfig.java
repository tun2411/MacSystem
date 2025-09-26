package com.example.maschat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private AutoLoginInterceptor autoLoginInterceptor;
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // no-op; placeholder in case we need static mappings
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /*
        registry.addInterceptor(autoLoginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/register", "/static/**", "/css/**", "/js/**", "/images/**");
        */
    }
}


