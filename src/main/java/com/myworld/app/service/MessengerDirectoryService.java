
package com.myworld.app.service;

import com.myworld.app.entity.Department;
import com.myworld.app.entity.Member;
import com.myworld.app.repository.MemberRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class MessengerDirectoryService {

    private final MemberRepository memberRepository;

    public MessengerDirectoryService(
            MemberRepository memberRepository
    ) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeView> getEmployees(Long loginMemberId) {

        if (loginMemberId == null) {
            throw new SecurityException(
                    "로그인이 필요합니다."
            );
        }

        Member loginMember = memberRepository
                .findById(loginMemberId)
                .orElseThrow(() ->
                        new SecurityException(
                                "유효하지 않은 회원입니다."
                        )
                );

        // 이용 정지 또는 승인 대기 회원 차단
        if (!loginMember.isApproved()) {
            throw new SecurityException(
                    "메신저를 사용할 수 없는 계정입니다."
            );
        }

        return memberRepository.findAll()
                .stream()
                .filter(Member::isApproved)

                // 본인은 대화 상대 목록에서 제외
                .filter(member ->
                        !member.getId().equals(loginMemberId)
                )

                .map(member -> {

                    Department department =
                            member.getDepartment();

                    String departmentName =
                            department == null
                                    ? "미배정"
                                    : department.getName();

                    String position =
                            member.getPosition();

                    if (position == null
                            || position.isBlank()) {
                        position = "미배정";
                    }

                    return new EmployeeView(
                            member.getId(),
                            member.getNickname(),
                            departmentName,
                            position
                    );
                })

                .sorted(
                        Comparator.comparing(
                                EmployeeView::departmentName
                        ).thenComparing(
                                EmployeeView::nickname
                        )
                )
                .toList();
    }

    // 화면에 전달할 정보만 정의
    // 비밀번호, 권한 변경 기능 등은 전달하지 않음
    public record EmployeeView(
            Long id,
            String nickname,
            String departmentName,
            String position
    ) {
    }
}
