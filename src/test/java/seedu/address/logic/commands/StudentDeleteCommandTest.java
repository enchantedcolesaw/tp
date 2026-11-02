package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalGroups.NAME_T01;
import static seedu.address.testutil.TypicalGroups.NAME_T02;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.FIONA;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.testutil.TypicalGroups;

public class StudentDeleteCommandTest {
    @Test
    public void execute_existingStudent_removesRecordsOnlyInActiveGroup() {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(NAME_T01);
        Student recorded = ALICE.withAttendanceRecord(3, "present").withParticipationRecord(3, 4);
        model.setStudent(NAME_T01, recorded);
        model.addStudent(NAME_T02, recorded);
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.removeStudent(NAME_T01, ALICE.getStudentId());

        assertCommandSuccess(new StudentDeleteCommand(new StudentId("a0123456x")), model,
                "Removed Alice Tan (A0123456X) from T01.", expected);
        assertEquals(expected.getActiveGroupStudentList(), model.getActiveGroupStudentList());
        assertEquals(recorded, model.getGroup(NAME_T02).findStudent(ALICE.getStudentId()).orElseThrow());
    }

    @Test
    public void execute_unknownStudentOrNoActiveGroup_preservesData() {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        StudentDeleteCommand command = new StudentDeleteCommand(ALICE.getStudentId());
        assertCommandFailure(command, model, Messages.MESSAGE_NO_ACTIVE_GROUP);
        model.setActiveGroup(NAME_T02);
        assertCommandFailure(command, model, "Student ID A0123456X does not exist in T02. "
                + "See students in the active group with: student list");
    }

    @Test
    public void getConfirmationPrompt_namesStudentAndCanBeSkipped() {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(NAME_T01);

        assertEquals(Optional.of("Remove Alice Tan (A0123456X) and all their attendance, participation, "
                        + "submission, and grade records from T01?"),
                new StudentDeleteCommand(ALICE.getStudentId()).getConfirmationPrompt(model));
        assertEquals(Optional.empty(),
                new StudentDeleteCommand(ALICE.getStudentId(), true).getConfirmationPrompt(model));
        assertEquals(Optional.empty(),
                new StudentDeleteCommand(FIONA.getStudentId()).getConfirmationPrompt(model));
    }

    @Test
    public void nullInputs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StudentDeleteCommand(null));
        assertThrows(NullPointerException.class, () -> new StudentDeleteCommand(ALICE.getStudentId()).execute(null));
    }

    @Test
    public void equals_comparesStudentIds() {
        StudentDeleteCommand command = new StudentDeleteCommand(ALICE.getStudentId());
        assertEquals(command, command);
        assertEquals(command, new StudentDeleteCommand(new StudentId("a0123456x")));
        assertEquals(command.hashCode(), new StudentDeleteCommand(ALICE.getStudentId()).hashCode());
        assertNotEquals(command, new StudentDeleteCommand(FIONA.getStudentId()));
        assertNotEquals(command, new StudentDeleteCommand(ALICE.getStudentId(), true));
        assertNotEquals(command, null);
        assertNotEquals(command, "student");
    }
}
