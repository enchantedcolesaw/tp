package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AttendanceStatusTest {
    @Test
    public void fromValue_validStatus_normalizesCaseAndSpaces() {
        for (AttendanceStatus status : AttendanceStatus.values()) {
            assertEquals(status, AttendanceStatus.fromValue(" " + status.name() + " "));
            assertEquals(status, AttendanceStatus.fromValue(status.getCommandValue()));
            assertEquals(status, AttendanceStatus.fromValue(status.getDisplayValue()));
        }
        assertEquals("present", AttendanceStatus.PRESENT.getCommandValue());
        assertEquals("Absent", AttendanceStatus.ABSENT.getDisplayValue());
    }

    @Test
    public void fromValue_invalidStatus_throwsIllegalArgumentException() {
        for (String value : new String[] {"", " ", "unknown", "present absent", "p", "x".repeat(1000)}) {
            assertThrows(IllegalArgumentException.class, AttendanceStatus.MESSAGE_CONSTRAINTS, () -> {
                AttendanceStatus.fromValue(value);
            });
        }
        assertThrows(NullPointerException.class, () -> AttendanceStatus.fromValue(null));
    }

    @Test
    public void formatStoredValue_storedStatus_returnsDisplayValue() {
        assertEquals("Present", AttendanceStatus.formatStoredValue("PRESENT"));
        assertEquals("Absent", AttendanceStatus.formatStoredValue("ABSENT"));
    }

    @Test
    public void formatStoredValue_otherText_returnsItUnchanged() {
        assertEquals("present", AttendanceStatus.formatStoredValue("present"));
        assertEquals("Late", AttendanceStatus.formatStoredValue("Late"));
        assertThrows(NullPointerException.class, () -> AttendanceStatus.formatStoredValue(null));
    }
}
