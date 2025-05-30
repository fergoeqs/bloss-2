package org.fergoeqs.blps1.services;

import org.fergoeqs.blps1.dto.ApplicationRequest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationProcessor {

    private final ApplicationService applicationService;

    public ApplicationProcessor(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @JmsListener(destination = "${app.queue.application}")
    @Transactional
    public void processApplication(ApplicationRequest request) {
        try {
            applicationService.createApplication(request);
        } catch (Exception e) {
            // Логика обработки ошибок и ретрая
            handleProcessingError(request, e);
        }
    }

    private void handleProcessingError(ApplicationRequest request, Exception e) {
        // Здесь будет логика ретрая и обработки ошибок
        // Пока просто логируем
        System.err.println("Ошибка обработки заявки: " + e.getMessage());
        e.printStackTrace();
    }
}