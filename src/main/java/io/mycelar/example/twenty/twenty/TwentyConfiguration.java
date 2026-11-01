package io.mycelar.example.twenty.twenty;

import io.mycelar.core.constraint.ConstraintEvaluator;
import io.mycelar.core.graph.MycelarGraph;
import io.mycelar.example.twenty.constraint.ConfirmBeforeSendEvaluator;
import io.mycelar.example.twenty.graph.TwentyGraph;
import io.mycelar.example.twenty.tool.TwentyTools;
import java.util.Objects;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TwentyProperties.class)
public class TwentyConfiguration {

    @Bean
    MycelarGraph mycelarGraph() {
        return TwentyGraph.graph();
    }

    @Bean
    ConstraintEvaluator confirmBeforeSendEvaluator() {
        return new ConfirmBeforeSendEvaluator();
    }

    @Bean
    RestClient twentyRestClient(TwentyProperties properties) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException("twenty.api-key / TWENTY_API_KEY is required");
        }
        return RestClient.builder()
                .baseUrl(properties.restBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .build();
    }

    @Bean
    TwentyClient twentyClient(RestClient twentyRestClient) {
        return new TwentyClient(twentyRestClient);
    }

    @Bean
    TwentyTools twentyTools(TwentyClient twentyClient) {
        return new TwentyTools(twentyClient);
    }

    @Bean
    ToolCallbackProvider twentyToolCallbackProvider(TwentyTools twentyTools) {
        return MethodToolCallbackProvider.builder().toolObjects(twentyTools).build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return Objects.requireNonNull(builder, "builder").build();
    }
}
