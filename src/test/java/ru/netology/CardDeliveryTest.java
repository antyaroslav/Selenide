package ru.netology;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.openqa.selenium.Keys;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class CardDeliveryTest {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String APP_URL = "http://localhost:9999";

    @BeforeAll
    static void configureBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        Configuration.browserCapabilities = options;
        Configuration.baseUrl = APP_URL;
    }

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(isServerAvailable(), "Run app-card-delivery.jar before tests");
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