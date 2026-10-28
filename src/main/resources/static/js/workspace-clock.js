
document.addEventListener("DOMContentLoaded", () => {

    const clockElement = document.getElementById("liveClock");
    const dateElement = document.getElementById("liveDate");
    const weekDatesElement = document.getElementById("weekDates");

    // 한국 시간 기준으로 현재 시각 표시
    function updateClock() {
        const now = new Date();

        if (clockElement) {
            clockElement.textContent =
                new Intl.DateTimeFormat("ko-KR", {
                    timeZone: "Asia/Seoul",
                    hour: "2-digit",
                    minute: "2-digit",
                    second: "2-digit",
                    hour12: false
                }).format(now);
        }

        if (dateElement) {
            dateElement.textContent =
                new Intl.DateTimeFormat("ko-KR", {
                    timeZone: "Asia/Seoul",
                    year: "numeric",
                    month: "long",
                    day: "numeric",
                    weekday: "long"
                }).format(now);
        }
    }

    // 이번 주 날짜 표시 (월요일 ~ 일요일)
    function updateWeekDates() {
        if (!weekDatesElement) return;

        const todayKst = new Intl.DateTimeFormat("en-CA", {
            timeZone: "Asia/Seoul",
            year: "numeric",
            month: "2-digit",
            day: "2-digit"
        }).format(new Date());

        const [year, month, day] =
            todayKst.split("-").map(Number);

        const today = new Date(
            Date.UTC(year, month - 1, day)
        );

        const dayOfWeek = today.getUTCDay();

        const mondayOffset = (dayOfWeek + 6) % 7;

        const monday = new Date(today);
        monday.setUTCDate(
            monday.getUTCDate() - mondayOffset
        );

        weekDatesElement.innerHTML = "";

        for (let i = 0; i < 7; i++) {
            const date = new Date(monday);
            date.setUTCDate(monday.getUTCDate() + i);

            const box = document.createElement("div");

            box.textContent = date.getUTCDate();

            if (
                date.getUTCFullYear() === year &&
                date.getUTCMonth() === month - 1 &&
                date.getUTCDate() === day
            ) {
                box.classList.add("today");
            }

            weekDatesElement.appendChild(box);
        }
    }

    updateClock();
    updateWeekDates();

    // 시계는 1초마다 업데이트
    setInterval(updateClock, 1000);

    // 자정이 지나도 달력이 갱신되도록
    setInterval(updateWeekDates, 60 * 1000);
});
