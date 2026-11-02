package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.commands.AttendanceClearCommand;
import seedu.address.logic.commands.AttendanceListCommand;
import seedu.address.logic.commands.AttendanceMarkCommand;
import seedu.address.logic.help.HelpEntry;

/**
 * Registers attendance commands and their offline help before application startup.
 */
public final class AttendanceCommandRegistration {
    private AttendanceCommandRegistration() {}

    /**
     * Adds attendance parsers and help to the given registry.
     *
     * @param registry The registry assembled before constructing the logic component.
     */
    public static void registerCommands(CommandRegistry registry) {
        requireNonNull(registry);
        registry.register("attendance", "mark", new AttendanceMarkCommandParser(),
                new HelpEntry("attendance", AttendanceMarkCommand.MESSAGE_USAGE,
                        "Mark students present, absent, late or excused for a week from 1 to 13. "
                                + "Late means arrived late; excused means absent with an accepted reason. "
                                + "Counts keep all four statuses separate. "
                                + "Repeat id/ to mark several students at once. "
                                + "IDs and statuses ignore case. "
                                + "Marking the same student and week again replaces the earlier mark. "
                                + "A positive participation score with absent or excused triggers a warning. "
                                + "On the Students screen, clicking a student's row fills in the command "
                                + "up to w/ for you to complete.",
                        List.of("attendance mark id/A0123456X w/3 s/present")));
        registry.register("attendance", "list", new AttendanceListCommandParser(),
                new HelpEntry("attendance", AttendanceListCommand.MESSAGE_USAGE,
                        "List the active group's attendance for a week from 1 to 13. Students without a mark show "
                                + "Not recorded. A group with no students shows a message saying so.",
                        List.of("attendance list w/3")));
        registry.register("attendance", "clear", new AttendanceClearCommandParser(),
                new HelpEntry("attendance", AttendanceClearCommand.MESSAGE_USAGE,
                        "Remove a student's attendance mark for a week from 1 to 13, so that the week shows "
                                + "Not recorded again. Use it to correct a mark entered for the wrong student or week. "
                                + "IDs ignore case. Repeat id/ to clear several students at once.",
                        List.of("attendance clear id/A0123456X w/3")));
    }
}
