package com.personal.course.configuration;

import com.personal.course.entity.DO.Session;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.Whitelist;
import com.personal.course.service.AuthService;
import com.personal.course.service.CookieService;
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
    private final CookieService cookieService;
    private final AuthService authService;

    @Inject
    public AuthInterceptor(SessionService sessionService, CookieService cookieService, AuthService authService) {
        this.sessionService = sessionService;
        this.cookieService = cookieService;
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!request.getRequestURI().startsWith("/api/v1")) {
            return true;
        }

        // get User & save user info to ThreadLocal
        if (request.getCookies() != null) {
            if (request.getRequestURI().startsWith("/api/v1/session") && request.getMethod().equals("POST")) {
                // 如果用户带着 cookie 访问登录接口
                authService.deleteSession(request);
            } else {
                Arrays.stream(request.getCookies())
                        .filter(item -> item.getName().equals(COOKIE_NAME))
                        .map(Cookie::getValue)
                        .findFirst()
                        .map(cookieValue -> cookieService.updateCookieExpire(cookieValue, response))
                        .flatMap(sessionService::getSessionByCookie)
                        .map(Session::getUser)
                        .ifPresent(UserContext::setUser);
            }
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
                    .orElseThrow(() -> {
                        authService.deleteSession(request);
                        return HttpException.unauthorized("请登录!");
                    });
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        // remove user info from ThreadLocal
        UserContext.removeUser();
    }
}
