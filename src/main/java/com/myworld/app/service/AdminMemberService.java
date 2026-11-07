
package com.myworld.app.service;

import com.myworld.app.entity.Department;
import com.myworld.app.entity.Member;
import com.myworld.app.repository.DepartmentRepository;
import com.myworld.app.repository.MemberRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminMemberService {

    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;

    public AdminMemberService(
            MemberRepository memberRepository,
            DepartmentRepository departmentRepository
    ) {
        this.memberRepository = memberRepository;
        this.departmentRepository = departmentRepository;
    }

    // =========================================
    // 관리자 권한 확인
    // =========================================

    @Transactional(readOnly = true)
    public void requireAdmin(Long loginMemberId) {

        if (loginMemberId == null) {
            throw new SecurityException(
                    "로그인이 필요합니다."
            );
        }

        Member admin = memberRepository
                .findById(loginMemberId)
                .orElseThrow(() ->
                        new SecurityException(
                                "유효하지 않은 계정입니다."
                        )
                );

        if (!"ADMIN".equals(admin.getRole())) {
            throw new SecurityException(
                    "관리자 권한이 없습니다."
            );
        }

        if (!admin.isApproved()) {
            throw new SecurityException(
                    "현재 이용할 수 없는 계정입니다."
            );
        }
    }

    // =========================================
    // 가입 대기 회원 목록
    // =========================================

    @Transactional(readOnly = true)
    public List<Member> getPendingMembers(
            Long loginMemberId
    ) {

        requireAdmin(loginMemberId);

        return memberRepository
                .findAll()
                .stream()
                .filter(Member::isPending)
                .toList();
    }

    // =========================================
    // 전체 회원 목록
    // =========================================

    @Transactional(readOnly = true)
    public List<Member> getAllMembers(
            Long loginMemberId
    ) {

        requireAdmin(loginMemberId);

        return memberRepository.findAll();
    }

    // =========================================
    // 회원 승인 + 부서 및 직급 배정
    // =========================================

    @Transactional
    public void approveMember(
            Long loginMemberId,
            Long memberId,
            Long departmentId,
            String position
    ) {

        requireAdmin(loginMemberId);

        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원을 찾을 수 없습니다."
                        )
                );

        if (!member.isPending()) {
            throw new IllegalArgumentException(
                    "승인 대기 중인 회원만 승인할 수 있습니다."
            );
        }

        Department department = departmentRepository
                .findById(departmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "부서를 찾을 수 없습니다."
                        )
                );

        if (!department.isActive()) {
            throw new IllegalArgumentException(
                    "사용하지 않는 부서입니다."
            );
        }

        String validPosition = validatePosition(position);

        // Member Entity에 만든 승인 메서드 사용
        member.approve(
                department,
                validPosition
        );
    }

    // =========================================
    // 직원 부서 변경
    // =========================================

    @Transactional
    public void changeDepartment(
            Long loginMemberId,
            Long memberId,
            Long departmentId
    ) {

        requireAdmin(loginMemberId);

        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원을 찾을 수 없습니다."
                        )
                );

        Department department = departmentRepository
                .findById(departmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "부서를 찾을 수 없습니다."
                        )
                );

        if (!department.isActive()) {
            throw new IllegalArgumentException(
                    "사용하지 않는 부서입니다."
            );
        }

        if (!member.isApproved()) {
            throw new IllegalArgumentException(
                    "승인된 회원만 부서를 변경할 수 있습니다."
            );
        }

        member.changeDepartment(department);
    }

    // =========================================
    // 직원 직급 변경
    // =========================================

    @Transactional
    public void changePosition(
            Long loginMemberId,
            Long memberId,
            String position
    ) {

        requireAdmin(loginMemberId);

        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원을 찾을 수 없습니다."
                        )
                );

        if (!member.isApproved()) {
            throw new IllegalArgumentException(
                    "승인된 회원만 직급을 변경할 수 있습니다."
            );
        }

        member.changePosition(
                validatePosition(position)
        );
    }

    // =========================================
    // 계정 이용 정지
    // =========================================

    @Transactional
    public void suspendMember(
            Long loginMemberId,
            Long memberId
    ) {

        requireAdmin(loginMemberId);

        if (loginMemberId.equals(memberId)) {
            throw new IllegalArgumentException(
                    "자신의 계정은 정지할 수 없습니다."
            );
        }

        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원을 찾을 수 없습니다."
                        )
                );

        // 관리자 계정의 임의 정지 방지
        if ("ADMIN".equals(member.getRole())) {
            throw new IllegalArgumentException(
                    "관리자 계정은 이 화면에서 정지할 수 없습니다."
            );
        }

        member.suspend();
    }

    // =========================================
    // 직급 입력값 검증
    // =========================================

    private String validatePosition(String position) {

        if (position == null || position.isBlank()) {
            throw new IllegalArgumentException(
                    "직급을 입력해주세요."
            );
        }

        String value = position.trim();

        if (value.length() > 50) {
            throw new IllegalArgumentException(
                    "직급은 50자 이내로 입력해주세요."
            );
        }

        return value;
    }
}
