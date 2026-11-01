package io.mycelar.example.twenty.chat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void chatReturnsModelText() throws Exception {
        mvc.perform(
                        post("/api/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"message\":\"search people named Ada\",\"confirmed\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("ok"));
    }
}
