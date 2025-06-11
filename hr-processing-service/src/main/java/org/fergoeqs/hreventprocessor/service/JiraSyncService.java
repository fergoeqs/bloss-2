package org.fergoeqs.hreventprocessor.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.fergoeqs.hreventprocessor.DTOs.JiraEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class JiraSyncService {
    @Value("${jira.project-key}")
    private String projectKey;

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TransitionsResponse {
        public List<Transition> transitions;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Transition {
        public String id;
        public Status to;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Status {
        public String name;
    }

    private static final String JIRA_API_PATH = "/rest/api/2/issue";
    private static final String JIRA_TRANSITION_PATH = "/rest/api/2/issue/%s/transitions";

    @Value("${jira.base-url}")
    private String jiraBaseUrl;

    @Value("${jira.username}")
    private String username;

    @Value("${jira.api-token}")
    private String apiToken;

    @Value("${jira.issue-type}")
    private String issueType;

    private final RestTemplate restTemplate = new RestTemplate();

    private List<Transition> getAvailableTransitions(String issueKey) {
        String url = String.format(jiraBaseUrl + JIRA_TRANSITION_PATH, issueKey);
        HttpHeaders headers = createJiraHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<TransitionsResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                TransitionsResponse.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().transitions;
        }
        throw new RuntimeException("Failed to get transitions for issue: " + issueKey);
    }

    @Retryable(
            value = {HttpServerErrorException.class, HttpClientErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public String createIssue(JiraEvent event) {
        String url = jiraBaseUrl + JIRA_API_PATH;

        String requestBody = String.format(
                "{ \"fields\": {"
                        + "\"project\": {\"key\": \"%s\"},"
                        + "\"summary\": \"Кандидат: %s на вакансию: %s\","
                        + "\"description\": \"Кандидат %s приглашен на собеседование по вакансии %s. Email: %s\","
                        + "\"issuetype\": {\"name\": \"%s\"}"
                        + "}}",
                projectKey,
                event.candidateName(),
                event.vacancyTitle(),
                event.candidateName(),
                event.vacancyTitle(),
                event.candidateEmail(),
                issueType
        );

        HttpHeaders headers = createJiraHeaders();
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Ошибка создания задачи в Jira: " + response.getBody());
        }


        String responseBody = response.getBody();
        String issueKey = "REC-" + System.currentTimeMillis();
        return issueKey;
    }

    @Retryable(
            value = {HttpServerErrorException.class, HttpClientErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public void transitionIssue(String issueKey, String statusName) {
        List<Transition> transitions = getAvailableTransitions(issueKey);

        Optional<Transition> targetTransition = transitions.stream()
                .filter(t -> statusName.equalsIgnoreCase(t.to.name))
                .findFirst();

        if (targetTransition.isEmpty()) {
            throw new IllegalArgumentException(
                    "No transition found to status: " + statusName + " for issue: " + issueKey
            );
        }

        executeTransition(issueKey, targetTransition.get().id);
    }

    private void executeTransition(String issueKey, String transitionId) {
        String url = String.format(jiraBaseUrl + JIRA_TRANSITION_PATH, issueKey);
        String requestBody = "{\"transition\":{\"id\":\"" + transitionId + "\"}}";

        HttpHeaders headers = createJiraHeaders();
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        restTemplate.postForEntity(url, request, String.class);
    }

    private HttpHeaders createJiraHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(username, apiToken);
        return headers;
    }
}