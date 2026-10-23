package co.edu.unicauca.question.client;

import co.edu.unicauca.question.client.dto.UserDTO;
import co.edu.unicauca.question.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Cliente REST síncrono hacia User Service (verificación de usuario y roles). */
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

    public UserDTO getUser(Long userId) {
        try {
            return restClient.get().uri("/users/{id}", userId).retrieve().body(UserDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "El usuario " + userId + " no existe");
        } catch (RestClientException ex) {
            throw RemoteCallSupport.translate("User Service", ex);
        }
    }
}
