
"use strict";

document.addEventListener("DOMContentLoaded", () => {

    const API_URL = "/api/memos";

    const form = document.getElementById("memoForm");
    const titleInput = document.getElementById("memoTitle");
    const contentInput = document.getElementById("memoContent");
    const memoIdInput = document.getElementById("memoId");

    const saveButton = document.getElementById("memoSaveButton");
    const cancelButton = document.getElementById("memoCancelButton");
    const memoList = document.getElementById("memoList");
    const memoCount = document.getElementById("memoCount");
    const memoStatus = document.getElementById("memoStatus");

    let memos = [];

    if (!form || !memoList) {
        return;
    }

    function showStatus(message, isError = false) {
        memoStatus.textContent = message;
        memoStatus.classList.toggle("memo-error", isError);
    }

    function resetForm() {
        form.reset();
        memoIdInput.value = "";
        saveButton.textContent = "♡ 메모 저장";
        cancelButton.hidden = true;
    }

    function formatDate(value) {
        if (!value) {
            return "";
        }

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return "";
        }

        return new Intl.DateTimeFormat("ko-KR", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
            hour: "2-digit",
            minute: "2-digit"
        }).format(date);
    }

    async function apiRequest(url, options = {}) {
        const response = await fetch(url, {
            credentials: "same-origin",
            ...options
        });

        if (response.status === 401) {
            window.location.href = "/login";
            throw new Error("로그인이 만료되었습니다.");
        }

        if (!response.ok) {
            if (response.status === 400) {
                throw new Error("제목과 내용을 확인해 주세요.");
            }

            if (response.status === 404) {
                throw new Error("메모를 찾을 수 없습니다.");
            }

            throw new Error("요청을 처리하지 못했습니다.");
        }

        if (response.status === 204) {
            return null;
        }

        return response.json();
    }

    function createElement(tag, className, text) {
        const element = document.createElement(tag);

        if (className) {
            element.className = className;
        }

        if (text !== undefined) {
            element.textContent = text;
        }

        return element;
    }

    function renderMemos() {
        memoList.replaceChildren();

        memoCount.textContent = `${memos.length}개의 메모`;

        if (memos.length === 0) {
            const empty = createElement(
                "p",
                "memo-empty",
                "아직 저장된 메모가 없어요. 첫 메모를 작성해 보세요 ♡"
            );

            memoList.appendChild(empty);
            return;
        }

        memos.forEach((memo) => {

            const card = createElement("article", "saved-memo");

            const header = createElement(
                "div",
                "saved-memo-header"
            );

            const title = createElement(
                "h3",
                "saved-memo-title",
                memo.title
            );

            const date = createElement(
                "small",
                "saved-memo-date",
                formatDate(memo.updatedAt)
            );

            header.append(title, date);

            const content = createElement(
                "p",
                "saved-memo-content",
                memo.content
            );

            const actions = createElement(
                "div",
                "saved-memo-actions"
            );

            const editButton = createElement(
                "button",
                "memo-edit-button",
                "✏️ 수정"
            );

            editButton.type = "button";

            editButton.addEventListener("click", () => {
                startEdit(memo);
            });

            const deleteButton = createElement(
                "button",
                "memo-delete-button",
                "🗑️ 삭제"
            );

            deleteButton.type = "button";

            deleteButton.addEventListener("click", () => {
                deleteMemo(memo.id);
            });

            actions.append(editButton, deleteButton);

            card.append(header, content, actions);
            memoList.appendChild(card);
        });
    }

    async function loadMemos() {
        showStatus("메모를 불러오는 중...");

        try {
            memos = await apiRequest(API_URL);
            renderMemos();
            showStatus("");
        } catch (error) {
            showStatus(error.message, true);
        }
    }

    function startEdit(memo) {
        memoIdInput.value = memo.id;
        titleInput.value = memo.title;
        contentInput.value = memo.content;

        saveButton.textContent = "♡ 수정 완료";
        cancelButton.hidden = false;

        titleInput.focus();
        form.scrollIntoView({
            behavior: "smooth",
            block: "center"
        });
    }

    async function deleteMemo(memoId) {
        const confirmed = window.confirm(
            "이 메모를 정말 삭제할까요?"
        );

        if (!confirmed) {
            return;
        }

        showStatus("메모를 삭제하는 중...");

        try {
            await apiRequest(`${API_URL}/${memoId}`, {
                method: "DELETE"
            });

            if (memoIdInput.value === String(memoId)) {
                resetForm();
            }

            await loadMemos();
            showStatus("메모를 삭제했어요.");
        } catch (error) {
            showStatus(error.message, true);
        }
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const title = titleInput.value.trim();
        const content = contentInput.value.trim();
        const memoId = memoIdInput.value;

        if (!title || !content) {
            showStatus(
                "제목과 내용을 모두 입력해 주세요.",
                true
            );
            return;
        }

        if (title.length > 100 || content.length > 2000) {
            showStatus(
                "제목은 100자, 내용은 2000자 이하로 작성해 주세요.",
                true
            );
            return;
        }

        const isEditing = memoId !== "";

        const url = isEditing
            ? `${API_URL}/${memoId}`
            : API_URL;

        const method = isEditing ? "PUT" : "POST";

        saveButton.disabled = true;
        showStatus("메모를 저장하는 중...");

        try {
            await apiRequest(url, {
                method,
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    title,
                    content
                })
            });

            resetForm();
            await loadMemos();

            showStatus(
                isEditing
                    ? "메모를 수정했어요 ♡"
                    : "메모를 저장했어요 ♡"
            );
        } catch (error) {
            showStatus(error.message, true);
        } finally {
            saveButton.disabled = false;
        }
    });

    cancelButton.addEventListener("click", () => {
        resetForm();
        showStatus("");
    });

    loadMemos();
});
