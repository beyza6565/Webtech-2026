package htw.webtech.dailyhabits;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ChallangeController {

    private final ChallangeService challangeService;

    public ChallangeController(ChallangeService challangeService) {
        this.challangeService = challangeService;
    }

    @GetMapping("/challanges")
    public List<Challange> getChallange() {
        return challangeService.getChallanges();
    }
}
