package scb.schedule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
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
        DeadlineEngine deadlineEngine = populateBasic();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> deadlineEngine.poll(4l, consumer, 10));
    }

    @Test
    public void addSomeDeadline_WhenPolling_ThenNumberAndOrdering() {
        Consumer<Long> consumer = Mockito.mock(Consumer.class);

        DeadlineEngine deadlineEngine = populateBasic();
        Assertions.assertEquals(3, deadlineEngine.size());
        Assertions.assertEquals(3, deadlineEngine.poll(4l, consumer, 10));
        Assertions.assertEquals(0, deadlineEngine.size());

        deadlineEngine = populateBasic();
        Assertions.assertEquals(2, deadlineEngine.poll(2l, consumer, 10));
        Assertions.assertEquals(1, deadlineEngine.size());


        deadlineEngine = populateBasic();
        Assertions.assertEquals(1, deadlineEngine.poll(4l, consumer, 1));
        Assertions.assertEquals(2, deadlineEngine.size());

        deadlineEngine = populateBasic();
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
    public void addSomeEpochDeadline_WhenPolling_ThenNumberAndOrdering() throws ParseException {
        Consumer<Long> consumer = Mockito.mock(Consumer.class);

        DeadlineEngine deadlineEngine = populateEpoch();
        Assertions.assertEquals(3, deadlineEngine.size());
        Assertions.assertEquals(3, deadlineEngine.poll(Instant.now().toEpochMilli(), consumer, 10));
        Assertions.assertEquals(0, deadlineEngine.size());

        deadlineEngine = populateEpoch();
        Assertions.assertEquals(2, deadlineEngine.poll(Instant.now().minusSeconds(100l).toEpochMilli(), consumer, 10));
        Assertions.assertEquals(1, deadlineEngine.size());
    }

    @Test
    public void emptyDeadlineEngine_WhenPolling_ThenNothing() {
        Consumer<Long> consumer = Mockito.mock(Consumer.class);
        DeadlineEngine deadlineEngine = new DeadlineEngineImpl();
        Assertions.assertEquals(0, deadlineEngine.size());
        Assertions.assertEquals(0, deadlineEngine.poll(4l, consumer, 1));
        Assertions.assertEquals(0, deadlineEngine.poll(0l, consumer, 1));
    }

    private DeadlineEngine populateBasic() {
        DeadlineEngine deadlineEngine = new DeadlineEngineImpl();
        deadlineEngine.schedule(1l);
        deadlineEngine.schedule(2l);
        deadlineEngine.schedule(3l);
        return deadlineEngine;
    }

    private DeadlineEngine populateEpoch() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        Date date1 = simpleDateFormat.parse("1875/08/12 08:12:30");
        Date date2 = simpleDateFormat.parse("2021/08/12 08:12:32");
        DeadlineEngine deadlineEngine = new DeadlineEngineImpl();
        deadlineEngine.schedule(date1.getTime());
        deadlineEngine.schedule(date2.getTime());
        deadlineEngine.schedule(Instant.now().minusMillis(1l).toEpochMilli());
        return deadlineEngine;
    }
}