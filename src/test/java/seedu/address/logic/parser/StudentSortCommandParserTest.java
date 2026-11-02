package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.blankValueMessage;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BEN;
import static seedu.address.testutil.TypicalStudents.CARL;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.StudentSortCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.TypicalGroups;

public class StudentSortCommandParserTest {
    private final StudentSortCommandParser parser = new StudentSortCommandParser();

    @Test
    public void parse_supportedFields_ignoresCase() throws Exception {
        for (String input : List.of(" by/name", " by/NaMe", " by/ID", " by/id ")) {
            Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
            model.setActiveGroup(TypicalGroups.NAME_T01);
            model.removeStudent(TypicalGroups.NAME_T01, ALICE.getStudentId());
            model.addStudent(TypicalGroups.NAME_T01, ALICE);
            parser.parse(input).execute(model);
            assertEquals(List.of(ALICE, BEN, CARL), model.getDisplayedStudentList());
        }
    }

    @Test
    public void parse_invalidFields_rejectsInput() {
        for (String input : List.of("", " ", " name", " by/", " by/grade", " by/name by/id",
                " by/name n/Alice", " x/name", " by/name descending")) {
            assertThrows(ParseException.class, () -> parser.parse(input));
        }
        String invalidFieldMessage = StudentSortCommandParser.MESSAGE_INVALID_FIELD;
        assertThrows(ParseException.class, invalidFieldMessage, () -> parser.parse(" by/grade"));
        assertThrows(ParseException.class, blankValueMessage("by/", StudentSortCommand.MESSAGE_USAGE), () ->
                parser.parse(" by/"));
        String extraTextMessage = String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "by/id", "now");
        assertThrows(ParseException.class, extraTextMessage, () -> parser.parse(" by/id now"));
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
