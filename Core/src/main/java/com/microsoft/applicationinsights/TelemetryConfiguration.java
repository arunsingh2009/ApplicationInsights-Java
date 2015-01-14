package com.microsoft.applicationinsights;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.applicationinsights.channel.TelemetryChannel;

import com.google.common.base.Strings;
import com.microsoft.applicationinsights.extensibility.ContextInitializer;
import com.microsoft.applicationinsights.extensibility.TelemetryInitializer;
import com.microsoft.applicationinsights.internal.config.TelemetryConfigurationFactory;

/**
 * Configuration data and logic for telemetry clients.
 */
public final class TelemetryConfiguration {

    // Synchronization for instance initialization
    private final static Object s_lock = new Object();
    private static volatile boolean initialized = false;
    private static TelemetryConfiguration active;

    private String instrumentationKey;

    private String endpoint;

    private final ArrayList<ContextInitializer> contextInitializers = new ArrayList<ContextInitializer>();
    private final ArrayList<TelemetryInitializer> telemetryInitializers = new ArrayList<TelemetryInitializer>();

    private TelemetryChannel channel;

    private boolean trackingIsDisabled = false;

    private boolean developerMode = false;

    /**
     * We initialize the instance once by using {@link com.microsoft.applicationinsights.internal.config.TelemetryConfigurationFactory}
     * @return The instance
     */
    public static TelemetryConfiguration getActive() {
        if (!initialized) {
            synchronized (s_lock) {
                if (!initialized) {
                    active = new TelemetryConfiguration();
                    TelemetryConfigurationFactory.INSTANCE.initialize(active);
                    initialized = true;
                }
            }
        }

        return active;
    }

    public static TelemetryConfiguration createDefault() {
        TelemetryConfiguration telemetryConfiguration = new TelemetryConfiguration();
        TelemetryConfigurationFactory.INSTANCE.initialize(telemetryConfiguration);
        return telemetryConfiguration;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public TelemetryChannel getChannel() {
        return channel;
    }

    public void setChannel(TelemetryChannel channel) {
        this.channel = channel;
    }

    public boolean isTrackingDisabled() {
        return trackingIsDisabled;
    }

    public void setTrackingIsDisabled(boolean disable) {
        trackingIsDisabled = disable;
    }

    public boolean isDeveloperMode() {
        return developerMode;
    }

    public void setDeveloperMode(boolean developerMode) {
        this.developerMode = developerMode;
    }

    public List<ContextInitializer> getContextInitializers() {
        return contextInitializers;
    }

    public List<TelemetryInitializer> getTelemetryInitializers() {
        return telemetryInitializers;
    }

    public String getInstrumentationKey() {
        return instrumentationKey;
    }

    public void setInstrumentationKey(String key) {
        // A non null, non empty instrumentation key is a must
        if (Strings.isNullOrEmpty(key)) {
            throw new IllegalArgumentException("key");
        }

        instrumentationKey = key;
    }
}
