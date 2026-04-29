package com.back.domain.flipflop.repository;

import com.back.domain.flipflop.entity.FlipFlop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlipFlopRepository extends JpaRepository<FlipFlop, Long> {
    Page<FlipFlop> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<FlipFlop> findByPoliticianNameContainingOrTitleContainingOrderByCreatedAtDesc(
            String politicianName, String title, Pageable pageable);
}
