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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.student.Student;
import seedu.address.testutil.StudentBuilder;
import seedu.address.testutil.TypicalGroups;

public class StudentAddCommandTest {
    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(NAME_T01);
    }

    @Test
    public void execute_newStudent_updatesActiveRosterOnly() {
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.addStudent(NAME_T01, FIONA);
        assertCommandSuccess(new StudentAddCommand(FIONA), model, "Added Fiona Lee to T01.", expected);
        assertEquals(expected.getActiveGroupStudentList(), model.getActiveGroupStudentList());
    }

    @Test
    public void execute_sameNameOrIdInAnotherGroup_succeedsWithoutWarning() {
        model.setActiveGroup(NAME_T02);
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.setActiveGroup(NAME_T02);
        expected.addStudent(NAME_T02, ALICE);
        assertCommandSuccess(new StudentAddCommand(ALICE), model, "Added Alice Tan to T02.", expected);
    }

    @Test
    public void execute_sameNameDifferentId_addsStudentWithWarning() {
        Student sameName = new StudentBuilder(FIONA).withName("Alice Tan").build();
        Student sameNameOtherCase = new StudentBuilder(FIONA).withName("alice  TAN")
                .withStudentId("A0999999Z").build();
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.addStudent(NAME_T01, sameName);
        String warning = "Warning: T01 already has a student named Alice Tan (A0123456X). "
                + "Check that the student ID is correct.";
        assertCommandSuccess(new StudentAddCommand(sameName), model, "Added Alice Tan to T01. " + warning, expected);

        expected.addStudent(NAME_T01, sameNameOtherCase);
        warning = "Warning: T01 already has a student named Alice Tan (A0123456X). "
                + "Check that the student ID is correct.";
        assertCommandSuccess(new StudentAddCommand(sameNameOtherCase), model,
                "Added alice TAN to T01. " + warning, expected);
    }

    @Test
    public void execute_canonicalUnicodeName_addsStudentWithWarningAndPreservesSpelling() {
        for (String[] names : new String[][] {{"José Tan", "Jose\u0301 Tan"}, {"Jose\u0301 Tan", "José Tan"}}) {
            Model actual = new ModelManager(model.getTAssist(), new UserPrefs());
            Student existing = new StudentBuilder(ALICE).withName(names[0]).build();
            actual.setStudent(NAME_T01, existing);
            Student added = new StudentBuilder(FIONA).withName(names[1]).build();
            Model expected = new ModelManager(actual.getTAssist(), new UserPrefs());
            expected.addStudent(NAME_T01, added);
            String message = "Added " + names[1] + " to T01. Warning: T01 already has a student named "
                    + names[0] + " (A0123456X). Check that the student ID is correct.";

            assertCommandSuccess(new StudentAddCommand(added), actual, message, expected);
            assertEquals(names[1], actual.getGroup(NAME_T01).findStudent(added.getStudentId()).orElseThrow()
                    .getName().fullName);
        }
    }

    @Test
    public void execute_duplicateIdOrNoActiveGroup_preservesData() {
        Student duplicate = new StudentBuilder(FIONA).withStudentId(" a0123456x ").build();
        assertCommandFailure(new StudentAddCommand(duplicate), model, "Student ID A0123456X already exists in T01.");
        assertCommandFailure(new StudentAddCommand(FIONA), new ModelManager(), Messages.MESSAGE_NO_ACTIVE_GROUP);
    }

    @Test
    public void nullInputs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StudentAddCommand(null));
        assertThrows(NullPointerException.class, () -> new StudentAddCommand(ALICE).execute(null));
    }

    @Test
    public void equals_comparesStudentDetails() {
        StudentAddCommand command = new StudentAddCommand(ALICE);
        assertEquals(command, command);
        assertEquals(command, new StudentAddCommand(new StudentBuilder(ALICE).build()));
        assertEquals(command.hashCode(), new StudentAddCommand(ALICE).hashCode());
        assertNotEquals(command, new StudentAddCommand(FIONA));
        assertNotEquals(command, new StudentAddCommand(new StudentBuilder(ALICE).withName("Alicia").build()));
        assertNotEquals(command, null);
        assertNotEquals(command, "student");
    }
}
