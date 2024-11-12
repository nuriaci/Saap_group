package es.storeapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                // Configurar reglas de seguridad
                http
                        .authorizeHttpRequests(authz -> authz
                                        // Permitir acceso sin autenticación a /actuator/health y /actuator/info
                                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                                        // Proteger el resto de los endpoints de actuator (incluido shutdown)
                                        .requestMatchers("/actuator/**").authenticated()
                                        // Permitir el acceso sin autenticación a otras partes de la app
                                        .anyRequest().permitAll())
                        // Autenticación básica para los endpoints que lo requieren
                        .httpBasic()
                        // Deshabilitar CSRF para simplificar
                        .and()
                        .csrf().disable();

                return http.build();
        }
 
    @Bean
    public UserDetailsService userDetailsService() {
        // Definir un usuario en memoria para autenticación básica
        return new InMemoryUserDetailsManager(
                User.withDefaultPasswordEncoder()
                        .username("admin")
                        .password("password")
                        .roles("ACTUATOR")
                        .build());
    }

}
