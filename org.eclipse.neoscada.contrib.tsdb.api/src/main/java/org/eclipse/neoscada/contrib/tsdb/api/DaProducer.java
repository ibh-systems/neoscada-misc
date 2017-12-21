package org.eclipse.neoscada.contrib.tsdb.api;

import java.util.Deque;
import java.util.Optional;

public interface DaProducer
{
    Optional<Deque<ValueChangeEvent>> getQueue ();
}
