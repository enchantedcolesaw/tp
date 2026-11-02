package seedu.address.logic.commands;

import static seedu.address.logic.Messages.MESSAGE_NO_ACTIVE_GROUP;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_ASSIGNMENT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_ASSIGNMENT_TO_CHANGE;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_STUDENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.model.group.Group;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Returns a confirmation prompt when this command may remove user data.
     *
     * @param model {@code Model} which the command should operate on.
     * @return A prompt to show before execution, or empty if confirmation is unnecessary.
     */
    public Optional<String> getConfirmationPrompt(Model model) {
        return Optional.empty();
    }

    /**
     * Returns the active tutorial group.
     *
     * @throws CommandException If no group is active.
     */
    protected static Group getActiveGroup(Model model) throws CommandException {
        return model.getActiveGroup().orElseThrow(() -> new CommandException(MESSAGE_NO_ACTIVE_GROUP));
    }

    /**
     * Returns the student with {@code studentId} in {@code group}.
     *
     * @throws CommandException If no such student is in the group.
     */
    protected static Student getStudent(Group group, StudentId studentId) throws CommandException {
        return group.findStudent(studentId).orElseThrow(() -> new CommandException(
                String.format(MESSAGE_UNKNOWN_STUDENT, studentId, group.getName())));
    }

    /**
     * Returns the students with {@code studentIds} in {@code group}, in the same order.
     * Fails without returning anything if any of them is not in the group.
     *
     * @throws CommandException If any such student is not in the group.
     */
    protected static List<Student> getStudents(Group group, List<StudentId> studentIds) throws CommandException {
        List<Student> students = new ArrayList<>();
        for (StudentId studentId : studentIds) {
            students.add(getStudent(group, studentId));
        }
        return students;
    }

    /**
     * Returns the assignment named {@code assignmentName} in {@code group}.
     *
     * @throws CommandException If no such assignment is in the group.
     */
    protected static Assignment getAssignment(Group group, AssignmentName assignmentName) throws CommandException {
        return group.findAssignment(assignmentName).orElseThrow(() -> new CommandException(
                String.format(MESSAGE_UNKNOWN_ASSIGNMENT, assignmentName, group.getName(), assignmentName)));
    }

    /**
     * Returns the assignment named {@code assignmentName} in {@code group}, for a command that renames or deletes it.
     * Unlike {@link #getAssignment}, the error does not suggest creating the assignment.
     *
     * @throws CommandException If no such assignment is in the group.
     */
    protected static Assignment getAssignmentToChange(Group group, AssignmentName assignmentName)
            throws CommandException {
        return group.findAssignment(assignmentName).orElseThrow(() -> new CommandException(
                String.format(MESSAGE_UNKNOWN_ASSIGNMENT_TO_CHANGE, assignmentName, group.getName())));
    }

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return the result of the command, including the feedback to display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

    /**
     * Returns true if the data changes made by this command should be recorded so that they can be undone.
     * Commands that move through the undo history themselves return false.
     */
    public boolean isUndoable() {
        return true;
    }

    /**
     * Returns true if this command works without the TA's saved data, so it still runs after that data could not be
     * read at startup. Commands that show or change tutorial records return false.
     */
    public boolean canRunWithoutSavedData() {
        return false;
    }

}
