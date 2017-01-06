/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.emc.pravega.common.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An Executor that runs commands that don't have a delay inline when they are submitted.
 * Delayed tasks are run on a background thread.
 */
public class InlineExecutor implements ScheduledExecutorService {
    
    ScheduledExecutorService delayedExecutor = new ScheduledThreadPoolExecutor(1);

    @Override
    public void execute(Runnable command) {
        command.run();
    }

    @Override
    public void shutdown() {
        delayedExecutor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delayedExecutor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delayedExecutor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delayedExecutor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delayedExecutor.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        try {
            return CompletableFuture.completedFuture(task.call());
        } catch (Exception e) {
            return FutureHelpers.failedFuture(e);
        }
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        try {
            task.run();
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            return FutureHelpers.failedFuture(e);
        }
    }

    @Override
    public Future<?> submit(Runnable task) {
        try {
            task.run();
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return FutureHelpers.failedFuture(e);
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<Future<T>> result = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            result.add(submit(task));
        }
        return result;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return invokeAll(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        for (Callable<T> task : tasks) {
            try {
                return task.call();
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return invokeAny(tasks);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return delayedExecutor.schedule(command, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return delayedExecutor.schedule(callable, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return delayedExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return delayedExecutor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

}
