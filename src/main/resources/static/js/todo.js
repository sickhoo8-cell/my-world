
"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const API = "/api/todos";

    const form = document.getElementById("todoForm");
    const idInput = document.getElementById("todoId");
    const titleInput = document.getElementById("todoTitle");
    const dateInput = document.getElementById("todoDueDate");
    const priorityInput = document.getElementById("todoPriority");

    const saveButton = document.getElementById("todoSaveButton");
    const cancelButton = document.getElementById("todoCancelButton");
    const list = document.getElementById("todoList");
    const status = document.getElementById("todoStatus");
    const counter = document.getElementById("todoCounter");
    const filter = document.getElementById("todoFilter");

    if (!form || !list) {
        return;
    }

    let todos = [];
    let busy = false;

    function showStatus(message, error = false) {
        status.textContent = message;
        status.classList.toggle("todo-error", error);
    }

    function element(tag, className, text) {
        const node = document.createElement(tag);

        if (className) {
            node.className = className;
        }

        if (text !== undefined) {
            node.textContent = text;
        }

        return node;
    }

    async function request(url, options = {}) {
        const response = await fetch(url, {
            credentials: "same-origin",
            cache: "no-store",
            ...options
        });

        if (response.status === 401) {
            window.location.replace("/login");
            throw new Error("로그인이 만료되었습니다.");
        }

        if (!response.ok) {
            if (response.status === 400) {
                throw new Error("입력 내용을 확인해 주세요.");
            }

            if (response.status === 404) {
                throw new Error("할 일을 찾을 수 없습니다.");
            }

            throw new Error(
                `요청 처리에 실패했습니다. (${response.status})`
            );
        }

        if (response.status === 204) {
            return null;
        }

        return response.json();
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

    function resetForm() {
        form.reset();
        idInput.value = "";
        priorityInput.value = "NORMAL";
        saveButton.textContent = "♡ 할 일 추가";
        cancelButton.hidden = true;
    }

    function priorityLabel(priority) {
        if (priority === "HIGH") return "🔴 높음";
        if (priority === "LOW") return "🟢 낮음";
        return "🟣 보통";
    }

    function formatDueDate(dateString) {
        if (!dateString) {
            return "마감일 없음";
        }

        const [year, month, day] = dateString.split("-");
        return `${year}.${month}.${day}`;
    }

    function render() {
        list.replaceChildren();

        const completedCount = todos.filter(
            todo => todo.completed
        ).length;

        counter.textContent =
            `${completedCount}/${todos.length}개 완료`;

        const selected = filter.value;

        const visibleTodos = todos.filter(todo => {
            if (selected === "active") {
                return !todo.completed;
            }

            if (selected === "completed") {
                return todo.completed;
            }

            return true;
        });

        visibleTodos.sort((a, b) => {
            if (a.completed !== b.completed) {
                return a.completed ? 1 : -1;
            }

            const priorityOrder = {
                HIGH: 0,
                NORMAL: 1,
                LOW: 2
            };

            const aPriority = priorityOrder[a.priority] ?? 1;
            const bPriority = priorityOrder[b.priority] ?? 1;

            if (aPriority !== bPriority) {
                return aPriority - bPriority;
            }

            return b.id - a.id;
        });

        if (visibleTodos.length === 0) {
            list.appendChild(
                element(
                    "p",
                    "todo-empty",
                    selected === "all"
                        ? "아직 등록된 할 일이 없어요 ♡"
                        : "해당하는 할 일이 없어요 ♡"
                )
            );

            return;
        }

        visibleTodos.forEach(todo => {
            const item = element(
                "article",
                `todo-item ${todo.completed ? "is-completed" : ""}`
            );

            const top = element("div", "todo-item-top");

            const checkbox = element(
                "input",
                "todo-complete-checkbox"
            );
            checkbox.type = "checkbox";
            checkbox.checked = todo.completed;
            checkbox.setAttribute(
                "aria-label",
                `${todo.title} 완료 상태`
            );

            checkbox.addEventListener("change", async () => {
                checkbox.disabled = true;

                try {
                    const updated = await request(
                        `${API}/${todo.id}/completed`,
                        jsonOptions("PATCH", {
                            completed: checkbox.checked
                        })
                    );

                    todos = todos.map(current =>
                        current.id === updated.id
                            ? updated
                            : current
                    );

                    render();
                    showStatus(
                        updated.completed
                            ? "할 일을 완료했어요! 🎉"
                            : "미완료 상태로 변경했어요."
                    );
                } catch (error) {
                    checkbox.checked = !checkbox.checked;
                    checkbox.disabled = false;
                    showStatus(error.message, true);
                }
            });

            const title = element(
                "strong",
                "todo-item-title",
                todo.title
            );

            top.append(checkbox, title);

            const info = element("div", "todo-item-info");

            const priority = element(
                "span",
                "todo-priority",
                priorityLabel(todo.priority)
            );

            const date = element(
                "span",
                "todo-due-date",
                `📅 ${formatDueDate(todo.dueDate)}`
            );

            info.append(priority, date);

            if (
                todo.dueDate &&
                !todo.completed
            ) {
                const today = new Date();
                const localToday = [
                    today.getFullYear(),
                    String(today.getMonth() + 1).padStart(2, "0"),
                    String(today.getDate()).padStart(2, "0")
                ].join("-");

                if (todo.dueDate < localToday) {
                    info.appendChild(
                        element(
                            "span",
                            "todo-overdue",
                            "기한 지남"
                        )
                    );
                }
            }

            const actions = element(
                "div",
                "todo-item-actions"
            );

            const editButton = element(
                "button",
                "todo-edit-button",
                "✏️ 수정"
            );
            editButton.type = "button";
            editButton.addEventListener("click", () => {
                idInput.value = String(todo.id);
                titleInput.value = todo.title;
                dateInput.value = todo.dueDate || "";
                priorityInput.value = todo.priority;

                saveButton.textContent = "♡ 수정 완료";
                cancelButton.hidden = false;
                titleInput.focus();

                form.scrollIntoView({
                    behavior: "smooth",
                    block: "center"
                });
            });

            const deleteButton = element(
                "button",
                "todo-delete-button",
                "🗑️ 삭제"
            );
            deleteButton.type = "button";
            deleteButton.addEventListener("click", async () => {
                if (busy) return;

                if (!window.confirm(
                    "이 할 일을 삭제할까요?"
                )) {
                    return;
                }

                busy = true;
                deleteButton.disabled = true;

                try {
                    await request(`${API}/${todo.id}`, {
                        method: "DELETE"
                    });

                    todos = todos.filter(
                        current => current.id !== todo.id
                    );

                    if (idInput.value === String(todo.id)) {
                        resetForm();
                    }

                    render();
                    showStatus("할 일을 삭제했어요.");
                } catch (error) {
                    showStatus(error.message, true);
                } finally {
                    busy = false;
                    deleteButton.disabled = false;
                }
            });

            actions.append(editButton, deleteButton);
            item.append(top, info, actions);
            list.appendChild(item);
        });
    }

    async function loadTodos() {
        try {
            showStatus("할 일을 불러오는 중...");

            todos = await request(API);
            render();

            showStatus("");
        } catch (error) {
            showStatus(error.message, true);
        }
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();

        if (busy) return;

        const title = titleInput.value.trim();

        if (!title || title.length > 150) {
            showStatus(
                "할 일을 1~150자로 입력해 주세요.",
                true
            );
            return;
        }

        const todoId = idInput.value;
        const editing = todoId !== "";

        const url = editing
            ? `${API}/${todoId}`
            : API;

        const data = {
            title,
            dueDate: dateInput.value || null,
            priority: priorityInput.value
        };

        busy = true;
        saveButton.disabled = true;

        try {
            showStatus("저장하는 중...");

            const saved = await request(
                url,
                jsonOptions(
                    editing ? "PUT" : "POST",
                    data
                )
            );

            if (editing) {
                todos = todos.map(todo =>
                    todo.id === saved.id ? saved : todo
                );
            } else {
                todos.unshift(saved);
            }

            resetForm();
            render();

            showStatus(
                editing
                    ? "할 일을 수정했어요 ♡"
                    : "새 할 일을 저장했어요 ♡"
            );
        } catch (error) {
            showStatus(error.message, true);
        } finally {
            busy = false;
            saveButton.disabled = false;
        }
    });

    cancelButton.addEventListener("click", () => {
        resetForm();
        showStatus("");
    });

    filter.addEventListener("change", render);

    loadTodos();
});
