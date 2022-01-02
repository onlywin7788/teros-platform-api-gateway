package com.teros.api_gateway.common.network;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class RemoteNetworkCall {

    public ResponseEntity<String> requestRestEntity(String url, HttpMethod method, String payload) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<String>(payload, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(url,
                method, entity, String.class);

        log.info(String.format("REST::method:[%s], url:[%s], response_status:[%d]", method, url, responseEntity.getStatusCodeValue()));
        return responseEntity;
    }

}
