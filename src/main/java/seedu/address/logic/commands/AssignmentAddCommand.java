package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.model.group.Group;

/**
 * Creates an assignment in the active tutorial group.
 */
public class AssignmentAddCommand extends Command {
    public static final String MESSAGE_USAGE = "assignment add a/ASSIGNMENT_NAME";
    public static final String MESSAGE_SUCCESS = "Created assignment %s for %s.";
    public static final String MESSAGE_DUPLICATE_ASSIGNMENT = "Assignment %s already exists in %s.";
    public static final String MESSAGE_SIMILAR_WARNING =
            " Warning: %s already has a similar assignment named %s. Check that this is a different assignment.";

    private static final Logger logger = LogsCenter.getLogger(AssignmentAddCommand.class);
    private final AssignmentName assignmentName;

    /**
     * Creates a command to add an assignment with the given valid name.
     */
    public AssignmentAddCommand(AssignmentName assignmentName) {
        this.assignmentName = requireNonNull(assignmentName);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = getActiveGroup(model);
        Optional<Assignment> existing = group.findAssignment(assignmentName);
        if (existing.isPresent()) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_ASSIGNMENT,
                    existing.get().getName(), group.getName()));
        }

        String warning = group.getAssignmentList().stream().map(Assignment::getName)
                .filter(name -> simplify(name).equals(simplify(assignmentName))).findFirst()
                .map(name -> String.format(MESSAGE_SIMILAR_WARNING, group.getName(), name)).orElse("");
        model.addAssignment(group.getName(), new Assignment(assignmentName));
        assert group.hasAssignment(assignmentName) : "The added assignment must belong to the active group";
        logger.fine("Added an assignment to " + group.getName());
        return new CommandResult(String.format(MESSAGE_SUCCESS, assignmentName, group.getName()) + warning);
    }

    /**
     * Returns the letters and digits of {@code name} in lowercase, so that PS1, PS 1 and PS-1 look alike.
     */
    private static String simplify(AssignmentName name) {
        return name.fullName.replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof AssignmentAddCommand otherCommand
                && assignmentName.equals(otherCommand.assignmentName);
    }

    @Override
    public int hashCode() {
        return assignmentName.hashCode();
    }
}
