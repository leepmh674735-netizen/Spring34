package com.example.demo.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// 프로젝트 내부의 엔티티 클래스(Member) 위치를 명시적으로 임포트해야 합니다.
// (java.lang.reflect.Member가 아니라 도메인 패키지의 Member여야 정상 작동합니다)
import com.example.demo.model.Member;
import com.example.demo.model.MemberUser;
import com.example.demo.model.Authority;
import com.example.demo.repository.AuthorityRepository;
import com.example.demo.repository.MemberRepository;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(withDefaults())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/", "/article/list", "/article/content").permitAll()
						.requestMatchers("/member/**").hasAuthority("ROLE_ADMIN")
						.requestMatchers("/signup").permitAll()
						.requestMatchers("/health").permitAll()
						.anyRequest().authenticated()
				)
				.httpBasic(withDefaults())
				.formLogin(form -> form.loginPage("/login").permitAll())
				.logout(logout -> logout.logoutSuccessUrl("/"));
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// 첫 번째 테스트용 임베디드 UserDetailsService 메서드 (오타 교정 및 Member 연결)
	public UserDetailsService userDetailsServiceEmbed(MemberRepository memberRepository) {
		return new UserDetailsService() {
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				Member member = memberRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
				return new UserDetails() {
					@Override
					public Collection<? extends GrantedAuthority> getAuthorities() {
						return List.of();
					}

					@Override
					public String getPassword() {
						return member.getPassword();
					}

					@Override
					public String getUsername() {
						return member.getEmail();
					}
				};
			}
		};
	}

	// 두 번째 실제 동작용 UserDetailsService 빈
	@Bean
	public UserDetailsService userDetailsService(MemberRepository memberRepository, AuthorityRepository authorityRepository) {
		return new UserDetailsService() { // ◀ UserDetilsService 오타 수정
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { // ◀ 메서드명 및 반환타입 오타 수정
				Member member = memberRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found")); // ◀ member 오타 수정
				List<Authority> authorities = authorityRepository.findByMember(member);
				return new MemberUser(member, authorities); 
			}
		}; // ◀ 닫는 세미콜론 괄호 추가
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return new WebSecurityCustomizer() {
			@Override
			public void customize(WebSecurity web) {
				web.ignoring().requestMatchers( // ◀ ingnoring, requestMathchers 오타 수정
						"/css/**",
						"/js/**",
						"/image/**", // ◀ 쉼표 누락 추가
						"/health/**", // ◀ 쉼표 누락 추가
						"/actuator/**", // ◀ 쉼표 누락 추가
						"/h2-console/**"
				);
			}
		};
	}

	@Bean // 주석을 해제하여 빈으로 정상 등록 가능하게 설정
	public FilterRegistrationBean<OpenEntityManagerInViewFilter> filterRegistration() { // ◀ 메서드명 정돈
		FilterRegistrationBean<OpenEntityManagerInViewFilter> filterRegistrationBean = new FilterRegistrationBean<>(); // ◀ 오타 수정
		filterRegistrationBean.setFilter(new OpenEntityManagerInViewFilter()); // ◀ 변수명 오타 수정
		filterRegistrationBean.setOrder(Integer.MIN_VALUE); // ◀ 변수명 오타 수정
		return filterRegistrationBean;
	}
}