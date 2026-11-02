package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalGroups.NAME_T01;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.logic.help.HelpEntry;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.TAssist;
import seedu.address.testutil.TAssistBuilder;
import seedu.address.testutil.TypicalAssignments;
import seedu.address.testutil.TypicalGroups;

public class AssignmentCommandsIntegrationTest extends AppIntegrationTest {
    private static final String LIST_PS1_IN_T01 = "PS1 records for T01:"
            + "\nAlice Tan | A0123456X | Not submitted | -"
            + "\nBen Lim | A0234567Y | Not submitted | -"
            + "\nCarl Goh | A0345678Z | Not submitted | -";

    @BeforeEach
    public void setUp() throws Exception {
        app = openAppWith(new TAssistBuilder(TypicalGroups.getTypicalTAssist()).withActiveGroup(NAME_T01).build());
    }

    @Test
    public void execute_addListAndRestart_preservesAssignmentsPerGroup() throws Exception {
        assertEquals("Created assignment PS1 for T01.", feedback("assignment add a/PS1"));
        assertEquals("Created assignment Mid-term 1 for T01.", feedback("assignment add a/  Mid-term   1 "));
        assertEquals("Created assignment PS1.5 for T01.", feedback("assignment add a/PS1.5"));
        assertEquals("Created assignment Lab 2 (Graphs) for T01.", feedback("assignment add a/Lab 2 (Graphs)"));
        assertEquals("Created assignment Kattis: Week 3 for T01.", feedback("assignment add a/Kattis: Week 3"));
        assertEquals(LIST_PS1_IN_T01, feedback("assignment list a/ps1"));
        TAssist expected = new TAssist(app.model.getTAssist());

        restart();

        assertEquals(expected, app.model.getTAssist());
        assertEquals(List.of(TypicalAssignments.PS1.getName().fullName, "Mid-term 1", "PS1.5", "Lab 2 (Graphs)",
                        "Kattis: Week 3"),
                app.model.getActiveGroup().orElseThrow().getAssignmentList().stream()
                        .map(assignment -> assignment.getName().fullName).toList());
        assertEquals(LIST_PS1_IN_T01, feedback("assignment list a/PS1"));
        app.logic.execute("group use g/T02");
        assertRejected("assignment list a/PS1", CommandException.class);
        assertEquals("Created assignment PS1 for T02.", feedback("assignment add a/PS1"));
    }

    @Test
    public void execute_submitAndGrade_persistsStudentRecordsAfterRestart() throws Exception {
        feedback("assignment add a/PS1");
        assertEquals("Recorded Alice Tan's PS1 submission status as submitted.",
                feedback("assignment submit a/PS1 id/A0123456X s/yes"));
        assertEquals("Recorded grade 87 for Alice Tan's PS1.",
                feedback("assignment grade a/PS1 id/A0123456X m/87"));
        TAssist expected = new TAssist(app.model.getTAssist());

        restart();

        assertEquals(expected, app.model.getTAssist());
        assertEquals("PS1 records for T01:\nAlice Tan | A0123456X | Submitted | 87"
                        + "\nBen Lim | A0234567Y | Not submitted | -"
                        + "\nCarl Goh | A0345678Z | Not submitted | -",
                feedback("assignment list a/PS1"));
    }

    @Test
    public void execute_submitSeveralStudentsAndRestart_savesEveryNamedStudentOnly() throws Exception {
        feedback("assignment add a/PS1");
        CommandResult result = app.logic.execute(
                "assignment submit a/PS1 id/A0345678Z id/a0123456x id/A0345678Z s/yes");
        assertEquals("Recorded Carl Goh's PS1 submission status as submitted.\n"
                + "Recorded Alice Tan's PS1 submission status as submitted.", result.getFeedbackToUser());
        assertTrue(result.hasSavedChanges());

        restart();

        assertEquals("PS1 records for T01:\nAlice Tan | A0123456X | Submitted | -"
                        + "\nBen Lim | A0234567Y | Not submitted | -"
                        + "\nCarl Goh | A0345678Z | Submitted | -",
                feedback("assignment list a/PS1"));
    }

    @Test
    public void execute_submitSeveralStudentsWithUnknownId_savesNobody() throws Exception {
        feedback("assignment add a/PS1");
        feedback("assignment submit a/PS1 id/A0234567Y s/yes");
        assertRejected("assignment submit a/PS1 id/A0123456X id/A0456789W id/A0234567Y s/no", CommandException.class);
    }

