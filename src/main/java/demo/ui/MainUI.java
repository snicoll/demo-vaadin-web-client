package demo.ui;

import java.time.Duration;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import demo.VaadinRestDemoProperties;
import demo.github.Commit;
import demo.github.GithubWebClient;
import reactor.core.Disposable;
import reactor.core.Disposables;

@PageTitle("Vaadin with RestTemplate demo")
@Route("")
@Push
public class MainUI extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	private Grid<Commit> commits = new Grid<>();

	private TextField organization = new TextField("Organization:", "vaadin", "");

	private TextField project = new TextField("Project:", "spring", "");

	private final Notification notification = new Notification(
			new Span("Waiting for github.com"));

	private final GithubWebClient githubClient;

	private final Duration delay;

	private final Disposable.Swap listCommitsDisposable = Disposables.swap();

	public MainUI(GithubWebClient c, VaadinRestDemoProperties properties) {
		this.githubClient = c;
		this.delay = properties.getGithub().getDelay();

		commits.addComponentColumn(this::createLink).setFlexGrow(3).setHeader("Message");
		commits.addColumn(commit -> commit.getCommitter().getName()).setFlexGrow(1).setHeader("Committer");
		commits.setWidth("100%");

		Button refresh = new Button("", this::refresh);
		refresh.setIcon(VaadinIcon.REFRESH.create());
		refresh.getElement().getThemeList().add("primary");

		ProgressBar progressBar = new ProgressBar();
		progressBar.setIndeterminate(true);
		notification.setPosition(Notification.Position.MIDDLE);
		notification.add(progressBar);

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
		this.notification.open();
		Disposable cancellable = githubClient
				.getRecentCommits(organization.getValue(), project.getValue())
				.log()
				.collectList().delayElement(this.delay)
				.doAfterTerminate(() -> uiAccess(this.notification::close))
				.subscribe((commits) -> uiAccess(() -> this.commits.setItems(commits)),
						(exception) -> System.out.println("Ooops " + exception.getMessage()));
		listCommitsDisposable.update(cancellable);
	}

	private void uiAccess(Command command) {
		getUI().ifPresent(ui -> ui.access(command));
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
