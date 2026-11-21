package ru.janeforjane.timeline.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DecisionServiceTest {

    @Test
    void should_return_empty_branches_when_decision_provided() {
        DecisionService decisionService = new DecisionService();

        assertTrue(decisionService.generateBranches("Переехать в другой город").isEmpty());
    }
}
