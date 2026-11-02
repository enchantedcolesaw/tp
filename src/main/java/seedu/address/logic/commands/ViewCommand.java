package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.WorkspaceView;
import seedu.address.model.Model;

/**
 * Shows a workspace screen without changing stored records.
 */
public class ViewCommand extends Command {
    public static final String COMMAND_WORD = "view";
    public static final String MESSAGE_SCREENS = "Use one of: students, records, help, storage. Example: view storage";
    public static final String MESSAGE_MISSING_SCREEN = "Missing screen name. " + MESSAGE_SCREENS;
    public static final String MESSAGE_EXTRA_TEXT = "Unexpected extra text after '%s': '%s'. Enter one screen name.";
    public static final String MESSAGE_UNKNOWN_SCREEN = "Unknown screen '%s'. " + MESSAGE_SCREENS;

    private final WorkspaceView view;

    /**
     * Creates a command to show the specified screen.
     */
    public ViewCommand(WorkspaceView view) {
        this.view = requireNonNull(view);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult("Showing " + view.getKeyword() + ".", view);
    }

    @Override
    public boolean canRunWithoutSavedData() {
        return true;
    }
}
