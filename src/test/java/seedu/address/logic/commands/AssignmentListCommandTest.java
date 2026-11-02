package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.assertRecordListingSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAssignments.NAME_PS1;
import static seedu.address.testutil.TypicalAssignments.NAME_PS2;
import static seedu.address.testutil.TypicalAssignments.PS1;
import static seedu.address.testutil.TypicalGroups.NAME_T01;
import static seedu.address.testutil.TypicalGroups.NAME_T02;
import static seedu.address.testutil.TypicalGroups.NAME_T03;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.model.group.GroupName;
import seedu.address.model.student.StudentId;
import seedu.address.testutil.TypicalGroups;

public class AssignmentListCommandTest {

    private Model modelWithActiveGroup(GroupName activeGroup) {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.addAssignment(NAME_T01, PS1);
        model.setActiveGroup(activeGroup);
        return model;
    }

    private static void assertPs1ListingSuccess(AssignmentName typedName, Model model, String listing,
            Model expected) {
        assertCommandSuccess(new AssignmentListCommand(typedName), model,
                CommandResult.forRecordListing(listing, "assignment list a/PS1"), expected);
    }

    @Test
    public void execute_existingAssignment_listsEveryStudentAsNotSubmittedWithoutChangingData() {
        Model model = modelWithActiveGroup(NAME_T01);
        Model expected = modelWithActiveGroup(NAME_T01);
        assertPs1ListingSuccess(NAME_PS1, model,
                "PS1 records for T01:\nAlice Tan | A0123456X | Not submitted | -"
                        + "\nBen Lim | A0234567Y | Not submitted | -"
                        + "\nCarl Goh | A0345678Z | Not submitted | -", expected);
    }

    @Test
    public void execute_nameInDifferentCase_showsStoredNameAndFindsAssignment() {
        Model model = modelWithActiveGroup(NAME_T01);
        Model expected = modelWithActiveGroup(NAME_T01);
        assertPs1ListingSuccess(new AssignmentName("ps1"), model,
                "PS1 records for T01:\nAlice Tan | A0123456X | Not submitted | -"
                        + "\nBen Lim | A0234567Y | Not submitted | -"
                        + "\nCarl Goh | A0345678Z | Not submitted | -", expected);
    }

    @Test
    public void execute_recordedValues_listsSubmissionAndGrade() {
        StudentId aliceId = new StudentId("A0123456X");
        StudentId benId = new StudentId("A0234567Y");
        Assignment assignment = PS1.withSubmissionStatus(aliceId, true).withGrade(aliceId, 87)
                .withSubmissionStatus(benId, true);
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(NAME_T01);
        model.addAssignment(NAME_T01, assignment);
        Model expected = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        expected.setActiveGroup(NAME_T01);
        expected.addAssignment(NAME_T01, assignment);

        assertPs1ListingSuccess(NAME_PS1, model,
                "PS1 records for T01:\nAlice Tan | A0123456X | Submitted | 87"
                        + "\nBen Lim | A0234567Y | Submitted | -"
                        + "\nCarl Goh | A0345678Z | Not submitted | -", expected);
    }

    @Test
    public void execute_assignmentWithNoStudents_reportsNoStudents() {
        Model model = modelWithActiveGroup(NAME_T03);
        model.addAssignment(NAME_T03, PS1);
        Model expected = modelWithActiveGroup(NAME_T03);
        expected.addAssignment(NAME_T03, PS1);
        assertPs1ListingSuccess(NAME_PS1, model, "No students in T03.", expected);
    }

    @Test
    public void execute_unknownAssignment_reportsSpecificError() {
        assertCommandFailure(new AssignmentListCommand(NAME_PS2), modelWithActiveGroup(NAME_T01),
                "Assignment PS2 does not exist in T01. See the group's assignments with: assignment list\n"
                        + "Or create it with: assignment add a/PS2");
    }

    @Test
    public void execute_assignmentOnlyInAnotherGroup_reportsUnknownAssignment() {
        assertCommandFailure(new AssignmentListCommand(NAME_PS1), modelWithActiveGroup(NAME_T02),
                "Assignment PS1 does not exist in T02. See the group's assignments with: assignment list\n"
                        + "Or create it with: assignment add a/PS1");
    }

    @Test
    public void execute_noName_listsAssignmentsInCreationOrderWithoutChangingData() {
        Model model = modelWithActiveGroup(NAME_T01);
        model.addAssignment(NAME_T01, new Assignment(new AssignmentName("Lab 2 (Graphs)")));
        Model expected = modelWithActiveGroup(NAME_T01);
        expected.addAssignment(NAME_T01, new Assignment(new AssignmentName("Lab 2 (Graphs)")));
        assertRecordListingSuccess(new AssignmentListCommand(), model, "Assignments in T01:\n1. PS1\n2. Lab 2 (Graphs)",
                expected);
    }

    @Test
    public void execute_noNameAndNoAssignments_reportsHowToCreateOne() {
        Model model = modelWithActiveGroup(NAME_T02);
        Model expected = modelWithActiveGroup(NAME_T02);
        assertRecordListingSuccess(new AssignmentListCommand(), model,
                "No assignments in T02. Create one with: assignment add a/ASSIGNMENT_NAME", expected);
    }

    @Test
    public void execute_noActiveGroup_reportsError() {
        assertCommandFailure(new AssignmentListCommand(NAME_PS1), new ModelManager(), Messages.MESSAGE_NO_ACTIVE_GROUP);
        assertCommandFailure(new AssignmentListCommand(), new ModelManager(), Messages.MESSAGE_NO_ACTIVE_GROUP);
    }

    @Test
    public void nullInputs_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AssignmentListCommand(null));
        assertThrows(NullPointerException.class, () -> new AssignmentListCommand(NAME_PS1).execute(null));
    }

    @Test
    public void equals_comparesAssignmentNameIgnoringCase() {
        AssignmentListCommand command = new AssignmentListCommand(NAME_PS1);
        assertEquals(command, command);
        assertEquals(command, new AssignmentListCommand(new AssignmentName("ps1")));
        assertEquals(command.hashCode(), new AssignmentListCommand(new AssignmentName("ps1")).hashCode());
        assertNotEquals(command, new AssignmentListCommand(NAME_PS2));
        assertNotEquals(command, new AssignmentListCommand());
        assertEquals(new AssignmentListCommand(), new AssignmentListCommand());
        assertNotEquals(command, new AssignmentAddCommand(NAME_PS1));
        assertNotEquals(command, null);
    }
}
