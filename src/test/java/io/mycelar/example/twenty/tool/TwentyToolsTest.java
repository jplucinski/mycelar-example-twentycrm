package io.mycelar.example.twenty.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.mycelar.example.twenty.twenty.TwentyClient;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TwentyToolsTest {

    @Mock
    private TwentyClient client;

    @Test
    void searchPeopleDelegates() {
        when(client.list("people", "name.firstName[eq]:Ada", 10)).thenReturn("{\"ok\":true}");
        String out = new TwentyTools(client).searchPeople("name.firstName[eq]:Ada", 10);
        assertThat(out).isEqualTo("{\"ok\":true}");
    }

    @Test
    void createPersonUsesLockedBody() {
        when(client.create(
                        eq("people"),
                        eq(
                                Map.of(
                                        "name",
                                        Map.of("firstName", "Ada", "lastName", "Lovelace"),
                                        "emails",
                                        Map.of("primaryEmail", "ada@example.com")))))
                .thenReturn("created");
        String out = new TwentyTools(client).createPerson("Ada", "Lovelace", "ada@example.com");
        assertThat(out).isEqualTo("created");
    }

    @Test
    void createNoteLinksPerson() {
        when(client.create(eq("notes"), eq(Map.of("title", "Hi", "bodyV2", Map.of("markdown", "md")))))
                .thenReturn("{\"data\":{\"createNote\":{\"id\":\"n1\"}}}");
        when(client.createNoteTarget("n1", "p1", null, null)).thenReturn("linked");
        String out = new TwentyTools(client).createNote("Hi", "md", "p1", null, null);
        assertThat(out).contains("n1");
        verify(client).createNoteTarget("n1", "p1", null, null);
    }

    @Test
    void createNoteWithoutTargetDoesNotLink() {
        when(client.create(eq("notes"), eq(Map.of("title", "Hi", "bodyV2", Map.of("markdown", "md")))))
                .thenReturn("{\"data\":{\"createNote\":{\"id\":\"n1\"}}}");
        new TwentyTools(client).createNote("Hi", "md", null, null, null);
        verify(client).create(eq("notes"), eq(Map.of("title", "Hi", "bodyV2", Map.of("markdown", "md"))));
        verifyNoMoreInteractions(client);
    }
}
