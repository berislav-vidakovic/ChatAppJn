package chatappjn.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import chatappjn.Common.PublicEndpoints;

@Configuration
public class SecurityConfig {

  private final JwtValidator jwtValidator;

  public SecurityConfig(JwtValidator jwtValidator) {
      this.jwtValidator = jwtValidator;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> {}) // enables CORS support
      .authorizeHttpRequests( auth -> {
            // Loop over public endpoints
            PublicEndpoints.ENDPOINTS.forEach(
              ep -> auth.requestMatchers(ep).permitAll());
            auth.anyRequest().authenticated();
        })
         // Register JwtValidator
      .addFilterBefore(jwtValidator, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
