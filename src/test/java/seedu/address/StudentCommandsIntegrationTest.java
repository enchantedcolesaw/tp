package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.GroupExportCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.logic.help.HelpEntry;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.TAssist;
import seedu.address.model.student.Student;
import seedu.address.storage.GroupRecordsCsvExporter;

public class StudentCommandsIntegrationTest extends AppIntegrationTest {
    @BeforeEach
    public void setUp() throws Exception {
        app = openApp();
        app.logic.execute("group add g/T01");
    }

    @Test
    public void execute_namesWithPeriods_addedAndRestored() throws Exception {
        feedback("student add n/S. Ramanathan id/A1");
        feedback("student add n/Mohd. Ali id/A2");
        feedback("student add n/John Smith Jr. id/A3");
        restart();
        assertEquals("Students in T01:\n1. S. Ramanathan | A1\n2. Mohd. Ali | A2\n3. John Smith Jr. | A3",
                feedback("student list"));
        feedback("student find n/Smith Jr.");
        assertEquals("John Smith Jr.", app.logic.getDisplayedStudentList().getFirst().getName().toString());
    }

    @Test
    public void execute_unicodeEquivalentNames_warnAndKeepSpellingAcrossUndoRedoAndRestart() throws Exception {
        String decomposedName = "Jose\u0301 Tan";
        feedback("student add n/José Tan id/J1");
        assertEquals("Added " + decomposedName + " to T01. Warning: T01 already has a student named José Tan (J1). "
                + "Check that the student ID is correct.", feedback("student add n/" + decomposedName + " id/J2"));
        feedback("student add n/Ben Lim id/J3");
        assertEquals("Edited Ben Lim (J3) to " + decomposedName + " (J3) in T01. Their records are kept."
                + " Warning: T01 already has another student named " + decomposedName + ".",
                feedback("student edit id/J3 n/" + decomposedName));

        feedback("undo");
        assertTrue(feedback("student list").endsWith("Ben Lim | J3"));
        feedback("redo");
        restart();
        assertEquals(List.of("José Tan", decomposedName, decomposedName), app.logic.getActiveGroupStudentList().stream()
                .map(student -> student.getName().fullName).toList());
        assertEquals("Added Jose Tan to T01.", feedback("student add n/Jose Tan id/J4"));
    }

    @Test
    public void execute_namesWithSlashParticles_addedAndRestored() throws Exception {
        assertEquals("Added Ravi s/o Kumar to T01.", feedback("student add n/Ravi s/o Kumar id/A0345678Z"));
        assertEquals("Added Siti D/O Ahmad to T01.", feedback("student add n/Siti D/O Ahmad id/A0456789W"));
        restart();
        assertEquals("Students in T01:\n1. Ravi s/o Kumar | A0345678Z\n2. Siti D/O Ahmad | A0456789W",
                feedback("student list"));
        // The particle does not change how the other commands of the group read their own s/ prefixes.
        assertEquals("Recorded Ravi s/o Kumar as present for Week 3.",
                feedback("attendance mark id/A0345678Z w/3 s/present"));
    }

    @Test
    public void execute_findWithSlashParticles_matchesNames() throws Exception {
        feedback("student add n/Ravi s/o Kumar id/A0345678Z");
        feedback("student add n/Muthu a/l Raju id/A0456789W");
        Map<String, String> searches = Map.of("student find n/Ravi s/o", "A0345678Z",
                "student find n/MUTHU A/L raju", "A0456789W");
        for (Map.Entry<String, String> search : searches.entrySet()) {
            assertEquals("Found 1 matching student in T01.", feedback(search.getKey()), search.getKey());
            assertEquals(List.of(search.getValue()), app.logic.getDisplayedStudentList().stream()
                    .map(student -> student.getStudentId().toString()).toList(), search.getKey());
        }
        String gluedParticleMessage = "Unknown prefix 's/'. Allowed prefixes: n/ id/. "
                + "If s/o is part of the value, put a space before and after it.";
        String gluedSearch = "student find n/Ravi s/oKumar";
        assertThrows(ParseException.class, gluedParticleMessage, () -> app.logic.execute(gluedSearch));
    }

