package htw.webtech.dailyhabits;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Random;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ChallengeService {

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

}
