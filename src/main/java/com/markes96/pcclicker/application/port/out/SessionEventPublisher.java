package com.markes96.pcclicker.application.port.out;

import com.markes96.pcclicker.application.port.out.event.SessionEvent;

/**
 * Tells the outside world what happens to the session. Events are published in order from
 * whichever thread produced them; implementations must return quickly and never block.
 */
@FunctionalInterface
public interface SessionEventPublisher {

    void publish(SessionEvent event);
}
