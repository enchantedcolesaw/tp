package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import seedu.address.commons.core.WorkspaceView;
import seedu.address.logic.help.HelpEntry;
import seedu.address.model.group.Group;
import seedu.address.model.student.Student;

/**
 * Hosts the TAssist screens while preserving the live student roster across navigation.
 */
public class WorkspacePanel extends UiPart<TabPane> {
    private final Map<WorkspaceView, Tab> views = new EnumMap<>(WorkspaceView.class);
    private final HelpPanel helpPanel;
    private final RecordListPanel recordListPanel = new RecordListPanel();
    private final NavigationBar navigationBar = new NavigationBar(this::showView);

    @FXML
    private TabPane tabs;

    /**
     * Creates the workspace with the active group's live students, the help reference, and the storage details.
     */
    public WorkspacePanel(ObservableValue<Optional<Group>> activeGroup, ObservableList<Group> groups,
            ObservableList<Student> students, ObservableList<Student> displayedStudents,
            ObservableBooleanValue hasStudentFilter,
            Path dataFilePath, List<HelpEntry> helpEntries, Consumer<Student> studentClickHandler,
            boolean isDataLoadFailed) {
        super("WorkspacePanel.fxml");
        requireNonNull(helpEntries);
        StudentListPanel studentListPanel = new StudentListPanel(activeGroup, groups, students, displayedStudents,
                hasStudentFilter, studentClickHandler, isDataLoadFailed);
        addView(WorkspaceView.STUDENTS, studentListPanel.getRoot());
        addView(WorkspaceView.RECORDS, recordListPanel.getRoot());
        helpPanel = new HelpPanel(helpEntries);
        addView(WorkspaceView.HELP, helpPanel.getRoot());
        addView(WorkspaceView.STORAGE, new StoragePanel(dataFilePath).getRoot());
        assert views.size() == WorkspaceView.values().length : "Every workspace screen must be registered";
        tabs.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) ->
                navigationBar.showCurrent(findView(newTab)));
        navigationBar.showCurrent(findView(tabs.getSelectionModel().getSelectedItem()));
    }

    /**
     * Returns the bar that opens each screen, for the window to place above the workspace.
     */
    public Node getNavigationBar() {
        return navigationBar.getRoot();
    }

    private WorkspaceView findView(Tab tab) {
        return views.entrySet().stream().filter(entry -> entry.getValue() == tab).map(Map.Entry::getKey)
                .findFirst().orElseThrow();
    }

    private void addView(WorkspaceView view, Node content) {
        Tab tab = new Tab(view.getTitle(), content);
        tab.setId(view.getKeyword());
        views.put(view, tab);
        tabs.getTabs().add(tab);
    }

    /**
     * Selects a screen.
     */
    public void showView(WorkspaceView view) {
        tabs.getSelectionModel().select(views.get(requireNonNull(view)));
    }

    /**
     * Replaces the snapshot shown by the records screen.
     */
    public void setRecordListing(String listing) {
        recordListPanel.setListing(listing);
    }

    /**
     * Replaces the records screen's listing with an updated version of it, keeping its scroll position.
     */
    public void refreshRecordListing(String listing) {
        recordListPanel.refreshListing(listing);
    }

    /**
     * Scrolls the records screen if selected, reporting whether it handled the request.
     */
    public boolean scrollRecords(boolean isDown) {
        if (findView(tabs.getSelectionModel().getSelectedItem()) != WorkspaceView.RECORDS) {
            return false;
        }
        recordListPanel.scrollPage(isDown);
        return true;
    }

    /**
     * Opens Help at the commands of {@code topic}, or at the top if {@code topic} is empty.
     */
    public void showHelp(String topic) {
        showView(WorkspaceView.HELP);
        // Lay out the newly selected Help screen first, so that it can find where the topic starts.
        getRoot().applyCss();
        getRoot().layout();
        helpPanel.scrollToTopic(topic);
    }
}
