package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_emptyRemark_isAllowed() {
        assertTrue(new Remark("").value.isEmpty());
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Likes baseball");

        assertTrue(remark.equals(new Remark("Likes baseball")));
        assertTrue(remark.equals(remark));
        assertFalse(remark.equals(null));
        assertFalse(remark.equals(5));
        assertFalse(remark.equals(new Remark("Dislikes baseball")));
    }

    @Test
    public void toStringMethod() {
        Remark remark = new Remark("Likes baseball");
        assertTrue(remark.toString().equals("Likes baseball"));
        assertEquals(remark.hashCode(), new Remark("Likes baseball").hashCode());
    }
}