    @Test
    public void execute_repeatedSubmitOrGrade_savesOnlyRealChanges() throws Exception {
        feedback("assignment add a/PS1");
        feedback("assignment submit a/PS1 id/A0123456X s/yes");
        feedback("assignment grade a/PS1 id/A0123456X m/87");
        String saved = Files.readString(dataFile);

        CommandResult submitted = app.logic.execute("assignment submit a/PS1 id/A0123456X s/yes");
        assertEquals("Alice Tan's PS1 submission status is already submitted. Nothing changed.",
                submitted.getFeedbackToUser());
        assertFalse(submitted.hasSavedChanges());
        CommandResult sameGrade = app.logic.execute("assignment grade a/PS1 id/A0123456X m/87");
        assertEquals("Alice Tan's PS1 grade is already 87. Nothing changed.", sameGrade.getFeedbackToUser());
        assertFalse(sameGrade.hasSavedChanges());
        assertEquals(saved, Files.readString(dataFile));

        CommandResult newGrade = app.logic.execute("assignment grade a/PS1 id/A0123456X m/60");
        assertEquals("Updated Alice Tan's PS1 grade: 87 to 60.", newGrade.getFeedbackToUser());
        assertTrue(newGrade.hasSavedChanges());
    }

    @Test
    public void execute_noWithoutSubmission_preservesModelAndSavedFile() throws Exception {
        feedback("assignment add a/PS1");
        TAssist expected = new TAssist(app.model.getTAssist());
        String saved = Files.readString(dataFile);

        for (int i = 0; i < 2; i++) {
            CommandResult result = app.logic.execute("assignment submit a/PS1 id/A0234567Y s/no");

            assertEquals("Ben Lim's PS1 submission status is already not submitted. Nothing changed.",
                    result.getFeedbackToUser());
            assertFalse(result.hasSavedChanges());
            assertEquals(expected, app.model.getTAssist());
            assertEquals(saved, Files.readString(dataFile));
            restart();
        }
    }

    @Test
    public void execute_withdrawSubmission_reportsActualGradeRemovalAndPersists() throws Exception {
        feedback("assignment add a/PS1");
        feedback("assignment submit a/PS1 id/A0123456X s/yes");
        CommandResult ungraded = app.logic.execute("assignment submit a/PS1 id/A0123456X s/no");
        assertEquals("Recorded Alice Tan's PS1 submission status as not submitted.", ungraded.getFeedbackToUser());
        assertTrue(ungraded.hasSavedChanges());
        feedback("assignment submit a/PS1 id/A0123456X s/yes");
        feedback("assignment grade a/PS1 id/A0123456X m/87");

        CommandResult graded = app.logic.execute("assignment submit a/PS1 id/A0123456X s/no");
        assertEquals("Recorded Alice Tan's PS1 submission status as not submitted; the recorded grade was cleared.",
                graded.getFeedbackToUser());
        assertTrue(graded.hasSavedChanges());
        TAssist expected = new TAssist(app.model.getTAssist());
        restart();
        assertEquals(expected, app.model.getTAssist());
        assertEquals(LIST_PS1_IN_T01, feedback("assignment list a/PS1"));

        String saved = Files.readString(dataFile);
        CommandResult repeated = app.logic.execute("assignment submit a/PS1 id/A0123456X s/no");
        assertEquals("Alice Tan's PS1 submission status is already not submitted. Nothing changed.",
                repeated.getFeedbackToUser());
        assertFalse(repeated.hasSavedChanges());
        assertEquals(expected, app.model.getTAssist());
        assertEquals(saved, Files.readString(dataFile));
    }

    @Test
    public void execute_gradeUnsubmitted_recordsSubmissionAndGradeAfterRestart() throws Exception {
        feedback("assignment add a/PS1");
        assertEquals("Recorded Ben Lim's PS1 submission status as submitted and grade 75.",
                feedback("assignment grade a/PS1 id/A0234567Y m/75"));
        assertEquals("Undone: assignment grade a/PS1 id/A0234567Y m/75", feedback("undo"));
        assertEquals(LIST_PS1_IN_T01, feedback("assignment list a/PS1"));
        feedback("redo");

        restart();

        assertEquals("PS1 records for T01:\nAlice Tan | A0123456X | Not submitted | -"
                + "\nBen Lim | A0234567Y | Submitted | 75"
                + "\nCarl Goh | A0345678Z | Not submitted | -", feedback("assignment list a/PS1"));
    }

    @Test
    public void execute_rejectedInput_preservesModelAndSavedFile() throws Exception {
        feedback("assignment add a/PS1");
        assertRejected("assignment submit a/PS1 id/A0123456X s/maybe", ParseException.class);
        assertRejected("assignment grade a/PS1 id/A0123456X m/101", ParseException.class);
        assertRejected("assignment grade a/PS1 id/A0999999Z m/87", CommandException.class);
        for (String command : List.of("assignment", "assignment remove", "assignment add", "assignment add a/",
                "assignment add a/PS_1", "assignment add a/PS1/PS2", "assignment add a/--",
                "assignment add a/" + "a".repeat(10000), "assignment add a/PS2 a/PS3", "assignment add PS2",
                "assignment add n/PS2", "assignment list a/", "assignment list a/PS2 a/PS3",
                "assignment list a/PS1!", "assignment list w/3")) {
            assertRejected(command, ParseException.class);
        }
        assertRejected("assignment add a/PS1", CommandException.class);
        assertRejected("assignment add a/ps1", CommandException.class);
        assertRejected("assignment list a/PS2", CommandException.class);
    }

