package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AttendanceCommandRegistration;
import seedu.address.logic.parser.CommandRegistry;
import seedu.address.logic.parser.GroupCommandRegistration;
import seedu.address.logic.parser.ParticipationCommandRegistration;
import seedu.address.logic.parser.StudentCommandRegistration;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyTAssist;
import seedu.address.model.TAssist;
import seedu.address.model.UserPrefs;
import seedu.address.model.student.StudentId;
import seedu.address.storage.JsonTAssistStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Verifies attendance categories through real commands, history, JSON storage, and CSV export. */
public class AttendanceStatusesIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private Logic logic;
    private StorageManager storage;
    private Path data;

    @BeforeEach
    public void setUp() throws Exception {
        data = temporaryFolder.resolve("tassist.json");
        storage = new StorageManager(new JsonTAssistStorage(data),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        model = new ModelManager();
        logic = createLogic();
        logic.execute("group add g/T01");
        for (String entry : List.of("Alice id/A1", "Ben id/B2", "Carl id/C3", "Dana id/D4", "Elle id/E5")) {
            logic.execute("student add n/" + entry);
        }
    }

    @Test
    public void execute_allCategories_countsEachStudentOnceAndExportsAfterReload() throws Exception {
        logic.execute("attendance mark id/A1 w/1 s/present");
        logic.execute("attendance mark id/B2 w/1 s/absent");
        logic.execute("attendance mark id/C3 w/1 s/LaTe");
        logic.execute("attendance mark id/D4 w/1 s/EXCUSED");
        restart();

        assertEquals("Attendance for T01, Week 1: 1 present, 1 absent, 1 late, 1 excused, 1 not recorded\n"
                + "Alice | A1 | Present\nBen | B2 | Absent\nCarl | C3 | Late\nDana | D4 | Excused\n"
                + "Elle | E5 | Not recorded", logic.execute("attendance list w/1").getFeedbackToUser());
        logic.execute("group export");
        List<String> rows = Files.readAllLines(temporaryFolder.resolve("exports/T01-records.csv"));
        assertEquals(6, rows.size());
        assertEquals("Present", rows.get(1).split(",", -1)[2]);
        assertEquals("Absent", rows.get(2).split(",", -1)[2]);
        assertEquals("Late", rows.get(3).split(",", -1)[2]);
        assertEquals("Excused", rows.get(4).split(",", -1)[2]);
        assertEquals("", rows.get(5).split(",", -1)[2]);
        assertTrue(Files.readString(data).contains("\"EXCUSED\""));
    }

    @Test
    public void execute_correctClearUndoRedo_preservesWeeksAndGroupIsolation() throws Exception {
        logic.execute("attendance mark id/A1 id/B2 id/a1 w/13 s/late");
        logic.execute("attendance mark id/A1 w/1 s/present");
        logic.execute("attendance mark id/A1 w/13 s/excused");
        String saved = Files.readString(data);
        assertFalse(logic.execute("attendance mark id/A1 w/13 s/excused").hasSavedChanges());
        assertEquals(saved, Files.readString(data));
        logic.execute("attendance clear id/A1 id/B2 w/13");
        assertFalse(records("A1").containsKey(13));
        logic.execute("undo");
        assertEquals(Map.of(1, "PRESENT", 13, "EXCUSED"), records("A1"));
        assertEquals(Map.of(13, "LATE"), records("B2"));
        logic.execute("undo");
        assertEquals("LATE", records("A1").get(13));
        logic.execute("redo");
        assertEquals("EXCUSED", records("A1").get(13));
        logic.execute("redo");
        assertEquals(Map.of(1, "PRESENT"), records("A1"));
        logic.execute("undo");
        logic.execute("group add g/T02");
        logic.execute("student add n/Alice id/A1");
        logic.execute("attendance mark id/A1 w/13 s/absent");
        logic.execute("group use g/T01");
        restart();
        assertEquals("EXCUSED", records("A1").get(13));
        assertEquals("LATE", records("B2").get(13));
    }

    @Test
    public void execute_unknownIdOrInvalidStatus_changesNobody() throws Exception {
        logic.execute("attendance mark id/A1 id/B2 w/3 s/late");
        TAssist before = new TAssist(model.getTAssist());
        String saved = Files.readString(data);
        for (String command : List.of("attendance mark id/A1 id/Z9 w/3 s/excused",
                "attendance clear id/A1 id/Z9 w/3")) {
            assertThrows(CommandException.class, () -> logic.execute(command));
            assertEquals(before, model.getTAssist());
            assertEquals(saved, Files.readString(data));
        }
        for (String command : List.of("attendance mark id/A1 w/3 s/unknown",
                "attendance mark id/A1 w/14 s/late", "attendance mark id/A1 w/3 s/excused s/late")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(before, model.getTAssist());
            assertEquals(saved, Files.readString(data));
        }
    }

    @Test
    public void execute_positiveParticipation_warnsOnlyForAbsencesInBothCommandOrders() throws Exception {
        logic.execute("participation set id/B2 w/4 s/3");
        for (String status : List.of("present", "late", "absent", "excused")) {
            boolean expectsWarning = status.equals("absent") || status.equals("excused");
            logic.execute("attendance mark id/A1 w/4 s/" + status);
            String feedback = logic.execute("participation set id/A1 w/4 s/3").getFeedbackToUser();
            assertEquals(expectsWarning, feedback.contains("Warning:"));
            if (expectsWarning) {
                assertTrue(feedback.contains("marked " + status + " for Week 4"));
            }
            feedback = logic.execute("attendance mark id/B2 w/4 s/" + status).getFeedbackToUser();
            assertEquals(expectsWarning, feedback.contains("Warning:"));
            feedback = logic.execute("attendance mark id/B2 w/4 s/" + status).getFeedbackToUser();
            assertEquals(expectsWarning, feedback.contains("Warning:"));
            assertFalse(logic.execute("participation set id/A1 w/4 s/0").getFeedbackToUser().contains("Warning:"));
        }
    }

    @Test
    public void read_manuallyEditedStatuses_normalizesWithoutLosingLegacyRecords() throws Exception {
        Files.writeString(data, """
                {"activeGroup":"T01","groups":[{"name":"T01","students":[{
                  "name":"Alice","studentId":"A1",
                  "attendance":{"1":"PRESENT","2":"ABSENT","3":" late ","13":"Excused"}
                }]}]}
                """);
        restart();
        assertEquals(Map.of(1, "PRESENT", 2, "ABSENT", 3, "LATE", 13, "EXCUSED"), records("A1"));
        logic.execute("attendance mark id/A1 w/4 s/late");
        restart();
        assertEquals("LATE", records("A1").get(4));
        assertEquals("EXCUSED", records("A1").get(13));
    }

    @Test
    public void execute_saveFailure_rollsBackNewStatusAndHistory() throws Exception {
        logic.execute("attendance mark id/A1 w/3 s/late");
        TAssist before = new TAssist(model.getTAssist());
        String saved = Files.readString(data);
        StorageManager failingStorage = new StorageManager(new JsonTAssistStorage(data) {
            @Override
            public void saveTAssist(ReadOnlyTAssist state) throws IOException {
                throw new IOException("Simulated disk failure");
            }
        }, new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        StorageManager successfulStorage = storage;
        storage = failingStorage;
        logic = createLogic();
        assertThrows(CommandException.class, () -> logic.execute("attendance mark id/A1 w/3 s/excused"));
        assertEquals(before, model.getTAssist());
        assertEquals(saved, Files.readString(data));
        storage = successfulStorage;
        logic = createLogic();
        logic.execute("undo");
        assertFalse(records("A1").containsKey(3));
    }

    private Map<Integer, String> records(String id) {
        return model.getActiveGroup().orElseThrow().findStudent(new StudentId(id)).orElseThrow()
                .getAttendanceRecords().getRecords();
    }

    private Logic createLogic() {
        CommandRegistry registry = new CommandRegistry();
        AttendanceCommandRegistration.registerCommands(registry);
        ParticipationCommandRegistration.registerCommands(registry);
        StudentCommandRegistration.registerCommands(registry);
        GroupCommandRegistration.registerCommands(registry, temporaryFolder.resolve("exports"));
        return new LogicManager(model, storage, registry);
    }

    private void restart() throws Exception {
        model = new ModelManager(storage.readTAssist().orElseThrow(), new UserPrefs());
        logic = createLogic();
    }
}
