package io.mycelar.example.twenty.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerTest {

    private static final String NOTE = "{\"message\":\"create a note about the deal\",\"confirmed\":";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FakeChatModel model;

    @BeforeEach
    void setUp() {
        model.clear();
    }

    @Test
    void searchPeopleRegistersOnlyThePeopleReadTools() throws Exception {
        mvc.perform(
                        post("/api/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"message\":\"search people named Ada\",\"confirmed\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("ok"));
        assertThat(model.lastToolNames()).containsExactlyInAnyOrder("searchPeople", "getPerson");
    }

    @Test
    void createNoteIsAbsentUntilConfirmed() throws Exception {
        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content(NOTE + "false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("ok"));
        assertThat(model.lastToolNames()).isEmpty();

        model.clear();

        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content(NOTE + "true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("ok"));
        assertThat(model.lastToolNames()).containsExactly("createNote");
    }
}