    @Test
    public void execute_noActiveGroup_preservesModelAndSavedFile() throws Exception {
        app.model.setTAssist(new TAssist());
        app.storage.saveTAssist(app.model.getTAssist());
        for (String command : List.of("assignment add a/PS1", "assignment submit a/PS1 id/A0123456X s/yes",
                "assignment grade a/PS1 id/A0123456X m/87", "assignment list a/PS1", "assignment list")) {
            assertRejected(command, CommandException.class);
        }
    }

    @Test
    public void execute_listWithoutName_listsAssignmentNamesAfterRestart() throws Exception {
        assertEquals("No assignments in T01. Create one with: assignment add a/ASSIGNMENT_NAME",
                feedback("assignment list"));
        for (String command : List.of("assignment add a/PS1", "assignment add a/Lab 2 (Graphs)",
                "assignment add a/Kattis: Week 3")) {
            feedback(command);
        }
        byte[] saved = Files.readAllBytes(dataFile);

        restart();

        assertEquals("Assignments in T01:\n1. PS1\n2. Lab 2 (Graphs)\n3. Kattis: Week 3", feedback("assignment list"));
        assertArrayEquals(saved, Files.readAllBytes(dataFile), "Listing must not save");
    }

    @Test
    public void execute_duplicateAndUnknownAssignment_showSpecificMessages() throws Exception {
        feedback("assignment add a/PS1");
        assertThrows(CommandException.class, "Assignment PS1 already exists in T01.", () ->
                app.logic.execute("assignment add a/ps1"));
        assertThrows(CommandException.class, "Assignment PS2 does not exist in T01. "
                + "See the group's assignments with: assignment list\nOr create it with: assignment add a/PS2", () ->
                app.logic.execute("assignment list a/PS2"));
    }

    @Test
    public void getConfirmationPrompt_namesAssignmentWithoutChangingSavedData() throws Exception {
        feedback("assignment add a/PS1");
        TAssist previousData = new TAssist(app.model.getTAssist());
        String savedFile = Files.readString(dataFile);

        assertEquals(Optional.of("Delete assignment PS1 and all its submission and grade records from T01?"),
                app.logic.getConfirmationPrompt("assignment delete a/ps1"));
        assertEquals(app.logic.getConfirmationPrompt("assignment delete a/ps1"),
                app.logic.getConfirmationPrompt("ASSIGNMENT DeLeTe a/ps1"));
        assertEquals(Optional.empty(), app.logic.getConfirmationPrompt("assignment delete a/ps1 f/yes"));
        assertEquals(Optional.empty(), app.logic.getConfirmationPrompt("assignment delete a/PS2"));
        assertEquals(previousData, app.model.getTAssist());
        assertEquals(savedFile, Files.readString(dataFile));
    }

    @Test
    public void execute_renameAssignment_keepsRecordsPersistsAndCanBeUndone() throws Exception {
        feedback("assignment add a/PS1");
        feedback("assignment add a/PS2");
        feedback("assignment submit a/PS1 id/A0123456X s/yes");
        feedback("assignment grade a/PS1 id/A0123456X m/87");
        TAssist beforeRename = new TAssist(app.model.getTAssist());

        CommandResult renamed = app.logic.execute("assignment rename a/ps1 n/  Problem   Set 1 ");
        assertEquals("Renamed assignment PS1 to Problem Set 1 in T01.", renamed.getFeedbackToUser());
        assertTrue(renamed.hasSavedChanges());
        assertEquals("Assignments in T01:\n1. Problem Set 1\n2. PS2", feedback("assignment list"));

        assertEquals("Undone: assignment rename a/ps1 n/ Problem Set 1", feedback("undo"));
        assertEquals(beforeRename, app.model.getTAssist());
        assertEquals("Redone: assignment rename a/ps1 n/ Problem Set 1", feedback("redo"));
        restart();
        assertEquals("Assignments in T01:\n1. Problem Set 1\n2. PS2", feedback("assignment list"));
        assertEquals("Problem Set 1 records for T01:\nAlice Tan | A0123456X | Submitted | 87"
                        + "\nBen Lim | A0234567Y | Not submitted | -\nCarl Goh | A0345678Z | Not submitted | -",
                feedback("assignment list a/problem set 1"));
        assertRejected("assignment list a/PS1", CommandException.class);
    }

