
const snowContainer = document.getElementById("snow-container");

const reduceMotion = window.matchMedia(
    "(prefers-reduced-motion: reduce)"
).matches;

if (snowContainer && !reduceMotion) {

    const snowCount = 70;

    for (let i = 0; i < snowCount; i++) {

        const snowflake = document.createElement("div");

        snowflake.classList.add("snowflake");

        const size = Math.random() * 6 + 3;
        const duration = Math.random() * 12 + 8;
        const delay = Math.random() * -20;

        snowflake.style.left = Math.random() * 100 + "%";

        snowflake.style.width = size + "px";
        snowflake.style.height = size + "px";

        snowflake.style.opacity =
            Math.random() * 0.6 + 0.3;

        snowflake.style.animationDuration =
            duration + "s";

        snowflake.style.animationDelay =
            delay + "s";

        snowContainer.appendChild(snowflake);
    }
}
