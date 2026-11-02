package seedu.address.logic.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.StudentId;

/**
 * Checks the required parameters of student commands before validating their values.
 * Every value except the name, such as an ID or a sort field, is a single word.
 */
final class StudentCommandParserUtil {
    static final Prefix PREFIX_NAME = new Prefix("n/");
    static final Prefix PREFIX_ID = new Prefix("id/");
    static final Prefix PREFIX_NEW_ID = new Prefix("nid/");

    private StudentCommandParserUtil() {}

    /**
     * Parses the allowed prefixes and reports missing parameters with the command's usage.
     */
    static ArgumentMultimap parse(String arguments, String usage, Prefix... prefixes) throws ParseException {
        return parse(arguments, usage, List.of(), prefixes);
    }

    /**
     * Parses the allowed prefixes, keeping {@code literalWords} such as {@code s/o} in values, and reports missing
     * parameters with the command's usage.
     */
    static ArgumentMultimap parse(String arguments, String usage, Collection<String> literalWords, Prefix... prefixes)
            throws ParseException {
        // Check presence here so the error includes this command's complete format.
        List<Prefix> singleWordPrefixes = Stream.of(prefixes)
                .filter(prefix -> !prefix.equals(PREFIX_NAME))
                .toList();
        TAssistArgumentParser parser = new TAssistArgumentParser(List.of(), List.of(prefixes), singleWordPrefixes,
                literalWords);
        ArgumentMultimap values = parser.parseWithoutExtraTextCheck(arguments);
        List<Prefix> missingPrefixes = new ArrayList<>();
        for (Prefix prefix : prefixes) {
            if (values.getValue(prefix).isEmpty()) {
                if (!PREFIX_ID.equals(prefix) || hasJoinedStudentId(values)) {
                    parser.verifyNoMissingSpaceBefore(values, prefix);
                }
                missingPrefixes.add(prefix);
            }
        }
        if (!missingPrefixes.isEmpty()) {
            throw new ParseException(getMissingParametersMessage(usage, missingPrefixes));
        }
        verifyNoBlankValues(values, usage, prefixes);
        parser.verifyNoExtraText(values);
        return values;
    }

    /**
     * Reports the first of {@code prefixes} that has a blank value as a missing value, followed by {@code usage}.
     */
    static void verifyNoBlankValues(ArgumentMultimap values, String usage, Prefix... prefixes) throws ParseException {
        for (Prefix prefix : prefixes) {
            if (values.getValue(prefix).filter(String::isBlank).isPresent()) {
                throw new ParseException(String.format(TAssistArgumentParser.MESSAGE_BLANK_VALUE, prefix)
                        + " Usage: " + usage);
            }
        }
    }

    /**
     * Returns the error naming {@code missingPrefixes}, followed by the command's {@code usage}.
     */
    static String getMissingParametersMessage(String usage, List<Prefix> missingPrefixes) {
        String prefixes = missingPrefixes.stream().map(Prefix::toString).collect(Collectors.joining(" "));
        return String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, prefixes) + " Usage: " + usage;
    }

    /**
     * Returns true if the name's first word hides an id/ prefix followed by a valid student ID.
     * A name fragment such as David/Tan has no valid ID after id/, so it needs the missing-parameter error.
     */
    private static boolean hasJoinedStudentId(ArgumentMultimap values) {
        String firstWord = values.getValue(PREFIX_NAME).orElse("").split("\\s+", 2)[0];
        int prefixStart = firstWord.lastIndexOf(PREFIX_ID.getPrefix());
        return prefixStart > 0
                && StudentId.isValidStudentId(firstWord.substring(prefixStart + PREFIX_ID.getPrefix().length()));
    }
}
