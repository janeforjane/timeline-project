package ru.janeforjane.timeline.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DecisionPageE2eTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort private int port;

    private static Playwright playwright;
    private static Browser browser;

    @BeforeAll
    static void setUpBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void tearDownBrowser() {
        browser.close();
        playwright.close();
    }

    @Test
    void should_render_branches_when_decision_submitted() throws Exception {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate("http://localhost:" + port + "/");

            page.fill("#decision", "Переехать в другой город");
            page.click("button[type=submit]");
            page.waitForFunction("() => document.getElementById('result').textContent.length > 0");

            String text = page.textContent("#result");
            JsonNode response = OBJECT_MAPPER.readTree(text);
            JsonNode branches = response.get("branches");

            assertTrue(branches.isArray(), "Ожидали поле branches в ответе");
            assertFalse(branches.isEmpty(), "Ожидали непустой список веток");
        }
    }
}
