package org.fergoeqs.blps1.utils;

import jakarta.jms.Message;
import org.fergoeqs.blps1.dto.UpdateIssueKeyCommand;
import org.fergoeqs.blps1.dto.UpdateStatusCommand;
import org.fergoeqs.blps1.dto.CloseVacancyCommand;
import org.fergoeqs.blps1.services.ApplicationService;
import org.fergoeqs.blps1.services.VacancyService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MainServiceCommandListener {

    private final ApplicationService applicationService;
    private final VacancyService vacancyService;

    public MainServiceCommandListener(ApplicationService applicationService,
                                      VacancyService vacancyService) {
        this.applicationService = applicationService;
        this.vacancyService = vacancyService;
    }

    @JmsListener(destination = "main.service.commands")
    public void handleCommand(Object command, Message message) {
        try {
            String commandType = message.getJMSType();
            System.out.printf("Received command: %s%n", commandType);
            switch (commandType) {
                case "UpdateIssueKey":
                    if (command instanceof UpdateIssueKeyCommand) {
                        handleCloseVacancy((CloseVacancyCommand) command);
                    }
                    break;

                case "UpdateStatus":
                    if (command instanceof UpdateStatusCommand) {
                        handleUpdateStatus((UpdateStatusCommand) command);
                    }
                    break;


            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleUpdateIssueKey(UpdateIssueKeyCommand command) {
        applicationService.updateIssueKey(command.applicationId(), command.issueKey());
    }

    private void handleUpdateStatus(UpdateStatusCommand command) {
        applicationService.updateStatusByIssueKey(command.issueKey(), command.status());
    }

    private void handleCloseVacancy(CloseVacancyCommand command) {
        vacancyService.closeVacancyByIssueKey(command.issueKey());
    }
}