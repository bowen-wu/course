package com.personal.course.controller;

import com.personal.course.annotation.Admin;
import com.personal.course.dao.CustomConfigDao;
import com.personal.course.entity.DO.CustomConfig;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.Response;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class CustomConfigController {
    private final CustomConfigDao customConfigDao;

    @Inject
    public CustomConfigController(CustomConfigDao customConfigDao) {
        this.customConfigDao = customConfigDao;
    }

    /**
     * 更新配置
     *
     * @param customConfig 待更新的配置
     * @return 更新后的配置
     */
    @PatchMapping("/customConfig")
    @Admin
    public Response<CustomConfig> updateCustomConfig(@RequestBody CustomConfig customConfig) {
        CustomConfig customConfigInDb = customConfigDao.findByName(customConfig.getName()).orElseThrow(() -> {
            throw HttpException.notFound("在数据库 CUSTOM_CONFIG 表中没有" + customConfig.getName() + "配置");
        });
        customConfigInDb.setValue(customConfig.getValue());
        customConfigInDb.setUpdatedOn(Instant.now());
        return Response.success(customConfigDao.save(customConfigInDb));
    }
}
