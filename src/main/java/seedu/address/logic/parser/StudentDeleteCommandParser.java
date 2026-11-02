package seedu.address.logic.parser;

import static seedu.address.logic.parser.FastDeleteOption.PREFIX_FAST;
import static seedu.address.logic.parser.StudentCommandParserUtil.PREFIX_ID;

import java.util.List;

import seedu.address.logic.commands.StudentDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses a student ID and optional fast-delete choice into a deletion command.
 */
public class StudentDeleteCommandParser implements Parser<StudentDeleteCommand> {
    public static final String MESSAGE_INVALID_FAST_DELETE_OPTION = FastDeleteOption.MESSAGE_INVALID;

    @Override
    public StudentDeleteCommand parse(String arguments) throws ParseException {
        TAssistArgumentParser parser = new TAssistArgumentParser(List.of(), List.of(PREFIX_ID, PREFIX_FAST),
                List.of(PREFIX_ID, PREFIX_FAST));
        ArgumentMultimap values = parser.parseWithoutExtraTextCheck(arguments);
        if (values.getValue(PREFIX_ID).isEmpty()) {
            parser.verifyNoMissingSpaceBefore(values, PREFIX_ID);
            throw new ParseException(StudentCommandParserUtil.getMissingParametersMessage(
                    StudentDeleteCommand.MESSAGE_USAGE, List.of(PREFIX_ID)));
        }
        if (values.getValue(PREFIX_FAST).isEmpty()) {
            parser.verifyNoMissingSpaceBefore(values, PREFIX_FAST);
        }
        StudentCommandParserUtil.verifyNoBlankValues(values, StudentDeleteCommand.MESSAGE_USAGE, PREFIX_ID);
        parser.verifyNoExtraText(values);
        return new StudentDeleteCommand(TAssistParserUtil.parseStudentId(values.getValue(PREFIX_ID).orElseThrow()),
                FastDeleteOption.isPresent(values));
    }
}
