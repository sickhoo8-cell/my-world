
package com.myworld.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(nullable = false, length = 20)
    private String role = "USER";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "region", length = 100)
    private String region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "position", length = 50)
    private String position;

    @Column(name = "account_status", length = 20)
    private String accountStatus;

    protected Member() {
    }

    public Member(
            String username,
            String password,
            String nickname
    ) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.role = "USER";
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

        if (accountStatus == null
                || accountStatus.isBlank()) {
            accountStatus = "PENDING";
        }
    }

    // =====================================
    // Getter / Setter
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

    // =====================================
    // 회원 상태 확인
    // =====================================

    public boolean isApproved() {
        // 기존 회원의 NULL 상태를 유지
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
    // 관리자용 회원 관리
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
        if (!isApproved()) {
            throw new IllegalArgumentException(
                    "승인된 회원만 정지할 수 있습니다."
            );
        }

        this.accountStatus = "SUSPENDED";
    }

    // 신규 추가: 정지 해제
    public void resume() {

        if (!isSuspended()) {
            throw new IllegalArgumentException(
                    "정지된 회원만 해제할 수 있습니다."
            );
        }

        this.accountStatus = "ACTIVE";
    }

    public void changeDepartment(
            Department department
    ) {
        this.department = department;
    }

    public void changePosition(String position) {
        this.position = position;
    }

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
