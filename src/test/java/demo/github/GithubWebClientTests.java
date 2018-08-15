package demo.github;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

public class GithubWebClientTests {

	private MockWebServer server;

	private GithubWebClient githubWebClient;

	@Before
	public void setup() {
		this.server = new MockWebServer();
		this.githubWebClient = new GithubWebClient(WebClient.builder()
				.baseUrl(this.server.url("/").toString())
				.build());
	}

	@After
	public void shutdown() throws IOException {
		this.server.shutdown();
	}

	@Test
	public void getRecentCommits() throws IOException {
		expectJson("github/vaadin-spring-commits.json");
		Flux<Commit> recentCommits = this.githubWebClient.getRecentCommits(
				"vaadin", "spring");
		StepVerifier.create(recentCommits)
				.consumeNextWith(commit -> assertCommit(commit, "957a709",
						"Make Spring Boot tests more robust",
						"2016-10-14T11:56:09Z",
						"elmot", "Ilia Motornyi", "https://avatars.githubusercontent.com/u/5366945?v=3"))
				.expectNextCount(4).verifyComplete();
		expectRequestCount(1);
		expectRequest(request -> {
			assertThat(request.getPath()).isEqualTo("/repos/vaadin/spring/commits");
		});
	}

	@Test
	public void getRecentCommitsNoCommit() throws IOException {
		expectJson("github/no-commit.json");
		Flux<Commit> latestCommit = this.githubWebClient.getRecentCommits("vaadin", "spring2");
		StepVerifier.create(latestCommit).expectComplete().verify();
		expectRequestCount(1);
		expectRequest(request -> {
			assertThat(request.getPath()).isEqualTo("/repos/vaadin/spring2/commits");
		});
	}

	private void expectJson(String bodyPath) throws IOException {
		MockResponse response = new MockResponse();
		ClassPathResource resource = new ClassPathResource(bodyPath);
		response.setHeader("Content-Type", "application/json").setBody(
				StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8));
		this.server.enqueue(response);
	}

	private void expectRequest(Consumer<RecordedRequest> consumer) {
		try {
			consumer.accept(this.server.takeRequest());
		}
		catch (InterruptedException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private void expectRequestCount(int count) {
		assertThat(this.server.getRequestCount()).isEqualTo(count);
	}

	private void assertCommit(Commit commit, String sha, String message, String date,
			String committerId, String committerName, String committerAvatar) {
		assertThat(commit).isNotNull();
		assertThat(commit.getSha()).isEqualTo(sha);
		assertThat(commit.getMessage()).isEqualTo(message);
		assertThat(commit.getDate().toString()).isEqualTo(date);
		Commit.Committer committer = commit.getCommitter();
		assertThat(committer).isNotNull();
		assertThat(committer.getId()).isEqualTo(committerId);
		assertThat(committer.getName()).isEqualTo(committerName);
		assertThat(committer.getAvatarUrl()).isEqualTo(committerAvatar);
	}

}
