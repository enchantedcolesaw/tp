package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.blankValueMessage;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAssignments.NAME_PS1;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AssignmentAddCommand;
import seedu.address.model.assignment.AssignmentName;

public class AssignmentAddCommandParserTest {
    private final AssignmentAddCommandParser parser = new AssignmentAddCommandParser();

    @Test
    public void parse_validName_success() {
        assertParseSuccess(parser, "a/PS1", new AssignmentAddCommand(NAME_PS1));
        assertParseSuccess(parser, "  a/  PS1  ", new AssignmentAddCommand(NAME_PS1));
        assertParseSuccess(parser, "a/Problem   Set-1", new AssignmentAddCommand(new AssignmentName("Problem Set-1")));
        assertParseSuccess(parser, "a/PS1.5", new AssignmentAddCommand(new AssignmentName("PS1.5")));
        assertParseSuccess(parser, "a/Lab 2 (Graphs)", new AssignmentAddCommand(new AssignmentName("Lab 2 (Graphs)")));
        assertParseSuccess(parser, "a/Kattis: Week 3", new AssignmentAddCommand(new AssignmentName("Kattis: Week 3")));
        assertParseSuccess(parser, "a/" + "a".repeat(AssignmentName.MAX_LENGTH),
                new AssignmentAddCommand(new AssignmentName("a".repeat(AssignmentName.MAX_LENGTH))));
    }

    @Test
    public void parse_invalidName_reportsConstraints() {
        assertParseFailure(parser, "a/   ", blankValueMessage("a/", AssignmentAddCommand.MESSAGE_USAGE));
        for (String name : new String[] {"-", "PS1_A", "PS1/PS2", "PS1!", "PS1,2", "(...)", "().:",
                "a".repeat(61)}) {
            assertParseFailure(parser, "a/" + name, AssignmentName.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_malformedParameters_reportsSpecificError() {
        assertParseFailure(parser, "", String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, "a/")
                        + " Usage: " + AssignmentAddCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "a/PS1 a/PS2", Messages.getErrorMessageForDuplicatePrefixes(new Prefix("a/")));
        assertParseFailure(parser, "a/PS1 n/PS2",
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "n/", "a/"));
        assertParseFailure(parser, "PS1", String.format(TAssistArgumentParser.MESSAGE_UNEXPECTED_TEXT, "PS1", "a/"));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
