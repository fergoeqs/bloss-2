package org.fergoeqs.blps1.jobs;


import org.fergoeqs.blps1.models.applicantdb.Application;
import org.fergoeqs.blps1.models.enums.ApplicationStatus;
import org.fergoeqs.blps1.repositories.applicantdb.ApplicationRepository;
import org.fergoeqs.blps1.services.ApplicationService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RejectOldApplicationsJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(RejectOldApplicationsJob.class);
    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;

    public RejectOldApplicationsJob(ApplicationRepository applicationRepository, ApplicationService applicationService) {
        this.applicationRepository = applicationRepository;
        this.applicationService = applicationService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(14);

        List<Application> oldApplications = applicationRepository
                .findByStatusAndCreatedAtBefore(ApplicationStatus.PENDING, thresholdDate);

        log.info("Found {} old applications to reject", oldApplications.size());

        oldApplications.forEach(application -> {
            applicationService.rejectApplication(application.getId(), (long) -1);
        });
    }
}
