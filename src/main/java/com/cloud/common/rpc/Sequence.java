package com.cloud.common.rpc;

import java.util.concurrent.atomic.AtomicInteger;

public class Sequence {

	private static AtomicInteger value = new AtomicInteger(1);

	public static int next() {
		int seq = value.getAndIncrement();
		if (seq < 1) {
			seq = 1;
			value.set(seq);
		}
		return seq;
	}

}