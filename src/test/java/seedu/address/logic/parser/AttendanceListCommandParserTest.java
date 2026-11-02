package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.blankValueMessage;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AttendanceListCommand;

public class AttendanceListCommandParserTest {
    private final AttendanceListCommandParser parser = new AttendanceListCommandParser();

    @Test
    public void parse_validWeeks_acceptsBoundariesAndSpaces() {
        assertParseSuccess(parser, " w/ 1 ", new AttendanceListCommand(1));
        assertParseSuccess(parser, "w/13", new AttendanceListCommand(13));
    }

    @Test
    public void parse_missingWeek_reportsRequiredPrefix() {
        assertParseFailure(parser, "", String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, "w/")
                        + " Usage: " + AttendanceListCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_invalidWeek_reportsAttendanceMessage() {
        for (String week : new String[] {"0", "14", "-1", "3.5", "three", "9".repeat(1000)}) {
            assertParseFailure(parser, "w/" + week, AttendanceCommandParserUtil.MESSAGE_INVALID_WEEK);
        }
        assertParseFailure(parser, "w/ ", blankValueMessage("w/", AttendanceListCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_extraOrRepeatedParameters_reportsFormatError() {
        assertParseFailure(parser, "w/3 w/4", Messages.getErrorMessageForDuplicatePrefixes(new Prefix("w/")));
        assertParseFailure(parser, "w/3 s/present",
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "s/", "w/"));
        assertParseFailure(parser, "3", String.format(TAssistArgumentParser.MESSAGE_UNEXPECTED_TEXT, "3", "w/"));
    }

    @Test
    public void parse_extraTextAfterWeek_reportsExtraText() {
        assertParseFailure(parser, "w/3 please",
                String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "w/3", "please"));
    }
}
