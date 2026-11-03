package seedu.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.WorkspaceView;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.logic.help.HelpEntry;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.TAssist;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupName;
import seedu.address.model.student.StudentId;

/**
 * Checks the help of the running app: that it covers every command, that its examples work when typed, and that
 * the User Guide shows the same formats and examples.
 */
public class HelpIntegrationTest {
    /** Every command format, in the order the Help screen shows them. */
    private static final List<String> EXPECTED_FORMATS = List.of(
            "view SCREEN", "help [TOPIC]", "undo", "redo", "exit",
            "group add g/GROUP_NAME", "group list", "group use g/GROUP_NAME", "group export",
            "group rename g/GROUP_NAME n/NEW_NAME", "group delete g/GROUP_NAME [f/yes]",
            "student add n/NAME id/STUDENT_ID", "student edit id/STUDENT_ID [n/NEW_NAME] [nid/NEW_ID]",
            "student list",
            "student find [n/NAME_FRAGMENT] [id/ID_FRAGMENT]", "student sort by/FIELD",
            "student records id/STUDENT_ID", "student delete id/STUDENT_ID [f/yes]",
            "attendance mark id/STUDENT_ID [id/STUDENT_ID]... w/WEEK s/STATUS", "attendance list w/WEEK",
            "attendance clear id/STUDENT_ID [id/STUDENT_ID]... w/WEEK",
            "participation set id/STUDENT_ID [id/STUDENT_ID]... w/WEEK s/SCORE", "participation list w/WEEK",
            "participation clear id/STUDENT_ID [id/STUDENT_ID]... w/WEEK",
            "assignment add a/ASSIGNMENT_NAME",
            "assignment submit a/ASSIGNMENT_NAME id/STUDENT_ID [id/STUDENT_ID]... s/STATUS",
            "assignment grade a/ASSIGNMENT_NAME id/STUDENT_ID m/GRADE", "assignment list [a/ASSIGNMENT_NAME]",
            "assignment rename a/ASSIGNMENT_NAME n/NEW_NAME", "assignment delete a/ASSIGNMENT_NAME [f/yes]");

    private static final Path USER_GUIDE = Path.of("docs", "UserGuide.md");
    private static final String COMMAND_SUMMARY_HEADING = "## Command summary";
    /** An example in the command summary table, such as {@code e.g., `group add g/T01`}. */
    private static final Pattern SUMMARY_EXAMPLE = Pattern.compile("e\\.g\\., `([^`]+)`");

    @TempDir
    public Path temporaryFolder;

    private MainApp app;

    @BeforeEach
    public void setUp() throws Exception {
        app = new MainApp(temporaryFolder.resolve("prefs.json"), temporaryFolder.resolve("tassist.json"));
        app.init();
    }

    @Test
    public void getHelpEntries_listsEveryCommandInHelpOrder() {
        assertEquals(EXPECTED_FORMATS, app.logic.getHelpEntries().stream().map(HelpEntry::getCommandFormat).toList());
    }

    @Test
    public void execute_singularAndPluralNavigation_preservesDataAndSavedFile() throws Exception {
        app.logic.execute("group add g/T01");
        app.logic.execute("student add n/Alice Tan id/A1");
        TAssist previousData = new TAssist(app.model.getTAssist());
        Path dataFile = temporaryFolder.resolve("tassist.json");
        String savedFile = Files.readString(dataFile);

        for (String name : List.of("student", "students")) {
            CommandResult viewResult = app.logic.execute("view " + name);
            assertEquals(new CommandResult("Showing students.", WorkspaceView.STUDENTS), viewResult);
            assertFalse(viewResult.hasSavedChanges());
        }
        for (String topic : List.of("group", "student", "assignment")) {
            for (String name : List.of(topic, topic + "s")) {
                CommandResult helpResult = app.logic.execute("help " + name);
                assertEquals(CommandResult.forHelp("Showing " + topic + " commands.", topic), helpResult);
                assertFalse(helpResult.hasSavedChanges());
            }
        }
        assertEquals(previousData, app.model.getTAssist());
        assertEquals(savedFile, Files.readString(dataFile));
    }

    @Test
    public void getHelpEntries_everyTopicWithCommands_hasExamplesOfItsOwnCommands() {
        for (String topic : HelpCatalog.TOPICS) {
            List<HelpEntry> entries = HelpCatalog.filterByTopic(app.logic.getHelpEntries(), topic);
            assertFalse(entries.isEmpty(), "No help for " + topic);
            for (HelpEntry entry : entries) {
                String commandWords = getCommandWords(entry.getCommandFormat());
                assertTrue(entry.getExamples().stream().allMatch(example -> example.startsWith(commandWords + " ")
                        || example.equals(commandWords)), "An example of " + commandWords + " runs another command");
            }
        }
    }

