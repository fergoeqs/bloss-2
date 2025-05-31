package org.fergoeqs.hreventprocessor.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.fergoeqs.hreventprocessor.DTOs.ApplicationStatusEvent;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Service
public class MailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;


    public MailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendEmailNotification(String email, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("example@yandex.ru");
        mailSender.send(message);
    }

    public void sendInterviewInvitation(ApplicationStatusEvent invitation) throws MessagingException {
        Context context = new Context();
        context.setVariable("candidateName", invitation.candidateName());
        context.setVariable("vacancyTitle", invitation.vacancyTitle());
//        context.setVariable("interviewDate", invitation.interviewDate());

        String htmlContent = templateEngine.process(
                "interview-invitation",
                context
        );

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(invitation.candidateEmail());
        helper.setSubject("Приглашение на собеседование");
        helper.setText(htmlContent, true);
        helper.setFrom("example@yandex.ru");

        mailSender.send(message);
    }

    public void sendRejectionEmail(ApplicationStatusEvent rejection) throws MessagingException {
        Context context = new Context();
        context.setVariable("candidateName", rejection.candidateName());
        context.setVariable("vacancyTitle", rejection.vacancyTitle());

        String htmlContent = templateEngine.process(
                "rejection",
                context
        );

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(rejection.candidateEmail());
        helper.setSubject("Ответ по вакансии " + rejection.vacancyTitle());
        helper.setText(htmlContent, true);
        helper.setFrom("example@yandex.ru");

        mailSender.send(message);
    }

}

