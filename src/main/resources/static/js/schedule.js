
"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const $ = (id) => document.getElementById(id);
    const dates = $("scheduleWeekDates");
    const heading = $("scheduleWeekHeading");
    const list = $("scheduleList");
    const status = $("scheduleStatus");
    const form = $("scheduleForm");
    const title = $("scheduleTitle");
    const description = $("scheduleDescription");
    const eventDate = $("scheduleDate");
    const eventTime = $("scheduleTime");
    const editId = $("scheduleId");
    const cancel = $("scheduleCancel");
    const save = $("scheduleSave");

    if (!dates || !form) return;

    let selectedDate = todayString();
    let weekStart = mondayOf(parseLocalDate(selectedDate));
    let schedules = [];
    let loading = false;

    function dateString(date) {
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
    }

    function parseLocalDate(value) {
        const [year, month, day] = value.split("-").map(Number);
        return new Date(year, month - 1, day, 12);
    }

    function todayString() {
        return dateString(new Date());
    }

    function addDays(date, days) {
        const copy = new Date(date);
        copy.setDate(copy.getDate() + days);
        return copy;
    }

    function mondayOf(date) {
        const day = (date.getDay() + 6) % 7;
        return addDays(date, -day);
    }

    function textNode(tag, className, text) {
        const item = document.createElement(tag);
        if (className) item.className = className;
        if (text !== undefined) item.textContent = text;
        return item;
    }

    function tell(message, isError = false) {
        status.textContent = message;
        status.classList.toggle("schedule-error", isError);
    }

    async function api(url, options = {}) {
        const res = await fetch(url, {
            credentials: "same-origin",
            cache: "no-store",
            ...options
        });

        if (res.status === 401) {
            window.location.replace("/login");
            throw new Error("로그인이 만료되었습니다.");
        }

        if (!res.ok) {
            throw new Error(`일정 처리 실패 (${res.status})`);
        }

        return res.status === 204 ? null : res.json();
    }

    function jsonOptions(method, data) {
        return {
            method,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        };
    }

    function drawWeek() {
        dates.replaceChildren();
        const last = addDays(weekStart, 6);

        heading.textContent =
            `${weekStart.getFullYear()}년 ${weekStart.getMonth() + 1}월 ${weekStart.getDate()}일 ~ ${last.getMonth() + 1}월 ${last.getDate()}일`;

        for (let i = 0; i < 7; i++) {
            const day = addDays(weekStart, i);
            const date = dateString(day);

            const count = schedules.filter(
                s => s.eventDate === date
            ).length;

            const button = textNode(
                "button",
                "schedule-day",
                String(day.getDate())
            );

            button.type = "button";

            if (date === selectedDate) {
                button.classList.add("selected");
            }

            if (date === todayString()) {
                button.classList.add("today");
            }

            if (count > 0) {
                button.appendChild(
                    textNode("span", "schedule-day-count", String(count))
                );
            }

            button.setAttribute(
                "aria-label",
                `${date}, 일정 ${count}개`
            );

            button.setAttribute(
                "aria-pressed",
                String(date === selectedDate)
            );

            button.addEventListener("click", () => {
                selectedDate = date;

                if (!editId.value) {
                    eventDate.value = date;
                }

                drawWeek();
                drawList();
            });

            dates.appendChild(button);
        }
    }

    function resetEditor() {
        form.reset();
        editId.value = "";
        eventDate.value = selectedDate;
        save.textContent = "♡ 일정 저장";
        cancel.hidden = true;
    }

    function drawList() {
        list.replaceChildren();

        $("scheduleSelectedDate").textContent =
            `${selectedDate} 일정`;

        const daily = schedules.filter(
            s => s.eventDate === selectedDate
        );

        if (daily.length === 0) {
            list.appendChild(
                textNode(
                    "p",
                    "schedule-empty",
                    "선택한 날짜에 등록된 일정이 없어요 ♡"
                )
            );
            return;
        }

        for (const schedule of daily) {
            const item = textNode("article", "schedule-item");

            const name = textNode(
                "strong",
                "schedule-item-title",
                schedule.title
            );

            const time = textNode(
                "span",
                "schedule-item-time",
                schedule.eventTime
                    ? `🕒 ${schedule.eventTime.slice(0, 5)}`
                    : "종일"
            );

            const top = textNode("div", "schedule-item-top");
            top.append(name, time);
            item.appendChild(top);

            if (schedule.description) {
                item.appendChild(
                    textNode(
                        "p",
                        "schedule-item-description",
                        schedule.description
                    )
                );
            }

            const actions = textNode(
                "div",
                "schedule-item-actions"
            );

            const edit = textNode(
                "button",
                "schedule-edit",
                "✏️ 수정"
            );

            edit.type = "button";

            edit.addEventListener("click", () => {
                editId.value = String(schedule.id);
                title.value = schedule.title;
                description.value = schedule.description || "";
                eventDate.value = schedule.eventDate;

                eventTime.value = schedule.eventTime
                    ? schedule.eventTime.slice(0, 5)
                    : "";

                save.textContent = "♡ 수정 완료";
                cancel.hidden = false;
                title.focus();
            });

            const del = textNode(
                "button",
                "schedule-delete",
                "🗑️ 삭제"
            );

            del.type = "button";

            del.addEventListener("click", async () => {
                if (loading || !window.confirm("이 일정을 삭제할까요?")) {
                    return;
                }

                loading = true;

                try {
                    await api(
                        `/api/schedules/${schedule.id}`,
                        { method: "DELETE" }
                    );

                    schedules = schedules.filter(
                        s => s.id !== schedule.id
                    );

                    if (editId.value === String(schedule.id)) {
                        resetEditor();
                    }

                    drawWeek();
                    drawList();
                    tell("일정을 삭제했어요.");
                } catch (e) {
                    tell(e.message, true);
                } finally {
                    loading = false;
                }
            });

            actions.append(edit, del);
            item.appendChild(actions);
            list.appendChild(item);
        }
    }

    async function loadWeek() {
        if (loading) return;

        loading = true;

        const from = dateString(weekStart);
        const to = dateString(addDays(weekStart, 6));

        tell("일정을 불러오는 중...");

        try {
            schedules = await api(
                `/api/schedules?from=${from}&to=${to}`
            );

            drawWeek();
            drawList();
            tell("");
        } catch (e) {
            schedules = [];
            drawWeek();
            drawList();
            tell(e.message, true);
        } finally {
            loading = false;
        }
    }

    $("schedulePrev").addEventListener("click", () => {
        weekStart = addDays(weekStart, -7);
        selectedDate = dateString(weekStart);
        resetEditor();
        loadWeek();
    });

    $("scheduleNext").addEventListener("click", () => {
        weekStart = addDays(weekStart, 7);
        selectedDate = dateString(weekStart);
        resetEditor();
        loadWeek();
    });

    $("scheduleToday").addEventListener("click", () => {
        selectedDate = todayString();
        weekStart = mondayOf(parseLocalDate(selectedDate));
        resetEditor();
        loadWeek();
    });

    cancel.addEventListener("click", () => {
        resetEditor();
        tell("");
    });

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        if (loading) return;

        const name = title.value.trim();

        if (!name || name.length > 120) {
            tell(
                "일정 제목은 1~120자로 입력해 주세요.",
                true
            );
            return;
        }

        if (!eventDate.value) {
            tell("날짜를 선택해 주세요.", true);
            return;
        }

        const id = editId.value;
        const editing = !!id;

        const payload = {
            title: name,
            description: description.value.trim(),
            eventDate: eventDate.value,
            eventTime: eventTime.value || null
        };

        loading = true;
        save.disabled = true;
        tell("저장하는 중...");

        try {
            await api(
                editing
                    ? `/api/schedules/${id}`
                    : "/api/schedules",
                jsonOptions(editing ? "PUT" : "POST", payload)
            );

            selectedDate = eventDate.value;
            weekStart = mondayOf(
                parseLocalDate(selectedDate)
            );

            resetEditor();

            loading = false;
            await loadWeek();

            tell(
                editing
                    ? "일정을 수정했어요 ♡"
                    : "일정을 저장했어요 ♡"
            );
        } catch (err) {
            tell(err.message, true);
        } finally {
            loading = false;
            save.disabled = false;
        }
    });

    resetEditor();
    loadWeek();
});