    @Test
    public void execute_studentCommandsAndRestart_preservesRosterAndDeletion() throws Exception {
        assertEquals("Added Alice Tan to T01.", feedback("student add n/Alice Tan id/a0123456x"));
        assertEquals("Added Alice Tan to T01. Warning: T01 already has a student named Alice Tan (A0123456X). "
                + "Check that the student ID is correct.", feedback("student add n/Alice Tan id/A2"));
        assertEquals(2, app.logic.getActiveGroupStudentList().size());
        restart();
        assertEquals("Students in T01:\n1. Alice Tan | A0123456X\n2. Alice Tan | A2", feedback("student list"));
        assertEquals("Removed Alice Tan (A0123456X) from T01.", feedback("student delete id/a0123456x"));
        restart();
        assertEquals("Students in T01:\n1. Alice Tan | A2", feedback("student list"));
        feedback("student delete id/A2");
        restart();
        assertTrue(app.logic.getActiveGroupStudentList().isEmpty());
        assertEquals("No students in T01. Add one using: student add n/NAME id/STUDENT_ID", feedback("student list"));
    }

    @Test
    public void execute_studentEdit_keepsRecordsAcrossRestartUndoAndRedo() throws Exception {
        feedback("student add n/Ravi Kumarr id/A0345678Z");
        feedback("student add n/Ben Lim id/B2");
        feedback("attendance mark id/A0345678Z w/1 s/present");
        feedback("participation set id/A0345678Z w/1 s/4");
        feedback("assignment add a/PS1");
        feedback("assignment submit a/PS1 id/A0345678Z s/yes");
        feedback("assignment grade a/PS1 id/A0345678Z m/87");

        assertTrue(app.logic.execute("student edit id/A0345678Z n/Ravi s/o Kumar").hasSavedChanges());
        assertEquals("Edited Ravi s/o Kumar (A0345678Z) to Ravi s/o Kumar (A0345679Z) in T01. "
                + "Their records are kept.", feedback("student edit id/a0345678z nid/a0345679z"));
        assertEditedStudentKeepsRecords();

        assertEquals("Undone: student edit id/a0345678z nid/a0345679z", feedback("undo"));
        assertEquals("Students in T01:\n1. Ravi s/o Kumar | A0345678Z\n2. Ben Lim | B2", feedback("student list"));
        assertEquals("PS1 records for T01:\nRavi s/o Kumar | A0345678Z | Submitted | 87\n"
                + "Ben Lim | B2 | Not submitted | -", feedback("assignment list a/PS1"));
        feedback("redo");
        assertEditedStudentKeepsRecords();
        restart();
        assertEditedStudentKeepsRecords();
    }

    @Test
    public void execute_studentEditRejected_preservesModelAndSavedFile() throws Exception {
        feedback("student add n/Alice Tan id/A1");
        feedback("student add n/Ben Lim id/B2");
        for (String command : List.of("student edit", "student edit id/A1", "student edit n/Alice Tan",
                "student edit nid/A3", "student edit id/A1 n/", "student edit id/A1 nid/", "student edit id/A1 nid/!",
                "student edit id/A1 n/Al1ce", "student edit id/A1 id/A2 nid/A3", "student edit id/A1 nid/A3 nid/A4",
                "student edit id/A1 n/Alice n/Alicia", "student edit id/A1 x/Alice", "student edit id/A1nid/A3",
                "student edit id/A1 nid/A3 now", "student edit A1 n/Alice")) {
            assertRejected(command, ParseException.class);
        }
        for (String command : List.of("student edit id/C3 n/Carl Goh", "student edit id/A1 nid/b2",
                "student edit id/A1 n/Alicia nid/ B2 ", "student edit id/a1 nid/B2")) {
            assertRejected(command, CommandException.class);
        }
    }

    @Test
    public void execute_studentEditWithoutChange_isNotSavedOrUndoable() throws Exception {
        feedback("student add n/Alice Tan id/A1");
        String saved = Files.readString(dataFile);

        CommandResult result = app.logic.execute("student edit id/a1 n/Alice  Tan nid/A1");

        assertEquals("Alice Tan (A1) already has those details. Nothing changed.", result.getFeedbackToUser());
        assertFalse(result.hasSavedChanges());
        assertEquals(saved, Files.readString(dataFile));
        assertEquals("Undone: student add n/Alice Tan id/A1", feedback("undo"));
    }

    @Test
    public void execute_studentEditWhileSearching_keepsSearchAndSort() throws Exception {
        feedback("student add n/Alice Tan id/A1");
        feedback("student add n/Ben Lim id/B2");
        feedback("student find n/lim");
        feedback("student sort by/id");

        feedback("student edit id/B2 nid/A0");
        assertEquals(List.of("A0"), app.logic.getDisplayedStudentList().stream()
                .map(student -> student.getStudentId().toString()).toList());

        feedback("student edit id/A0 n/Benjamin Tan");
        assertTrue(app.logic.getDisplayedStudentList().isEmpty());
        assertEquals(2, app.logic.getActiveGroupStudentList().size());
        feedback("undo");
        assertEquals(List.of("A0"), app.logic.getDisplayedStudentList().stream()
                .map(student -> student.getStudentId().toString()).toList());
    }

