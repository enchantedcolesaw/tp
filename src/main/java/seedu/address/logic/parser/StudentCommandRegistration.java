package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.commands.StudentAddCommand;
import seedu.address.logic.commands.StudentDeleteCommand;
import seedu.address.logic.commands.StudentEditCommand;
import seedu.address.logic.commands.StudentFindCommand;
import seedu.address.logic.commands.StudentListCommand;
import seedu.address.logic.commands.StudentRecordsCommand;
import seedu.address.logic.commands.StudentSortCommand;
import seedu.address.logic.help.HelpEntry;

/**
 * Registers student commands and their help entries before application startup.
 */
public final class StudentCommandRegistration {
    private StudentCommandRegistration() {}

    /**
     * Adds student parsers and help to the given registry.
     *
     * @param registry The registry assembled before constructing the logic component.
     */
    public static void registerCommands(CommandRegistry registry) {
        requireNonNull(registry);
        registry.register("student", "add", new StudentAddCommandParser(),
                new HelpEntry("student", StudentAddCommand.MESSAGE_USAGE,
                        "Add a student to the active group. Names: 1-60 letters, spaces, apostrophes, hyphens "
                                + "or periods, and s/o, d/o, a/l or a/p between two parts, as in Ravi s/o Kumar. "
                                + "Include at least one letter besides s/o, d/o, a/l or a/p. "
                                + "IDs: 1-20 letters and digits, at least one digit; unique per group, ignoring case.",
                        List.of("student add n/Alice Tan id/A0123456X", "student add n/Ben Lim id/A0234567Y")));
        registry.register("student", "edit", new StudentEditCommandParser(),
                new HelpEntry("student", StudentEditCommand.MESSAGE_USAGE,
                        "Correct a student's name, student ID, or both in the active group. Attendance, "
                                + "participation, and assignment records are kept. Include n/ or nid/ or both. "
                                + "IDs ignore case and must be unique in the group.",
                        List.of("student edit id/A0234567Y n/Benjamin Lim")));
        registry.register("student", "list", new StudentListCommandParser(),
                new HelpEntry("student", StudentListCommand.MESSAGE_USAGE,
                        "Clear search and sorting, and show all students in roster order. Takes no parameters.",
                        List.of("student list")));
        registry.register("student", "find", new StudentFindCommandParser(),
                new HelpEntry("student", StudentFindCommand.MESSAGE_USAGE,
                        "Find students in the active group by name or ID fragments, ignoring case. "
                                + "Include at least one filter; both filters must match when combined. "
                                + "Use student list to show all students again.",
                        List.of("student find n/alice", "student find id/0123", "student find n/alice id/0123")));
        registry.register("student", "sort", new StudentSortCommandParser(),
                new HelpEntry("student", StudentSortCommand.MESSAGE_USAGE,
                        "Sort the displayed students by name or ID, ascending and ignoring case. "
                                + "FIELD is name or id. Keep the current search; use student list to reset both.",
                        List.of("student sort by/name", "student sort by/id")));
        registry.register("student", "records", new StudentRecordsCommandParser(),
                new HelpEntry("student", StudentRecordsCommand.MESSAGE_USAGE,
                        "Show one student's attendance and participation for weeks 1 to 13, and their submission "
                                + "status and grade for every assignment, on the Records screen. IDs ignore case.",
                        List.of("student records id/A0123456X")));
        registry.register("student", "delete", new StudentDeleteCommandParser(),
                new HelpEntry("student", StudentDeleteCommand.MESSAGE_USAGE,
                        "Remove a student and their records from the active group after confirmation. "
                                + "Add f/yes to skip the prompt. IDs ignore case.",
                        List.of("student delete id/A0234567Y")));
    }
}
