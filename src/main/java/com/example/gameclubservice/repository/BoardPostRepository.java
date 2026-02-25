package com.example.gameclubservice.repository;

import com.example.gameclubservice.entity.BoardPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {
    // 💡 특정 카테고리에 속한 글만 최신순으로 싹 긁어오는 맞춤형 기능!
    List<BoardPost> findByCategory_IdOrderByCreatedAtDesc(Long categoryId);
}