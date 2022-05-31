package com.personal.course.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.personal.course.dao.RoleDao;
import com.personal.course.dao.UserDao;
import com.personal.course.entity.DO.Role;
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

@Service
public class AuthService {
    private final UserDao userDao;
    private final RoleDao roleDao;
    private final CookieService cookieService;

    @Inject
    public AuthService(UserDao userDao, RoleDao roleDao, CookieService cookieService) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.cookieService = cookieService;
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
        User user = userDao.findById(id).orElseThrow(() -> HttpException.notFound("无此用户"));
        return user;
    }

    public User login(UsernameAndPassword usernameAndPassword, HttpServletResponse response) {
        User userInDB = getUserByUsername(usernameAndPassword.getUsername());
        if (userInDB == null) {
            throw HttpException.notFound("该用户尚未注册！");
        }
        if (BCrypt.verifyer().verify(usernameAndPassword.getPassword().toCharArray(), userInDB.getEncrypted_password()).verified) {
            // 账号密码正确
            Cookie cookie = cookieService.generatorCookie(userInDB);
            response.addCookie(cookie);
            return userInDB;
        } else {
            throw HttpException.badRequest("密码错误！");
        }
    }
}
