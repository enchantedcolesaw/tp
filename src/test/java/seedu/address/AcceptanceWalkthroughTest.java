package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.DataLoadStatus;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.TAssistArgumentParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.TAssist;
import seedu.address.model.student.StudentName;

/**
 * Walks through a TA's use of TAssist from a first launch, as the end-to-end acceptance pass does by hand with the
 * JAR: record a tutorial in every feature, restart, and check that everything is restored. Each restart opens a new
 * app on the same files, so the checks go through the real parsers, model and JSON storage.
 */
public class AcceptanceWalkthroughTest extends AppIntegrationTest {
    /** Commands of one tutorial, covering every feature, in the order a TA would enter them. */
    private static final List<String> TUTORIAL_SESSION = List.of(
            "group add g/T01",
            "group add g/T02",
            "group use g/t01",
            "student add n/Alice Tan id/A0123456X",
            "student add n/Ben Lim id/A0234567Y",
            "student add n/Chloe O'Neil-Ng id/a0345678z",
            "attendance mark id/A0123456X w/1 s/present",
            "attendance mark id/A0234567Y w/1 s/absent",
            "attendance mark id/a0123456x w/1 s/ABSENT",
            "participation set id/A0123456X w/1 s/3",
            "participation set id/A0345678Z w/1 s/0",
            "assignment add a/PS1",
            "assignment submit a/PS1 id/A0123456X s/yes",
            "assignment grade a/ps1 id/A0123456X m/87",
            "assignment submit a/PS1 id/A0234567Y s/yes",
            "group use g/T02",
            "student add n/Alice Tan id/A0123456X",
            "attendance mark id/A0123456X w/1 s/present",
            "group use g/T01");

    @BeforeEach
    public void setUp() throws Exception {
        app = openApp();
    }

    @Test
    public void firstLaunch_readOnlyCommands_startEmptyWithoutCreatingDataFile() throws Exception {
        assertEquals(DataLoadStatus.MISSING, app.getDataLoadStatus());
        for (String command : List.of("help", "help student", "view storage", "group list")) {
            app.logic.execute(command);
        }

        assertEquals(new TAssist(), app.model.getTAssist());
        assertFalse(Files.exists(dataFile), "Commands that change nothing must not create the data file");
    }

    @Test
    public void tutorialSession_restart_restoresEveryRecord() throws Exception {
        enterTutorialSession();
        TAssist recorded = new TAssist(app.model.getTAssist());

        restart();

        assertEquals(DataLoadStatus.LOADED, app.getDataLoadStatus());
        assertEquals(recorded, app.model.getTAssist());
        assertRecordsOfT01();
        assertEquals("Switched active group to T02.", feedback("group use g/T02"));
        assertEquals("Attendance for T02, Week 1: 1 present, 0 absent, "
                + "0 late, 0 excused, 0 not recorded\nAlice Tan | A0123456X | Present",
                feedback("attendance list w/1"));
    }

    @Test
    public void invalidInput_afterSession_changesNeitherDataNorFile() throws Exception {
        enterTutorialSession();
        TAssist recorded = new TAssist(app.model.getTAssist());
        byte[] savedFile = Files.readAllBytes(dataFile);

        List<String> invalidCommands = List.of(
                "group add g/t01", "group use g/T09", "group add g/T_01", "group add g/T03 g/T04",
                "student add n/Alice", "student add n/Dan id/a0123456x", "student add n/" + "a".repeat(10_000)
                        + " id/A1", "student delete id/A404",
                "attendance mark id/A0123456X w/14 s/present", "attendance mark id/A0123456X w/1 s/unknown",
                "attendance mark id/A0123456X w/1 s/present w/2", "attendance mark id/A9 w/1 s/present",
                "participation set id/A0123456X w/1 s/6", "participation set id/A0123456X w/1",
                "assignment add a/ps1", "assignment grade a/PS1 id/A0999999Z m/50",
                "assignment grade a/PS1 id/A0123456X m/101", "assignment list a/PS9",
                "attendance", "unknown", "view nowhere", "help nothing");
        for (String command : invalidCommands) {
            assertThrows(Exception.class, () -> app.logic.execute(command));
            assertEquals(recorded, app.model.getTAssist(), "Rejected command changed data: " + command);
        }

        assertArrayEquals(savedFile, Files.readAllBytes(dataFile), "Rejected commands must not save");
        restart();
        assertEquals(recorded, app.model.getTAssist());
    }

