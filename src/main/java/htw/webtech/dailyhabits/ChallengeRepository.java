package htw.webtech.dailyhabits;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    List<Challenge> findByCategoryIgnoreCase(String category);
}
