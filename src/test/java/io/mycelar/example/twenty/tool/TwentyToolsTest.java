package io.mycelar.example.twenty.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.mycelar.core.capability.CapabilityDescriptor;
import io.mycelar.core.graph.GraphNode;
import io.mycelar.example.twenty.graph.TwentyGraph;
import io.mycelar.example.twenty.twenty.TwentyClient;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.tool.annotation.Tool;

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

    @Test
    void createNoteFailsWhenCreatedNoteHasNoId() {
        when(client.create(eq("notes"), eq(Map.of("title", "Hi", "bodyV2", Map.of("markdown", "md")))))
                .thenReturn("{\"data\":{}}");
        assertThatThrownBy(() -> new TwentyTools(client).createNote("Hi", "md", "p1", null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("note id missing from create response");
        verify(client).create(eq("notes"), eq(Map.of("title", "Hi", "bodyV2", Map.of("markdown", "md"))));
        verifyNoMoreInteractions(client);
    }

    @Test
    void createNoteFailsWhenCreateResponseIsNotJson() {
        when(client.create(eq("notes"), eq(Map.of("title", "Hi", "bodyV2", Map.of("markdown", "md")))))
                .thenReturn("not-json");
        assertThatThrownBy(() -> new TwentyTools(client).createNote("Hi", "md", "p1", null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("note id missing from create response");
        verify(client).create(eq("notes"), eq(Map.of("title", "Hi", "bodyV2", Map.of("markdown", "md"))));
        verifyNoMoreInteractions(client);
    }

    @Test
    void createNoteRejectsTwoOrThreeTargetsBeforeCreate() {
        TwentyTools tools = new TwentyTools(client);
        assertThatThrownBy(() -> tools.createNote("Hi", "md", "p1", "c1", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> tools.createNote("Hi", "md", "p1", "c1", "o1"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(client);
    }

    @Test
    void updatePersonOmitsNullFields() {
        when(client.patch(eq("people"), eq("p1"), eq(Map.of("emails", Map.of("primaryEmail", "ada@example.com")))))
                .thenReturn("patched");
        String out = new TwentyTools(client).updatePerson("p1", null, null, "ada@example.com");
        assertThat(out).isEqualTo("patched");
    }

    @Test
    void updatePersonRejectsEmptyBody() {
        assertThatThrownBy(() -> new TwentyTools(client).updatePerson("p1", null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(client);
    }

    @Test
    void updateCompanyRejectsNullName() {
        assertThatThrownBy(() -> new TwentyTools(client).updateCompany("c1", null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(client);
    }

    @Test
    void updateOpportunityOmitsNullFields() {
        when(client.patch(eq("opportunities"), eq("o1"), eq(Map.of("stage", "WON")))).thenReturn("patched");
        String out = new TwentyTools(client).updateOpportunity("o1", null, "WON");
        assertThat(out).isEqualTo("patched");
    }

    @Test
    void updateOpportunityRejectsEmptyBody() {
        assertThatThrownBy(() -> new TwentyTools(client).updateOpportunity("o1", null, null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(client);
    }

    @Test
    void createOpportunityUsesNewWhenStageIsNull() {
        when(client.create(eq("opportunities"), eq(Map.of("name", "Acme", "stage", "NEW")))).thenReturn("created");
        String out = new TwentyTools(client).createOpportunity("Acme", null);
        assertThat(out).isEqualTo("created");
    }

    @Test
    void toolNamesMatchCapabilityOperationIds() {
        Set<String> toolNames =
                Arrays.stream(TwentyTools.class.getDeclaredMethods())
                        .map(method -> method.getAnnotation(Tool.class))
                        .filter(Objects::nonNull)
                        .map(Tool::name)
                        .collect(Collectors.toSet());
        Set<String> operationIds =
                TwentyGraph.graph().nodes().stream()
                        .map(GraphNode::capability)
                        .flatMap(Optional::stream)
                        .map(CapabilityDescriptor::operationId)
                        .collect(Collectors.toSet());
        assertThat(toolNames).isEqualTo(operationIds);
    }
}
