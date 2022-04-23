package com.personal.course.service;

import com.personal.course.dao.UserDao;
import com.personal.course.entity.User;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

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

    public User getUserById(Integer id) {
        Optional<User> byId = userDao.findById(id);
        if (byId.isPresent()) {
            return byId.get();
        }
        return null;
    }
}
