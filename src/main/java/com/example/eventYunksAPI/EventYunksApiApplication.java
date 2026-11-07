package com.example.eventYunksAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.eventYunksAPI.security.JwtFilter;

@SpringBootApplication
public class EventYunksApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventYunksApiApplication.class, args);
	}
	// ================================================================
	//  Registro del Filtro JWT
	// ================================================================
	/**
	 * Registra el JwtFilter como un filtro activo en el servidor web.
	 *
	 * ¿Por qué usamos FilterRegistrationBean?
	 * Cuando anotamos una clase con @Component (como JwtFilter), Spring
	 * la detecta como un bean, pero necesitamos decirle explícitamente
	 * que es un filtro HTTP y a qué rutas debe aplicarse.
	 * FilterRegistrationBean es la forma estándar de hacerlo en Spring Boot.
	 *
	 * addUrlPatterns("/api/*") → el filtro se aplica a TODAS las rutas
	 * que empiecen con /api/. La lógica de cuáles son públicas y cuáles
	 * requieren token está dentro del propio JwtFilter.
	 *
	 * @param jwtFilter Spring inyecta el JwtFilter automáticamente (es un @Component)
	 * @return el FilterRegistrationBean configurado
	 */
	@Bean
	public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter jwtFilter) {
		FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(jwtFilter);    // el filtro a registrar
		registration.addUrlPatterns("/api/*"); // rutas donde se aplica
		return registration;
	}

	/**
	 * Configura y registra el filtro de CORS globalmente con la máxima prioridad.
	 * Esto asegura que las solicitudes preflight (OPTIONS) y los encabezados CORS
	 * se procesen antes de que cualquier otro filtro (como el filtro JWT) intervenga.
	 *
	 * @return el FilterRegistrationBean configurado para CORS
	 */
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOriginPattern("*"); // Permite todos los orígenes
		config.addAllowedHeader("*");        // Permite todas las cabeceras
		config.addAllowedMethod("*");        // Permite todos los métodos HTTP (GET, POST, PUT, DELETE, OPTIONS, etc.)
		source.registerCorsConfiguration("/**", config);

		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE); // Prioridad máxima
		return bean;
	}
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
