package com.personal.course.service;

import com.personal.course.dao.RoleDao;
import com.personal.course.entity.DO.Role;
import com.personal.course.entity.DO.User;
import com.personal.course.entity.HttpException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoleService {
    private final RoleDao roleDao;

    @Inject
    public RoleService(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    public List<Role> getRoleEnum(User loggedUser) {
        if (loggedUser.getRoles().stream().map(Role::getName).noneMatch(roleName -> roleName.equals("admin"))) {
            throw HttpException.forbidden();
        }
        List<Role> target = new ArrayList<>();
        roleDao.findAll().forEach(target::add);
        return target;
    }

    public List<Role> getAllById(List<Integer> roleIds) {
        List<Role> target = new ArrayList<>();
        roleDao.findAllById(roleIds).forEach(target::add);
        return target;
    }
}
