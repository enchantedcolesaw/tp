package seedu.address.model.student;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Arrays;
import java.util.Locale;

/**
 * Represents whether a student attended a tutorial.
 */
public enum AttendanceStatus {
    PRESENT("present", "Present"),
    ABSENT("absent", "Absent"),
    LATE("late", "Late"),
    EXCUSED("excused", "Excused");

    public static final String MESSAGE_CONSTRAINTS = "Invalid attendance status. Use present, absent, late or excused.";

    private final String commandValue;
    private final String displayValue;

    AttendanceStatus(String commandValue, String displayValue) {
        this.commandValue = commandValue;
        this.displayValue = displayValue;
    }

    /**
     * Parses an attendance status, ignoring case and surrounding whitespace.
     *
     * @throws IllegalArgumentException if the value is not one of the supported attendance statuses.
     */
    public static AttendanceStatus fromValue(String value) {
        String normalizedValue = requireNonNull(value).trim().toLowerCase(Locale.ROOT);
        checkArgument(Arrays.stream(values())
                .anyMatch(status -> status.commandValue.equals(normalizedValue)), MESSAGE_CONSTRAINTS);
        return valueOf(normalizedValue.toUpperCase(Locale.ROOT));
    }

    /**
     * Returns how a stored attendance value is shown to the user, such as {@code Present} for {@code PRESENT}.
     * Commands and the data file store enum names, such as {@code PRESENT} or {@code EXCUSED}. Any other stored text
     * is returned unchanged, so that it is shown as it is instead of failing.
     */
    public static String formatStoredValue(String storedValue) {
        requireNonNull(storedValue);
        for (AttendanceStatus status : values()) {
            if (status.name().equals(storedValue)) {
                return status.getDisplayValue();
            }
        }
        return storedValue;
    }

    public String getCommandValue() {
        return commandValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    /** Returns whether the student missed the tutorial, with or without an accepted excuse. */
    public boolean isAbsence() {
        return this == ABSENT || this == EXCUSED;
    }
}
