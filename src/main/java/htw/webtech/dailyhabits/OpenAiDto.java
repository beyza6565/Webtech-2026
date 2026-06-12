package htw.webtech.dailyhabits;

import java.util.List;

public class OpenAiDto {
    public record OpenAiRequest(String model, List<Message> messages, ResponseFormat response_format) {}
    public record Message(String role, String content) {}
    public record ResponseFormat(String type) {}

    public record OpenAiResponse(List<Choice> choices) {}
    public record Choice(Message message) {}

    // Das Format, in dem wir die Antwort von der KI erwarten
    public record GeneratedChallenge(String title, String category) {}
}