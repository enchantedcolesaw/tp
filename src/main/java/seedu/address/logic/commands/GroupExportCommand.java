package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;
import seedu.address.storage.GroupRecordsCsvExporter;

/**
 * Exports the attendance, participation, and assignment records of the active tutorial group to a CSV file.
 * Exporting reads the data only, so it never changes or saves TAssist data.
 */
public class GroupExportCommand extends Command {
    public static final String MESSAGE_USAGE = "group export";
    public static final String MESSAGE_SUCCESS = "Exported the records of %1$s in %2$s to:\n%3$s";
    public static final String MESSAGE_EXPORT_FAILED = "Could not export %1$s to %2$s. "
            + "Check that TAssist can write to that folder and that the file is not open in another app.";

    private static final Logger logger = LogsCenter.getLogger(GroupExportCommand.class);

    private final GroupRecordsCsvExporter exporter;

    /**
     * Creates a command that exports the active group with {@code exporter}.
     */
    public GroupExportCommand(GroupRecordsCsvExporter exporter) {
        this.exporter = requireNonNull(exporter);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = getActiveGroup(model);
        try {
            Path exportFile = exporter.export(group);
            return new CommandResult(String.format(MESSAGE_SUCCESS, describeStudentCount(group), group.getName(),
                    exportFile));
        } catch (IOException e) {
            logger.warning("Exporting " + group.getName() + " failed: " + e);
            throw new CommandException(String.format(MESSAGE_EXPORT_FAILED, group.getName(),
                    exporter.getExportFile(group.getName())), e);
        }
    }

    /**
     * Returns the number of students in {@code group} with a noun that agrees, such as "1 student".
     */
    private static String describeStudentCount(Group group) {
        return StringUtil.pluralize(group.getStudentList().size(), "student");
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof GroupExportCommand otherCommand && exporter.equals(otherCommand.exporter);
    }

    @Override
    public int hashCode() {
        return exporter.hashCode();
    }
}
