
package com.myworld.app.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "departments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_departments_code",
                        columnNames = "code"
                )
        }
)
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 부서 코드: DEV, HR, SALES 등
    @Column(
            name = "code",
            nullable = false,
            length = 30
    )
    private String code;

    // 부서명: 개발팀, 인사팀 등
    @Column(
            name = "name",
            nullable = false,
            length = 100
    )
    private String name;

    // 사용 여부
    @Column(nullable = false)
    private boolean active = true;

    protected Department() {
    }

    public Department(
            String code,
            String name
    ) {
        this.code = code;
        this.name = name;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
