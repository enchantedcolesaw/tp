package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedGroup.MESSAGE_DUPLICATE_ASSIGNMENT;
import static seedu.address.storage.JsonAdaptedGroup.MESSAGE_DUPLICATE_STUDENT;
import static seedu.address.storage.JsonAdaptedGroup.MESSAGE_EMPTY_ASSIGNMENT;
import static seedu.address.storage.JsonAdaptedGroup.MESSAGE_EMPTY_STUDENT;
import static seedu.address.storage.JsonAdaptedGroup.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAssignments.PS1;
import static seedu.address.testutil.TypicalAssignments.PS2;
import static seedu.address.testutil.TypicalStudents.ALICE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.model.assignment.AssignmentRecord;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupName;
import seedu.address.model.student.StudentId;
import seedu.address.testutil.GroupBuilder;
import seedu.address.testutil.TypicalGroups;

public class JsonAdaptedGroupTest {
    private static final String INVALID_NAME = "T01!";

    private static final Group T01 = TypicalGroups.getT01();
    private static final String VALID_NAME = T01.getName().toString();
    private static final List<JsonAdaptedStudent> VALID_STUDENTS = T01.getStudentList().stream()
            .map(JsonAdaptedStudent::new)
            .toList();

    @Test
    public void toModelType_validGroupDetails_returnsGroup() throws Exception {
        JsonAdaptedGroup group = new JsonAdaptedGroup(T01);
        assertEquals(T01, group.toModelType());
    }

    @Test
    public void toModelType_unicodeSpacesInNames_returnsNamesWithNormalSpaces() throws Exception {
        String json = "{\"name\":\"Lab\\u00a01\",\"students\":[],\"assignments\":[\"Lab\\u20032\"]}";
        Group group = JsonUtil.fromJsonString(json, JsonAdaptedGroup.class).toModelType();
        assertEquals("Lab 1", group.getName().fullName);
        assertEquals(new AssignmentName("Lab 2"), group.getAssignmentList().getFirst().getName());
    }

