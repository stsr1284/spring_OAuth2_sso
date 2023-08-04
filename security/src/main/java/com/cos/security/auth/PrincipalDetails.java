package com.cos.security.auth;

//시큐리티가 '/login' 주소 요청이 오면 낚아채서 로그인을 진행시킨다.
// -> 로그인을 진행이 완료되면 session을 만들어준다. (Security ContextHolder)라는 key값에 session정보를 저장한다.
// security session에 들어갈 오브젝트는 Authentication 객체로 type이 정해져있다.
// Authentication 안에 User 정보가 있어야 한다.
// User 오브젝트 타입은 UserDetails 타입 객체다.

import com.cos.security.model.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

// Security Session => Authentication => UserDetails type이어야 한다.
// UserDetails를 통해 User를 꺼내올 수 있다.
@Data
public class PrincipalDetails implements UserDetails, OAuth2User {

    private User user; // 콤포지션
    private Map<String, Object> attributes;

    // 일반 로그인 할 때
    public PrincipalDetails(User user) {
        this.user = user; // 생성자를 통해 User를 사용할 수 있도록 넣어준다.
    }

    // OAuth2 로그인 할 떄
    public PrincipalDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // 해당 User의 권한을 리턴하는 곳!
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return "ROLE_"+user.getRole();
            }
        });
        return collect;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() { // 비밀번호 오래사용하지 않았니?
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 사이트에서 1년동안 회원이 로그인을 안하면 휴먼 계정으로 하기로 함
        // 현재시간 - 로그인 시간 해서 1년 넘으면 false 처럼 할 수 있다,
        // -> db에 loing날짜를 넣어서 판단하여 비활성화 시킬 수 있다.
        return true;
    }

    @Override
    public String getName() {
        return null; // attributes.get("sub")
    }
}