    @Test
    public void execute_renameAssignmentOnlyChangingCase_isSavedAndRestored() throws Exception {
        feedback("assignment add a/PS1");
        String saved = Files.readString(dataFile);

        CommandResult renamed = app.logic.execute("assignment rename a/PS1 n/ps1");
        assertEquals("Renamed assignment PS1 to ps1 in T01.", renamed.getFeedbackToUser());
        assertTrue(renamed.hasSavedChanges());
        assertNotEquals(saved, Files.readString(dataFile));
        restart();
        assertEquals("Assignments in T01:\n1. ps1", feedback("assignment list"));

        CommandResult unchanged = app.logic.execute("assignment rename a/PS1 n/ ps1 ");
        assertEquals("Assignment ps1 already has that name. Nothing changed.", unchanged.getFeedbackToUser());
        assertFalse(unchanged.hasSavedChanges());
    }

    @Test
    public void execute_renameRejected_preservesModelAndSavedFile() throws Exception {
        feedback("assignment add a/PS1");
        feedback("assignment add a/PS2");
        for (String command : List.of("assignment rename", "assignment rename a/PS1", "assignment rename n/PS3",
                "assignment rename a/PS1 n/", "assignment rename a/PS1 n/PS_3", "assignment rename a/PS1 n/PS3 n/PS4",
                "assignment rename PS1 PS3", "assignment rename a/PS1 g/PS3")) {
            assertRejected(command, ParseException.class);
        }
        assertRejected("assignment rename a/PS1 n/ps2", CommandException.class);
        assertRejected("assignment rename a/PS3 n/PS4", CommandException.class);
    }

    @Test
    public void execute_deleteAssignment_removesRecordsPersistsAndCanBeUndone() throws Exception {
        feedback("assignment add a/PS1");
        feedback("assignment add a/PS2");
        feedback("assignment submit a/PS1 id/A0123456X s/yes");
        feedback("assignment grade a/PS1 id/A0123456X m/87");
        TAssist beforeDelete = new TAssist(app.model.getTAssist());

        CommandResult deleted = app.logic.execute("assignment delete a/ps1 f/yes");
        assertEquals("Deleted assignment PS1 from T01.", deleted.getFeedbackToUser());
        assertTrue(deleted.hasSavedChanges());
        assertEquals("Assignments in T01:\n1. PS2", feedback("assignment list"));

        assertEquals("Undone: assignment delete a/ps1 f/yes", feedback("undo"));
        assertEquals(beforeDelete, app.model.getTAssist());
        assertEquals("PS1 records for T01:\nAlice Tan | A0123456X | Submitted | 87"
                        + "\nBen Lim | A0234567Y | Not submitted | -\nCarl Goh | A0345678Z | Not submitted | -",
                feedback("assignment list a/PS1"));

        assertEquals("Redone: assignment delete a/ps1 f/yes", feedback("redo"));
        restart();
        assertEquals("Assignments in T01:\n1. PS2", feedback("assignment list"));
        assertRejected("assignment list a/PS1", CommandException.class);
        assertEquals("Created assignment PS1 for T01.", feedback("assignment add a/PS1"));
        assertEquals(LIST_PS1_IN_T01, feedback("assignment list a/PS1"));
    }

    @Test
    public void execute_deleteUnknownAssignment_preservesModelAndSavedFile() throws Exception {
        feedback("assignment add a/PS1");
        for (String command : List.of("assignment delete", "assignment delete a/", "assignment delete PS1",
                "assignment delete a/PS1 f/no", "assignment delete a/PS1 f/yes now", "assignment delete a/PS1 a/PS2",
                "assignment delete a/PS1 n/PS2")) {
            assertRejected(command, ParseException.class);
        }
        assertRejected("assignment delete a/PS2", CommandException.class);
    }

    @Test
    public void init_assignmentHelp_registersFormatsAndWorkingExamples() throws Exception {
        List<HelpEntry> entries = HelpCatalog.filterByTopic(app.logic.getHelpEntries(), "assignment");
        assertEquals(List.of("assignment add a/ASSIGNMENT_NAME",
                        "assignment submit a/ASSIGNMENT_NAME id/STUDENT_ID [id/STUDENT_ID]... s/STATUS",
                        "assignment grade a/ASSIGNMENT_NAME id/STUDENT_ID m/GRADE",
                        "assignment list [a/ASSIGNMENT_NAME]", "assignment rename a/ASSIGNMENT_NAME n/NEW_NAME",
                        "assignment delete a/ASSIGNMENT_NAME [f/yes]"),
                entries.stream().map(HelpEntry::getCommandFormat).toList());
        assertEquals(CommandResult.forHelp("Showing assignment commands.", "assignment"),
                app.logic.execute("help assignment"));
        for (HelpEntry entry : entries) {
            for (String example : entry.getExamples()) {
                app.logic.execute(example);
            }
        }
    }
}
