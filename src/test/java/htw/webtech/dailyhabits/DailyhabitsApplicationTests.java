package htw.webtech.dailyhabits;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
		Challenge challenge = new Challenge("Wasser trinken", "Achtsamkeit", false);

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

}
