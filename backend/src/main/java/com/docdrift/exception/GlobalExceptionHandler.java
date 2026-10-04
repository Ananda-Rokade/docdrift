package com.docdrift.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> badRequest(Exception e){return Map.of("status","error","message",e.getMessage());}
    @ExceptionHandler(Exception.class) @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String,String> error(Exception e){return Map.of("status","error","message",e.getMessage()==null?"The request could not be completed.":e.getMessage());}
}
