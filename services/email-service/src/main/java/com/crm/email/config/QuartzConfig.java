package com.crm.email.config;

import com.crm.email.scheduler.CampaignDispatchJob;
import com.crm.email.scheduler.ReminderDispatchJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail reminderJobDetail() {
        return JobBuilder.newJob(ReminderDispatchJob.class)
            .withIdentity("reminderDispatchJob")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger reminderTrigger(JobDetail reminderJobDetail) {
        return TriggerBuilder.newTrigger()
            .forJob(reminderJobDetail)
            .withIdentity("reminderDispatchTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?"))  // every minute
            .build();
    }

    @Bean
    public JobDetail campaignJobDetail() {
        return JobBuilder.newJob(CampaignDispatchJob.class)
            .withIdentity("campaignDispatchJob")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger campaignTrigger(JobDetail campaignJobDetail) {
        return TriggerBuilder.newTrigger()
            .forJob(campaignJobDetail)
            .withIdentity("campaignDispatchTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?"))  // every minute
            .build();
    }
}
