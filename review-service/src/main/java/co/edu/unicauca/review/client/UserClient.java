package co.edu.unicauca.review.client;

import co.edu.unicauca.review.client.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserClient {

    private final RestClient restClient;

    public UserClient(RestClient.Builder builder,
                      @Value("${services.user.url}") String baseUrl,
                      @Value("${services.timeout-ms:3000}") int timeoutMs) {
        this.restClient = builder.baseUrl(baseUrl)
                .requestFactory(RemoteCallSupport.requestFactory(timeoutMs))
                .build();
    }

    /** @return el usuario o null si no existe. */
    public UserDTO findUser(Long userId) {
        try {
            return restClient.get().uri("/users/{id}", userId).retrieve().body(UserDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        } catch (RestClientException ex) {
            throw RemoteCallSupport.translate("User Service", ex);
        }
    }
}
