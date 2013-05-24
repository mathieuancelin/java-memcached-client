/**
 * Copyright (C) 2006-2009 Dustin Sallings
 * Copyright (C) 2009-2011 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */


package net.spy.memcached.internal;

import net.spy.memcached.compat.SpyObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public abstract class CompletableFuture<T> extends SpyObject implements Future<T> {

    private final CountDownLatch completeLatch = new CountDownLatch(1);

    private List<CompleteCallback<T>> callbacks =
            Collections.synchronizedList(new ArrayList<CompleteCallback<T>>());

    public static interface CompleteCallback<T> {
        public void onComplete(Future<T> future);
    }

    protected void complete() {
        synchronized (this) {
            if (!isComplete() && !callbacks.isEmpty()) {
                completeLatch.countDown();
            } else {
                return;
            }
        }
        for (CompleteCallback<T> callback : callbacks) {
            callback.onComplete(this);
        }
    }

    public void onComplete(CompleteCallback<T> callback) {
        synchronized (this) {
            if (!isComplete()) {
                callbacks.add(callback);
            }
        }
        if (isComplete()) {
            callback.onComplete(this);
        }
    }

    protected boolean isComplete() {
        synchronized (this) {
            return completeLatch.getCount() == 0;
        }
    }
}
