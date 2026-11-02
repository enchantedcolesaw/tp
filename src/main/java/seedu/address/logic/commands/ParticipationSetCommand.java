package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.List;
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
 * Records or replaces the participation score of one or more students in the active group for one week.
 */
public class ParticipationSetCommand extends Command {
    public static final String MESSAGE_USAGE = "participation set id/STUDENT_ID [id/STUDENT_ID]... w/WEEK s/SCORE";
    public static final String MESSAGE_RECORDED = "Recorded participation score %s for %s in Week %s.";
    public static final String MESSAGE_UPDATED = "Updated %s's participation score for Week %s: %s to %s.";
    public static final String MESSAGE_UNCHANGED =
            "%s's participation score for Week %s is already %s. Nothing changed.";
    public static final String MESSAGE_ABSENT_WARNING = " Warning: %s is marked %s for Week %d.";

    private static final Logger logger = LogsCenter.getLogger(ParticipationSetCommand.class);
    private final List<StudentId> studentIds;
    private final int week;
    private final int score;

    /**
     * Creates a command with a valid student ID, week from 1 to 13, and score from 0 to 5.
     */
    public ParticipationSetCommand(StudentId studentId, int week, int score) {
        this(List.of(requireNonNull(studentId)), week, score);
    }

    /**
     * Creates a command with a non-empty list of valid student IDs, a week from 1 to 13, and a score from 0 to 5.
     */
    public ParticipationSetCommand(List<StudentId> studentIds, int week, int score) {
        requireNonNull(studentIds);
        checkArgument(!studentIds.isEmpty(), "At least one student ID is required.");
        this.studentIds = List.copyOf(studentIds);
        WeeklyRecord.validateWeek(week);
        checkArgument(score >= 0 && score <= 5, "Participation score must be a whole number from 0 to 5.");
        this.week = week;
        this.score = score;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = getActiveGroup(model);
        List<Student> students = getStudents(group, studentIds);

        List<String> feedback = new ArrayList<>();
        for (Student student : students) {
            Optional<Integer> oldScore = student.getParticipationRecord(week);
            Student updated = student.withParticipationRecord(week, score);
            model.setStudent(group.getName(), updated);
            assert updated.getParticipationRecord(week).orElseThrow() == score
                    : "The new score must replace this week's value";
            feedback.add(createFeedback(student, oldScore) + warnIfAbsent(student));
        }
        logger.fine("Set participation for " + students.size() + " student(s) in Week " + week
                + " in " + group.getName());
        return new CommandResult(String.join("\n", feedback));
    }

    /**
     * Returns feedback that reports a new score, an unchanged score, or the old and new score.
     */
    private String createFeedback(Student student, Optional<Integer> oldScore) {
        if (oldScore.isEmpty()) {
            return String.format(MESSAGE_RECORDED, score, student.getName(), week);
        }
        if (oldScore.get() == score) {
            return String.format(MESSAGE_UNCHANGED, student.getName(), week, score);
        }
        return String.format(MESSAGE_UPDATED, student.getName(), week, oldScore.get(), score);
    }

    /**
     * Returns a warning if a positive score is given to a student marked absent or excused for the week.
     */
    private String warnIfAbsent(Student student) {
        return student.getAttendanceRecord(week).map(AttendanceStatus::fromValue)
                .filter(AttendanceStatus::isAbsence).filter(status -> score > 0)
                .map(status -> String.format(MESSAGE_ABSENT_WARNING, student.getName(), status.getCommandValue(), week))
                .orElse("");
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ParticipationSetCommand otherCommand
                && studentIds.equals(otherCommand.studentIds) && week == otherCommand.week
                && score == otherCommand.score;
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIds, week, score);
    }
}
