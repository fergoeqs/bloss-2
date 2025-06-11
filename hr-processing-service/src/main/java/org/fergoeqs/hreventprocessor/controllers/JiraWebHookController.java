package org.fergoeqs.hreventprocessor.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.fergoeqs.hreventprocessor.DTOs.ApplicationStatusEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jira-webhook")
public class JiraWebHookController {

    private final JmsTemplate jmsTemplate;

    public JiraWebHookController(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @PostMapping
    public ResponseEntity<?> handleWebhook(@RequestBody JiraWebhookPayload payload) {
        if ("HIRED".equals(payload.getNewStatus()) || "REJECTED".equals(payload.getNewStatus())) {
            ApplicationStatusEvent event = new ApplicationStatusEvent(
                    null,
                    null,
                    null,
                    payload.getIssueKey(),
                    payload.getNewStatus(),
                    payload.getNewStatus()
            );

            jmsTemplate.convertAndSend(
                    "applications.queue",
                    event,
                    message -> {
                        message.setJMSType("StatusChangedFromJira");
                        return message;
                    }
            );
        }
        return ResponseEntity.ok().build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class JiraWebhookPayload {
        private String issueKey;
        private String newStatus;

    }
}
