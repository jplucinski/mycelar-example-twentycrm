package io.mycelar.example.twenty.chat;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public final class FakeChatModel implements ChatModel {

    @Override
    public ChatResponse call(Prompt prompt) {
        return new ChatResponse(java.util.List.of(new Generation(new AssistantMessage("ok"))));
    }
}
