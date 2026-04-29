package com.back.domain.flipflop.service;

import com.back.domain.flipflop.dto.FlipFlopForm;
import com.back.domain.flipflop.entity.FlipFlop;
import com.back.domain.flipflop.repository.FlipFlopRepository;
import com.back.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FlipFlopService {

    private final FlipFlopRepository flipFlopRepository;

    @Transactional
    public FlipFlop create(FlipFlopForm form, Member author) {
        FlipFlop flipFlop = new FlipFlop();
        flipFlop.setPoliticianName(form.getPoliticianName());
        flipFlop.setTitle(form.getTitle());
        flipFlop.setBeforeStatement(form.getBeforeStatement());
        flipFlop.setBeforeSource(form.getBeforeSource());
        flipFlop.setBeforeDate(form.getBeforeDate());
        flipFlop.setAfterStatement(form.getAfterStatement());
        flipFlop.setAfterSource(form.getAfterSource());
        flipFlop.setAfterDate(form.getAfterDate());
        flipFlop.setAuthor(author);
        return flipFlopRepository.save(flipFlop);
    }

    @Transactional
    public void update(Long id, FlipFlopForm form) {
        FlipFlop flipFlop = findById(id);
        flipFlop.setPoliticianName(form.getPoliticianName());
        flipFlop.setTitle(form.getTitle());
        flipFlop.setBeforeStatement(form.getBeforeStatement());
        flipFlop.setBeforeSource(form.getBeforeSource());
        flipFlop.setBeforeDate(form.getBeforeDate());
        flipFlop.setAfterStatement(form.getAfterStatement());
        flipFlop.setAfterSource(form.getAfterSource());
        flipFlop.setAfterDate(form.getAfterDate());
    }

    @Transactional
    public void delete(Long id) {
        flipFlopRepository.deleteById(id);
    }

    @Transactional
    public FlipFlop getDetail(Long id) {
        FlipFlop flipFlop = findById(id);
        flipFlop.incrementViewCount();
        return flipFlop;
    }

    public FlipFlop findById(Long id) {
        return flipFlopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
    }

    public Page<FlipFlop> getList(int page, String keyword) {
        Pageable pageable = PageRequest.of(page, 10);
        if (StringUtils.hasText(keyword)) {
            return flipFlopRepository.findByPoliticianNameContainingOrTitleContainingOrderByCreatedAtDesc(
                    keyword, keyword, pageable);
        }
        return flipFlopRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
