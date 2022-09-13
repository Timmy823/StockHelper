package com.example.demo.Handler;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import net.sf.json.JSONObject;

@RestControllerAdvice
public class CommomExceptionHandler {
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public JSONObject processValidException(MethodArgumentNotValidException ex) throws Exception {
        return ResponseBody(ex.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ExceptionHandler
    public JSONObject badRequestException(Exception ex) {
        return ResponseBody(ex.getMessage());
    }

    private JSONObject ResponseBody(String message) {
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();
        data.put("data", "");

        status_code.put("status", "99999");
        status_code.put("desc", message);

        result.put("metadata", status_code);
        result.put("data", data);

        return result;
    }
}
