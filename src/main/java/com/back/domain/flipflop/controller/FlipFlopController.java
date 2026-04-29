package com.back.domain.flipflop.controller;

import com.back.domain.comment.entity.Comment;
import com.back.domain.comment.service.CommentService;
import com.back.domain.flipflop.dto.FlipFlopForm;
import com.back.domain.flipflop.entity.FlipFlop;
import com.back.domain.flipflop.service.FlipFlopService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/flipflop")
@RequiredArgsConstructor
public class FlipFlopController {

    private final FlipFlopService flipFlopService;
    private final CommentService commentService;
    private final MemberService memberService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "") String keyword) {
        Page<FlipFlop> flipFlopPage = flipFlopService.getList(page, keyword);
        model.addAttribute("flipFlopPage", flipFlopPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        return "flipflop/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model,
                         @AuthenticationPrincipal UserDetails userDetails) {
        FlipFlop flipFlop = flipFlopService.getDetail(id);
        model.addAttribute("flipFlop", flipFlop);
        model.addAttribute("newComment", new Comment());
        if (userDetails != null) {
            model.addAttribute("currentMember", memberService.findByUsername(userDetails.getUsername()));
        }
        return "flipflop/detail";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("flipFlopForm", new FlipFlopForm());
        return "flipflop/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute FlipFlopForm flipFlopForm,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "flipflop/form";
        }
        Member member = memberService.findByUsername(userDetails.getUsername());
        FlipFlop flipFlop = flipFlopService.create(flipFlopForm, member);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 등록되었습니다.");
        return "redirect:/flipflop/" + flipFlop.getId();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        FlipFlop flipFlop = flipFlopService.findById(id);
        FlipFlopForm form = new FlipFlopForm();
        form.setPoliticianName(flipFlop.getPoliticianName());
        form.setTitle(flipFlop.getTitle());
        form.setBeforeStatement(flipFlop.getBeforeStatement());
        form.setBeforeSource(flipFlop.getBeforeSource());
        form.setBeforeDate(flipFlop.getBeforeDate());
        form.setAfterStatement(flipFlop.getAfterStatement());
        form.setAfterSource(flipFlop.getAfterSource());
        form.setAfterDate(flipFlop.getAfterDate());
        model.addAttribute("flipFlopForm", form);
        model.addAttribute("flipFlopId", id);
        return "flipflop/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute FlipFlopForm flipFlopForm,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "flipflop/form";
        }
        flipFlopService.update(id, flipFlopForm);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 수정되었습니다.");
        return "redirect:/flipflop/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        flipFlopService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 삭제되었습니다.");
        return "redirect:/flipflop";
    }
}
