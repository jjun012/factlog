package com.back.domain.newstip.controller;

import com.back.domain.newstip.entity.NewsTip;
import com.back.domain.newstip.service.NewsTipService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/news-tip")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminNewsTipController {

    private final NewsTipService newsTipService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "PENDING") String status) {
        if ("ALL".equals(status)) {
            model.addAttribute("tips", newsTipService.getAllTips());
        } else {
            model.addAttribute("tips", newsTipService.getPendingTips());
        }
        model.addAttribute("currentStatus", status);
        model.addAttribute("pendingCount", newsTipService.countPending());
        return "admin/news-tip-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("tip", newsTipService.findById(id));
        return "admin/news-tip-detail";
    }

    @PostMapping("/{id}/review")
    public String review(@PathVariable Long id,
                         @RequestParam(defaultValue = "") String adminNote,
                         RedirectAttributes redirectAttributes) {
        newsTipService.review(id, adminNote);
        redirectAttributes.addFlashAttribute("successMessage", "제보를 검토 완료로 처리했습니다.");
        return "redirect:/admin/news-tip";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(defaultValue = "") String adminNote,
                         RedirectAttributes redirectAttributes) {
        newsTipService.reject(id, adminNote);
        redirectAttributes.addFlashAttribute("successMessage", "제보를 거절 처리했습니다.");
        return "redirect:/admin/news-tip";
    }
}
