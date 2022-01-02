package com.teros.api_gateway.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class scheduleRoute {

    @Value("${extra.env.teros_home}")
    private String terosHomeEnv;

    @Scheduled(fixedDelay = 3000)
    public void updateRouteRule() {
        log.info("update rule");
    }
}
