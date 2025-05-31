package org.fergoeqs.blps1.config;


import org.fergoeqs.blps1.jobs.RejectOldApplicationsJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail rejectOldApplicationsJobDetail() {
        return JobBuilder.newJob(RejectOldApplicationsJob.class)
                .withIdentity("rejectOldApplicationsJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger rejectOldApplicationsTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(rejectOldApplicationsJobDetail())
                .withIdentity("rejectOldApplicationsTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?"))
                .build();
    }
}
