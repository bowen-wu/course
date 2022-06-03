package com.personal.course.configuration;

import com.personal.course.service.AuthService;
import com.personal.course.service.CookieService;
import com.personal.course.service.SessionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Inject;

@Configuration
public class Config implements WebMvcConfigurer {
    private final SessionService sessionService;
    private final CookieService cookieService;
    private final AuthService authService;

    @Inject
    public Config(SessionService sessionService, CookieService cookieService, AuthService authService) {
        this.sessionService = sessionService;
        this.cookieService = cookieService;
        this.authService = authService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(sessionService, cookieService, authService));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST", "PATCH", "DELETE");
    }
}
