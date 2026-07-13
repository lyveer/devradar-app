package com.devradar.controller;

import com.devradar.model.Announcement;
import com.devradar.model.User;
import com.devradar.repository.AnnouncementRepository;
import com.devradar.repository.ProjectAnalysisRepository;
import com.devradar.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class AdminController {

    private final UserRepository userRepository;
    private final ProjectAnalysisRepository projectAnalysisRepository;
    private final AnnouncementRepository announcementRepository;

    public AdminController(UserRepository userRepository,
                           ProjectAnalysisRepository projectAnalysisRepository,
                           AnnouncementRepository announcementRepository) {
        this.userRepository = userRepository;
        this.projectAnalysisRepository = projectAnalysisRepository;
        this.announcementRepository = announcementRepository;
    }

    // --- Public Announcements ---

    @GetMapping("/api/announcements")
    public ResponseEntity<List<Announcement>> getAnnouncements() {
        return ResponseEntity.ok(announcementRepository.findAllByOrderByCreatedAtDesc());
    }

    // --- Admin Endpoints (Require email to contain "admin") ---

    private void checkAdminAccess(Authentication authentication) {
        if (authentication == null || !authentication.getName().toLowerCase().contains("admin")) {
            throw new RuntimeException("Bu işlem için yetkiniz yok. Yönetici girişi yapmalısınız.");
        }
    }

    @GetMapping("/api/admin/stats")
    public ResponseEntity<Map<String, Object>> getStats(Authentication authentication) {
        checkAdminAccess(authentication);

        long totalUsers = userRepository.count();
        long totalAnalyses = projectAnalysisRepository.count();
        long premiumUsers = userRepository.findAll().stream().filter(User::getIsPremium).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalAnalyses", totalAnalyses);
        stats.put("premiumUsers", premiumUsers);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/admin/users")
    public ResponseEntity<List<User>> getUsers(Authentication authentication) {
        checkAdminAccess(authentication);
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/api/admin/users/{id}/credits")
    public ResponseEntity<User> adjustCredits(@PathVariable Long id, @RequestParam Integer credits, Authentication authentication) {
        checkAdminAccess(authentication);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        user.setCredits(credits);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping("/api/admin/users/{id}/premium")
    public ResponseEntity<User> togglePremium(@PathVariable Long id, @RequestParam Boolean isPremium, Authentication authentication) {
        checkAdminAccess(authentication);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        user.setIsPremium(isPremium);
        if (isPremium) {
            user.setCredits(99999);
        } else {
            user.setCredits(5);
        }
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping("/api/admin/announcements")
    public ResponseEntity<Announcement> createAnnouncement(@RequestBody Announcement announcement, Authentication authentication) {
        checkAdminAccess(authentication);

        if (announcement.getTitle() == null || announcement.getTitle().isBlank()) {
            throw new RuntimeException("Duyuru başlığı boş olamaz");
        }
        if (announcement.getContent() == null || announcement.getContent().isBlank()) {
            throw new RuntimeException("Duyuru içeriği boş olamaz");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(announcementRepository.save(announcement));
    }
}
