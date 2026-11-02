package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.model.assignment.AssignmentRecord;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupName;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/**
 * Jackson-friendly version of {@link Group}.
 * A missing student list is read as a group with no students, and a missing assignment list as no assignments.
 */
class JsonAdaptedGroup {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Group's %s field is missing.";
    public static final String MESSAGE_EMPTY_STUDENT = "Group %s has an empty entry in its student list.";
    public static final String MESSAGE_DUPLICATE_STUDENT = "Group %s has more than one student with ID %s.";
    public static final String MESSAGE_EMPTY_ASSIGNMENT = "Group %s has an empty entry in its assignment list.";
    public static final String MESSAGE_DUPLICATE_ASSIGNMENT = "Group %s has more than one assignment named %s.";
    public static final String MESSAGE_EMPTY_ASSIGNMENT_RECORD = "Group %s has an empty assignment record.";
    public static final String MESSAGE_DUPLICATE_ASSIGNMENT_RECORD =
            "Group %s has more than one assignment record for %s and %s.";
    public static final String MESSAGE_UNKNOWN_RECORD_ASSIGNMENT =
            "Assignment record refers to unknown assignment %s in group %s.";
    public static final String MESSAGE_UNKNOWN_RECORD_STUDENT =
            "Assignment record refers to unknown student %s in group %s.";
    public static final String MESSAGE_INVALID_STUDENT = "Group %s, student %s: %s";
    public static final String MESSAGE_INVALID_GROUP_NAME = "Group '%s': %s";
    public static final String MESSAGE_INVALID_ASSIGNMENT_NAME = "Group %s, assignment '%s': %s";
    public static final String MESSAGE_INVALID_RECORD = "Group %s, assignment record %d: %s";
    public static final String MISSING_STUDENT_ID = "with missing ID";

