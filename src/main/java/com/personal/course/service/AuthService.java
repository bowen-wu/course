package com.personal.course.service;

import com.personal.course.dao.UserDao;
import com.personal.course.entity.DO.User;
import com.personal.course.entity.HttpException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AuthService {
    private final UserDao userDao;

    @Inject
    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User registerUser(User registerUser) {
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
}
