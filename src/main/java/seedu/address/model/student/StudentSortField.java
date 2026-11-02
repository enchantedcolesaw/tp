package seedu.address.model.student;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Defines the supported ascending student display orders.
 */
public enum StudentSortField {
    NAME("name", Comparator.comparing((Student student) -> student.getName().toString(), createNameOrder())
            .thenComparing(student -> student.getStudentId().toString())),
    ID("id", Comparator.comparing(student -> student.getStudentId().toString()));

    private final String keyword;
    private final Comparator<Student> comparator;

    StudentSortField(String keyword, Comparator<Student> comparator) {
        this.keyword = keyword;
        this.comparator = comparator;
    }

    /**
     * Returns an alphabetical order of names, word by word, so that "Tan Ah Kow" comes before "Tanaka Yuki".
     * It ignores case and places accented letters next to their plain letters.
     */
    private static Comparator<String> createNameOrder() {
        Collator collator = Collator.getInstance(Locale.ENGLISH);
        collator.setStrength(Collator.SECONDARY);
        return (name, otherName) -> {
            String[] words = name.split(" ");
            String[] otherWords = otherName.split(" ");
            for (int i = 0; i < Math.min(words.length, otherWords.length); i++) {
                int order = collator.compare(words[i], otherWords[i]);
                if (order != 0) {
                    return order;
                }
            }
            return Integer.compare(words.length, otherWords.length);
        };
    }

    public String getKeyword() {
        return keyword;
    }

    public Comparator<Student> getComparator() {
        return comparator;
    }
}
