package com.crm.email.scheduler;

import com.crm.email.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderDispatchJob implements Job {

    private final ReminderService reminderService;

    @Override
    public void execute(JobExecutionContext context) {
        log.debug("ReminderDispatchJob triggered");
        reminderService.dispatchDueReminders();
    }
}
