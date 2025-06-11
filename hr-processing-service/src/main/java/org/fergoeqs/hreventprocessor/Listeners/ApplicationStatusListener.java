package org.fergoeqs.hreventprocessor.Listeners;

import jakarta.jms.Message;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.fergoeqs.hreventprocessor.DTOs.ApplicationStatusEvent;
import org.fergoeqs.hreventprocessor.DTOs.JiraEvent;
import org.fergoeqs.hreventprocessor.DTOs.CloseVacancyCommand;
import org.fergoeqs.hreventprocessor.DTOs.UpdateIssueKeyCommand;
import org.fergoeqs.hreventprocessor.DTOs.UpdateStatusCommand;
import org.fergoeqs.hreventprocessor.service.JiraSyncService;
import org.fergoeqs.hreventprocessor.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationStatusListener {

    private final MailService mailService;
    private final JiraSyncService jiraSyncService;
    private final RetryTemplate retryTemplate;
    private final JmsTemplate jmsTemplate;

    public ApplicationStatusListener(
            MailService mailService,
            JiraSyncService jiraSyncService,
            RetryTemplate retryTemplate,
            JmsTemplate jmsTemplate) {
        this.mailService = mailService;
        this.jiraSyncService = jiraSyncService;
        this.retryTemplate = retryTemplate;
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = "applications.queue")
    public void handleApplicationEvent(ApplicationStatusEvent event, Message message) {
        try {
            String eventType = message.getJMSType();

            switch (eventType) {
                case "CreateJiraIssue":
                    handleJiraCreation(event);
                    break;
                case "StatusChanged":
                    handleStatusChange(event);
                    break;
                case "StatusChangedFromJira":
                    handleStatusChangeFromJira(event);
                    break;
                case "InterviewScheduled":
                    log.info("Interview scheduled event received");
                    break;
                default:
                    log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing application event: {}", event, e);
        }
    }

    private void handleJiraCreation(ApplicationStatusEvent event) {
        retryTemplate.execute(context -> {
            JiraEvent jiraEvent = new JiraEvent(
                    event.candidateEmail(),
                    event.candidateName(),
                    event.vacancyTitle(),
                    null
            );

            String issueKey = jiraSyncService.createIssue(jiraEvent);
            sendUpdateIssueKeyCommand(event.applicationId(), issueKey);
            return null;
        });
    }

    private void sendUpdateIssueKeyCommand(Long applicationId, String issueKey) {
        jmsTemplate.convertAndSend("main.service.commands",
                new UpdateIssueKeyCommand(applicationId, issueKey),
                message -> {
                    message.setJMSType("UpdateIssueKey");
                    return message;
                }
        );
    }

    private void handleStatusChange(ApplicationStatusEvent event) throws MessagingException {
        retryTemplate.execute(context -> {
            switch (event.status()) {
                case "ACCEPTED":
                    // Уже обрабатывается в handleJiraCreation
                    break;
                case "REJECTED":
                    handleRejectedStatus(event);
                    break;
                case "HIRED":
                    handleHiredStatus(event);
                    break;
            }
            return null;
        });
    }

    private void handleRejectedStatus(ApplicationStatusEvent event) throws MessagingException {
        if (event.issueKey() != null) {
            jiraSyncService.transitionIssue(event.issueKey(), "Rejected");
        }
        mailService.sendRejectionEmail(event);
    }

    private void handleHiredStatus(ApplicationStatusEvent event) throws MessagingException {
        if (event.issueKey() != null) {
            jiraSyncService.transitionIssue(event.issueKey(), "Hired");
        }
        mailService.sendHiringEmail(event);
    }

    private void handleStatusChangeFromJira(ApplicationStatusEvent event) {
        retryTemplate.execute(context -> {
            switch (event.status()) {
                case "HIRED":
                    sendCloseVacancyCommand(event.issueKey());
                    sendUpdateStatusCommand(event.issueKey(), "HIRED");
                    break;
                case "REJECTED":
                    sendUpdateStatusCommand(event.issueKey(), "REJECTED");
                    break;
            }
            return null;
        });
    }

    private void sendUpdateStatusCommand(String issueKey, String status) {
        jmsTemplate.convertAndSend("main.service.commands",
                new UpdateStatusCommand(issueKey, status),
                message -> {
                    message.setJMSType("UpdateStatus");
                    return message;
                }
        );
    }

    private void sendCloseVacancyCommand(String issueKey) {
        jmsTemplate.convertAndSend("main.service.commands",
                new CloseVacancyCommand(issueKey),
                message -> {
                    message.setJMSType("CloseVacancy");
                    return message;
                }
        );
    }
}