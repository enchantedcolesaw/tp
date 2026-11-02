package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedStudent.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalStudents.BEN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.student.AttendanceStatus;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.WeeklyRecord;
import seedu.address.testutil.TypicalStudents;

public class JsonAdaptedStudentTest {
    private static final String INVALID_NAME = "B3n";
    private static final String INVALID_STUDENT_ID = "A012-3456X";

    private static final String VALID_NAME = BEN.getName().toString();
    private static final String VALID_STUDENT_ID = BEN.getStudentId().toString();

    @Test
    public void toModelType_validStudentDetails_returnsStudent() throws Exception {
        JsonAdaptedStudent student = new JsonAdaptedStudent(BEN);
        assertEquals(BEN, student.toModelType());
    }

    @Test
    public void toModelType_jsonWeeklyRecords_returnsStudentWithRecords() throws Exception {
        String json = "{\"name\":\"Ben Lim\",\"studentId\":\"A0234567Y\","
                + "\"attendance\":{\"1\":\"PRESENT\",\"13\":\"ABSENT\"},"
                + "\"participation\":{\"2\":4}}";
        JsonAdaptedStudent adapted = JsonUtil.fromJsonString(json, JsonAdaptedStudent.class);
        Student expected = TypicalStudents.BEN.withAttendanceRecord(1, "PRESENT")
                .withAttendanceRecord(13, "ABSENT").withParticipationRecord(2, 4);

        assertEquals(expected, adapted.toModelType());
    }

    @Test
    public void toModelType_unicodeSpacesInName_returnsStudentWithNormalSpaces() throws Exception {
        for (String name : List.of("Ben\\u00a0Lim", "Ben\u00A0Lim", "Ben\\u2003Lim", "\\u3000Ben Lim\\u00a0")) {
            String json = "{\"name\":\"" + name + "\",\"studentId\":\"A0234567Y\"}";
            assertEquals(BEN, JsonUtil.fromJsonString(json, JsonAdaptedStudent.class).toModelType());
        }
    }

    @Test
    public void toModelType_duplicateWeekAliases_throwsIllegalValueException() throws Exception {
        for (String weeklyRecords : List.of("\"participation\":{\"1\":3,\"01\":5}",
                "\"participation\":{\"01\":5,\"1\":3}", "\"participation\":{\"1\":3,\"001\":3}",
                "\"attendance\":{\"1\":\"PRESENT\",\"01\":\"ABSENT\"}")) {
            String json = "{\"name\":\"Ben Lim\",\"studentId\":\"A0234567Y\"," + weeklyRecords + "}";
            JsonAdaptedStudent adapted = JsonUtil.fromJsonString(json, JsonAdaptedStudent.class);

            assertThrows(IllegalValueException.class,
                    "Duplicate weekly record for Week 1. Use only one entry per week.", adapted::toModelType);
        }
    }

    @Test
    public void toModelType_nonIntegerWeekKey_throwsIllegalValueException() throws Exception {
        for (String weeklyRecords : List.of("\"attendance\":{\"3.0\":\"PRESENT\"}",
                "\"attendance\":{\"abc\":\"ABSENT\"}", "\"participation\":{\"\":3}",
                "\"participation\":{\"99999999999\":3}")) {
            String json = "{\"name\":\"Ben Lim\",\"studentId\":\"A0234567Y\"," + weeklyRecords + "}";
            JsonAdaptedStudent adapted = JsonUtil.fromJsonString(json, JsonAdaptedStudent.class);

            assertThrows(IllegalValueException.class, WeeklyRecord.MESSAGE_INVALID_WEEK, adapted::toModelType);
        }
    }

    @Test
    public void toModelType_distinctWeekAliases_returnsStudentWithRecords() throws Exception {
        String json = "{\"name\":\"Ben Lim\",\"studentId\":\"A0234567Y\","
                + "\"attendance\":{\"01\":\"PRESENT\",\"13\":\"ABSENT\"},"
                + "\"participation\":{\"01\":3,\"02\":5}}";
        JsonAdaptedStudent adapted = JsonUtil.fromJsonString(json, JsonAdaptedStudent.class);
        Student expected = BEN.withAttendanceRecord(1, "PRESENT").withAttendanceRecord(13, "ABSENT")
                .withParticipationRecord(1, 3).withParticipationRecord(2, 5);

        assertEquals(expected, adapted.toModelType());
        assertEquals(expected, JsonUtil.fromJsonString(JsonUtil.toJsonString(adapted), JsonAdaptedStudent.class)
                .toModelType());
    }

    @Test
    public void toModelType_oldJsonWithoutWeeklyRecords_returnsEmptyRecords() throws Exception {
        String json = "{\"name\":\"Ben Lim\",\"studentId\":\"A0234567Y\"}";
        JsonAdaptedStudent adapted = JsonUtil.fromJsonString(json, JsonAdaptedStudent.class);

        assertEquals(BEN, adapted.toModelType());
    }