    @Test
    public void examples_typedInHelpOrderIntoNewApp_allSucceed() {
        for (HelpEntry entry : app.logic.getHelpEntries()) {
            for (String example : entry.getExamples()) {
                assertDoesNotThrow(() -> app.logic.execute(example), "Help example failed: " + example);
            }
        }

        Group t01 = app.model.getGroup(new GroupName("T01"));
        // The group examples rename T02 to T02-Thu and then delete it.
        assertFalse(app.model.hasGroup(new GroupName("T02")));
        assertFalse(app.model.hasGroup(new GroupName("T02-Thu")));
        assertEquals(t01, app.model.getActiveGroup().orElseThrow());
        assertTrue(t01.hasStudent(new StudentId("A0123456X")));
        assertFalse(t01.hasStudent(new StudentId("A0234567Y")));
    }

    @Test
    public void examples_uppercaseCommandWords_produceSameDataAndSurviveRestart() throws Exception {
        Path uppercaseData = temporaryFolder.resolve("uppercase.json");
        MainApp uppercaseApp = new MainApp(temporaryFolder.resolve("uppercase-prefs.json"), uppercaseData);
        uppercaseApp.init();
        for (HelpEntry entry : app.logic.getHelpEntries()) {
            String commandWords = getCommandWords(entry.getCommandFormat());
            for (String example : entry.getExamples()) {
                app.logic.execute(example);
                uppercaseApp.logic.execute(commandWords.toUpperCase(Locale.ROOT)
                        + example.substring(commandWords.length()));
                assertEquals(app.model.getTAssist(), uppercaseApp.model.getTAssist(), example);
            }
        }
        MainApp reopenedApp = new MainApp(temporaryFolder.resolve("uppercase-prefs.json"), uppercaseData);
        reopenedApp.init();
        assertEquals(uppercaseApp.model.getTAssist(), reopenedApp.model.getTAssist());
    }

    @Test
    public void execute_uppercaseCommandsWithInvalidArguments_preservesDataAndSavedFile() throws Exception {
        app.logic.execute("GROUP ADD g/T01");
        TAssist previousData = new TAssist(app.model.getTAssist());
        Path dataFile = temporaryFolder.resolve("tassist.json");
        String savedFile = Files.readString(dataFile);
        for (String input : List.of("GROUP ADD G/T02", "GROUP ADD g/T02 g/T03", "GROUP ADD", "GROUP LiSts",
                "STUDENT ADD n/Alice ID/A1", "UNDO extra", "REDO extra", "EXIT extra")) {
            assertThrows(ParseException.class, () -> app.logic.execute(input), input);
            assertEquals(previousData, app.model.getTAssist());
            assertEquals(savedFile, Files.readString(dataFile));
        }
        app.logic.execute("UnDo");
        assertTrue(app.model.getTAssist().getGroupList().isEmpty());
        app.logic.execute("ReDo");
        assertEquals(previousData, app.model.getTAssist());
    }

    @Test
    public void userGuide_showsEveryHelpFormatAndExample() throws Exception {
        String userGuide = Files.readString(USER_GUIDE);
        for (HelpEntry entry : app.logic.getHelpEntries()) {
            assertTrue(userGuide.contains("`" + entry.getCommandFormat() + "`"),
                    "The User Guide does not show the format " + entry.getCommandFormat());
            for (String example : entry.getExamples()) {
                assertTrue(userGuide.contains("`" + example + "`"), "The User Guide does not show " + example);
            }
        }
    }

    @Test
    public void userGuideCommandSummary_usesOnlyHelpExamples() throws Exception {
        String userGuide = Files.readString(USER_GUIDE);
        String commandSummary = userGuide.substring(userGuide.indexOf(COMMAND_SUMMARY_HEADING));
        List<String> helpExamples = app.logic.getHelpEntries().stream()
                .flatMap(entry -> entry.getExamples().stream()).toList();

        Matcher summaryExamples = SUMMARY_EXAMPLE.matcher(commandSummary);
        int exampleCount = 0;
        while (summaryExamples.find()) {
            exampleCount++;
            assertTrue(helpExamples.contains(summaryExamples.group(1)),
                    "The command summary example is not a help example: " + summaryExamples.group(1));
        }
        assertTrue(exampleCount > 0, "The command summary has no examples");
    }

    /**
     * Returns the leading command words of {@code commandFormat}, such as {@code group add} for
     * {@code group add g/GROUP_NAME}. Command words contain lowercase letters only.
     */
    private static String getCommandWords(String commandFormat) {
        return Arrays.stream(commandFormat.split(" "))
                .takeWhile(word -> word.matches("[a-z]+"))
                .collect(Collectors.joining(" "));
    }
}
