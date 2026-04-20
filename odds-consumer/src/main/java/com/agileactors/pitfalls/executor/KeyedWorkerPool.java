package com.agileactors.pitfalls.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KeyedWorkerPool {

    private final List<ExecutorService> executors;

    public KeyedWorkerPool(int numThreads) {
        this.executors = new ArrayList<>(numThreads);
        for (int i = 0; i < numThreads; i++) {
            int key = i;
            executors.add(Executors.newSingleThreadExecutor(
                r -> new Thread(r, "keyed-worker-" + key)
            ));
        }
    }

    /**
     * P4 — Stale Data <br>
     * By routing all tasks for the same key (e.g., eventId) to the same worker,<br>
     * message order is preserved per key, preventing out-of-order processing and stale data overwrites.
     */
    public void submit(long key, Runnable task) {
        int workerKey = Math.floorMod(key, executors.size());
        executors.get(workerKey).submit(task);
    }

    public void shutdown() {
        for (ExecutorService executor : executors) {
            executor.shutdown();
        }
    }
}
