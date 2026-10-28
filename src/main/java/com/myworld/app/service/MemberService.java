
package com.myworld.app.service;

import com.myworld.app.dto.MemberSignupDTO;
import com.myworld.app.entity.Member;
import com.myworld.app.repository.MemberRepository;
import com.myworld.app.dto.MemberLoginDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void signup(MemberSignupDTO dto) {

        String username = dto.getUsername() == null
                ? ""
                : dto.getUsername().trim();

        String nickname = dto.getNickname() == null
                ? ""
                : dto.getNickname().trim();

        String password = dto.getPassword();
        String passwordConfirm = dto.getPasswordConfirm();

        // 1. 아이디 검사
        if (!username.matches("[a-zA-Z0-9]{4,20}")) {
            throw new IllegalArgumentException(
                    "아이디는 영문과 숫자 4~20자로 입력해주세요."
            );
        }

        // 2. 닉네임 검사
        if (nickname.isEmpty() || nickname.length() > 30) {
            throw new IllegalArgumentException(
                    "닉네임은 1~30자로 입력해주세요."
            );
        }

        // 3. 비밀번호 검사
        if (password == null
                || password.length() < 8
                || password.length() > 72) {
            throw new IllegalArgumentException(
                    "비밀번호는 8~72자로 입력해주세요."
            );
        }

        // 4. 비밀번호 확인
        if (!password.equals(passwordConfirm)) {
            throw new IllegalArgumentException(
                    "비밀번호가 일치하지 않습니다."
            );
        }

        // 5. 아이디 중복 확인
        if (memberRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 아이디입니다."
            );
        }

        // 6. 닉네임 중복 확인
        if (memberRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 닉네임입니다."
            );
        }

        // 7. 비밀번호 BCrypt 해싱
        String encodedPassword = passwordEncoder.encode(password);

        // 8. Member Entity 생성
        Member member = new Member(
                username,
                encodedPassword,
                nickname
        );

// 회원가입할 때 선택한 지역 저장
        String region = dto.getRegion();

        if (region == null || region.isBlank()) {
            region = "서울특별시";
        }

        member.setRegion(region);

// Neon PostgreSQL에 회원정보 저장
        memberRepository.save(member);
    }

    // 로그인 아이디와 비밀번호 확인
    @Transactional(readOnly = true)
    public Member login(MemberLoginDTO dto) {

        if (dto.getUsername() == null
                || dto.getPassword() == null) {
            throw new IllegalArgumentException(
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );
        }

        Member member = memberRepository
                .findByUsername(dto.getUsername().trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "아이디 또는 비밀번호가 올바르지 않습니다."
                        )
                );

        // 입력 비밀번호와 DB에 저장된 해시 비교
        boolean matches = passwordEncoder.matches(
                dto.getPassword(),
                member.getPassword()
        );

        if (!matches) {
            throw new IllegalArgumentException(
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );
        }

        return member;
    }


}
