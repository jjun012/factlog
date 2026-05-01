package com.back.domain.aidraft.controller;

import com.back.domain.aidraft.entity.AiFlipFlopDraft;
import com.back.domain.aidraft.service.AiFlipFlopDraftService;
import com.back.domain.flipflop.dto.FlipFlopForm;
import com.back.domain.flipflop.entity.FlipFlop;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.global.ai.NewsSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/ai-draft")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AiFlipFlopDraftController {

    private final AiFlipFlopDraftService draftService;
    private final MemberService memberService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "PENDING") String status) {
        if ("ALL".equals(status)) {
            model.addAttribute("drafts", draftService.getAllDrafts());
        } else {
            model.addAttribute("drafts", draftService.getPendingDrafts());
        }
        model.addAttribute("currentStatus", status);
        model.addAttribute("politicians", NewsSearchService.MAJOR_POLITICIANS);
        model.addAttribute("isScanning", draftService.isScanning());
        return "admin/ai-draft-list";
    }

    // 전체 자동 분석 (주요 정치인 전부)
    @PostMapping("/scan-all")
    public String scanAll(RedirectAttributes redirectAttributes) {
        if (draftService.isScanning()) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 AI 분석이 진행 중입니다. 잠시 후 확인해 주세요.");
            return "redirect:/admin/ai-draft";
        }
        draftService.generateAllDrafts();
        redirectAttributes.addFlashAttribute("successMessage",
                "주요 정치인 " + NewsSearchService.MAJOR_POLITICIANS.size() + "명에 대한 AI 분석을 백그라운드에서 시작했습니다.");
        return "redirect:/admin/ai-draft";
    }

    // 특정 정치인만 분석
    @PostMapping("/search")
    public String search(@RequestParam String politicianName,
                         RedirectAttributes redirectAttributes) {
        if (politicianName == null || politicianName.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "정치인 이름을 입력하세요.");
            return "redirect:/admin/ai-draft";
        }
        if (draftService.isScanning()) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 AI 분석이 진행 중입니다. 잠시 후 확인해 주세요.");
            return "redirect:/admin/ai-draft";
        }
        draftService.generateDraftsForPolitician(politicianName.trim());
        redirectAttributes.addFlashAttribute("successMessage",
                "'" + politicianName + "' AI 분석을 백그라운드에서 시작했습니다. 잠시 후 새로고침해 주세요.");
        return "redirect:/admin/ai-draft";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        AiFlipFlopDraft draft = draftService.findById(id);
        model.addAttribute("draft", draft);

        FlipFlopForm form = new FlipFlopForm();
        form.setPoliticianName(draft.getPoliticianName());
        form.setTitle(draft.getTitle());
        form.setBeforeStatement(draft.getBeforeStatement());
        form.setBeforeDate(draft.getBeforeDate());
        form.setBeforeSource(draft.getBeforeSource());
        form.setAfterStatement(draft.getAfterStatement());
        form.setAfterDate(draft.getAfterDate());
        form.setAfterSource(draft.getAfterSource());
        model.addAttribute("flipFlopForm", form);

        return "admin/ai-draft-detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id,
                          @ModelAttribute FlipFlopForm flipFlopForm,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        Member admin = memberService.findByUsername(userDetails.getUsername());
        FlipFlop flipFlop = draftService.approve(id, flipFlopForm, admin);
        redirectAttributes.addFlashAttribute("successMessage", "승인되어 팩트로그에 등록되었습니다.");
        return "redirect:/flipflop/" + flipFlop.getId();
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        draftService.reject(id);
        redirectAttributes.addFlashAttribute("successMessage", "초안이 거절되었습니다.");
        return "redirect:/admin/ai-draft";
    }
}