    @Test
    public void execute_rejectedCommands_preservesModelAndSavedFile() throws Exception {
        feedback("student add n/Alice Tan id/A1");
        for (String command : List.of("student add", "student add n/Alice", "student add n/ id/A2",
                "student add n/Alice id/ABC", "student add n/Alice id/A2 id/A3", "student add n/Alice x/A2",
                "student add n/Aliceid/A2", "student add n/" + "a".repeat(10000) + " id/A2",
                "student list extra", "student delete", "student delete id/", "student delete id/A1 id/A2",
                "student delete n/Alice", "student", "student update")) {
            assertRejected(command, ParseException.class);
        }
        assertRejected("student add n/Other Name id/a1", CommandException.class);
        assertRejected("student delete id/A2", CommandException.class);
    }

    @Test
    public void execute_noActiveGroup_preservesModelAndSavedFile() throws Exception {
        app.model.setTAssist(new TAssist());
        app.storage.saveTAssist(app.model.getTAssist());
        for (String command : List.of("student add n/Alice id/A1", "student list", "student find n/Alice",
                "student sort by/name", "student delete id/A1")) {
            assertRejected(command, CommandException.class);
        }
    }

    @Test
    public void getConfirmationPrompt_namesStudentWithoutChangingSavedData() throws Exception {
        feedback("student add n/Alice Tan id/A1");
        TAssist previousData = new TAssist(app.model.getTAssist());
        String savedFile = Files.readString(dataFile);

        assertEquals(Optional.of("Remove Alice Tan (A1) and all their attendance, participation, submission, "
                        + "and grade records from T01?"),
                app.logic.getConfirmationPrompt("student delete id/A1"));
        assertEquals(Optional.empty(), app.logic.getConfirmationPrompt("student delete id/A1 f/yes"));
        assertEquals(app.logic.getConfirmationPrompt("student delete id/A1"),
                app.logic.getConfirmationPrompt("STUDENT DeLeTe id/A1"));
        assertEquals(Optional.empty(), app.logic.getConfirmationPrompt("STUDENT DELETE id/A1 f/yes"));
        assertEquals(app.logic.getConfirmationPrompt("group delete g/T01"),
                app.logic.getConfirmationPrompt("GROUP DeLeTe g/T01"));
        assertEquals(previousData, app.model.getTAssist());
        assertEquals(savedFile, Files.readString(dataFile));
    }

    @Test
    public void init_studentHelp_registersFormatsAndWorkingExamples() throws Exception {
        List<HelpEntry> entries = HelpCatalog.filterByTopic(app.logic.getHelpEntries(), "student");
        assertEquals(List.of("student add n/NAME id/STUDENT_ID", "student edit id/STUDENT_ID [n/NEW_NAME] [nid/NEW_ID]",
                        "student list",
                        "student find [n/NAME_FRAGMENT] [id/ID_FRAGMENT]", "student sort by/FIELD",
                        "student records id/STUDENT_ID", "student delete id/STUDENT_ID [f/yes]"),
                entries.stream().map(HelpEntry::getCommandFormat).toList());
        assertEquals(CommandResult.forHelp("Showing student commands.", "student"), app.logic.execute("help student"));
        for (HelpEntry entry : entries) {
            for (String example : entry.getExamples()) {
                app.logic.execute(example);
            }
        }
    }

    @Test
    public void execute_renameActiveGroup_preservesSearchAndSortAndSavesNewName() throws Exception {
        feedback("student add n/Ben Lim id/B2");
        feedback("student add n/Alice Tan id/A3");
        feedback("student add n/Alice Ng id/A1");
        feedback("student find n/Alice");
        feedback("student sort by/id");
        List<Student> displayed = List.copyOf(app.logic.getDisplayedStudentList());
        assertTrue(app.logic.execute("group rename g/T01 n/T01-Thu").hasSavedChanges());
        assertEquals(displayed, app.logic.getDisplayedStudentList());
        feedback("student add n/Alice Koh id/A2");
        assertEquals(List.of("A1", "A2", "A3"), app.logic.getDisplayedStudentList().stream()
                .map(student -> student.getStudentId().toString()).toList());
        restart();
        assertEquals("T01-Thu", app.model.getActiveGroup().orElseThrow().getName().toString());
        assertEquals(app.logic.getActiveGroupStudentList(), app.logic.getDisplayedStudentList());
    }

