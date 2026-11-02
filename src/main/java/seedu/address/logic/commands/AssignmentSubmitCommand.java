package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.model.assignment.AssignmentRecord;
import seedu.address.model.group.Group;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/**
 * Records or replaces the submission status of one or more students for an assignment.
 * Marking the assignment not submitted clears any grade recorded for it.
 */
public class AssignmentSubmitCommand extends Command {
    public static final String MESSAGE_USAGE = "assignment submit a/ASSIGNMENT_NAME id/STUDENT_ID "
            + "[id/STUDENT_ID]... s/STATUS";
    public static final String MESSAGE_SUCCESS = "Recorded %s's %s submission status as %s.";
    public static final String MESSAGE_UNCHANGED =
            "%s's %s submission status is already %s. Nothing changed.";
    public static final String MESSAGE_SUCCESS_WITHDRAWN =
            "Recorded %s's %s submission status as not submitted; the recorded grade was cleared.";

    private static final Logger logger = LogsCenter.getLogger(AssignmentSubmitCommand.class);

    private final AssignmentName assignmentName;
    private final List<StudentId> studentIds;
    private final boolean isSubmitted;

    /**
     * Creates a command to set the student's status to submitted or not submitted.
     */
    public AssignmentSubmitCommand(AssignmentName assignmentName, StudentId studentId, boolean isSubmitted) {
        this(assignmentName, List.of(requireNonNull(studentId)), isSubmitted);
    }

    /**
     * Creates a command to set the status of the given students to submitted or not submitted.
     * The list must not be empty.
     */
    public AssignmentSubmitCommand(AssignmentName assignmentName, List<StudentId> studentIds, boolean isSubmitted) {
        this.assignmentName = requireNonNull(assignmentName);
        requireNonNull(studentIds);
        checkArgument(!studentIds.isEmpty(), "At least one student ID is required.");
        this.studentIds = List.copyOf(studentIds);
        this.isSubmitted = isSubmitted;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = getActiveGroup(model);
        Assignment assignment = getAssignment(group, assignmentName);
        List<Student> students = getStudents(group, studentIds);

        Assignment updated = assignment;
        List<String> feedback = new ArrayList<>();
        for (Student student : students) {
            AssignmentRecord previousRecord = assignment.getStudentRecord(student.getStudentId());
            feedback.add(createFeedback(student, assignment, previousRecord));
            if (isSubmitted != previousRecord.isSubmitted()) {
                updated = updated.withSubmissionStatus(student.getStudentId(), isSubmitted);
            }
        }

        if (!updated.equals(assignment)) {
            model.setAssignment(group.getName(), updated);
            logger.fine("Updated assignment submissions for " + students.size() + " student(s) in "
                    + group.getName());
        }
        return new CommandResult(String.join("\n", feedback));
    }

    /**
     * Returns the feedback for one student, given that student's record before this command.
     */
    private String createFeedback(Student student, Assignment assignment, AssignmentRecord previousRecord) {
        if (isSubmitted == previousRecord.isSubmitted()) {
            return String.format(MESSAGE_UNCHANGED, student.getName(), assignment.getName(), describeStatus());
        }
        if (!isSubmitted && previousRecord.getGrade().isPresent()) {
            return String.format(MESSAGE_SUCCESS_WITHDRAWN, student.getName(), assignment.getName());
        }
        return String.format(MESSAGE_SUCCESS, student.getName(), assignment.getName(), describeStatus());
    }

    private String describeStatus() {
        return isSubmitted ? "submitted" : "not submitted";
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AssignmentSubmitCommand otherCommand
                && assignmentName.equals(otherCommand.assignmentName)
                && studentIds.equals(otherCommand.studentIds) && isSubmitted == otherCommand.isSubmitted;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentName, studentIds, isSubmitted);
    }
}
