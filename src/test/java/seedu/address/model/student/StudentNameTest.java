package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class StudentNameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StudentName(null));
    }

    @Test
    public void constructor_invalidStudentName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, StudentName.MESSAGE_CONSTRAINTS, () -> new StudentName(" "));
    }

    @Test
    public void constructor_extraSpaces_normalizesSpacesAndKeepsCapitalization() {
        assertEquals("Alice Tan", new StudentName("  Alice    Tan ").fullName);
    }

    @Test
    public void isValidStudentName() {
        // null name
        assertThrows(NullPointerException.class, () -> StudentName.isValidStudentName(null));

        // invalid names
        assertFalse(StudentName.isValidStudentName("")); // empty string
        assertFalse(StudentName.isValidStudentName("   ")); // spaces only
        assertFalse(StudentName.isValidStudentName("'-")); // no letters
        assertFalse(StudentName.isValidStudentName("Alice 2")); // contains a digit
        assertFalse(StudentName.isValidStudentName("Alice*")); // contains a symbol
        assertFalse(StudentName.isValidStudentName("Alice/Tan")); // a slash outside a particle
        assertFalse(StudentName.isValidStudentName("a".repeat(StudentName.MAX_LENGTH + 1))); // too long

        // valid names
        assertTrue(StudentName.isValidStudentName("A")); // one letter (boundary value)
        assertTrue(StudentName.isValidStudentName("Alice Tan")); // letters and a space
        assertTrue(StudentName.isValidStudentName("Mary-Jane O'Neil")); // hyphen and apostrophe
        assertTrue(StudentName.isValidStudentName("Mary O’Neil")); // curly apostrophe
        assertTrue(StudentName.isValidStudentName("José Ng")); // accented letter
        assertTrue(StudentName.isValidStudentName("a".repeat(StudentName.MAX_LENGTH))); // maximum length (boundary)
        assertTrue(StudentName.isValidStudentName("A s/o " + "b".repeat(StudentName.MAX_LENGTH - 6))); // with particle
        assertFalse(StudentName.isValidStudentName("A s/o " + "b".repeat(StudentName.MAX_LENGTH - 5))); // too long
    }

    @Test
    public void isValidStudentName_periods_acceptsInitialsAndAbbreviations() {
        for (String name : new String[] {"S. Ramanathan", "Mohd. Ali", "John Smith Jr.", "J.R. Tan",
            "S. s/o Kumar", "A".repeat(StudentName.MAX_LENGTH - 1) + "."}) {
            assertTrue(StudentName.isValidStudentName(name), name);
            assertEquals(name, new StudentName(name).fullName);
        }
        for (String name : new String[] {".", "...", ". '-", "S. Ramanathan2", "A".repeat(60) + "."}) {
            assertFalse(StudentName.isValidStudentName(name), name);
        }
    }

    @Test
    public void isValidStudentName_slashParticles() {
        // valid: each particle, in any case, between two parts of a name
        for (String name : new String[] {"Ravi s/o Kumar", "Siti d/o Ahmad", "Ahmad a/l Ismail", "Nur a/p Hassan",
            "RAVI S/O KUMAR", "Ravi S/o Kumar", "Ravi s/o Kumar s/o Raman", "Mary-Jane d/o O'Neil",
            "  Ravi   s/o   Kumar  "}) {
            assertTrue(StudentName.isValidStudentName(name), name);
        }

        // invalid: misplaced, repeated, joined or unknown particles, and slashes elsewhere
        for (String name : new String[] {"s/o Kumar", "Ravi s/o", "s/o", "Ravi s/o d/o Kumar", "Ravi s/oKumar",
            "Ravi s/ o Kumar", "Ravi s / o Kumar", "Ravi x/o Kumar", "Ravi w/o Kumar", "Ravi/Kumar", "' s/o -",
            "Ravi s//o Kumar"}) {
            assertFalse(StudentName.isValidStudentName(name), name);
        }

        assertEquals("Ravi S/O Kumar", new StudentName(" Ravi  S/O Kumar ").fullName);
    }

    @Test
    public void isSameName() {
        StudentName name = new StudentName("Alice Tan");

        assertTrue(name.isSameName(new StudentName("alice  TAN ")));
        assertFalse(name.isSameName(new StudentName("Alice")));
        assertTrue(new StudentName("Sean O’Brien").isSameName(new StudentName("sean o'brien")));
        assertThrows(NullPointerException.class, () -> name.isSameName(null));
    }

    @Test
    public void isSameName_canonicalUnicodeForms_matchesAndPreservesSpelling() {
        for (String[] names : new String[][] {{"José Tan", "Jose\u0301 Tan"},
            {"Åsa Tan", "A\u030asa Tan"}, {"Jo\u0301\u0327 Tan", "Jo\u0327\u0301 Tan"}}) {
            StudentName first = new StudentName(names[0]);
            StudentName second = new StudentName(names[1]);
            assertTrue(first.isSameName(second));
            assertTrue(second.isSameName(first));
            assertEquals(names[0], first.fullName);
            assertEquals(names[1], second.fullName);
            assertFalse(first.equals(second));
        }
        assertTrue(new StudentName("José O’Neil").isSameName(new StudentName(" jose\u0301   o'neil ")));
    }

    @Test
    public void isSameName_differentAccentsOrCompatibilityForms_remainDifferent() {
        StudentName accented = new StudentName("José Tan");
        assertFalse(accented.isSameName(new StudentName("Jose Tan")));
        assertFalse(accented.isSameName(new StudentName("Josè Tan")));
        assertFalse(new StudentName("\uFB01ona Tan").isSameName(new StudentName("fiona Tan")));
    }

    @Test
    public void equals() {
        StudentName name = new StudentName("Alice Tan");

        // same values -> returns true
        assertTrue(name.equals(new StudentName("Alice Tan")));

        // same values after normalizing spaces -> returns true
        assertTrue(name.equals(new StudentName(" Alice  Tan ")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new StudentName("Ben Lim")));
    }
}