    @Test
    public void execute_undoOrRedoActiveGroupRename_preservesSearchAndSort() throws Exception {
        feedback("student add n/Ben Lim id/B2");
        feedback("student add n/Alice Tan id/A3");
        feedback("student add n/Alice Ng id/A1");
        feedback("student find n/Alice");
        feedback("student sort by/id");
        List<Student> displayed = List.copyOf(app.logic.getDisplayedStudentList());
        feedback("group rename g/T01 n/T01-Thu");

        feedback("undo");
        assertEquals("T01", app.model.getActiveGroup().orElseThrow().getName().toString());
        assertEquals(displayed, app.logic.getDisplayedStudentList());
        feedback("redo");
        assertEquals(displayed, app.logic.getDisplayedStudentList());

        feedback("group add g/T05");
        feedback("undo");
        assertEquals(app.logic.getActiveGroupStudentList(), app.logic.getDisplayedStudentList());
    }

    @Test
    public void execute_searchAndList_changesOnlyDisplayAndResetsOnRestart() throws Exception {
        feedback("student add n/Alice Tan id/A1");
        feedback("student add n/Ben Lim id/B2");
        TAssist previousData = new TAssist(app.model.getTAssist());
        String saved = Files.readString(dataFile);

        assertFalse(app.logic.execute("student find n/liCE id/a").hasSavedChanges());
        assertEquals(List.of("A1"), app.logic.getDisplayedStudentList().stream()
                .map(student -> student.getStudentId().toString()).toList());
        assertEquals(previousData, app.model.getTAssist());
        assertEquals(saved, Files.readString(dataFile));

        feedback("student find n/Ben");
        assertEquals("B2", app.logic.getDisplayedStudentList().getFirst().getStudentId().toString());
        feedback("student list");
        assertEquals(app.logic.getActiveGroupStudentList(), app.logic.getDisplayedStudentList());
        feedback("student find n/nobody");
        assertTrue(app.logic.getDisplayedStudentList().isEmpty());
        restart();
        assertEquals(app.logic.getActiveGroupStudentList(), app.logic.getDisplayedStudentList());
        assertEquals(saved, Files.readString(dataFile));
    }

    @Test
    public void execute_searchAndSort_errorsPreserveDisplayAndOtherFeaturesUseFullRoster() throws Exception {
        feedback("student add n/Ben Lim id/B2");
        feedback("student add n/Alice Tan id/A1");
        feedback("student find n/alice");
        feedback("student sort by/id");
        List<Student> displayed = List.copyOf(app.logic.getDisplayedStudentList());
        TAssist previousData = new TAssist(app.model.getTAssist());
        String saved = Files.readString(dataFile);
        for (String command : List.of("student find", "student find n/", "student find id/",
                "student find n/Alice n/Ben", "student find n/Alice w/1", "student sort by/grade",
                "student sort by/name by/id", "student sort", "student sort by/id now")) {
            assertThrows(ParseException.class, () -> app.logic.execute(command));
            assertEquals(displayed, app.logic.getDisplayedStudentList(), command);
        }
        assertThrows(CommandException.class, () -> app.logic.execute("student delete id/C3"));
        assertEquals(displayed, app.logic.getDisplayedStudentList());
        assertEquals(previousData, app.model.getTAssist());
        assertEquals(saved, Files.readString(dataFile));

        feedback("attendance mark id/B2 w/1 s/present");
        feedback("participation set id/B2 w/1 s/5");
        feedback("assignment add a/PS1");
        feedback("assignment submit a/PS1 id/B2 s/yes");
        feedback("assignment grade a/PS1 id/B2 m/87");
        for (String command : List.of("attendance list w/1", "participation list w/1", "assignment list a/PS1")) {
            String rows = feedback(command);
            assertTrue(rows.indexOf("Ben Lim | B2") < rows.indexOf("Alice Tan | A1"), command);
            assertTrue(rows.contains("Ben Lim | B2"), command);
        }
        GroupRecordsCsvExporter exporter = new GroupRecordsCsvExporter(temporaryFolder.resolve("exports"));
        new GroupExportCommand(exporter).execute(app.model);
        Path exportFile = exporter.getExportFile(app.model.getActiveGroup().orElseThrow().getName());
        List<String> rows = Files.readAllLines(exportFile);
        assertEquals(3, rows.size());
        assertTrue(rows.get(1).startsWith("Ben Lim,B2,"));
        assertTrue(rows.get(2).startsWith("Alice Tan,A1,"));
        assertEquals(displayed, app.logic.getDisplayedStudentList());
    }

