package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.model.group.Group;

/**
 * Deletes an assignment, with its submission and grade records, from the active tutorial group.
 */
public class AssignmentDeleteCommand extends Command {
    public static final String MESSAGE_USAGE = "assignment delete a/ASSIGNMENT_NAME [f/yes]";
    public static final String MESSAGE_SUCCESS = "Deleted assignment %s from %s.";
    public static final String MESSAGE_CONFIRMATION =
            "Delete assignment %s and all its submission and grade records from %s?";

    private static final Logger logger = LogsCenter.getLogger(AssignmentDeleteCommand.class);
    private final AssignmentName assignmentName;
    private final boolean shouldSkipConfirmation;

    /**
     * Creates a command to delete the assignment with the given name.
     */
    public AssignmentDeleteCommand(AssignmentName assignmentName) {
        this(assignmentName, false);
    }

    /**
     * Creates an assignment deletion command that optionally skips the confirmation prompt.
     *
     * @param assignmentName The name of the assignment to delete.
     * @param shouldSkipConfirmation Whether to skip the confirmation prompt.
     */
    public AssignmentDeleteCommand(AssignmentName assignmentName, boolean shouldSkipConfirmation) {
        this.assignmentName = requireNonNull(assignmentName);
        this.shouldSkipConfirmation = shouldSkipConfirmation;
    }

    @Override
    public Optional<String> getConfirmationPrompt(Model model) {
        requireNonNull(model);
        if (shouldSkipConfirmation) {
            return Optional.empty();
        }
        return model.getActiveGroup().flatMap(group -> group.findAssignment(assignmentName)
                .map(assignment -> String.format(MESSAGE_CONFIRMATION, assignment.getName(), group.getName())));
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = getActiveGroup(model);
        getAssignmentToChange(group, assignmentName);

        Assignment removed = model.removeAssignment(group.getName(), assignmentName);
        assert !group.hasAssignment(assignmentName) : "The deleted assignment must no longer belong to the group";
        logger.fine("Deleted an assignment from " + group.getName());
        return new CommandResult(String.format(MESSAGE_SUCCESS, removed.getName(), group.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AssignmentDeleteCommand otherCommand
                && assignmentName.equals(otherCommand.assignmentName)
                && shouldSkipConfirmation == otherCommand.shouldSkipConfirmation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentName, shouldSkipConfirmation);
    }
}
