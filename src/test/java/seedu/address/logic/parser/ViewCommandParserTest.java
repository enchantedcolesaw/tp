package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.WorkspaceView;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;

public class ViewCommandParserTest {
    private final TAssistParser parser = new TAssistParser();

    @Test
    public void parseCommand_allScreens_returnsRequestedView() throws Exception {
        for (WorkspaceView view : WorkspaceView.values()) {
            assertEquals(view, parser.parseCommand("view " + view.getKeyword())
                    .execute(new ModelManager()).getView().orElseThrow());
        }
    }

    @Test
    public void parseCommand_mixedCaseAndWhitespace_returnsRequestedView() throws Exception {
        assertEquals(WorkspaceView.STORAGE, parser.parseCommand("  view   StOrAgE  ")
                .execute(new ModelManager()).getView().orElseThrow());
    }

    @Test
    public void parseCommand_singularStudent_returnsStudentsView() throws Exception {
        for (String name : List.of("student", "STUDENT")) {
            assertEquals(WorkspaceView.STUDENTS, parser.parseCommand("view\t" + name + "  ")
                    .execute(new ModelManager()).getView().orElseThrow());
        }
    }

    @Test
    public void parseCommand_singularRecord_returnsRecordsView() throws Exception {
        for (String name : List.of("record", "Record")) {
            assertEquals(WorkspaceView.RECORDS, parser.parseCommand("view " + name)
                    .execute(new ModelManager()).getView().orElseThrow());
        }
    }

    @Test
    public void parseCommand_missingScreen_namesTheMissingScreen() {
        for (String input : List.of("view", "view   ", "view \t")) {
            assertThrows(ParseException.class, ViewCommand.MESSAGE_MISSING_SCREEN, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void parseCommand_multipleWords_namesTheExtraText() {
        assertThrows(ParseException.class, String.format(ViewCommand.MESSAGE_EXTRA_TEXT, "storage", "now"), () ->
                parser.parseCommand("view storage now"));
        assertThrows(ParseException.class, String.format(ViewCommand.MESSAGE_EXTRA_TEXT, "students", "help"), () ->
                parser.parseCommand("view students\thelp"));
        assertThrows(ParseException.class, String.format(ViewCommand.MESSAGE_EXTRA_TEXT, "student", "student x"), () ->
                parser.parseCommand("view student student x"));
    }

    @Test
    public void parseCommand_unknownScreen_throwsParseException() {
        for (String screen : List.of("unknown", "groups", "Attendance", "participation", "assignment")) {
            String expected = String.format(ViewCommand.MESSAGE_UNKNOWN_SCREEN, screen);
            assertThrows(ParseException.class, expected, () -> parser.parseCommand("view " + screen));
        }
    }
}
