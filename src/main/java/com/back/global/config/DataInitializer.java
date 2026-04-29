package com.back.global.config;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!memberRepository.existsByUsername("admin")) {
            Member admin = new Member(
                    "admin",
                    passwordEncoder.encode("admin1234"),
                    "admin@politicscheck.com",
                    "관리자"
            );
            admin.setRole(Member.Role.ADMIN);
            memberRepository.save(admin);
            log.info("관리자 계정 생성 완료 - username: admin / password: admin1234");
        }
    }
}
