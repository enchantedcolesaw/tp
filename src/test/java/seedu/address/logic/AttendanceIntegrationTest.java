package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.Messages.MESSAGE_NO_ACTIVE_GROUP;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_STUDENT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalStudents.ALICE;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AttendanceClearCommand;
import seedu.address.logic.commands.AttendanceMarkCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AttendanceCommandParserUtil;
import seedu.address.logic.parser.AttendanceCommandRegistration;
import seedu.address.logic.parser.CommandRegistry;
import seedu.address.logic.parser.GroupCommandRegistration;
import seedu.address.logic.parser.TAssistArgumentParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.TAssist;
import seedu.address.model.UserPrefs;
import seedu.address.model.student.AttendanceStatus;
import seedu.address.storage.JsonTAssistStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.TypicalGroups;

public class AttendanceIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private StorageManager storage;
    private Path dataFile;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(TypicalGroups.NAME_T01);
        dataFile = temporaryFolder.resolve("tassist.json");
        storage = new StorageManager(new JsonTAssistStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        logic = createLogic();
    }

    @Test
    public void execute_markCorrectAndReload_preservesRecordsAndGroupIsolation() throws Exception {
        model.addStudent(TypicalGroups.NAME_T02, ALICE);
        model.setStudent(TypicalGroups.NAME_T01, ALICE.withParticipationRecord(3, 4));
        assertFeedback("attendance mark id/A0123456X w/1 s/present", "Recorded Alice Tan as present for Week 1.");
        assertFeedback("attendance mark id/A0123456X w/3 s/present", "Recorded Alice Tan as present for Week 3.");
        assertFeedback("attendance mark id/A0123456X w/3 s/absent",
                "Updated Alice Tan's attendance for Week 3: present to absent."
                        + " Warning: Alice Tan has participation score 4 for Week 3.");
        restart();

        assertFeedback("attendance list w/3", "Attendance for T01, Week 3: 0 present, 1 absent, "
                + "0 late, 0 excused, 2 not recorded\n"
                + "Alice Tan | A0123456X | Absent\n"
                + "Ben Lim | A0234567Y | Not recorded\n"
                + "Carl Goh | A0345678Z | Not recorded");
        assertEquals(ALICE.withAttendanceRecord(1, "PRESENT").withAttendanceRecord(3, "ABSENT")
                .withParticipationRecord(3, 4), model.getGroup(TypicalGroups.NAME_T01)
                        .findStudent(ALICE.getStudentId()).orElseThrow());

        logic.execute("group use g/T02");
        assertFeedback("attendance list w/3", "Attendance for T02, Week 3: 0 present, 0 absent, "
                + "0 late, 0 excused, 3 not recorded\n"
                + "Daniel Ong | A0456789W | Not recorded\n"
                + "Elle Teo | A0567890U | Not recorded\n"
                + "Alice Tan | A0123456X | Not recorded");
        logic.execute("attendance mark id/A0123456X w/13 s/present");
        restart();
        assertFeedback("attendance list w/13", "Attendance for T02, Week 13: 1 present, 0 absent, "
                + "0 late, 0 excused, 2 not recorded\n"
                + "Daniel Ong | A0456789W | Not recorded\n"
                + "Elle Teo | A0567890U | Not recorded\n"
                + "Alice Tan | A0123456X | Present");
        assertEquals(ALICE.withAttendanceRecord(13, "PRESENT"),
                model.getGroup(TypicalGroups.NAME_T02).findStudent(ALICE.getStudentId()).orElseThrow());
    }

    @Test
    public void execute_markSeveralStudentsAndReload_savesEveryStudentOnly() throws Exception {
        assertFeedback("attendance mark id/A0123456X id/a0345678z id/A0123456X w/2 s/absent",
                "Recorded Alice Tan as absent for Week 2.\nRecorded Carl Goh as absent for Week 2.");
        restart();

        assertFeedback("attendance list w/2", "Attendance for T01, Week 2: 0 present, 2 absent, "
                + "0 late, 0 excused, 1 not recorded\n"
                + "Alice Tan | A0123456X | Absent\n"
                + "Ben Lim | A0234567Y | Not recorded\n"
                + "Carl Goh | A0345678Z | Absent");
    }

    @Test
    public void execute_markSeveralStudentsWithUnknownId_savesNobody() throws Exception {
        assertFeedback("attendance mark id/A0234567Y w/1 s/present", "Recorded Ben Lim as present for Week 1.");
        assertFailureUnchanged("attendance mark id/A0123456X id/A0456789W id/A0123456X w/2 s/absent",
                CommandException.class, String.format(MESSAGE_UNKNOWN_STUDENT, "A0456789W", TypicalGroups.NAME_T01));
    }

    @Test
    public void execute_clearAndReload_removesOnlyThatMark() throws Exception {
        model.setStudent(TypicalGroups.NAME_T01, ALICE.withParticipationRecord(4, 3));
        logic.execute("attendance mark id/A0123456X w/3 s/present");
        logic.execute("attendance mark id/A0123456X w/4 s/present");
        logic.execute("attendance mark id/A0234567Y w/4 s/absent");

        assertFeedback("attendance clear id/a0123456x w/4",
                "Cleared Alice Tan's attendance for Week 4: present to Not recorded.");
        restart();

        assertFeedback("attendance list w/4", "Attendance for T01, Week 4: 0 present, 1 absent, "
                + "0 late, 0 excused, 2 not recorded\n"
                + "Alice Tan | A0123456X | Not recorded\n"
                + "Ben Lim | A0234567Y | Absent\n"
                + "Carl Goh | A0345678Z | Not recorded");
        assertEquals(ALICE.withAttendanceRecord(3, "PRESENT").withParticipationRecord(4, 3),
                model.getGroup(TypicalGroups.NAME_T01).findStudent(ALICE.getStudentId()).orElseThrow());
        logic.execute("attendance clear id/A0234567Y w/4");
        assertFeedback("attendance list w/4", "Attendance for T01, Week 4: 0 present, 0 absent, "
                + "0 late, 0 excused, 3 not recorded\n"
                + "Alice Tan | A0123456X | Not recorded\n"
                + "Ben Lim | A0234567Y | Not recorded\n"
                + "Carl Goh | A0345678Z | Not recorded");
    }

    @Test
    public void execute_clearUnrecordedWeek_leavesSavedFileUntouched() throws Exception {
        logic.execute("attendance mark id/A0123456X w/3 s/present");
        String savedData = Files.readString(dataFile);

        CommandResult result = logic.execute("attendance clear id/A0123456X w/4");

        assertEquals("Alice Tan has no attendance recorded for Week 4. Nothing changed.", result.getFeedbackToUser());
        assertFalse(result.hasSavedChanges());
        assertEquals(savedData, Files.readString(dataFile));
    }

    @Test
    public void execute_rejectedClear_preservesModelAndFile() throws Exception {
        logic.execute("attendance mark id/A0123456X w/3 s/present");
        for (String week : new String[] {"0", "14", "3.5"}) {
            assertFailureUnchanged("attendance clear id/A0123456X w/" + week,
                    ParseException.class, AttendanceCommandParserUtil.MESSAGE_INVALID_WEEK);
        }
        assertFailureUnchanged("attendance clear id/A0123456X", ParseException.class,
                String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, "w/")
                        + " Usage: " + AttendanceClearCommand.MESSAGE_USAGE);
        assertFailureUnchanged("attendance clear id/A0123456X w/3 s/present", ParseException.class,
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "s/", "id/ w/"));
        assertFailureUnchanged("attendance clear id/A0123456X w/3 now", ParseException.class,
                String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "w/3", "now"));
        assertFailureUnchanged("attendance clear id/A0456789W w/3",
                CommandException.class, String.format(MESSAGE_UNKNOWN_STUDENT, "A0456789W", TypicalGroups.NAME_T01));
    }

    @Test
    public void execute_invalidValuesAndStudent_preservesModelAndFile() throws Exception {
        logic.execute("attendance mark id/A0123456X w/3 s/present");
        for (String week : new String[] {"0", "14", "-1", "3.5", "three", "9".repeat(1000)}) {
            assertFailureUnchanged("attendance mark id/A0123456X w/" + week + " s/absent",
                    ParseException.class, AttendanceCommandParserUtil.MESSAGE_INVALID_WEEK);
            assertFailureUnchanged("attendance list w/" + week,
                    ParseException.class, AttendanceCommandParserUtil.MESSAGE_INVALID_WEEK);
        }
        assertFailureUnchanged("attendance mark id/A0123456X w/3 s/unknown",
                ParseException.class, AttendanceStatus.MESSAGE_CONSTRAINTS);
        assertFailureUnchanged("attendance mark id/A0456789W w/3 s/present",
                CommandException.class, String.format(MESSAGE_UNKNOWN_STUDENT, "A0456789W", TypicalGroups.NAME_T01));
    }

    @Test
    public void execute_badFormat_preservesModelAndFile() throws Exception {
        logic.execute("attendance mark id/A0123456X w/3 s/present");
        assertFailureUnchanged("attendance mark id/A0123456X s/absent", ParseException.class,
                String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, "w/")
                        + " Usage: " + AttendanceMarkCommand.MESSAGE_USAGE);
        assertFailureUnchanged("attendance mark id/A0123456Xw/3 s/absent", ParseException.class,
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "w/", "id/A0123456Xw/3"));
        assertFailureUnchanged("attendance mark id/A0123456X w/3 s/absent s/present", ParseException.class,
                String.format(Messages.MESSAGE_DUPLICATE_FIELDS, "s/"));
        assertFailureUnchanged("attendance list w/3 x/value", ParseException.class,
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "x/", "w/"));
    }

    @Test
    public void execute_noActiveGroup_preservesModelAndFile() throws Exception {
        model.setTAssist(TypicalGroups.getTypicalTAssist());
        storage.saveTAssist(model.getTAssist());
        assertFailureUnchanged("attendance mark id/A0123456X w/3 s/present",
                CommandException.class, MESSAGE_NO_ACTIVE_GROUP);
        assertFailureUnchanged("attendance list w/3", CommandException.class, MESSAGE_NO_ACTIVE_GROUP);
        assertFailureUnchanged("attendance clear id/A0123456X w/3", CommandException.class, MESSAGE_NO_ACTIVE_GROUP);
    }

    private Logic createLogic() {
        CommandRegistry registry = new CommandRegistry();
        GroupCommandRegistration.registerCommands(registry);
        AttendanceCommandRegistration.registerCommands(registry);
        return new LogicManager(model, storage, registry);
    }

    @Test
    public void execute_clearSeveralStudents_clearsAllOrNoneAndSaves() throws Exception {
        assertFeedback("attendance mark id/A0123456X id/A0234567Y w/5 s/present",
                "Recorded Alice Tan as present for Week 5.\nRecorded Ben Lim as present for Week 5.");
        assertFailureUnchanged("attendance clear id/A0123456X id/A0234567Y id/A0999999Z w/5",
                CommandException.class, String.format(MESSAGE_UNKNOWN_STUDENT, "A0999999Z", TypicalGroups.NAME_T01));
        assertFeedback("attendance clear id/a0234567y id/A0345678Z id/A0123456X w/5",
                "Cleared Ben Lim's attendance for Week 5: present to Not recorded.\n"
                        + "Carl Goh has no attendance recorded for Week 5. Nothing changed.\n"
                        + "Cleared Alice Tan's attendance for Week 5: present to Not recorded.");
        restart();
        assertFeedback("attendance list w/5", "Attendance for T01, Week 5: 0 present, 0 absent, "
                + "0 late, 0 excused, 3 not recorded\n"
                + "Alice Tan | A0123456X | Not recorded\nBen Lim | A0234567Y | Not recorded\n"
                + "Carl Goh | A0345678Z | Not recorded");
    }

    private void restart() throws Exception {
        model = new ModelManager(storage.readTAssist().orElseThrow(), new UserPrefs());
        logic = createLogic();
    }

    private void assertFeedback(String command, String feedback) throws Exception {
        assertEquals(feedback, logic.execute(command).getFeedbackToUser());
    }

    private void assertFailureUnchanged(String command, Class<? extends Throwable> exceptionType,
            String message) throws Exception {
        TAssist before = new TAssist(model.getTAssist());
        String savedData = Files.readString(dataFile);
        assertThrows(exceptionType, message, () -> logic.execute(command));
        assertEquals(before, model.getTAssist());
        assertEquals(savedData, Files.readString(dataFile));
    }
}
