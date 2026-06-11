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

	// 1. 첫 번째 임베디드용 테스트 빈 (에러 방지를 위해 나머지 4개 필수 메서드 오버라이드 추가)
	@Bean
	public UserDetailsService userDetailsServiceEmbed(MemberRepository memberRepository) {
		return new UserDetailsService() {
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				Member member = memberRepository.findByEmail(username)
						.orElseThrow(() -> new UsernameNotFoundException("User not found"));
				
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

					// ⚠️ 아래 4개 메서드가 누락되어 컴파일 에러가 발생했었습니다. 기본값인 true로 채워줍니다.
					@Override
					public boolean isAccountNonExpired() { return true; }
					@Override
					public boolean isAccountNonLocked() { return true; }
					@Override
					public boolean isCredentialsNonExpired() { return true; }
					@Override
					public boolean isEnabled() { return true; }
				};
			}
		};
	}

	// 2. 두 번째 실제 동작용 UserDetailsService 빈
	// ⚠️ 주의: 스프링은 동일한 타입(UserDetailsService)의 빈이 2개 존재하면 구동할 때 충돌(NoUniqueBeanDefinitionException)이 발생할 수 있습니다.
	// 이 빈을 메인으로 쓰시려면 위 userDetailsServiceEmbed 메서드의 @Bean 어노테이션을 지우거나, 여기에 @Primary를 붙여주어야 안전합니다.
	@Bean
	public UserDetailsService userDetailsService(MemberRepository memberRepository, AuthorityRepository authorityRepository) {
		return new UserDetailsService() { 
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { 
				Member member = memberRepository.findByEmail(username)
						.orElseThrow(() -> new UsernameNotFoundException("User not found")); 
				List<Authority> authorities = authorityRepository.findByMember(member);
				return new MemberUser(member, authorities); 
			}
		}; 
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return new WebSecurityCustomizer() {
			@Override
			public void customize(WebSecurity web) {
				web.ignoring().requestMatchers( 
						"/css/**",
						"/js/**",
						"/image/**", 
						"/health/**", 
						"/actuator/**", 
						"/h2-console/**"
				);
			}
		};
	}

	@Bean 
	public FilterRegistrationBean<OpenEntityManagerInViewFilter> filterRegistration() { 
		FilterRegistrationBean<OpenEntityManagerInViewFilter> filterRegistrationBean = new FilterRegistrationBean<>(); 
		filterRegistrationBean.setFilter(new OpenEntityManagerInViewFilter()); 
		filterRegistrationBean.setOrder(Integer.MIN_