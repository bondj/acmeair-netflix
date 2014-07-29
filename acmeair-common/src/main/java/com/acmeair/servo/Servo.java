package com.acmeair.servo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.netflix.servo.publish.AsyncMetricObserver;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.CounterToRateMetricTransform;
import com.netflix.servo.publish.JvmMetricPoller;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MetricPoller;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;
import com.netflix.servo.publish.graphite.GraphiteMetricObserver;

public class Servo {

	private static final AtomicBoolean started = new AtomicBoolean(false);

	public static final void initMetricsPublishing() {

		if (!started.getAndSet(true)) {
			final List<MetricObserver> observers = new ArrayList<MetricObserver>();			

			if (Config.isGraphiteObserverEnabled()) {
				observers.add(createGraphiteObserver());
			}

			PollScheduler.getInstance().start();
			schedule(new MonitorRegistryMetricPoller(), observers);

			if (Config.isJvmPollerEnabled()) {
				schedule(new JvmMetricPoller(), observers);
			}
		}
	}

	private static MetricObserver createGraphiteObserver() {
		final String prefix = Config.getGraphiteObserverPrefix();
		final String addr = Config.getGraphiteObserverAddress();
		return rateTransform(async("graphite", new GraphiteMetricObserver(
				prefix, addr,new GraphiteNaming())));
	}

	private static MetricObserver rateTransform(MetricObserver observer) {
		final long heartbeat = 2 * Config.getPollInterval();
		return new CounterToRateMetricTransform(observer, heartbeat,
				TimeUnit.SECONDS);
	}

	private static MetricObserver async(String name, MetricObserver observer) {
		final long expireTime = 2000 * Config.getPollInterval();
		final int queueSize = 10;
		return new AsyncMetricObserver(name, observer, queueSize, expireTime);
	}

	private static void schedule(MetricPoller poller,
			List<MetricObserver> observers) {
		final PollRunnable task = new PollRunnable(poller,
				BasicMetricFilter.MATCH_ALL, true, observers);
		PollScheduler.getInstance().addPoller(task, Config.getPollInterval(),
				TimeUnit.SECONDS);
	}

}
