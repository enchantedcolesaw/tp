package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_NO_ACTIVE_GROUP =
            "No active tutorial group. Create or select a group first.";
    public static final String MESSAGE_UNKNOWN_GROUP =
            "Tutorial group %s does not exist. See groups with: group list";
    public static final String MESSAGE_DUPLICATE_GROUP = "A tutorial group named %s already exists.";
    public static final String MESSAGE_UNKNOWN_STUDENT =
            "Student ID %s does not exist in %s. See students in the active group with: student list";
    public static final String MESSAGE_UNKNOWN_ASSIGNMENT_TO_CHANGE =
            "Assignment %s does not exist in %s. See the group's assignments with: assignment list";
    public static final String MESSAGE_UNKNOWN_ASSIGNMENT = MESSAGE_UNKNOWN_ASSIGNMENT_TO_CHANGE
            + "\nOr create it with: assignment add a/%s";
    public static final String MESSAGE_UNKNOWN_COMMAND =
            "Unknown command '%s'. Enter help to see supported commands.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_DUPLICATE_FIELDS =
            "Repeated parameter(s): %s. Include each only once.";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return String.format(MESSAGE_DUPLICATE_FIELDS, String.join(" ", duplicateFields));
    }

}
