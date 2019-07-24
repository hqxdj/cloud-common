package com.cloud.common.exception;

import com.cloud.common.bean.ResponseInfo;
import com.cloud.common.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@Slf4j
@ResponseBody
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 内部错误
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    public ResponseInfo handleException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return new ResponseInfo(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, "服务器内部错误");
    }

    /**
     * 参数错误
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(ParameterException.class)
    public ResponseInfo handleParameterException(ParameterException exception) {
        return new ResponseInfo(HttpStatus.BAD_REQUEST.value(), null, exception.getMessage());
    }

    /**
     * 参数验证错误
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseInfo handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        StringBuilder message = new StringBuilder();
        List<FieldError> fieldErrorList = exception.getBindingResult().getFieldErrors();
        for (FieldError fieldError : fieldErrorList) {
            message.append(fieldError.getDefaultMessage()).append(",");
        }
        message.deleteCharAt(message.length() - 1);
        return new ResponseInfo(HttpStatus.BAD_REQUEST.value(), null, message.toString());
    }

    /**
     * 错误请求
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseInfo handleNotReadableException() {
        return new ResponseInfo(HttpStatus.BAD_REQUEST.value(), null, "错误请求");
    }

    /**
     * 请求方法不支持
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseInfo handleRequestMethodNotSupportedException() {
        return new ResponseInfo(HttpStatus.BAD_REQUEST.value(), null, "请求方法不支持");
    }

    /**
     * 请求路径不存在
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseInfo handleNoHandlerFoundException() {
        return new ResponseInfo(HttpStatus.BAD_REQUEST.value(), null, "请求路径不存在");
    }

    /**
     * 业务逻辑错误
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(LogicException.class)
    public ResponseInfo handleLogicException(LogicException exception) {
        return new ResponseInfo(HttpStatus.NOT_ACCEPTABLE.value(), null, exception.getMessage());
    }

    /**
     * 业务异常
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BusinessException.class)
    public ResponseInfo handleBusinessException(BusinessException exception) {
        return ResultUtils.error(exception.getCode(), exception.getMessage());
    }

}