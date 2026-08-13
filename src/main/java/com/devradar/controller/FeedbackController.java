package com.devradar.controller;

import com.devradar.model.Feedback;
import com.devradar.repository.FeedbackRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;

    public FeedbackController(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String email = payload.get("email");
        String topic = payload.get("topic");
        String message = payload.get("message");

        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            topic == null || topic.trim().isEmpty() ||
            message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lütfen tüm alanları doldurun."));
        }

        Feedback feedback = new Feedback(name, email, topic, message);
        feedbackRepository.save(feedback);

        return ResponseEntity.ok(Map.of("message", "Geri bildiriminiz başarıyla iletildi. Teşekkür ederiz!"));
    }
}
