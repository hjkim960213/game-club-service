package com.example.gameclubservice.controller;

import com.example.gameclubservice.entity.BoardCategory;
import com.example.gameclubservice.entity.BoardPost;
import com.example.gameclubservice.entity.ClubMember;
import com.example.gameclubservice.repository.BoardCategoryRepository;
import com.example.gameclubservice.repository.BoardPostRepository;
import com.example.gameclubservice.repository.ClubMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityApiController {

    private final ClubMemberRepository memberRepo;
    private final BoardCategoryRepository categoryRepo;
    private final BoardPostRepository postRepo;

    // ==========================================
    // 👥 1. 멤버 소개 API
    // ==========================================
    @GetMapping("/members")
    public List<ClubMember> getMembers() {
        return memberRepo.findAll();
    }

    @PostMapping("/members")
    public String addMember(@RequestParam String nickname, @RequestParam String position, @RequestParam String description) {
        ClubMember member = ClubMember.builder()
                .nickname(nickname)
                .position(position)
                .description(description)
                .build();
        memberRepo.save(member);
        return "ok";
    }

    @DeleteMapping("/members/{id}")
    public String deleteMember(@PathVariable Long id) {
        memberRepo.deleteById(id);
        return "ok";
    }

    // ==========================================
    // 📁 2. 게시판 카테고리 API
    // ==========================================
    @GetMapping("/categories")
    public List<BoardCategory> getCategories() {
        return categoryRepo.findAll();
    }

    @PostMapping("/categories")
    public String addCategory(@RequestParam String name) {
        BoardCategory category = BoardCategory.builder()
                .name(name)
                .adminOnlyWrite(false) // 기본적으로 누구나 쓸 수 있게 세팅
                .build();
        categoryRepo.save(category);
        return "ok";
    }

    // ==========================================
    // 📝 3. 게시글 API
    // ==========================================
    @GetMapping("/posts")
    public List<BoardPost> getPosts(@RequestParam Long categoryId) {
        return postRepo.findByCategory_IdOrderByCreatedAtDesc(categoryId);
    }

    @PostMapping("/posts")
    public String addPost(@RequestParam Long categoryId, @RequestParam String title, @RequestParam String content, @RequestParam String authorNickname) {
        BoardCategory category = categoryRepo.findById(categoryId).orElseThrow();
        BoardPost post = BoardPost.builder()
                .category(category)
                .title(title)
                .content(content)
                .authorNickname(authorNickname)
                .build();
        postRepo.save(post);
        return "ok";
    }
}