package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_STUDENT;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAssignments.NAME_PS1;
import static seedu.address.testutil.TypicalAssignments.PS1;
import static seedu.address.testutil.TypicalGroups.NAME_T01;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BEN;
import static seedu.address.testutil.TypicalStudents.CARL;
import static seedu.address.testutil.TypicalStudents.DANIEL;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.model.student.StudentId;
import seedu.address.testutil.TypicalGroups;

public class AssignmentSubmitCommandTest {
    private static final StudentId ALICE_ID = ALICE.getStudentId();
    private static final StudentId BEN_ID = BEN.getStudentId();
    private static final StudentId CARL_ID = CARL.getStudentId();

    private Model modelWithAssignment() {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(NAME_T01);
        model.addAssignment(NAME_T01, PS1);
        return model;
    }

    @Test
    public void execute_yes_recordsSubmissionAndNamesStudent() {
        Model model = modelWithAssignment();
        Model expected = modelWithAssignment();
        expected.setAssignment(NAME_T01, PS1.withSubmissionStatus(ALICE_ID, true));

        assertCommandSuccess(new AssignmentSubmitCommand(NAME_PS1, ALICE_ID, true), model,
                "Recorded Alice Tan's PS1 submission status as submitted.", expected);
    }

    @Test
    public void execute_no_replacesSubmissionAndClearsGrade() {
        for (int grade : List.of(0, 87)) {
            Assignment prior = PS1.withSubmissionStatus(ALICE_ID, true).withGrade(ALICE_ID, grade);
            Model model = modelWithAssignment();
            model.setAssignment(NAME_T01, prior);
            Model expected = modelWithAssignment();
            expected.setAssignment(NAME_T01, prior.withSubmissionStatus(ALICE_ID, false));

            assertCommandSuccess(new AssignmentSubmitCommand(NAME_PS1, ALICE_ID, false), model,
                    "Recorded Alice Tan's PS1 submission status as not submitted; the recorded grade was cleared.",
                    expected);
        }
    }

    @Test
    public void execute_noAlreadyNotSubmitted_reportsNoChanges() {
        for (Assignment prior : List.of(PS1, PS1.withSubmissionStatus(ALICE_ID, false))) {
            Model model = modelWithAssignment();
            model.setAssignment(NAME_T01, prior);
            Model expected = modelWithAssignment();
            expected.setAssignment(NAME_T01, prior);
            AssignmentSubmitCommand command = new AssignmentSubmitCommand(NAME_PS1, ALICE_ID, false);

            for (int i = 0; i < 2; i++) {
                assertCommandSuccess(command, model,
                        "Alice Tan's PS1 submission status is already not submitted. Nothing changed.", expected);
            }
        }
    }

    @Test
    public void execute_yesAlreadySubmitted_reportsNoChangesAndKeepsGrade() {
        for (Assignment prior : List.of(PS1.withSubmissionStatus(ALICE_ID, true),
                PS1.withSubmissionStatus(ALICE_ID, true).withGrade(ALICE_ID, 87))) {
            Model model = modelWithAssignment();
            model.setAssignment(NAME_T01, prior);
            Model expected = modelWithAssignment();
            expected.setAssignment(NAME_T01, prior);

            assertCommandSuccess(new AssignmentSubmitCommand(NAME_PS1, ALICE_ID, true), model,
                    "Alice Tan's PS1 submission status is already submitted. Nothing changed.", expected);
        }
    }

    @Test
    public void execute_noWithoutGrade_updatesStatusWithoutClearingGradeMessage() {
        Assignment prior = PS1.withSubmissionStatus(ALICE_ID, true);
        Model model = modelWithAssignment();
        model.setAssignment(NAME_T01, prior);
        Model expected = modelWithAssignment();
        expected.setAssignment(NAME_T01, prior.withSubmissionStatus(ALICE_ID, false));

        assertCommandSuccess(new AssignmentSubmitCommand(NAME_PS1, ALICE_ID, false), model,
                "Recorded Alice Tan's PS1 submission status as not submitted.", expected);
    }

