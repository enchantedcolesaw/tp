package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.DataLoadStatus;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.WorkspaceView;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.AssignmentListCommand;
import seedu.address.logic.commands.AttendanceMarkCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.group.Group;

/**
 * Connects the TAssist workspace, command input and feedback to application logic.
 */
public class MainWindow extends UiPart<Stage> {
    static final String MESSAGE_CANCELLED = "Command cancelled. No changes were made.";
    static final String MESSAGE_LISTING_UNAVAILABLE = "This listing is no longer available. ";
    private static final String FXML = "MainWindow.fxml";
    private final Logger logger = LogsCenter.getLogger(getClass());
    private final Stage primaryStage;
    private final Logic logic;
    private final Path dataFilePath;
    private final DataLoadStatus dataLoadStatus;
    private final ConfirmationHandler confirmationHandler;

    private WorkspacePanel workspacePanel;
    private ResultDisplay resultDisplay;
    /** The last list command shown on Records, rerun when data changes so its listing stays current. */
    private String recordListingCommand;
    /** The last assignment rename that Records followed, so that undo and redo can follow it back. */
    private Map.Entry<String, String> followedRename;
    private CommandBox commandBox;
    private StatusBarFooter statusBarFooter;

    @FXML
    private StackPane commandBoxPlaceholder;
    @FXML
    private StackPane groupStripPlaceholder;
    @FXML
    private StackPane navigationPlaceholder;
    @FXML
    private StackPane workspacePlaceholder;
    @FXML
    private StackPane resultDisplayPlaceholder;
    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates the main window with its logic and configured data path.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        this(primaryStage, logic, dataFilePath, DataLoadStatus.LOADED);
    }

    /**
     * Creates the main window with the startup data-loading outcome.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath, DataLoadStatus dataLoadStatus) {
        this(primaryStage, logic, dataFilePath, dataLoadStatus, MainWindow::isConfirmationAccepted);
    }

    /**
     * Creates the main window with a testable handler for destructive-command confirmations.
     */
    MainWindow(Stage primaryStage, Logic logic, Path dataFilePath, DataLoadStatus dataLoadStatus,
            ConfirmationHandler confirmationHandler) {
        super(FXML, primaryStage);
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;
        this.dataLoadStatus = requireNonNull(dataLoadStatus);
        this.confirmationHandler = requireNonNull(confirmationHandler);

        setWindowDefaultSize(logic.getGuiSettings());
        setAccelerators();

        Region workspace = (Region) primaryStage.getScene().getRoot();
        workspace.heightProperty().addListener((observable, oldHeight, height) ->
                workspace.pseudoClassStateChanged(PseudoClass.getPseudoClass("compact"), height.doubleValue() < 540));
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F1) {
                handleHelp();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                commandBox.focus();
                event.consume();
            }
        });
    }

    /**
     * Fills the window with its independent UI parts.
     */
    void fillInnerParts() {
        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        commandBox = new CommandBox(this::executeCommand, this::scrollOutput);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());

        GroupStrip groupStrip = new GroupStrip(logic.getGroupList(), logic.activeGroupProperty(),
                dataLoadStatus.isFailure());
        groupStripPlaceholder.getChildren().add(groupStrip.getRoot());

        workspacePanel = new WorkspacePanel(logic.activeGroupProperty(), logic.getGroupList(),
                logic.getActiveGroupStudentList(),
                logic.getDisplayedStudentList(), logic.studentFilterActiveProperty(),
                dataFilePath, logic.getHelpEntries(), student -> commandBox.fillTemplate(
                        AttendanceMarkCommand.buildTemplate(student.getStudentId())),
                dataLoadStatus.isFailure());
        workspacePlaceholder.getChildren().add(workspacePanel.getRoot());
        navigationPlaceholder.getChildren().add(workspacePanel.getNavigationBar());

        statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());
        if (dataLoadStatus != DataLoadStatus.LOADED) {
            resultDisplay.setFeedbackToUser(dataLoadStatus.getMessage(), dataLoadStatus.isFailure());
            statusBarFooter.setStatus(dataLoadStatus.getMessage(), dataLoadStatus.isFailure());
        }

        Platform.runLater(commandBox::focus);
    }

    private void scrollOutput(boolean isDown) {
        if (resultDisplay.hasLongMessage() || !workspacePanel.scrollRecords(isDown)) {
            resultDisplay.scrollPage(isDown);
        }
    }

    /**
     * Reruns the list command shown on Records, so that it shows the records as they are after a change.
     */
    private void refreshRecordListing(Optional<Group> previousGroup) {
        if (recordListingCommand == null) {
            return;
        }
        try {
            CommandResult listing = executeListing(previousGroup);
            // The listed assignment may now have other capitals, as in another group, so keep its stored name.
            recordListingCommand = listing.getListingCommand().orElse(recordListingCommand);
            workspacePanel.refreshRecordListing(listing.getFeedbackToUser());
        } catch (CommandException | ParseException e) {
            logger.fine("Records could not be refreshed: " + e.getMessage());
            String reason = e.getMessage().lines().findFirst().orElse("");
            workspacePanel.setRecordListing(MESSAGE_LISTING_UNAVAILABLE + reason);
        }
    }

    /**
     * Reruns the list command shown on Records. If it fails after an undone or redone assignment rename, reruns it
     * with the assignment's name from the other side of that rename, only when that name appeared in the same group.
     */
    private CommandResult executeListing(Optional<Group> previousGroup) throws CommandException, ParseException {
        try {
            return logic.execute(recordListingCommand);
        } catch (CommandException e) {
            if (followedRename == null || previousGroup.isEmpty()
                    || !previousGroup.map(Group::getName)
                            .equals(logic.activeGroupProperty().get().map(Group::getName))) {
                throw e;
            }
            String otherSide = followedRename.getValue().equalsIgnoreCase(recordListingCommand)
                    ? followedRename.getKey() : followedRename.getValue();
            List<String> previousListings = previousGroup.get().getAssignmentList().stream()
                    .map(assignment -> AssignmentListCommand.getListCommand(assignment.getName())).toList();
            if (!previousListings.stream().anyMatch(recordListingCommand::equalsIgnoreCase)
                    || previousListings.stream().anyMatch(otherSide::equalsIgnoreCase)) {
                throw e;
            }
            try {
                CommandResult listing = logic.execute(otherSide);
                recordListingCommand = otherSide;
                return listing;
            } catch (CommandException | ParseException otherSideError) {
                throw e;
            }
        }
    }

    private void setWindowDefaultSize(GuiSettings guiSettings) {
        var bounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setHeight(Math.min(guiSettings.getWindowHeight(), bounds.getHeight()));
        primaryStage.setWidth(Math.min(guiSettings.getWindowWidth(), bounds.getWidth()));
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    private static boolean isConfirmationAccepted(Stage owner, String prompt) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.initOwner(owner);
        dialog.setTitle("Confirm destructive action");
        dialog.setHeaderText("You can reverse this with undo.");
        dialog.setContentText(prompt);
        ButtonType continueButton = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getButtonTypes().setAll(continueButton, cancelButton);
        Button cancel = (Button) dialog.getDialogPane().lookupButton(cancelButton);
        assert cancel != null : "The confirmation dialog must include a Cancel button";
        // Enter activates the default button, so Cancel must be that button.
        selectCancelByDefault(dialog, continueButton, cancel);
        dialog.setOnShown(event -> Platform.runLater(() ->
                selectCancelByDefault(dialog, continueButton, cancel)));
        return dialog.showAndWait().filter(continueButton::equals).isPresent();
    }

    /**
     * Makes Cancel the default and focused button so Enter does not confirm deletion.
     */
    private static void selectCancelByDefault(Alert dialog, ButtonType continueButton, Button cancel) {
        if (dialog.getDialogPane().lookupButton(continueButton) instanceof Button continueAction) {
            continueAction.setDefaultButton(false);
        }
        cancel.setDefaultButton(true);
        cancel.requestFocus();
    }

    /**
     * Shows the inline command reference.
     */
    private void handleHelp() {
        workspacePanel.showView(WorkspaceView.HELP);
        commandBox.focus();
    }

    /**
     * Opens Help, at the requested topic, if {@code result} asks for it. Other commands keep the current screen.
     */
    private void showHelpIfRequested(CommandResult result) {
        if (result.isShowHelp()) {
            workspacePanel.showHelp(result.getHelpTopic());
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Saves window preferences and closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        primaryStage.hide();
    }

    /**
     * Executes a command and displays its result without clearing failed input.
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            Optional<String> confirmationPrompt = logic.getConfirmationPrompt(commandText);
            if (confirmationPrompt.isPresent()
                    && !confirmationHandler.isConfirmed(primaryStage, confirmationPrompt.get())) {
                logger.info("Command cancelled: " + commandText);
                resultDisplay.setFeedbackToUser(MESSAGE_CANCELLED);
                statusBarFooter.setStatus("Command cancelled");
                return CommandResult.forCancelled(MESSAGE_CANCELLED);
            }

            Optional<Group> previousGroup = logic.activeGroupProperty().get().map(Group::new);
            CommandResult result = logic.execute(commandText);
            logger.info("Result: " + result.getFeedbackToUser());
            if (result.isRecordListing()) {
                recordListingCommand = result.getListingCommand().orElse(commandText);
                workspacePanel.setRecordListing(result.getFeedbackToUser());
            } else if (result.hasSavedChanges()) {
                result.getListingRename().filter(rename -> rename.getKey().equalsIgnoreCase(recordListingCommand))
                        .ifPresent(rename -> {
                            recordListingCommand = rename.getValue();
                            followedRename = rename;
                        });
                refreshRecordListing(previousGroup);
            }
            resultDisplay.setFeedbackToUser(result.isRecordListing()
                    ? result.getFeedbackToUser().lines().findFirst().orElse("") : result.getFeedbackToUser());
            if (result.getView().isPresent()) {
                workspacePanel.showView(result.getView().get());
            }
            if (result.hasSavedChanges()) {
                statusBarFooter.setStatus("Changes saved.");
            } else if (dataLoadStatus.isFailure()) {
                statusBarFooter.setStatus(dataLoadStatus.getMessage(), true);
            } else {
                statusBarFooter.setStatus("Local data file");
            }
            showHelpIfRequested(result);
            if (result.isExit()) {
                handleExit();
            }
            return result;
        } catch (CommandException | ParseException e) {
            logger.info("Command failed: " + e.getMessage());
            resultDisplay.setFeedbackToUser(e.getMessage(), true);
            statusBarFooter.setStatus("Command failed — see feedback", true);
            throw e;
        }
    }

    /**
     * Confirms a destructive command before allowing it to execute.
     */
    @FunctionalInterface
    interface ConfirmationHandler {
        /**
         * Shows a confirmation prompt and returns whether the user accepted it.
         */
        boolean isConfirmed(Stage owner, String prompt);
    }
}
