package io.mycelar.example.twenty.chat;

import java.util.Objects;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = Objects.requireNonNull(chatClient, "chatClient");
    }

    @PostMapping("/api/chat")
    public ChatTurnResponse chat(@RequestBody ChatTurnRequest request) {
        Objects.requireNonNull(request, "request");
        if (request.message() == null || request.message().isBlank()) {
            throw new IllegalArgumentException("message is required");
        }
        String content =
                chatClient
                        .prompt()
                        .user(request.message())
                        .advisors(spec -> spec.param("mycelar.routing.confirmed", request.confirmed()))
                        .call()
                        .content();
        return new ChatTurnResponse(content);
    }
}
