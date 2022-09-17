package com.example.demo.Handler;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.example.demo.Service.ResponseService;

import net.sf.json.JSONObject;

@RestControllerAdvice
public class CommomExceptionHandler {
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public JSONObject processValidException(MethodArgumentNotValidException ex) throws Exception {
        return ResponseService.responseError("99999", ex.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ExceptionHandler
    public JSONObject badRequestException(Exception ex) {
        return ResponseService.responseError("99999", ex.getMessage());
    }
}
