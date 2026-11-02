package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_GROUP;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupName;

/**
 * Deletes a tutorial group together with its students and their records.
 */
public class GroupDeleteCommand extends Command {
    public static final String MESSAGE_USAGE = "group delete g/GROUP_NAME [f/yes]";
    public static final String MESSAGE_SUCCESS = "Deleted tutorial group %s and its %s.";
    public static final String MESSAGE_CONFIRMATION =
            "Delete tutorial group %s, its %s and %s, with all their attendance, "
                    + "participation, submission, and grade records?";
    public static final String MESSAGE_NOW_NO_ACTIVE_GROUP =
            "\nNo group is active now. Select one with: group use g/GROUP_NAME";
    public static final String MESSAGE_NOW_NO_GROUPS =
            "\nNo groups left. Create one with: group add g/GROUP_NAME";

    private static final Logger logger = LogsCenter.getLogger(GroupDeleteCommand.class);
    private final GroupName name;
    private final boolean shouldSkipConfirmation;

    /**
     * Creates a command to delete the group with the given name.
     */
    public GroupDeleteCommand(GroupName name) {
        this(name, false);
    }

    /**
     * Creates a group deletion command that optionally skips the confirmation prompt.
     *
     * @param name The name of the group to delete.
     * @param shouldSkipConfirmation Whether to skip the confirmation prompt.
     */
    public GroupDeleteCommand(GroupName name, boolean shouldSkipConfirmation) {
        this.name = requireNonNull(name);
        this.shouldSkipConfirmation = shouldSkipConfirmation;
    }

    @Override
    public Optional<String> getConfirmationPrompt(Model model) {
        requireNonNull(model);
        if (shouldSkipConfirmation || !model.hasGroup(name)) {
            return Optional.empty();
        }
        Group group = model.getGroup(name);
        return Optional.of(String.format(MESSAGE_CONFIRMATION, group.getName(),
                StringUtil.pluralize(group.getStudentList().size(), "student"),
                StringUtil.pluralize(group.getAssignmentList().size(), "assignment")));
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (!model.hasGroup(name)) {
            throw new CommandException(String.format(MESSAGE_UNKNOWN_GROUP, name));
        }

        Group removed = model.removeGroup(name);
        assert !model.hasGroup(name) : "The deleted group must no longer exist";
        logger.fine("Deleted a group with " + removed.getStudentList().size() + " student(s)");
        String feedback = String.format(MESSAGE_SUCCESS, removed.getName(),
                StringUtil.pluralize(removed.getStudentList().size(), "student"));
        if (model.getActiveGroup().isPresent()) {
            return new CommandResult(feedback);
        }
        boolean hasNoGroups = model.getTAssist().getGroupList().isEmpty();
        return new CommandResult(feedback + (hasNoGroups ? MESSAGE_NOW_NO_GROUPS : MESSAGE_NOW_NO_ACTIVE_GROUP));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof GroupDeleteCommand otherCommand
                && name.equals(otherCommand.name) && shouldSkipConfirmation == otherCommand.shouldSkipConfirmation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, shouldSkipConfirmation);
    }
}
