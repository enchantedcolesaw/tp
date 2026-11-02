package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.model.group.GroupName;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

public class TAssistParserUtilTest {
    @Test
    public void isValidCommandKeyword() {
        assertTrue(TAssistParserUtil.isValidCommandKeyword("attendance"));
        for (String keyword : new String[] {"", "Group", "ADD", "group add", "group/", " add "}) {
            assertFalse(TAssistParserUtil.isValidCommandKeyword(keyword));
        }
        assertThrows(NullPointerException.class, () -> TAssistParserUtil.isValidCommandKeyword(null));
    }

    @Test
    public void isValidPrefix() {
        assertTrue(TAssistParserUtil.isValidPrefix(new Prefix("n/")));
        assertTrue(TAssistParserUtil.isValidPrefix(new Prefix("id/")));
        for (String prefix : new String[] {"", "N/", "id", " id/", "id /", "id//"}) {
            assertFalse(TAssistParserUtil.isValidPrefix(new Prefix(prefix)));
        }
        assertThrows(NullPointerException.class, () -> TAssistParserUtil.isValidPrefix(null));
        assertThrows(NullPointerException.class, () -> TAssistParserUtil.isValidPrefix(new Prefix(null)));
    }

    @Test
    public void parseStudentName_valid_normalizesAndKeepsCapitalization() throws Exception {
        assertEquals("Mary-Jane O’Neil", TAssistParserUtil.parseStudentName(" Mary-Jane   O’Neil ").fullName);
        assertEquals("José Ng", TAssistParserUtil.parseStudentName("José\tNg").fullName);
        assertEquals("A", TAssistParserUtil.parseStudentName("A").fullName);
        assertEquals("a".repeat(60), TAssistParserUtil.parseStudentName("a".repeat(60)).fullName);
    }

    @Test
    public void parseStudentName_invalid_throwsConstraintMessage() {
        for (String name : new String[] {"", "  ", "Alice2", "Alice*", "a".repeat(61)}) {
            assertThrows(ParseException.class, StudentName.MESSAGE_CONSTRAINTS, () ->
                    TAssistParserUtil.parseStudentName(name));
        }
        assertThrows(NullPointerException.class, () -> TAssistParserUtil.parseStudentName(null));
    }

    @Test
    public void parseStudentId_valid_normalizesUppercaseAndIgnoresCase() throws Exception {
        StudentId studentId = TAssistParserUtil.parseStudentId(" a0123456x ");
        assertEquals("A0123456X", studentId.value);
        assertEquals(TAssistParserUtil.parseStudentId("A0123456X"), studentId);
        assertEquals("1", TAssistParserUtil.parseStudentId("1").value);
        assertEquals("1".repeat(20), TAssistParserUtil.parseStudentId("1".repeat(20)).value);
    }

    @Test
    public void parseStudentId_invalid_throwsConstraintMessage() {
        for (String studentId : new String[] {"", "ABC", "A 1", "A1-", "1".repeat(21)}) {
            assertThrows(ParseException.class, StudentId.MESSAGE_CONSTRAINTS, () ->
                    TAssistParserUtil.parseStudentId(studentId));
        }
        assertThrows(NullPointerException.class, () -> TAssistParserUtil.parseStudentId(null));
    }

    @Test
    public void parseGroupName_valid_normalizesSpacesAndIgnoresCase() throws Exception {
        GroupName groupName = TAssistParserUtil.parseGroupName(" T01   Thursday ");
        assertEquals("T01 Thursday", groupName.fullName);
        assertEquals(TAssistParserUtil.parseGroupName("t01 thursday"), groupName);
        assertEquals("T", TAssistParserUtil.parseGroupName("T").fullName);
        assertEquals("a".repeat(60), TAssistParserUtil.parseGroupName("a".repeat(60)).fullName);
    }

    @Test
    public void parseGroupName_invalid_throwsConstraintMessage() {
        for (String name : new String[] {"", "   ", "T01/T02", "a".repeat(61)}) {
            assertThrows(ParseException.class, GroupName.MESSAGE_CONSTRAINTS, () ->
                    TAssistParserUtil.parseGroupName(name));
        }
        assertThrows(NullPointerException.class, () -> TAssistParserUtil.parseGroupName(null));
    }

    @Test
    public void parseAssignmentName_valid_normalizesSpacesAndPreservesCapitalization() throws Exception {
        assertEquals(new AssignmentName("Problem Set 1"), TAssistParserUtil.parseAssignmentName(" Problem   Set\t1 "));
        assertEquals("Problem Set 1", TAssistParserUtil.parseAssignmentName(" Problem   Set\t1 ").fullName);
        assertEquals("Mid-term", TAssistParserUtil.parseAssignmentName("Mid-term").fullName);
        assertEquals("PS1.5", TAssistParserUtil.parseAssignmentName("PS1.5").fullName);
        assertEquals("Lab 2 (Graphs)", TAssistParserUtil.parseAssignmentName(" Lab  2 (Graphs) ").fullName);
        assertEquals("Kattis: Week 3", TAssistParserUtil.parseAssignmentName("Kattis: Week 3").fullName);
        assertEquals("A", TAssistParserUtil.parseAssignmentName("A").fullName);
        assertEquals("a".repeat(60), TAssistParserUtil.parseAssignmentName(" " + "a".repeat(60) + " ").fullName);
    }

