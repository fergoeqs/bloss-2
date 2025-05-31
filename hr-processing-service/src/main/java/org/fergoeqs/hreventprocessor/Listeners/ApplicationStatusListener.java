package org.fergoeqs.hreventprocessor.Listeners;

import jakarta.jms.Message;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.fergoeqs.hreventprocessor.DTOs.ApplicationStatusEvent;
import org.fergoeqs.hreventprocessor.service.MailService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStatusListener {

    @Component
    @Slf4j
    public static class ApplicationQueueListener {
        private final MailService mailService;

        public ApplicationQueueListener(MailService mailService) {
            this.mailService = mailService;
        }

        @JmsListener(destination = "applications.queue")
        public void handleApplicationEvent(ApplicationStatusEvent event, Message message) {
            try {
                String messageType = message.getJMSType();

                switch (messageType) {
                    case "ApplicationRejected":
                        handleRejectedApplication(event);
                        break;
                    case "ApplicationAccepted":
                        handleApprovedApplication(event);
                        break;
                    default:
                        log.warn("Unknown message type: {}", messageType);
                }
            } catch (Exception e) {
                log.error("Error processing application event: {}", event, e);
                // Можно добавить логику повторной попытки или перемещения в DLQ
                throw new RuntimeException(e);
            }
        }

        private void handleRejectedApplication(ApplicationStatusEvent event) throws MessagingException {
            // Логика обработки отказа
            log.info("Processing rejected application for applicant {} (vacancy: {})",
                    event.candidateName(),
                    event.vacancyTitle());

            mailService.sendRejectionEmail(event);

            // Отправка email уведомления и т.д.
            // yourEmailService.sendRejectionEmail(event.getApplicantEmail(), ...);
        }

        private void handleApprovedApplication(ApplicationStatusEvent event) throws MessagingException {
            // Логика обработки приглашения
            log.info("Processing approved application for applicant {} (vacancy: {})",
                    event.candidateName(),
                    event.vacancyTitle());

            mailService.sendInterviewInvitation(event);
            // Отправка email приглашения и т.д.
            // yourEmailService.sendInvitationEmail(event.getApplicantEmail(), ...);
        }
    }
}