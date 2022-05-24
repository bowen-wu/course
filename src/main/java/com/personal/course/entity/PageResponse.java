package com.personal.course.entity;

import org.springframework.data.domain.Sort.Direction;

import java.util.List;

public class PageResponse<T> extends Response<List<T>> {
    public static int DEFAULT_PAGE_NUM = 1;
    public static int DEFAULT_PAGE_SIZE = 10;
    public static Direction DEFAULT_ORDER_BY = Direction.ASC;
    public static String DEFAULT_ORDER_TYPE = "id";

    private int pageNum;
    private int pageSize;
    private int total;

    public static <R> PageResponse<R> of(int pageNum, int pageSize, int total, String message, List<R> data) {
        return new PageResponse<>(pageNum, pageSize, total, message, data);
    }

    private PageResponse(int pageNum, int pageSize, int total, String message, List<T> data) {
        super(message, data);
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
    }

    private PageResponse() {
        super();
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
