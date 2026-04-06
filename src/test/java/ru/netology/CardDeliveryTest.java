package ru.netology;

import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class CardDeliveryTest {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String APP_URL = "http://localhost:9999";
    private static Process appProcess;
    private static boolean startedByTest;

    @BeforeAll
    static void startApplication() throws Exception {
        if (isServerAvailable()) {
            return;
        }

        Path jarPath = Path.of("artifacts", "app-card-delivery.jar").toAbsolutePath();
        appProcess = new ProcessBuilder("java", "-jar", jarPath.toString())
                .redirectErrorStream(true)
                .start();
        startedByTest = true;
        waitForServer();
    }

    @AfterAll
    static void stopApplication() {
        if (startedByTest && appProcess != null) {
            appProcess.destroy();
        }
    }

    @BeforeEach
    void setUp() {
        open(APP_URL);
    }

    @Test
    void shouldSubmitFormWithValidData() {
        String meetingDate = generateDate(3);

        $("[data-test-id='city'] input").setValue("Москва");
        $("[data-test-id='date'] input").doubleClick().sendKeys(Keys.BACK_SPACE);
        $("[data-test-id='date'] input").setValue(meetingDate);
        $("[data-test-id='name'] input").setValue("Иванов Иван");
        $("[data-test-id='phone'] input").setValue("+79001234567");
        $("[data-test-id='agreement']").click();
        $$("button").findBy(Condition.exactText("Забронировать")).click();

        $(".button .spin_visible")
                .shouldBe(Condition.visible, Duration.ofSeconds(15));

        $("[data-test-id='notification'].notification_visible")
                .shouldBe(Condition.visible, Duration.ofSeconds(15));
        $("[data-test-id='notification'] .notification__title")
                .shouldHave(Condition.exactText("Успешно!"));
        $("[data-test-id='notification'] .notification__content")
                .shouldHave(Condition.exactText("Встреча успешно забронирована на " + meetingDate));
    }

    private String generateDate(int daysToAdd) {
        return LocalDate.now().plusDays(daysToAdd).format(DATE_FORMATTER);
    }

    private static void waitForServer() throws Exception {
        long deadline = System.currentTimeMillis() + Duration.ofSeconds(20).toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (isServerAvailable()) {
                return;
            }
            Thread.sleep(500);
        }
        throw new IllegalStateException("Application did not start at " + APP_URL);
    }

    private static boolean isServerAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(APP_URL).openConnection();
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            connection.connect();
            int statusCode = connection.getResponseCode();
            connection.disconnect();
            return statusCode < 500;
        } catch (IOException exception) {
            return false;
        }
    }
}