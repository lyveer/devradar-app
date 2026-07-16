package com.devradar.controller;

import com.devradar.repository.ProjectAnalysisRepository;
import com.devradar.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin
public class StatsController {

    private final UserRepository userRepository;
    private final ProjectAnalysisRepository projectAnalysisRepository;

    public StatsController(UserRepository userRepository, ProjectAnalysisRepository projectAnalysisRepository) {
        this.userRepository = userRepository;
        this.projectAnalysisRepository = projectAnalysisRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStats() {
        long realUsers = userRepository.count();
        long realProjects = projectAnalysisRepository.count();
        long realPremium = userRepository.countByIsPremium(true);

        Map<String, Object> stats = new HashMap<>();
        stats.put("registeredDevelopers", realUsers);
        stats.put("analyzedProjects", realProjects);
        stats.put("premiumMembers", realPremium);
        stats.put("aiAccuracy", 98.7);

        return ResponseEntity.ok(stats);
    }
}
