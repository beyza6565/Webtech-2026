package htw.webtech.dailyhabits;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Random;

import static htw.webtech.dailyhabits.OpenAiDto.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    // Der API Key wird aus der application.properties injiziert
    public ChallengeService(
            ChallengeRepository challengeRepository,
            @Value("${openai.api.key}") String openAiApiKey,
            ObjectMapper objectMapper) {

        this.challengeRepository = challengeRepository;
        this.objectMapper = objectMapper;

        // RestClient mit dem API-Key konfigurieren
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + openAiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // --- ALTE METHODEN (Bleiben unverändert für die Datenbank) ---

    public List<Challenge> getChallenges(String category) {
        if (category == null || category.isBlank()) {
            return challengeRepository.findAll();
        }
        return challengeRepository.findByCategoryIgnoreCase(category.trim());
    }

    public List<Challenge> getChallenges() {
        return challengeRepository.findAll();
    }

    public Challenge createChallenge(Challenge challenge) {
        challenge.setId(null);
        return challengeRepository.save(challenge);
    }

    public Challenge toggleChallenge(Long id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Challenge not found"));

        challenge.setDone(!challenge.isDone());
        return challengeRepository.save(challenge);
    }

    public Challenge getRandomChallenge() {
        List<Challenge> challenges = challengeRepository.findAll();
        if (challenges.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "No challenges found");
        }

        return challenges.get(random.nextInt(challenges.size()));
    }

    // --- NEUE METHODE (Generiert Vorschläge mit OpenAI) ---

    public Challenge getRandomChallengeSuggestion() {
        // Prompt definieren
        String prompt = "Generiere eine zufällige, alltägliche Aufgabe (Challenge) auf Deutsch. " +
                "Kategorien dürfen NUR sein: Fitness, Lernen, Gesundheit, Alltag, Sozial. " +
                "Antworte strikt mit einem JSON-Objekt mit den Schlüsseln 'title' und 'category'.";

        OpenAiRequest requestBody = new OpenAiRequest(
                "gpt-3.5-turbo", // Oder gpt-4o-mini für günstigere/schnellere Antworten
                List.of(new Message("user", prompt)),
                new ResponseFormat("json_object") // Zwingt die KI, gültiges JSON zu liefern
        );

        try {
            // API Call ausführen
            OpenAiResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAiResponse.class);

            // Antwort auslesen und parsen
            String jsonContent = response.choices().get(0).message().content();
            GeneratedChallenge generated = objectMapper.readValue(jsonContent, GeneratedChallenge.class);

            // Zurückgeben (done ist standardmäßig false)
            return new Challenge(generated.title(), generated.category(), false);

        } catch (Exception e) {
            // Fallback, falls die API mal ausfällt oder das JSON falsch ist
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Konnte keine Challenge generieren: " + e.getMessage());
        }
    }
}