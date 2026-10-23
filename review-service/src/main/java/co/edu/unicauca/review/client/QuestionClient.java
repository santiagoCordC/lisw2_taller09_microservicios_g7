package co.edu.unicauca.review.client;

import co.edu.unicauca.review.client.dto.QuestionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class QuestionClient {

    private final RestClient restClient;

    public QuestionClient(RestClient.Builder builder,
                          @Value("${services.question.url}") String baseUrl,
                          @Value("${services.timeout-ms:3000}") int timeoutMs) {
        this.restClient = builder.baseUrl(baseUrl)
                .requestFactory(RemoteCallSupport.requestFactory(timeoutMs))
                .build();
    }

    /** @return la pregunta o null si no existe. */
    public QuestionDTO findQuestion(Long questionId) {
        try {
            return restClient.get().uri("/questions/{id}", questionId).retrieve().body(QuestionDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        } catch (RestClientException ex) {
            throw RemoteCallSupport.translate("Question Service", ex);
        }
    }
}
