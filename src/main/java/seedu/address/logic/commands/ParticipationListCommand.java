package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;
import seedu.address.model.student.Student;
import seedu.address.model.student.WeeklyRecord;
import seedu.address.model.student.WeeklyRecordEntry;

/**
 * Lists participation scores, including missing records, for the active group in one week.
 */
public class ParticipationListCommand extends Command {
    public static final String MESSAGE_USAGE = "participation list w/WEEK";
    public static final String MESSAGE_NO_STUDENTS = "No students in %s.";
    public static final String MESSAGE_HEADER = "Participation for %s, Week %d: %d recorded, %d not recorded";

    private final int week;

    /**
     * Creates a listing command for a week from 1 to 13.
     */
    public ParticipationListCommand(int week) {
        WeeklyRecord.validateWeek(week);
        this.week = week;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = getActiveGroup(model);
        List<WeeklyRecordEntry<Integer>> entries = group.getWeeklyRecordEntries(week, Student::getParticipationRecords);
        if (entries.isEmpty()) {
            return CommandResult.forRecordListing(String.format(MESSAGE_NO_STUDENTS, group.getName()));
        }

        long recorded = entries.stream().filter(entry -> entry.getRecord().isPresent()).count();
        StringBuilder feedback = new StringBuilder(String.format(MESSAGE_HEADER, group.getName(), week, recorded,
                entries.size() - recorded));
        for (WeeklyRecordEntry<Integer> entry : entries) {
            feedback.append('\n').append(entry.getStudent().getName()).append(" | ")
                    .append(entry.getStudent().getStudentId()).append(" | ")
                    .append(entry.formatValueOrNotRecorded(String::valueOf));
        }
        return CommandResult.forRecordListing(feedback.toString());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ParticipationListCommand otherCommand && week == otherCommand.week;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(week);
    }
}
