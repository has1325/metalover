package com.example.metalover;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

@Configuration
public class SecurityConfig {

	private final DataSource dataSource;

	public SecurityConfig(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors(Customizer.withDefaults()) // CORS 활성화
				.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests(auth -> auth.requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
						.requestMatchers("/login", "/signup", "/api/**", "/h2-console/**").permitAll().anyRequest()
						.authenticated())
				.formLogin(form -> form.loginPage("/login") // GET /login 페이지
						.loginProcessingUrl("/login") // POST /login 처리
						.usernameParameter("email") // 폼 name="email"
						.passwordParameter("password") // 폼 name="password"
						.defaultSuccessUrl("/", true) // 성공 시 이동
						.failureUrl("/login?error") // 실패 시 이동
						.permitAll())

				// 로그아웃
				.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout")
						.deleteCookies("JSESSIONID").invalidateHttpSession(true).permitAll())

				// Remember-Me (체크박스 name="remember-me")
				.rememberMe(r -> r.rememberMeParameter("remember-me").tokenValiditySeconds(60 * 60 * 24 * 14) // 14일
				)

				// 기본 CSRF 활성 (Thymeleaf 폼에서 토큰 전송)
				.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**")) // 필요 시
		;
		return http.build();
	}

	@Bean
	public JdbcTokenRepositoryImpl persistentTokenRepository() {
		JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
		repo.setDataSource(dataSource);
		return repo;
	}

	// Render 환경변수 FRONTEND_ORIGIN과 연동
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		String frontendOrigin = System.getenv("FRONTEND_ORIGIN");
		if (frontendOrigin == null) {
			frontendOrigin = "https://metalover.kr"; // 기본값
		}
		config.setAllowedOrigins(List.of(frontendOrigin));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
