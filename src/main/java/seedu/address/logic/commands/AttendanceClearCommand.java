package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;
import seedu.address.model.student.AttendanceStatus;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.WeeklyRecord;

/**
 * Clears the attendance of one or more students in the active group for a teaching week, so that the week shows
 * Not recorded. This corrects marks entered for the wrong students or week.
 */
public class AttendanceClearCommand extends Command {
    public static final String MESSAGE_USAGE = "attendance clear id/STUDENT_ID [id/STUDENT_ID]... w/WEEK";
    public static final String MESSAGE_CLEARED = "Cleared %s's attendance for Week %d: %s to Not recorded.";
    public static final String MESSAGE_NOT_RECORDED = "%s has no attendance recorded for Week %d. Nothing changed.";

    private static final Logger logger = LogsCenter.getLogger(AttendanceClearCommand.class);

    private final List<StudentId> studentIds;
    private final int week;

    /**
     * Creates a command to clear the given student's attendance for a valid teaching week.
     */
    public AttendanceClearCommand(StudentId studentId, int week) {
        this(List.of(requireNonNull(studentId)), week);
    }

    /**
     * Creates a command to clear the given students' attendance for a valid teaching week.
     * The list must not be empty.
     */
    public AttendanceClearCommand(List<StudentId> studentIds, int week) {
        checkArgument(!studentIds.isEmpty(), "At least one student ID is required.");
        this.studentIds = List.copyOf(studentIds);
        WeeklyRecord.validateWeek(week);
        this.week = week;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = getActiveGroup(model);
        List<String> feedback = new ArrayList<>();
        for (Student student : getStudents(group, studentIds)) {
            feedback.add(clear(model, group, student));
        }
        return new CommandResult(String.join("\n", feedback));
    }

    /**
     * Clears {@code student}'s mark for the week if there is one, and returns the feedback line for that student.
     */
    private String clear(Model model, Group group, Student student) {
        Optional<String> storedStatus = student.getAttendanceRecord(week);
        if (storedStatus.isEmpty()) {
            return String.format(MESSAGE_NOT_RECORDED, student.getName(), week);
        }

        String oldStatus = AttendanceStatus.formatStoredValue(storedStatus.get()).toLowerCase(Locale.ROOT);
        Student cleared = student.withoutAttendanceRecord(week);
        model.setStudent(group.getName(), cleared);
        assert cleared.getAttendanceRecord(week).isEmpty() : "The week must no longer have a mark";
        logger.fine("Cleared attendance for Week " + week + " in " + group.getName());
        return String.format(MESSAGE_CLEARED, student.getName(), week, oldStatus);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AttendanceClearCommand otherCommand
                && studentIds.equals(otherCommand.studentIds) && week == otherCommand.week;
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIds, week);
    }
}
