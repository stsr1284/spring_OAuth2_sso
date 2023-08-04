package com.cos.security.controller;

import com.cos.security.auth.PrincipalDetails;
import com.cos.security.model.KakaoLoginRequest;
import com.cos.security.model.User;
import com.cos.security.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("test/login")
    public  @ResponseBody String testLogin(
            Authentication authentication,
            @AuthenticationPrincipal PrincipalDetails userDetails) { // DI(의존성 주입)
        System.out.println("/test/login =====================");
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        System.out.println("authentication: " + principalDetails.getUser());

        System.out.println("userDetails: " + userDetails.getUsername());

        return "세션 정보 확인하기";
    }

    @GetMapping("test/oauth/login")
    public  @ResponseBody String testOAuthLogin(
            Authentication authentication,
            @AuthenticationPrincipal OAuth2User userDetails) { // DI(의존성 주입)
        System.out.println("/test/login =====================");
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal(); // 다운캐스팅
        System.out.println("authentication: " + oAuth2User.getAttributes());
        System.out.println("OAuth2User: " + userDetails.getAttributes());

        return "세션 정보 확인하기";
    }


    @PostMapping("/test/oauth/kakao")
    public String kakaologin(@RequestBody KakaoLoginRequest request) {
        System.out.println("Access Token: " + request.getAccessToken());
        System.out.println("Access Token Expires At: " + request.getAccessTokenExpiresAt());
        System.out.println("Refresh Token: " + request.getRefreshToken());
        System.out.println("Refresh Token Expires At: " + request.getRefreshTokenExpiresAt());
        System.out.println("id: " + request.getId());
        System.out.println("Provider: " + request.getProvider());
        return "loginForm";
    }



    //localhost:8080/
    @GetMapping({"", "/"})
    public String index() {
        // 머스테치 기본폴더: src/main/resources/
        // 뷰 리졸버 설정: templates -> prefix로 설정, .mustache -> suffix로 설정
        // => application.yml 에서
        //    mvc:
        //      view:
        //        prefix: /templates/
        //        suffix: .mustache
        // 이렇게 설정을 해준다.
        // !!!! 하지만 의존성 설정을 해주면 기본적으로 설정해주기 때문에 삭제해도 된다..!

        return "index";
        // => //src/main/resources/templates/index.mustache 로 인식 한다.
    }

    @GetMapping("/user")
    public @ResponseBody String user(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        System.out.println("PrincipalDetails: " + principalDetails.getUser());
        return "user";
    }

    @GetMapping("/admin")
    public @ResponseBody String admin() {
        return "admin";
    }

    @GetMapping("/manager")
    public @ResponseBody String manager() {
        return "manager";
    }

    // security기본 /login이 인터셉터 한다.
    @GetMapping("/loginForm")
    public String loginForm() {
        return "loginForm";
    }

    @GetMapping("/joinForm") // 회원가입을 하기위한 form
    public String joinForm() {
        return "joinForm";
    }

    @PostMapping("/join") // 실제로 회원가입이 이루어진다.
    public String join(User user) {
        System.out.println(user);
        user.setRole("USER"); // 비밀번호가 1234 그대로 노출됨, 암호화가 안되었기 때문에 security로 회원가입 불가,
        String rawPassword = user.getPassword();
        String encPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encPassword);
        userRepository.save(user);
        // id랑 timestamp는 알아서 만들어진다.
        return "redirect:/loginForm";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/info")
    public @ResponseBody String info() {
        return "개인정보";
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/data")
    public @ResponseBody String data() {
        return "데이터 정보";
    }
}
