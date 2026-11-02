package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * Helper functions for handling strings.
 */
public class StringUtil {

    /**
     * Returns true if the {@code sentence} contains the {@code word}.
     *   Ignores case, but a full word match is required.
     *   <br>examples:<pre>
     *       containsWordIgnoreCase("ABc def", "abc") == true
     *       containsWordIgnoreCase("ABc def", "DEF") == true
     *       containsWordIgnoreCase("ABc def", "AB") == false //not a full word match
     *       </pre>
     * @param sentence cannot be null
     * @param word cannot be null, cannot be empty, must be a single word
     */
    public static boolean containsWordIgnoreCase(String sentence, String word) {
        requireNonNull(sentence);
        requireNonNull(word);

        String preppedWord = word.trim();
        checkArgument(!preppedWord.isEmpty(), "Word parameter cannot be empty");
        checkArgument(preppedWord.split("\\s+").length == 1, "Word parameter should be a single word");

        String preppedSentence = sentence;
        String[] wordsInPreppedSentence = preppedSentence.split("\\s+");

        return Arrays.stream(wordsInPreppedSentence)
                .anyMatch(preppedWord::equalsIgnoreCase);
    }

    /**
     * Returns a detailed message of {@code t}, including the stack trace.
     */
    public static String getDetails(Throwable t) {
        requireNonNull(t);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return t.getMessage() + "\n" + sw.toString();
    }

    /**
     * Returns true if {@code s} represents a non-zero unsigned integer
     * e.g. 1, 2, 3, ..., {@code Integer.MAX_VALUE} <br>
     * Will return false for any other non-null string input
     * e.g. empty string, "-1", "0", "+1", and " 2 " (untrimmed), "3 0" (contains whitespace), "1 a" (contains letters)
     * @throws NullPointerException if {@code s} is null.
     */
    public static boolean isNonZeroUnsignedInteger(String s) {
        requireNonNull(s);

        try {
            int value = Integer.parseInt(s);
            return value > 0 && !s.startsWith("+"); // "+1" is successfully parsed by Integer#parseInt(String)
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Returns {@code s} without leading and trailing whitespace, and with each run of internal whitespace
     * replaced by a single space. Unicode spaces, such as the non-breaking space, count as whitespace.
     * e.g. {@code "  Alice   Tan "} becomes {@code "Alice Tan"}.
     *
     * @throws NullPointerException if {@code s} is null.
     */
    public static String normalizeSpaces(String s) {
        requireNonNull(s);
        return s.replaceAll("(?U)\\s+", " ").trim();
    }

    /**
     * Returns a hash code for {@code s} that ignores case, so that strings equal under
     * {@link String#equalsIgnoreCase(String)} have the same hash code.
     * Each Unicode code point is converted to uppercase and then lowercase, as {@code equalsIgnoreCase} does,
     * e.g. {@code "PSI"} and {@code "psı"} (with a dotless i) have the same hash code.
     *
     * @throws NullPointerException if {@code s} is null.
     */
    public static int hashCodeIgnoreCase(String s) {
        requireNonNull(s);
        int hash = 0;
        for (int codePoint : s.codePoints().toArray()) {
            hash = 31 * hash + Character.toLowerCase(Character.toUpperCase(codePoint));
        }
        return hash;
    }

    /**
     * Returns {@code count} followed by {@code noun}, adding "s" unless the count is 1, such as "1 student" or
     * "3 students".
     */
    public static String pluralize(int count, String noun) {
        requireNonNull(noun);
        return count + " " + noun + (count == 1 ? "" : "s");
    }
}
