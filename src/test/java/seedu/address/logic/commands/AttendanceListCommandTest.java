package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.Messages.MESSAGE_NO_ACTIVE_GROUP;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertRecordListingSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.CARL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.TypicalGroups;

public class AttendanceListCommandTest {
    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(TypicalGroups.NAME_T01);
    }

    @Test
    public void execute_partialRecords_listsEveryoneInStudentOrder() {
        model.setStudent(TypicalGroups.NAME_T01, ALICE.withAttendanceRecord(3, "PRESENT"));
        model.setStudent(TypicalGroups.NAME_T01, CARL.withAttendanceRecord(3, "ABSENT"));

        assertListingUnchanged(3, "Attendance for T01, Week 3: 1 present, 1 absent, 0 late, 0 excused, 1 not recorded\n"
                + "Alice Tan | A0123456X | Present\n"
                + "Ben Lim | A0234567Y | Not recorded\n"
                + "Carl Goh | A0345678Z | Absent");
    }

    //@@author MarcusMa06-code
    @Test
    public void execute_invalidStoredStatus_showsStoredText() {
        model.setStudent(TypicalGroups.NAME_T01, ALICE.withAttendanceRecord(3, "late"));

        assertListingUnchanged(3, "Attendance for T01, Week 3: 0 present, 0 absent, 0 late, 0 excused, 2 not recorded\n"
                + "Alice Tan | A0123456X | late\n"
                + "Ben Lim | A0234567Y | Not recorded\n"
                + "Carl Goh | A0345678Z | Not recorded");
    }

    //@@author
    @Test
    public void execute_noRecordsForSelectedWeek_listsEveryoneAsNotRecorded() {
        model.setStudent(TypicalGroups.NAME_T01, ALICE.withAttendanceRecord(3, "PRESENT"));
        for (int week : new int[] {1, 13}) {
            assertListingUnchanged(week, "Attendance for T01, Week " + week + ": 0 present, 0 absent, "
                    + "0 late, 0 excused, 3 not recorded\n"
                    + "Alice Tan | A0123456X | Not recorded\n"
                    + "Ben Lim | A0234567Y | Not recorded\n"
                    + "Carl Goh | A0345678Z | Not recorded");
        }
    }

    @Test
    public void execute_otherActiveGroup_doesNotListAnotherGroupsRecords() {
        model.setStudent(TypicalGroups.NAME_T01, ALICE.withAttendanceRecord(3, "PRESENT"));
        model.setActiveGroup(TypicalGroups.NAME_T02);
        assertListingUnchanged(3, "Attendance for T02, Week 3: 0 present, 0 absent, 0 late, 0 excused, 2 not recorded\n"
                + "Daniel Ong | A0456789W | Not recorded\n"
                + "Elle Teo | A0567890U | Not recorded");
    }

    @Test
    public void execute_emptyGroup_showsNoStudentsMessage() {
        model.setActiveGroup(TypicalGroups.NAME_T03);
        assertListingUnchanged(3, "No students in T03.");
    }

    @Test
    public void execute_noActiveGroup_failsWithoutChangingData() {
        Model inactive = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        assertCommandFailure(new AttendanceListCommand(3), inactive, MESSAGE_NO_ACTIVE_GROUP);
    }

    @Test
    public void invalidInputs_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new AttendanceListCommand(0));
        assertThrows(NullPointerException.class, () -> new AttendanceListCommand(3).execute(null));
    }

    @Test
    public void equals_comparesWeeks() {
        AttendanceListCommand command = new AttendanceListCommand(3);
        assertEquals(command, command);
        assertEquals(command, new AttendanceListCommand(3));
        assertEquals(command.hashCode(), new AttendanceListCommand(3).hashCode());
        assertNotEquals(command, new AttendanceListCommand(4));
        assertNotEquals(command, null);
        assertNotEquals(command, "attendance list");
    }

    private void assertListingUnchanged(int week, String feedback) {
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        assertRecordListingSuccess(new AttendanceListCommand(week), model, feedback, expected);
    }
}