    private final String name;
    private final List<JsonAdaptedStudent> students = new ArrayList<>();
    private final List<String> assignments = new ArrayList<>();
    private final List<JsonAdaptedAssignmentRecord> assignmentRecords = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedGroup} with the given group details.
     */
    @JsonCreator
    public JsonAdaptedGroup(@JsonProperty("name") String name,
            @JsonProperty("students") List<JsonAdaptedStudent> students,
            @JsonProperty("assignments") List<String> assignments,
            @JsonProperty("assignmentRecords") List<JsonAdaptedAssignmentRecord> assignmentRecords) {
        this.name = name;
        if (students != null) {
            this.students.addAll(students);
        }
        if (assignments != null) {
            this.assignments.addAll(assignments);
        }
        if (assignmentRecords != null) {
            this.assignmentRecords.addAll(assignmentRecords);
        }
    }

    /**
     * Constructs a group without assignment records, for legacy JSON and tests.
     */
    public JsonAdaptedGroup(String name, List<JsonAdaptedStudent> students, List<String> assignments) {
        this(name, students, assignments, null);
    }

    /**
     * Constructs a {@code JsonAdaptedGroup} without assignments, for existing student-only data and tests.
     */
    public JsonAdaptedGroup(String name, List<JsonAdaptedStudent> students) {
        this(name, students, null, null);
    }

    /**
     * Converts a given {@code Group} into this class for Jackson use.
     */
    public JsonAdaptedGroup(Group source) {
        name = source.getName().fullName;
        students.addAll(source.getStudentList().stream().map(JsonAdaptedStudent::new).toList());
        assignments.addAll(source.getAssignmentList().stream()
                .map(assignment -> assignment.getName().fullName)
                .toList());
        for (Assignment assignment : source.getAssignmentList()) {
            for (Student student : source.getStudentList()) {
                AssignmentRecord record = assignment.getStudentRecords().get(student.getStudentId());
                if (record != null) {
                    assignmentRecords.add(new JsonAdaptedAssignmentRecord(
                            assignment.getName(), student.getStudentId(), record));
                }
            }
        }
    }

    /**
     * Converts this Jackson-friendly adapted group object into the model's {@code Group} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted group.
     */
    public Group toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "name"));
        }
        if (!GroupName.isValidGroupName(name)) {
            throw new IllegalValueException(String.format(MESSAGE_INVALID_GROUP_NAME, name,
                    GroupName.MESSAGE_CONSTRAINTS));
        }
        final Group group = new Group(new GroupName(name));

        for (JsonAdaptedStudent jsonAdaptedStudent : students) {
            addStudent(group, jsonAdaptedStudent);
        }
        for (String assignmentName : assignments) {
            addAssignment(group, assignmentName);
        }
        addAssignmentRecords(group);
        return group;
    }

    private void addAssignmentRecords(Group group) throws IllegalValueException {
        Map<AssignmentName, Set<StudentId>> seenRecords = new HashMap<>();
        for (JsonAdaptedAssignmentRecord adaptedRecord : assignmentRecords) {
            if (adaptedRecord == null) {
                throw new IllegalValueException(String.format(MESSAGE_EMPTY_ASSIGNMENT_RECORD, group.getName()));
            }
            AssignmentName assignmentName;
            StudentId studentId;
            AssignmentRecord record;
            try {
                assignmentName = adaptedRecord.getAssignmentName();
                studentId = adaptedRecord.getStudentId();
                record = adaptedRecord.toModelType();
            } catch (IllegalValueException exception) {
                // Count records from 1, as the TA does when finding the entry in the file.
                throw new IllegalValueException(String.format(MESSAGE_INVALID_RECORD, group.getName(),
                        assignmentRecords.indexOf(adaptedRecord) + 1, exception.getMessage()), exception);
            }
            Assignment assignment = group.findAssignment(assignmentName).orElseThrow(() ->
                    new IllegalValueException(String.format(MESSAGE_UNKNOWN_RECORD_ASSIGNMENT,
                            assignmentName, group.getName())));
            if (!group.hasStudent(studentId)) {
                throw new IllegalValueException(String.format(MESSAGE_UNKNOWN_RECORD_STUDENT,
                        studentId, group.getName()));
            }
            Set<StudentId> studentIds = seenRecords.computeIfAbsent(assignmentName, unused -> new HashSet<>());
            if (!studentIds.add(studentId)) {
                throw new IllegalValueException(String.format(MESSAGE_DUPLICATE_ASSIGNMENT_RECORD,
                        group.getName(), assignmentName, studentId));
            }
            group.setAssignment(assignment.withRecord(studentId, record));
        }
    }

    private static void addStudent(Group group, JsonAdaptedStudent jsonAdaptedStudent)
            throws IllegalValueException {
        if (jsonAdaptedStudent == null) {
            throw new IllegalValueException(String.format(MESSAGE_EMPTY_STUDENT, group.getName()));
        }
        Student student;
        try {
            student = jsonAdaptedStudent.toModelType();
        } catch (IllegalValueException exception) {
            String storedId = jsonAdaptedStudent.getStudentId();
            String identifier = storedId == null || storedId.isBlank() ? MISSING_STUDENT_ID : storedId;
            throw new IllegalValueException(String.format(MESSAGE_INVALID_STUDENT,
                    group.getName(), identifier, exception.getMessage()), exception);
        }
        if (group.hasStudent(student.getStudentId())) {
            throw new IllegalValueException(String.format(MESSAGE_DUPLICATE_STUDENT,
                    group.getName(), student.getStudentId()));
        }
        group.addStudent(student);
    }

    private static void addAssignment(Group group, String assignmentName) throws IllegalValueException {
        if (assignmentName == null) {
            throw new IllegalValueException(String.format(MESSAGE_EMPTY_ASSIGNMENT, group.getName()));
        }
        if (!AssignmentName.isValidAssignmentName(assignmentName)) {
            throw new IllegalValueException(String.format(MESSAGE_INVALID_ASSIGNMENT_NAME, group.getName(),
                    assignmentName, AssignmentName.MESSAGE_CONSTRAINTS));
        }
        Assignment assignment = new Assignment(new AssignmentName(assignmentName));
        if (group.hasAssignment(assignment.getName())) {
            throw new IllegalValueException(String.format(MESSAGE_DUPLICATE_ASSIGNMENT,
                    group.getName(), assignment.getName()));
        }
        group.addAssignment(assignment);
    }

}
