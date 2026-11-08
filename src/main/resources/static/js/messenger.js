
"use strict";

/* ==========================================
   MY WORLD | MESSENGER

   - 직원 검색
   - 1:1 대화 상대 선택
   - 메시지 DB 저장
   - 최근 메시지 조회
   - 3초 간격 새 메시지 확인
========================================== */

document.addEventListener("DOMContentLoaded", () => {

    const searchInput =
        document.getElementById("employeeSearch");

    const employeeItems = Array.from(
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

    const conversationBody =
        document.getElementById("conversationBody");

    const messageInput =
        document.getElementById("messageInput");

    const sendButton =
        document.getElementById("sendMessageButton");

    const composerNotice =
        document.querySelector(".composer-actions p");

    const POLL_INTERVAL = 3000;

    let selectedPartnerId = null;
    let csrfToken = null;

    let latestMessageSignature = "";
    let loadingMessages = false;
    let sendingMessage = false;

    let currentMessages = [];

    // ======================================
    // 공통 API 요청
    // ======================================

    async function apiRequest(url, options = {}) {

        const response = await fetch(url, {
            credentials: "same-origin",
            cache: "no-store",
            ...options
        });

        if (response.status === 401) {
            throw new Error(
                "로그인이 만료되었습니다. 다시 로그인해주세요."
            );
        }

        if (response.status === 403) {
            throw new Error(
                "접근 권한이 없거나 요청이 만료되었습니다."
            );
        }

        if (!response.ok) {
            throw new Error(
                `요청 실패 (${response.status})`
            );
        }

        return response.json();
    }

    async function initializeCsrf() {

        const data =
            await apiRequest("/api/chat/csrf");

        csrfToken = data.token;

        if (!csrfToken) {
            throw new Error(
                "메시지 전송을 준비하지 못했습니다."
            );
        }
    }

    // ======================================
    // 직원 검색
    // ======================================

    function filterEmployees() {

        const keyword = searchInput.value
            .trim()
            .toLocaleLowerCase();

        let visibleCount = 0;

        employeeItems.forEach(item => {

            const name = item.dataset.name || "";
            const department =
                item.dataset.department || "";
            const position =
                item.dataset.position || "";

            const searchable =
                `${name} ${department} ${position}`
                    .toLocaleLowerCase();

            const matches =
                searchable.includes(keyword);

            item.hidden = !matches;

            if (matches) {
                visibleCount++;
            }
        });

        noSearchResults.hidden =
            visibleCount > 0 ||
            employeeItems.length === 0;
    }

    searchInput.addEventListener(
        "input",
        filterEmployees
    );

    // ======================================
    // 상태 화면 표시
    // ======================================

    function showNotice(title, description) {

        const wrapper =
            document.createElement("div");

        wrapper.className = "conversation-empty";

        const heading =
            document.createElement("h3");

        heading.textContent = title;

        const text =
            document.createElement("p");

        text.textContent = description;

        wrapper.append(heading, text);

        conversationBody.replaceChildren(wrapper);
        conversationBody.classList.remove(
            "has-messages"
        );
    }

    function setComposerEnabled(enabled) {

        messageInput.disabled = !enabled;
        sendButton.disabled = !enabled;

        messageInput.placeholder = enabled
            ? "메시지를 입력하세요."
            : "먼저 대화 상대를 선택하세요.";

        composerNotice.textContent = enabled
            ? "Enter: 전송 / Shift + Enter: 줄바꿈"
            : "직원 목록에서 상대방을 선택하세요.";
    }

    // ======================================
    // 직원 선택
    // ======================================

    async function selectEmployee(item) {

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

        const partnerId =
            Number(item.dataset.memberId);

        if (!Number.isSafeInteger(partnerId)
            || partnerId <= 0) {
            showNotice(
                "직원 정보 오류",
                "올바른 직원 정보를 찾지 못했습니다."
            );
            return;
        }

        selectedPartnerId = partnerId;
        latestMessageSignature = "";
        currentMessages = [];

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
            "대화 불러오는 중";

        setComposerEnabled(Boolean(csrfToken));

        showNotice(
            "대화 불러오는 중",
            `${name}님과의 대화를 확인하고 있습니다.`
        );

        await loadMessages(true);
    }

    employeeItems.forEach(item => {
        item.setAttribute("aria-pressed", "false");

        item.addEventListener("click", () => {
            selectEmployee(item);
        });
    });

    // ======================================
    // 메시지 표시
    // ======================================

    function formatTime(value) {

        if (!value) {
            return "";
        }

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return "";
        }

        return new Intl.DateTimeFormat(
            "ko-KR",
            {
                month: "2-digit",
                day: "2-digit",
                hour: "2-digit",
                minute: "2-digit",
                hour12: false
            }
        ).format(date);
    }

    function renderMessages(messages) {

        currentMessages = messages;

        if (messages.length === 0) {

            showNotice(
                "아직 메시지가 없습니다.",
                "첫 번째 메시지를 보내 대화를 시작하세요."
            );

            return;
        }

        const list =
            document.createElement("div");

        list.className = "chat-message-list";

        messages.forEach(message => {

            const isMine =
                Number(message.senderId) !==
                Number(selectedPartnerId);

            const row =
                document.createElement("div");

            row.className =
                isMine
                    ? "chat-message-row mine"
                    : "chat-message-row theirs";

            const bubble =
                document.createElement("div");

            bubble.className = "chat-bubble";

            if (!isMine) {
                const sender =
                    document.createElement("strong");

                sender.className = "chat-sender";

                sender.textContent =
                    message.senderName || "직원";

                bubble.append(sender);
            }

            const content =
                document.createElement("p");

            content.className = "chat-content";
            content.textContent =
                message.content || "";

            const time =
                document.createElement("small");

            time.className = "chat-time";
            time.textContent =
                formatTime(message.createdAt);

            bubble.append(content, time);
            row.append(bubble);
            list.append(row);
        });

        conversationBody.replaceChildren(list);

        conversationBody.classList.add(
            "has-messages"
        );

        conversationBody.scrollTop =
            conversationBody.scrollHeight;
    }

    // ======================================
    // 최근 메시지 조회
    // ======================================

    async function loadMessages(force = false) {

        if (selectedPartnerId === null ||
            loadingMessages) {
            return;
        }

        loadingMessages = true;

        const requestedPartnerId =
            selectedPartnerId;

        try {

            const messages = await apiRequest(
                "/api/chat/messages?partnerId=" +
                encodeURIComponent(requestedPartnerId)
            );

            // 조회 도중 다른 직원을 선택한 경우
            if (requestedPartnerId !== selectedPartnerId) {
                return;
            }

            const signature = messages
                .map(message => message.id)
                .join(",");

            if (force ||
                signature !== latestMessageSignature) {

                latestMessageSignature = signature;

                renderMessages(messages);
            }

            conversationStatus.textContent =
                "대화 연결됨";

        } catch (error) {

            if (requestedPartnerId === selectedPartnerId) {

                conversationStatus.textContent =
                    "연결 오류";

                showNotice(
                    "메시지를 불러올 수 없습니다.",
                    error.message
                );
            }

        } finally {
            loadingMessages = false;
        }
    }

    // ======================================
    // 메시지 전송
    // ======================================

    async function sendMessage() {

        if (sendingMessage ||
            selectedPartnerId === null ||
            !csrfToken) {
            return;
        }

        const content = messageInput.value.trim();

        if (!content) {
            return;
        }

        if (content.length > 2000) {
            alert("메시지는 2000자 이내로 입력해주세요.");
            return;
        }

        const partnerId = selectedPartnerId;

        sendingMessage = true;
        sendButton.disabled = true;

        try {

            await apiRequest(
                "/api/chat/messages",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json",
                        "X-Chat-CSRF": csrfToken
                    },

                    body: JSON.stringify({
                        partnerId: partnerId,
                        content: content
                    })
                }
            );

            if (selectedPartnerId === partnerId) {
                messageInput.value = "";

                await loadMessages(true);
            }

        } catch (error) {

            alert(
                "메시지 전송에 실패했습니다.\n" +
                error.message
            );

        } finally {

            sendingMessage = false;

            if (selectedPartnerId !== null
                && csrfToken) {
                sendButton.disabled = false;
            }
        }
    }

    sendButton.addEventListener(
        "click",
        sendMessage
    );

    messageInput.addEventListener(
        "keydown",
        event => {

            if (event.key === "Enter"
                && !event.shiftKey
                && !event.isComposing) {

                event.preventDefault();
                sendMessage();
            }
        }
    );

    // ======================================
    // 초기 설정 및 자동 확인
    // ======================================

    setComposerEnabled(false);

    initializeCsrf()
        .then(() => {
            if (selectedPartnerId !== null) {
                setComposerEnabled(true);
            }
        })
        .catch(error => {
            conversationStatus.textContent =
                "인증 오류";

            composerNotice.textContent =
                error.message;
        });

    setInterval(() => {

        if (!document.hidden &&
            selectedPartnerId !== null) {

            loadMessages();
        }

    }, POLL_INTERVAL);

});
