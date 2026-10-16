package dss.example.pr1.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos
                .requestMatchers("/", "/index", "/index.html", "/login", "/error",
                                 "/css/**", "/js/**", "/images/**", "/webjars/**",
                                 "/uploads/**").permitAll()
                // Consola H2 (solo desarrollo)
                .requestMatchers(PathRequest.toH2Console()).permitAll()
                // Catálogo: solo la consulta es pública
                .requestMatchers(HttpMethod.GET, "/products").permitAll()
                // Zona de administración
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                // Gestión del catálogo: solo ADMIN
                .requestMatchers("/products/add", "/products/edit/**",
                                 "/products/delete/**", "/products/save").hasRole("ADMIN")
                // Carrito: público, independiente por sesión
                .requestMatchers("/cart", "/cart/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/products", false)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(PathRequest.toH2Console())
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );
        return http.build();
    }

    // Usuarios de demostración del enunciado (solo para uso local).
    // {noop} = contraseña sin cifrar; en una aplicación real usar un PasswordEncoder seguro.
    @Bean
    public UserDetailsService users() {
        UserDetails admin = User.withUsername("admin")
                .password("{noop}admin").roles("ADMIN").build();
        UserDetails user = User.withUsername("user")
                .password("{noop}user").roles("USER").build();
        return new InMemoryUserDetailsManager(admin, user);
    }
}
