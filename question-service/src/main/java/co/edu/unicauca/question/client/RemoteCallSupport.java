package co.edu.unicauca.question.client;

import co.edu.unicauca.question.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Utilidades comunes para las llamadas REST síncronas: timeouts y traducción de errores
 * remotos a ApiException (para no propagar excepciones técnicas al cliente).
 */
public final class RemoteCallSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RemoteCallSupport() {
    }

    /** Fábrica con timeouts: una dependencia lenta no debe bloquear indefinidamente el hilo. */
    public static SimpleClientHttpRequestFactory requestFactory(int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return factory;
    }

    public static ApiException translate(String serviceName, RestClientException ex) {
        if (ex instanceof RestClientResponseException response) {
            HttpStatus status = HttpStatus.resolve(response.getStatusCode().value());
            if (status == null || status.is5xxServerError()) {
                return new ApiException(HttpStatus.BAD_GATEWAY,
                        serviceName + " respondió con error " + response.getStatusCode().value()
                                + ": " + extractMessage(response));
            }
            return new ApiException(status, extractMessage(response));
        }
        // Conexión rechazada, timeout, DNS... => el servicio no está disponible
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                serviceName + " no está disponible en este momento (" + ex.getMessage() + ")");
    }

    private static String extractMessage(RestClientResponseException response) {
        try {
            JsonNode node = MAPPER.readTree(response.getResponseBodyAsString());
            if (node != null && node.hasNonNull("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {
            // cuerpo no JSON
        }
        return response.getStatusText();
    }
}
