package com.opsguardian.copilot_backend.auth0security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/app.js",
                                "/static/**"
                        ).permitAll()   // ✅ allow UI

                        .requestMatchers("/api/**").authenticated()  // 🔐 protect APIs
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> {}));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(AudienceValidator audienceValidator) {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(
                "https://dev-frvjdwj3fq0gwfb7.us.auth0.com/"
        );

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(
                "https://dev-frvjdwj3fq0gwfb7.us.auth0.com/"
        );

        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(validator);

        return jwtDecoder;
    }
}
