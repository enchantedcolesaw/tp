package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.blankValueMessage;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BEN;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ParticipationClearCommand;
import seedu.address.model.student.StudentId;

public class ParticipationClearCommandParserTest {
    private final ParticipationClearCommandParser parser = new ParticipationClearCommandParser();

    @Test
    public void parse_validArguments_acceptsAnyOrderAndNormalizesValues() {
        assertParseSuccess(parser, "  w/ 03   id/ a0123456x ", new ParticipationClearCommand(ALICE.getStudentId(), 3));
        assertParseSuccess(parser, "id/A0123456X w/1", new ParticipationClearCommand(ALICE.getStudentId(), 1));
        assertParseSuccess(parser, "id/A0123456X w/13", new ParticipationClearCommand(ALICE.getStudentId(), 13));
    }

    @Test
    public void parse_repeatedStudentIds_keepsFirstOccurrenceOrder() {
        assertParseSuccess(parser, "id/A0234567Y id/a0123456x id/A0234567Y w/3",
                new ParticipationClearCommand(List.of(BEN.getStudentId(), ALICE.getStudentId()), 3));
        assertParseFailure(parser, "id/A0123456X id/A1! w/3",
                String.format(TAssistParserUtil.MESSAGE_INVALID_STUDENT_ID_IN_LIST, "A1!"));
    }

    @Test
    public void parse_missingParameters_reportsMissingPrefixes() {
        String[][] cases = {{"", "id/ w/"}, {"w/3", "id/"}, {"id/A0123456X", "w/"}};
        for (String[] testCase : cases) {
            assertParseFailure(parser, testCase[0],
                    String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, testCase[1])
                        + " Usage: " + ParticipationClearCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_invalidValues_reportsValueConstraints() {
        for (String week : new String[] {"0", "14", "-1", "1.5", "three", "9".repeat(1000)}) {
            assertParseFailure(parser, "id/A0123456X w/" + week, TAssistParserUtil.MESSAGE_WEEK);
        }
        for (String id : new String[] {"ABC", "A1!", "1".repeat(21)}) {
            assertParseFailure(parser, "id/" + id + " w/3", StudentId.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, "id/A0123456X w/", blankValueMessage("w/", ParticipationClearCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "id/ w/3", blankValueMessage("id/", ParticipationClearCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_repeatedOrUnknownPrefixes_reportsFormatError() {
        String validArguments = "id/A0123456X w/3";
        assertParseFailure(parser, validArguments + " w/4",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("w/")));
        // A score copied from participation set is not accepted, so a typo never clears the wrong record.
        assertParseFailure(parser, validArguments + " s/2",
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "s/", "id/ w/"));
        assertParseFailure(parser, "extra " + validArguments,
                String.format(TAssistArgumentParser.MESSAGE_UNEXPECTED_TEXT, "extra", "id/ w/"));
    }

    @Test
    public void parse_forgottenPrefixBeforeExtraText_reportsMissingPrefix() {
        assertParseFailure(parser, "id/A0123456X 3",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, "w/")
                        + " Usage: " + ParticipationClearCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_extraTextAfterValue_reportsExtraText() {
        assertParseFailure(parser, "id/A0123456X w/3 now",
                String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "w/3", "now"));
        assertParseFailure(parser, "id/A0123456X later w/3",
                String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "id/A0123456X", "later"));
    }
}
