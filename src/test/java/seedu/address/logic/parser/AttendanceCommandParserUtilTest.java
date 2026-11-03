package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.AttendanceStatus;

public class AttendanceCommandParserUtilTest {
    @Test
    public void parseWeek_validWeek_acceptsBoundariesAndSpaces() throws Exception {
        assertEquals(1, AttendanceCommandParserUtil.parseWeek(" 1 "));
        assertEquals(13, AttendanceCommandParserUtil.parseWeek("13"));
    }

    @Test
    public void parseWeek_invalidWeek_reportsAttendanceMessage() {
        for (String week : new String[] {"", "0", "14", "-1", "3.5", "three", "9".repeat(1000)}) {
            assertThrows(ParseException.class, AttendanceCommandParserUtil.MESSAGE_INVALID_WEEK, () -> {
                AttendanceCommandParserUtil.parseWeek(week);
            });
        }
    }

    @Test
    public void parseStatus_validAndInvalidStatus() throws Exception {
        assertEquals(AttendanceStatus.PRESENT, AttendanceCommandParserUtil.parseStatus(" Present "));
        assertEquals(AttendanceStatus.ABSENT, AttendanceCommandParserUtil.parseStatus("absent"));
        assertThrows(ParseException.class, AttendanceStatus.MESSAGE_CONSTRAINTS, () -> {
            AttendanceCommandParserUtil.parseStatus("unknown");
        });
    }
}
