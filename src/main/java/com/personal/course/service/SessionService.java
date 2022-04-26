package com.personal.course.service;

import com.personal.course.dao.SessionDao;
import com.personal.course.entity.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Optional;

@Service
@Transactional
public class SessionService {
    private final SessionDao sessionDao;

    @Inject
    public SessionService(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public Optional<Session> getSessionByCookie(String cookie) {
        return sessionDao.findByCookie(cookie);
    }

    public void save(Session session) {
        sessionDao.save(session);
    }

    public void deleteSessionByCookie(String cookie) {
        sessionDao.deleteByCookie(cookie);
    }

}
