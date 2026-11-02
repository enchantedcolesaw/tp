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
import static seedu.address.testutil.TypicalGroups.NAME_T01;
import static seedu.address.testutil.TypicalGroups.NAME_T02;
import static seedu.address.testutil.TypicalStudents.ALICE;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.testutil.TypicalGroups;

public class AssignmentDeleteCommandTest {

    private Model modelWithActiveT01() {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(NAME_T01);
        model.addAssignment(NAME_T01, PS1.withSubmissionStatus(ALICE.getStudentId(), true));
        model.addAssignment(NAME_T01, PS2);
        model.addAssignment(NAME_T02, PS1);
        return model;
    }

    @Test
    public void execute_existingAssignment_removesItWithRecordsFromActiveGroupOnly() {
        Model model = modelWithActiveT01();
        Model expected = modelWithActiveT01();
        expected.removeAssignment(NAME_T01, NAME_PS1);

        assertCommandSuccess(new AssignmentDeleteCommand(new AssignmentName("ps1")), model,
                "Deleted assignment PS1 from T01.", expected);
        assertEquals(List.of(PS2), model.getGroup(NAME_T01).getAssignmentList());
        assertEquals(List.of(PS1), model.getGroup(NAME_T02).getAssignmentList());
    }

    @Test
    public void execute_afterDeletion_nameCanBeCreatedAgainWithoutOldRecords() throws Exception {
        Model model = modelWithActiveT01();
        new AssignmentDeleteCommand(NAME_PS1).execute(model);

        new AssignmentAddCommand(NAME_PS1).execute(model);

        assertEquals(List.of(PS2, PS1), model.getGroup(NAME_T01).getAssignmentList());
    }

    @Test
    public void execute_unknownAssignment_reportsErrorAndChangesNothing() {
        Model model = modelWithActiveT01();
        String message = String.format(Messages.MESSAGE_UNKNOWN_ASSIGNMENT_TO_CHANGE, "PS3", "T01");

        assertCommandFailure(new AssignmentDeleteCommand(new AssignmentName("PS3")), model, message);
        assertEquals(List.of(PS1.withSubmissionStatus(ALICE.getStudentId(), true), PS2),
                model.getGroup(NAME_T01).getAssignmentList());
    }

    @Test
    public void execute_noActiveGroup_reportsError() {
        assertCommandFailure(new AssignmentDeleteCommand(NAME_PS1),
                new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs()),
                Messages.MESSAGE_NO_ACTIVE_GROUP);
    }

    @Test
    public void getConfirmationPrompt_namesAssignmentAndGroupAndCanBeSkipped() {
        Model model = modelWithActiveT01();

        assertEquals(Optional.of("Delete assignment PS1 and all its submission and grade records from T01?"),
                new AssignmentDeleteCommand(new AssignmentName("ps1")).getConfirmationPrompt(model));
        assertEquals(Optional.empty(), new AssignmentDeleteCommand(NAME_PS1, true).getConfirmationPrompt(model));
    }

    @Test
    public void getConfirmationPrompt_nothingToDelete_isEmpty() {
        assertEquals(Optional.empty(), new AssignmentDeleteCommand(new AssignmentName("PS3"))
                .getConfirmationPrompt(modelWithActiveT01()));
        assertEquals(Optional.empty(), new AssignmentDeleteCommand(NAME_PS1)
                .getConfirmationPrompt(new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs())));
    }

    @Test
    public void nullInputs_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AssignmentDeleteCommand(null));
        assertThrows(NullPointerException.class, () -> new AssignmentDeleteCommand(NAME_PS1).execute(null));
        assertThrows(NullPointerException.class, () ->
                new AssignmentDeleteCommand(NAME_PS1).getConfirmationPrompt(null));
    }

    @Test
    public void equals_comparesNameIgnoringCaseAndFastDeleteChoice() {
        AssignmentDeleteCommand command = new AssignmentDeleteCommand(NAME_PS1);
        assertEquals(command, command);
        assertEquals(command, new AssignmentDeleteCommand(new AssignmentName("ps1")));
        assertEquals(command.hashCode(), new AssignmentDeleteCommand(new AssignmentName("ps1")).hashCode());
        assertNotEquals(command, new AssignmentDeleteCommand(NAME_PS2));
        assertNotEquals(command, new AssignmentDeleteCommand(NAME_PS1, true));
        assertNotEquals(command, new AssignmentAddCommand(NAME_PS1));
        assertNotEquals(command, null);
    }
}
