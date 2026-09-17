package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.application.Platform;
import seedu.address.testutil.PersonBuilder;

@EnabledOnOs(OS.MAC)
public class PersonCardTest {

    @BeforeAll
    public static void startJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        try {
            Platform.startup(latch::countDown);
            latch.await();
        } catch (IllegalStateException e) {
            failure.set(e);
        }
        if (failure.get() != null && !failure.get().getMessage().contains("already been initialized")) {
            throw new AssertionError(failure.get());
        }
    }

    @Test
    public void constructor_loadsRemarkIntoCard() {
        PersonCard card = new PersonCard(new PersonBuilder().withRemark("Likes baseball").build(), 1);
        assertNotNull(card.getRoot());
    }
}
