package com.cloud.common.rpc;

import lombok.Data;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Data
public class WaitFuture implements Future {
	int sequence;
	Object result;
	Type returnType;

	int timeout;
	CountDownLatch latch = new CountDownLatch(1);

	public WaitFuture(int sequence, int timeout, Type returnType) {
		this.sequence = sequence;
		this.timeout = timeout;
		this.returnType = returnType;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return latch.getCount() <= 0;
	}

	@Override
	public Object get() throws InterruptedException {
		return get(timeout, TimeUnit.MILLISECONDS);
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException {
		try {
			latch.await(timeout, unit);
		} finally {
			WaitFutureManager.remove(sequence);
		}
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
		latch.countDown();
	}

}
