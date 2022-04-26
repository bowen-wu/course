package com.personal.course.configuration;

import com.personal.course.dao.SessionDao;
import com.personal.course.entity.Session;
import com.personal.course.entity.User;
import com.personal.course.service.AuthService;
import com.personal.course.service.SessionService;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

public class AuthInterceptor implements HandlerInterceptor {
    public static String COOKIE_NAME = "COURSE_APP_SESSION_ID";

    private final SessionService sessionService;

    @Inject
    public AuthInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
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

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        // remove user info from ThreadLocal
        UserContext.removeUser();
    }
}
