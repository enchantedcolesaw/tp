package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.blankValueMessage;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ParticipationListCommand;

public class ParticipationListCommandParserTest {
    private final ParticipationListCommandParser parser = new ParticipationListCommandParser();

    @Test
    public void parse_validWeeks_acceptsBoundariesAndLeadingZeros() {
        assertParseSuccess(parser, " w/01 ", new ParticipationListCommand(1));
        assertParseSuccess(parser, "w/13", new ParticipationListCommand(13));
    }

    @Test
    public void parse_missingOrInvalidWeek_reportsSpecificError() {
        assertParseFailure(parser, "", String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, "w/")
                        + " Usage: " + ParticipationListCommand.MESSAGE_USAGE);
        for (String week : new String[] {"0", "14", "-1", "+1", "1.5", "three", "9".repeat(1000)}) {
            assertParseFailure(parser, "w/" + week, TAssistParserUtil.MESSAGE_WEEK);
        }
        assertParseFailure(parser, "w/", blankValueMessage("w/", ParticipationListCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_malformedParameters_reportsSpecificError() {
        assertParseFailure(parser, "w/3 w/4", Messages.getErrorMessageForDuplicatePrefixes(new Prefix("w/")));
        assertParseFailure(parser, "w/3 s/2",
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "s/", "w/"));
        assertParseFailure(parser, "3", String.format(TAssistArgumentParser.MESSAGE_UNEXPECTED_TEXT, "3", "w/"));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
