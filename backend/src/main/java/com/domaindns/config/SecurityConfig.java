package com.domaindns.config;

import com.domaindns.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService, StringRedisTemplate redis)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                            FilterChain filterChain) throws ServletException, IOException {
                        String auth = request.getHeader("Authorization");
                        if (auth != null && auth.startsWith("Bearer ")) {
                            String token = auth.substring(7);
                            try {
                                Jws<Claims> jws = jwtService.parse(token);
                                String jti = jws.getBody().getId();
                                if (jti != null) {
                                    String key = "jwt:blacklist:" + jti;
                                    String v = redis.opsForValue().get(key);
                                    if (v != null) {
                                        response.setStatus(401);
                                        response.setContentType("application/json;charset=UTF-8");
                                        response.getWriter().write("{\"code\":40101,\"message\":\"Token 已失效\"}");
                                        return;
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        filterChain.doFilter(request, response);
                    }
                }, AbstractPreAuthenticatedProcessingFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/api/auth/**").permitAll()
                        .anyRequest().permitAll() // TODO: 后续改为鉴权拦截
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
