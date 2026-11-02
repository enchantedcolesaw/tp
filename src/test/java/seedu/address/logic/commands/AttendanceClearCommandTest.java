package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.Messages.MESSAGE_NO_ACTIVE_GROUP;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_STUDENT;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BEN;
import static seedu.address.testutil.TypicalStudents.CARL;
import static seedu.address.testutil.TypicalStudents.DANIEL;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.testutil.TypicalGroups;

public class AttendanceClearCommandTest {
    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(TypicalGroups.NAME_T01);
    }

    @Test
    public void execute_recordedWeek_clearsOnlyThatWeekAndShowsOldStatus() {
        Student recorded = ALICE.withAttendanceRecord(3, "PRESENT").withAttendanceRecord(4, "ABSENT")
                .withParticipationRecord(4, 2);
        model.setStudent(TypicalGroups.NAME_T01, recorded);
        model.setStudent(TypicalGroups.NAME_T01, BEN.withAttendanceRecord(4, "PRESENT"));
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.setStudent(TypicalGroups.NAME_T01, recorded.withoutAttendanceRecord(4));

        assertCommandSuccess(new AttendanceClearCommand(ALICE.getStudentId(), 4), model,
                "Cleared Alice Tan's attendance for Week 4: absent to Not recorded.", expected);
    }

    @Test
    public void execute_presentAtWeekBoundaries_clearsMark() {
        for (int week : new int[] {1, 13}) {
            setUp();
            model.setStudent(TypicalGroups.NAME_T01, ALICE.withAttendanceRecord(week, "PRESENT"));
            Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
            expected.setStudent(TypicalGroups.NAME_T01, ALICE);

            assertCommandSuccess(new AttendanceClearCommand(ALICE.getStudentId(), week), model,
                    "Cleared Alice Tan's attendance for Week " + week + ": present to Not recorded.", expected);
        }
    }

    @Test
    public void execute_unrecordedWeek_changesNothing() {
        model.setStudent(TypicalGroups.NAME_T01, ALICE.withAttendanceRecord(3, "PRESENT"));
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        AttendanceClearCommand command = new AttendanceClearCommand(ALICE.getStudentId(), 4);

        assertCommandSuccess(command, model, "Alice Tan has no attendance recorded for Week 4. Nothing changed.",
                expected);
    }

    @Test
    public void execute_unrecognizedStoredStatus_clearsMarkWithoutFailing() {
        model.setStudent(TypicalGroups.NAME_T01, ALICE.withAttendanceRecord(3, "LATE"));
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.setStudent(TypicalGroups.NAME_T01, ALICE);

        assertCommandSuccess(new AttendanceClearCommand(ALICE.getStudentId(), 3), model,
                "Cleared Alice Tan's attendance for Week 3: late to Not recorded.", expected);
    }

    @Test
    public void execute_twice_secondChangesNothing() {
        model.setStudent(TypicalGroups.NAME_T01, ALICE.withAttendanceRecord(3, "ABSENT"));
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.setStudent(TypicalGroups.NAME_T01, ALICE);
        AttendanceClearCommand command = new AttendanceClearCommand(ALICE.getStudentId(), 3);

        assertCommandSuccess(command, model, "Cleared Alice Tan's attendance for Week 3: absent to Not recorded.",
                expected);
        assertCommandSuccess(command, model, "Alice Tan has no attendance recorded for Week 3. Nothing changed.",
                expected);
    }

    @Test
    public void execute_severalStudents_clearsEachAndReportsEachInOrder() {
        model.setStudent(TypicalGroups.NAME_T01, ALICE.withAttendanceRecord(3, "ABSENT"));
        model.setStudent(TypicalGroups.NAME_T01,
                BEN.withAttendanceRecord(3, "PRESENT").withAttendanceRecord(4, "ABSENT"));
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.setStudent(TypicalGroups.NAME_T01, ALICE);
        expected.setStudent(TypicalGroups.NAME_T01, BEN.withAttendanceRecord(4, "ABSENT"));

        assertCommandSuccess(new AttendanceClearCommand(List.of(BEN.getStudentId(), CARL.getStudentId(),
                ALICE.getStudentId()), 3), model, "Cleared Ben Lim's attendance for Week 3: present to Not recorded.\n"
                + "Carl Goh has no attendance recorded for Week 3. Nothing changed.\n"
                + "Cleared Alice Tan's attendance for Week 3: absent to Not recorded.", expected);
    }

    @Test
    public void execute_severalStudentsWithUnknownLast_failsWithoutClearingAny() {
        model.setStudent(TypicalGroups.NAME_T01, ALICE.withAttendanceRecord(3, "ABSENT"));
        assertCommandFailure(new AttendanceClearCommand(List.of(ALICE.getStudentId(), DANIEL.getStudentId()), 3),
                model, String.format(MESSAGE_UNKNOWN_STUDENT, DANIEL.getStudentId(), TypicalGroups.NAME_T01));
    }

    @Test
    public void execute_studentInAnotherGroup_failsWithoutChangingData() {
        assertCommandFailure(new AttendanceClearCommand(DANIEL.getStudentId(), 3), model,
                String.format(MESSAGE_UNKNOWN_STUDENT, DANIEL.getStudentId(), TypicalGroups.NAME_T01));
    }

    @Test
    public void execute_noActiveGroup_failsWithoutChangingData() {
        Model inactive = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        assertCommandFailure(new AttendanceClearCommand(ALICE.getStudentId(), 3), inactive, MESSAGE_NO_ACTIVE_GROUP);
    }

    @Test
    public void invalidConstruction_throwsException() {
        assertThrows(NullPointerException.class, () -> new AttendanceClearCommand((StudentId) null, 3));
        assertThrows(NullPointerException.class, () -> new AttendanceClearCommand((List<StudentId>) null, 3));
        assertThrows(IllegalArgumentException.class, () -> new AttendanceClearCommand(List.of(), 3));
        assertThrows(IllegalArgumentException.class, () -> new AttendanceClearCommand(ALICE.getStudentId(), 0));
        assertThrows(IllegalArgumentException.class, () -> new AttendanceClearCommand(ALICE.getStudentId(), 14));
    }

    @Test
    public void equals_comparesStudentAndWeek() {
        AttendanceClearCommand command = new AttendanceClearCommand(ALICE.getStudentId(), 3);
        AttendanceClearCommand equal = new AttendanceClearCommand(ALICE.getStudentId(), 3);
        assertEquals(command, command);
        assertEquals(command, equal);
        assertEquals(command.hashCode(), equal.hashCode());
        assertNotEquals(command, new AttendanceClearCommand(DANIEL.getStudentId(), 3));
        assertNotEquals(command, new AttendanceClearCommand(ALICE.getStudentId(), 4));
        assertEquals(command, new AttendanceClearCommand(List.of(ALICE.getStudentId()), 3));
        assertNotEquals(command, new AttendanceClearCommand(List.of(ALICE.getStudentId(), BEN.getStudentId()), 3));
        assertNotEquals(command, null);
        assertNotEquals(command, "attendance clear");
    }
}
