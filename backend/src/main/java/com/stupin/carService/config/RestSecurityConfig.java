package com.stupin.carService.config;

import com.stupin.carService.domain.dto.UserImplUserDetails;
import com.stupin.carService.repository.UserImplUserDatailsRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
public class RestSecurityConfig {

    private final UserImplUserDatailsRepository userRepo;
    private final JwtTokenFilter jwtTokenFilter;

    public RestSecurityConfig(UserImplUserDatailsRepository userRepo, JwtTokenFilter jwtTokenFilter) {
        this.userRepo = userRepo;
        this.jwtTokenFilter = jwtTokenFilter;
    }

    // Add 2 users at start
    @EventListener(ApplicationReadyEvent.class)
    public void saveUser() {
        UserImplUserDetails user1 = new UserImplUserDetails("test1@gmail.com",
                getBcryptPasswordEncoder().encode("admin123"), "ROLE_USER");
        userRepo.save(user1);
        UserImplUserDetails user2 = new UserImplUserDetails("test2@gmail.com",
                getBcryptPasswordEncoder().encode("admin123"), "ROLE_ADMIN");
        userRepo.save(user2);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with email not found: " + username));
    }

    @Bean
    public PasswordEncoder getBcryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authorizationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues());
        http.authorizeRequests()
                .antMatchers("/rest/login").permitAll()
                .antMatchers("/rest/hello").hasRole("ADMIN")
                .anyRequest().authenticated();
        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}