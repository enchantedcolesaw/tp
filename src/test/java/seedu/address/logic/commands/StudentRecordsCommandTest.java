package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BEN;
import static seedu.address.testutil.TypicalStudents.DANIEL;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.TAssist;
import seedu.address.model.UserPrefs;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.model.student.StudentId;
import seedu.address.testutil.TypicalGroups;

public class StudentRecordsCommandTest {
    private static final String NOT_RECORDED_WEEK = " | Not recorded | Not recorded";

    @Test
    public void execute_recordsAndAssignments_listsEveryWeekAndAssignmentWithoutChangingData() throws Exception {
        Model model = getModelWithT01Active();
        model.setStudent(TypicalGroups.NAME_T01, ALICE.withAttendanceRecord(1, "PRESENT")
                .withAttendanceRecord(2, "LATE").withAttendanceRecord(13, "EXCUSED")
                .withParticipationRecord(1, 3).withParticipationRecord(3, 0));
        model.addAssignment(TypicalGroups.NAME_T01, new Assignment(new AssignmentName("PS1"))
                .withSubmissionStatus(ALICE.getStudentId(), true).withGrade(ALICE.getStudentId(), 87));
        model.addAssignment(TypicalGroups.NAME_T01, new Assignment(new AssignmentName("PS2"))
                .withSubmissionStatus(ALICE.getStudentId(), true));
        model.addAssignment(TypicalGroups.NAME_T01, new Assignment(new AssignmentName("Lab 3")));
        TAssist previousData = new TAssist(model.getTAssist());

        StringBuilder expected = new StringBuilder("Records for Alice Tan (A0123456X) in T01:\n"
                + StudentRecordsCommand.MESSAGE_WEEKS_HEADING + "\n"
                + "Week 1 | Present | 3\n"
                + "Week 2 | Late | Not recorded\n"
                + "Week 3 | Not recorded | 0");
        for (int week = 4; week <= 12; week++) {
            expected.append("\nWeek ").append(week).append(NOT_RECORDED_WEEK);
        }
        expected.append("\nWeek 13 | Excused | Not recorded\n")
                .append(StudentRecordsCommand.MESSAGE_ASSIGNMENTS_HEADING).append('\n')
                .append("PS1 | Submitted | 87\n")
                .append("PS2 | Submitted | -\n")
                .append("Lab 3 | Not submitted | -");

        // IDs ignore case, and the listing reruns with the stored ID.
        CommandResult result = new StudentRecordsCommand(new StudentId("a0123456x")).execute(model);
        assertEquals(CommandResult.forRecordListing(expected.toString(), "student records id/A0123456X"), result);
        assertTrue(result.isRecordListing());
        assertFalse(result.hasSavedChanges());
        assertEquals(previousData, model.getTAssist());
    }

    @Test
    public void execute_noRecordsOrAssignments_listsEveryWeekAsNotRecorded() throws Exception {
        Model model = getModelWithT01Active();
        StringBuilder expected = new StringBuilder("Records for Ben Lim (A0234567Y) in T01:\n"
                + StudentRecordsCommand.MESSAGE_WEEKS_HEADING);
        for (int week = 1; week <= 13; week++) {
            expected.append("\nWeek ").append(week).append(NOT_RECORDED_WEEK);
        }
        expected.append("\nAssignments: none in T01.");

        assertEquals(CommandResult.forRecordListing(expected.toString(), "student records id/A0234567Y"),
                new StudentRecordsCommand(BEN.getStudentId()).execute(model));
    }

    @Test
    public void execute_studentNotInActiveGroup_throwsCommandException() {
        Model model = getModelWithT01Active();
        // Daniel belongs to T02, so T01 cannot show his records.
        assertCommandFailure(new StudentRecordsCommand(DANIEL.getStudentId()), model,
                String.format(Messages.MESSAGE_UNKNOWN_STUDENT, DANIEL.getStudentId(), TypicalGroups.NAME_T01));
    }

    @Test
    public void execute_noActiveGroup_throwsCommandException() {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        assertCommandFailure(new StudentRecordsCommand(ALICE.getStudentId()), model, Messages.MESSAGE_NO_ACTIVE_GROUP);
    }

    @Test
    public void constructor_nullStudentId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StudentRecordsCommand(null));
    }

    @Test
    public void equals() {
        StudentRecordsCommand aliceCommand = new StudentRecordsCommand(ALICE.getStudentId());
        assertEquals(aliceCommand, aliceCommand);
        assertEquals(aliceCommand, new StudentRecordsCommand(new StudentId("a0123456x")));
        assertEquals(aliceCommand.hashCode(), new StudentRecordsCommand(ALICE.getStudentId()).hashCode());
        assertNotEquals(aliceCommand, new StudentRecordsCommand(BEN.getStudentId()));
        assertNotEquals(aliceCommand, null);
        assertNotEquals(aliceCommand, new StudentListCommand());
    }

    private static Model getModelWithT01Active() {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(TypicalGroups.NAME_T01);
        return model;
    }
}
