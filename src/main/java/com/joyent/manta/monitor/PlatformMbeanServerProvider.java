package com.joyent.manta.monitor;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

public class PlatformMbeanServerProvider {
    public MBeanServer getPlatformMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }
}