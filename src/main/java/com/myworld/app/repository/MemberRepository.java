
package com.myworld.app.repository;

import com.myworld.app.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository
        extends JpaRepository<Member, Long> {

    // 로그인 아이디로 회원 조회
    Optional<Member> findByUsername(String username);

    // 로그인 아이디 중복 확인
    boolean existsByUsername(String username);

    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);
}
