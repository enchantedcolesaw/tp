package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addRemark_success() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person edited = new PersonBuilder(original).withRemark("Likes baseball").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, edited);

        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(edited));
        assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, "Likes baseball"), model,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_removeRemark_success() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person withRemark = new PersonBuilder(original).withRemark("Temporary").build();
        model.setPerson(original, withRemark);

        Person edited = new PersonBuilder(withRemark).withRemark("").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(withRemark, edited);

        String expectedMessage = String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS, Messages.format(edited));
        assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, ""), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Person original = model.getFilteredPersonList().get(0);
        Person edited = new PersonBuilder(original).withRemark("Filtered").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, edited);

        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(edited));
        assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, "Filtered"), model,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidPersonIndex_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RemarkCommand(outOfBoundIndex, "test"), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand standard = new RemarkCommand(INDEX_FIRST_PERSON, "remark");

        assertTrue(standard.equals(standard));
        assertTrue(standard.equals(new RemarkCommand(INDEX_FIRST_PERSON, "remark")));
        assertFalse(standard.equals(null));
        assertFalse(standard.equals(new ClearCommand()));
        assertFalse(standard.equals(new RemarkCommand(INDEX_SECOND_PERSON, "remark")));
        assertFalse(standard.equals(new RemarkCommand(INDEX_FIRST_PERSON, "other")));
    }

    @Test
    public void toStringMethod() {
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, "remark");
        assertEquals(RemarkCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", remark=remark}", command.toString());
    }
}