    @Test
    public void toModelType_attendanceWithDifferentCase_returnsCanonicalStatus() throws Exception {
        JsonAdaptedStudent adapted = new JsonAdaptedStudent(VALID_NAME, VALID_STUDENT_ID,
                Map.of(1, " present ", 13, "Absent"), Map.of());
        assertEquals(BEN.withAttendanceRecord(1, "PRESENT").withAttendanceRecord(13, "ABSENT"),
                adapted.toModelType());
    }

    @Test
    public void toModelType_invalidAttendanceStatus_throwsIllegalValueException() {
        for (String status : new String[] {"", "unknown", "present absent"}) {
            JsonAdaptedStudent adapted = new JsonAdaptedStudent(VALID_NAME, VALID_STUDENT_ID,
                    Map.of(3, status), Map.of());
            assertThrows(IllegalValueException.class, AttendanceStatus.MESSAGE_CONSTRAINTS, adapted::toModelType);
        }
    }

    @Test
    public void toModelType_invalidWeeklyRecord_throwsIllegalValueException() throws Exception {
        String json = "{\"name\":\"Ben Lim\",\"studentId\":\"A0234567Y\","
                + "\"attendance\":{\"14\":\"PRESENT\"}}";
        JsonAdaptedStudent adapted = JsonUtil.fromJsonString(json, JsonAdaptedStudent.class);

        assertThrows(IllegalValueException.class, "Week must be a whole number from 1 to 13.", adapted::toModelType);

        Map<Integer, String> attendanceWithNullValue = new HashMap<>();
        attendanceWithNullValue.put(1, null);
        JsonAdaptedStudent adaptedWithNullValue = new JsonAdaptedStudent(VALID_NAME, VALID_STUDENT_ID,
                attendanceWithNullValue, Map.of());
        assertThrows(IllegalValueException.class, "Weekly record value must not be null.",
                adaptedWithNullValue::toModelType);
    }

    //@@author limenxi07
    @Test
    public void toModelType_invalidParticipationScore_throwsIllegalValueException() throws Exception {
        String aboveRange = "{\"name\":\"Ben Lim\",\"studentId\":\"A0234567Y\","
                + "\"participation\":{\"3\":6}}";
        JsonAdaptedStudent adaptedAboveRange = JsonUtil.fromJsonString(aboveRange, JsonAdaptedStudent.class);
        assertThrows(IllegalValueException.class,
                "Participation score must be a whole number from 0 to 5.", adaptedAboveRange::toModelType);

        String belowRange = "{\"name\":\"Ben Lim\",\"studentId\":\"A0234567Y\","
                + "\"participation\":{\"1\":-1}}";
        JsonAdaptedStudent adaptedBelowRange = JsonUtil.fromJsonString(belowRange, JsonAdaptedStudent.class);
        assertThrows(IllegalValueException.class,
                "Participation score must be a whole number from 0 to 5.", adaptedBelowRange::toModelType);
    }

    //@@author
    @Test
    public void toModelType_fractionalParticipationScore_throwsIllegalValueException() {
        JsonAdaptedStudent adapted = new JsonAdaptedStudent(VALID_NAME, VALID_STUDENT_ID, Map.of(), Map.of(3, 4.5));
        assertThrows(IllegalValueException.class, JsonAdaptedStudent.MESSAGE_INVALID_PARTICIPATION_SCORE,
                adapted::toModelType);
    }

    @Test
    public void toModelType_participationScoreWithZeroFraction_returnsWholeScore() throws Exception {
        JsonAdaptedStudent adapted = new JsonAdaptedStudent(VALID_NAME, VALID_STUDENT_ID, Map.of(), Map.of(3, 5.0));
        assertEquals(BEN.withParticipationRecord(3, 5), adapted.toModelType());
    }

    @Test
    public void toModelType_unnormalizedDetails_returnsNormalizedStudent() throws Exception {
        JsonAdaptedStudent student = new JsonAdaptedStudent("  Ben   Lim ", " a0234567y ");
        assertEquals(BEN, student.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(INVALID_NAME, VALID_STUDENT_ID);
        assertThrows(IllegalValueException.class, StudentName.MESSAGE_CONSTRAINTS, student::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(null, VALID_STUDENT_ID);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "name");
        assertThrows(IllegalValueException.class, expectedMessage, student::toModelType);
    }

    @Test
    public void toModelType_invalidStudentId_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, INVALID_STUDENT_ID);
        assertThrows(IllegalValueException.class, StudentId.MESSAGE_CONSTRAINTS, student::toModelType);
    }

    @Test
    public void toModelType_nullStudentId_throwsIllegalValueException() {
        JsonAdaptedStudent student = new JsonAdaptedStudent(VALID_NAME, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "studentId");
        assertThrows(IllegalValueException.class, expectedMessage, student::toModelType);
    }
}
