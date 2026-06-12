package htw.webtech.dailyhabits;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class DailyhabitsApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ChallengeRepository challengeRepository;

	@BeforeEach
	void setUp() {
		challengeRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void getChallengesReturnsAllChallenges() throws Exception {
		challengeRepository.save(new Challenge("Push up", "Fitness", false));
		challengeRepository.save(new Challenge("Lesen", "Bildung", true));

		mockMvc.perform(get("/api/v1/challenges"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].title", is("Push up")))
				.andExpect(jsonPath("$[1].title", is("Lesen")));
	}

	@Test
	void createChallengeSavesNewChallenge() throws Exception {
        ChallengeCreateDto challenge = new ChallengeCreateDto("Wasser trinken", "Achtsamkeit", false);
		mockMvc.perform(post("/api/v1/challenges")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(challenge)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.title", is("Wasser trinken")))
				.andExpect(jsonPath("$.category", is("Achtsamkeit")))
				.andExpect(jsonPath("$.done", is(false)));

		mockMvc.perform(get("/api/v1/challenges"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void toggleChallengeChangesDoneState() throws Exception {
		Challenge challenge = challengeRepository.save(new Challenge("Push up", "Fitness", false));

		mockMvc.perform(patch("/api/v1/challenges/{id}/toggle", challenge.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(challenge.getId().intValue())))
				.andExpect(jsonPath("$.done", is(true)));

		mockMvc.perform(patch("/api/v1/challenges/{id}/toggle", challenge.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.done", is(false)));
	}

	@Test
	void corsPreflightAllowsPatchToggle() throws Exception {
		mockMvc.perform(options("/api/v1/challenges/1/toggle")
						.header("Origin", "http://localhost:5173")
						.header("Access-Control-Request-Method", "PATCH"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
				.andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PATCH,OPTIONS"));
	}

	@Test
	void getChallengesFiltersByCategory() throws Exception {
		challengeRepository.save(new Challenge("Push up", "Fitness", false));
		challengeRepository.save(new Challenge("Joggen", "Fitness", false));
		challengeRepository.save(new Challenge("Lesen", "Bildung", false));

		mockMvc.perform(get("/api/v1/challenges").param("category", "Fitness"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].category", is("Fitness")))
				.andExpect(jsonPath("$[1].category", is("Fitness")));
	}

	@Test
	void getRandomChallengeReturnsOneChallenge() throws Exception {
		challengeRepository.save(new Challenge("Push up", "Fitness", false));
		challengeRepository.save(new Challenge("Lesen", "Bildung", false));

		mockMvc.perform(get("/api/v1/challenges/random"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.title", notNullValue()));
	}

	@Test
	void getRandomChallengeReturnsNotFoundWhenEmpty() throws Exception {
		mockMvc.perform(get("/api/v1/challenges/random"))
				.andExpect(status().isNotFound());
	}

	@Test
    @Disabled("Deaktiviert, um API-Kosten und Fehler beim BUild zu vermeiden")
	void getRandomChallengeSuggestionReturnsGeneratedChallenge() throws Exception {
		mockMvc.perform(get("/api/v1/challenges/suggestions/random"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title", notNullValue()))
				.andExpect(jsonPath("$.category", anyOf(
						equalTo("Fitness"),
						equalTo("Lernen"),
						equalTo("Gesundheit"),
						equalTo("Alltag"),
						equalTo("Sozial")
				)))
				.andExpect(jsonPath("$.done", is(false)));

		assertEquals(0, challengeRepository.count());
	}

    @Test
    void updateChallengeChangesChallengeData() throws Exception {
        Challenge challenge = challengeRepository.save(new Challenge("Push up", "Fitness", false));
        ChallengeCreateDto updatedData = new ChallengeCreateDto("100 Liegestuetze", "Sport", true);

        mockMvc.perform(put("/api/v1/challenges/{id}", challenge.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("100 Liegestuetze")))
                .andExpect(jsonPath("$.category", is("Sport")))
                .andExpect(jsonPath("$.done", is(true)));
    }

    @Test
    void deleteChallengeRemovesChallenge() throws Exception {
        Challenge challenge = challengeRepository.save(new Challenge("Lesen", "Bildung", false));

        mockMvc.perform(delete("/api/v1/challenges/{id}", challenge.getId()))
                .andExpect(status().isNoContent());

        assertEquals(0, challengeRepository.count());
    }

    @Test
    void deleteChallengeReturnsNotFoundForInvalidId() throws Exception {
        mockMvc.perform(delete("/api/v1/challenges/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateChallengeReturnsNotFoundForInvalidId() throws Exception {
        // Versuchen, eine Challenge zu bearbeiten, die nicht existiert (ID 999)
        Challenge updatedData = new Challenge("100 Liegestuetze", "Sport", true);

        mockMvc.perform(put("/api/v1/challenges/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isNotFound());
    }

    @Test
    void toggleChallengeReturnsNotFoundForInvalidId() throws Exception {
        // Versuchen, den Status einer Challenge zu ändern, die nicht existiert (ID 999)
        mockMvc.perform(patch("/api/v1/challenges/999/toggle"))
                .andExpect(status().isNotFound());
    }

}
