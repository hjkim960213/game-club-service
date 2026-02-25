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

    // --- 👥 멤버 소개 API ---
    @GetMapping("/members") public List<ClubMember> getMembers() { return memberRepo.findAll(); }
    @PostMapping("/members") public String addMember(@RequestParam String nickname, @RequestParam String position, @RequestParam String description) {
        memberRepo.save(ClubMember.builder().nickname(nickname).position(position).description(description).build()); return "ok";
    }
    @DeleteMapping("/members/{id}") public String deleteMember(@PathVariable Long id) { memberRepo.deleteById(id); return "ok"; }

    // --- 📁 카테고리 API ---
    @GetMapping("/categories") public List<BoardCategory> getCategories() { return categoryRepo.findAllByOrderByDisplayOrderAsc(); } // 💡 순서대로 가져옴

    @PostMapping("/categories") public String addCategory(@RequestParam String name) {
        categoryRepo.save(BoardCategory.builder().name(name).adminOnlyWrite(false).displayOrder(99).build()); return "ok";
    }

    // 💡 신규: 카테고리 삭제 (속해있는 글들도 같이 삭제)
    @DeleteMapping("/categories/{id}")
    public String deleteCategory(@PathVariable Long id) {
        List<BoardPost> posts = postRepo.findByCategory_IdOrderByCreatedAtDesc(id);
        postRepo.deleteAll(posts); // 안의 글 먼저 삭제
        categoryRepo.deleteById(id); // 탭 삭제
        return "ok";
    }

    // 💡 신규: 카테고리 드래그 앤 드롭 순서 변경
    @PutMapping("/categories/reorder")
    public String reorderCategories(@RequestBody List<Long> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            BoardCategory cat = categoryRepo.findById(categoryIds.get(i)).orElse(null);
            if (cat != null) { cat.setDisplayOrder(i); categoryRepo.save(cat); }
        }
        return "ok";
    }

    // --- 📝 게시글 API ---
    @GetMapping("/posts") public List<BoardPost> getPosts(@RequestParam Long categoryId) { return postRepo.findByCategory_IdOrderByCreatedAtDesc(categoryId); }

    @PostMapping("/posts") public String addPost(@RequestParam Long categoryId, @RequestParam String title, @RequestParam String content, @RequestParam String authorNickname) {
        BoardCategory category = categoryRepo.findById(categoryId).orElseThrow();
        postRepo.save(BoardPost.builder().category(category).title(title).content(content).authorNickname(authorNickname).build()); return "ok";
    }

    // 💡 신규: 게시글 삭제 (작성자 본인 or 관리자만 가능하도록 체크)
    @DeleteMapping("/posts/{id}")
    public String deletePost(@PathVariable Long id, @RequestParam String nickname, @RequestParam String role) {
        BoardPost post = postRepo.findById(id).orElse(null);
        if (post != null) {
            if ("ADMIN".equals(role) || post.getAuthorNickname().equals(nickname)) {
                postRepo.deleteById(id);
                return "ok";
            }
            return "forbidden"; // 권한 없음
        }
        return "notfound";
    }
}