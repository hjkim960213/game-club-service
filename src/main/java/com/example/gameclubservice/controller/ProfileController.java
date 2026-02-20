package com.example.gameclubservice.controller;

import com.example.gameclubservice.domain.Profile;
import com.example.gameclubservice.domain.ProfileRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ProfileController {

    private final ProfileRepository profileRepository;

    public ProfileController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @GetMapping("/profiles")
    // ❌ List<com.example.gameclubservice.controller.Profile> -> 잘못된 경로
    // ✅ List<Profile> -> 상단에서 import한 domain의 Profile을 사용함
    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }
}