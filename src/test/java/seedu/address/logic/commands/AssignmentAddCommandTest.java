package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAssignments.NAME_PS1;
import static seedu.address.testutil.TypicalAssignments.NAME_PS2;
import static seedu.address.testutil.TypicalAssignments.PS1;
import static seedu.address.testutil.TypicalGroups.NAME_T01;
import static seedu.address.testutil.TypicalGroups.NAME_T02;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.testutil.TypicalGroups;

public class AssignmentAddCommandTest {

    private static Model withAssignment(Model model, AssignmentName name) {
        model.addAssignment(NAME_T01, new Assignment(name));
        return model;
    }

    private Model modelWithActiveT01() {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(NAME_T01);
        return model;
    }

    @Test
    public void execute_newAssignment_addedToActiveGroupOnly() {
        Model model = modelWithActiveT01();
        Model expected = modelWithActiveT01();
        expected.addAssignment(NAME_T01, PS1);

        assertCommandSuccess(new AssignmentAddCommand(NAME_PS1), model, "Created assignment PS1 for T01.", expected);
        assertEquals(List.of(PS1), model.getGroup(NAME_T01).getAssignmentList());
        assertEquals(List.of(), model.getGroup(NAME_T02).getAssignmentList());
    }

    @Test
    public void execute_nameWithDifferentCapitalization_keepsEnteredCapitalization() {
        Model model = modelWithActiveT01();
        AssignmentName name = new AssignmentName("problem  set-1");
        Model expected = modelWithActiveT01();
        expected.addAssignment(NAME_T01, new Assignment(name));

        assertCommandSuccess(new AssignmentAddCommand(name), model,
                "Created assignment problem set-1 for T01.", expected);
    }

    @Test
    public void execute_similarNameInSameGroup_warnsButCreates() {
        Model model = modelWithActiveT01();
        model.addAssignment(NAME_T01, PS1);
        AssignmentName similar = new AssignmentName("ps-1");
        Model expected = modelWithActiveT01();
        expected.addAssignment(NAME_T01, PS1);
        expected.addAssignment(NAME_T01, new Assignment(similar));

        assertCommandSuccess(new AssignmentAddCommand(similar), model, "Created assignment ps-1 for T01."
                + String.format(AssignmentAddCommand.MESSAGE_SIMILAR_WARNING, "T01", "PS1"), expected);
        assertCommandSuccess(new AssignmentAddCommand(NAME_PS2), model, "Created assignment PS2 for T01.",
                withAssignment(expected, NAME_PS2));
    }

    @Test
    public void execute_sameNameInAnotherGroup_success() {
        Model model = modelWithActiveT01();
        model.addAssignment(NAME_T02, PS1);
        Model expected = modelWithActiveT01();
        expected.addAssignment(NAME_T02, PS1);
        expected.addAssignment(NAME_T01, PS1);

        assertCommandSuccess(new AssignmentAddCommand(NAME_PS1), model, "Created assignment PS1 for T01.", expected);
    }

    @Test
    public void execute_duplicateNameIgnoringCase_reportsExistingNameAndChangesNothing() {
        Model model = modelWithActiveT01();
        model.addAssignment(NAME_T01, PS1);

        assertCommandFailure(new AssignmentAddCommand(NAME_PS1), model, "Assignment PS1 already exists in T01.");
        assertCommandFailure(new AssignmentAddCommand(new AssignmentName("ps1")), model,
                "Assignment PS1 already exists in T01.");
        assertEquals(List.of(PS1), model.getGroup(NAME_T01).getAssignmentList());
    }

    @Test
    public void execute_noActiveGroup_reportsError() {
        assertCommandFailure(new AssignmentAddCommand(NAME_PS1),
                new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs()),
                Messages.MESSAGE_NO_ACTIVE_GROUP);
    }

    @Test
    public void nullInputs_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AssignmentAddCommand(null));
        assertThrows(NullPointerException.class, () -> new AssignmentAddCommand(NAME_PS1).execute(null));
    }

    @Test
    public void equals_comparesAssignmentNameIgnoringCase() {
        AssignmentAddCommand command = new AssignmentAddCommand(NAME_PS1);
        assertEquals(command, command);
        assertEquals(command, new AssignmentAddCommand(new AssignmentName("ps1")));
        assertEquals(command.hashCode(), new AssignmentAddCommand(new AssignmentName("ps1")).hashCode());
        assertNotEquals(command, new AssignmentAddCommand(NAME_PS2));
        assertNotEquals(command, null);
        assertNotEquals(command, "assignment add");
    }
}
