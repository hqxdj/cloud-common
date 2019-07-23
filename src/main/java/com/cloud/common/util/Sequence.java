package com.cloud.common.util;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Enumeration;

@Component
public class Sequence {

// ==============================Fields===========================================
	/** 开始时间截 (2018-03-13) */
	private final long timestart = 1520949192988L;

	/** 机器id所占的位数 */
	private final long workerIdBits = 6L;

	/** 数据中心id所占的位数 */
	private final long datacenterIdBits = 3L;

	/** 支持的最大机器id (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */
	private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

	/** 支持的最大数据标识id */
	private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

	/** 序列在id中占的位数 */
	private final long sequenceBits = 12L;

	/** 机器ID向左移 */
	private final long workerIdShift = sequenceBits;

	/** 数据标识id向左移 */
	private final long datacenterIdShift = sequenceBits + workerIdBits;

	/** 时间截向左移 */
	private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

	/** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
	private final long sequenceMask = -1L ^ (-1L << sequenceBits);

	/** 工作机器ID */
	private long workerId;

	/** 数据中心ID */
	private long datacenterId;

	/** 毫秒内序列(0~4095) */
	private long sequence = 0L;

	/** 上次生成ID的时间截 */
	private long lastTimestamp = -1L;

	//==============================Constructors=====================================
	/**
	 * 构造函数
	 */
	public Sequence() {
		String ip = innerIp();
		String[] arr = ip.split("\\.");
		int v1 = Integer.parseInt(arr[3]);
		int v2 = Integer.parseInt(arr[2]);
		this.workerId = v1 % 64;
		this.datacenterId = (v2 + (v1/64)) % 8;
	}

	/**
	 * 构造函数
	 * @param workerId 工作ID (0~64)
	 * @param datacenterId 数据中心ID (0~7)
	 */
	public Sequence(long workerId, long datacenterId) {
		if (workerId > maxWorkerId || workerId < 0) {
			throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
		}
		if (datacenterId > maxDatacenterId || datacenterId < 0) {
			throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
		}
		this.workerId = workerId;
		this.datacenterId = datacenterId;
	}

	// ==============================Methods==========================================
	/**
	 * 获得下一个ID (该方法是线程安全的)
	 * @return
	 */
	public synchronized long nextId() {
		long timestamp = timeGen();

		//如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
		if (timestamp < lastTimestamp) {
			throw new RuntimeException(
					String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
		}

		//如果是同一时间生成的，则进行毫秒内序列
		if (lastTimestamp == timestamp) {
			sequence = (sequence + 1) & sequenceMask;
			//毫秒内序列溢出
			if (sequence == 0) {
				//阻塞到下一个毫秒,获得新的时间戳
				timestamp = tilNextMillis(lastTimestamp);
			}
		} else {
			//时间戳改变，毫秒内序列重置
			sequence = 0L;
		}

		//上次生成ID的时间截
		lastTimestamp = timestamp;

		//移位并通过或运算拼到一起组成64位的ID
		return ((timestamp - timestart) << timestampLeftShift) //
				| (datacenterId << datacenterIdShift) //
				| (workerId << workerIdShift) //
				| sequence;
	}

	/**
	 * 获取日期时间
	 * @param value
	 * @return LocalDateTime
	 */
	public LocalDateTime getLocalDateTime(long value) {
		long time = (value >> timestampLeftShift) + timestart;
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
	}

	/**
	 * 阻塞到下一个毫秒，直到获得新的时间戳
	 * @param lastTimestamp 上次生成ID的时间截
	 * @return 当前时间戳
	 */
	private long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}

	/**
	 * 返回以毫秒为单位的当前时间
	 * @return 当前时间(毫秒)
	 */
	private long timeGen() {
		return System.currentTimeMillis();
	}

	/**
	 * 获取内网IP
	 * @return
	 */
	private String innerIp() {
		try {
			Enumeration<NetworkInterface> enu = NetworkInterface.getNetworkInterfaces();
			while (enu.hasMoreElements()) {
				Enumeration<InetAddress> en = enu.nextElement().getInetAddresses();
				while (en.hasMoreElements()) {
					String hostAddress = en.nextElement().getHostAddress();
					if (hostAddress.split("\\.").length == 4) {
						if (hostAddress.startsWith("10.") || hostAddress.startsWith("172.") || hostAddress.startsWith("192.")) {
							return hostAddress;
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

}