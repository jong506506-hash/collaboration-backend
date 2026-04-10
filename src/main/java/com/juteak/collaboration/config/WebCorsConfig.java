package com.juteak.collaboration.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows the local Vite frontend to call the backend during development without browser CORS blocks.
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

	private final String[] allowedOrigins;

	public WebCorsConfig(@Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173,http://127.0.0.1:5173}") String allowedOrigins) {
		this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
			.map(String::trim)
			.filter(value -> !value.isBlank())
			.toArray(String[]::new);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
			.allowedOrigins(allowedOrigins)
			.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
			.allowedHeaders("*");
	}
}