    @Test
    public void execute_emptyRosterQueries_preservesFileAndShowsLaterAdditions() throws Exception {
        String saved = Files.readString(dataFile);
        CommandResult findResult = app.logic.execute("student find n/alice");
        assertEquals("No students in T01. Add one using: student add n/NAME id/STUDENT_ID",
                findResult.getFeedbackToUser());
        assertFalse(findResult.hasSavedChanges());
        CommandResult sortResult = app.logic.execute("student sort by/name");
        assertEquals("Sorted 0 students in T01 by name.", sortResult.getFeedbackToUser());
        assertFalse(sortResult.hasSavedChanges());
        assertEquals(saved, Files.readString(dataFile));

        feedback("student add n/Ben Lim id/B2");
        feedback("student add n/Alice Tan id/A1");
        assertEquals(List.of("A1", "B2"), app.logic.getDisplayedStudentList().stream()
                .map(student -> student.getStudentId().toString()).toList());
        restart();
        assertEquals("Students in T01:\n1. Ben Lim | B2\n2. Alice Tan | A1", feedback("student list"));
    }

    @Test
    public void execute_studentRecords_listsRecordsWithoutSavingAndFollowsChanges() throws Exception {
        feedback("student add n/Alice Tan id/A1");
        feedback("student add n/Ben Lim id/B2");
        feedback("attendance mark id/A1 w/1 s/present");
        feedback("participation set id/A1 w/1 s/4");
        feedback("assignment add a/PS1");
        feedback("assignment grade a/PS1 id/A1 m/87");
        String saved = Files.readString(dataFile);

        CommandResult result = app.logic.execute("student records id/a1");
        assertTrue(result.isRecordListing());
        assertEquals(Optional.of("student records id/A1"), result.getListingCommand());
        assertFalse(result.hasSavedChanges());
        assertTrue(result.getFeedbackToUser().startsWith("Records for Alice Tan (A1) in T01:\n"
                + "Weeks (attendance | participation score):\n"
                + "Week 1 | Present | 4\nWeek 2 | Not recorded | Not recorded\n"));
        assertTrue(result.getFeedbackToUser().endsWith("\nAssignments (submission | grade):\nPS1 | Submitted | 87"));
        assertEquals(saved, Files.readString(dataFile));

        // Rerunning the listing shows corrections, and undo and a restart restore the earlier records.
        feedback("attendance mark id/A1 w/1 s/late");
        assertTrue(feedback("student records id/A1").contains("\nWeek 1 | Late | 4\n"));
        feedback("undo");
        restart();
        assertTrue(feedback("student records id/A1").contains("\nWeek 1 | Present | 4\n"));

        // Records belong to one group, and a deleted student has none to show.
        feedback("group add g/T02");
        assertRejected("student records id/A1", CommandException.class);
        feedback("group use g/T01");
        feedback("student delete id/A1 f/yes");
        assertRejected("student records id/A1", CommandException.class);
        assertRejected("student records id/A1 now", ParseException.class);
    }

    /**
     * Asserts that the group shows Ravi s/o Kumar under the ID A0345679Z with the attendance, participation and
     * assignment records entered under the ID A0345678Z.
     */
    private void assertEditedStudentKeepsRecords() throws Exception {
        assertEquals("Students in T01:\n1. Ravi s/o Kumar | A0345679Z\n2. Ben Lim | B2", feedback("student list"));
        assertEquals("Attendance for T01, Week 1: 1 present, 0 absent, 0 late, 0 excused, 1 not recorded\n"
                + "Ravi s/o Kumar | A0345679Z | Present\n"
                + "Ben Lim | B2 | Not recorded", feedback("attendance list w/1"));
        assertEquals("Participation for T01, Week 1: 1 recorded, 1 not recorded\nRavi s/o Kumar | A0345679Z | 4\n"
                + "Ben Lim | B2 | Not recorded", feedback("participation list w/1"));
        assertEquals("PS1 records for T01:\nRavi s/o Kumar | A0345679Z | Submitted | 87\n"
                + "Ben Lim | B2 | Not submitted | -", feedback("assignment list a/PS1"));
    }
}
