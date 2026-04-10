package com.microservice.auth.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class ConfigServiceExceptionHandler {

    // ================== CUSTOM BUSINESS EXCEPTIONS ==================
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ServiceResponse handleResourceNotFound(final ResourceNotFoundException exception) {
        return ServiceResponse.asFailure(ErrorCode.NOT_FOUND, exception.getLocalizedMessage());
    }

    @ExceptionHandler(ServiceAuthorizationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public @ResponseBody ServiceResponse handleServiceAuthorization(final ServiceAuthorizationException exception) {
        return ServiceResponse.asFailure(ErrorCode.UNAUTHORIZED, exception.getLocalizedMessage());
    }

    // ================== SPRING SECURITY ==================
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public @ResponseBody ServiceResponse handleAuthenticationException(final AuthenticationException exception) {
        log.warn("Authentication failed: {}", exception.getMessage());
        return ServiceResponse.asFailure(ErrorCode.UNAUTHORIZED,
                exception.getLocalizedMessage() != null ? exception.getLocalizedMessage() : "Unauthorized user");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public @ResponseBody ServiceResponse handleAccessDeniedException(final AccessDeniedException exception) {
        log.warn("Access denied: {}", exception.getMessage());
        return ServiceResponse.asFailure(ErrorCode.FORBIDDEN,
                exception.getLocalizedMessage() != null ? exception.getLocalizedMessage() : "Access forbidden");
    }

    // ================== VALIDATION ==================
    @ExceptionHandler(DataInputValidationException.class)
    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    public @ResponseBody ServiceResponse handleDataInputValidation(final DataInputValidationException exception) {
        return ServiceResponse.asFailure(ErrorCode.EXPECTATION_FAILED, exception.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public @ResponseBody ServiceResponse handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ServiceResponse.asFailure(ErrorCode.EXPECTATION_FAILED,
                errors.isEmpty() ? "Input validation failed" : errors);
    }

    // ================== OTHER COMMON ERRORS ==================
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public @ResponseBody ServiceResponse handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return ServiceResponse.asFailure(ErrorCode.METHOD_NOT_ALLOWED, exception.getLocalizedMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ServiceResponse handleNoHandlerFound(NoHandlerFoundException exception, HttpServletRequest request) {
        return ServiceResponse.asFailure(ErrorCode.NOT_FOUND,
                "No handler found for " + exception.getHttpMethod() + " " + request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ServiceResponse handleConstraintViolation(ConstraintViolationException ex) {
        return ServiceResponse.asFailure(ErrorCode.EXPECTATION_FAILED, ex.getMessage());
    }

    // Keep this if you call other microservices via RestTemplate/Feign and want to handle their 401
    @ExceptionHandler(Unauthorized.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public @ResponseBody ServiceResponse handleClientUnauthorized(final Unauthorized exception) {
        return ServiceResponse.asFailure(ErrorCode.UNAUTHORIZED, exception.getLocalizedMessage());
    }

    // ================== CATCH-ALL ==================
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ServiceResponse handleAnyException(final Exception exception) {
        log.error("Unhandled exception occurred", exception);
        return ServiceResponse.asFailure(ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getLocalizedMessage() != null ? exception.getLocalizedMessage() : "Internal server error");
    }
}