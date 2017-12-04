package org.eclipse.neoscada.contrib.tsdb.api;

import java.util.Optional;

import org.osgi.util.pushstream.PushEventSource;

public interface DaProducer
{
    Optional<PushEventSource<ValueChangeEvent>> getPushEventSource ();
}
