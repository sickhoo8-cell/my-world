
document.addEventListener("DOMContentLoaded", () => {

    const chamber = document.getElementById("ballChamber");
    const floatingBalls = document.getElementById("floatingBalls");
    const resultBalls = document.getElementById("resultBalls");
    const drawButton = document.getElementById("drawButton");
    const machineStatus = document.getElementById("machineStatus");
    const drawMessage = document.getElementById("drawMessage");

    let isDrawing = false;

    // 번호에 따라 공 색상을 결정
    function getBallColor(number) {
        if (number <= 10) return "ball-yellow";
        if (number <= 20) return "ball-blue";
        if (number <= 30) return "ball-red";
        if (number <= 40) return "ball-gray";
        return "ball-green";
    }

    // 1~45번 공 생성
    function createBalls() {
        floatingBalls.innerHTML = "";

        for (let number = 1; number <= 45; number++) {
            const ball = document.createElement("div");

            ball.className =
                "lotto-ball " + getBallColor(number);

            ball.textContent = number;

            // 추첨기 안에 공 배치
            const x = 13 + Math.random() * 74;
            const y = 16 + Math.random() * 68;

            ball.style.left = x + "%";
            ball.style.top = y + "%";

            // 공마다 움직이는 속도를 다르게 설정
            ball.style.setProperty(
                "--speed",
                (0.15 + Math.random() * 0.23) + "s"
            );

            ball.style.animationDelay =
                (-Math.random() * 2) + "s";

            floatingBalls.appendChild(ball);
        }
    }

    // 중복 없는 번호 7개 생성
    function generateNumbers() {
        const numbers = Array.from(
            { length: 45 },
            (_, index) => index + 1
        );

        // Fisher-Yates 셔플
        for (let i = numbers.length - 1; i > 0; i--) {
            const randomValues = new Uint32Array(1);
            const range = i + 1;
            const limit = Math.floor(4294967296 / range) * range;

            let randomNumber;
            do {
                crypto.getRandomValues(randomValues);
                randomNumber = randomValues[0];
            } while (randomNumber >= limit);

            const j = randomNumber % range;

            [numbers[i], numbers[j]] =
                [numbers[j], numbers[i]];
        }

        // 앞의 6개는 기본 번호, 7번째는 보너스
        return {
            main: numbers.slice(0, 6).sort((a, b) => a - b),
            bonus: numbers[6]
        };
    }

    // 결과를 ? 상태로 초기화
    function resetResults() {
        resultBalls.innerHTML = "";

        for (let i = 0; i < 6; i++) {
            const ball = document.createElement("div");
            ball.className = "result-ball empty";
            ball.textContent = "?";
            resultBalls.appendChild(ball);
        }

        const plus = document.createElement("span");
        plus.className = "plus-sign";
        plus.textContent = "+";
        resultBalls.appendChild(plus);

        const bonus = document.createElement("div");
        bonus.className = "result-ball empty bonus";
        bonus.textContent = "?";
        resultBalls.appendChild(bonus);
    }

    // 결과 공 하나 표시
    function revealBall(number, index) {
        const balls = resultBalls.querySelectorAll(
            ".result-ball"
        );

        const ball = balls[index];

        ball.className =
            "result-ball revealed " + getBallColor(number);

        if (index === 6) {
            ball.classList.add("bonus");
        }

        ball.textContent = number;
    }

    // 일정 시간 기다리기
    function sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    // 추첨 시작
    async function startDrawing() {
        if (isDrawing) return;

        isDrawing = true;
        drawButton.disabled = true;
        drawButton.textContent = "🎱 추첨 중...";

        resetResults();
        createBalls();

        machineStatus.textContent = "MIXING!";
        drawMessage.textContent = "45개의 공을 섞는 중이야!";

        // 공 회전 시작
        chamber.classList.add("is-spinning");

        const result = generateNumbers();

        // 먼저 공을 섞기
        await sleep(1800);

        // 기본 번호 6개를 차례대로 공개
        for (let i = 0; i < 6; i++) {
            revealBall(result.main[i], i);

            machineStatus.textContent =
                (i + 1) + " / 6 PICKED";

            drawMessage.textContent =
                (i + 1) + "번째 번호가 나왔어!";

            await sleep(750);
        }

        // 보너스 번호 공개
        machineStatus.textContent = "BONUS TIME!";
        drawMessage.textContent =
            "마지막 보너스 번호를 뽑는 중!";

        await sleep(950);

        revealBall(result.bonus, 6);

        // 추첨 종료
        chamber.classList.remove("is-spinning");

        machineStatus.textContent = "LUCKY DONE!";
        drawMessage.textContent =
            "🍀 행운의 번호 7개가 완성됐어!";

        drawButton.textContent = "🔄 다시 추첨하기";
        drawButton.disabled = false;
        isDrawing = false;
    }

    createBalls();
    resetResults();

    drawButton.addEventListener("click", startDrawing);
});
