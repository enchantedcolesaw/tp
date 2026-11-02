package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.TAssist;
import seedu.address.model.UserPrefs;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupName;
import seedu.address.testutil.GroupBuilder;
import seedu.address.testutil.TAssistBuilder;
import seedu.address.testutil.TypicalGroups;

public class GroupDeleteCommandTest {
    @Test
    public void execute_inactiveGroup_deletesGroupAndKeepsActiveGroup() {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(TypicalGroups.NAME_T02);
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.removeGroup(TypicalGroups.NAME_T01);

        assertCommandSuccess(new GroupDeleteCommand(new GroupName("t01")), model,
                "Deleted tutorial group T01 and its 3 students.", expected);
        assertEquals(TypicalGroups.NAME_T02, model.getActiveGroup().orElseThrow().getName());
    }

    @Test
    public void execute_activeGroup_deletesGroupAndSaysNoGroupIsActive() {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(TypicalGroups.NAME_T02);
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.removeGroup(TypicalGroups.NAME_T02);

        assertCommandSuccess(new GroupDeleteCommand(TypicalGroups.NAME_T02), model,
                "Deleted tutorial group T02 and its 2 students.\n"
                        + "No group is active now. Select one with: group use g/GROUP_NAME", expected);
        assertEquals(0, model.getActiveGroupStudentList().size());
    }

    @Test
    public void execute_lastGroup_suggestsAddingGroup() {
        TAssist tAssist = new TAssistBuilder().withGroup(new GroupBuilder().build()).build();
        Model model = new ModelManager(tAssist, new UserPrefs());
        model.setActiveGroup(TypicalGroups.NAME_T01);
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.removeGroup(TypicalGroups.NAME_T01);

        assertCommandSuccess(new GroupDeleteCommand(TypicalGroups.NAME_T01), model,
                "Deleted tutorial group T01 and its 0 students.\n"
                        + "No groups left. Create one with: group add g/GROUP_NAME", expected);
    }

    @Test
    public void getConfirmationPrompt_namesGroupAndCanBeSkipped() {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());

        assertEquals(Optional.of("Delete tutorial group T01, its 3 students and 0 assignments, with all their "
                        + "attendance, participation, submission, and grade records?"),
                new GroupDeleteCommand(new GroupName("t01")).getConfirmationPrompt(model));
        assertEquals(Optional.empty(), new GroupDeleteCommand(TypicalGroups.NAME_T01, true)
                .getConfirmationPrompt(model));
        assertEquals(Optional.empty(), new GroupDeleteCommand(TypicalGroups.NAME_T04)
                .getConfirmationPrompt(model));
    }

    @Test
    public void getConfirmationPrompt_groupWithAssignments_includesAssignmentCount() {
        Group group = new GroupBuilder().withAssignments(
                new Assignment(new AssignmentName("PS1")), new Assignment(new AssignmentName("PS2"))).build();
        Model model = new ModelManager(new TAssistBuilder().withGroup(group).build(), new UserPrefs());

        assertEquals(Optional.of("Delete tutorial group T01, its 0 students and 2 assignments, with all their "
                        + "attendance, participation, submission, and grade records?"),
                new GroupDeleteCommand(TypicalGroups.NAME_T01).getConfirmationPrompt(model));
    }

    @Test
    public void execute_unknownGroup_preservesData() {
        Model model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        assertCommandFailure(new GroupDeleteCommand(TypicalGroups.NAME_T04), model,
                "Tutorial group T04 does not exist. See groups with: group list");
        assertEquals(TypicalGroups.getTypicalGroups(), model.getTAssist().getGroupList());
    }

    @Test
    public void nullInputs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new GroupDeleteCommand(null));
        assertThrows(NullPointerException.class, () -> new GroupDeleteCommand(TypicalGroups.NAME_T01).execute(null));
    }

    @Test
    public void equals_comparesGroupNames() {
        GroupDeleteCommand command = new GroupDeleteCommand(TypicalGroups.NAME_T01);
        assertEquals(command, command);
        assertEquals(command, new GroupDeleteCommand(new GroupName("t01")));
        assertEquals(command.hashCode(), new GroupDeleteCommand(new GroupName("t01")).hashCode());
        assertNotEquals(command, new GroupDeleteCommand(TypicalGroups.NAME_T02));
        assertNotEquals(command, new GroupDeleteCommand(TypicalGroups.NAME_T01, true));
        assertNotEquals(command, new GroupUseCommand(TypicalGroups.NAME_T01));
        assertNotEquals(command, null);
    }
}
