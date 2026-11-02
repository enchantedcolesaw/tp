package seedu.address.logic.parser;

import java.util.List;

import seedu.address.logic.commands.AssignmentRenameCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses an assignment name and its new name into a renaming command.
 */
public class AssignmentRenameCommandParser implements Parser<AssignmentRenameCommand> {
    private static final Prefix PREFIX_ASSIGNMENT = new Prefix("a/");
    private static final Prefix PREFIX_NEW_NAME = new Prefix("n/");

    private final TAssistArgumentParser argumentParser = new TAssistArgumentParser(
            List.of(PREFIX_ASSIGNMENT, PREFIX_NEW_NAME), List.of());

    @Override
    public AssignmentRenameCommand parse(String arguments) throws ParseException {
        ArgumentMultimap values = argumentParser.parse(arguments, AssignmentRenameCommand.MESSAGE_USAGE);
        return new AssignmentRenameCommand(
                TAssistParserUtil.parseAssignmentName(values.getValue(PREFIX_ASSIGNMENT).orElseThrow()),
                TAssistParserUtil.parseAssignmentName(values.getValue(PREFIX_NEW_NAME).orElseThrow()));
    }
}
