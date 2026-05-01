package com.back.domain.newstip.service;

import com.back.domain.member.entity.Member;
import com.back.domain.newstip.dto.NewsTipForm;
import com.back.domain.newstip.entity.NewsTip;
import com.back.domain.newstip.repository.NewsTipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsTipService {

    private final NewsTipRepository newsTipRepository;

    @Transactional
    public NewsTip submit(NewsTipForm form, Member submitter) {
        NewsTip tip = new NewsTip();
        tip.setPoliticianName(form.getPoliticianName());
        tip.setNewsUrl(form.getNewsUrl());
        tip.setDescription(form.getDescription());
        tip.setSubmitter(submitter);
        return newsTipRepository.save(tip);
    }

    public List<NewsTip> getPendingTips() {
        return newsTipRepository.findAllByStatusOrderByCreatedAtDesc(NewsTip.TipStatus.PENDING);
    }

    public List<NewsTip> getAllTips() {
        return newsTipRepository.findAllByOrderByCreatedAtDesc();
    }

    public NewsTip findById(Long id) {
        return newsTipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("제보를 찾을 수 없습니다."));
    }

    public long countPending() {
        return newsTipRepository.countByStatus(NewsTip.TipStatus.PENDING);
    }

    @Transactional
    public void review(Long id, String adminNote) {
        NewsTip tip = findById(id);
        tip.setStatus(NewsTip.TipStatus.REVIEWED);
        tip.setAdminNote(adminNote);
        newsTipRepository.save(tip);
    }

    public List<NewsTip> getMyTips(Member submitter) {
        return newsTipRepository.findBySubmitterOrderByCreatedAtDesc(submitter);
    }

    @Transactional
    public void reject(Long id, String adminNote) {
        NewsTip tip = findById(id);
        tip.setStatus(NewsTip.TipStatus.REJECTED);
        tip.setAdminNote(adminNote);
        newsTipRepository.save(tip);
    }
}
