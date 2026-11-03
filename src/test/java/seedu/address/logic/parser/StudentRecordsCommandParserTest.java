package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.blankValueMessage;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.StudentRecordsCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.StudentId;

public class StudentRecordsCommandParserTest {
    private final StudentRecordsCommandParser parser = new StudentRecordsCommandParser();

    @Test
    public void parse_validId_ignoresCaseAndSurroundingSpaces() throws Exception {
        StudentRecordsCommand expected = new StudentRecordsCommand(new StudentId("A0123456X"));
        for (String input : List.of(" id/A0123456X", " id/a0123456x", "   id/A0123456X   ")) {
            assertEquals(expected, parser.parse(input));
        }
    }

    @Test
    public void parse_invalidInput_throwsParseException() {
        for (String input : List.of("", " A0123456X", " id/", " id/A0123456X id/A0234567Y", " id/A0123456X now",
                " id/A!", " n/Alice", " id/A0123456X n/Alice", " x/A1")) {
            assertThrows(ParseException.class, () -> parser.parse(input));
        }
        assertThrows(ParseException.class, "Missing required parameter(s): id/. Usage: "
                + StudentRecordsCommand.MESSAGE_USAGE, () -> parser.parse(""));
        assertThrows(ParseException.class, blankValueMessage("id/", StudentRecordsCommand.MESSAGE_USAGE), () ->
                parser.parse(" id/"));
        String extraTextMessage = String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "id/A0123456X", "now");
        assertThrows(ParseException.class, extraTextMessage, () -> parser.parse(" id/A0123456X now"));
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
