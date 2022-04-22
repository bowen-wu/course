package com.personal.course.service;

import com.personal.course.dao.UserDao;
import com.personal.course.entity.User;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class AuthService {
    private final UserDao userDao;

    @Inject
    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<User> getAllUser() {
        return Streamable.of(userDao.findAll()).toList();
    }
}
