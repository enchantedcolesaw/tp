package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.commands.AssignmentAddCommand;
import seedu.address.logic.commands.AssignmentDeleteCommand;
import seedu.address.logic.commands.AssignmentGradeCommand;
import seedu.address.logic.commands.AssignmentListCommand;
import seedu.address.logic.commands.AssignmentRenameCommand;
import seedu.address.logic.commands.AssignmentSubmitCommand;
import seedu.address.logic.help.HelpEntry;
import seedu.address.model.TAssistHistory;

/**
 * Registers assignment commands and their help entries before application startup.
 */
public final class AssignmentCommandRegistration {
    private AssignmentCommandRegistration() {}

    /**
     * Adds assignment parsers and help to the given registry.
     *
     * @param registry The registry assembled before constructing the logic component.
     */
    public static void registerCommands(CommandRegistry registry) {
        requireNonNull(registry);
        registry.register("assignment", "add", new AssignmentAddCommandParser(),
                new HelpEntry("assignment", AssignmentAddCommand.MESSAGE_USAGE,
                        "Create an assignment in the active group. Names: 1-60 letters, digits, spaces, hyphens, "
                                + "periods, colons or parentheses, with at least one letter or digit; "
                                + "unique per group, ignoring case.",
                        List.of("assignment add a/PS1")));
        registry.register("assignment", "submit", new AssignmentSubmitCommandParser(),
                new HelpEntry("assignment", AssignmentSubmitCommand.MESSAGE_USAGE,
                        "Record or replace the submission status of one or more students for an assignment. "
                                + "Repeat id/ to give several students the same status. Status is yes or no; "
                                + "no also clears any recorded grade.",
                        List.of("assignment submit a/PS1 id/A0123456X s/yes")));
        registry.register("assignment", "grade", new AssignmentGradeCommandParser(),
                new HelpEntry("assignment", AssignmentGradeCommand.MESSAGE_USAGE,
                        "Record or replace a whole-number grade from 0 to 100. Grading an assignment that is not "
                                + "yet submitted also records it as submitted.",
                        List.of("assignment grade a/PS1 id/A0123456X m/87")));
        registry.register("assignment", "list", new AssignmentListCommandParser(),
                new HelpEntry("assignment", AssignmentListCommand.MESSAGE_USAGE,
                        "Without a/, list the assignments of the active group. With a/, list every student's "
                                + "submission status and grade for that assignment. Names ignore case.",
                        List.of("assignment list", "assignment list a/PS1")));
        registry.register("assignment", "rename", new AssignmentRenameCommandParser(),
                new HelpEntry("assignment", AssignmentRenameCommand.MESSAGE_USAGE,
                        "Rename an assignment of the active group. Its submissions and grades stay with it. "
                                + "The new name must not match another assignment, ignoring case.",
                        List.of("assignment rename a/PS1 n/Problem Set 1")));
        registry.register("assignment", "delete", new AssignmentDeleteCommandParser(),
                new HelpEntry("assignment", AssignmentDeleteCommand.MESSAGE_USAGE,
                        "Delete an assignment of the active group with its submissions and grades, after asking "
                                + "to confirm. Add f/yes to skip the prompt. Undo can reverse it within your last "
                                + TAssistHistory.MAX_CHANGES + " changes.",
                        List.of("assignment delete a/Problem Set 1")));
    }
}
