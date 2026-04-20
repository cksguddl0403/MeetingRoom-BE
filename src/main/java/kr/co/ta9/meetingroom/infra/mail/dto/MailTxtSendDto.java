package kr.co.ta9.meetingroom.infra.mail.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MailTxtSendDto {

    private String emailAddress; // 수신자 이메일

    private String subject;  // 이메일 제목

    private String content; // 이메일 내용

    @Builder
    public MailTxtSendDto(String emailAddress, String subject, String content) {
        this.emailAddress = emailAddress;
        this.subject = subject;
        this.content = content;
    }
}