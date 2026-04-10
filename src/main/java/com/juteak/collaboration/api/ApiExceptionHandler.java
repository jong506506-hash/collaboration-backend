package com.juteak.collaboration.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Keeps validation errors easy to read when teammates test the APIs manually.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidationException(MethodArgumentNotValidException exception) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		detail.setTitle("Validation failed");
		detail.setProperty(
			"errors",
			exception.getBindingResult().getFieldErrors().stream()
				.collect(java.util.stream.Collectors.toMap(
					FieldError::getField,
					FieldError::getDefaultMessage,
					(left, right) -> left,
					java.util.LinkedHashMap::new
				))
		);
		return detail;
	}
}
