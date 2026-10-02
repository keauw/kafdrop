package kafdrop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

  @Value("${kafdropAdmin.user}")
  private String username;

  @Value("${kafdropAdmin.protectedKey}")
  private String password;

  @Value("${kafdropAdmin.enabled:true}")
  private boolean adminEnabled;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    if (!adminEnabled) {
      return http
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
          .build();
    }

    return http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/actuator/**")
            .permitAll()
            .anyRequest()
            .authenticated())
        .httpBasic(withDefaults -> {})
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "kafdropAdmin.enabled", havingValue = "true", matchIfMissing = true)
  public UserDetailsService userDetailsService() {
    var user = User.builder()
        .username(username)
        .password("{noop}" + password)
        .roles("ADMIN")
        .build();
    return new InMemoryUserDetailsManager(user);
  }
}
