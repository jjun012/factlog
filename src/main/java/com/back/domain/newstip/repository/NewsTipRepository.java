package com.back.domain.newstip.repository;

import com.back.domain.member.entity.Member;
import com.back.domain.newstip.entity.NewsTip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsTipRepository extends JpaRepository<NewsTip, Long> {
    List<NewsTip> findAllByStatusOrderByCreatedAtDesc(NewsTip.TipStatus status);
    List<NewsTip> findAllByOrderByCreatedAtDesc();
    long countByStatus(NewsTip.TipStatus status);
    List<NewsTip> findBySubmitterOrderByCreatedAtDesc(Member submitter);
}
