package com.vqms.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle IllegalStateException (e.g., "No active counters available")
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalStateException(IllegalStateException ex, Model model) {
        logger.error("IllegalStateException occurred: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "error"; // Maps to a generic error page
    }

    // Handle general exceptions
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception ex, Model model) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        model.addAttribute("error", "An unexpected error occurred. Please try again later.");
        return "error";
    }
}
