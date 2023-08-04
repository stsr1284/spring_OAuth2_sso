package com.cos.security.repository;

import com.cos.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository가 기본적인 CRUD 함수를 가지고 있는다.
// @Repository 어노테이션이 없어도 IoC가 된다. JpaRepository를 상속하기 떄문이다. -> bean 등록 된다.
public interface UserRepository extends JpaRepository<User, Integer> {
    // "extends JpaRepository<User, Integer" => Jpa에서 사용할 type = User로 설정, PK값 Int형 정의

    // findBy규칙 -> Username 문법
    // sql => select * from user where username = ? (=> ?에는 파라미터 값 들어간다.)
    // ex1)
    // public User findByEmail(String email) 이면
    // -> select * from user where email = ? 이 실행된다.
    public User findByUsername(String username); // JPA Quary Methods 검색하셤 ㄴ나옴

}