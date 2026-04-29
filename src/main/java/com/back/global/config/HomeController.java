package com.back.global.config;

import com.back.domain.flipflop.service.FlipFlopService;
import com.back.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final FlipFlopService flipFlopService;
    private final PostService postService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recentFlipFlops", flipFlopService.getList(0, "").getContent());
        model.addAttribute("hotPosts", postService.getHotPosts());
        model.addAttribute("recentPosts", postService.getList(0, "").getContent());
        return "home";
    }
}
