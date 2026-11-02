package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Locale;

import seedu.address.commons.core.WorkspaceView;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses a screen name for the view command.
 */
public class ViewCommandParser implements Parser<ViewCommand> {
    @Override
    public ViewCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String screenName = args.trim();
        if (screenName.isEmpty() || screenName.split("\\s+").length != 1) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE));
        }

        String screenKeyword = screenName.toLowerCase(Locale.ROOT);
        if (screenKeyword.equals("student") || screenKeyword.equals("record")) {
            screenKeyword = screenKeyword + "s";
        }
        for (WorkspaceView view : WorkspaceView.values()) {
            if (view.getKeyword().equals(screenKeyword)) {
                return new ViewCommand(view);
            }
        }

        throw new ParseException(String.format(ViewCommand.MESSAGE_UNKNOWN_SCREEN, screenName));
    }
}
