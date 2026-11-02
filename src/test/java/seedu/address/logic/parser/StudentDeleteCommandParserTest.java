package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.blankValueMessage;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalStudents.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.StudentDeleteCommand;
import seedu.address.model.student.StudentId;

public class StudentDeleteCommandParserTest {
    private static final String MISSING_ID = String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, "id/")
            + " Usage: " + StudentDeleteCommand.MESSAGE_USAGE;

    private final StudentDeleteCommandParser parser = new StudentDeleteCommandParser();

    @Test
    public void parse_validId_normalizesCaseAndSpaces() {
        assertParseSuccess(parser, " id/ a0123456x ", new StudentDeleteCommand(ALICE.getStudentId()));
        assertParseSuccess(parser, "id/a0123456x f/yes", new StudentDeleteCommand(ALICE.getStudentId(), true));
    }

    @Test
    public void parse_invalidFastDeleteOption_reportsHowToSkipConfirmation() {
        assertParseFailure(parser, "id/A1 f/no", StudentDeleteCommandParser.MESSAGE_INVALID_FAST_DELETE_OPTION);
        assertParseFailure(parser, "id/A1 f/", StudentDeleteCommandParser.MESSAGE_INVALID_FAST_DELETE_OPTION);
    }

    @Test
    public void parse_missingOrInvalidId_reportsSpecificError() {
        assertParseFailure(parser, "", MISSING_ID);
        assertParseFailure(parser, "id/", blankValueMessage("id/", StudentDeleteCommand.MESSAGE_USAGE));
        for (String id : new String[] {"ABC", "A1!", "1".repeat(21)}) {
            assertParseFailure(parser, "id/" + id, StudentId.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_malformedParameters_reportsSpecificError() {
        assertParseFailure(parser, "id/A1 id/A2",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("id/")));
        assertParseFailure(parser, "id/A1 n/Alice",
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "n/", "id/ f/"));
        assertParseFailure(parser, "A1",
                String.format(TAssistArgumentParser.MESSAGE_UNEXPECTED_TEXT, "A1", "id/ f/"));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_extraTextAfterValue_reportsExtraText() {
        assertParseFailure(parser, "id/A0123456X now",
                String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "id/A0123456X", "now"));
        assertParseFailure(parser, "id/A0123456X f/yes now",
                String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "f/yes", "now"));
    }

    @Test
    public void parse_missingSpaceBeforeId_reportsMissingSpace() {
        assertParseFailure(parser, "f/yesid/A0123456X",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "id/", "f/yesid/A0123456X"));
    }

    @Test
    public void parse_missingSpaceBeforeFastOption_reportsMissingSpace() {
        assertParseFailure(parser, " id/A0123456Xf/yes",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "f/", "id/A0123456Xf/yes"));
        assertParseSuccess(parser, " id/A0123456X f/yes", new StudentDeleteCommand(new StudentId("A0123456X"), true));
    }

    @Test
    public void parse_missingIdBeforeExtraText_reportsMissingParameter() {
        assertParseFailure(parser, "f/yes A0123456X", MISSING_ID);
    }
}
