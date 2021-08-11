package scb.schedule;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;

/**
 * Basic
 */
@NoArgsConstructor
public class DeadlineEngineImpl implements DeadlineEngine {

    @Getter
    private static final class Deadline implements Comparable<Deadline> {
        private final long id;
        private final long deadlineMs;

        Deadline(long deadlineMs) {
            this.id = UUID.randomUUID().getMostSignificantBits();
            this.deadlineMs = deadlineMs;
        }

        @Override
        public int compareTo(Deadline o) {
            if (this.getDeadlineMs() == o.getDeadlineMs()) {
                return 0;
            } else {
                return this.getDeadlineMs() > o.getDeadlineMs() ? 1 : -1;
            }
        }
    }

    private final NavigableSet<Deadline> set = new ConcurrentSkipListSet<>();
    private final Map<Long, Deadline> map = new ConcurrentHashMap<>();

    @Override
    public long schedule(long deadlineMs) {
        assert deadlineMs >= 0;
        Deadline deadline = new Deadline(deadlineMs);
        set.add(deadline);
        map.put(Long.valueOf(deadline.getId()), deadline);
        return deadline.getId();
    }

    @Override
    public boolean cancel(long requestId) {
        Deadline deadline = map.remove(requestId);
        if (deadline != null) {
            return set.remove(deadline);
        }
        return false;
    }

    /**
     * Considering any handler exception as a stopper
     * @param nowMs   time in millis since epoch to check deadlines against.
     * @param handler to call with identifier of expired deadlines.
     * @param maxPoll count of maximum number of expired deadlines to process.
     * @return number of expired deadlines that fired successfully.
     */
    @Override
    public int poll(long nowMs, Consumer<Long> handler, int maxPoll) {
        assert handler != null;
        Deadline boundaryDeadline = new Deadline(nowMs);
        SortedSet<Deadline> candidateDeadlines = set.headSet(boundaryDeadline, true);
        Set<Deadline> processedDeadlines = new HashSet<>();
        candidateDeadlines.parallelStream().limit(maxPoll).forEach(deadlineElem -> {
            try {
                handler.accept(deadlineElem.getId());
            } catch (Exception e) {
                throw new UnsupportedOperationException("Handler processing exception", e);
            }
            processedDeadlines.add(deadlineElem);
        });
        candidateDeadlines.removeAll(processedDeadlines);
        return processedDeadlines.size();
    }

    /**
     * Inaccurate with multithreading
     *
     * @return the number of registered deadlines.
     */
    @Override
    public int size() {
        return map.size();
    }
}