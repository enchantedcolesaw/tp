package seedu.address.logic.parser;

import static seedu.address.logic.parser.StudentCommandParserUtil.PREFIX_ID;

import seedu.address.logic.commands.StudentRecordsCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the student ID of a student records listing.
 */
public class StudentRecordsCommandParser implements Parser<StudentRecordsCommand> {
    @Override
    public StudentRecordsCommand parse(String arguments) throws ParseException {
        ArgumentMultimap values = StudentCommandParserUtil.parse(arguments, StudentRecordsCommand.MESSAGE_USAGE,
                PREFIX_ID);
        return new StudentRecordsCommand(TAssistParserUtil.parseStudentId(values.getValue(PREFIX_ID).orElseThrow()));
    }
}
