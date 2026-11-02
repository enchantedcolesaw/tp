package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BEN;
import static seedu.address.testutil.TypicalStudents.CARL;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.core.DataLoadStatus;
import seedu.address.commons.core.WorkspaceView;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.AssignmentCommandRegistration;
import seedu.address.logic.parser.AttendanceCommandRegistration;
import seedu.address.logic.parser.CommandRegistry;
import seedu.address.logic.parser.GroupCommandRegistration;
import seedu.address.logic.parser.ParticipationCommandRegistration;
import seedu.address.logic.parser.StudentCommandRegistration;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyTAssist;
import seedu.address.model.TAssist;
import seedu.address.model.UserPrefs;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupName;
import seedu.address.model.student.Student;
import seedu.address.storage.JsonTAssistStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.StudentBuilder;
import seedu.address.testutil.TypicalGroups;

/**
 * Exercises the real JavaFX views on a desktop when tassist.uiTests is enabled.
 */
@EnabledIfSystemProperty(named = "tassist.uiTests", matches = "true")
public class WorkspaceSmokeTest {
    /** Number of repeats that brings a word near the 60-character limit of group and student names. */
    private static final int LONG_NAME_REPEATS = 6;

    @TempDir
    public Path temporaryFolder;

    private Stage stage;
    private ModelManager model;
    private Parent root;
    private TextField input;
    private TabPane tabs;
    private Path data;
    private boolean isConfirmationAccepted = true;
    private String confirmationPrompt;
    private boolean shouldUseDefaultConfirmationHandler;

    @BeforeAll
    public static void startToolkit() throws Exception {
        FutureTask<Void> startup = new FutureTask<>(() -> {
            Platform.setImplicitExit(false);
            return null;
        });
        Platform.startup(startup);
        startup.get(20, TimeUnit.SECONDS);
    }

    @AfterAll
    public static void stopToolkit() {
        Platform.exit();
    }

