package com.personal.course.configuration;

import com.personal.course.entity.HttpException;
import com.personal.course.entity.DO.Session;
import com.personal.course.entity.Whitelist;
import com.personal.course.service.SessionService;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

public class AuthInterceptor implements HandlerInterceptor {
    public static String COOKIE_NAME = "COURSE_APP_SESSION_ID";

    private final SessionService sessionService;

    @Inject
    public AuthInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(!request.getRequestURI().startsWith("/api/v1")) {
            return true;
        }
        // get User & save user info to ThreadLocal
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                    .filter(item -> item.getName().equals(COOKIE_NAME))
                    .map(Cookie::getValue)
                    .findFirst()
                    .flatMap(sessionService::getSessionByCookie)
                    .map(Session::getUser)
                    .ifPresent(UserContext::setUser);
        }
        List<Whitelist> whitelistList = Arrays.asList(
                Whitelist.of("/api/v1/test", "GET"),
                Whitelist.of("/api/v1/test", "POST"),
                Whitelist.of("/api/v1/session", "POST"),
                Whitelist.of("/api/v1/session", "GET"),
                Whitelist.of("/api/v1/user", "POST"));
        if (UserContext.getUser() == null) {
            whitelistList.stream()
                    .filter(whitelist -> whitelist.getUri().equals(request.getRequestURI()) && whitelist.getMethod().equals(request.getMethod()))
                    .findAny()
                    .orElseThrow(() -> HttpException.unauthorized("请登录!"));
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        // remove user info from ThreadLocal
        UserContext.removeUser();
    }
}
