package com.teros.api_gateway.schedule;

import com.teros.api_gateway.common.network.RemoteNetworkCall;
import com.teros.ext.common.file.CommonFile;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Slf4j
@Component
public class scheduleRoute {

    @Value("${extra.env.teros_home}")
    private String terosHomeEnv;


    @Value("${server.port}")
    private String servicePort;

    @Scheduled(fixedDelay = 3000)
    public void updateRouteRule() {

        try {
            CommonFile commonFile = new CommonFile();
            RemoteNetworkCall remoteNetworkCall = new RemoteNetworkCall();
            String configDir = String.format("%s/api-service/config", terosHomeEnv);
            ResponseEntity responseEntity = null;
            boolean bRouteFound = true;
            boolean bRouteChange = false;

            // search config files
            File dir = new File(configDir);
            File files[] = dir.listFiles();

            for (int i = 0; i < files.length; i++) {
                String configFilePath = files[i].toString();

                // create route
                String contents = new String(commonFile.readFile(configFilePath));
                JSONObject jObject = new JSONObject(contents);
                String apiId = jObject.getString("id");

                try {
                    responseEntity = remoteNetworkCall.requestRestEntity(
                            String.format("http://127.0.0.1:%s/actuator/gateway/routes/%s", servicePort, apiId)
                            , HttpMethod.GET, "");
                } catch (Exception ex) {
                    if(ex.toString().contains("404"))
                        bRouteFound = false;
                }

                if (bRouteFound == false) {
                    bRouteChange = true;

                    // create route
                    remoteNetworkCall.requestRestEntity(
                            String.format("http://127.0.0.1:%s/actuator/gateway/routes/%s", servicePort, apiId)
                            , HttpMethod.POST, contents);
                }
            }

            if(bRouteChange == true) {
                // refresh route
                remoteNetworkCall.requestRestEntity(
                        String.format("http://127.0.0.1:%s/actuator/gateway/refresh", servicePort)
                        , HttpMethod.POST, "");
            }
        } catch (Exception ex) {
            log.error(ex.toString());
        }
    }
}

