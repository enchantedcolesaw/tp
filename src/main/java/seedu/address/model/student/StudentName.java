package seedu.address.model.student;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import seedu.address.commons.util.StringUtil;

/**
 * Represents a student's name.
 * Guarantees: immutable; is valid as declared in {@link #isValidStudentName(String)}.
 * Leading and trailing spaces are removed and repeated internal spaces are treated as one space.
 * The capitalization entered by the user is kept.
 * Besides ordinary words, a name may contain the particles in {@link #SLASH_PARTICLES}, such as {@code s/o} in
 * {@code Ravi s/o Kumar}, as they appear on official records.
 */
public class StudentName {

    public static final int MAX_LENGTH = 60;

    /**
     * Name particles that contain a slash: s/o (son of), d/o (daughter of), a/l (anak lelaki, son of) and
     * a/p (anak perempuan, daughter of). Each stands between two parts of a name, and matching ignores case.
     */
    public static final List<String> SLASH_PARTICLES = List.of("s/o", "d/o", "a/l", "a/p");

    public static final String MESSAGE_CONSTRAINTS = "Invalid student name. Use 1-" + MAX_LENGTH
            + " letters, spaces, apostrophes, hyphens, or periods, with at least one letter besides s/o, d/o, a/l, "
            + "or a/p. Between two parts of a name, you may also use s/o, d/o, a/l, or a/p.";

    /** One word: letters (including accented letters), apostrophes (straight or curly), hyphens and periods. */
    private static final String WORD_REGEX = "[\\p{L}\\p{M}'’.-]+";

    private static final String SLASH_PARTICLE_REGEX = SLASH_PARTICLES.stream().map(Pattern::quote)
            .collect(Collectors.joining("|", "(?i:", ")"));

    /*
     * Words separated by single spaces, where a slash particle may stand alone between two words, with at least one
     * letter outside the particles. Only the particles contain slashes, so a letter not next to a slash is in a word.
     */
    public static final String VALIDATION_REGEX = "(?=.*(?<!/)\\p{L}(?!/))" + WORD_REGEX
            + "(?: (?:" + SLASH_PARTICLE_REGEX + " )?" + WORD_REGEX + ")*";

    public final String fullName;

    /**
     * Constructs a {@code StudentName}.
     *
     * @param name A valid student name.
     */
    public StudentName(String name) {
        requireNonNull(name);
        checkArgument(isValidStudentName(name), MESSAGE_CONSTRAINTS);
        fullName = StringUtil.normalizeSpaces(name);
    }

    /**
     * Returns true if a given string is a valid student name after its spaces are normalized.
     */
    public static boolean isValidStudentName(String test) {
        requireNonNull(test);
        String normalized = StringUtil.normalizeSpaces(test);
        return normalized.length() <= MAX_LENGTH && normalized.matches(VALIDATION_REGEX);
    }

    /**
     * Returns true if both names are canonically equivalent after ignoring capitalization and the kind of apostrophe.
     * Spaces are already normalized, so extra spaces do not make names differ.
     */
    public boolean isSameName(StudentName otherName) {
        requireNonNull(otherName);
        return Normalizer.normalize(withStraightApostrophes(fullName), Normalizer.Form.NFC)
                .equalsIgnoreCase(Normalizer.normalize(withStraightApostrophes(otherName.fullName),
                        Normalizer.Form.NFC));
    }

    /**
     * Returns {@code text} with each curly apostrophe, as pasted from documents, replaced by a straight one.
     */
    public static String withStraightApostrophes(String text) {
        return text.replace('’', '\'');
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof StudentName otherStudentName)) {
            return false;
        }

        return fullName.equals(otherStudentName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
