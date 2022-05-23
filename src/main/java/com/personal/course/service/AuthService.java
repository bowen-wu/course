package com.personal.course.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.personal.course.dao.CustomConfigDao;
import com.personal.course.dao.RoleDao;
import com.personal.course.dao.UserDao;
import com.personal.course.entity.DO.CustomConfig;
import com.personal.course.entity.DO.Role;
import com.personal.course.entity.DO.Session;
import com.personal.course.entity.DO.User;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.VO.UsernameAndPassword;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

import static com.personal.course.configuration.AuthInterceptor.COOKIE_NAME;

@Service
public class AuthService {
    private final UserDao userDao;
    private final RoleDao roleDao;
    private final SessionService sessionService;
    private final CustomConfigDao customConfigDao;

    @Inject
    public AuthService(UserDao userDao, RoleDao roleDao, SessionService sessionService, CustomConfigDao customConfigDao) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.sessionService = sessionService;
        this.customConfigDao = customConfigDao;
    }

    public User registerUser(UsernameAndPassword usernameAndPassword) {
        String encryptedPassword = BCrypt.withDefaults().hashToString(12, usernameAndPassword.getPassword().toCharArray());
        User registerUser = new User();
        registerUser.setUsername(usernameAndPassword.getUsername());
        registerUser.setEncrypted_password(encryptedPassword);
        Role studentRole = roleDao.findByName("student").orElseThrow(() -> {
            throw new RuntimeException("未找到角色名为 student 的角色");
        });
        registerUser.setRoles(List.of(studentRole));
        try {
            return userDao.save(registerUser);
        } catch (DataIntegrityViolationException e) {
            throw HttpException.of(HttpStatus.CONFLICT, "用户名已经被注册！");
        }
    }

    public User getUserByUsername(String username) {
        return userDao.findByUsername(username);
    }

    public User getUserById(Integer id) {
        return userDao.findById(id).orElseThrow(() -> HttpException.notFound("无此用户"));
    }

    public User login(UsernameAndPassword usernameAndPassword, HttpServletResponse response) {
        User userInDB = getUserByUsername(usernameAndPassword.getUsername());
        if (userInDB == null) {
            throw HttpException.notFound("该用户尚未注册！");
        }
        if (BCrypt.verifyer().verify(usernameAndPassword.getPassword().toCharArray(), userInDB.getEncrypted_password()).verified) {
            // 账号密码正确
            String cookieValue = UUID.randomUUID().toString();
            Cookie cookie = new Cookie(COOKIE_NAME, cookieValue);
            CustomConfig customConfig = customConfigDao.findByName("cookieMaxAge").orElseThrow(() -> {
                // TODO: 配置报错信息
                throw new RuntimeException("在数据库 CUSTOM_CONFIG 表中没有 cookieMaxAge 配置");
            });
            try {
                cookie.setMaxAge(Integer.parseInt(customConfig.getValue()));
            } catch (NumberFormatException e) {
                // TODO: 配置报错信息
                e.printStackTrace();
                // 默认值：30min
                cookie.setMaxAge(1800);
            }
            response.addCookie(cookie);
            Session session = new Session(cookieValue, userInDB);
            sessionService.deleteSessionByUserId(userInDB.getId());
            sessionService.save(session);
            return userInDB;
        } else {
            throw HttpException.badRequest("密码错误！");
        }
    }
}
