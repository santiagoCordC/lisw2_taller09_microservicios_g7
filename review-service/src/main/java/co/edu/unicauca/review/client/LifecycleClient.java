package co.edu.unicauca.review.client;

import co.edu.unicauca.review.client.dto.LifecycleStateDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

/** Review Service nunca cambia el estado por su cuenta: se lo solicita a Lifecycle Service. */
@Component
public class LifecycleClient {

    private static final String SERVICE = "Lifecycle Service";

    private final RestClient restClient;

    public LifecycleClient(RestClient.Builder builder,
                           @Value("${services.lifecycle.url}") String baseUrl,
                           @Value("${services.timeout-ms:3000}") int timeoutMs) {
        this.restClient = builder.baseUrl(baseUrl)
                .requestFactory(RemoteCallSupport.requestFactory(timeoutMs))
                .build();
    }

    public LifecycleStateDTO getState(Long questionId) {
        try {
            return restClient.get().uri("/lifecycle/{id}", questionId).retrieve().body(LifecycleStateDTO.class);
        } catch (RestClientException ex) {
            throw RemoteCallSupport.translate(SERVICE, ex);
        }
    }

    public LifecycleStateDTO transition(Long questionId, String targetState, Long requestedBy, String comment) {
        Map<String, Object> body = new HashMap<>();
        body.put("targetState", targetState);
        body.put("requestedBy", requestedBy);
        body.put("comment", comment);
        try {
            return restClient.post()
                    .uri("/lifecycle/{id}/transition", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(LifecycleStateDTO.class);
        } catch (RestClientException ex) {
            throw RemoteCallSupport.translate(SERVICE, ex);
        }
    }
}
