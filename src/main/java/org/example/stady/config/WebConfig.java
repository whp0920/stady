package org.example.stady.config;

import org.example.stady.interceptor.LoginIntercepter;
import org.example.stady.utils.UserContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginIntercepter())
                .addPathPatterns("/api/records/**", "/api/journal/**")
                .excludePathPatterns("/api/users/**");
    }
}
