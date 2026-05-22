package htw.webtech.dailyhabits;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChallengeService {

    public List<Challenge> getChallenges() {
        return List.of(
                new Challenge(1, "Push up", "Fitness", true),
                new Challenge(2, "Wasser trinken", "Achtsamkeit", true)
        );
    }

}
