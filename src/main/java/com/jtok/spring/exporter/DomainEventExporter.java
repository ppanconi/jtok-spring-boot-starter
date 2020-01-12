package com.jtok.spring.exporter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface DomainEventExporter {
    void export(int partition);
}
