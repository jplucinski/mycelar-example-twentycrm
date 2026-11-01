package io.mycelar.example.twenty.twenty;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class TwentyClientValidationTest {

    @Test
    void createNoteTargetRequiresExactlyOneTarget() {
        TwentyClient client = new TwentyClient(RestClient.builder().baseUrl("http://localhost/rest").build());
        assertThatThrownBy(() -> client.createNoteTarget("n1", null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client.createNoteTarget("n1", "p1", "c1", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
