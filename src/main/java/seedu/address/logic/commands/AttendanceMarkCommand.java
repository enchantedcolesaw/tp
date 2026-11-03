package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;
import seedu.address.model.student.AttendanceStatus;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.WeeklyRecord;

/**
 * Records or replaces the attendance of one or more students in the active group for a teaching week.
 */
public class AttendanceMarkCommand extends Command {
    public static final String PREFIX_STUDENT_ID = "id/";
    public static final String PREFIX_WEEK = "w/";
    public static final String PREFIX_STATUS = "s/";
    public static final String MESSAGE_USAGE = "attendance mark " + PREFIX_STUDENT_ID + "STUDENT_ID "
            + "[" + PREFIX_STUDENT_ID + "STUDENT_ID]... " + PREFIX_WEEK + "WEEK " + PREFIX_STATUS + "STATUS";
    public static final String MESSAGE_RECORDED = "Recorded %s as %s for Week %d.";
    public static final String MESSAGE_UPDATED = "Updated %s's attendance for Week %d: %s to %s.";
    public static final String MESSAGE_UNCHANGED = "%s's attendance for Week %d is already %s. Nothing changed.";
    public static final String MESSAGE_SCORE_WARNING = " Warning: %s has participation score %d for Week %d.";

    private final List<StudentId> studentIds;
    private final int week;
    private final AttendanceStatus status;

    /**
     * Creates a command to record the given student's attendance status for a valid teaching week.
     */
    public AttendanceMarkCommand(StudentId studentId, int week, AttendanceStatus status) {
        this(List.of(requireNonNull(studentId)), week, status);
    }

    /**
     * Creates a command to record the given students' attendance status for a valid teaching week.
     * The list must not be empty.
     */
    public AttendanceMarkCommand(List<StudentId> studentIds, int week, AttendanceStatus status) {
        requireNonNull(studentIds);
        checkArgument(!studentIds.isEmpty(), "At least one student ID is required.");
        this.studentIds = List.copyOf(studentIds);
        WeeklyRecord.validateWeek(week);
        this.week = week;
        this.status = requireNonNull(status);
    }

    /**
     * Returns an incomplete command for {@code studentId} that the TA finishes by entering the week and status.
     */
    public static String buildTemplate(StudentId studentId) {
        requireNonNull(studentId);
        return "attendance mark " + PREFIX_STUDENT_ID + studentId + " " + PREFIX_WEEK;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = getActiveGroup(model);
        List<Student> students = getStudents(group, studentIds);

        List<String> feedback = new ArrayList<>();
        for (Student student : students) {
            Optional<String> oldStatus = student.getAttendanceRecord(week).map(AttendanceMarkCommand::toCommandValue);
            model.setStudent(group.getName(), student.withAttendanceRecord(week, status.name()));
            feedback.add(createFeedback(student, oldStatus) + warnIfScored(student));
        }
        return new CommandResult(String.join("\n", feedback));
    }

    /**
     * Returns feedback that reports a new mark, an unchanged mark, or the old and new status.
     */
    private String createFeedback(Student student, Optional<String> oldStatus) {
        String newStatus = status.getCommandValue();
        if (oldStatus.isEmpty()) {
            return String.format(MESSAGE_RECORDED, student.getName(), newStatus, week);
        }
        if (oldStatus.get().equals(newStatus)) {
            return String.format(MESSAGE_UNCHANGED, student.getName(), week, newStatus);
        }
        return String.format(MESSAGE_UPDATED, student.getName(), week, oldStatus.get(), newStatus);
    }

    /**
     * Returns a warning if a student with a positive score for the week is marked absent or excused.
     */
    private String warnIfScored(Student student) {
        Optional<Integer> score = student.getParticipationRecord(week).filter(value -> value > 0);
        return status.isAbsence() && score.isPresent()
                ? String.format(MESSAGE_SCORE_WARNING, student.getName(), score.get(), week) : "";
    }

    private static String toCommandValue(String storedStatus) {
        return AttendanceStatus.fromValue(storedStatus).getCommandValue();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AttendanceMarkCommand otherCommand
                && studentIds.equals(otherCommand.studentIds)
                && week == otherCommand.week && status == otherCommand.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIds, week, status);
    }
}
