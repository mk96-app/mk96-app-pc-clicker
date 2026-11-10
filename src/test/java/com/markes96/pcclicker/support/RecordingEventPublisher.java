package com.markes96.pcclicker.support;

import com.markes96.pcclicker.application.port.out.SessionEventPublisher;
import com.markes96.pcclicker.application.port.out.event.SessionEvent;
import java.util.ArrayList;
import java.util.List;

/** Keeps every published event so tests can assert on them. */
public final class RecordingEventPublisher implements SessionEventPublisher {

    private final List<SessionEvent> events = new ArrayList<>();

    @Override
    public void publish(SessionEvent event) {
        events.add(event);
    }

    public List<SessionEvent> events() {
        return List.copyOf(events);
    }

    public <T extends SessionEvent> List<T> eventsOfType(Class<T> type) {
        return events.stream().filter(type::isInstance).map(type::cast).toList();
    }

    public SessionEvent last() {
        return events.get(events.size() - 1);
    }

    public void clear() {
        events.clear();
    }
}
