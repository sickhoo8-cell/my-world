
package com.myworld.app.service;

import com.myworld.app.entity.Member;
import com.myworld.app.repository.MemberRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyPageService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    public MyPageService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================
    // 회원 본인 정보 수정
    // =========================================

    @Transactional
    public void updateProfile(
            Long memberId,
            String nickname,
            String region
    ) {

        Member member = getActiveMember(memberId);

        String validNickname = nickname == null
                ? ""
                : nickname.trim();

        String validRegion = region == null
                ? ""
                : region.trim();

        // 닉네임 검증
        if (validNickname.isBlank()
                || validNickname.length() > 30) {

            throw new IllegalArgumentException(
                    "닉네임은 1~30자로 입력해주세요."
            );
        }

        // 지역 검증
        if (validRegion.isBlank()
                || validRegion.length() > 100) {

            throw new IllegalArgumentException(
                    "지역은 1~100자로 입력해주세요."
            );
        }

        // 다른 회원과 닉네임 중복 확인
        Long duplicateCount = entityManager
                .createQuery(
                        "SELECT COUNT(m) FROM Member m " +
                                "WHERE m.nickname = :nickname " +
                                "AND m.id <> :memberId",
                        Long.class
                )
                .setParameter("nickname", validNickname)
                .setParameter("memberId", memberId)
                .getSingleResult();

        if (duplicateCount > 0) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 닉네임입니다."
            );
        }

        // 본인 닉네임 및 지역만 수정
        entityManager.createQuery(
                        "UPDATE Member m " +
                                "SET m.nickname = :nickname, " +
                                "m.region = :region " +
                                "WHERE m.id = :memberId"
                )
                .setParameter("nickname", validNickname)
                .setParameter("region", validRegion)
                .setParameter("memberId", memberId)
                .executeUpdate();

        entityManager.clear();
    }

    // =========================================
    // 비밀번호 변경
    // =========================================

    @Transactional
    public void changePassword(
            Long memberId,
            String currentPassword,
            String newPassword,
            String passwordConfirm
    ) {

        Member member = getActiveMember(memberId);

        // 현재 비밀번호 확인
        if (currentPassword == null
                || !passwordEncoder.matches(
                currentPassword,
                member.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "현재 비밀번호가 올바르지 않습니다."
            );
        }

        // 새 비밀번호 검사
        if (newPassword == null
                || newPassword.length() < 8
                || newPassword.length() > 72) {

            throw new IllegalArgumentException(
                    "새 비밀번호는 8~72자로 입력해주세요."
            );
        }

        // 비밀번호 확인
        if (!newPassword.equals(passwordConfirm)) {
            throw new IllegalArgumentException(
                    "새 비밀번호가 일치하지 않습니다."
            );
        }

        // 기존 비밀번호와 동일한지 확인
        if (passwordEncoder.matches(
                newPassword,
                member.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "기존 비밀번호와 다른 비밀번호를 입력해주세요."
            );
        }

        // BCrypt 암호화
        String encodedPassword =
                passwordEncoder.encode(newPassword);

        // DB 비밀번호 변경
        entityManager.createQuery(
                        "UPDATE Member m " +
                                "SET m.password = :password " +
                                "WHERE m.id = :memberId"
                )
                .setParameter("password", encodedPassword)
                .setParameter("memberId", memberId)
                .executeUpdate();

        entityManager.clear();
    }

    // =========================================
    // 회원 상태 확인
    // =========================================

    private Member getActiveMember(Long memberId) {

        if (memberId == null) {
            throw new SecurityException(
                    "로그인이 필요합니다."
            );
        }

        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new SecurityException(
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        if (!member.isApproved()) {
            throw new SecurityException(
                    "현재 이용할 수 없는 계정입니다."
            );
        }

        return member;
    }
}
