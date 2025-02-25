package com.gamemoonchul.config;

import com.gamemoonchul.config.apple.AppleClientSecretGenerator;
import com.gamemoonchul.config.apple.OAuthRequestEntityConverter;
import com.gamemoonchul.config.jwt.JwtAuthorizationFilter;
import com.gamemoonchul.config.oauth.CustomAuthenticationEntryPoint;
import com.gamemoonchul.config.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.gamemoonchul.config.oauth.handler.OAuth2AuthenticationFailureHandler;
import com.gamemoonchul.config.oauth.handler.OAuth2AuthenticationSuccessHandler;
import com.gamemoonchul.config.oauth.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SpringSecurityConfig {
    private final CustomOAuth2UserService customOauth;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final AppleClientSecretGenerator appleClientSecretGenerator;
    private final CorsConfigurationSource corsConfig;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final List<String> SWAGGER = List.of("/swagger-ui.html", "/swagger-ui/**", "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs/**");
    //private List<String> EXCEPTION = List.of("/test/**");
    private final List<String> EXCEPTION = List.of("/open-api/**", "/actuator/**");

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(req -> {
                    // static Resource에 대해서는 모두 허가함
                    req.requestMatchers(
                                    PathRequest.toStaticResources()
                                            .atCommonLocations()
                            )
                            .permitAll()
                            .requestMatchers(SWAGGER.toArray(new String[0]))
                            .permitAll()
                            .requestMatchers(EXCEPTION.toArray(new String[0]))
                            .permitAll()
                            .requestMatchers("/api/**")
                            .hasAnyRole("USER")
                            .requestMatchers("/privacy/**")
                            .hasAnyRole("PRIVACY_NOT_AGREED", "USER")
                            .anyRequest()
                            .authenticated();
                })
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headersConfigurer -> headersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // For H2 DB
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // session 사용하지 않도록 설정
                .sessionManagement(sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(configure ->
                        configure.authorizationEndpoint(config -> config.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository))
                                .tokenEndpoint(config -> config.accessTokenResponseClient(accessTokenResponseClient()))
                                .userInfoEndpoint(config -> config.userService(customOauth))
                                .successHandler(oAuth2AuthenticationSuccessHandler)
                                .failureHandler(oAuth2AuthenticationFailureHandler)
                ).cors(corsCustomizer -> {
                    corsCustomizer.configurationSource(corsConfig);
                });

        /**
         * Exception 발생시 Redirect를 하지 않고 401을 반환
         */
        http.exceptionHandling(
                exceptionHandlingConfigurer -> exceptionHandlingConfigurer.authenticationEntryPoint(customAuthenticationEntryPoint)
        );

        http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
        accessTokenResponseClient.setRequestEntityConverter(new OAuthRequestEntityConverter(appleClientSecretGenerator));
        return accessTokenResponseClient;
    }
}
