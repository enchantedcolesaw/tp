package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_STUDENT;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalGroups.NAME_T01;
import static seedu.address.testutil.TypicalGroups.NAME_T02;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BEN;
import static seedu.address.testutil.TypicalStudents.FIONA;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.testutil.TypicalGroups;

public class ParticipationSetCommandTest {
    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(NAME_T01);
    }

    @Test
    public void execute_positiveScoreForAbsentStudent_warnsButRecords() {
        model.setStudent(NAME_T01, ALICE.withAttendanceRecord(3, "ABSENT"));
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.setStudent(NAME_T01, ALICE.withAttendanceRecord(3, "ABSENT").withParticipationRecord(3, 4));

        assertCommandSuccess(new ParticipationSetCommand(ALICE.getStudentId(), 3, 4), model,
                "Recorded participation score 4 for Alice Tan in Week 3."
                        + String.format(ParticipationSetCommand.MESSAGE_ABSENT_WARNING, "Alice Tan", "absent", 3),
                                expected);
        expected.setStudent(NAME_T01, ALICE.withAttendanceRecord(3, "ABSENT").withParticipationRecord(3, 0));
        assertCommandSuccess(new ParticipationSetCommand(ALICE.getStudentId(), 3, 0), model,
                "Updated Alice Tan's participation score for Week 3: 4 to 0.", expected);
    }

    @Test
    public void execute_firstScore_preservesOtherRecordsAndGroups() {
        Student recorded = ALICE.withAttendanceRecord(3, "present").withParticipationRecord(1, 5);
        model.setStudent(NAME_T01, recorded);
        model.addStudent(NAME_T02, recorded);
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.setStudent(NAME_T01, recorded.withParticipationRecord(3, 2));

        assertCommandSuccess(new ParticipationSetCommand(ALICE.getStudentId(), 3, 2), model,
                "Recorded participation score 2 for Alice Tan in Week 3.", expected);
        assertEquals(expected.getActiveGroupStudentList(), model.getActiveGroupStudentList());
        assertEquals(recorded, model.getGroup(NAME_T02).findStudent(ALICE.getStudentId()).orElseThrow());
    }

    @Test
    public void execute_existingScore_replacesValueAndPreservesRosterOrder() {
        model.setStudent(NAME_T01, BEN.withParticipationRecord(3, 0));
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.setStudent(NAME_T01, BEN.withParticipationRecord(3, 4));

        assertCommandSuccess(new ParticipationSetCommand(BEN.getStudentId(), 3, 4), model,
                "Updated Ben Lim's participation score for Week 3: 0 to 4.", expected);
        assertEquals(BEN.getStudentId(), model.getActiveGroupStudentList().get(1).getStudentId());
        assertCommandSuccess(new ParticipationSetCommand(BEN.getStudentId(), 3, 4), model,
                "Ben Lim's participation score for Week 3 is already 4. Nothing changed.", expected);
    }

    @Test
    public void execute_severalStudents_setsAllAndReportsEachInOrder() {
        model.setStudent(NAME_T01, BEN.withParticipationRecord(3, 1));
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.setStudent(NAME_T01, ALICE.withParticipationRecord(3, 4));
        expected.setStudent(NAME_T01, BEN.withParticipationRecord(3, 4));

        assertCommandSuccess(new ParticipationSetCommand(List.of(ALICE.getStudentId(), BEN.getStudentId()), 3, 4),
                model, "Recorded participation score 4 for Alice Tan in Week 3.\n"
                        + "Updated Ben Lim's participation score for Week 3: 1 to 4.", expected);
    }

    @Test
    public void execute_oneUnknownStudent_failsWithoutSettingAnyone() {
        Model unchanged = new ModelManager(model.getTAssist(), new UserPrefs());
        assertCommandFailure(new ParticipationSetCommand(List.of(ALICE.getStudentId(), FIONA.getStudentId()), 3, 2),
                model, String.format(MESSAGE_UNKNOWN_STUDENT, FIONA.getStudentId(), NAME_T01));
        assertEquals(unchanged, model);
    }

    @Test
    public void execute_boundaryValues_succeeds() {
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.setStudent(NAME_T01, ALICE.withParticipationRecord(1, 0));
        assertCommandSuccess(new ParticipationSetCommand(ALICE.getStudentId(), 1, 0), model,
                "Recorded participation score 0 for Alice Tan in Week 1.", expected);
        expected.setStudent(NAME_T01, ALICE.withParticipationRecord(1, 0).withParticipationRecord(13, 5));
        assertCommandSuccess(new ParticipationSetCommand(ALICE.getStudentId(), 13, 5), model,
                "Recorded participation score 5 for Alice Tan in Week 13.", expected);
    }

    @Test
    public void execute_missingStudentOrActiveGroup_preservesData() {
        assertCommandFailure(new ParticipationSetCommand(FIONA.getStudentId(), 3, 2), model,
                String.format(MESSAGE_UNKNOWN_STUDENT, FIONA.getStudentId(), NAME_T01));
        model.setActiveGroup(NAME_T02);
        assertCommandFailure(new ParticipationSetCommand(ALICE.getStudentId(), 3, 2), model,
                String.format(MESSAGE_UNKNOWN_STUDENT, ALICE.getStudentId(), NAME_T02));
        assertCommandFailure(new ParticipationSetCommand(ALICE.getStudentId(), 3, 2), new ModelManager(),
                Messages.MESSAGE_NO_ACTIVE_GROUP);
    }

    @Test
    public void constructor_invalidValues_throwsIllegalArgumentException() {
        for (int week : List.of(0, 14, Integer.MIN_VALUE, Integer.MAX_VALUE)) {
            assertThrows(IllegalArgumentException.class, () -> new ParticipationSetCommand(
                    ALICE.getStudentId(), week, 2));
        }
        assertThrows(IllegalArgumentException.class, () -> new ParticipationSetCommand(List.of(), 3, 2));
        for (int score : List.of(-1, 6, Integer.MIN_VALUE, Integer.MAX_VALUE)) {
            assertThrows(IllegalArgumentException.class, () -> new ParticipationSetCommand(
                    ALICE.getStudentId(), 3, score));
        }
    }

    @Test
    public void nullInputs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ParticipationSetCommand((StudentId) null, 3, 2));
        assertThrows(NullPointerException.class, () -> new ParticipationSetCommand((List<StudentId>) null, 3, 2));
        assertThrows(NullPointerException.class, () -> new ParticipationSetCommand(ALICE.getStudentId(), 3, 2)
                .execute(null));
    }

    @Test
    public void equals_comparesIdWeekAndScore() {
        ParticipationSetCommand command = new ParticipationSetCommand(ALICE.getStudentId(), 3, 2);
        assertEquals(command, command);
        assertEquals(command, new ParticipationSetCommand(ALICE.getStudentId(), 3, 2));
        assertEquals(command.hashCode(), new ParticipationSetCommand(ALICE.getStudentId(), 3, 2).hashCode());
        assertNotEquals(command, new ParticipationSetCommand(BEN.getStudentId(), 3, 2));
        assertNotEquals(command, new ParticipationSetCommand(
                List.of(ALICE.getStudentId(), BEN.getStudentId()), 3, 2));
        assertNotEquals(command, new ParticipationSetCommand(ALICE.getStudentId(), 4, 2));
        assertNotEquals(command, new ParticipationSetCommand(ALICE.getStudentId(), 3, 4));
        assertNotEquals(command, null);
        assertNotEquals(command, "participation");
    }
}
