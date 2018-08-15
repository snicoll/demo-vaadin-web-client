package demo.ui;

import java.time.Duration;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import demo.VaadinRestDemoProperties;
import demo.github.Commit;
import demo.github.GithubClient;

@PageTitle("Vaadin with RestTemplate demo")
@Route("")
public class MainUI extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	private Grid<Commit> commits = new Grid<>();

	private TextField organization = new TextField("Organization:", "vaadin", "");

	private TextField project = new TextField("Project:", "spring", "");

	private final GithubClient githubClient;

	private final Duration delay;

	public MainUI(GithubClient c, VaadinRestDemoProperties properties) {
		this.githubClient = c;
		this.delay = properties.getGithub().getDelay();

		commits.addComponentColumn(this::createLink).setFlexGrow(3).setHeader("Message");
		commits.addColumn(commit -> commit.getCommitter().getName()).setFlexGrow(1).setHeader("Committer");
		commits.setWidth("100%");

		Button refresh = new Button("", this::refresh);
		refresh.setIcon(VaadinIcon.REFRESH.create());
		refresh.getElement().getThemeList().add("primary");

		HorizontalLayout horizontalLayout = new HorizontalLayout(
				organization, project, refresh
		);
		horizontalLayout.setVerticalComponentAlignment(Alignment.END, refresh);

		add(
				new Text("Vaadin with RestTemplate demo"),
				horizontalLayout,
				commits
		);
		expand(commits);
		setSizeFull();
		listCommits();
	}

	private void listCommits() {
		try {
			Thread.sleep(this.delay.toMillis());
		}
		catch (InterruptedException ex) {
			// Oh well
		}
		commits.setItems(githubClient.getRecentCommits(
				organization.getValue(), project.getValue()));
	}

	private void refresh(ClickEvent clickEvent) {
		listCommits();
	}

	private Anchor createLink(Commit c) {
		String url = String.format("https://github.com/%s/%s/commit/%s",
				organization.getValue(), project.getValue(), c.getSha());
		return new Anchor(url, c.getMessage());
	}

}
