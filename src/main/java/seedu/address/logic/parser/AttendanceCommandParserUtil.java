package seedu.address.logic.parser;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.AttendanceStatus;

/**
 * Validates attendance values using shared week validation and attendance-specific messages.
 */
public final class AttendanceCommandParserUtil {
    public static final String MESSAGE_INVALID_WEEK = "Invalid week. Enter a number from 1 to 13.";

    private AttendanceCommandParserUtil() {}

    /**
     * Parses a teaching week using the shared validation rules.
     *
     * @throws ParseException if the week is not a whole number from 1 to 13.
     */
    public static int parseWeek(String week) throws ParseException {
        try {
            return TAssistParserUtil.parseWeek(week);
        } catch (ParseException exception) {
            throw new ParseException(MESSAGE_INVALID_WEEK, exception);
        }
    }

    /**
     * Parses a supported attendance status, ignoring case and surrounding whitespace.
     *
     * @throws ParseException if the status is invalid.
     */
    public static AttendanceStatus parseStatus(String status) throws ParseException {
        try {
            return AttendanceStatus.fromValue(status);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(AttendanceStatus.MESSAGE_CONSTRAINTS, exception);
        }
    }
}
