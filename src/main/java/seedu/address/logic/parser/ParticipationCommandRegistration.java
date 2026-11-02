package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.commands.ParticipationClearCommand;
import seedu.address.logic.commands.ParticipationListCommand;
import seedu.address.logic.commands.ParticipationSetCommand;
import seedu.address.logic.help.HelpEntry;

/**
 * Registers participation commands and their help entries before application startup.
 */
public final class ParticipationCommandRegistration {
    private ParticipationCommandRegistration() {}

    /**
     * Adds participation parsers and help to the given registry.
     *
     * @param registry The registry assembled before constructing the logic component.
     */
    public static void registerCommands(CommandRegistry registry) {
        requireNonNull(registry);
        registry.register("participation", "set", new ParticipationSetCommandParser(),
                new HelpEntry("participation", ParticipationSetCommand.MESSAGE_USAGE,
                        "Set a whole-number score from 0 to 5 for one or more students in the active group, for a "
                                + "week from 1 to 13. Repeat id/ to give several students the same score. "
                                + "IDs ignore case. "
                                + "Setting the same student and week again replaces the score.",
                        List.of("participation set id/A0123456X w/3 s/2")));
        registry.register("participation", "list", new ParticipationListCommandParser(),
                new HelpEntry("participation", ParticipationListCommand.MESSAGE_USAGE,
                        "List every student in the active group with their score for a week from 1 to 13. "
                                + "Missing scores show Not recorded.",
                        List.of("participation list w/3")));
        registry.register("participation", "clear", new ParticipationClearCommandParser(),
                new HelpEntry("participation", ParticipationClearCommand.MESSAGE_USAGE,
                        "Remove a student's score for a week from 1 to 13, so that the week shows Not recorded "
                                + "again. Use it to correct a score entered for the wrong student or week, since 0 "
                                + "is a real score. IDs ignore case. Repeat id/ to clear several students at once.",
                        List.of("participation clear id/A0123456X w/3")));
    }
}
