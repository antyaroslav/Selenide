package ru.netology;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class CardDeliveryTest {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @BeforeAll
    static void setUpAll() {
        Configuration.baseUrl = "http://localhost:9999";
    }

    @BeforeEach
    void setUp() {
        open("http://localhost:9999");
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
}