    @Test
    public void recordListings_refreshAfterScrolling_restartsAtTopAndNamesTheGroup() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "group use g/T01");
                for (int i = 0; i < 17; i++) {
                    enter(input, "student add n/Student id/S" + i);
                }
                enter(input, "attendance list w/3");
                render(root, "records-before-scroll", 853, 440);
            });
            runOnFx(() -> {
                TextArea listing = (TextArea) root.lookup("#recordListing");
                assertEquals(21, listing.getText().lines().count());
                pressKey(KeyCode.PAGE_DOWN);
                assertTrue(listing.getScrollTop() > 0);
                enter(input, "attendance mark id/A0123456X w/3 s/present");
                assertTrue(listing.getText().contains("Alice Tan | A0123456X | Present"));
                enter(input, "undo");
                assertTrue(listing.getText().contains("Alice Tan | A0123456X | Not recorded"));
                TextArea feedback = (TextArea) root.lookup("#resultDisplay");
                enter(input, "attendance mark id/S0 id/S1 id/S2 id/S3 id/S4 w/3 s/absent");
                double listingScrollTop = listing.getScrollTop();
                pressKey(KeyCode.PAGE_DOWN);
                assertTrue(feedback.getScrollTop() > 0, "PageDown reaches the hidden lines of long feedback");
                assertEquals(listingScrollTop, listing.getScrollTop());
                enter(input, "attendance mark id/A0123456X w/3 s/present");
                enter(input, "attendance list w/3");
                assertEquals(0, listing.getCaretPosition());
                assertEquals(0, listing.getScrollTop());
                assertTrue(listing.getText().contains("Alice Tan | A0123456X | Present"));
                render(root, "records-guide", 1280, 720, 2);
                enter(input, "group use g/T02");
                assertTrue(listing.getText().startsWith("Attendance for T02"));
                String name = "A".repeat(60);
                enter(input, "student add n/" + name + " id/A1234567890123456789");
                enter(input, "attendance list w/3");
                assertTrue(listing.getText().startsWith("Attendance for T02"));
                assertTrue(listing.getText().contains(name + " | A1234567890123456789 | Not recorded"));
                render(root, "records-long-values", 853, 440);
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void recordListings_largeGroup_preservesOutputAcrossNavigationAndFailures() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                model.setActiveGroup(TypicalGroups.NAME_T01);
                for (int i = 0; i < 17; i++) {
                    model.addStudent(TypicalGroups.NAME_T01, new StudentBuilder().withName("Student")
                            .withStudentId("A" + (100 + i)).build());
                }
                enter(input, "assignment add a/PS1");
                String savedData = Files.readString(data);
                for (String command : List.of("attendance list w/3", "participation list w/3",
                        "assignment list a/PS1")) {
                    enter(input, command);
                    TextArea listing = (TextArea) root.lookup("#recordListing");
                    String snapshot = listing.getText();
                    assertEquals(21, snapshot.lines().count());
                    assertTrue(snapshot.contains("Student | A116 |"));
                    assertEquals("records", tabs.getSelectionModel().getSelectedItem().getId());
                    assertNavigationHighlights("records");
                    enter(input, "view storage");
                    enter(input, "view records");
                    assertEquals(snapshot, listing.getText());
                    enter(input, "attendance list w/14");
                    assertEquals(snapshot, listing.getText());
                    assertEquals("attendance list w/14", input.getText());
                    assertEquals(savedData, Files.readString(data));
                }
            });
            for (int[] size : new int[][] {{1280, 720}, {1920, 1080}, {1536, 864}, {853, 440}}) {
                runOnFx(() -> {
                    enter(input, "assignment list a/PS1");
                    render(root, "records-" + size[0] + "x" + size[1], size[0], size[1]);
                    TextArea listing = (TextArea) root.lookup("#recordListing");
                    TextArea feedback = (TextArea) root.lookup("#resultDisplay");
                    assertTrue(listing.getHeight() > feedback.getHeight() * 3);
                    assertTrue(input.getBoundsInParent().getHeight() > 0);
                });
            }
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void recordListing_assignmentRenamedOrMissing_followsRenameOrExplainsUnavailable() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "group use g/T01");
                enter(input, "assignment add a/PS1");
                enter(input, "assignment list a/ps1");
                TextArea listing = (TextArea) root.lookup("#recordListing");
                assertTrue(listing.getText().startsWith("PS1 records for T01:"));
                enter(input, "assignment rename a/PS1 n/Problem Set 1");
                assertTrue(listing.getText().startsWith("Problem Set 1 records for T01:"));
                enter(input, "undo");
                assertTrue(listing.getText().startsWith("PS1 records for T01:"));
                enter(input, "redo");
                assertTrue(listing.getText().startsWith("Problem Set 1 records for T01:"));
                enter(input, "group use g/T02");
                assertEquals(MainWindow.MESSAGE_LISTING_UNAVAILABLE + "Assignment Problem Set 1 does not exist in T02. "
                        + "See the group's assignments with: assignment list", listing.getText());
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void recordListing_pageKeysAndRefresh_stayInListingAndKeepPosition() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "group use g/T01");
                for (int i = 0; i < 30; i++) {
                    enter(input, "student add n/Student id/S" + i);
                }
                enter(input, "attendance list w/3");
                render(root, "records-page-keys", 1280, 720);
            });
            runOnFx(() -> {
                TextArea listing = (TextArea) root.lookup("#recordListing");
                for (int i = 0; i < 6; i++) {
                    pressKey(KeyCode.PAGE_DOWN);
                }
                double bottom = listing.getScrollTop();
                assertTrue(bottom > 0);
                pressKey(KeyCode.PAGE_UP);
                assertTrue(listing.getScrollTop() < bottom, "PageUp moves at once after PageDown passes the end");
                for (int i = 0; i < 6; i++) {
                    pressKey(KeyCode.PAGE_DOWN);
                }
                enter(input, "attendance mark id/S29 w/3 s/absent");
                assertTrue(listing.getText().contains("Student | S29 | Absent"));
                assertEquals(bottom, listing.getScrollTop(), 1);
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void recordsScreen_emptyState_opensWithoutChangingData() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                TAssist previousData = new TAssist(model.getTAssist());
                enter(input, "view records");
                assertEquals("records", tabs.getSelectionModel().getSelectedItem().getId());
                TextArea listing = (TextArea) root.lookup("#recordListing");
                assertTrue(listing.getText().contains("attendance list w/WEEK"));
                assertFalse(listing.isEditable());
                assertNavigationHighlights("records");
                assertEquals(previousData, model.getTAssist());
                enter(input, "view students");
                assertNavigationHighlights("students");
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void startup_missingOrUnreadableData_showsLoadOutcome() throws Exception {
        for (DataLoadStatus status : List.of(DataLoadStatus.MISSING, DataLoadStatus.FAILED)) {
            runOnFx(() -> initializeWorkspace(status));
            try {
                runOnFx(() -> {
                    assertEquals(status.getMessage(), ((TextArea) root.lookup("#resultDisplay")).getText());
                    assertTrue(((Label) root.lookup("#saveLocationStatus")).getText()
                            .startsWith(status.getMessage()));
                    assertEquals(status.isFailure(), isFooterShowingFailure());
                    assertEquals(status.isFailure(), isShowingDataNotRead());
                    enter(input, "help");
                    assertEquals(status.isFailure(), isFooterShowingFailure());
                    assertTrue(((Label) root.lookup("#saveLocationStatus")).getText()
                            .startsWith(status.isFailure() ? status.getMessage() : "Local data file"));
                });
            } finally {
                runOnFx(() -> stage.hide());
            }
        }
    }

    @Test
    public void startup_footerAfterCommands_preservesFailedLoadUntilDataCanBeSaved() throws Exception {
        for (DataLoadStatus status : List.of(DataLoadStatus.LOADED, DataLoadStatus.MISSING,
                DataLoadStatus.FAILED, DataLoadStatus.failed("Invalid JSON."))) {
            runOnFx(() -> initializeWorkspace(status));
            try {
                runOnFx(() -> {
                    Files.writeString(data, "{");
                    Label footer = (Label) root.lookup("#saveLocationStatus");
                    for (String command : List.of("help", "view students")) {
                        enter(input, command);
                        assertEquals(status.isFailure(), isFooterShowingFailure());
                        assertTrue(footer.getText().startsWith(
                                status.isFailure() ? status.getMessage() : "Local data file"));
                    }
                    TAssist beforeEdit = new TAssist(model.getTAssist());
                    enter(input, "group add g/New group");
                    if (status.isFailure()) {
                        assertEquals(beforeEdit, model.getTAssist());
                        assertEquals("{", Files.readString(data));
                        assertTrue(isFooterShowingFailure());
                        assertTrue(footer.getText().startsWith("Command failed"));
                        assertFalse(footer.getText().contains("Changes saved."));
                        enter(input, "help");
                        assertTrue(footer.getText().startsWith(status.getMessage()));
                        assertTrue(isFooterShowingFailure());
                    } else {
                        assertTrue(footer.getText().startsWith("Changes saved."));
                        assertFalse(isFooterShowingFailure());
                        assertFalse(Files.readString(data).equals("{"));
                    }
                });
            } finally {
                runOnFx(() -> stage.hide());
            }
        }
    }

    private boolean isShowingDataNotRead() {
        boolean isStripNoted = root.lookupAll(".muted").stream().anyMatch(node -> node instanceof Label label
                && label.getText().equals(GroupStrip.DATA_NOT_READ));
        boolean isStudentsNoted = root.lookupAll("#emptyTitle").stream().anyMatch(node -> node instanceof Label label
                && label.getText().equals(StudentListPanel.DATA_NOT_READ));
        return isStripNoted && isStudentsNoted;
    }

    @Test
    public void saveFailure_showsErrorWithoutSuccessAndRestoresWorkspace() throws Exception {
        for (IOException failure : List.of(new IOException("Disk full"), new AccessDeniedException("Data folder"))) {
            runOnFx(() -> initializeWorkspace(DataLoadStatus.LOADED, failure));
            try {
                runOnFx(() -> {
                    TAssist previousData = new TAssist(model.getTAssist());
                    enter(input, "group add g/T04");
                    String expected = failure instanceof AccessDeniedException
                            ? LogicManager.MESSAGE_SAVE_PERMISSION_FAILURE : LogicManager.MESSAGE_SAVE_FAILURE;
                    TextArea feedback = (TextArea) root.lookup("#resultDisplay");
                    assertEquals(expected, feedback.getText());
                    assertEquals("group add g/T04", input.getText());
                    assertTrue(isFooterShowingFailure());
                    assertFalse(((Label) root.lookup("#saveLocationStatus")).getText().contains("Changes saved."));
                    assertEquals(previousData, model.getTAssist());
                    assertGroupChips(List.of("T01", "T02", "T03"), "");
                    assertStudentsShown(StudentListPanel.NO_ACTIVE_GROUP);
                    enter(input, "help");
                    assertEquals(HelpCommand.MESSAGE_SHOWING_HELP, feedback.getText());
                    assertFalse(isFooterShowingFailure());
                });
                assertFalse(Files.exists(data));
            } finally {
                runOnFx(() -> stage.hide());
            }
        }
    }

    @Test
    public void workspace_navigationCommandsAndResizing_rendersAndPreservesBehavior() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            verifyScreens();
            verifyKeyboard();
            assertFalse(Files.exists(data), "Screen navigation must not write TAssist data");
            verifyCommands();
            verifyFailure();
            runOnFx(() -> render(root, "error", 853, 440));
            String longCommand = "unknown" + "x".repeat(600);
            runOnFx(() -> enter(input, longCommand));
            runOnFx(() -> {
                render(root, "error-usage", 1280, 680);
                TextArea feedback = (TextArea) root.lookup("#resultDisplay");
                assertTrue(feedback.getText().startsWith("Unknown command"));
                assertTrue(hasVisibleVerticalScrollBar(feedback), "Long usage feedback scrolls within its cap");
                assertTrue(feedback.getPrefRowCount() <= ResultDisplay.MAX_VISIBLE_LINES);
                String completeFeedback = feedback.getText();
                ScrollBar vertical = feedback.lookupAll(".scroll-bar").stream()
                        .filter(node -> node instanceof ScrollBar bar && bar.getOrientation() == Orientation.VERTICAL)
                        .map(node -> (ScrollBar) node).findFirst().orElseThrow();
                input.requestFocus();
                for (int i = 0; i < completeFeedback.lines().count(); i++) {
                    pressKey(KeyCode.PAGE_DOWN);
                }
                assertEquals(vertical.getMax(), vertical.getValue(), 0.001,
                        "PageDown reaches the final usage line");
                for (int i = 0; i < completeFeedback.lines().count(); i++) {
                    pressKey(KeyCode.PAGE_UP);
                }
                assertEquals(vertical.getMin(), vertical.getValue(), 0.001);
                assertEquals(completeFeedback, feedback.getText());
                assertEquals(longCommand, input.getText());
                assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(input, stage.getScene().getFocusOwner());
            });
            runOnFx(() -> enter(input, "view"));
            runOnFx(() -> {
                render(root, "view-missing-screen", 1280, 680);
                TextArea feedback = (TextArea) root.lookup("#resultDisplay");
                assertEquals(ViewCommand.MESSAGE_MISSING_SCREEN, feedback.getText());
                assertFalse(hasVisibleVerticalScrollBar(feedback), "The view usage error fits without scrolling");
            });
            runOnFx(() -> {
                enter(input, "view storage");
                Label status = (Label) root.lookup("#saveLocationStatus");
                assertTrue(status.getText().startsWith("Local data file"));
                assertFalse(isFooterShowingFailure(), "A later successful command clears the failure mark");
            });
            runOnFx(() -> {
                model.setActiveGroup(TypicalGroups.NAME_T01);
                enter(input, "view students");
            });
            runOnFx(() -> render(root, "students-large", 1920, 1040));
            runOnFx(() -> render(root, "students-medium", 1536, 824));
            runOnFx(() -> render(root, "students-laptop", 1280, 680));
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void studentsScreen_activeGroupChanges_showsActiveGroupAndItsStudents() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                assertStudentsShown(StudentListPanel.NO_ACTIVE_GROUP);
                assertEmptyTitle(StudentListPanel.NO_ACTIVE_GROUP);
                assertGroupChips(List.of("T01", "T02", "T03"), "");
            });
            runOnFx(() -> render(root, "no-active-group", 853, 440));
            runOnFx(() -> render(root, "no-active-group-minimum", 640, 420));

            runOnFx(() -> {
                model.setActiveGroup(TypicalGroups.NAME_T01);
                assertStudentsShown("T01");
                assertGroupChips(List.of("T01", "T02", "T03"), "T01");
                assertEquals("1", studentTable().getColumns().getFirst().getCellObservableValue(0).getValue());
            });

            runOnFx(() -> {
                model.setActiveGroup(TypicalGroups.NAME_T03);
                assertStudentsShown("T03");
                assertEmptyTitle(String.format(StudentListPanel.EMPTY_GROUP_TITLE_FORMAT, "T03"));
            });
            runOnFx(() -> render(root, "empty-group", 853, 440));

            runOnFx(() -> {
                model.addStudent(TypicalGroups.NAME_T03, new StudentBuilder().build());
                assertStudentsShown("T03");
                assertEquals(1, studentTable().getItems().size());
            });

            runOnFx(() -> {
                model.setTAssist(new TAssist());
                assertStudentsShown(StudentListPanel.NO_ACTIVE_GROUP);
                assertGroupChips(List.of(), "");
                assertTrue(root.lookupAll(".muted").stream().anyMatch(node -> node instanceof Label label
                        && label.getText().equals(GroupStrip.NO_GROUPS)));
            });
            runOnFx(() -> render(root, "no-groups", 853, 440));
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void groupCommands_updateChipsStudentsAndFeedback() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "group use g/t01");
                assertStudentsShown("T01");
                assertGroupChips(List.of("T01", "T02", "T03"), "T01");
                assertEquals(TypicalGroups.getT01().getStudentList(), studentTable().getItems());
                enter(input, "group use g/T02");
                assertStudentsShown("T02");
                assertEquals(TypicalGroups.getT02().getStudentList(), studentTable().getItems());
                enter(input, "GROUP AdD g/ Thursday   1 ");
                Label status = (Label) root.lookup("#saveLocationStatus");
                assertTrue(status.getText().startsWith("Changes saved."));
                assertStudentsShown("Thursday 1");
                assertTrue(studentTable().getItems().isEmpty());
                assertGroupChips(List.of("T01", "T02", "T03", "Thursday 1"), "Thursday 1");
                TextArea feedback = (TextArea) root.lookup("#resultDisplay");
                assertEquals("Created tutorial group: Thursday 1.\nActive group is now Thursday 1.",
                        feedback.getText());
                enter(input, "group list");
                assertTrue(status.getText().startsWith("Local data file"));
                assertEquals("1. T01\n2. T02\n3. T03\n4. Thursday 1\nActive group: Thursday 1", feedback.getText());
            });
            runOnFx(() -> render(root, "group-commands", 853, 440));
            runOnFx(() -> {
                enter(input, "group add g/t01");
                assertEquals("group add g/t01", input.getText());
                assertTrue(isFooterShowingFailure());
                assertStudentsShown("Thursday 1");
                enter(input, "group use g/T01");
                assertEquals(TypicalGroups.getT01().getStudentList(), studentTable().getItems());
                enter(input, "HeLp group");
                assertEquals("help", tabs.getSelectionModel().getSelectedItem().getId());
                assertEquals("Showing group commands.", ((TextArea) root.lookup("#resultDisplay")).getText());
            });
            runOnFx(() -> render(root, "group-help", 1280, 680));
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void participationCommands_showScoresAndKeepRejectedInput() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "group use g/T01");
                TextArea feedback = (TextArea) root.lookup("#resultDisplay");
                enter(input, "participation set id/a0123456x w/3 s/0");
                assertEquals("Recorded participation score 0 for Alice Tan in Week 3.", feedback.getText());
                enter(input, "participation list w/3");
                TextArea listing = (TextArea) root.lookup("#recordListing");
                assertEquals("Participation for T01, Week 3: 1 recorded, 2 not recorded\nAlice Tan | A0123456X | 0"
                        + "\nBen Lim | A0234567Y | Not recorded\nCarl Goh | A0345678Z | Not recorded",
                        listing.getText());
                enter(input, "participation set id/A0123456X w/3 s/6");
                assertEquals("Invalid participation score. Enter a whole number from 0 to 5.", feedback.getText());
                assertEquals("participation set id/A0123456X w/3 s/6", input.getText());
                assertTrue(input.getStyleClass().contains("error"));
                enter(input, "participation set id/A0123456X w/3 s/4");
                assertEquals("Updated Alice Tan's participation score for Week 3: 0 to 4.", feedback.getText());
                assertTrue(input.getText().isEmpty());
                assertEquals("records", tabs.getSelectionModel().getSelectedItem().getId());
            });
            runOnFx(() -> render(root, "participation-commands", 853, 440));
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void studentCommands_updateRosterAndKeepFailedInput() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "group use g/T03");
                enter(input, "student add n/Alice Tan id/A1");
                enter(input, "student add n/Ben Lim id/A2");
                assertEquals(2, studentTable().getItems().size());
                TextArea feedback = (TextArea) root.lookup("#resultDisplay");
                enter(input, "student list");
                assertEquals("Students in T03:\n1. Alice Tan | A1\n2. Ben Lim | A2", feedback.getText());
                enter(input, "student add n/Alice Tan id/a1");
                assertEquals("Student ID A1 already exists in T03.", feedback.getText());
                assertEquals("student add n/Alice Tan id/a1", input.getText());
                assertEquals(2, studentTable().getItems().size());

                isConfirmationAccepted = false;
                TAssist beforeCancelledDelete = new TAssist(model.getTAssist());
                String savedBeforeCancelledDelete = Files.readString(data);
                enter(input, "student delete id/a1");
                assertEquals(MainWindow.MESSAGE_CANCELLED, feedback.getText());
                assertEquals("student delete id/a1", input.getText());
                assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(beforeCancelledDelete, model.getTAssist());
                assertEquals(savedBeforeCancelledDelete, Files.readString(data));
                assertEquals(2, studentTable().getItems().size());
                assertEquals("Remove Alice Tan (A1) and all their attendance, participation, submission, "
                        + "and grade records from T03?", confirmationPrompt);

                isConfirmationAccepted = true;
                confirmationPrompt = null;
                enter(input, "student delete id/a1");
                assertEquals("Removed Alice Tan (A1) from T03.", feedback.getText());
                assertEquals(1, studentTable().getItems().size());
                assertEquals("1", studentTable().getColumns().getFirst().getCellObservableValue(0).getValue());
                assertEquals("Remove Alice Tan (A1) and all their attendance, participation, submission, "
                        + "and grade records from T03?", confirmationPrompt);
                confirmationPrompt = null;
                enter(input, "student delete id/A2 f/yes");
                assertEquals("Removed Ben Lim (A2) from T03.", feedback.getText());
                assertNull(confirmationPrompt);
                assertEmptyTitle(String.format(StudentListPanel.EMPTY_GROUP_TITLE_FORMAT, "T03"));
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void studentDelete_defaultConfirmationDialogHonorsContinueAndCancel() throws Exception {
        shouldUseDefaultConfirmationHandler = true;
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "group use g/T03");
                enter(input, "student add n/Alice Tan id/A1");
                enter(input, "student add n/Ben Lim id/A2");
                TextArea feedback = (TextArea) root.lookup("#resultDisplay");

                respondToConfirmation("Continue");
                enter(input, "student delete id/A1");
                assertEquals("Removed Alice Tan (A1) from T03.", feedback.getText());
                assertEquals(1, studentTable().getItems().size());

                TAssist beforeCancelledDelete = new TAssist(model.getTAssist());
                cancelConfirmationWithEnter("Remove Ben Lim (A2) and all their attendance, participation, "
                        + "submission, and grade records from T03?");
                enter(input, "student delete id/A2");
                assertEquals(MainWindow.MESSAGE_CANCELLED, feedback.getText());
                assertEquals("student delete id/A2", input.getText());
                assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(beforeCancelledDelete, model.getTAssist());
                assertEquals(1, studentTable().getItems().size());

                respondToConfirmation("Cancel");
                enter(input, "student delete id/A2");
                assertEquals(MainWindow.MESSAGE_CANCELLED, feedback.getText());
                assertEquals("student delete id/A2", input.getText());
                assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(beforeCancelledDelete, model.getTAssist());
                assertEquals(1, studentTable().getItems().size());
            });
        } finally {
            runOnFx(() -> stage.hide());
            shouldUseDefaultConfirmationHandler = false;
        }
    }

    @Test
    public void groupDelete_cancel_keepsCommandAndSavedData() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "group use g/T01");
                TAssist beforeDelete = new TAssist(model.getTAssist());
                String savedBeforeDelete = Files.readString(data);
                isConfirmationAccepted = false;
                enter(input, "group delete g/T01");
                assertEquals("group delete g/T01", input.getText());
                assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(MainWindow.MESSAGE_CANCELLED, ((TextArea) root.lookup("#resultDisplay")).getText());
                assertEquals(beforeDelete, model.getTAssist());
                assertEquals(savedBeforeDelete, Files.readString(data));
                input.setText("edited draft");
                pressKey(KeyCode.UP);
                assertEquals("group delete g/T01", input.getText());
                pressKey(KeyCode.UP);
                assertEquals("group use g/T01", input.getText());
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void studentsScreen_resizing_givesFreeWidthToNames() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                model.setActiveGroup(TypicalGroups.NAME_T01);
                model.addStudent(TypicalGroups.NAME_T01, new StudentBuilder()
                        .withName("Muhammad Hafiz bin Abdul Rahman bin Mohamed Ismail").withStudentId("Z9")
                        .build());
                for (int i = 0; i < 16; i++) {
                    model.addStudent(TypicalGroups.NAME_T01, new StudentBuilder()
                            .withName("Extra Student").withStudentId("EX" + i).build());
                }
            });
            for (int width : List.of(853, 1280, 1920, 853)) {
                runOnFx(() -> render(root, "student-column-width-" + width, width, 680));
                runOnFx(() -> {
                    TableView<?> table = studentTable();
                    assertTrue(table.getColumns().get(1).getWidth() > 320);
                    double idWidth = table.getColumns().get(2).getWidth();
                    assertTrue(idWidth >= 179 && idWidth <= 261, "IDs get 180 to 260 pixels, enough for 20 characters");
                    double columnWidth = table.getColumns().stream().mapToDouble(column -> column.getWidth()).sum();
                    assertTrue(table.getWidth() - columnWidth < 20, "Columns should use the table's free width");
                    assertTrue(columnWidth <= table.getWidth(), "Columns should fit without horizontal scrolling");
                });
            }
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void studentsScreen_longNames_keepsLayoutUsable() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                GroupName longGroupName = new GroupName("Longgroup".repeat(LONG_NAME_REPEATS));
                model.addGroup(new Group(longGroupName));
                model.addStudent(longGroupName, new StudentBuilder().withName("Longname".repeat(LONG_NAME_REPEATS))
                        .withStudentId("A".repeat(19) + "1").build());
                model.setActiveGroup(longGroupName);
                Label title = (Label) root.lookup("#groupTitle");
                assertEquals(longGroupName.toString(), title.getTooltip().getText());
            });
            runOnFx(() -> render(root, "long-values", 853, 440));
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void navigationBar_clickAndCommand_openScreenAndKeepCommandFocus() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                assertNavigationHighlights("students");
                Button helpButton = (Button) root.lookup("#nav-help");
                helpButton.fire();
                assertEquals("help", tabs.getSelectionModel().getSelectedItem().getId());
                assertNavigationHighlights("help");
                assertEquals(input, stage.getScene().getFocusOwner(), "Clicking a button keeps the command focus");

                enter(input, "view storage");
                assertNavigationHighlights("storage");
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void exitCommand_savesWindowSettingsAndClosesWindow() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                double width = stage.getWidth();
                double height = stage.getHeight();

                enter(input, "exit");

                assertFalse(stage.isShowing());
                assertEquals(width, model.getGuiSettings().getWindowWidth());
                assertEquals(height, model.getGuiSettings().getWindowHeight());
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void attendanceCommands_largeGroup_showsRecordsAndRetainsInvalidInput() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                model.setActiveGroup(TypicalGroups.NAME_T01);
                for (int i = 0; i < 17; i++) {
                    model.addStudent(TypicalGroups.NAME_T01, new StudentBuilder().withName("Student")
                            .withStudentId("A" + (100 + i)).build());
                }
                enter(input, "attendance mark id/A0123456X w/3 s/present");
                TextArea feedback = (TextArea) root.lookup("#resultDisplay");
                assertEquals("Recorded Alice Tan as present for Week 3.", feedback.getText());
                enter(input, "attendance list w/3");
                TextArea listing = (TextArea) root.lookup("#recordListing");
                render(root, "attendance-list-default", 1280, 720);
                assertNotNull(listing, "Weekly listings should appear in the main workspace");
                assertEquals(21, listing.getText().lines().count());
                assertTrue(listing.getText().contains("Alice Tan | A0123456X | Present"));
                assertTrue(listing.getText().contains("Student | A116 | Not recorded"));
                assertEquals("records", tabs.getSelectionModel().getSelectedItem().getId());
                assertEquals("Attendance for T01, Week 3: 1 present, 0 absent, 19 not recorded", feedback.getText());
                assertTrue(input.getText().isEmpty());
            });
            runOnFx(() -> render(root, "attendance-list", 853, 440));
            runOnFx(() -> {
                TextArea feedback = (TextArea) root.lookup("#resultDisplay");
                TextArea listing = (TextArea) root.lookup("#recordListing");
                assertTrue(hasVisibleVerticalScrollBar(listing), "A compact records screen can scroll");
                assertTrue(listing.getHeight() > feedback.getHeight() * 3);
                input.setText("draft");
                input.requestFocus();
                listing.setScrollTop(0);
                pressKey(KeyCode.PAGE_DOWN);
                assertTrue(listing.getScrollTop() > 0, "PageDown reaches hidden list output");
                assertEquals("draft", input.getText());
                assertEquals(input, stage.getScene().getFocusOwner());
                pressKey(KeyCode.PAGE_UP);
                assertEquals(0, listing.getScrollTop());
                pressKey(KeyCode.UP);
                assertEquals("attendance list w/3", input.getText());
                enter(input, "attendance mark id/A0123456X w/3 s/late");
                assertEquals("attendance mark id/A0123456X w/3 s/late", input.getText());
                assertTrue(input.getStyleClass().contains("error"));
                assertEquals("Invalid attendance status. Use present or absent.", feedback.getText());
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void commandBox_upAndDownKeys_recallEarlierCommandsForEditing() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "view help");
                enter(input, "view nowhere");
                input.setText("view stor");
                pressKey(KeyCode.UP);
                assertEquals("view nowhere", input.getText());
                assertEquals("view nowhere".length(), input.getCaretPosition());
                pressKey(KeyCode.UP);
                pressKey(KeyCode.UP);
                assertEquals("view help", input.getText(), "Up stops at the oldest command");
                pressKey(KeyCode.DOWN);
                assertEquals("view nowhere", input.getText(), "Failed commands are recalled too");
                pressKey(KeyCode.DOWN);
                pressKey(KeyCode.DOWN);
                assertEquals("view stor", input.getText(), "Down past the newest command restores the typed text");

                pressKey(KeyCode.UP);
                input.setText(input.getText().replace("nowhere", "storage"));
                input.fireEvent(new ActionEvent());
                assertEquals("storage", tabs.getSelectionModel().getSelectedItem().getId());
                assertTrue(input.getText().isEmpty());
                enter(input, "   ");
                assertFalse(input.getStyleClass().contains("error"), "Blank input is ignored");
                input.clear();
                pressKey(KeyCode.UP);
                assertEquals("view storage", input.getText(), "The edited command is the newest entry");
                input.setText("view st");
                for (KeyCode modifier : List.of(KeyCode.SHIFT, KeyCode.CONTROL, KeyCode.ALT, KeyCode.META)) {
                    pressKeyWithModifier(KeyCode.UP, modifier);
                    pressKeyWithModifier(KeyCode.DOWN, modifier);
                    assertEquals("view st", input.getText(), modifier + "+Up and Down are left to the text field");
                }
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void commandBox_recalledCommandFails_keepsItEditableAndHistoryUsable() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "view help");
                enter(input, "view storage");
                pressKey(KeyCode.UP);
                input.setText(input.getText().replace("storage", "storge"));
                input.fireEvent(new ActionEvent());
                assertEquals("view storge", input.getText());
                assertTrue(input.getStyleClass().contains("error"));

                pressKey(KeyCode.UP);
                assertEquals("view storage", input.getText(), "Up skips the failed command already in the box");
                pressKey(KeyCode.DOWN);
                assertEquals("view storge", input.getText());
                input.setText(input.getText().replace("storge", "storage"));
                input.fireEvent(new ActionEvent());
                assertEquals("storage", tabs.getSelectionModel().getSelectedItem().getId());
                assertTrue(input.getText().isEmpty());
                assertFalse(input.getStyleClass().contains("error"));
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    private void verifyScreens() throws Exception {
        for (WorkspaceView view : WorkspaceView.values()) {
            runOnFx(() -> {
                enter(input, "view " + view.getKeyword());
                assertEquals(view.getKeyword(), tabs.getSelectionModel().getSelectedItem().getId());
                assertTrue(input.getText().isEmpty());
            });
            runOnFx(() -> render(root, view.getKeyword(), 960, 640));
            runOnFx(() -> render(root, view.getKeyword() + "-compact", 853, 440));
        }
    }

    private void verifyKeyboard() throws Exception {
        runOnFx(() -> {
            input.setText("view students");
            pressKey(KeyCode.F1);
            assertEquals("help", tabs.getSelectionModel().getSelectedItem().getId());
            assertEquals("view students", input.getText());
            tabs.requestFocus();
            tabs.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false));
            assertEquals(input, stage.getScene().getFocusOwner());
        });
    }

    private void verifyCommands() throws Exception {
        runOnFx(() -> {
            TextArea feedbackArea = (TextArea) root.lookup("#resultDisplay");
            ScrollPane helpScrollPane = (ScrollPane) root.lookup("#helpScrollPane");
            for (String topic : List.of("group", "student")) {
                enter(input, "view students");
                assertEquals("students", tabs.getSelectionModel().getSelectedItem().getId());
                enter(input, "help " + topic + "s");
                assertEquals("help", tabs.getSelectionModel().getSelectedItem().getId());
                assertEquals("Showing " + topic + " commands.", feedbackArea.getText());
                assertTrue(helpScrollPane.getVvalue() > helpScrollPane.getVmin(), "Help scrolls to the topic");
            }
            enter(input, "help student");
            assertEquals("help", tabs.getSelectionModel().getSelectedItem().getId());
            assertEquals("Showing student commands.", feedbackArea.getText());
            assertTrue(helpScrollPane.getVvalue() > helpScrollPane.getVmin(), "Help scrolls to the topic");
            enter(input, "help");
            assertEquals(HelpCommand.MESSAGE_SHOWING_HELP, feedbackArea.getText());
            assertEquals(helpScrollPane.getVmin(), helpScrollPane.getVvalue());
            enter(input, "ViEw help");
            VBox commands = (VBox) root.lookup("#commands");
            assertTrue(commands.getChildren().stream().anyMatch(node -> node instanceof Label label
                    && label.getText().equals("Student commands")));
            enter(input, "student list");
            assertEquals(Messages.MESSAGE_NO_ACTIVE_GROUP, feedbackArea.getText());
            assertEquals("help", tabs.getSelectionModel().getSelectedItem().getId(),
                    "A failed listing keeps the current screen");
        });
    }

    @Test
    public void commandBox_fillTemplate_preparesNewDraftWithoutExecuting() throws Exception {
        runOnFx(() -> {
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("Recorded");
            }, isDown -> { });
            Stage templateStage = new Stage();
            templateStage.setScene(new Scene(box.getRoot()));
            templateStage.show();
            try {
                TextField field = (TextField) box.getRoot().lookup("#commandTextField");
                String template = "attendance mark id/A1 w/";
                field.setText("unfinished previous draft");
                field.getStyleClass().add(CommandBox.ERROR_STYLE_CLASS);
                box.fillTemplate(template);
                assertEquals(template, field.getText());
                assertEquals(template.length(), field.getCaretPosition());
                assertEquals(field, templateStage.getScene().getFocusOwner());
                assertFalse(field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertTrue(executed.isEmpty());

                field.getStyleClass().add(CommandBox.ERROR_STYLE_CLASS);
                box.fillTemplate(template);
                assertFalse(field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertThrows(NullPointerException.class, () -> box.fillTemplate(null));
                assertEquals(template, field.getText());
                String completed = template + "1 s/present";
                enter(field, completed);
                assertEquals(List.of(completed), executed);
                assertTrue(field.getText().isEmpty());

                field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.UP,
                        false, false, false, false));
                box.fillTemplate(template);
                field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.UP,
                        false, false, false, false));
                assertEquals(completed, field.getText());
                field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN,
                        false, false, false, false));
                assertEquals(template, field.getText());
                assertEquals(List.of(completed), executed);
            } finally {
                templateStage.close();
            }
        });
    }

    @Test
    public void commandBox_pageKeys_scrollFeedbackWithoutChangingInput() throws Exception {
        runOnFx(() -> {
            List<Boolean> scrolls = new ArrayList<>();
            CommandBox box = new CommandBox(command -> new CommandResult("Recorded"), scrolls::add);
            TextField field = (TextField) box.getRoot().lookup("#commandTextField");
            field.setText("draft");
            field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.PAGE_DOWN,
                    false, false, false, false));
            field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.PAGE_UP,
                    false, false, false, false));
            for (KeyCode modifier : List.of(KeyCode.SHIFT, KeyCode.CONTROL, KeyCode.ALT, KeyCode.META)) {
                for (KeyCode key : List.of(KeyCode.PAGE_UP, KeyCode.PAGE_DOWN)) {
                    field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", key, modifier == KeyCode.SHIFT,
                            modifier == KeyCode.CONTROL, modifier == KeyCode.ALT, modifier == KeyCode.META));
                }
            }
            assertEquals(List.of(true, false), scrolls);
            assertEquals("draft", field.getText());
        });
    }

    @Test
    public void resultDisplay_scrollPage_movesByVisiblePageAndStopsAtStart() throws Exception {
        runOnFx(() -> {
            ResultDisplay display = new ResultDisplay();
            Stage displayStage = new Stage();
            displayStage.setScene(new Scene(display.getRoot(), 400, 120));
            display.setFeedbackToUser(String.join("\n", Collections.nCopies(30, "line")));
            displayStage.show();
            try {
                TextArea area = (TextArea) display.getRoot().lookup("#resultDisplay");
                display.getRoot().applyCss();
                display.getRoot().layout();
                display.scrollPage(false);
                assertEquals(0, area.getScrollTop());
                display.scrollPage(true);
                assertTrue(area.getScrollTop() > 0);
                display.scrollPage(false);
                display.scrollPage(false);
                assertEquals(0, area.getScrollTop());
            } finally {
                displayStage.close();
            }
        });
    }

    @Test
    public void commandBox_enter_clearsInputUnlessCommandCancelled() throws Exception {
        runOnFx(() -> {
            CommandBox box = new CommandBox(command -> command.startsWith("cancel")
                    ? CommandResult.forCancelled(MainWindow.MESSAGE_CANCELLED)
                    : new CommandResult("Recorded"), isDown -> { });
            TextField field = (TextField) box.getRoot().lookup("#commandTextField");
            field.setText("cancel this");
            field.getStyleClass().add(CommandBox.ERROR_STYLE_CLASS);
            field.fireEvent(new ActionEvent());
            assertEquals("cancel this", field.getText());
            assertFalse(field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

            field.setText("run this");
            field.fireEvent(new ActionEvent());
            assertTrue(field.getText().isEmpty());
        });
    }

    @Test
    public void fillTemplate_afterFailedEnter_keepsInputEditable() throws Exception {
        runOnFx(() -> {
            CommandBox box = new CommandBox(command -> {
                throw new ParseException("Complete the attendance template first.");
            }, isDown -> { });
            TextField field = (TextField) box.getRoot().lookup("#commandTextField");
            String template = "attendance mark id/A1 w/";
            box.fillTemplate(template);
            field.fireEvent(new ActionEvent());
            assertEquals(template, field.getText());
            assertTrue(field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            box.fillTemplate(template);
            assertFalse(field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            assertEquals(template.length(), field.getCaretPosition());
        });
    }

    @Test
    public void studentClick_preparesTemplateAndRequiresEnter_preservesDataUntilCompleted() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "group use g/T01");
                String saved = Files.readString(data);
                TAssist previousData = new TAssist(model.getTAssist());
                TextArea feedback = (TextArea) root.lookup("#resultDisplay");
                String previousFeedback = feedback.getText();
                input.setText("unfinished draft");
                clickRow(tableRow(studentTable(), 0), MouseButton.PRIMARY);
                String template = "attendance mark id/A0123456X w/";
                assertEquals(template, input.getText());
                assertEquals(template.length(), input.getCaretPosition());
                assertEquals(input, stage.getScene().getFocusOwner());
                assertEquals(previousFeedback, feedback.getText());
                assertEquals(previousData, model.getTAssist());
                assertEquals(saved, Files.readString(data));

                input.fireEvent(new ActionEvent());
                assertEquals(template, input.getText());
                assertTrue(input.getStyleClass().contains("error"));
                assertEquals(previousData, model.getTAssist());
                assertEquals(saved, Files.readString(data));
                input.appendText("1 s/present");
                input.fireEvent(new ActionEvent());
                assertTrue(input.getText().isEmpty());
                assertEquals("PRESENT", model.getActiveGroupStudentList().getFirst()
                        .getAttendanceRecord(1).orElseThrow());
                assertFalse(saved.equals(Files.readString(data)));
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    private TableRow<?> tableRow(TableView<?> table, int index) {
        table.applyCss();
        table.layout();
        return table.lookupAll(".table-row-cell").stream()
                .filter(node -> node instanceof TableRow<?>)
                .map(node -> (TableRow<?>) node)
                .filter(row -> row.getIndex() == index)
                .findFirst().orElseThrow();
    }

    private void clickRow(TableRow<?> row, MouseButton button) {
        row.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 1, 1, 1, 1, button, 1,
                false, false, false, false, false, false, false, false, false, true, null));
    }

    @Test
    public void studentClick_repeatedAndIgnoredActions_preservesDraftAndHistory() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "group use g/T01");
                String saved = Files.readString(data);
                TAssist previousData = new TAssist(model.getTAssist());
                input.setText("unfinished draft");
                studentTable().getSelectionModel().select(0);
                studentTable().requestFocus();
                studentTable().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN,
                        false, false, false, false));
                assertEquals(1, studentTable().getSelectionModel().getSelectedIndex());
                assertEquals("unfinished draft", input.getText());
                clickRow(tableRow(studentTable(), 0), MouseButton.SECONDARY);
                clickRow(tableRow(studentTable(), 0), MouseButton.MIDDLE);
                TableRow<?> emptyRow = tableRow(studentTable(), studentTable().getItems().size());
                assertTrue(emptyRow.isEmpty());
                clickRow(emptyRow, MouseButton.PRIMARY);
                assertEquals("unfinished draft", input.getText());
                clickRow(tableRow(studentTable(), 0), MouseButton.PRIMARY);
                input.appendText("3 s/present");
                clickRow(tableRow(studentTable(), 0), MouseButton.PRIMARY);
                String template = "attendance mark id/A0123456X w/";
                assertEquals(template, input.getText());
                assertEquals(template.length(), input.getCaretPosition());
                assertEquals(input, stage.getScene().getFocusOwner());
                pressKey(KeyCode.UP);
                assertEquals(template + "3 s/present", input.getText());
                pressKey(KeyCode.UP);
                assertEquals("unfinished draft", input.getText());
                pressKey(KeyCode.UP);
                assertEquals("group use g/T01", input.getText());
                pressKey(KeyCode.DOWN);
                assertEquals("unfinished draft", input.getText());
                pressKey(KeyCode.DOWN);
                assertEquals(template + "3 s/present", input.getText());
                pressKey(KeyCode.DOWN);
                assertEquals(template, input.getText());
                assertEquals(previousData, model.getTAssist());
                assertEquals(saved, Files.readString(data));
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void studentClick_groupAndRosterChanges_usesCurrentRowStudent() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "group use g/T01");
                clickRow(tableRow(studentTable(), 0), MouseButton.PRIMARY);
                enter(input, "group use g/T02");
                assertTrue(input.getText().isEmpty(), "Changing groups does not fill a template automatically");
                clickRow(tableRow(studentTable(), 0), MouseButton.PRIMARY);
                assertEquals("attendance mark id/A0456789W w/", input.getText());
                model.removeStudent(TypicalGroups.NAME_T02,
                        model.getActiveGroupStudentList().getFirst().getStudentId());
                clickRow(tableRow(studentTable(), 0), MouseButton.PRIMARY);
                assertEquals("attendance mark id/A0567890U w/", input.getText());
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void studentClick_transformedList_usesDisplayedStudentInsteadOfRosterIndex() throws Exception {
        runOnFx(() -> {
            var roster = FXCollections.observableArrayList(ALICE, BEN, CARL);
            FilteredList<Student> filtered = new FilteredList<>(roster, student -> !student.equals(ALICE));
            SortedList<Student> sorted = new SortedList<>(filtered,
                    Comparator.comparing((Student student) -> student.getStudentId().toString()).reversed());
            List<Student> clicked = new ArrayList<>();
            StudentListPanel panel = new StudentListPanel(
                    new SimpleObjectProperty<>(Optional.of(TypicalGroups.getT01())),
                    FXCollections.observableArrayList(TypicalGroups.getT01()), roster, sorted,
                    new SimpleBooleanProperty(true), clicked::add, false);
            Stage transformedStage = new Stage();
            transformedStage.setScene(new Scene(panel.getRoot(), 800, 500));
            transformedStage.show();
            try {
                TableView<?> table = (TableView<?>) panel.getRoot().lookup("#studentTable");
                clickRow(tableRow(table, 0), MouseButton.PRIMARY);
                assertEquals(List.of(CARL), clicked);
                filtered.setPredicate(student -> student.equals(ALICE));
                clickRow(tableRow(table, 0), MouseButton.PRIMARY);
                assertEquals(List.of(CARL, ALICE), clicked);
            } finally {
                transformedStage.close();
            }
        });
    }

    @Test
    public void studentAdd_longRoster_scrollsToNewRowWithoutSelectingIt() throws Exception {
        List<Integer> scrolledRows = new ArrayList<>();
        runOnFx(() -> {
            initializeWorkspace();
            stage.setWidth(854);
            stage.setHeight(480);
            enter(input, "view students");
            enter(input, "group use g/T03");
            for (int i = 0; i < 15; i++) {
                enter(input, "student add n/Student " + (char) ('A' + i) + " id/S" + i);
            }
        });
        try {
            runOnFx(() -> {
                studentTable().setOnScrollTo(event -> scrolledRows.add(event.getScrollTarget()));
                enter(input, "student add n/New Person id/A9999");
            });
            // VirtualFlow realizes the scrolled row on a subsequent JavaFX pulse.
            Thread.sleep(300);
            runOnFx(() -> {
                root.applyCss();
                root.layout();
                assertEquals(List.of(16), scrolledRows);
                assertEquals(-1, studentTable().getSelectionModel().getSelectedIndex());
                assertFalse(tableRow(studentTable(), 15).isEmpty());
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void studentEmptyHint_groupChanges_updatesBoundHint() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "view students");
                Label hint = (Label) root.lookup("#emptyHint");
                assertEquals(StudentListPanel.SELECT_GROUP_HINT, hint.getText());
                model.setTAssist(new TAssist());
                assertEquals(StudentListPanel.NO_ACTIVE_GROUP_HINT, hint.getText());
                model.addGroup(TypicalGroups.getT03());
                assertEquals(StudentListPanel.SELECT_GROUP_HINT, hint.getText());
                enter(input, "group add g/T01");
                enter(input, "group add g/T02");
                enter(input, "group delete g/T02 f/yes");
                assertEquals(StudentListPanel.SELECT_GROUP_HINT, hint.getText());
                model.removeGroup(TypicalGroups.NAME_T01);
                model.removeGroup(TypicalGroups.NAME_T03);
                assertEquals(StudentListPanel.NO_ACTIVE_GROUP_HINT, hint.getText());
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void studentSearchAndSort_updatesTableCountsEmptyStateAndNavigation() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "group use g/T03");
                enter(input, "student add n/Zoe id/Z3");
                enter(input, "student add n/Alice Tan id/A2");
                enter(input, "student add n/alice Tan id/A1");
                enter(input, "view help");
                enter(input, "student find n/ALI id/a");
                assertEquals("students", tabs.getSelectionModel().getSelectedItem().getId());
                assertEquals("2 of 3 students", ((Label) root.lookup("#studentCount")).getText());
                enter(input, "student sort by/name");
                assertEquals("A1", studentTable().getColumns().get(2).getCellObservableValue(0).getValue());
                assertEquals("1", studentTable().getColumns().getFirst().getCellObservableValue(0).getValue());
                clickRow(tableRow(studentTable(), 0), MouseButton.PRIMARY);
                assertEquals("attendance mark id/A1 w/", input.getText());
                enter(input, "student sort by/grade");
                assertEquals("student sort by/grade", input.getText());
                assertEquals("A1", studentTable().getColumns().get(2).getCellObservableValue(0).getValue());
                enter(input, "student find n/nobody");
                assertEmptyTitle("No matching students");
                assertEquals("Use student list to show all students.", ((Label) root.lookup("#emptyHint")).getText());
                enter(input, "student list");
                assertEquals("3 students", ((Label) root.lookup("#studentCount")).getText());
                assertEquals("Z3", studentTable().getColumns().get(2).getCellObservableValue(0).getValue());
                assertEquals(3, studentTable().getItems().size());
                enter(input, "student find n/alice");
                enter(input, "group use g/T01");
                assertStudentsShown("T01");
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    @Test
    public void studentSearch_rosterBecomesEmpty_updatesHintsAndCounts() throws Exception {
        runOnFx(this::initializeWorkspace);
        try {
            runOnFx(() -> {
                enter(input, "group use g/T03");
                enter(input, "student add n/Alice id/A1");
                enter(input, "student find n/missing");
                assertEmptyTitle("No matching students");
                assertEquals("0 of 1 student", ((Label) root.lookup("#studentCount")).getText());
                enter(input, "student delete id/A1 f/yes");
                assertEmptyTitle("No matching students");
                assertEquals("Use student list to show all students.", ((Label) root.lookup("#emptyHint")).getText());
                assertEquals("0 of 0 students", ((Label) root.lookup("#studentCount")).getText());
                enter(input, "student add n/Zoe id/Z2");
                assertEmptyTitle("No matching students");
                enter(input, "student add n/Missing Person id/M3");
                assertEquals(1, studentTable().getItems().size());
                assertTrue(studentTable().isVisible());
                assertEquals("1 of 2 students", ((Label) root.lookup("#studentCount")).getText());
                enter(input, "student delete id/Z2 f/yes");
                assertEquals("1 of 1 student", ((Label) root.lookup("#studentCount")).getText());
                enter(input, "group use g/T02");
                assertStudentsShown("T02");
            });
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    private void verifyFailure() throws Exception {
        runOnFx(() -> {
            model.setActiveGroup(TypicalGroups.NAME_T01);
            Student firstStudent = model.getActiveGroupStudentList().getFirst();
            model.removeStudent(TypicalGroups.NAME_T01, firstStudent.getStudentId());
            assertEquals("1", studentTable().getColumns().getFirst().getCellObservableValue(0).getValue());
            enter(input, "view nowhere");
            assertEquals("view nowhere", input.getText());
            assertTrue(input.getStyleClass().contains("error"));
            TextArea feedback = (TextArea) root.lookup("#resultDisplay");
            assertEquals(String.format(ViewCommand.MESSAGE_UNKNOWN_SCREEN, "nowhere"), feedback.getText());
            assertTrue(isFooterShowingFailure(), "The footer marks a failed command");
        });
    }

    /**
     * Asserts that the Students screen is titled for {@code expectedGroup} and lists the active group's students.
     */
    private void assertStudentsShown(String expectedGroup) {
        String expectedTitle = expectedGroup.equals(StudentListPanel.NO_ACTIVE_GROUP)
                ? StudentListPanel.STUDENTS_TITLE : expectedGroup;
        assertEquals(expectedTitle, ((Label) root.lookup("#groupTitle")).getText());
        assertEquals(model.getActiveGroupStudentList(), studentTable().getItems());
    }

    /**
     * Asserts that the empty Students screen explains itself with {@code expectedTitle}.
     */
    private void assertEmptyTitle(String expectedTitle) {
        assertTrue(studentTable().getItems().isEmpty());
        assertEquals(expectedTitle, ((Label) root.lookup("#emptyTitle")).getText());
    }

    @Test
    public void groupStrip_manyGroups_revealsActiveChipAtEitherEnd() throws Exception {
        runOnFx(() -> {
            initializeWorkspace();
            stage.setWidth(854);
            stage.setHeight(480);
            for (int i = 0; i < 12; i++) {
                model.addGroup(new Group(new GroupName("Long tutorial group number " + i)));
            }
            model.setActiveGroup(new GroupName("Long tutorial group number 11"));
        });
        try {
            runOnFx(this::assertActiveChipVisible);
            runOnFx(() -> model.setActiveGroup(TypicalGroups.NAME_T01));
            runOnFx(this::assertActiveChipVisible);
            runOnFx(() -> {
                model.setActiveGroup(new GroupName("Long tutorial group number 11"));
                model.setActiveGroup(TypicalGroups.NAME_T02);
            });
            runOnFx(this::assertActiveChipVisible);
        } finally {
            runOnFx(() -> stage.hide());
        }
    }

    private void assertActiveChipVisible() {
        root.applyCss();
        root.layout();
        ScrollPane strip = (ScrollPane) root.lookup(".group-chips-scroll");
        Label chip = root.lookupAll(".group-chip").stream().map(node -> (Label) node)
                .filter(label -> label.getPseudoClassStates().contains(PseudoClass.getPseudoClass("active")))
                .findFirst().orElseThrow();
        var chipBounds = chip.localToScene(chip.getBoundsInLocal());
        var viewport = strip.lookup(".viewport");
        var viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());
        assertTrue(chipBounds.getMinX() >= viewportBounds.getMinX() - 1, "Active chip left edge is visible");
        assertTrue(chipBounds.getMaxX() <= viewportBounds.getMaxX() + 1, "Active chip right edge is visible");
    }

    /**
     * Asserts that the group strip shows a chip for each of {@code expectedNames}, in order, and that only the
     * chip named {@code expectedActiveName} is highlighted. An empty {@code expectedActiveName} means none is.
     */
    private void assertGroupChips(List<String> expectedNames, String expectedActiveName) {
        List<Label> chips = root.lookupAll(".group-chip").stream().map(node -> (Label) node).toList();
        assertEquals(expectedNames, chips.stream().map(Label::getText).toList());
        for (Label chip : chips) {
            boolean isActive = chip.getPseudoClassStates().contains(PseudoClass.getPseudoClass("active"));
            assertEquals(chip.getText().equals(expectedActiveName), isActive, chip.getText());
        }
    }

    private boolean isFooterShowingFailure() {
        return root.lookup(".status-bar").getPseudoClassStates().contains(PseudoClass.getPseudoClass("failure"));
    }

    /**
     * Asserts that only the navigation button of the screen with {@code keyword} is highlighted.
     */
    private void assertNavigationHighlights(String keyword) {
        PseudoClass current = PseudoClass.getPseudoClass("current");
        for (WorkspaceView view : WorkspaceView.values()) {
            Button button = (Button) root.lookup("#nav-" + view.getKeyword());
            assertEquals(view.getKeyword().equals(keyword), button.getPseudoClassStates().contains(current),
                    view.getKeyword());
        }
    }

    private static boolean hasVisibleVerticalScrollBar(Parent parent) {
        return parent.lookupAll(".scroll-bar").stream().anyMatch(node -> node instanceof ScrollBar bar
                && bar.getOrientation() == Orientation.VERTICAL && bar.isVisible());
    }

    private TableView<?> studentTable() {
        return (TableView<?>) root.lookup("#studentTable");
    }

    private void initializeWorkspace() {
        initializeWorkspace(DataLoadStatus.LOADED);
    }

    private void initializeWorkspace(DataLoadStatus dataLoadStatus) {
        initializeWorkspace(dataLoadStatus, null);
    }

    private void initializeWorkspace(DataLoadStatus dataLoadStatus, IOException saveFailure) {
        isConfirmationAccepted = true;
        confirmationPrompt = null;
        TAssist initialData = dataLoadStatus.isFailure() ? new TAssist() : TypicalGroups.getTypicalTAssist();
        model = new ModelManager(initialData, new UserPrefs());
        data = temporaryFolder.resolve("tassist.json");
        JsonTAssistStorage dataStorage = new JsonTAssistStorage(data) {
            @Override
            public void saveTAssist(ReadOnlyTAssist tAssist) throws IOException {
                if (saveFailure != null) {
                    throw saveFailure;
                }
                super.saveTAssist(tAssist);
            }
        };
        StorageManager storage = new StorageManager(dataStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        CommandRegistry commandRegistry = new CommandRegistry();
        GroupCommandRegistration.registerCommands(commandRegistry);
        AttendanceCommandRegistration.registerCommands(commandRegistry);
        AssignmentCommandRegistration.registerCommands(commandRegistry);
        ParticipationCommandRegistration.registerCommands(commandRegistry);
        StudentCommandRegistration.registerCommands(commandRegistry);
        stage = new Stage();
        LogicManager logic = new LogicManager(model, storage, commandRegistry, dataLoadStatus);
        MainWindow window = shouldUseDefaultConfirmationHandler
                ? new MainWindow(stage, logic, data, dataLoadStatus)
                : new MainWindow(stage, logic, data, dataLoadStatus, this::isPromptAccepted);
        window.fillInnerParts();
        window.show();
        root = stage.getScene().getRoot();
        root.applyCss();
        input = (TextField) root.lookup("#commandTextField");
        tabs = (TabPane) root.lookup("#tabs");
        assertNotNull(input);
        assertEquals(WorkspaceView.values().length, tabs.getTabs().size());
        assertEquals(StudentListPanel.STUDENTS_TITLE, ((Label) root.lookup("#groupTitle")).getText());
    }

    private boolean isPromptAccepted(Stage owner, String prompt) {
        confirmationPrompt = prompt;
        return isConfirmationAccepted;
    }

    private void respondToConfirmation(String buttonText) {
        Platform.runLater(() -> confirmationButton(buttonText).fire());
    }

    /**
     * Presses Enter on the real confirmation dialog after it has selected Cancel.
     */
    private void cancelConfirmationWithEnter(String expectedPrompt) {
        Platform.runLater(() -> Platform.runLater(() -> {
            Stage dialogStage = confirmationDialog();
            DialogPane pane = (DialogPane) dialogStage.getScene().getRoot();
            Button cancel = confirmationButton("Cancel");
            Button proceed = confirmationButton("Continue");
            assertEquals(expectedPrompt, pane.getContentText());
            assertTrue(cancel.isDefaultButton());
            assertTrue(cancel.isCancelButton());
            assertFalse(proceed.isDefaultButton());
            assertEquals(cancel, dialogStage.getScene().getFocusOwner());
            Runnable enterAction = dialogStage.getScene().getAccelerators()
                    .get(new KeyCodeCombination(KeyCode.ENTER));
            assertNotNull(enterAction);
            enterAction.run();
        }));
    }

    private Button confirmationButton(String buttonText) {
        DialogPane pane = (DialogPane) confirmationDialog().getScene().getRoot();
        ButtonType choice = pane.getButtonTypes().stream()
                .filter(buttonType -> buttonText.equals(buttonType.getText()))
                .findFirst().orElseThrow();
        return (Button) pane.lookupButton(choice);
    }

    private Stage confirmationDialog() {
        return Window.getWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(Stage::isShowing)
                .filter(window -> "Confirm destructive action".equals(window.getTitle()))
                .findFirst().orElseThrow();
    }

    private void runOnFx(CheckedAction action) throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            action.run();
            return null;
        });
        Platform.runLater(check);
        check.get(20, TimeUnit.SECONDS);
    }

    /**
     * Allows a JavaFX test step to report checked failures to the test thread.
     */
    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }

    private void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }

    private void pressKey(KeyCode key) {
        input.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", key, false, false, false, false));
    }

    private void pressKeyWithModifier(KeyCode key, KeyCode modifier) {
        input.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", key, modifier == KeyCode.SHIFT,
                modifier == KeyCode.CONTROL, modifier == KeyCode.ALT, modifier == KeyCode.META));
    }

    private void render(Parent root, String name, int width, int height) throws Exception {
        render(root, name, width, height, 1);
    }

    private void render(Parent root, String name, int width, int height, double scale) throws Exception {
        root.resize(width, height);
        root.applyCss();
        root.layout();
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(new Scale(scale, scale));
        WritableImage image = root.snapshot(parameters, null);
        BufferedImage output = new BufferedImage((int) image.getWidth(), (int) image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < output.getHeight(); y++) {
            for (int x = 0; x < output.getWidth(); x++) {
                output.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        Path directory = Path.of("build", "ui-previews");
        Files.createDirectories(directory);
        ImageIO.write(output, "png", directory.resolve(name + ".png").toFile());
    }
}
