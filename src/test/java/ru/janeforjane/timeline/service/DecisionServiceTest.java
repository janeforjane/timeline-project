package ru.janeforjane.timeline.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.janeforjane.timeline.llm.LlmClient;

class DecisionServiceTest {

    @Test
    void should_return_non_empty_branches_when_decision_provided() {
        DecisionService decisionService = new DecisionService(new LlmClient());

        List<String> branches = decisionService.generateBranches("Переехать в другой город");

        assertFalse(branches.isEmpty());
        assertTrue(branches.contains("Шаблонная ветка A"));
        assertTrue(branches.contains("Шаблонная ветка B"));
    }
}
