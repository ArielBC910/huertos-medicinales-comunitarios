package ec.huertos.security;

import ec.huertos.persistence.AccountRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService users(AccountRepository accounts) {
    return username ->
        accounts
            .findByUsername(username)
            .filter(a -> a.active)
            .map(a -> User.withUsername(a.username).password(a.password).roles(a.role).build())
            .orElseThrow(() -> new UsernameNotFoundException("Cuenta no disponible"));
  }

  @Bean
  @Profile("!provision")
  SecurityFilterChain filter(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/",
                        "/login",
                        "/css/**",
                        "/js/**",
                        "/vendor/**",
                        "/images/**",
                        "/comunidades/**",
                        "/plantas",
                        "/mapa",
                        "/ranking",
                        "/calendario",
                        "/error")
                    .permitAll()
                    .requestMatchers("/gestion/**")
                    .hasRole("GESTOR")
                    .requestMatchers("/revision/**")
                    .hasRole("REVISOR")
                    .anyRequest()
                    .authenticated())
        .headers(
            h ->
                h.referrerPolicy(
                    r ->
                        r.policy(
                            org.springframework.security.web.header.writers
                                .ReferrerPolicyHeaderWriter.ReferrerPolicy
                                .STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
        .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
        .logout(logout -> logout.logoutSuccessUrl("/"))
        .build();
  }
}
