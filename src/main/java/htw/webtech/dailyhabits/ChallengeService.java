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

    public ChallengeService(
            ChallengeRepository challengeRepository,
            @Value("${openai.api.key}") String openAiApiKey,
            ObjectMapper objectMapper) {

        this.challengeRepository = challengeRepository;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + openAiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // --- NEUE HILFSMETHODE: Verwandelt eine Entity in ein DTO ---
    private ChallengeResponseDto toDto(Challenge challenge) {
        return new ChallengeResponseDto(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getCategory(),
                challenge.isDone()
        );
    }

    public List<ChallengeResponseDto> getChallenges(String category) {
        List<Challenge> challenges;
        if (category == null || category.isBlank()) {
            challenges = challengeRepository.findAll();
        } else {
            challenges = challengeRepository.findByCategoryIgnoreCase(category.trim());
        }
        // Alle aus der DB in DTOs umwandeln
        return challenges.stream().map(this::toDto).toList();
    }

    public ChallengeResponseDto createChallenge(ChallengeCreateDto request) {
        // Aus dem Request-DTO ein neues Entity für die DB machen
        Challenge challenge = new Challenge(request.title(), request.category(), request.done());
        Challenge saved = challengeRepository.save(challenge);
        return toDto(saved);
    }

    public ChallengeResponseDto toggleChallenge(Long id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Challenge not found"));

        challenge.setDone(!challenge.isDone());
        return toDto(challengeRepository.save(challenge));
    }

    public ChallengeResponseDto updateChallenge(Long id, ChallengeCreateDto request) {
        Challenge existingChallenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Challenge not found"));

        existingChallenge.setTitle(request.title());
        existingChallenge.setCategory(request.category());
        existingChallenge.setDone(request.done());

        return toDto(challengeRepository.save(existingChallenge));
    }

    public ChallengeResponseDto getRandomChallenge() {
        List<Challenge> challenges = challengeRepository.findAll();
        if (challenges.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "No challenges found");
        }
        return toDto(challenges.get(random.nextInt(challenges.size())));
    }

    public ChallengeResponseDto getRandomChallengeSuggestion() {
        String prompt = "Generiere eine zufällige, alltägliche Aufgabe (Challenge) auf Deutsch. " +
                "Kategorien dürfen NUR sein: Fitness, Lernen, Gesundheit, Alltag, Sozial. " +
                "Antworte strikt mit einem JSON-Objekt mit den Schlüsseln 'title' und 'category'.";

        OpenAiRequest requestBody = new OpenAiRequest(
                "gpt-3.5-turbo",
                List.of(new Message("user", prompt)),
                new ResponseFormat("json_object")
        );

        try {
            OpenAiResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAiResponse.class);

            String jsonContent = response.choices().get(0).message().content();
            GeneratedChallenge generated = objectMapper.readValue(jsonContent, GeneratedChallenge.class);

            // Rückgabe als DTO (ID ist null, da noch nicht gespeichert)
            return new ChallengeResponseDto(null, generated.title(), generated.category(), false);

        } catch (Exception e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Konnte keine Challenge generieren: " + e.getMessage());
        }
    }

    public void deleteChallenge(Long id) {
        if (!challengeRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Challenge not found");
        }
        challengeRepository.deleteById(id);
    }
}