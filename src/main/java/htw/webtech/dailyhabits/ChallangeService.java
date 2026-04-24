package htw.webtech.dailyhabits;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChallangeService {

    public List<Challange> getChallanges() {
        return List.of(
                new Challange("Push up"),
                new Challange ("Sit up")
        );
    }

}
