package seedu.address.logic.parser;

import java.util.List;

import seedu.address.logic.commands.AttendanceClearCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the student and week whose attendance mark is to be cleared.
 */
public class AttendanceClearCommandParser implements Parser<AttendanceClearCommand> {
    private static final Prefix PREFIX_STUDENT_ID = new Prefix("id/");
    private static final Prefix PREFIX_WEEK = new Prefix("w/");

    private final TAssistArgumentParser argumentParser = new TAssistArgumentParser(
            List.of(PREFIX_STUDENT_ID, PREFIX_WEEK), List.of(),
            List.of(PREFIX_STUDENT_ID, PREFIX_WEEK)).allowRepeated(PREFIX_STUDENT_ID);

    @Override
    public AttendanceClearCommand parse(String arguments) throws ParseException {
        ArgumentMultimap values = argumentParser.parse(arguments, AttendanceClearCommand.MESSAGE_USAGE);
        return new AttendanceClearCommand(
                TAssistParserUtil.parseStudentIds(values.getAllValues(PREFIX_STUDENT_ID)),
                AttendanceCommandParserUtil.parseWeek(values.getValue(PREFIX_WEEK).orElseThrow()));
    }
}