    @Test
    public void toModelType_nullStudents_returnsEmptyGroup() throws Exception {
        JsonAdaptedGroup group = new JsonAdaptedGroup(VALID_NAME, null);
        assertEquals(new GroupBuilder().withName(VALID_NAME).build(), group.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedGroup group = new JsonAdaptedGroup(INVALID_NAME, VALID_STUDENTS);
        assertThrows(IllegalValueException.class, String.format(JsonAdaptedGroup.MESSAGE_INVALID_GROUP_NAME,
                INVALID_NAME, GroupName.MESSAGE_CONSTRAINTS), group::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedGroup group = new JsonAdaptedGroup(null, VALID_STUDENTS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "name");
        assertThrows(IllegalValueException.class, expectedMessage, group::toModelType);
    }

    @Test
    public void toModelType_invalidStudent_throwsIllegalValueException() {
        List<JsonAdaptedStudent> students = new ArrayList<>(VALID_STUDENTS);
        students.add(new JsonAdaptedStudent("Fiona Lee", null));
        JsonAdaptedGroup group = new JsonAdaptedGroup(VALID_NAME, students);
        String expectedMessage = "Group " + VALID_NAME + ", student with missing ID: "
                + String.format(JsonAdaptedStudent.MISSING_FIELD_MESSAGE_FORMAT, "studentId");
        assertThrows(IllegalValueException.class, expectedMessage, group::toModelType);
    }

    @Test
    public void toModelType_blankStudentId_reportsMissingId() {
        List<JsonAdaptedStudent> students = new ArrayList<>(VALID_STUDENTS);
        students.add(new JsonAdaptedStudent("Fiona Lee", "  "));
        JsonAdaptedGroup group = new JsonAdaptedGroup(VALID_NAME, students);
        String expectedMessage = "Group " + VALID_NAME + ", student with missing ID: "
                + StudentId.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, group::toModelType);
    }

    @Test
    public void toModelType_nullStudent_throwsIllegalValueException() {
        List<JsonAdaptedStudent> students = new ArrayList<>(VALID_STUDENTS);
        students.add(null);
        JsonAdaptedGroup group = new JsonAdaptedGroup(VALID_NAME, students);
        String expectedMessage = String.format(MESSAGE_EMPTY_STUDENT, VALID_NAME);
        assertThrows(IllegalValueException.class, expectedMessage, group::toModelType);
    }

    @Test
    public void toModelType_duplicateStudentIds_throwsIllegalValueException() {
        String sameIdInLowercase = ALICE.getStudentId().toString().toLowerCase();
        JsonAdaptedStudent duplicate = new JsonAdaptedStudent("Alice Ng", sameIdInLowercase);
        JsonAdaptedGroup group = new JsonAdaptedGroup(VALID_NAME,
                Arrays.asList(new JsonAdaptedStudent(ALICE), duplicate));
        String expectedMessage = String.format(MESSAGE_DUPLICATE_STUDENT, VALID_NAME,
                new StudentId(sameIdInLowercase));
        assertThrows(IllegalValueException.class, expectedMessage, group::toModelType);
    }

    @Test
    public void toModelType_groupWithAssignments_returnsSameGroupInSameOrder() throws Exception {
        Group source = new GroupBuilder(T01).withAssignments(PS2, PS1).build();
        assertEquals(source, new JsonAdaptedGroup(source).toModelType());
    }

    @Test
    public void toModelType_assignmentRecords_restoresSubmissionAndGrade() throws Exception {
        AssignmentRecord record = new AssignmentRecord(true, Optional.of(87));
        JsonAdaptedGroup group = new JsonAdaptedGroup(VALID_NAME, VALID_STUDENTS, List.of("PS1"),
                List.of(new JsonAdaptedAssignmentRecord("PS1", ALICE.getStudentId().value, true, 87)));
        Group expected = new GroupBuilder(T01).withAssignments(PS1.withRecord(ALICE.getStudentId(), record)).build();

        assertEquals(expected, group.toModelType());
    }

    @Test
    public void toModelType_duplicateUnknownAssignmentOrStudentRecord_rejectsData() {
        JsonAdaptedAssignmentRecord first = new JsonAdaptedAssignmentRecord("PS1", ALICE.getStudentId().value,
                true, null);
        JsonAdaptedAssignmentRecord duplicate = new JsonAdaptedAssignmentRecord("ps1", ALICE.getStudentId().value,
                false, null);
        JsonAdaptedGroup duplicateRecords = new JsonAdaptedGroup(VALID_NAME, VALID_STUDENTS, List.of("PS1"),
                List.of(first, duplicate));
        assertThrows(IllegalValueException.class,
                String.format(JsonAdaptedGroup.MESSAGE_DUPLICATE_ASSIGNMENT_RECORD, VALID_NAME,
                        "ps1", ALICE.getStudentId()), duplicateRecords::toModelType);

        // A Unicode case variant (dotless i) of the assignment name is still a duplicate record.
        JsonAdaptedGroup unicodeDuplicateRecords = new JsonAdaptedGroup(VALID_NAME, VALID_STUDENTS, List.of("PSI"),
                List.of(new JsonAdaptedAssignmentRecord("PSI", ALICE.getStudentId().value, true, 87),
                        new JsonAdaptedAssignmentRecord("psı", ALICE.getStudentId().value, true, 12)));
        assertThrows(IllegalValueException.class,
                String.format(JsonAdaptedGroup.MESSAGE_DUPLICATE_ASSIGNMENT_RECORD, VALID_NAME,
                        "psı", ALICE.getStudentId()), unicodeDuplicateRecords::toModelType);

        JsonAdaptedGroup unknownAssignment = new JsonAdaptedGroup(VALID_NAME, VALID_STUDENTS, List.of("PS1"),
                List.of(new JsonAdaptedAssignmentRecord("PS2", ALICE.getStudentId().value, true, null)));
        assertThrows(IllegalValueException.class,
                String.format(JsonAdaptedGroup.MESSAGE_UNKNOWN_RECORD_ASSIGNMENT, "PS2", VALID_NAME),
                unknownAssignment::toModelType);

        // A hand-edited file that removes a student but leaves their explicit assignment record is rejected.
        JsonAdaptedGroup recordForRemovedStudent = new JsonAdaptedGroup(VALID_NAME, VALID_STUDENTS, List.of("PS1"),
                List.of(new JsonAdaptedAssignmentRecord("PS1", "A0999999Z", true, null)));
        assertThrows(IllegalValueException.class,
                String.format(JsonAdaptedGroup.MESSAGE_UNKNOWN_RECORD_STUDENT, "A0999999Z", VALID_NAME),
                recordForRemovedStudent::toModelType);

        JsonAdaptedGroup emptyRecord = new JsonAdaptedGroup(VALID_NAME, VALID_STUDENTS, List.of("PS1"),
                Arrays.asList((JsonAdaptedAssignmentRecord) null));
        assertThrows(IllegalValueException.class,
                String.format(JsonAdaptedGroup.MESSAGE_EMPTY_ASSIGNMENT_RECORD, VALID_NAME),
                emptyRecord::toModelType);
    }

    @Test
    public void toModelType_nullAssignments_returnsGroupWithoutAssignments() throws Exception {
        JsonAdaptedGroup group = new JsonAdaptedGroup(VALID_NAME, VALID_STUDENTS, null);
        assertEquals(T01, group.toModelType());
    }

    @Test
    public void toModelType_assignmentNameWithExtraSpaces_normalizesName() throws Exception {
        JsonAdaptedGroup group = new JsonAdaptedGroup(VALID_NAME, null, List.of(" Problem   Set 1 "));
        Group expected = new GroupBuilder().withName(VALID_NAME).build();
        expected.addAssignment(new Assignment(new AssignmentName("Problem Set 1")));
        assertEquals(expected, group.toModelType());
    }

    @Test
    public void toModelType_invalidAssignmentName_throwsIllegalValueException() {
        for (String name : new String[] {"", "PS1/PS2", "-", "a".repeat(AssignmentName.MAX_LENGTH + 1)}) {
            JsonAdaptedGroup group = new JsonAdaptedGroup(VALID_NAME, null, List.of(name));
            assertThrows(IllegalValueException.class, String.format(JsonAdaptedGroup.MESSAGE_INVALID_ASSIGNMENT_NAME,
                    VALID_NAME, name, AssignmentName.MESSAGE_CONSTRAINTS), group::toModelType);
        }
    }

    @Test
    public void toModelType_nullAssignment_throwsIllegalValueException() {
        JsonAdaptedGroup group = new JsonAdaptedGroup(VALID_NAME, null, Arrays.asList("PS1", null));
        String expectedMessage = String.format(MESSAGE_EMPTY_ASSIGNMENT, VALID_NAME);
        assertThrows(IllegalValueException.class, expectedMessage, group::toModelType);
    }

    @Test
    public void toModelType_duplicateAssignmentNames_throwsIllegalValueException() {
        JsonAdaptedGroup group = new JsonAdaptedGroup(VALID_NAME, null, List.of("PS1", "ps1"));
        String expectedMessage = String.format(MESSAGE_DUPLICATE_ASSIGNMENT, VALID_NAME, new AssignmentName("ps1"));
        assertThrows(IllegalValueException.class, expectedMessage, group::toModelType);
    }
}