    @Test
    public void parseAssignmentName_invalid_throwsConstraintMessage() {
        for (String name : new String[] {"", "  ", "--", "PS1/PS2", "PS1: Trees & Graphs", "a".repeat(61)}) {
            assertThrows(ParseException.class, AssignmentName.MESSAGE_CONSTRAINTS, () ->
                    TAssistParserUtil.parseAssignmentName(name));
        }
        assertThrows(NullPointerException.class, () -> TAssistParserUtil.parseAssignmentName(null));
    }

    @Test
    public void parseWholeNumbers_boundariesAndWhitespace_success() throws Exception {
        assertEquals(1, TAssistParserUtil.parseWeek("1"));
        assertEquals(13, TAssistParserUtil.parseWeek(" 13 "));
        assertEquals(0, TAssistParserUtil.parseParticipationScore("0"));
        assertEquals(5, TAssistParserUtil.parseParticipationScore(" 5 "));
        assertEquals(0, TAssistParserUtil.parseGrade("0"));
        assertEquals(100, TAssistParserUtil.parseGrade(" 100 "));
        assertEquals(1, TAssistParserUtil.parseWeek("01"));
    }

    @Test
    public void parseWholeNumbers_outOfRange_throwsFieldSpecificError() {
        for (String week : new String[] {"0", "14"}) {
            assertThrows(ParseException.class, TAssistParserUtil.MESSAGE_WEEK, () -> TAssistParserUtil.parseWeek(week));
        }
        assertThrows(ParseException.class, TAssistParserUtil.MESSAGE_PARTICIPATION_SCORE, () ->
                TAssistParserUtil.parseParticipationScore("6"));
        assertThrows(ParseException.class, TAssistParserUtil.MESSAGE_GRADE, () -> TAssistParserUtil.parseGrade("101"));
    }

    @Test
    public void parseWholeNumbers_malformedOrOverflow_throwsFieldSpecificError() {
        for (String value : new String[] {"", "  ", "-1", "+1", "1.5", "one", "1 2", "9".repeat(100)}) {
            assertThrows(ParseException.class, TAssistParserUtil.MESSAGE_WEEK, () ->
                    TAssistParserUtil.parseWeek(value));
            assertThrows(ParseException.class, TAssistParserUtil.MESSAGE_PARTICIPATION_SCORE, () ->
                    TAssistParserUtil.parseParticipationScore(value));
            assertThrows(ParseException.class, TAssistParserUtil.MESSAGE_GRADE, () ->
                    TAssistParserUtil.parseGrade(value));
        }
    }

    @Test
    public void parseWholeNumbers_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> TAssistParserUtil.parseWeek(null));
        assertThrows(NullPointerException.class, () -> TAssistParserUtil.parseParticipationScore(null));
        assertThrows(NullPointerException.class, () -> TAssistParserUtil.parseGrade(null));
    }

    @Test
    public void parseSubmissionStatus_yesAndNo_success() throws Exception {
        assertTrue(TAssistParserUtil.parseSubmissionStatus("yes"));
        assertFalse(TAssistParserUtil.parseSubmissionStatus(" NO "));
    }

    @Test
    public void parseSubmissionStatus_invalid_throwsSpecificError() {
        for (String status : new String[] {"", "true", "y", "submitted"}) {
            assertThrows(ParseException.class, TAssistParserUtil.MESSAGE_SUBMISSION_STATUS, () ->
                    TAssistParserUtil.parseSubmissionStatus(status));
        }
        assertThrows(NullPointerException.class, () -> TAssistParserUtil.parseSubmissionStatus(null));
    }

    @Test
    public void parseStudentIds_invalidIdAmongSeveral_namesInvalidId() throws Exception {
        assertEquals(List.of(new StudentId("A1"), new StudentId("C3")),
                TAssistParserUtil.parseStudentIds(List.of("a1", "C3", "A1")));
        String message = String.format(TAssistParserUtil.MESSAGE_INVALID_STUDENT_ID_IN_LIST, "B-2");
        assertThrows(ParseException.class, message, () -> TAssistParserUtil.parseStudentIds(List.of("A1", " B-2 ")));
        assertThrows(ParseException.class, StudentId.MESSAGE_CONSTRAINTS, ()
                -> TAssistParserUtil.parseStudentIds(List.of("B-2")));
    }
}
