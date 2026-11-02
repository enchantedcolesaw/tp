package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.model.assignment.AssignmentRecord;
import seedu.address.model.group.GroupName;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

/**
 * Validates and normalizes values shared by TAssist feature parsers.
 */
public class TAssistParserUtil {
    public static final String MESSAGE_WEEK = "Invalid week. Enter a number from 1 to 13.";
    public static final String MESSAGE_PARTICIPATION_SCORE =
            "Invalid participation score. Enter a whole number from 0 to 5.";
    public static final String MESSAGE_GRADE = AssignmentRecord.MESSAGE_INVALID_GRADE;
    public static final String MESSAGE_SUBMISSION_STATUS = "Invalid submission status. Use yes or no.";
    public static final String MESSAGE_INVALID_STUDENT_ID_IN_LIST =
            "Invalid student ID '%s'. " + StudentId.MESSAGE_RULES;

    private TAssistParserUtil() {}

    /**
     * Returns whether a command keyword contains only lowercase letters.
     */
    public static boolean isValidCommandKeyword(String keyword) {
        requireNonNull(keyword);
        return keyword.matches("[a-z]+");
    }

    /**
     * Returns whether a prefix consists of a lowercase keyword followed by a slash.
     */
    public static boolean isValidPrefix(Prefix prefix) {
        requireNonNull(prefix);
        requireNonNull(prefix.getPrefix());
        return prefix.getPrefix().matches("[a-z]+/");
    }

    /**
     * Parses a student name using the domain's character, length, and whitespace rules.
     *
     * @throws ParseException if the name is invalid.
     */
    public static StudentName parseStudentName(String name) throws ParseException {
        requireNonNull(name);
        if (!StudentName.isValidStudentName(name)) {
            throw new ParseException(StudentName.MESSAGE_CONSTRAINTS);
        }
        return new StudentName(name);
    }

    /**
     * Parses a student ID using the domain's validation and uppercase normalization.
     *
     * @throws ParseException if the ID is invalid.
     */
    public static StudentId parseStudentId(String studentId) throws ParseException {
        requireNonNull(studentId);
        if (!StudentId.isValidStudentId(studentId)) {
            throw new ParseException(StudentId.MESSAGE_CONSTRAINTS);
        }
        return new StudentId(studentId);
    }

    /**
     * Parses student IDs as {@link #parseStudentId(String)} does, keeping the first of any IDs that are the same
     * once case is ignored. When there are several IDs, an invalid one is named in the error.
     *
     * @throws ParseException if any ID is invalid.
     */
    public static List<StudentId> parseStudentIds(List<String> studentIds) throws ParseException {
        requireNonNull(studentIds);
        Set<StudentId> parsedIds = new LinkedHashSet<>();
        for (String studentId : studentIds) {
            if (studentIds.size() > 1 && !studentId.isBlank() && !StudentId.isValidStudentId(studentId)) {
                // Among several IDs, the TA needs to know which one to correct.
                throw new ParseException(String.format(MESSAGE_INVALID_STUDENT_ID_IN_LIST, studentId.trim()));
            }
            parsedIds.add(parseStudentId(studentId));
        }
        return List.copyOf(parsedIds);
    }

    /**
     * Parses a group name using the domain's character, length, and whitespace rules.
     *
     * @throws ParseException if the name is invalid.
     */
    public static GroupName parseGroupName(String name) throws ParseException {
        requireNonNull(name);
        if (!GroupName.isValidGroupName(name)) {
            throw new ParseException(GroupName.MESSAGE_CONSTRAINTS);
        }
        return new GroupName(name);
    }

    /**
     * Parses an assignment name using the domain's character, length, and whitespace rules.
     *
     * @throws ParseException if the name is invalid.
     */
    public static AssignmentName parseAssignmentName(String name) throws ParseException {
        requireNonNull(name);
        if (!AssignmentName.isValidAssignmentName(name)) {
            throw new ParseException(AssignmentName.MESSAGE_CONSTRAINTS);
        }
        return new AssignmentName(name);
    }

    /**
     * Parses a whole-number week from 1 to 13, ignoring surrounding whitespace.
     *
     * @throws ParseException if the week is invalid.
     */
    public static int parseWeek(String week) throws ParseException {
        return parseWholeNumber(week, 1, 13, MESSAGE_WEEK);
    }

    /**
     * Parses a whole-number participation score from 0 to 5, ignoring surrounding whitespace.
     *
     * @throws ParseException if the score is invalid.
     */
    public static int parseParticipationScore(String score) throws ParseException {
        return parseWholeNumber(score, 0, 5, MESSAGE_PARTICIPATION_SCORE);
    }

    /**
     * Parses a whole-number grade from 0 to 100, ignoring surrounding whitespace.
     *
     * @throws ParseException if the grade is invalid.
     */
    public static int parseGrade(String grade) throws ParseException {
        return parseWholeNumber(grade, 0, 100, MESSAGE_GRADE);
    }

    /**
     * Parses yes or no as a submission status, ignoring case and surrounding whitespace.
     *
     * @throws ParseException If the status is neither yes nor no.
     */
    public static boolean parseSubmissionStatus(String status) throws ParseException {
        requireNonNull(status);
        return switch (status.trim().toLowerCase(Locale.ROOT)) {
            case "yes" -> true;
            case "no" -> false;
            default -> throw new ParseException(MESSAGE_SUBMISSION_STATUS);
        };
    }

    private static int parseWholeNumber(String value, int minimum, int maximum, String message) throws ParseException {
        requireNonNull(value);
        String trimmedValue = value.trim();
        if (!trimmedValue.matches("[0-9]+")) {
            throw new ParseException(message);
        }

        try {
            int number = Integer.parseInt(trimmedValue);
            if (number < minimum || number > maximum) {
                throw new ParseException(message);
            }
            return number;
        } catch (NumberFormatException e) {
            throw new ParseException(message, e);
        }
    }
}
