package com.nicolas.bankingws.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Este proyecto usa spring-boot-starter-webservices, no
 * spring-boot-starter-web — no hay un WebMvcConfigurer con
 * addCorsMappings disponible como en los otros dos backends. Acá CORS se
 * resuelve un nivel más abajo, con un Filter de servlet puro (CorsFilter,
 * de spring-web) registrado con máxima precedencia, para que también
 * conteste bien el preflight OPTIONS que manda el navegador antes del
 * POST real — sin esto, ese OPTIONS terminaría cayendo en el
 * MessageDispatcherServlet, que no sabe qué hacer con un método que no es
 * un mensaje SOAP.
 */
@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "SOAPAction"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/ws/**", configuration);

        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(source));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
