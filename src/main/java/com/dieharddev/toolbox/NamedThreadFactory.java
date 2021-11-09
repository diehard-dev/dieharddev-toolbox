package com.dieharddev.toolbox;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
	final ThreadGroup group;
	final AtomicInteger threadNumber = new AtomicInteger(1);
	final String namePrefix;
	private final static Map<String, NamedThreadFactory> namePrefixToNamedThreadFactoryMap = new HashMap<String, NamedThreadFactory>();

	public static NamedThreadFactory getNamedThreadFactory(String namePrefix) {
		if (namePrefixToNamedThreadFactoryMap.containsKey(namePrefix) == false) {
			namePrefixToNamedThreadFactoryMap.put(namePrefix, new NamedThreadFactory(namePrefix));
		}
		NamedThreadFactory result = namePrefixToNamedThreadFactoryMap.put(namePrefix,
				new NamedThreadFactory(namePrefix));
		return ValueChecks.get().checkNotNullParameter(result,
				"Return value for getNamedThreadFactory(" + namePrefix + ")");
	}

	private NamedThreadFactory(String namePrefix) {
		SecurityManager s = System.getSecurityManager();
		if (s != null) {
			group = s.getThreadGroup();
		} else {
			group = Thread.currentThread().getThreadGroup();
		}
		this.namePrefix = namePrefix + "-thread-";
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
		if (thread.isDaemon()) {
			thread.setDaemon(false);
		}
		if (thread.getPriority() != Thread.NORM_PRIORITY) {
			thread.setPriority(Thread.NORM_PRIORITY);
		}
		return thread;
	}
}
