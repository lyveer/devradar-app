package com.devradar.controller;

import com.devradar.dto.ProfileRequest;
import com.devradar.dto.ProfileScoreResponse;
import com.devradar.model.Profile;
import com.devradar.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<Profile> getProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<Profile> saveProfile(Authentication authentication,
                                               @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.saveProfile(authentication.getName(), request));
    }

    @PostMapping("/score")
    public ResponseEntity<ProfileScoreResponse> scoreProfile(
            Authentication authentication,
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(profileService.scoreProfile(authentication.getName(), language));
    }
}
