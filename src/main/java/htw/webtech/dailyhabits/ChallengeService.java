package htw.webtech.dailyhabits;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    public List<Challenge> getChallenges() {
        return challengeRepository.findAll();
    }

    public Challenge createChallenge(Challenge challenge) {
        challenge.setId(null);
        return challengeRepository.save(challenge);
    }

}
