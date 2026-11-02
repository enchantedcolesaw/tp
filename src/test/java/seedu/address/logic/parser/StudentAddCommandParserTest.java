package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.blankValueMessage;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalStudents.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.StudentAddCommand;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;
import seedu.address.testutil.StudentBuilder;

public class StudentAddCommandParserTest {
    private final StudentAddCommandParser parser = new StudentAddCommandParser();

    /** Returns the error for missing {@code prefixes}, followed by the usage of student add. */
    private static String missingParameters(String prefixes) {
        return String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, prefixes)
                + " Usage: " + StudentAddCommand.MESSAGE_USAGE;
    }

    @Test
    public void parse_validInput_normalizesValuesInEitherOrder() {
        assertParseSuccess(parser, " n/ Alice   Tan id/a0123456x ", new StudentAddCommand(ALICE));
        assertParseSuccess(parser, "id/A0123456X n/Alice Tan", new StudentAddCommand(ALICE));
        assertParseSuccess(parser, "n/Élodie O'Neil id/1", new StudentAddCommand(new StudentBuilder()
                .withName("Élodie O'Neil").withStudentId("1").build()));
        assertParseSuccess(parser, "n/" + "A".repeat(60) + " id/" + "1".repeat(20),
                new StudentAddCommand(new StudentBuilder().withName("A".repeat(60))
                        .withStudentId("1".repeat(20)).build()));
    }

    @Test
    public void parse_slashParticleInName_keepsParticleInName() {
        String[][] cases = {
            {"n/Ravi s/o Kumar id/A0345678Z", "Ravi s/o Kumar"},
            {"id/A0345678Z n/Siti D/O Ahmad", "Siti D/O Ahmad"},
            {"n/Ahmad a/l Ismail id/A0345678Z", "Ahmad a/l Ismail"},
            {"n/ Nur   a/p  Hassan  id/A0345678Z", "Nur a/p Hassan"}
        };
        for (String[] testCase : cases) {
            assertParseSuccess(parser, testCase[0], new StudentAddCommand(new StudentBuilder()
                    .withName(testCase[1]).withStudentId("A0345678Z").build()));
        }
    }

    @Test
    public void parse_misplacedSlashParticle_reportsNameConstraints() {
        for (String name : new String[] {"Ravi s/o", "s/o Kumar", "Ravi s/o d/o Kumar"}) {
            assertParseFailure(parser, "n/" + name + " id/A0345678Z", StudentName.MESSAGE_CONSTRAINTS);
        }
        // A particle joined to the next word is still read as a prefix, with a hint to separate it.
        assertParseFailure(parser, "n/Ravi s/oKumar id/A0345678Z",
                "Unknown prefix 's/'. Allowed prefixes: n/ id/. If s/o is part of the value, put a space before and "
                        + "after it.");
        assertParseFailure(parser, "s/o n/Kumar id/A0345678Z",
                String.format(TAssistArgumentParser.MESSAGE_UNEXPECTED_TEXT, "s/o", "n/ id/"));
    }

    @Test
    public void parse_missingParameter_reportsUsage() {
        assertParseFailure(parser, "", missingParameters("n/ id/"));
        assertParseFailure(parser, "id/A0123456X", missingParameters("n/"));
        for (String input : new String[] {"n/Alice Tan", "n/Ravi s/o Kumar"}) {
            assertParseFailure(parser, input, missingParameters("id/"));
        }
    }

    @Test
    public void parse_nameEndingInIdBeforeSlash_reportsMissingParameter() {
        for (String input : new String[] {"n/David/Tan", "n/David/", "n/David/A1!",
            "n/David/" + "1".repeat(StudentId.MAX_LENGTH + 1)}) {
            assertParseFailure(parser, input, missingParameters("id/"));
        }
    }

    @Test
    public void parse_davidWithSeparatedId_preservesNameValidation() {
        assertParseSuccess(parser, "n/David Tan id/A1", new StudentAddCommand(new StudentBuilder()
                .withName("David Tan").withStudentId("A1").build()));
        assertParseSuccess(parser, "n/David s/o Tan id/A1", new StudentAddCommand(new StudentBuilder()
                .withName("David s/o Tan").withStudentId("A1").build()));
        assertParseFailure(parser, "n/David/Tan id/A1", StudentName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_missingSpaceBeforePrefix_reportsMissingSpace() {
        assertParseFailure(parser, "n/Aliceid/A0123456X",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "id/", "n/Aliceid/A0123456X"));
        assertParseFailure(parser, "id/A0123456Xn/Alice Tan",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "n/", "id/A0123456Xn/Alice"));
        assertParseFailure(parser, "n/Alice Tanid/A0123456X",
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "Tanid/", "n/ id/")
                + String.format(TAssistArgumentParser.MESSAGE_SEPARATE_PREFIX, "id/"));
    }

    @Test
    public void parse_missingNameBeforeExtraText_reportsMissingParameter() {
        assertParseFailure(parser, "id/A0123456X Alice Tan", missingParameters("n/"));
    }

    @Test
    public void parse_invalidValues_reportsFieldConstraints() {
        assertParseFailure(parser, "n/ id/A0123456X", blankValueMessage("n/", StudentAddCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "n/Alice id/", blankValueMessage("id/", StudentAddCommand.MESSAGE_USAGE));
        for (String name : new String[] {"123", "--", "A".repeat(61), "Alice!"}) {
            assertParseFailure(parser, "n/" + name + " id/A0123456X", StudentName.MESSAGE_CONSTRAINTS);
        }
        for (String id : new String[] {"ABC", "A1!", "1".repeat(21), "A1n/Alice"}) {
            assertParseFailure(parser, "n/Alice id/" + id, StudentId.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_nameWithoutLetters_explainsLetterRequirement() {
        String message = "Invalid student name. Use 1-60 letters, spaces, apostrophes, hyphens, or periods, "
                + "with at least one letter besides s/o, d/o, a/l, or a/p. "
                + "Between two parts of a name, you may also use s/o, d/o, a/l, or a/p.";
        for (String name : new String[] {"'-", "' s/o -"}) {
            assertParseFailure(parser, "n/" + name + " id/A1", message);
        }
    }

    @Test
    public void parse_malformedParameters_reportsSpecificError() {
        assertParseFailure(parser, "n/Alice n/Ben id/A1",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("n/")));
        assertParseFailure(parser, "n/Alice id/A1 id/A2",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("id/")));
        assertParseFailure(parser, "n/Alice id/A1 x/extra",
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "x/", "n/ id/"));
        assertParseFailure(parser, "Alice id/A1",
                String.format(TAssistArgumentParser.MESSAGE_UNEXPECTED_TEXT, "Alice", "n/ id/"));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
