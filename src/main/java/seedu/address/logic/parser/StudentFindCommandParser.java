package seedu.address.logic.parser;

import static seedu.address.logic.parser.StudentCommandParserUtil.PREFIX_ID;
import static seedu.address.logic.parser.StudentCommandParserUtil.PREFIX_NAME;

import java.util.List;

import seedu.address.logic.commands.StudentFindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.student.StudentName;
import seedu.address.model.student.StudentSearchPredicate;

/**
 * Parses optional name and ID fragments, requiring at least one nonblank filter.
 * Name particles such as {@code s/o} are part of the name fragment, as in {@code student add}.
 */
public class StudentFindCommandParser implements Parser<StudentFindCommand> {
    public static final String MESSAGE_MISSING_FILTER =
            "Include at least one search filter. Usage: " + StudentFindCommand.MESSAGE_USAGE;
    public static final String MESSAGE_BLANK_FILTER = "Search filter %s cannot be blank. Enter a name or ID fragment.";

    @Override
    public StudentFindCommand parse(String arguments) throws ParseException {
        TAssistArgumentParser parser = new TAssistArgumentParser(List.of(), List.of(PREFIX_NAME, PREFIX_ID),
                List.of(PREFIX_ID), StudentName.SLASH_PARTICLES);
        ArgumentMultimap values = parser.parse(arguments);
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_ID)) {
            if (values.getValue(prefix).isEmpty()) {
                parser.verifyNoMissingSpaceBefore(values, prefix);
            }
            if (values.getValue(prefix).isPresent() && values.getValue(prefix).orElseThrow().isBlank()) {
                throw new ParseException(String.format(MESSAGE_BLANK_FILTER, prefix));
            }
        }
        String nameFragment = values.getValue(PREFIX_NAME).orElse("");
        String idFragment = values.getValue(PREFIX_ID).orElse("");
        if (nameFragment.isEmpty() && idFragment.isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_FILTER);
        }
        return new StudentFindCommand(new StudentSearchPredicate(nameFragment, idFragment));
    }
}
