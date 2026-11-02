package seedu.address.logic.help;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import seedu.address.model.TAssistHistory;

/**
 * Combines built-in command help with entries registered by TAssist features.
 */
public final class HelpCatalog {
    /** Topics accepted by {@code help TOPIC}, in the order they are shown to the user. */
    public static final List<String> TOPICS = List.of("group", "student", "attendance", "participation",
            "assignment");

    private static final List<HelpEntry> BUILT_IN_ENTRIES = List.of(
            new HelpEntry("", "view SCREEN",
                    "Open a screen: students, records, help, or storage. "
                            + "The singular forms student and record are also accepted. Screen names ignore case. "
                            + "Opening a screen does not change any data.",
                    List.of("view storage")),
            new HelpEntry("", "help [TOPIC]",
                    "Open this reference, or open it at the commands of one topic: "
                            + String.join(", ", TOPICS)
                            + ". The plural forms groups, students, and assignments are also accepted. "
                            + "F1 also opens Help; Escape returns to the command box.",
                    List.of("help", "help student")),
            new HelpEntry("", "undo",
                    "Undo the latest change to groups, students, attendance, participation, or assignments, including "
                            + "switching groups. Repeat to undo up to " + TAssistHistory.MAX_CHANGES
                            + " changes made since TAssist was opened. The restored data is saved.",
                    List.of("undo")),
            new HelpEntry("", "redo",
                    "Redo the latest change reversed by undo. Making a new change clears the changes to redo.",
                    List.of("redo")),
            new HelpEntry("", "exit",
                    "Close TAssist. Takes no parameters. Your data is already saved after each change.",
                    List.of("exit")));

    private HelpCatalog() {
    }

    /**
     * Returns the built-in commands followed by commands registered by features.
     * Feature commands are grouped by topic, in the order of {@link #TOPICS}, so that the reference reads in the
     * same order as the topics that {@code help TOPIC} accepts. Commands keep their given order within a topic.
     *
     * @param featureEntries The help entries registered with feature command parsers.
     * @return An immutable list of all help entries.
     */
    public static List<HelpEntry> getEntries(List<HelpEntry> featureEntries) {
        requireNonNull(featureEntries);
        List<HelpEntry> entries = new ArrayList<>(BUILT_IN_ENTRIES);
        featureEntries.stream()
                .sorted(Comparator.comparingInt(entry -> getTopicOrder(entry.getTopic())))
                .forEachOrdered(entries::add);
        return List.copyOf(entries);
    }

    /**
     * Returns the position of {@code topic} in {@link #TOPICS}. Topics that are not listed come after all others.
     */
    private static int getTopicOrder(String topic) {
        int position = TOPICS.indexOf(topic.toLowerCase(Locale.ROOT));
        return position < 0 ? TOPICS.size() : position;
    }

    /**
     * Returns whether {@code topic} can be requested with {@code help TOPIC}. Matching ignores case.
     */
    public static boolean isValidTopic(String topic) {
        requireNonNull(topic);
        return TOPICS.contains(topic.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the entries whose topic is {@code topic}, ignoring case, in their original order.
     */
    public static List<HelpEntry> filterByTopic(List<HelpEntry> entries, String topic) {
        requireNonNull(entries);
        requireNonNull(topic);
        return entries.stream()
                .filter(entry -> entry.getTopic().equalsIgnoreCase(topic))
                .toList();
    }

    /**
     * Returns the heading shown above the commands of {@code topic}.
     * Existing general commands use the empty topic.
     */
    public static String getTopicHeading(String topic) {
        requireNonNull(topic);
        if (topic.isEmpty()) {
            return "General commands";
        }
        return Character.toUpperCase(topic.charAt(0)) + topic.substring(1) + " commands";
    }
}
