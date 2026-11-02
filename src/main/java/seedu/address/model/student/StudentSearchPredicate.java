package seedu.address.model.student;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.text.Normalizer;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;

/**
 * Matches students whose names and IDs contain the supplied fragments, ignoring case and, in names, accents.
 */
public class StudentSearchPredicate implements Predicate<Student> {
    private final String nameFragment;
    private final String idFragment;

    /**
     * Creates a search using at least one nonblank fragment; an empty string omits that filter.
     */
    public StudentSearchPredicate(String nameFragment, String idFragment) {
        requireAllNonNull(nameFragment, idFragment);
        this.nameFragment = StringUtil.normalizeSpaces(nameFragment);
        this.idFragment = idFragment.trim();
        checkArgument(!this.nameFragment.isEmpty() || !this.idFragment.isEmpty(),
                "At least one search fragment is required.");
    }

    @Override
    public boolean test(Student student) {
        requireAllNonNull(student);
        return containsIgnoreCase(removeAccents(student.getName().toString()), removeAccents(nameFragment))
                && containsIgnoreCase(student.getStudentId().toString(), idFragment);
    }

    /**
     * Returns {@code value} without accent marks and with straight apostrophes, so that {@code José O’Neil} contains
     * {@code jose o'neil}.
     */
    private static String removeAccents(String value) {
        String plain = StudentName.withStraightApostrophes(value);
        return Normalizer.normalize(plain, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private boolean containsIgnoreCase(String value, String fragment) {
        for (int i = 0; i <= value.length() - fragment.length(); i++) {
            if (value.regionMatches(true, i, fragment, 0, fragment.length())) {
                return true;
            }
        }
        return false;
    }
}
