package com.personal.course.controller;

import com.personal.course.entity.HttpException;
import com.personal.course.entity.Response;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class ErrorHandlingController {
    @ExceptionHandler(value = HttpException.class)
    @ResponseBody
    public Response<?> defaultErrorHandler(HttpServletResponse response, HttpException exception) throws IOException {
        response.setStatus(exception.getStatusCode().value());
        return Response.of(exception.getMessage(), null);
    }
}
