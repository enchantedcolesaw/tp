package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.logic.help.HelpEntry;
import seedu.address.model.Model;

/**
 * Opens the in-app command reference, either from the top or at the commands of one topic.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Opens the command reference. "
            + "Add a topic to open it at that topic's commands.\n"
            + "Parameters: [TOPIC]\n"
            + "Topics: " + String.join(", ", HelpCatalog.TOPICS) + "\n"
            + "The plural forms groups, students, and assignments are also accepted.\n"
            + "Example: " + COMMAND_WORD + " student";

    public static final String MESSAGE_UNKNOWN_TOPIC =
            "Unknown help topic. Available topics: " + String.join(", ", HelpCatalog.TOPICS) + ".";
    public static final String MESSAGE_NO_COMMANDS_FOR_TOPIC = "No %1$s commands are available yet.";
    public static final String MESSAGE_SHOWING_HELP = "Showing the command reference.";
    public static final String MESSAGE_SHOWING_TOPIC = "Showing %1$s commands.";

    private final List<HelpEntry> entries;
    private final String topic;

    /**
     * Creates a command that opens the command reference at the top.
     */
    public HelpCommand(List<HelpEntry> entries) {
        this(entries, "");
    }

    /**
     * Creates a command that opens the command reference scrolled to {@code topic}.
     * An empty {@code topic}, or one with no entries, opens it at the top.
     */
    public HelpCommand(List<HelpEntry> entries, String topic) {
        this.entries = List.copyOf(requireNonNull(entries));
        this.topic = requireNonNull(topic);
    }

    @Override
    public CommandResult execute(Model model) {
        if (topic.isEmpty()) {
            return CommandResult.forHelp(MESSAGE_SHOWING_HELP, "");
        }

        if (HelpCatalog.filterByTopic(entries, topic).isEmpty()) {
            return CommandResult.forHelp(String.format(MESSAGE_NO_COMMANDS_FOR_TOPIC, topic), "");
        }
        return CommandResult.forHelp(String.format(MESSAGE_SHOWING_TOPIC, topic), topic);
    }

    @Override
    public boolean canRunWithoutSavedData() {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HelpCommand otherCommand)) {
            return false;
        }

        return entries.equals(otherCommand.entries) && topic.equals(otherCommand.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries, topic);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("topic", topic)
                .toString();
    }
}
