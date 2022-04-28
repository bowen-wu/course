package com.personal.course.service;

import com.personal.course.dao.RoleDao;
import com.personal.course.dao.UserDao;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.Role;
import com.personal.course.entity.User;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class UserService {
    private final UserDao userDao;
    private final AuthService authService;
    private final RoleDao roleDao;

    @Inject
    public UserService(UserDao userDao, AuthService authService, RoleDao roleDao) {
        this.userDao = userDao;
        this.authService = authService;
        this.roleDao = roleDao;
    }

    public User updateUserRole(Integer userId, List<Role> roles) {
        // 更新 Users 表，JPA是级联更新，通过 Hibernate ORM 框架实现，它会检查 users 和 users 下的 role 对象的变化，有变化更新的时候会一起更新 DB
        User userInDb = authService.getUserById(userId);
        if (userInDb == null) {
            throw HttpException.notFound("请检查用户ID");
        }

        List<Role> roleList = roles.stream()
                .map(Role::getName)
                .map(roleDao::findByName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        userInDb.setRoles(roleList);
        userDao.save(userInDb);

        return userInDb;
    }
}
