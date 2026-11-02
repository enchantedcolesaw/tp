package seedu.address.logic.parser;

import java.util.List;

import seedu.address.logic.commands.ParticipationClearCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the student and week whose participation score is to be cleared.
 */
public class ParticipationClearCommandParser implements Parser<ParticipationClearCommand> {
    private static final Prefix PREFIX_ID = new Prefix("id/");
    private static final Prefix PREFIX_WEEK = new Prefix("w/");

    private final TAssistArgumentParser argumentParser = new TAssistArgumentParser(
            List.of(PREFIX_ID, PREFIX_WEEK), List.of(),
            List.of(PREFIX_ID, PREFIX_WEEK)).allowRepeated(PREFIX_ID);

    @Override
    public ParticipationClearCommand parse(String arguments) throws ParseException {
        ArgumentMultimap values = argumentParser.parse(arguments, ParticipationClearCommand.MESSAGE_USAGE);
        return new ParticipationClearCommand(
                TAssistParserUtil.parseStudentIds(values.getAllValues(PREFIX_ID)),
                TAssistParserUtil.parseWeek(values.getValue(PREFIX_WEEK).orElseThrow()));
    }
}
