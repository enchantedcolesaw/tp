package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.BEN;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.StudentBuilder;

public class StudentSearchPredicateTest {
    @Test
    public void test_fragments_matchesSubstringsIgnoringCase() {
        assertTrue(new StudentSearchPredicate("LiCe", "").test(ALICE));
        assertTrue(new StudentSearchPredicate("", "x").test(ALICE));
        assertTrue(new StudentSearchPredicate("CE   ta", "0123").test(ALICE));
        assertFalse(new StudentSearchPredicate("alice", "").test(BEN));
        assertFalse(new StudentSearchPredicate("alice", "0234").test(ALICE));
    }

    @Test
    public void test_accentedName_ignoresCaseAndAccents() {
        Student student = new StudentBuilder().withName("Élodie Tan").build();
        assertTrue(new StudentSearchPredicate("ÉLODIE", "").test(student));
        assertTrue(new StudentSearchPredicate("elodie", "").test(student));
        assertTrue(new StudentSearchPredicate("José", "").test(new StudentBuilder().withName("Jose Lim").build()));
        assertFalse(new StudentSearchPredicate("elodia", "").test(student));
        Student curlyApostrophe = new StudentBuilder().withName("Sean O’Brien").build();
        assertTrue(new StudentSearchPredicate("o'brien", "").test(curlyApostrophe));
    }

    @Test
    public void constructor_missingFragments_rejectsEmptySearch() {
        assertThrows(IllegalArgumentException.class, () -> new StudentSearchPredicate("  ", ""));
        assertThrows(NullPointerException.class, () -> new StudentSearchPredicate(null, "A1"));
        assertThrows(NullPointerException.class, () -> new StudentSearchPredicate("Alice", null));
    }
}
