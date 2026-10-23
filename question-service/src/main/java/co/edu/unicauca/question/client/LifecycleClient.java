package co.edu.unicauca.question.client;

import co.edu.unicauca.question.client.dto.LifecycleOperationDTO;
import co.edu.unicauca.question.client.dto.LifecycleStateDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

/** Cliente REST síncrono hacia Lifecycle Service. */
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

    /** Registra la pregunta recién creada en estado DRAFT. */
    public LifecycleStateDTO register(Long questionId, Long requestedBy) {
        try {
            return restClient.post()
                    .uri("/lifecycle/questions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("questionId", questionId, "requestedBy", requestedBy))
                    .retrieve()
                    .body(LifecycleStateDTO.class);
        } catch (RestClientException ex) {
            throw RemoteCallSupport.translate(SERVICE, ex);
        }
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

    /** Devuelve null si Lifecycle aún no tiene registro de la operación. */
    public LifecycleOperationDTO getOperation(String operationId) {
        try {
            return restClient.get().uri("/lifecycle/operations/{id}", operationId)
                    .retrieve().body(LifecycleOperationDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        } catch (RestClientException ex) {
            throw RemoteCallSupport.translate(SERVICE, ex);
        }
    }
}
