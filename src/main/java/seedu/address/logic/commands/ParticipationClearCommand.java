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
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.WeeklyRecord;

/**
 * Clears the participation scores of one or more students in the active group for a week, so that the week shows
 * Not recorded. This corrects scores entered for the wrong students or week.
 */
public class ParticipationClearCommand extends Command {
    public static final String MESSAGE_USAGE = "participation clear id/STUDENT_ID [id/STUDENT_ID]... w/WEEK";
    public static final String MESSAGE_CLEARED = "Cleared %s's participation score for Week %d: %d to Not recorded.";
    public static final String MESSAGE_NOT_RECORDED =
            "%s has no participation score recorded for Week %d. Nothing changed.";

    private static final Logger logger = LogsCenter.getLogger(ParticipationClearCommand.class);

    private final List<StudentId> studentIds;
    private final int week;

    /**
     * Creates a command to clear the given student's participation score for a valid teaching week.
     */
    public ParticipationClearCommand(StudentId studentId, int week) {
        this(List.of(requireNonNull(studentId)), week);
    }

    /**
     * Creates a command to clear the given students' participation scores for a valid teaching week.
     * The list must not be empty.
     */
    public ParticipationClearCommand(List<StudentId> studentIds, int week) {
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
     * Clears {@code student}'s score for the week if there is one, and returns the feedback line for that student.
     */
    private String clear(Model model, Group group, Student student) {
        Optional<Integer> oldScore = student.getParticipationRecord(week);
        if (oldScore.isEmpty()) {
            return String.format(MESSAGE_NOT_RECORDED, student.getName(), week);
        }

        Student cleared = student.withoutParticipationRecord(week);
        model.setStudent(group.getName(), cleared);
        assert cleared.getParticipationRecord(week).isEmpty() : "The week must no longer have a score";
        logger.fine("Cleared participation for Week " + week + " in " + group.getName());
        return String.format(MESSAGE_CLEARED, student.getName(), week, oldScore.get());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ParticipationClearCommand otherCommand
                && studentIds.equals(otherCommand.studentIds) && week == otherCommand.week;
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIds, week);
    }
}
