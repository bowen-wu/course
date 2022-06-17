package com.personal.course.common.utils;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Schedule {
    private ScheduledThreadPoolExecutor scheduled;

    public Schedule() {
        this.scheduled = new ScheduledThreadPoolExecutor(1);
    }

    public void timer(Runnable command, long delay, TimeUnit timeUnit) {
        scheduled.schedule(command, delay, timeUnit);
    }

    /**
     * 停止定时器
     */
    public void stop() {
        if (scheduled != null) {
            scheduled.shutdownNow();
            scheduled = null;
        }
    }
}
