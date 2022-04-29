package com.personal.course.aop;

import com.personal.course.configuration.UserContext;
import com.personal.course.entity.HttpException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class AdminAspect {
    @Around("@annotation(com.personal.course.annotation.Admin)")
    public Object admin(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        UserContext.getUser().getRoles().stream().filter(role -> role.getName().equals("admin")).findAny().orElseThrow(HttpException::forbidden);
        return proceedingJoinPoint.proceed();
    }
}
