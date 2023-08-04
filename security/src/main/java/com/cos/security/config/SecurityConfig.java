package com.cos.security.config;
// 구글 로그인이 완료된 후의 후처리가 필요하다. 1.코드받기(인중), 2.엑세스 토큰(권한),
// 3.사용자프로필 정보를 가져와서, 4-1.정보를 토대로 회원가입을 자동으로 진행시키기도 한다.
// 4-2. (이메일, 전화번호, 이름. 아이디) ex)쇼핑몰 -> (집주소), 백화점몰-> (vip등급, 일반 등급)


import com.cos.security.oauth.PrincipalOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity //스프링 시큐리티 필터가 스프링 필터체인에 등록 (스프링 필터 사용해봣쥬?)
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
// secured 어노테이션 활성화, preAuthorize 어노테이션 활성화
public class SecurityConfig{

    @Autowired
    private PrincipalOauth2UserService principalOauth2UserService;
//     @Bean: 해당 메서드의 리턴되는 오브젝트를 IoC로 등록해준다.
    @Bean
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.csrf().ignoringRequestMatchers(new AntPathRequestMatcher("/**/"));
        http.csrf().disable();
        http.authorizeHttpRequests()
                .requestMatchers("/user/**").authenticated()
                .requestMatchers("/manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
                .and()
                .   formLogin()
                    .loginPage("/loginForm")
                    .loginProcessingUrl("/login") // login주소가 호출되면 시큐리티가 낚아채서 대신 login을 진행한다.
                    // => security 기본 login을 사용하면 따로 안 만들어도 된다.
                    .defaultSuccessUrl("/") // 성공시 리다이렉
                .and()
                    .oauth2Login()
                    .loginPage("/loginForm")// 여기까지 6강
                    // Tip.코드X (액세스토큰 + 사용자프로필정보 O) -> Oauth2라이브러리가 해준다.
                    .userInfoEndpoint()
                    .userService(principalOauth2UserService)
        ;

        return http.build();
    }

    /*
    기존: WebSecurityConfigurerAdapter를 상속하고 configure매소드를 오버라이딩하여 설정하는 방법
    => 현재: SecurityFilterChain을 리턴하는 메소드를 빈에 등록하는 방식(컴포넌트 방식으로 컨테이너가 관리)
    //https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter

    @Override
    protected void configure(HttpSecurity http) throws  Exception{
        http.csrf().disable();
        http.authorizeRequests()
                .antMatchers("/user/**").authenticated()
                .antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
                .antMatchers("/admin").access("\"hasRole('ROLE_ADMIN')")
                .anyRequest().permitAll();
    }

     */
}
