package fr.uha.ensisa.ff.todo_auto.config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
@ComponentScan("fr.uha.ensisa.ff.todo_auto.config")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthProvider authProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
    	String idForEncode = "bcrypt";
    	Map<String, PasswordEncoder> encoders = new TreeMap<>();
    	encoders.put(idForEncode, new BCryptPasswordEncoder());

    	return new DelegatingPasswordEncoder(idForEncode, encoders);
    }

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		auth.authenticationProvider(authProvider);
	}

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
          .csrf().disable()
          .authorizeHttpRequests()
          .requestMatchers("/api/**").authenticated()
          .anyRequest().permitAll()
          .and()
          .formLogin()
          .loginPage("/login")
          .loginProcessingUrl("/perform_login")
          .defaultSuccessUrl("/", true)
          .failureHandler(new AuthenticationFailureHandler() {

            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                    AuthenticationException exception) throws IOException, ServletException {
                String error = exception.getMessage();
                try {
                    error = URLEncoder.encode(error, "UTF-8");
                } catch (UnsupportedEncodingException ee) {
                    ee.printStackTrace();
                }
                response.sendRedirect("/login?error=" + error);
            }
            
          })
          .and()
          .logout()
          .logoutUrl("/perform_logout")
          .deleteCookies("JSESSIONID")
          ;
        http.exceptionHandling().authenticationEntryPoint(new RestAuthenticationEntryPoint());
    }
}
