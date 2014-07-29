package com.acmeair.servo;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.servo.monitor.Pollers;

public class Config {
	
	private static final boolean isGraphiteObserverEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty("com.acmeair.servo.isGraphiteObserverEnabled", false).get();
	private static final String graphiteObserverPrefix = DynamicPropertyFactory.getInstance().getStringProperty("com.acmeair.servo.graphiteObserverPrefix", getString("eureka.name", "UnknownEurekaName"+getRandomString())+"."+getString("eureka.hostname", getHostName())).get();
	private static final String graphiteObserverAddress = DynamicPropertyFactory.getInstance().getStringProperty("com.acmeair.servo.graphiteObserverAddress", "localhost:2003").get();
	private static final boolean isJvmPollerEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty("com.acmeair.servo.jvmPollerEnabled", false).get();
	private static final boolean isWlpPollerEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty("com.acmeair.servo.wlpPollerEnabled", false).get();
	
	private static String getRandomString()
	{		
		return Integer.toHexString((int)(Math.random()*Integer.MAX_VALUE));
	}
	
	private static String getHostName()
	{
		try {			
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "UnknownHost"+getRandomString();
		}
	}
	
	private static String getString(String prop,String defaultValue)
	{
		return DynamicPropertyFactory.getInstance().getStringProperty(prop,defaultValue).get();
	}
	
	public static boolean isGraphiteObserverEnabled()
	{
		return isGraphiteObserverEnabled;
	}
	
	public static String getGraphiteObserverPrefix()
	{
		return graphiteObserverPrefix;
	}
	
	public static String getGraphiteObserverAddress()
	{
		return graphiteObserverAddress;
	}
		
    public static boolean isJvmPollerEnabled() {
        return isJvmPollerEnabled;
    }
    
    public static boolean isWlpPollerEnabled() {
        return isWlpPollerEnabled;
    }
    
    //in seconds
    public static long getPollInterval() {
        return Pollers.getPollingIntervals().get(1) / 1000L;
    }

}
