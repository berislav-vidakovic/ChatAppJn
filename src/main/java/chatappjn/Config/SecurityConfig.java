package chatappjn.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> {}) // enables CORS support
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/ping").permitAll()  
        .requestMatchers("/api/pingdb").permitAll()  
        .requestMatchers("/api/users/**").permitAll()
        .requestMatchers("/api/auth/refresh").permitAll()  
        .requestMatchers("/api/auth/login").permitAll()  
        .requestMatchers("/api/auth/logout").permitAll()  
        .requestMatchers("/api/chat/new").permitAll()  
        .requestMatchers("/websocket/**").permitAll() // allow WS handshake
        .anyRequest().authenticated()     // any other request requires auth
      );

    return http.build();
  }
}
