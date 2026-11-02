package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.blankValueMessage;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAssignments.NAME_PS1;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AssignmentRenameCommand;
import seedu.address.model.assignment.AssignmentName;

public class AssignmentRenameCommandParserTest {
    private final AssignmentRenameCommandParser parser = new AssignmentRenameCommandParser();

    /** Returns the error for missing {@code prefixes}, followed by the usage of the command. */
    private static String missingParameters(String prefixes) {
        return String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, prefixes)
                + " Usage: " + AssignmentRenameCommand.MESSAGE_USAGE;
    }

    @Test
    public void parse_validNames_returnsRenameCommand() {
        AssignmentRenameCommand expected = new AssignmentRenameCommand(NAME_PS1, new AssignmentName("Problem Set 1"));
        assertParseSuccess(parser, " a/ PS1 n/ Problem   Set 1 ", expected);
        assertParseSuccess(parser, " n/Problem Set 1 a/ps1", expected);
    }

    @Test
    public void parse_invalidName_reportsConstraints() {
        assertParseFailure(parser, "a/PS_1 n/PS2", AssignmentName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "a/PS1 n/PS_2", AssignmentName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "a/PS1 n/", blankValueMessage("n/", AssignmentRenameCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "a/PS1 n/" + "a".repeat(61), AssignmentName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_malformedParameters_reportsSpecificError() {
        assertParseFailure(parser, "", missingParameters("a/ n/"));
        assertParseFailure(parser, "n/PS2", missingParameters("a/"));
        assertParseFailure(parser, "a/PS1", missingParameters("n/"));
        assertParseFailure(parser, "a/PS1 n/PS2 n/PS3",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("n/")));
        assertParseFailure(parser, "a/PS1 n/PS2 g/T01",
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "g/", "a/ n/"));
        assertParseFailure(parser, "PS1 n/PS2",
                String.format(TAssistArgumentParser.MESSAGE_UNEXPECTED_TEXT, "PS1", "a/ n/"));
    }

    @Test
    public void parse_missingSpaceBeforePrefix_reportsMissingSpace() {
        assertParseFailure(parser, "a/PS1n/PS2",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "n/", "a/PS1n/PS2"));
        assertParseFailure(parser, "n/PS2a/PS1",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "a/", "n/PS2a/PS1"));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
