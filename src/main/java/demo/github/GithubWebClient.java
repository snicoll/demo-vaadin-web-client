package demo.github;

import demo.VaadinRestDemoProperties;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GithubWebClient {

	private final WebClient webClient;

	@Autowired
	public GithubWebClient(WebClient.Builder webClientBuilder,
			VaadinRestDemoProperties properties) {
		this.webClient = setupWebClient(webClientBuilder,
				properties.getGithub().getToken());
	}

	protected GithubWebClient(WebClient webClient) {
		this.webClient = webClient;
	}

	public Flux<Commit> getRecentCommits(String organization, String project) {
		return this.webClient.get()
				.uri("/repos/{organization}/{project}/commits", organization, project)
				.retrieve().bodyToFlux(Commit.class);
	}

	private static WebClient setupWebClient(WebClient.Builder webClientBuilder,
			String githubToken) {
		if (StringUtils.hasText(githubToken)) {
			String[] content = githubToken.split(":");
			Assert.state(content.length == 2, "Invalid Github token");
			webClientBuilder = webClientBuilder.filter(
					ExchangeFilterFunctions.basicAuthentication(content[0], content[1]));
		}
		return webClientBuilder.baseUrl("https://api.github.com").build();
	}

}
