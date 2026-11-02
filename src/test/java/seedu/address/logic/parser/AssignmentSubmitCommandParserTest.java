package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.blankValueMessage;
import static seedu.address.testutil.TypicalAssignments.NAME_PS1;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BEN;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AssignmentSubmitCommand;

public class AssignmentSubmitCommandParserTest {
    private final AssignmentSubmitCommandParser parser = new AssignmentSubmitCommandParser();

    @Test
    public void parse_validStatus_success() {
        assertParseSuccess(parser, "a/PS1 id/A0123456X s/yes",
                new AssignmentSubmitCommand(NAME_PS1, ALICE.getStudentId(), true));
        assertParseSuccess(parser, "s/NO id/a0123456x a/ ps1 ",
                new AssignmentSubmitCommand(NAME_PS1, ALICE.getStudentId(), false));
    }

    @Test
    public void parse_repeatedStudentId_updatesEveryStudentOnceInGivenOrder() {
        assertParseSuccess(parser, "a/PS1 id/A0234567Y id/a0123456x s/yes id/a0234567y",
                new AssignmentSubmitCommand(NAME_PS1, List.of(BEN.getStudentId(), ALICE.getStudentId()), true));
    }

    @Test
    public void parse_repeatedStudentIdWithBadValue_reportsThatValue() {
        assertParseFailure(parser, "a/PS1 id/A0123456X id/ s/yes",
                blankValueMessage("id/", AssignmentSubmitCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "a/PS1 id/A0123456X id/invalid! s/yes",
                String.format(TAssistParserUtil.MESSAGE_INVALID_STUDENT_ID_IN_LIST, "invalid!"));
        assertParseFailure(parser, "a/PS1 id/A0123456X id/A0234567Y later s/yes",
                String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "id/A0234567Y", "later"));
        assertParseFailure(parser, "a/PS1 id/A0123456X id/A0234567Ys/yes",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "s/", "id/A0234567Ys/yes"));
    }

    @Test
    public void parse_invalidStatus_reportsFeatureError() {
        assertParseFailure(parser, "a/PS1 id/A0123456X s/maybe", TAssistParserUtil.MESSAGE_SUBMISSION_STATUS);
    }

    @Test
    public void parse_missingDuplicateAndUnknownParameters_reportErrors() {
        assertParseFailure(parser, "a/PS1 id/A0123456X",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, "s/")
                        + " Usage: " + AssignmentSubmitCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "a/PS1 id/A0123456X s/yes s/no",
                "Repeated parameter(s): s/. Include each only once.");
        assertParseFailure(parser, "a/PS1 id/A0123456X s/yes n/no",
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "n/", "a/ id/ s/"));
    }
}
