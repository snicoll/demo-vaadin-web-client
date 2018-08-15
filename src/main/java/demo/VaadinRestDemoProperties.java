package demo;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("demo.vaadin.rest")
public class VaadinRestDemoProperties {

	private final Github github = new Github();

	public Github getGithub() {
		return this.github;
	}

	public static class Github {

		/**
		 * Access token ("username:access_token") to query public github endpoints.
		 */
		private String token;

		/**
		 * Simulated latency.
		 */
		private Duration delay = Duration.ofSeconds(5);

		public String getToken() {
			return this.token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public Duration getDelay() {
			return this.delay;
		}

		public void setDelay(Duration delay) {
			this.delay = delay;
		}

	}
}
