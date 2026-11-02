package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.WorkspaceView;
import seedu.address.model.Model;

/**
 * Shows a workspace screen without changing stored records.
 */
public class ViewCommand extends Command {
    public static final String COMMAND_WORD = "view";
    public static final String MESSAGE_USAGE = "view: Opens a screen.\n"
            + "Parameters: students | records | help | storage\n"
            + "The singular form student is also accepted.\n"
            + "Example: view storage";
    public static final String MESSAGE_UNKNOWN_SCREEN = "Unknown screen: %s.\n" + MESSAGE_USAGE;

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
