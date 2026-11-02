package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.commands.AssignmentAddCommand.MESSAGE_DUPLICATE_ASSIGNMENT;

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
 * Renames an assignment of the active tutorial group without changing its submission and grade records.
 */
public class AssignmentRenameCommand extends Command {
    public static final String MESSAGE_USAGE = "assignment rename a/ASSIGNMENT_NAME n/NEW_NAME";
    public static final String MESSAGE_SUCCESS = "Renamed assignment %s to %s in %s.";
    public static final String MESSAGE_UNCHANGED = "Assignment %s already has that name. Nothing changed.";

    private static final Logger logger = LogsCenter.getLogger(AssignmentRenameCommand.class);
    private final AssignmentName assignmentName;
    private final AssignmentName newName;

    /**
     * Creates a command to rename the assignment named {@code assignmentName} to {@code newName}.
     */
    public AssignmentRenameCommand(AssignmentName assignmentName, AssignmentName newName) {
        requireAllNonNull(assignmentName, newName);
        this.assignmentName = assignmentName;
        this.newName = newName;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = getActiveGroup(model);
        Assignment target = getAssignment(group, assignmentName);
        Optional<Assignment> clash = group.findAssignment(newName).filter(other -> !other.isSameAssignment(target));
        if (clash.isPresent()) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_ASSIGNMENT,
                    clash.get().getName(), group.getName()));
        }

        AssignmentName oldName = target.getName();
        if (oldName.fullName.equals(newName.fullName)) {
            return new CommandResult(String.format(MESSAGE_UNCHANGED, oldName));
        }

        model.renameAssignment(group.getName(), oldName, newName);
        assert group.hasAssignment(newName) : "The renamed assignment must exist under its new name";
        logger.fine("Renamed an assignment in " + group.getName());
        return CommandResult.forListingRename(String.format(MESSAGE_SUCCESS, oldName, newName, group.getName()),
                AssignmentListCommand.getListCommand(oldName), AssignmentListCommand.getListCommand(newName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AssignmentRenameCommand otherCommand
                && assignmentName.equals(otherCommand.assignmentName) && newName.equals(otherCommand.newName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentName, newName);
    }
}
