package com.teros.api_gateway.service.schedule;

import com.teros.api_gateway.service.local.LocalServiceImpl;
import com.teros.api_gateway.service.remote.RemoteServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class scheduleRoute {

    private final RemoteServiceImpl remoteServiceImpl;
    private final LocalServiceImpl localServiceImpl;

    public scheduleRoute(RemoteServiceImpl remoteServiceImpl, LocalServiceImpl localServiceImpl) {
        this.remoteServiceImpl = remoteServiceImpl;
        this.localServiceImpl = localServiceImpl;
    }

    @Scheduled(fixedDelay = 10000)
    public void remoteService() {
        String routerRule = getRouterRule();

        createRouter("router-create-test", routerRule);
        refreshRouter();
    }

    public String getRouterRule() {
        System.out.println("GET ROUTER RULE");
        return remoteServiceImpl.getRoute();
    }

    public void createRouter(String id, String body) {
        System.out.println("CREATE ROUTER");
        localServiceImpl.createRouter(id, body);
    }

    public void refreshRouter() {
        System.out.println("REFRESH ROUTER");
        localServiceImpl.refreshRouter();
    }
}
