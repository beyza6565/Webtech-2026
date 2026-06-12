package htw.webtech.dailyhabits;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping("/challenges")
    public List<ChallengeResponseDto> getChallenge(@RequestParam(required = false) String category) {
        return challengeService.getChallenges(category);
    }

    @GetMapping("/challenges/random")
    public ChallengeResponseDto getRandomChallenge() {
        return challengeService.getRandomChallenge();
    }

    @GetMapping("/challenges/suggestions/random")
    public ChallengeResponseDto getRandomChallengeSuggestion() {
        return challengeService.getRandomChallengeSuggestion();
    }

    @PostMapping("/challenges")
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeResponseDto createChallenge(@RequestBody ChallengeCreateDto request) {
        return challengeService.createChallenge(request);
    }

    @PatchMapping("/challenges/{id}/toggle")
    public ChallengeResponseDto toggleChallenge(@PathVariable Long id) {
        return challengeService.toggleChallenge(id);
    }

    @PutMapping("/challenges/{id}")
    public ChallengeResponseDto updateChallenge(@PathVariable Long id, @RequestBody ChallengeCreateDto request) {
        return challengeService.updateChallenge(id, request);
    }

    @DeleteMapping("/challenges/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChallenge(@PathVariable Long id) {
        challengeService.deleteChallenge(id);
    }
}