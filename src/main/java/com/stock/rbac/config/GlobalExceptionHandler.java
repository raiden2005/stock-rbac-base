package com.stock.rbac.config;

import com.stock.rbac.exception.BusinessException;
import com.stock.rbac.exception.ForbiddenException;
import com.stock.rbac.exception.UnauthorizedException;
import com.stock.rbac.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMessage());
        return Result.error(405, "请求方法不支持，当前路径不允许使用 " + e.getMethod() + " 方法");
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Result<?> handleUnauthorized(UnauthorizedException e) {
        log.warn("未认证异常: {}", e.getMessage());
        return Result.error401(e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public Result<?> handleForbidden(ForbiddenException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error403(e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode() > 0 ? e.getCode() : 500, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() != null ?
                e.getBindingResult().getFieldError().getDefaultMessage() : "参数校验失败";
        log.warn("参数校验异常: {}", msg);
        return Result.error(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleAll(Exception e) {
        log.error("系统异常", e);
        return Result.error500("系统异常: " + e.getMessage());
    }
}
