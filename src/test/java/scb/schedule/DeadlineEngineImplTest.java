package scb.schedule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.Consumer;

public class DeadlineEngineImplTest {

    @Test
    public void nullHandler_WhenInstantiation_ThenException() {
        DeadlineEngine deadlineEngine = new DeadlineEngineImpl();
        Assertions.assertThrows(AssertionError.class, () -> deadlineEngine.poll(4l, null, 10));
    }

    @Test
    public void triggerUnsupportedDeadline__ThenException() {
        Consumer<Long> consumer = Mockito.mock(Consumer.class);
        Mockito.doThrow(new NullPointerException()).when(consumer).accept(Mockito.anyLong());
        DeadlineEngine deadlineEngine = populate();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> deadlineEngine.poll(4l, consumer, 10));
    }

    @Test
    public void addSomeDeadline_WhenPolling_ThenNumberAndOrdering() {
        Consumer<Long> consumer = Mockito.mock(Consumer.class);

        DeadlineEngine deadlineEngine = populate();
        Assertions.assertEquals(3, deadlineEngine.size());
        Assertions.assertEquals(3, deadlineEngine.poll(4l, consumer, 10));
        Assertions.assertEquals(0, deadlineEngine.size());

        deadlineEngine = populate();
        Assertions.assertEquals(2, deadlineEngine.poll(2l, consumer, 10));
        Assertions.assertEquals(1, deadlineEngine.size());


        deadlineEngine = populate();
        Assertions.assertEquals(1, deadlineEngine.poll(4l, consumer, 1));
        Assertions.assertEquals(2, deadlineEngine.size());

        deadlineEngine = populate();
        Assertions.assertEquals(0, deadlineEngine.poll(0l, consumer, 1));
        Assertions.assertEquals(3, deadlineEngine.size());

        Assertions.assertEquals(0, deadlineEngine.poll(1l, consumer, 0));
        Assertions.assertEquals(3, deadlineEngine.size());

        Assertions.assertFalse(deadlineEngine.cancel(4l));
        long handlerId4 = deadlineEngine.schedule(4l);
        Assertions.assertTrue(deadlineEngine.cancel(handlerId4));
        Assertions.assertEquals(3, deadlineEngine.size());

    }

    @Test
    public void emptyDeadlineEngine_WhenPolling_ThenNothing() {
        Consumer<Long> consumer = Mockito.mock(Consumer.class);
        DeadlineEngine deadlineEngine = new DeadlineEngineImpl();
        Assertions.assertEquals(0, deadlineEngine.size());
        Assertions.assertEquals(0, deadlineEngine.poll(4l, consumer, 1));
        Assertions.assertEquals(0, deadlineEngine.poll(0l, consumer, 1));
    }

    private DeadlineEngine populate() {
        DeadlineEngine deadlineEngine = new DeadlineEngineImpl();
        deadlineEngine.schedule(1l);
        deadlineEngine.schedule(2l);
        deadlineEngine.schedule(3l);
        return deadlineEngine;
    }
}