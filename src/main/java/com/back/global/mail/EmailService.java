package com.back.global.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[팩트로그] 비밀번호 재설정 안내");
            message.setText("""
                    안녕하세요, 팩트로그입니다.

                    비밀번호 재설정 요청이 접수되었습니다.
                    아래 링크를 클릭하여 새 비밀번호를 설정해주세요.

                    %s

                    ※ 이 링크는 1시간 후 만료됩니다.
                    ※ 본인이 요청하지 않았다면 이 메일을 무시하세요.

                    — 팩트로그 팀
                    """.formatted(resetLink));
            mailSender.send(message);
            log.info("비밀번호 재설정 이메일 발송 완료: {}", toEmail);
        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}
