package htw.webtech.dailyhabits;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Random;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ChallengeService {

    private static final List<Challenge> CHALLENGE_SUGGESTIONS = List.of(
            new Challenge("Mache 10 Kniebeugen", "Fitness", false),
            new Challenge("Gehe 15 Minuten spazieren", "Fitness", false),
            new Challenge("Lerne 5 neue Vokabeln", "Lernen", false),
            new Challenge("Lies 10 Seiten in einem Buch", "Lernen", false),
            new Challenge("Trinke ein grosses Glas Wasser", "Gesundheit", false),
            new Challenge("Mache 5 Minuten Atemuebungen", "Gesundheit", false),
            new Challenge("Raeume deinen Schreibtisch auf", "Alltag", false),
            new Challenge("Plane drei Aufgaben fuer morgen", "Alltag", false),
            new Challenge("Schreibe einer Person eine nette Nachricht", "Sozial", false),
            new Challenge("Rufe eine Person an, mit der du lange nicht gesprochen hast", "Sozial", false)
    );

    private final ChallengeRepository challengeRepository;
    private final Random random = new Random();

    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

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

    public Challenge getRandomChallengeSuggestion() {
        Challenge suggestion = CHALLENGE_SUGGESTIONS.get(random.nextInt(CHALLENGE_SUGGESTIONS.size()));
        return new Challenge(suggestion.getTitle(), suggestion.getCategory(), false);
    }

}
