package net.spantree.log4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Original code Copyright (c) 2012 sndyuk <sanada@sndyuk.com>
 *
 * Modified by Cedric Hurst for use in Log4J Appender <cedric@spantree.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public abstract class DaemonAppender<E> implements Runnable {
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
    private static final Logger LOG = Logger.getLogger(DaemonAppender.class.getName());
    private AtomicBoolean start = new AtomicBoolean(false);
    private final BlockingQueue<E> queue;

    public DaemonAppender(int maxQueueSize) {
        this.queue = new LinkedBlockingQueue<E>(maxQueueSize);
    }

    protected void execute() {
        THREAD_POOL.execute(this);
    }

    public void log(E eventObject) {
        if (!queue.offer(eventObject)) {
            LOG.warning("Message queue is full. Ignore the message.");
        } else if (start.compareAndSet(false, true)) {
            execute();
        }

    }

    @Override
    public void run() {
        try {
            for (; ; ) {
                append(queue.take());
            }

        } catch (InterruptedException e) {
            // ignore the error and rerun.
            run();
        } catch (Exception e) {
            close();
        }

    }

    protected abstract void append(E rawData);

    protected void close() {
        synchronized (THREAD_POOL) {
            if (!THREAD_POOL.isShutdown()) {
                shutdownAndAwaitTermination(THREAD_POOL);
            }

        }

    }

    private static void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();// Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                // Cancel currently executing tasks
                pool.shutdownNow();
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate");
                }

            }

        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }

    }
}
