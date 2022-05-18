package com.personal.course.configuration;

import com.personal.course.entity.DO.User;

public class UserContext {
    private static ThreadLocal<User> currentUser = new ThreadLocal<>();

    public static void setUser(User user) {
        currentUser.set(user);
    }

    public static User getUser() {
        return currentUser.get();
    }

    public static void removeUser() {
        currentUser.remove();
    }
}
