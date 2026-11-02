package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BEN;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.WorkspaceView;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.StudentDisplayState;
import seedu.address.model.TAssist;
import seedu.address.model.UserPrefs;
import seedu.address.model.student.StudentSearchPredicate;
import seedu.address.model.student.StudentSortField;
import seedu.address.testutil.TypicalGroups;

public class StudentFindCommandTest {
    @Test
    public void execute_searchAndReset_updatesDisplayWithoutChangingRoster() throws Exception {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(TypicalGroups.NAME_T01);
        TAssist previousData = new TAssist(model.getTAssist());

        CommandResult result = new StudentFindCommand(new StudentSearchPredicate("alice", "")).execute(model);
        assertEquals(new CommandResult("Found 1 matching student in T01.", WorkspaceView.STUDENTS), result);
        assertEquals(List.of(ALICE), model.getDisplayedStudentList());

        result = new StudentFindCommand(new StudentSearchPredicate("missing", "")).execute(model);
        assertEquals(new CommandResult("No matching students in T01. Use student list to show all students.",
                WorkspaceView.STUDENTS), result);
        new StudentListCommand().execute(model);
        assertEquals(model.getActiveGroupStudentList(), model.getDisplayedStudentList());
        assertEquals(previousData, model.getTAssist());
    }

    @Test
    public void execute_noActiveGroup_reportsError() {
        assertCommandFailure(new StudentFindCommand(new StudentSearchPredicate("Alice", "")),
                new ModelManager(), Messages.MESSAGE_NO_ACTIVE_GROUP);
    }

    @Test
    public void execute_emptyGroup_reportsEmptyRosterWithoutHidingFutureStudents() throws Exception {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(TypicalGroups.NAME_T03);
        model.sortStudents(StudentSortField.NAME.getComparator());
        StudentDisplayState previousDisplay = model.getStudentDisplayState();
        TAssist previousData = new TAssist(model.getTAssist());

        CommandResult result = new StudentFindCommand(new StudentSearchPredicate("alice", "")).execute(model);
        assertEquals(new CommandResult("No students in T03. Add one using: student add n/NAME id/STUDENT_ID",
                WorkspaceView.STUDENTS), result);
        assertEquals(previousData, model.getTAssist());
        assertEquals(previousDisplay, model.getStudentDisplayState());
        model.addStudent(TypicalGroups.NAME_T03, BEN);
        assertEquals(List.of(BEN), model.getDisplayedStudentList());
    }

    @Test
    public void execute_emptiedFilteredGroup_keepsExistingDisplaySettings() throws Exception {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(TypicalGroups.NAME_T03);
        model.addStudent(TypicalGroups.NAME_T03, ALICE);
        new StudentFindCommand(new StudentSearchPredicate("alice", "")).execute(model);
        model.removeStudent(TypicalGroups.NAME_T03, ALICE.getStudentId());
        StudentDisplayState previousDisplay = model.getStudentDisplayState();

        new StudentFindCommand(new StudentSearchPredicate("ben", "")).execute(model);
        assertEquals(previousDisplay, model.getStudentDisplayState());
        model.addStudent(TypicalGroups.NAME_T03, ALICE);
        assertEquals(List.of(ALICE), model.getDisplayedStudentList());
    }
}
