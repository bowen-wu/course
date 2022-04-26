package com.personal.course.service;

import com.personal.course.dao.UserDao;
import com.personal.course.entity.User;
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
        return userDao.save(registerUser);
    }

    public User getUserByUsername(String username) {
        return userDao.findByUsername(username);
    }
}
