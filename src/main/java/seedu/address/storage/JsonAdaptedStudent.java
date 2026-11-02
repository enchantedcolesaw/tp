package seedu.address.storage;

import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.student.AttendanceStatus;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.WeeklyRecord;

/**
 * Jackson-friendly version of {@link Student}.
 */
class JsonAdaptedStudent {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Student's %s field is missing.";
    public static final String MESSAGE_INVALID_PARTICIPATION_SCORE =
            "Participation score must be a whole number from 0 to 5.";

    private final String name;
    private final String studentId;
    private final Map<Integer, String> attendance;
    private final Map<Integer, Number> participation;

    /** Why the week keys read from the file are invalid, or null if they are valid. Never saved. */
    private transient String weekKeyError;

    /**
     * Constructs a {@code JsonAdaptedStudent} with the given student details.
     */
    public JsonAdaptedStudent(String name, String studentId, Map<Integer, String> attendance,
            Map<Integer, Number> participation) {
        this.name = name;
        this.studentId = studentId;
        this.attendance = attendance == null ? Map.of() : new TreeMap<>(attendance);
        this.participation = participation == null ? Map.of() : new TreeMap<>(participation);
    }

    /**
     * Constructs an adapted student without weekly records, for existing student-only data and tests.
     */
    public JsonAdaptedStudent(String name, String studentId) {
        this(name, studentId, Map.of(), Map.of());
    }

    /**
     * Converts a given {@code Student} into this class for Jackson use.
     */
    public JsonAdaptedStudent(Student source) {
        name = source.getName().fullName;
        studentId = source.getStudentId().value;
        attendance = new TreeMap<>(source.getAttendanceRecords().getRecords());
        participation = new TreeMap<>(source.getParticipationRecords().getRecords());
    }

    /** Returns the ID as stored in the file, including an invalid or missing ID, for error reporting. */
    String getStudentId() {
        return studentId;
    }

    /**
     * Creates an adapted student from JSON without discarding duplicate week aliases.
     * A week key that is not an integer, or that repeats another week, is reported by {@link #toModelType()},
     * so that the error names the student.
     */
    @JsonCreator
    public static JsonAdaptedStudent fromJson(@JsonProperty("name") String name,
            @JsonProperty("studentId") String studentId, @JsonProperty("attendance") Map<String, String> attendance,
            @JsonProperty("participation") Map<String, Number> participation) {
        try {
            return new JsonAdaptedStudent(name, studentId, parseWeekKeys(attendance), parseWeekKeys(participation));
        } catch (IllegalArgumentException exception) {
            JsonAdaptedStudent student = new JsonAdaptedStudent(name, studentId);
            student.weekKeyError = exception.getMessage();
            return student;
        }
    }

    private static <T> Map<Integer, T> parseWeekKeys(Map<String, T> records) {
        Map<Integer, T> normalizedRecords = new TreeMap<>();
        if (records != null) {
            records.forEach((weekKey, value) -> {
                int week = parseWeekKey(weekKey);
                checkArgument(!normalizedRecords.containsKey(week),
                        "Duplicate weekly record for Week " + week + ". Use only one entry per week.");
                normalizedRecords.put(week, value);
            });
        }
        return normalizedRecords;
    }

    private static int parseWeekKey(String weekKey) {
        try {
            return Integer.parseInt(weekKey);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(WeeklyRecord.MESSAGE_INVALID_WEEK, exception);
        }
    }

    /**
     * Converts this Jackson-friendly adapted student object into the model's {@code Student} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted student.
     */
    public Student toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "name"));
        }
        if (!StudentName.isValidStudentName(name)) {
            throw new IllegalValueException(StudentName.MESSAGE_CONSTRAINTS);
        }
        final StudentName modelName = new StudentName(name);

        if (studentId == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "studentId"));
        }
        if (!StudentId.isValidStudentId(studentId)) {
            throw new IllegalValueException(StudentId.MESSAGE_CONSTRAINTS);
        }
        final StudentId modelStudentId = new StudentId(studentId);
        if (weekKeyError != null) {
            throw new IllegalValueException(weekKeyError);
        }

        try {
            return new Student(modelName, modelStudentId, toAttendanceRecords(), toParticipationRecords());
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage(), exception);
        }
    }

    private WeeklyRecord<String> toAttendanceRecords() {
        WeeklyRecord<String> validatedRecords = new WeeklyRecord<>(attendance);
        Map<Integer, String> normalizedRecords = new TreeMap<>();
        validatedRecords.getRecords().forEach((week, status) ->
                normalizedRecords.put(week, AttendanceStatus.fromValue(status).name()));
        return new WeeklyRecord<>(normalizedRecords);
    }

    //@@author limenxi07
    /**
     * Returns participation records, rejecting a score that is fractional or outside 0 to 5.
     * Invalid weeks and missing scores are still rejected by {@code WeeklyRecord}.
     */
    private WeeklyRecord<Integer> toParticipationRecords() {
        Map<Integer, Integer> scores = new TreeMap<>();
        participation.forEach((week, score) ->
                scores.put(week, score == null ? null : toParticipationScore(score)));
        return new WeeklyRecord<>(scores);
    }

    private static int toParticipationScore(Number score) {
        Optional<Integer> wholeScore = JsonWholeNumber.toInt(score).filter(value -> value >= 0 && value <= 5);
        checkArgument(wholeScore.isPresent(), MESSAGE_INVALID_PARTICIPATION_SCORE);
        return wholeScore.get();
    }

}
