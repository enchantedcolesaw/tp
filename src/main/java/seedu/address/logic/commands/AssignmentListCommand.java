package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.model.assignment.AssignmentRecord;
import seedu.address.model.group.Group;
import seedu.address.model.student.Student;

/**
 * Lists the assignments of the active group in creation order, or every student's record for one of them in roster
 * order.
 */
public class AssignmentListCommand extends Command {
    public static final String MESSAGE_USAGE = "assignment list [a/ASSIGNMENT_NAME]";
    public static final String MESSAGE_NO_ASSIGNMENTS =
            "No assignments in %s. Create one with: assignment add a/ASSIGNMENT_NAME";
    public static final String MESSAGE_NO_STUDENTS = "No students in %s.";
    public static final String STATUS_SUBMITTED = "Submitted";
    public static final String STATUS_NOT_SUBMITTED = "Not submitted";
    public static final String GRADE_NOT_RECORDED = "-";

    private final Optional<AssignmentName> assignmentName;

    /**
     * Creates a command to list the names of the active group's assignments.
     */
    public AssignmentListCommand() {
        this.assignmentName = Optional.empty();
    }

    /**
     * Creates a command to list the records of the assignment with the given valid name.
     */
    public AssignmentListCommand(AssignmentName assignmentName) {
        this.assignmentName = Optional.of(requireNonNull(assignmentName));
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = getActiveGroup(model);
        if (assignmentName.isEmpty()) {
            return listAssignmentNames(group);
        }
        return listRecords(group, getAssignment(group, assignmentName.get()));
    }

    private static CommandResult listAssignmentNames(Group group) {
        List<Assignment> assignments = group.getAssignmentList();
        if (assignments.isEmpty()) {
            return CommandResult.forRecordListing(String.format(MESSAGE_NO_ASSIGNMENTS, group.getName()));
        }

        StringBuilder feedback = new StringBuilder("Assignments in " + group.getName() + ":");
        for (int i = 0; i < assignments.size(); i++) {
            feedback.append('\n').append(i + 1).append(". ").append(assignments.get(i).getName());
        }
        return CommandResult.forRecordListing(feedback.toString());
    }

    /**
     * Returns the command that lists the records of the assignment named {@code assignmentName}.
     */
    public static String getListCommand(AssignmentName assignmentName) {
        return "assignment list a/" + assignmentName;
    }

    private static CommandResult listRecords(Group group, Assignment assignment) {
        List<Student> students = group.getStudentList();
        String listCommand = getListCommand(assignment.getName());
        if (students.isEmpty()) {
            return CommandResult.forRecordListing(String.format(MESSAGE_NO_STUDENTS, group.getName()), listCommand);
        }

        StringBuilder feedback = new StringBuilder(assignment.getName() + " records for " + group.getName() + ":");
        for (Student student : students) {
            AssignmentRecord record = assignment.getStudentRecord(student.getStudentId());
            feedback.append('\n').append(student.getName()).append(" | ")
                    .append(student.getStudentId()).append(" | ")
                    .append(record.isSubmitted() ? STATUS_SUBMITTED : STATUS_NOT_SUBMITTED).append(" | ")
                    .append(record.getGrade().map(String::valueOf).orElse(GRADE_NOT_RECORDED));
        }
        return CommandResult.forRecordListing(feedback.toString(), listCommand);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof AssignmentListCommand otherCommand
                && assignmentName.equals(otherCommand.assignmentName);
    }

    @Override
    public int hashCode() {
        return assignmentName.hashCode();
    }
}
