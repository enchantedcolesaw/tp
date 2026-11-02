package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.WorkspaceView;

public class CommandResultTest {
    @Test
    public void forRecordListing_preservesListingAndDistinguishesNavigation() {
        CommandResult listing = CommandResult.forRecordListing("Week 3\nAlice | Present");
        assertTrue(listing.isRecordListing());
        assertEquals(WorkspaceView.RECORDS, listing.getView().orElseThrow());
        assertFalse(new CommandResult("Week 3\nAlice | Present", WorkspaceView.RECORDS).isRecordListing());
        assertNotEquals(listing, new CommandResult(listing.getFeedbackToUser(), WorkspaceView.RECORDS));
        assertEquals(listing, CommandResult.forRecordListing(listing.getFeedbackToUser()));
        assertEquals(listing.hashCode(), CommandResult.forRecordListing(listing.getFeedbackToUser()).hashCode());
        assertTrue(listing.withSavedChanges().isRecordListing());
    }

    @Test
    public void listingCommands_keptWhenSavedAndCompared() {
        CommandResult listing = CommandResult.forRecordListing("PS1 records", "assignment list a/PS1");
        assertEquals("assignment list a/PS1", listing.getListingCommand().orElseThrow());
        assertNotEquals(CommandResult.forRecordListing("PS1 records"), listing);
        assertTrue(CommandResult.forRecordListing("PS1 records").getListingCommand().isEmpty());

        CommandResult rename = CommandResult.forListingRename("Renamed", "assignment list a/PS1",
                "assignment list a/PS2");
        assertEquals(rename.getListingRename(), rename.withSavedChanges().getListingRename());
        assertEquals("assignment list a/PS2", rename.getListingRename().orElseThrow().getValue());
        assertFalse(rename.isRecordListing());
        assertNotEquals(new CommandResult("Renamed"), rename);
    }

    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));

        // different isCancelled value -> returns false
        assertFalse(commandResult.equals(CommandResult.forCancelled("feedback")));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());

        // different isCancelled value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), CommandResult.forCancelled("feedback").hashCode());
    }

    @Test
    public void forCancelled_marksResultAsCancelled() {
        CommandResult cancelled = CommandResult.forCancelled("feedback");
        assertTrue(cancelled.isCancelled());
        assertFalse(new CommandResult("feedback").isCancelled());
        assertEquals(cancelled, CommandResult.forCancelled("feedback"));
        assertTrue(cancelled.withSavedChanges().isCancelled());
    }

    @Test
    public void equals_screenNavigation_distinguishesTargets() {
        CommandResult result = new CommandResult("feedback", WorkspaceView.HELP);
        assertEquals(result, new CommandResult("feedback", WorkspaceView.HELP));
        assertEquals(result.hashCode(), new CommandResult("feedback", WorkspaceView.HELP).hashCode());
        assertNotEquals(result, new CommandResult("feedback", WorkspaceView.STUDENTS));
        assertNotEquals(result, new CommandResult("feedback"));
        assertTrue(new CommandResult("feedback").getView().isEmpty());
    }

    @Test
    public void forHelp_topic_opensHelpAtTopic() {
        CommandResult result = CommandResult.forHelp("feedback", "student");
        assertTrue(result.isShowHelp());
        assertEquals("student", result.getHelpTopic());
        assertEquals(result, CommandResult.forHelp("feedback", "student"));
        assertEquals(result.hashCode(), CommandResult.forHelp("feedback", "student").hashCode());
        assertNotEquals(result, CommandResult.forHelp("feedback", "group"));
        assertEquals(new CommandResult("feedback", true, false), CommandResult.forHelp("feedback", ""));
    }

    @Test
    public void withSavedChanges_preservesFeedbackAndNavigation() {
        CommandResult original = CommandResult.forHelp("feedback", "student");
        CommandResult saved = original.withSavedChanges();
        assertFalse(original.hasSavedChanges());
        assertTrue(saved.hasSavedChanges());
        assertEquals(original.getFeedbackToUser(), saved.getFeedbackToUser());
        assertEquals(original.getHelpTopic(), saved.getHelpTopic());
        assertTrue(saved.isShowHelp());
        assertNotEquals(original, saved);
        assertEquals(saved, original.withSavedChanges());
        assertEquals(saved.hashCode(), original.withSavedChanges().hashCode());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + ", view=null, helpTopic=, hasSavedChanges=false, "
                + "isCancelled=false, isRecordListing=false, listingCommand=null, listingRename=null}";
        assertEquals(expected, commandResult.toString());
    }
}
