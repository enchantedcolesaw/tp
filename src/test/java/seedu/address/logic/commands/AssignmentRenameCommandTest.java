package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAssignments.NAME_PS1;
import static seedu.address.testutil.TypicalAssignments.NAME_PS2;
import static seedu.address.testutil.TypicalAssignments.PS1;
import static seedu.address.testutil.TypicalAssignments.PS2;
import static seedu.address.testutil.TypicalAssignments.PS3;
import static seedu.address.testutil.TypicalGroups.NAME_T01;
import static seedu.address.testutil.TypicalGroups.NAME_T02;
import static seedu.address.testutil.TypicalStudents.ALICE;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.testutil.TypicalGroups;

public class AssignmentRenameCommandTest {
    private static final AssignmentName NEW_NAME = new AssignmentName("Problem Set 1");
    private static final Assignment GRADED_PS1 = PS1.withSubmissionStatus(ALICE.getStudentId(), true)
            .withGrade(ALICE.getStudentId(), 87);

    private Model modelWithActiveT01() {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(NAME_T01);
        model.addAssignment(NAME_T01, GRADED_PS1);
        model.addAssignment(NAME_T01, PS2);
        model.addAssignment(NAME_T02, PS1);
        return model;
    }

    /** Returns the result of renaming PS1 in T01, which moves a PS1 records listing to the new name. */
    private static CommandResult renamedPs1To(String newName) {
        return CommandResult.forListingRename("Renamed assignment PS1 to " + newName + " in T01.",
                "assignment list a/PS1", "assignment list a/" + newName);
    }

    @Test
    public void execute_existingAssignment_keepsRecordsAndPositionInActiveGroupOnly() {
        Model model = modelWithActiveT01();
        Model expected = modelWithActiveT01();
        expected.renameAssignment(NAME_T01, NAME_PS1, NEW_NAME);

        assertCommandSuccess(new AssignmentRenameCommand(new AssignmentName("ps1"), NEW_NAME), model,
                renamedPs1To("Problem Set 1"), expected);
        assertEquals(List.of(GRADED_PS1.withName(NEW_NAME), PS2), model.getGroup(NAME_T01).getAssignmentList());
        assertEquals(List.of(PS1), model.getGroup(NAME_T02).getAssignmentList());
    }

    @Test
    public void execute_onlyChangesCase_success() {
        Model model = modelWithActiveT01();
        Model expected = modelWithActiveT01();
        expected.renameAssignment(NAME_T01, NAME_PS1, new AssignmentName("ps1"));

        assertCommandSuccess(new AssignmentRenameCommand(NAME_PS1, new AssignmentName("ps1")), model,
                renamedPs1To("ps1"), expected);
        assertEquals("ps1", model.getGroup(NAME_T01).getAssignmentList().get(0).getName().fullName);
    }

    @Test
    public void execute_sameNameAfterNormalizing_reportsNoChanges() {
        Model model = modelWithActiveT01();
        Model expected = modelWithActiveT01();

        for (String newName : new String[] {"PS1", "  PS1 "}) {
            assertCommandSuccess(new AssignmentRenameCommand(new AssignmentName("ps1"), new AssignmentName(newName)),
                    model, "Assignment PS1 already has that name. Nothing changed.", expected);
        }
    }

    @Test
    public void execute_nameOfAnotherAssignment_reportsExistingNameAndChangesNothing() {
        Model model = modelWithActiveT01();

        assertCommandFailure(new AssignmentRenameCommand(NAME_PS1, new AssignmentName(" ps2 ")), model,
                "Assignment PS2 already exists in T01.");
        assertEquals(List.of(GRADED_PS1, PS2), model.getGroup(NAME_T01).getAssignmentList());
    }

    @Test
    public void execute_nameOfAssignmentInAnotherGroup_success() {
        AssignmentName otherGroupName = PS3.getName();
        Model model = modelWithActiveT01();
        model.addAssignment(NAME_T02, PS3);
        Model expected = modelWithActiveT01();
        expected.addAssignment(NAME_T02, PS3);
        expected.renameAssignment(NAME_T01, NAME_PS1, otherGroupName);

        assertCommandSuccess(new AssignmentRenameCommand(NAME_PS1, otherGroupName), model,
                renamedPs1To("PS3"), expected);
    }

    @Test
    public void execute_unknownAssignment_reportsErrorAndChangesNothing() {
        Model model = modelWithActiveT01();
        String message = String.format(Messages.MESSAGE_UNKNOWN_ASSIGNMENT_TO_CHANGE, "PS3", "T01");

        assertCommandFailure(new AssignmentRenameCommand(new AssignmentName("PS3"), NEW_NAME), model, message);
        assertEquals(List.of(GRADED_PS1, PS2), model.getGroup(NAME_T01).getAssignmentList());
    }

    @Test
    public void execute_noActiveGroup_reportsError() {
        assertCommandFailure(new AssignmentRenameCommand(NAME_PS1, NEW_NAME),
                new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs()),
                Messages.MESSAGE_NO_ACTIVE_GROUP);
    }

    @Test
    public void nullInputs_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AssignmentRenameCommand(null, NEW_NAME));
        assertThrows(NullPointerException.class, () -> new AssignmentRenameCommand(NAME_PS1, null));
        assertThrows(NullPointerException.class, () -> new AssignmentRenameCommand(NAME_PS1, NEW_NAME).execute(null));
    }

    @Test
    public void equals_comparesBothNamesIgnoringCase() {
        AssignmentRenameCommand command = new AssignmentRenameCommand(NAME_PS1, NEW_NAME);
        assertEquals(command, command);
        assertEquals(command, new AssignmentRenameCommand(new AssignmentName("ps1"), NEW_NAME));
        assertEquals(command.hashCode(), new AssignmentRenameCommand(new AssignmentName("ps1"), NEW_NAME).hashCode());
        assertNotEquals(command, new AssignmentRenameCommand(NAME_PS2, NEW_NAME));
        assertNotEquals(command, new AssignmentRenameCommand(NAME_PS1, NAME_PS2));
        assertNotEquals(command, new AssignmentDeleteCommand(NAME_PS1));
        assertNotEquals(command, null);
    }
}
