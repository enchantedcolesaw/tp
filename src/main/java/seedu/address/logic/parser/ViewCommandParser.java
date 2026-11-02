package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

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
        if (screenName.isEmpty()) {
            throw new ParseException(ViewCommand.MESSAGE_MISSING_SCREEN);
        }
        String[] words = screenName.split("\\s+", 2);
        if (words.length > 1) {
            throw new ParseException(String.format(ViewCommand.MESSAGE_EXTRA_TEXT, words[0], words[1]));
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