    @Test
    public void pastedCommands_unicodeSpaces_actLikeNormalSpaces() throws Exception {
        assertEquals("Created tutorial group: T04.\nActive group is now T04.", feedback("group add\u00A0g/T04"));
        assertEquals("Created tutorial group: T03.\nActive group is now T03.", feedback("group add g/T03\u00A0"));
        assertEquals("Added Ann Lee to T03.", feedback("student add n/Ann\u00A0Lee id/N1"));
        assertEquals("Added Bob Lee to T03.", feedback("\u3000student\u2003add n/Bob\u2003Lee id/A2\u202F"));
        assertEquals("Added Cy Ng to T03.", feedback("student add\u2028n/Cy\u0085Ng\u2029id/A3"));
        TAssist beforeRejected = new TAssist(app.model.getTAssist());
        assertThrows(ParseException.class, String.format(TAssistArgumentParser.MESSAGE_BLANK_VALUE, "a/")
                + " Usage: assignment add a/ASSIGNMENT_NAME", () -> app.logic.execute("assignment add a/\u2003"));
        assertEquals(beforeRejected, app.model.getTAssist());

        restart();
        assertEquals("Students in T03:\n1. Ann Lee | N1\n2. Bob Lee | A2\n3. Cy Ng | A3", feedback("student list"));
    }

    @Test
    public void pastedCommands_zeroWidthSpaces_rejectedWithoutChangingData() throws Exception {
        feedback("group add g/T03");
        TAssist beforeRejected = new TAssist(app.model.getTAssist());

        assertThrows(ParseException.class, String.format(Messages.MESSAGE_UNKNOWN_COMMAND, "group\u200Badd"), () ->
                app.logic.execute("group\u200Badd g/T05"));
        assertThrows(ParseException.class, StudentName.MESSAGE_CONSTRAINTS, () ->
                app.logic.execute("student add n/Ann\u200BLee id/N1"));
        assertEquals(beforeRejected, app.model.getTAssist());
    }

    @Test
    public void studentDelete_restart_removesOnlyThatStudentAndTheirRecords() throws Exception {
        enterTutorialSession();

        assertEquals("Removed Ben Lim (A0234567Y) from T01.", feedback("student delete id/a0234567y"));
        restart();

        assertEquals("Students in T01:\n1. Alice Tan | A0123456X\n2. Chloe O'Neil-Ng | A0345678Z",
                feedback("student list"));
        assertEquals("Attendance for T01, Week 1: 0 present, 1 absent, "
                + "0 late, 0 excused, 1 not recorded\nAlice Tan | A0123456X | Absent\n"
                + "Chloe O'Neil-Ng | A0345678Z | Not recorded", feedback("attendance list w/1"));
        assertEquals("PS1 records for T01:\nAlice Tan | A0123456X | Submitted | 87\n"
                + "Chloe O'Neil-Ng | A0345678Z | Not submitted | -", feedback("assignment list a/PS1"));
        feedback("group use g/T02");
        assertEquals("Students in T02:\n1. Alice Tan | A0123456X", feedback("student list"));
    }

    @Test
    public void unreadableDataFile_restart_keepsFileAndRejectsChanges() throws Exception {
        enterTutorialSession();
        Files.writeString(dataFile, "{ \"groups\" : [ ");
        byte[] unreadableFile = Files.readAllBytes(dataFile);

        restart();

        assertTrue(app.getDataLoadStatus().isFailure());
        assertEquals(new TAssist(), app.model.getTAssist());
        assertThrows(CommandException.class, LogicManager.getDataProtectedMessage(app.getDataLoadStatus()), () ->
                app.logic.execute("group add g/T01"));
        app.logic.execute("help");
        assertArrayEquals(unreadableFile, Files.readAllBytes(dataFile), "An unreadable file must never be replaced");
    }

    /**
     * Enters {@link #TUTORIAL_SESSION}, checking that every command succeeds and the session ends with saved data.
     */
    private void enterTutorialSession() throws Exception {
        for (String command : TUTORIAL_SESSION) {
            app.logic.execute(command);
        }
        assertTrue(Files.exists(dataFile), "The session must have been saved");
    }

    /**
     * Asserts that T01 is active and its students, attendance, participation and assignment records are those
     * entered in {@link #TUTORIAL_SESSION}.
     */
    private void assertRecordsOfT01() throws Exception {
        assertEquals("1. T01\n2. T02\nActive group: T01", feedback("group list"));
        assertEquals("Students in T01:\n1. Alice Tan | A0123456X\n2. Ben Lim | A0234567Y\n"
                + "3. Chloe O'Neil-Ng | A0345678Z", feedback("student list"));
        assertEquals("Attendance for T01, Week 1: 0 present, 2 absent, 0 late, 0 excused, 1 not recorded\n"
                + "Alice Tan | A0123456X | Absent\nBen Lim | A0234567Y | Absent\n"
                + "Chloe O'Neil-Ng | A0345678Z | Not recorded", feedback("attendance list w/1"));
        assertEquals("Participation for T01, Week 1: 2 recorded, 1 not recorded\n"
                + "Alice Tan | A0123456X | 3\nBen Lim | A0234567Y | Not recorded\n"
                + "Chloe O'Neil-Ng | A0345678Z | 0", feedback("participation list w/1"));
        assertEquals("PS1 records for T01:\nAlice Tan | A0123456X | Submitted | 87\n"
                + "Ben Lim | A0234567Y | Submitted | -\nChloe O'Neil-Ng | A0345678Z | Not submitted | -",
                feedback("assignment list a/PS1"));
    }
}
