package com.cloud.common.rpc;

import java.util.concurrent.ConcurrentHashMap;

public class WaitFutureManager {

	private static ConcurrentHashMap<Integer, WaitFuture> futureMap = new ConcurrentHashMap<Integer, WaitFuture>(5000);

	public static void add(WaitFuture future) {
		futureMap.put(future.sequence, future);
	}

	public static WaitFuture remove(int sequence) {
		return futureMap.remove(sequence);
	}

	public static WaitFuture get(int sequence) {
		return futureMap.get(sequence);
	}

}
