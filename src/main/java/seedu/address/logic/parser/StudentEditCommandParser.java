package seedu.address.logic.parser;

import static seedu.address.logic.parser.StudentCommandParserUtil.PREFIX_ID;
import static seedu.address.logic.parser.StudentCommandParserUtil.PREFIX_NAME;
import static seedu.address.logic.parser.StudentCommandParserUtil.PREFIX_NEW_ID;

import java.util.List;
import java.util.Optional;

import seedu.address.logic.commands.StudentEditCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

/**
 * Parses a student ID with a new name, a new student ID, or both into an edit command.
 * Name particles such as {@code s/o} are part of the name, not prefixes.
 */
public class StudentEditCommandParser implements Parser<StudentEditCommand> {
    public static final String MESSAGE_MISSING_ID = StudentCommandParserUtil.getMissingParametersMessage(
            StudentEditCommand.MESSAGE_USAGE, List.of(PREFIX_ID));
    public static final String MESSAGE_NOTHING_TO_EDIT =
            "Nothing to edit. Include a new name with n/ or a new student ID with nid/. Usage: "
                    + StudentEditCommand.MESSAGE_USAGE;

    @Override
    public StudentEditCommand parse(String arguments) throws ParseException {
        TAssistArgumentParser parser = new TAssistArgumentParser(List.of(),
                List.of(PREFIX_ID, PREFIX_NAME, PREFIX_NEW_ID), List.of(PREFIX_ID, PREFIX_NEW_ID),
                StudentName.SLASH_PARTICLES);
        ArgumentMultimap values = parser.parseWithoutExtraTextCheck(arguments);
        verifyRequiredValues(parser, values);
        StudentCommandParserUtil.verifyNoBlankValues(values, StudentEditCommand.MESSAGE_USAGE, PREFIX_ID, PREFIX_NAME,
                PREFIX_NEW_ID);
        parser.verifyNoExtraText(values);

        return new StudentEditCommand(TAssistParserUtil.parseStudentId(values.getValue(PREFIX_ID).orElseThrow()),
                parseNewName(values), parseNewStudentId(values));
    }

    /**
     * Checks that the student to edit and at least one change are given, reporting a missing prefix that was typed
     * right after another value without a space.
     */
    private static void verifyRequiredValues(TAssistArgumentParser parser, ArgumentMultimap values)
            throws ParseException {
        if (values.getValue(PREFIX_ID).isEmpty()) {
            parser.verifyNoMissingSpaceBefore(values, PREFIX_ID);
            throw new ParseException(MESSAGE_MISSING_ID);
        }
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_NEW_ID)) {
            if (values.getValue(prefix).isEmpty()) {
                parser.verifyNoMissingSpaceBefore(values, prefix);
            }
        }
        if (values.getValue(PREFIX_NAME).isEmpty() && values.getValue(PREFIX_NEW_ID).isEmpty()) {
            throw new ParseException(MESSAGE_NOTHING_TO_EDIT);
        }
    }

    private static Optional<StudentName> parseNewName(ArgumentMultimap values) throws ParseException {
        if (values.getValue(PREFIX_NAME).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(TAssistParserUtil.parseStudentName(values.getValue(PREFIX_NAME).orElseThrow()));
    }

    private static Optional<StudentId> parseNewStudentId(ArgumentMultimap values) throws ParseException {
        if (values.getValue(PREFIX_NEW_ID).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(TAssistParserUtil.parseStudentId(values.getValue(PREFIX_NEW_ID).orElseThrow()));
    }
}
