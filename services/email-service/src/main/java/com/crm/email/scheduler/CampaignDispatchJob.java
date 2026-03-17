package com.crm.email.scheduler;

import com.crm.email.enums.CampaignStatus;
import com.crm.email.repository.EmailCampaignRepository;
import com.crm.email.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignDispatchJob implements Job {

    private final EmailCampaignRepository campaignRepository;
    private final CampaignService         campaignService;

    @Override
    public void execute(JobExecutionContext context) {
        var due = campaignRepository.findByStatusAndScheduledAtBefore(CampaignStatus.SCHEDULED, Instant.now());
        if (!due.isEmpty()) {
            log.info("Dispatching {} scheduled campaigns", due.size());
            due.forEach(c -> campaignService.dispatch(c.getId()));
        }
    }
}
