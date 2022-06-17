package com.personal.course.service;

import com.personal.course.dao.CustomConfigDao;
import com.personal.course.entity.DO.CustomConfig;
import com.personal.course.entity.DO.Session;
import com.personal.course.entity.DO.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static com.personal.course.configuration.AuthInterceptor.COOKIE_NAME;

@Service
public class CookieService {
    private static final Logger logger = LoggerFactory.getLogger(CookieService.class);

    private final CustomConfigDao customConfigDao;
    private final SessionService sessionService;

    @Inject
    public CookieService(CustomConfigDao customConfigDao, SessionService sessionService) {
        this.customConfigDao = customConfigDao;
        this.sessionService = sessionService;
    }

    public Cookie generatorCookie(User loginUser) {
        String cookieValue = UUID.randomUUID().toString();
        Cookie cookie = getCookie(cookieValue);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        Session session = new Session(cookieValue, loginUser);
        sessionService.deleteSessionByUserId(loginUser.getId());
        sessionService.save(session);
        return cookie;
    }

    public String updateCookieExpire(String cookieValue, HttpServletResponse response) {
        Cookie cookie = getCookie(cookieValue);
        response.addCookie(cookie);
        return cookieValue;
    }

    private Cookie getCookie(String cookieValue) {
        Cookie cookie = new Cookie(COOKIE_NAME, cookieValue);
        CustomConfig customConfig = customConfigDao.findByName("cookieMaxAge").orElseThrow(() -> {
            logger.error("在数据库 CUSTOM_CONFIG 表中没有 cookieMaxAge 配置");
            throw new RuntimeException("在数据库 CUSTOM_CONFIG 表中没有 cookieMaxAge 配置");
        });
        try {
            cookie.setMaxAge(Integer.parseInt(customConfig.getValue()));
        } catch (NumberFormatException e) {
            logger.warn("cookie max age 使用默认值 1800，30min");
            e.printStackTrace();
            // 默认值：30min
            cookie.setMaxAge(1800);
        }
        return cookie;
    }
}
