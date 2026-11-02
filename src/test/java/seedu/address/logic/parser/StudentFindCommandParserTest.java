package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalStudents.ALICE;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.TypicalGroups;

public class StudentFindCommandParserTest {
    private final StudentFindCommandParser parser = new StudentFindCommandParser();

    @Test
    public void parse_optionalFiltersInEitherOrder_matchesFragments() throws Exception {
        for (String input : List.of(" n/liCE", " id/0123", " n/ce ta id/x", " id/x n/ce ta")) {
            Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
            model.setActiveGroup(TypicalGroups.NAME_T01);
            parser.parse(input).execute(model);
            assertEquals(List.of(ALICE), model.getDisplayedStudentList());
        }
    }

    @Test
    public void parse_invalidFilters_rejectsInput() {
        for (String input : List.of("", " ", " n/", " id/ ", " n/Alice id/", " n/ id/A1",
                " n/Alice n/Ben", " id/A1 id/A2", " x/Alice", " Alice", " n/Alice w/1")) {
            assertThrows(ParseException.class, () -> parser.parse(input));
        }
        String idMessage = String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "id/", "n/aliceid/0123");
        assertThrows(ParseException.class, idMessage, () -> parser.parse(" n/aliceid/0123"));
        String nameMessage = String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "n/", "id/0123n/alice");
        assertThrows(ParseException.class, nameMessage, () -> parser.parse(" id/0123n/alice"));
        assertThrows(NullPointerException.class, () -> parser.parse(null));
        assertThrows(ParseException.class, StudentFindCommandParser.MESSAGE_MISSING_FILTER, () -> parser.parse(""));
        String blankMessage = String.format(StudentFindCommandParser.MESSAGE_BLANK_FILTER, "n/");
        assertThrows(ParseException.class, blankMessage, () -> parser.parse(" n/"));
    }

    @Test
    public void parse_textAfterIdFragment_reportsExtraText() {
        String message = String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "id/A1", "extra");
        assertThrows(ParseException.class, message, () -> parser.parse(" id/A1 extra"));
        assertThrows(ParseException.class, message, () -> parser.parse(" n/alice id/A1 extra"));
    }
}
