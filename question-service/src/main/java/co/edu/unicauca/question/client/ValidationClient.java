package co.edu.unicauca.question.client;

import co.edu.unicauca.question.client.dto.ValidationRequestDTO;
import co.edu.unicauca.question.client.dto.ValidationResultDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Cliente REST síncrono hacia Validation Service (POST /validate). */
@Component
public class ValidationClient {

    private final RestClient restClient;

    public ValidationClient(RestClient.Builder builder,
                            @Value("${services.validation.url}") String baseUrl,
                            @Value("${services.timeout-ms:3000}") int timeoutMs) {
        this.restClient = builder.baseUrl(baseUrl)
                .requestFactory(RemoteCallSupport.requestFactory(timeoutMs))
                .build();
    }

    public ValidationResultDTO validate(ValidationRequestDTO request) {
        try {
            return restClient.post()
                    .uri("/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ValidationResultDTO.class);
        } catch (RestClientException ex) {
            throw RemoteCallSupport.translate("Validation Service", ex);
        }
    }
}
