
"use strict";

/* ==========================================
   MY WORLD | MESSENGER

   1차 기능
   - 직원 검색
   - 직원 선택
   - 선택된 직원 정보 표시

   아직 구현하지 않은 기능
   - 메시지 전송
   - WebSocket 연결
   - 메시지 DB 저장
========================================== */

document.addEventListener("DOMContentLoaded", () => {

    const searchInput =
        document.getElementById("employeeSearch");

    const employeeItems =
        Array.from(
            document.querySelectorAll(".employee-item")
        );

    const noSearchResults =
        document.getElementById("noSearchResults");

    const selectedName =
        document.getElementById("selectedName");

    const selectedDepartment =
        document.getElementById("selectedDepartment");

    const selectedAvatar =
        document.getElementById("selectedAvatar");

    const conversationStatus =
        document.getElementById("conversationStatus");

    const emptyTitle =
        document.getElementById("conversationEmptyTitle");

    const emptyDescription =
        document.getElementById(
            "conversationEmptyDescription"
        );

    /* ======================================
       직원 검색
    ====================================== */

    function filterEmployees() {

        const keyword = searchInput.value
            .trim()
            .toLocaleLowerCase();

        let visibleCount = 0;

        employeeItems.forEach(item => {

            const name =
                item.dataset.name || "";

            const department =
                item.dataset.department || "";

            const position =
                item.dataset.position || "";

            const searchText =
                `${name} ${department} ${position}`
                    .toLocaleLowerCase();

            const matches =
                searchText.includes(keyword);

            item.hidden = !matches;

            if (matches) {
                visibleCount++;
            }
        });

        noSearchResults.hidden =
            visibleCount !== 0 ||
            employeeItems.length === 0;
    }

    searchInput.addEventListener(
        "input",
        filterEmployees
    );

    /* ======================================
       대화 상대 선택
    ====================================== */

    function selectEmployee(item) {

        employeeItems.forEach(employee => {
            employee.classList.remove("selected");
            employee.setAttribute(
                "aria-pressed",
                "false"
            );
        });

        item.classList.add("selected");

        item.setAttribute(
            "aria-pressed",
            "true"
        );

        const name =
            item.dataset.name || "직원";

        const department =
            item.dataset.department || "미배정";

        const position =
            item.dataset.position || "미배정";

        selectedName.textContent = name;

        selectedDepartment.textContent =
            `${department} · ${position}`;

        selectedAvatar.textContent =
            name.charAt(0) || "직";

        conversationStatus.textContent =
            "전송 기능 준비 중";

        emptyTitle.textContent =
            `${name}님과의 대화`;

        emptyDescription.textContent =
            "대화 상대가 선택되었습니다. " +
            "메시지 송수신 기능은 다음 개발 단계에서 연결됩니다.";
    }

    employeeItems.forEach(item => {

        item.setAttribute(
            "aria-pressed",
            "false"
        );

        item.addEventListener(
            "click",
            () => selectEmployee(item)
        );
    });

    // 현재 단계에서는 실제 메시지가
    // 전송되거나 DB에 저장되지 않는다.
});
