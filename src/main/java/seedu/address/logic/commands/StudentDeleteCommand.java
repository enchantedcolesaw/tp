package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_STUDENT;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/**
 * Removes a student and their records from the active tutorial group.
 */
public class StudentDeleteCommand extends Command {
    public static final String MESSAGE_USAGE = "student delete id/STUDENT_ID [f/yes]";
    public static final String MESSAGE_SUCCESS = "Removed %s (%s) from %s.";
    public static final String MESSAGE_CONFIRMATION =
            "Remove %s (%s) and all their attendance, participation, submission, and grade records from %s?";

    private static final Logger logger = LogsCenter.getLogger(StudentDeleteCommand.class);
    private final StudentId studentId;
    private final boolean shouldSkipConfirmation;

    /**
     * Creates a command to delete the student with the given ID.
     */
    public StudentDeleteCommand(StudentId studentId) {
        this(studentId, false);
    }

    /**
     * Creates a student deletion command that optionally skips the confirmation prompt.
     *
     * @param studentId The ID of the student to delete.
     * @param shouldSkipConfirmation Whether to skip the confirmation prompt.
     */
    public StudentDeleteCommand(StudentId studentId, boolean shouldSkipConfirmation) {
        this.studentId = requireNonNull(studentId);
        this.shouldSkipConfirmation = shouldSkipConfirmation;
    }

    @Override
    public Optional<String> getConfirmationPrompt(Model model) {
        requireNonNull(model);
        if (shouldSkipConfirmation) {
            return Optional.empty();
        }
        return model.getActiveGroup().flatMap(group -> group.findStudent(studentId)
                .map(student -> String.format(MESSAGE_CONFIRMATION, student.getName(), student.getStudentId(),
                        group.getName())));
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = getActiveGroup(model);
        if (!group.hasStudent(studentId)) {
            throw new CommandException(String.format(MESSAGE_UNKNOWN_STUDENT, studentId, group.getName()));
        }

        Student removed = model.removeStudent(group.getName(), studentId);
        assert !group.hasStudent(studentId) : "The deleted student must no longer belong to the active group";
        logger.fine("Removed a student from " + group.getName());
        return new CommandResult(String.format(MESSAGE_SUCCESS, removed.getName(), removed.getStudentId(),
                group.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof StudentDeleteCommand otherCommand
                && studentId.equals(otherCommand.studentId)
                && shouldSkipConfirmation == otherCommand.shouldSkipConfirmation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, shouldSkipConfirmation);
    }
}
