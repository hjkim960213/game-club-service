package com.example.gameclubservice.repository;

import com.example.gameclubservice.entity.BoardCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardCategoryRepository extends JpaRepository<BoardCategory, Long> {
}