package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAssignments.NAME_PS1;
import static seedu.address.testutil.TypicalAssignments.PS1;
import static seedu.address.testutil.TypicalGroups.NAME_T01;
import static seedu.address.testutil.TypicalGroups.NAME_T02;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BEN;
import static seedu.address.testutil.TypicalStudents.CARL;
import static seedu.address.testutil.TypicalStudents.FIONA;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;
import seedu.address.testutil.StudentBuilder;
import seedu.address.testutil.TypicalGroups;

public class StudentEditCommandTest {
    private static final StudentName NEW_NAME = new StudentName("Alicia Tan");
    private static final StudentId NEW_ID = new StudentId("A0123457X");

    private Model model;
    private Student recordedAlice;
    private Assignment gradedPs1;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(TypicalGroups.getTypicalTAssist(), new UserPrefs());
        model.setActiveGroup(NAME_T01);
        recordedAlice = ALICE.withAttendanceRecord(3, "PRESENT").withParticipationRecord(3, 4);
        model.setStudent(NAME_T01, recordedAlice);
        gradedPs1 = PS1.withSubmissionStatus(ALICE.getStudentId(), true).withGrade(ALICE.getStudentId(), 87)
                .withSubmissionStatus(BEN.getStudentId(), true);
        model.addAssignment(NAME_T01, gradedPs1);
    }

    @Test
    public void execute_newName_keepsIdAndRecords() {
        Student edited = recordedAlice.withNameAndId(NEW_NAME, ALICE.getStudentId());

        assertCommandSuccess(new StudentEditCommand(new StudentId("a0123456x"), Optional.of(NEW_NAME),
                Optional.empty()), model,
                "Edited Alice Tan (A0123456X) to Alicia Tan (A0123456X) in T01. Their records are kept.",
                modelWithEditedAlice(edited));
        assertEquals(List.of(edited, BEN, CARL), model.getGroup(NAME_T01).getStudentList());
        assertEquals(gradedPs1, model.getGroup(NAME_T01).findAssignment(NAME_PS1).orElseThrow());
    }

    @Test
    public void execute_newId_movesAllRecordsToNewId() {
        Student edited = recordedAlice.withNameAndId(ALICE.getName(), NEW_ID);

        assertCommandSuccess(new StudentEditCommand(ALICE.getStudentId(), Optional.empty(), Optional.of(NEW_ID)),
                model, "Edited Alice Tan (A0123456X) to Alice Tan (A0123457X) in T01. Their records are kept.",
                modelWithEditedAlice(edited));

        assertEquals(List.of(edited, BEN, CARL), model.getActiveGroupStudentList());
        assertEquals(87, model.getGroup(NAME_T01).findAssignment(NAME_PS1).orElseThrow()
                .getStudentRecord(NEW_ID).getGrade().orElseThrow());
        assertEquals(gradedPs1.getStudentRecord(BEN.getStudentId()), model.getGroup(NAME_T01)
                .findAssignment(NAME_PS1).orElseThrow().getStudentRecord(BEN.getStudentId()));
    }

    @Test
    public void execute_newNameAndId_changesBothAndLeavesOtherGroupsAlone() {
        model.addStudent(NAME_T02, ALICE);
        Student edited = recordedAlice.withNameAndId(NEW_NAME, NEW_ID);

        StudentEditCommand command = new StudentEditCommand(ALICE.getStudentId(), Optional.of(NEW_NAME),
                Optional.of(NEW_ID));
        assertCommandSuccess(command, model,
                "Edited Alice Tan (A0123456X) to Alicia Tan (A0123457X) in T01. Their records are kept.",
                modelWithEditedAlice(edited));

        assertEquals(ALICE, model.getGroup(NAME_T02).findStudent(ALICE.getStudentId()).orElseThrow());
    }

    @Test
    public void execute_nameOnlyChangesCapitalization_isEdited() {
        StudentName lowercase = new StudentName("alice tan");
        Student edited = recordedAlice.withNameAndId(lowercase, ALICE.getStudentId());

        assertCommandSuccess(new StudentEditCommand(ALICE.getStudentId(), Optional.of(lowercase), Optional.empty()),
                model, "Edited Alice Tan (A0123456X) to alice tan (A0123456X) in T01. Their records are kept.",
                modelWithEditedAlice(edited));
    }

    @Test
    public void execute_nameOfAnotherStudent_editsWithWarning() {
        StudentName benName = new StudentName("ben  LIM");
        Student edited = recordedAlice.withNameAndId(benName, ALICE.getStudentId());

        assertCommandSuccess(new StudentEditCommand(ALICE.getStudentId(), Optional.of(benName), Optional.empty()),
                model, "Edited Alice Tan (A0123456X) to ben LIM (A0123456X) in T01. Their records are kept."
                        + " Warning: T01 already has another student named ben LIM.", modelWithEditedAlice(edited));
    }

    @Test
    public void execute_canonicalNameOfAnotherStudent_editsWithWarningAndPreservesSpelling() {
        for (String[] names : new String[][] {{"José Tan", "Jose\u0301 Tan"}, {"Jose\u0301 Tan", "José Tan"}}) {
            Model actual = new ModelManager(model.getTAssist(), new UserPrefs());
            actual.setStudent(NAME_T01, new StudentBuilder(BEN).withName(names[0]).build());
            StudentName newName = new StudentName(names[1]);
            Student edited = recordedAlice.withNameAndId(newName, ALICE.getStudentId());
            Model expected = new ModelManager(actual.getTAssist(), new UserPrefs());
            expected.replaceStudent(NAME_T01, ALICE.getStudentId(), edited);
            String message = "Edited Alice Tan (A0123456X) to " + names[1]
                    + " (A0123456X) in T01. Their records are kept."
                    + " Warning: T01 already has another student named " + names[1] + ".";

            assertCommandSuccess(new StudentEditCommand(ALICE.getStudentId(), Optional.of(newName), Optional.empty()),
                    actual, message, expected);
            assertEquals(names[1], actual.getGroup(NAME_T01).findStudent(ALICE.getStudentId()).orElseThrow()
                    .getName().fullName);
            assertEquals(gradedPs1, actual.getGroup(NAME_T01).findAssignment(NAME_PS1).orElseThrow());
        }
    }

    @Test
    public void execute_ownNameUnicodeFormChanges_editsSpellingWithoutWarning() {
        StudentName originalName = new StudentName("José Tan");
        StudentName newName = new StudentName("Jose\u0301 Tan");
        model.setStudent(NAME_T01, recordedAlice.withNameAndId(originalName, ALICE.getStudentId()));
        model.setStudent(NAME_T01, new StudentBuilder(BEN).withName(originalName.fullName).build());
        Student edited = recordedAlice.withNameAndId(newName, ALICE.getStudentId());

        assertCommandSuccess(new StudentEditCommand(ALICE.getStudentId(), Optional.of(newName), Optional.empty()),
                model, "Edited José Tan (A0123456X) to Jose\u0301 Tan (A0123456X) in T01. Their records are kept.",
                modelWithEditedAlice(edited));
        assertEquals(newName.fullName, model.getGroup(NAME_T01).findStudent(ALICE.getStudentId()).orElseThrow()
                .getName().fullName);
    }

    @Test
    public void execute_unchangedDetails_reportsNothingChangedAndKeepsData() {
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        String message = "Alice Tan (A0123456X) already has those details. Nothing changed.";

        assertCommandSuccess(new StudentEditCommand(ALICE.getStudentId(), Optional.of(ALICE.getName()),
                Optional.of(new StudentId("a0123456x"))), model, message, expected);
        assertCommandSuccess(new StudentEditCommand(ALICE.getStudentId(), Optional.empty(),
                Optional.of(ALICE.getStudentId())), model, message, expected);
    }

    @Test
    public void execute_idOfAnotherStudent_throwsCommandExceptionWithoutChange() {
        StudentId benIdInOtherCase = new StudentId(" a0234567y ");

        assertCommandFailure(new StudentEditCommand(ALICE.getStudentId(), Optional.of(NEW_NAME),
                Optional.of(benIdInOtherCase)), model, "Student ID A0234567Y already exists in T01.");
    }

    @Test
    public void execute_idOfStudentInOtherGroup_isAllowed() {
        model.addStudent(NAME_T02, FIONA);
        Student edited = recordedAlice.withNameAndId(ALICE.getName(), FIONA.getStudentId());

        assertCommandSuccess(new StudentEditCommand(ALICE.getStudentId(), Optional.empty(),
                Optional.of(FIONA.getStudentId())), model,
                "Edited Alice Tan (A0123456X) to Alice Tan (A0678901R) in T01. Their records are kept.",
                modelWithEditedAlice(edited));
    }

    @Test
    public void execute_unknownStudentOrNoActiveGroup_throwsCommandExceptionWithoutChange() {
        StudentEditCommand command = new StudentEditCommand(FIONA.getStudentId(), Optional.of(NEW_NAME),
                Optional.empty());
        assertCommandFailure(command, model, String.format(Messages.MESSAGE_UNKNOWN_STUDENT, "A0678901R", "T01"));

        model.setTAssist(TypicalGroups.getTypicalTAssist());
        assertCommandFailure(command, model, Messages.MESSAGE_NO_ACTIVE_GROUP);
    }

    @Test
    public void constructor_invalidArguments_throwsException() {
        StudentId id = ALICE.getStudentId();
        assertThrows(NullPointerException.class, () -> new StudentEditCommand(null, Optional.of(NEW_NAME),
                Optional.empty()));
        assertThrows(NullPointerException.class, () -> new StudentEditCommand(id, null, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new StudentEditCommand(id, Optional.empty(), null));
        assertThrows(IllegalArgumentException.class, () -> new StudentEditCommand(id, Optional.empty(),
                Optional.empty()));
        assertThrows(NullPointerException.class, () -> new StudentEditCommand(id, Optional.of(NEW_NAME),
                Optional.empty()).execute(null));
    }

    @Test
    public void equals_comparesIdNameAndNewId() {
        StudentEditCommand command = new StudentEditCommand(ALICE.getStudentId(), Optional.of(NEW_NAME),
                Optional.of(NEW_ID));
        assertEquals(command, command);
        assertEquals(command, new StudentEditCommand(new StudentId("a0123456x"), Optional.of(NEW_NAME),
                Optional.of(NEW_ID)));
        assertEquals(command.hashCode(), new StudentEditCommand(ALICE.getStudentId(), Optional.of(NEW_NAME),
                Optional.of(NEW_ID)).hashCode());
        assertNotEquals(command, new StudentEditCommand(BEN.getStudentId(), Optional.of(NEW_NAME),
                Optional.of(NEW_ID)));
        assertNotEquals(command, new StudentEditCommand(ALICE.getStudentId(), Optional.empty(), Optional.of(NEW_ID)));
        assertNotEquals(command, new StudentEditCommand(ALICE.getStudentId(), Optional.of(NEW_NAME),
                Optional.empty()));
        assertNotEquals(command, null);
        assertNotEquals(command, "student edit");
    }

    /**
     * Returns a copy of the model as it is now, with Alice replaced by {@code edited}.
     */
    private Model modelWithEditedAlice(Student edited) {
        Model expected = new ModelManager(model.getTAssist(), new UserPrefs());
        expected.replaceStudent(NAME_T01, ALICE.getStudentId(), edited);
        return expected;
    }
}
