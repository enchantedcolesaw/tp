package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentRecord;
import seedu.address.model.group.Group;
import seedu.address.model.student.AttendanceStatus;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.WeeklyRecord;

/**
 * Lists one student's attendance and participation for every week, and their record for every assignment of the
 * active group, so that the TA can answer a student's question about their standing.
 */
public class StudentRecordsCommand extends Command {
    public static final String MESSAGE_USAGE = "student records id/STUDENT_ID";
    public static final String MESSAGE_HEADER = "Records for %s (%s) in %s:";
    public static final String MESSAGE_WEEKS_HEADING = "Weeks (attendance | participation score):";
    public static final String MESSAGE_ASSIGNMENTS_HEADING = "Assignments (submission | grade):";
    public static final String MESSAGE_NO_ASSIGNMENTS = "Assignments: none in %s.";
    public static final String NOT_RECORDED = "Not recorded";

    private final StudentId studentId;

    /**
     * Creates a command to list the records of the student with the given valid ID.
     */
    public StudentRecordsCommand(StudentId studentId) {
        this.studentId = requireNonNull(studentId);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = getActiveGroup(model);
        Student student = getStudent(group, studentId);
        StringBuilder listing = new StringBuilder(String.format(MESSAGE_HEADER, student.getName(),
                student.getStudentId(), group.getName()));
        listing.append('\n').append(MESSAGE_WEEKS_HEADING);
        for (int week = WeeklyRecord.MIN_WEEK; week <= WeeklyRecord.MAX_WEEK; week++) {
            listing.append("\nWeek ").append(week).append(" | ")
                    .append(student.getAttendanceRecord(week).map(AttendanceStatus::formatStoredValue)
                            .orElse(NOT_RECORDED)).append(" | ")
                    .append(student.getParticipationRecord(week).map(String::valueOf).orElse(NOT_RECORDED));
        }
        appendAssignments(listing, group, student);
        // Rerunning by the stored ID keeps the listing current after changes, and reports it unavailable once the
        // student is deleted, given a new ID, or not in the newly active group.
        return CommandResult.forRecordListing(listing.toString(), getListCommand(student.getStudentId()));
    }

    private static void appendAssignments(StringBuilder listing, Group group, Student student) {
        List<Assignment> assignments = group.getAssignmentList();
        if (assignments.isEmpty()) {
            listing.append('\n').append(String.format(MESSAGE_NO_ASSIGNMENTS, group.getName()));
            return;
        }
        listing.append('\n').append(MESSAGE_ASSIGNMENTS_HEADING);
        for (Assignment assignment : assignments) {
            AssignmentRecord record = assignment.getStudentRecord(student.getStudentId());
            listing.append('\n').append(assignment.getName()).append(" | ")
                    .append(record.isSubmitted()
                            ? AssignmentListCommand.STATUS_SUBMITTED : AssignmentListCommand.STATUS_NOT_SUBMITTED)
                    .append(" | ")
                    .append(record.getGrade().map(String::valueOf).orElse(AssignmentListCommand.GRADE_NOT_RECORDED));
        }
    }

    /**
     * Returns the command that lists the records of the student with {@code studentId}.
     */
    public static String getListCommand(StudentId studentId) {
        return "student records id/" + studentId;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof StudentRecordsCommand otherCommand && studentId.equals(otherCommand.studentId);
    }

    @Override
    public int hashCode() {
        return studentId.hashCode();
    }
}