    @Test
    public void execute_severalStudents_updatesAllAndReportsEachInOrder() {
        Assignment prior = PS1.withSubmissionStatus(BEN_ID, true).withGrade(BEN_ID, 87)
                .withSubmissionStatus(CARL_ID, true);
        Model model = modelWithAssignment();
        model.setAssignment(NAME_T01, prior);
        Model expected = modelWithAssignment();
        expected.setAssignment(NAME_T01, prior.withSubmissionStatus(BEN_ID, false)
                .withSubmissionStatus(CARL_ID, false));

        assertCommandSuccess(new AssignmentSubmitCommand(NAME_PS1, List.of(CARL_ID, ALICE_ID, BEN_ID), false), model,
                "Recorded Carl Goh's PS1 submission status as not submitted.\n"
                        + "Alice Tan's PS1 submission status is already not submitted. Nothing changed.\n"
                        + "Recorded Ben Lim's PS1 submission status as not submitted; the recorded grade was cleared.",
                expected);
    }

    @Test
    public void execute_oneUnknownStudent_failsWithoutUpdatingAnyone() {
        Model model = modelWithAssignment();
        Model expected = modelWithAssignment();
        assertCommandFailure(new AssignmentSubmitCommand(NAME_PS1, List.of(ALICE_ID, DANIEL.getStudentId()), true),
                model, String.format(MESSAGE_UNKNOWN_STUDENT, DANIEL.getStudentId(), NAME_T01));
        assertEquals(expected, model);
    }

    @Test
    public void execute_unknownStudent_reportsErrorWithoutChanges() {
        Model model = modelWithAssignment();
        Model expected = modelWithAssignment();
        assertCommandFailure(new AssignmentSubmitCommand(NAME_PS1, new StudentId("A0999999Z"), true), model,
                String.format(MESSAGE_UNKNOWN_STUDENT, "A0999999Z", NAME_T01));
        assertEquals(expected, model);
    }

    @Test
    public void execute_unknownAssignment_reportsErrorWithoutChanges() {
        Model model = modelWithAssignment();
        assertCommandFailure(new AssignmentSubmitCommand(new AssignmentName("PS2"), ALICE_ID, true), model,
                "Assignment PS2 does not exist in T01. See the group's assignments with: assignment list\n"
                        + "Or create it with: assignment add a/PS2");
    }

    @Test
    public void execute_noActiveGroup_reportsError() {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        assertCommandFailure(new AssignmentSubmitCommand(NAME_PS1, ALICE_ID, true), model,
                Messages.MESSAGE_NO_ACTIVE_GROUP);
    }

    @Test
    public void constructorAndExecute_nullInputs_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AssignmentSubmitCommand(null, ALICE_ID, true));
        assertThrows(NullPointerException.class, () -> new AssignmentSubmitCommand(NAME_PS1, (StudentId) null, true));
        assertThrows(NullPointerException.class, () ->
                new AssignmentSubmitCommand(NAME_PS1, (List<StudentId>) null, true));
        assertThrows(IllegalArgumentException.class, () -> new AssignmentSubmitCommand(NAME_PS1, List.of(), true));
        assertThrows(NullPointerException.class, () ->
                new AssignmentSubmitCommand(NAME_PS1, ALICE_ID, true).execute(null));
    }

    @Test
    public void equals_comparesAssignmentStudentAndStatus() {
        AssignmentSubmitCommand command = new AssignmentSubmitCommand(NAME_PS1, ALICE_ID, true);
        assertEquals(command, new AssignmentSubmitCommand(new AssignmentName("ps1"), ALICE_ID, true));
        assertEquals(command.hashCode(), new AssignmentSubmitCommand(NAME_PS1, ALICE_ID, true).hashCode());
        assertNotEquals(command, new AssignmentSubmitCommand(NAME_PS1, ALICE_ID, false));
        assertNotEquals(command, new AssignmentSubmitCommand(NAME_PS1, new StudentId("A0999999Z"), true));
        assertNotEquals(command, new AssignmentSubmitCommand(NAME_PS1, List.of(ALICE_ID, BEN_ID), true));
        assertNotEquals(command, new AssignmentSubmitCommand(new AssignmentName("PS2"), ALICE_ID, true));
        assertNotEquals(command, null);
    }
}
