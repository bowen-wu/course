package com.personal.course.service;

import com.personal.course.dao.SessionDao;
import com.personal.course.entity.Session;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Optional;

@Service
public class SessionService {
    private final SessionDao sessionDao;

    @Inject
    public SessionService(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public Optional<Session> getSessionByCookie(String cookie) {
        return sessionDao.findByCookie(cookie);
    }
}
