package ru.janeforjane.timeline.llm;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    public List<String> generate(String decision) {
        log.info("LLM stub: generating branches for decision '{}'", decision);
        return List.of("Шаблонная ветка A", "Шаблонная ветка B");
    }
}
