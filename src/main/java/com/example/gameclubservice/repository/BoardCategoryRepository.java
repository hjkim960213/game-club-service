package com.example.gameclubservice.repository;

import com.example.gameclubservice.entity.BoardCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BoardCategoryRepository extends JpaRepository<BoardCategory, Long> {
    // 💡 순서(displayOrder) 오름차순으로 가져오기!
    List<BoardCategory> findAllByOrderByDisplayOrderAsc();
}