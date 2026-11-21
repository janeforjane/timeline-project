package ru.janeforjane.timeline.controller;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.janeforjane.timeline.controller.dto.DecisionRequest;
import ru.janeforjane.timeline.controller.dto.DecisionResponse;
import ru.janeforjane.timeline.service.DecisionService;

@RestController
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @PostMapping("/api/decision")
    public DecisionResponse decide(@RequestBody DecisionRequest request) {
        List<String> branches = decisionService.generateBranches(request.decision());
        return new DecisionResponse(branches);
    }
}
