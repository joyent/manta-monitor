/*
 * Copyright (c) 2018, Joyent, Inc. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.joyent.manta.monitor;

import com.google.inject.*;
import com.google.inject.name.Names;
//import com.google.inject.throwingproviders.ThrowingProviderBinder;
import com.joyent.manta.client.MantaClient;
import com.joyent.manta.monitor.config.Configuration;
import com.joyent.manta.monitor.config.ConfigurationProvider;
import io.honeybadger.reporter.NoticeReporter;
import io.logz.guice.jersey.JerseyModule;
import io.logz.guice.jersey.configuration.JerseyConfiguration;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MantaMonitorModule implements Module {
    private final Thread.UncaughtExceptionHandler honeyBadgerHandler;
    private final NoticeReporter noticeReporter;
    private final URI configURI;

    public MantaMonitorModule(final Thread.UncaughtExceptionHandler uncaughtExceptionHandler,
                              final NoticeReporter noticeReporter,
                              final URI configUri) {
        this.honeyBadgerHandler = uncaughtExceptionHandler;
        this.noticeReporter = noticeReporter;
        this.configURI = configUri;
    }

    public void configure(final Binder binder) {

        binder.bind(Long.class).annotatedWith(Names.named("retryCount")).toInstance(0L);

        binder.bind(new TypeLiteral<Map<String, AtomicLong>>() {})
                .annotatedWith(Names.named("SharedStats"))
                .toProvider(ConcurrentHashMap::new)
                .asEagerSingleton();

        binder.bind(PlatformMbeanServerProvider.class).asEagerSingleton();
        final JerseyConfiguration jerseyConfig = JerseyConfiguration.builder()
                .addPort(8090)
                .build();
        binder.bind(JMXMetricsProvider.class).annotatedWith(Names.named("JMXMetricsProvider")).to(JMXMetricsProvider.class).asEagerSingleton();
        binder.bind(CustomPrometheusCollector.class).asEagerSingleton();
        binder.install(new JerseyModule(jerseyConfig));
        binder.bind(InstanceMetadata.class).asEagerSingleton();
        binder.bind(io.honeybadger.reporter.config.ConfigContext.class).toInstance(noticeReporter.getConfig());
        binder.bind(NoticeReporter.class).toInstance(noticeReporter);
        binder.bind(Thread.UncaughtExceptionHandler.class).toInstance(honeyBadgerHandler);
        binder.bind(com.joyent.manta.config.ConfigContext.class).toProvider(MantaConfigContextProvider.class);
        binder.bind(MantaClient.class).toProvider(MantaClientProvider.class).asEagerSingleton();
        binder.bind(URI.class).annotatedWith(Names.named("configUri")).toInstance(configURI);
        binder.bind(Configuration.class).toProvider(ConfigurationProvider.class);
    }
}
