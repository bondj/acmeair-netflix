package com.acmeair.servo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.monitor.BucketConfig;
import com.netflix.servo.monitor.BucketTimer;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.Timer;

@WebFilter(dispatcherTypes = { DispatcherType.REQUEST, DispatcherType.FORWARD,
		DispatcherType.INCLUDE, DispatcherType.ERROR }, urlPatterns = { "/*" })
public class ServoFilter implements Filter {

	private static final Timer bucketTimer = new BucketTimer(MonitorConfig
			.builder("requestTime").build(), new BucketConfig.Builder()
			.withTimeUnit(TimeUnit.MILLISECONDS)
			.withBuckets(new long[] { 50, 500 }).build());

	static {
		DefaultMonitorRegistry.getInstance().register(bucketTimer);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		com.netflix.servo.monitor.Stopwatch sw = bucketTimer.start();
		try {
			chain.doFilter(request, response);
		} finally {
			sw.stop();			
		}

	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		// TODO Auto-generated method stub

	}

}
