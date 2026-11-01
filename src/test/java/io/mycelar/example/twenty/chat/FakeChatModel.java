package io.mycelar.example.twenty.chat;

import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public final class FakeChatModel implements ChatModel {

    private final List<List<String>> toolNames = new ArrayList<>();

    @Override
    public ChatResponse call(Prompt prompt) {
        toolNames.add(names(prompt));
        return new ChatResponse(List.of(new Generation(new AssistantMessage("ok"))));
    }

    public List<String> lastToolNames() {
        if (toolNames.isEmpty()) {
            return List.of();
        }
        return toolNames.getLast();
    }

    public void clear() {
        toolNames.clear();
    }

    private static List<String> names(Prompt prompt) {
        ChatOptions options = prompt.getOptions();
        if (!(options instanceof ToolCallingChatOptions toolOptions) || toolOptions.getToolCallbacks() == null) {
            return List.of();
        }
        return toolOptions.getToolCallbacks().stream()
                .map(callback -> callback.getToolDefinition().name())
                .toList();
    }
}
