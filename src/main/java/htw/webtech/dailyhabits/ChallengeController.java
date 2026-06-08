package htw.webtech.dailyhabits;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping("/challenges")
    public List<Challenge> getChallenge(@RequestParam(required = false) String category) {
        return challengeService.getChallenges(category);
    }

    @GetMapping("/challenges/random")
    public Challenge getRandomChallenge() {
        return challengeService.getRandomChallenge();
    }

    @PostMapping("/challenges")
    @ResponseStatus(HttpStatus.CREATED)
    public Challenge createChallenge(@RequestBody Challenge challenge) {
        return challengeService.createChallenge(challenge);
    }

    @PatchMapping("/challenges/{id}/toggle")
    public Challenge toggleChallenge(@PathVariable Long id) {
        return challengeService.toggleChallenge(id);
    }
}
