package seedu.address.model.student;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a student's ID, e.g. {@code A0123456X}.
 * Guarantees: immutable; is valid as declared in {@link #isValidStudentId(String)}.
 * Leading and trailing spaces are ignored. IDs are stored in uppercase, so they are compared case-insensitively.
 */
public class StudentId {

    public static final int MAX_LENGTH = 20;

    public static final String MESSAGE_RULES =
            "Use 1-" + MAX_LENGTH + " letters and digits, including at least one digit.";
    public static final String MESSAGE_CONSTRAINTS = "Invalid student ID. " + MESSAGE_RULES;

    /*
     * Letters and digits only, with at least one digit.
     */
    public static final String VALIDATION_REGEX = "(?=.*[0-9])[A-Za-z0-9]+";

    public final String value;

    /**
     * Constructs a {@code StudentId}.
     *
     * @param studentId A valid student ID.
     */
    public StudentId(String studentId) {
        requireNonNull(studentId);
        checkArgument(isValidStudentId(studentId), MESSAGE_CONSTRAINTS);
        value = studentId.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Returns true if a given string is a valid student ID after leading and trailing spaces are removed.
     */
    public static boolean isValidStudentId(String test) {
        requireNonNull(test);
        String trimmed = test.trim();
        return trimmed.length() <= MAX_LENGTH && trimmed.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof StudentId otherStudentId)) {
            return false;
        }

        return value.equals(otherStudentId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
