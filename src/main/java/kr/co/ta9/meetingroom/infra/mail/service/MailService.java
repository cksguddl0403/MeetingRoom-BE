package kr.co.ta9.meetingroom.infra.mail.service;

import kr.co.ta9.meetingroom.infra.mail.dto.MailTxtSendDto;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    // 이메일 발송
    public void sendSimpleMail(MailTxtSendDto mailTxtSendDto) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setTo(mailTxtSendDto.getEmailAddress());
        mailMessage.setSubject(mailTxtSendDto.getSubject());
        mailMessage.setText(mailTxtSendDto.getContent());

        try {
            mailSender.send(mailMessage);
            System.out.println("이메일 전송 성공!");
        } catch (MailException e) {
            System.out.println("[-] 이메일 전송중에 오류가 발생하였습니다 " + e.getMessage());
            throw e;
        }
    }
}
