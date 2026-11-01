package io.mycelar.example.twenty.twenty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.mycelar.example.twenty.support.TwentyDevContainer;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@Execution(ExecutionMode.SAME_THREAD)
class TwentyClientTest {

    @Container
    static final GenericContainer<?> TWENTY = TwentyDevContainer.create();

    private TwentyClient client;

    @BeforeEach
    void setUp() {
        RestClient rest =
                RestClient.builder()
                        .baseUrl(TwentyDevContainer.restBaseUrl(TWENTY))
                        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + TwentyDevContainer.SEEDED_ACCESS_TOKEN)
                        .build();
        client = new TwentyClient(rest);
    }

    @Test
    void listPeopleReturnsJson() {
        String body = client.list("people", null, 5);
        assertThat(body).contains("people");
    }

    @Test
    void createPersonPostsLockedShape() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "ada-" + suffix + "@example.com";
        String body =
                client.create(
                        "people",
                        Map.of(
                                "name",
                                Map.of("firstName", "Ada", "lastName", "Lovelace"),
                                "emails",
                                Map.of("primaryEmail", email)));

        assertThat(body).containsAnyOf("createPerson", "id", "people");
    }

    @Test
    void non2xxIsTwentyException() {
        assertThatThrownBy(() -> client.get("people", "00000000-0000-0000-0000-000000000000"))
                .isInstanceOf(TwentyException.class);
    }
}
