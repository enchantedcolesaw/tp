package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;

public class SampleDataUtilTest {

    @Test
    public void getSamplePersons_containsExpectedDefaultRemarks() {
        Person[] persons = SampleDataUtil.getSamplePersons();
        assertEquals(6, persons.length);
        for (Person person : persons) {
            assertEquals("", person.getRemark().value);
        }
    }
}
