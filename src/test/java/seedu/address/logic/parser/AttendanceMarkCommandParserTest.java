package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.blankValueMessage;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BEN;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AttendanceMarkCommand;
import seedu.address.model.student.AttendanceStatus;
import seedu.address.model.student.StudentId;

public class AttendanceMarkCommandParserTest {
    private final AttendanceMarkCommandParser parser = new AttendanceMarkCommandParser();

    @Test
    public void parse_validArguments_acceptsAnyOrderAndNormalizesValues() {
        assertParseSuccess(parser, " s/ Present   w/ 3 id/ a0123456x ",
                new AttendanceMarkCommand(ALICE.getStudentId(), 3, AttendanceStatus.PRESENT));
        assertParseSuccess(parser, "id/A0123456X w/13 s/absent",
                new AttendanceMarkCommand(ALICE.getStudentId(), 13, AttendanceStatus.ABSENT));
    }

    @Test
    public void parse_repeatedStudentId_marksEveryStudentOnceInGivenOrder() {
        assertParseSuccess(parser, "id/A0123456X id/a0234567y w/3 s/present",
                new AttendanceMarkCommand(List.of(ALICE.getStudentId(), BEN.getStudentId()), 3,
                        AttendanceStatus.PRESENT));
        assertParseSuccess(parser, "id/A0234567Y w/3 id/A0123456X s/absent id/a0234567y",
                new AttendanceMarkCommand(List.of(BEN.getStudentId(), ALICE.getStudentId()), 3,
                        AttendanceStatus.ABSENT));
    }

    @Test
    public void parse_repeatedStudentIdWithBadValue_reportsThatValue() {
        assertParseFailure(parser, "id/A0123456X id/ w/3 s/present",
                blankValueMessage("id/", AttendanceMarkCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "id/A0123456X id/invalid! w/3 s/present",
                String.format(TAssistParserUtil.MESSAGE_INVALID_STUDENT_ID_IN_LIST, "invalid!"));
        assertParseFailure(parser, "id/A0123456X later id/A0234567Y w/3 s/present",
                String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "id/A0123456X", "later"));
        assertParseFailure(parser, "id/A0123456X id/A0234567Yw/3 s/present",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "w/", "id/A0234567Yw/3"));
    }

    @Test
    public void parse_missingParameters_reportsMissingPrefixes() {
        String[][] cases = {
            {"", "id/ w/ s/"},
            {"w/3 s/present", "id/"},
            {"id/A0123456X s/present", "w/"},
            {"id/A0123456X w/3", "s/"}
        };
        for (String[] testCase : cases) {
            assertParseFailure(parser, testCase[0],
                    String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, testCase[1])
                        + " Usage: " + AttendanceMarkCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_missingSpaceBeforePrefix_reportsMissingSpace() {
        assertParseFailure(parser, "id/A0123456Xw/3 s/present",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "w/", "id/A0123456Xw/3"));
        assertParseFailure(parser, "id/A0123456X w/3s/present",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "s/", "w/3s/present"));
    }

    @Test
    public void parse_mistypedStatusPrefix_reportsUnknownPrefixWithoutHint() {
        assertParseFailure(parser, "id/A0123456X w/3 status/present",
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "status/", "id/ w/ s/"));
    }

    @Test
    public void parse_invalidValues_reportsValueConstraints() {
        assertParseFailure(parser, "id/ w/3 s/present", blankValueMessage("id/", AttendanceMarkCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "id/invalid! w/3 s/present", StudentId.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "id/A0123456X w/ s/present",
                blankValueMessage("w/", AttendanceMarkCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "id/A0123456X w/14 s/present", AttendanceCommandParserUtil.MESSAGE_INVALID_WEEK);
        assertParseFailure(parser, "id/A0123456X w/3 s/", blankValueMessage("s/", AttendanceMarkCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "id/A0123456X w/3 s/unknown", AttendanceStatus.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedOrUnknownPrefixes_reportsFormatError() {
        String validArguments = "id/A0123456X w/3 s/present";
        for (String prefix : new String[] {"w/", "s/"}) {
            assertParseFailure(parser, validArguments + " " + prefix + "value",
                    Messages.getErrorMessageForDuplicatePrefixes(new Prefix(prefix)));
        }
        assertParseFailure(parser, validArguments + " x/value",
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "x/", "id/ w/ s/"));
        assertParseFailure(parser, "extra " + validArguments,
                String.format(TAssistArgumentParser.MESSAGE_UNEXPECTED_TEXT, "extra", "id/ w/ s/"));
    }

    @Test
    public void parse_forgottenPrefixBeforeExtraText_reportsMissingPrefix() {
        assertParseFailure(parser, "id/A0123456X w/3 present",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, "s/")
                        + " Usage: " + AttendanceMarkCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_extraTextAfterValue_reportsExtraText() {
        assertParseFailure(parser, "id/A0123456X w/3 s/present today",
                String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "s/present", "today"));
        assertParseFailure(parser, "id/A0123456X w/3 later s/present",
                String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "w/3", "later"));
    }
}
