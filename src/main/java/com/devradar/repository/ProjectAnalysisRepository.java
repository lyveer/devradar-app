package com.devradar.repository;

import com.devradar.model.ProjectAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectAnalysisRepository extends JpaRepository<ProjectAnalysis, Long> {
    List<ProjectAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);
}
