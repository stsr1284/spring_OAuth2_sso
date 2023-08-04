package com.cos.security.oauth;

import com.cos.security.auth.PrincipalDetails;
import com.cos.security.model.User;
import com.cos.security.provider.FacbookUserInfo;
import com.cos.security.provider.GoogleUserInfo;
import com.cos.security.provider.NaverUserInfo;
import com.cos.security.provider.OAuth2UserInfo;
import com.cos.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    // 구글로부터 받은 userRequest 데이터에 대한 후처리되는 함수이다.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("getClientRegistration: " + userRequest.getClientRegistration().getRegistrationId());
        // registrationId로 어떤 OAuth로 로그인 했는지 확인 가능하다.
        System.out.println("getAccessToken: " + userRequest.getAccessToken().getTokenValue());
        OAuth2User oauth2User = super.loadUser(userRequest);
        // 구글로그인 버튼 클릭 -> 로그인창 -> 로그인 완료 -> code를 리턴(OAuth-client라이브러리) -> AccessToekn 요청
        // 여기까지가 userRequest 정보 -> loadUser 함수 -> 구글로부터 회원 프로필을 받아준다.
        System.out.println("getAttributes: " + oauth2User.getAttributes());

        // 로그인 요청에 따른 provider 확인 및 자료 저장
        OAuth2UserInfo oAuth2UserInfo = null;
        if(userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            System.out.println("구글 로그인 요청!!");
            oAuth2UserInfo = new GoogleUserInfo(oauth2User.getAttributes());
        } else if(userRequest.getClientRegistration().getRegistrationId().equals("facebook")) {
            System.out.println("페이스북 로그인 요청!!");
            oAuth2UserInfo = new FacbookUserInfo(oauth2User.getAttributes());
        } else if(userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            System.out.println("네이버 로그인 요청!!");
            oAuth2UserInfo = new NaverUserInfo((Map<String, Object>) oauth2User.getAttributes().get("response"));
        } else {
            System.out.println("우리는 구글과 페이스북, 네이버만 지원한다.");
        }


        String provider = oAuth2UserInfo.getProvider();
        String providerId = oAuth2UserInfo.getProviderId();
        String username = provider + "_" + providerId;
        String password = bCryptPasswordEncoder.encode("겟인데어");
        String email = oAuth2UserInfo.getEmail();
        String role = "USER";
        // 강제 회원가입
//        String provider = userRequest.getClientRegistration().getRegistrationId(); // google
//        String providerId = oauth2User.getAttribute("sub");
//        String username = provider + "_" + providerId;
//        String password = bCryptPasswordEncoder.encode("겟인데어");
//        String email = oauth2User.getAttribute("email");
//        String role = "USER";

        User userEntity = userRepository.findByUsername(username);

        if (userEntity == null) {
            System.out.println("최초 로그인이네요!!");
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(userEntity);
        }
        else {
            System.out.println("로그인 한적 있다");
        }

        // 회원가입을 강제로 진행한다.
        return new PrincipalDetails(userEntity, oauth2User.getAttributes());
    }
}
