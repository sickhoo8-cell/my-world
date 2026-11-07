
package com.myworld.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 아이디
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // 비밀번호
    @Column(nullable = false, length = 100)
    private String password;

    // 표시 이름
    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    // USER / ADMIN
    @Column(nullable = false, length = 20)
    private String role = "USER";

    // 가입일
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 기존 지역 정보
    @Column(name = "region", length = 100)
    private String region;

    /*
     * 신규 추가: 소속 부서
     *
     * 한 부서에 여러 회원이 소속될 수 있으므로
     * ManyToOne 관계로 연결한다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /*
     * 신규 추가: 직급
     * 예: 사원, 대리, 과장, 차장, 부장
     */
    @Column(name = "position", length = 50)
    private String position;

    /*
     * 신규 추가: 가입 승인 상태
     *
     * PENDING   : 승인 대기
     * ACTIVE    : 승인 완료
     * SUSPENDED : 이용 정지
     *
     * 기존 회원은 이 컬럼이 NULL일 수 있다.
     * 기존 회원 보호를 위해 우선 nullable로 둔다.
     */
    @Column(name = "account_status", length = 20)
    private String accountStatus;

    protected Member() {
    }

    // 기존 회원가입 생성자 유지
    public Member(
            String username,
            String password,
            String nickname
    ) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.role = "USER";

        // 앞으로 가입하는 회원은 승인 대기
        this.accountStatus = "PENDING";
    }

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (role == null || role.isBlank()) {
            role = "USER";
        }

        if (accountStatus == null ||
                accountStatus.isBlank()) {
            accountStatus = "PENDING";
        }
    }

    // =====================================
    // 기존 Getter
    // =====================================

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    // =====================================
    // 신규 Getter / Setter
    // =====================================

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    /*
     * 기존 회원 호환용
     *
     * 기존 DB 회원은 accountStatus가
     * NULL로 저장되어 있을 수 있다.
     *
     * NULL이면 기존 가입 회원으로 간주한다.
     */
    public boolean isApproved() {

        return accountStatus == null
                || "ACTIVE".equals(accountStatus);
    }

    public boolean isPending() {

        return "PENDING".equals(accountStatus);
    }

    public boolean isSuspended() {

        return "SUSPENDED".equals(accountStatus);
    }

    // =====================================
    // 관리자용 변경 메서드
    // =====================================

    public void approve(
            Department department,
            String position
    ) {
        this.department = department;
        this.position = position;
        this.accountStatus = "ACTIVE";
    }

    public void suspend() {
        this.accountStatus = "SUSPENDED";
    }

    public void changeDepartment(
            Department department
    ) {
        this.department = department;
    }

    public void changePosition(String position) {
        this.position = position;
    }

    /*
     * ADMIN 부여는 추후 관리자 서비스에서
     * 서버 권한을 검증한 후에만 호출할 것.
     */
    public void changeRole(String role) {

        if (!"USER".equals(role)
                && !"ADMIN".equals(role)) {
            throw new IllegalArgumentException(
                    "허용되지 않은 권한입니다."
            );
        }

        this.role = role;
    }
}
