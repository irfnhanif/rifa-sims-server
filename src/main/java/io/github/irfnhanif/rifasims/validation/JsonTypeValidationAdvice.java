package io.github.irfnhanif.rifasims.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

@ControllerAdvice
public class JsonTypeValidationAdvice implements RequestBodyAdvice {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        // Clear any previous data
        ThreadLocalTypeInfo.clear();

        // Read input stream to byte array to allow reuse
        InputStream inputStream = inputMessage.getBody();
        byte[] body = inputStream.readAllBytes();

        // Process the JSON to track types
        JsonNode rootNode = objectMapper.readTree(new ByteArrayInputStream(body));
        processJsonNode(rootNode, "");

        // Return a new input message with the same body for Spring's normal processing
        return new MappingJacksonInputMessage(new ByteArrayInputStream(body), inputMessage.getHeaders());
    }

    private void processJsonNode(JsonNode node, String path) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();

                String fieldPath = path.isEmpty() ? fieldName : path + "." + fieldName;

                if (fieldValue.isValueNode()) {
                    boolean isString = fieldValue.isTextual();
                    ThreadLocalTypeInfo.setIsString(fieldPath, isString);

                    // For non-string values, also store the string representation for lookup
                    if (!isString && !fieldValue.isNull()) {
                        ThreadLocalTypeInfo.setIsString(fieldValue.asText(), false);
                    }
                } else {
                    processJsonNode(fieldValue, fieldPath);
                }
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                processJsonNode(node.get(i), path + "[" + i + "]");
            }
        }
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
}