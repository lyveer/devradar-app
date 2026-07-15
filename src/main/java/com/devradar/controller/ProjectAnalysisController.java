package com.devradar.controller;

import com.devradar.dto.ProjectAnalysisRequest;
import com.devradar.dto.ProjectAnalysisResponse;
import com.devradar.service.ProjectAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin
public class ProjectAnalysisController {

    private final ProjectAnalysisService analysisService;

    public ProjectAnalysisController(ProjectAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping
    public ResponseEntity<ProjectAnalysisResponse> analyzeProject(Authentication authentication,
                                                                  @Valid @RequestBody ProjectAnalysisRequest request) {
        return ResponseEntity.ok(analysisService.analyzeProject(authentication.getName(), request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ProjectAnalysisResponse>> getHistory(Authentication authentication) {
        return ResponseEntity.ok(analysisService.getHistory(authentication.getName()));
    }

    @PostMapping("/{id}/steps")
    public ResponseEntity<Void> updateCompletedSteps(Authentication authentication,
                                                     @PathVariable Long id,
                                                     @RequestBody List<Integer> steps) {
        analysisService.updateCompletedSteps(authentication.getName(), id, steps);
        return ResponseEntity.ok().build();
    }
}
