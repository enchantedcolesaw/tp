package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_NO_ACTIVE_GROUP;

import seedu.address.commons.core.WorkspaceView;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;
import seedu.address.model.student.StudentSortField;

/**
 * Sorts the currently displayed students by name or ID in ascending order.
 */
public class StudentSortCommand extends Command {
    public static final String MESSAGE_USAGE = "student sort by/FIELD";
    public static final String MESSAGE_SUCCESS = "Sorted %s in %s by %s.";

    private final StudentSortField field;

    /** Creates a command to sort by {@code field}, preserving the current search. */
    public StudentSortCommand(StudentSortField field) {
        this.field = requireNonNull(field);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = model.getActiveGroup().orElseThrow(() -> new CommandException(MESSAGE_NO_ACTIVE_GROUP));
        model.sortStudents(field.getComparator());
        int count = model.getDisplayedStudentList().size();
        String feedback = String.format(MESSAGE_SUCCESS, StringUtil.pluralize(count, "student"), group.getName(),
                field.getKeyword());
        return new CommandResult(feedback, WorkspaceView.STUDENTS);
    }
}
