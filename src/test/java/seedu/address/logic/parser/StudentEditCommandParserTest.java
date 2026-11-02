package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.blankValueMessage;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalStudents.ALICE;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.StudentEditCommand;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

public class StudentEditCommandParserTest {
    private static final String ALLOWED_PREFIXES = "id/ n/ nid/";

    private final StudentEditCommandParser parser = new StudentEditCommandParser();

    @Test
    public void parse_nameOnly_success() {
        assertParseSuccess(parser, " id/ a0123456x  n/ Alicia   Tan ", new StudentEditCommand(ALICE.getStudentId(),
                Optional.of(new StudentName("Alicia Tan")), Optional.empty()));
    }

    @Test
    public void parse_newIdOnly_success() {
        assertParseSuccess(parser, "nid/a0123457x id/A0123456X", new StudentEditCommand(ALICE.getStudentId(),
                Optional.empty(), Optional.of(new StudentId("A0123457X"))));
    }

    @Test
    public void parse_nameAndNewId_successInAnyOrder() {
        StudentEditCommand expected = new StudentEditCommand(ALICE.getStudentId(),
                Optional.of(new StudentName("Alicia Tan")), Optional.of(new StudentId("A0123457X")));
        assertParseSuccess(parser, "id/A0123456X n/Alicia Tan nid/A0123457X", expected);
        assertParseSuccess(parser, "nid/A0123457X n/Alicia Tan id/A0123456X", expected);
    }

    @Test
    public void parse_nameWithSlashParticle_keepsParticleInName() {
        assertParseSuccess(parser, "id/A0123456X n/Ravi s/o Kumar", new StudentEditCommand(ALICE.getStudentId(),
                Optional.of(new StudentName("Ravi s/o Kumar")), Optional.empty()));
    }

    @Test
    public void parse_missingStudentId_reportsUsage() {
        String usage = String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, "id/") + " Usage: "
                + StudentEditCommand.MESSAGE_USAGE;
        assertParseFailure(parser, "", usage);
        assertParseFailure(parser, "n/Alicia Tan", usage);
        assertParseFailure(parser, "nid/A0123457X", usage);
        assertParseFailure(parser, "n/Alicia Tan nid/A0123457X", usage);
    }

    @Test
    public void parse_nothingToEdit_reportsHowToEdit() {
        assertParseFailure(parser, "id/A0123456X", StudentEditCommandParser.MESSAGE_NOTHING_TO_EDIT);
    }

    @Test
    public void parse_invalidValues_reportsConstraintOfThatValue() {
        assertParseFailure(parser, "id/ABC n/Alicia", StudentId.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "id/A1 n/Alicia1", StudentName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "id/A1 n/", blankValueMessage("n/", StudentEditCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "id/A1 n/" + "a".repeat(61), StudentName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "id/A1 nid/ABC", StudentId.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "id/A1 nid/", blankValueMessage("nid/", StudentEditCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "id/ nid/A2", blankValueMessage("id/", StudentEditCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_malformedParameters_reportsSpecificError() {
        assertParseFailure(parser, "id/A1 id/A2 n/Alicia",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("id/")));
        assertParseFailure(parser, "id/A1 nid/A2 nid/A3",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("nid/")));
        assertParseFailure(parser, "id/A1 n/Alicia n/Alice",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("n/")));
        assertParseFailure(parser, "id/A1 f/yes",
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "f/", ALLOWED_PREFIXES));
        assertParseFailure(parser, "A1 n/Alicia",
                String.format(TAssistArgumentParser.MESSAGE_UNEXPECTED_TEXT, "A1", ALLOWED_PREFIXES));
    }

    @Test
    public void parse_extraTextAfterIdValue_reportsExtraText() {
        assertParseFailure(parser, "id/A0123456X now n/Alicia",
                String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "id/A0123456X", "now"));
        assertParseFailure(parser, "id/A0123456X nid/A0123457X now",
                String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "nid/A0123457X", "now"));
    }

    @Test
    public void parse_missingSpaceBeforePrefix_reportsMissingSpace() {
        assertParseFailure(parser, "id/A0123456Xnid/A0123457X",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "nid/", "id/A0123456Xnid/A0123457X"));
        assertParseFailure(parser, "id/A0123456Xn/Alicia",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "n/", "id/A0123456Xn/Alicia"));
        assertParseFailure(parser, "n/Aliciaid/A0123456X",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "id/", "n/Aliciaid/A0123456X"));
        assertParseFailure(parser, "id/A0123456Xnid/A0123457X n/Alicia",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "nid/", "id/A0123456Xnid/A0123457X"));
        assertParseFailure(parser, "id/A0123456X nid/A0123457Xn/Alicia",
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "n/", "nid/A0123457Xn/Alicia"));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
