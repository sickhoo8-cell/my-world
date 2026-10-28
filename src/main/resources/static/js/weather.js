
document.addEventListener("DOMContentLoaded", () => {

    const card = document.getElementById("weatherCard");
    if (!card) return;

    const region = card.dataset.region;

    // 회원가입 화면에서 선택할 수 있는 지역별 좌표
    const regions = {
        "서울특별시": [37.5665, 126.9780],
        "경기도 고양시": [37.6584, 126.8320],
        "충청남도 서산시": [36.7845, 126.4503],
        "경기도 수원시": [37.2636, 127.0286],
        "경기도 화성시": [37.1995, 126.8312],
        "충청남도 천안시": [36.8151, 127.1139],
        "대전광역시": [36.3504, 127.3845],
        "인천광역시": [37.4563, 126.7052],
        "부산광역시": [35.1796, 129.0756],
        "대구광역시": [35.8714, 128.6014],
        "광주광역시": [35.1595, 126.8526],
        "울산광역시": [35.5384, 129.3114],
        "세종특별자치시": [36.4800, 127.2890],
        "제주특별자치도 제주시": [33.4996, 126.5312]
    };

    // 기존 회원이나 미등록 지역은 서울 기준
    const [latitude, longitude] =
    regions[region] || regions["서울특별시"];

    function getWeatherInfo(code, isDay) {
        if (code === 0) {
            return [isDay ? "☀️" : "🌙", "맑음"];
        }
        if (code <= 3) {
            return [isDay ? "⛅" : "☁️", "구름"];
        }
        if (code === 45 || code === 48) {
            return ["🌫️", "안개"];
        }
        if ([51, 53, 55, 56, 57].includes(code)) {
            return ["🌦️", "이슬비"];
        }
        if ([61, 63, 65, 66, 67, 80, 81, 82].includes(code)) {
            return ["🌧️", "비"];
        }
        if ([71, 73, 75, 77, 85, 86].includes(code)) {
            return ["❄️", "눈"];
        }
        if ([95, 96, 99].includes(code)) {
            return ["⛈️", "뇌우"];
        }
        return ["🌤️", "날씨 정보"];
    }

    async function loadWeather() {

        const params = new URLSearchParams({
            latitude: latitude,
            longitude: longitude,
            current: [
                "temperature_2m",
                "relative_humidity_2m",
                "weather_code",
                "wind_speed_10m",
                "is_day"
            ].join(","),
            timezone: "Asia/Seoul"
        });

        const url =
            "https://api.open-meteo.com/v1/forecast?" +
            params.toString();

        try {
            const response = await fetch(url);

            if (!response.ok) {
                throw new Error("날씨 API 요청 실패");
            }

            const data = await response.json();
            const current = data.current;

            if (!current ||
                typeof current.temperature_2m !== "number") {
                throw new Error("날씨 데이터 없음");
            }

            const [icon, description] = getWeatherInfo(
                current.weather_code,
                current.is_day === 1
            );

            document.getElementById("weatherIcon")
                .textContent = icon;

            document.getElementById("weatherTemp")
                .textContent =
                Math.round(current.temperature_2m) + "°C";

            document.getElementById("weatherDescription")
                .textContent = description;

            document.getElementById("weatherHumidity")
                .textContent =
                current.relative_humidity_2m + "%";

            document.getElementById("weatherWind")
                .textContent =
                current.wind_speed_10m + " km/h";

            document.getElementById("weatherUpdated")
                .textContent = "기준 시각: " + current.time;

        } catch (error) {
            console.error("날씨 조회 실패:", error);

            document.getElementById("weatherDescription")
                .textContent =
                "날씨 정보를 불러올 수 없습니다.";

            document.getElementById("weatherUpdated")
                .textContent = "잠시 후 새로고침해 주세요.";
        }
    }

    loadWeather();

    // 15분마다 갱신
    setInterval(loadWeather, 15 * 60 * 1000);
});
