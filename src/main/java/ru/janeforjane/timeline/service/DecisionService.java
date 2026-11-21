package ru.janeforjane.timeline.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DecisionService {

    private static final Logger log = LoggerFactory.getLogger(DecisionService.class);

    public List<String> generateBranches(String decision) {
        log.info("Generating branches for decision: {}", decision);
        return List.of();
    }
}
