package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BEN;
import static seedu.address.testutil.TypicalStudents.CARL;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.WorkspaceView;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.TAssist;
import seedu.address.model.UserPrefs;
import seedu.address.model.student.StudentSearchPredicate;
import seedu.address.model.student.StudentSortField;
import seedu.address.testutil.StudentBuilder;
import seedu.address.testutil.TypicalGroups;

public class StudentSortCommandTest {
    @Test
    public void execute_filteredRoster_sortsMatchesAndListRestoresOriginalOrder() throws Exception {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(TypicalGroups.NAME_T01);
        model.removeStudent(TypicalGroups.NAME_T01, ALICE.getStudentId());
        model.addStudent(TypicalGroups.NAME_T01, ALICE);
        TAssist previousData = new TAssist(model.getTAssist());
        model.filterStudents(new StudentSearchPredicate("a", ""));

        CommandResult result = new StudentSortCommand(StudentSortField.NAME).execute(model);
        assertEquals(new CommandResult("Sorted 2 students in T01 by name.", WorkspaceView.STUDENTS), result);
        assertEquals(List.of(ALICE, CARL), model.getDisplayedStudentList());
        assertEquals(previousData, model.getTAssist());

        new StudentListCommand().execute(model);
        assertEquals(model.getActiveGroupStudentList(), model.getDisplayedStudentList());
        assertEquals(ALICE, model.getDisplayedStudentList().getLast());
    }

    @Test
    public void execute_noMatches_reportsSortAndPreservesFilter() throws Exception {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(TypicalGroups.NAME_T01);
        model.filterStudents(new StudentSearchPredicate("nobody", ""));
        TAssist previousData = new TAssist(model.getTAssist());
        assertEquals(new CommandResult("Sorted 0 students in T01 by id.",
                        WorkspaceView.STUDENTS),
                new StudentSortCommand(StudentSortField.ID).execute(model));
        assertEquals(previousData, model.getTAssist());
        model.addStudent(TypicalGroups.NAME_T01,
                new StudentBuilder().withName("Nobody Two").withStudentId("N2").build());
        model.addStudent(TypicalGroups.NAME_T01,
                new StudentBuilder().withName("Nobody One").withStudentId("N1").build());
        assertEquals(List.of("N1", "N2"), model.getDisplayedStudentList().stream()
                .map(student -> student.getStudentId().toString()).toList());
    }

    @Test
    public void execute_emptyGroup_keepsSortForFutureStudents() throws Exception {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(TypicalGroups.NAME_T03);
        TAssist previousData = new TAssist(model.getTAssist());

        assertEquals(new CommandResult("Sorted 0 students in T03 by name.", WorkspaceView.STUDENTS),
                new StudentSortCommand(StudentSortField.NAME).execute(model));
        assertEquals(previousData, model.getTAssist());
        model.addStudent(TypicalGroups.NAME_T03, BEN);
        model.addStudent(TypicalGroups.NAME_T03, ALICE);
        assertEquals(List.of(ALICE, BEN), model.getDisplayedStudentList());
        assertEquals(List.of(BEN, ALICE), model.getActiveGroupStudentList());
    }

    @Test
    public void execute_noActiveGroup_reportsError() {
        assertCommandFailure(new StudentSortCommand(StudentSortField.ID), new ModelManager(),
                Messages.MESSAGE_NO_ACTIVE_GROUP);
    }
}
