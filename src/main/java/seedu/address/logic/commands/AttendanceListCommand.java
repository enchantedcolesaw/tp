package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;
import seedu.address.model.student.AttendanceStatus;
import seedu.address.model.student.Student;
import seedu.address.model.student.WeeklyRecord;
import seedu.address.model.student.WeeklyRecordEntry;

/**
 * Lists every student's attendance, including missing records, for one week in the active tutorial group.
 */
public class AttendanceListCommand extends Command {
    public static final String MESSAGE_USAGE = "attendance list w/WEEK";
    public static final String MESSAGE_HEADER =
            "Attendance for %s, Week %d: %d present, %d absent, %d late, %d excused, %d not recorded";
    public static final String MESSAGE_NO_STUDENTS = "No students in %s.";

    private final int week;

    /**
     * Creates a listing command for a valid teaching week.
     */
    public AttendanceListCommand(int week) {
        WeeklyRecord.validateWeek(week);
        this.week = week;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = getActiveGroup(model);
        List<WeeklyRecordEntry<String>> entries = group.getWeeklyRecordEntries(week, Student::getAttendanceRecords);
        if (entries.isEmpty()) {
            return CommandResult.forRecordListing(String.format(MESSAGE_NO_STUDENTS, group.getName()));
        }

        long present = countStatus(entries, AttendanceStatus.PRESENT);
        long absent = countStatus(entries, AttendanceStatus.ABSENT);
        long late = countStatus(entries, AttendanceStatus.LATE);
        long excused = countStatus(entries, AttendanceStatus.EXCUSED);
        long notRecorded = entries.stream().filter(entry -> entry.getRecord().isEmpty()).count();
        String header = String.format(MESSAGE_HEADER, group.getName(), week, present, absent, late, excused,
                notRecorded);
        String rows = entries.stream().map(AttendanceListCommand::formatRow).collect(Collectors.joining("\n"));
        return CommandResult.forRecordListing(header + "\n" + rows);
    }

    private static long countStatus(List<WeeklyRecordEntry<String>> entries, AttendanceStatus status) {
        return entries.stream().filter(entry -> entry.getRecord().filter(status.name()::equals).isPresent()).count();
    }

    private static String formatRow(WeeklyRecordEntry<String> entry) {
        Student student = entry.getStudent();
        String value = entry.formatValueOrNotRecorded(AttendanceStatus::formatStoredValue);
        return student.getName() + " | " + student.getStudentId() + " | " + value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AttendanceListCommand otherCommand && week == otherCommand.week;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(week);
    }
}
