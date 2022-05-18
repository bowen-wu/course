package com.personal.course.aop;

import com.personal.course.configuration.UserContext;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.DO.Permission;
import com.personal.course.entity.DO.Role;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Aspect
@Configuration
public class PermissionAspect {
    @Around("@annotation(com.personal.course.annotation.Admin)")
    public Object admin(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        UserContext.getUser().getRoles()
                .stream()
                .map(Role::getName)
                .filter("admin"::equals)
                .findAny()
                .orElseThrow(HttpException::forbidden);
        return proceedingJoinPoint.proceed();
    }

    @Around("@annotation(com.personal.course.annotation.ManagementCourse)")
    public Object managementCourse(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        UserContext.getUser().getRoles()
                .stream()
                .map(Role::getPermissionList)
                .flatMap(Collection::stream)
                .map(Permission::getName)
                .filter("managementCourse"::equals)
                .findAny()
                .orElseThrow(HttpException::forbidden);
        return proceedingJoinPoint.proceed();
    }
}
