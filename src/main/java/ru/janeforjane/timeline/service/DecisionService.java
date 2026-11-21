package ru.janeforjane.timeline.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.janeforjane.timeline.llm.LlmClient;

@Service
public class DecisionService {

    private static final Logger log = LoggerFactory.getLogger(DecisionService.class);

    private final LlmClient llmClient;

    public DecisionService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public List<String> generateBranches(String decision) {
        log.info("Generating branches for decision: {}", decision);
        return llmClient.generate(decision);
    }
}
