package seedu.address.logic.parser;

import static seedu.address.logic.parser.FastDeleteOption.PREFIX_FAST;

import java.util.List;

import seedu.address.logic.commands.AssignmentDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses an assignment name and optional fast-delete choice into a deletion command.
 */
public class AssignmentDeleteCommandParser implements Parser<AssignmentDeleteCommand> {
    private static final Prefix PREFIX_ASSIGNMENT = new Prefix("a/");

    private final TAssistArgumentParser argumentParser = new TAssistArgumentParser(
            List.of(PREFIX_ASSIGNMENT), List.of(PREFIX_FAST), List.of(PREFIX_FAST));

    @Override
    public AssignmentDeleteCommand parse(String arguments) throws ParseException {
        ArgumentMultimap values = argumentParser.parse(arguments, AssignmentDeleteCommand.MESSAGE_USAGE);
        if (values.getValue(PREFIX_FAST).isEmpty()) {
            argumentParser.verifyNoMissingSpaceBefore(values, PREFIX_FAST);
        }
        return new AssignmentDeleteCommand(
                TAssistParserUtil.parseAssignmentName(values.getValue(PREFIX_ASSIGNMENT).orElseThrow()),
                FastDeleteOption.isPresent(values));
    }
}
