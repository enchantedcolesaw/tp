package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_NO_ACTIVE_GROUP;

import seedu.address.commons.core.WorkspaceView;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;
import seedu.address.model.student.StudentSearchPredicate;

/**
 * Shows students in the active group matching a name fragment, ID fragment, or both.
 */
public class StudentFindCommand extends Command {
    public static final String MESSAGE_USAGE = "student find [n/NAME_FRAGMENT] [id/ID_FRAGMENT]";
    public static final String MESSAGE_NO_MATCHES =
            "No matching students in %s. Use student list to show all students.";
    public static final String MESSAGE_MATCHES = "Found %s in %s.";

    private final StudentSearchPredicate predicate;

    /** Creates a command that replaces the current search with {@code predicate}, if the group has students. */
    public StudentFindCommand(StudentSearchPredicate predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = model.getActiveGroup().orElseThrow(() -> new CommandException(MESSAGE_NO_ACTIVE_GROUP));
        if (group.getStudentList().isEmpty()) {
            return new CommandResult(String.format(StudentListCommand.MESSAGE_NO_STUDENTS, group.getName()),
                    WorkspaceView.STUDENTS);
        }
        model.filterStudents(predicate);
        int count = model.getDisplayedStudentList().size();
        String feedback = count == 0 ? String.format(MESSAGE_NO_MATCHES, group.getName())
                : String.format(MESSAGE_MATCHES, StringUtil.pluralize(count, "matching student"), group.getName());
        return new CommandResult(feedback, WorkspaceView.STUDENTS);
    }
}
