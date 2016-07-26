package com.sensorberg.sdk.resolver;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.RunLoop;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;

import android.os.Message;

import java.util.ArrayList;
import java.util.List;

public final class Resolver implements RunLoop.MessageHandlerCallback {

    private final Object listenersMonitor = new Object();

    private final Object resolutionsMonitor = new Object();

    public final ResolverConfiguration configuration;

    private final List<ResolverListener> listeners = new ArrayList<>();

    private final CurrentResolutions currentResolutions = new CurrentResolutions();

    private final Transport transport;

    private final RunLoop runLoop;

    public Resolver(ResolverConfiguration configuration, HandlerManager handlerManager, Transport transport) {
        this.configuration = configuration;
        runLoop = handlerManager.getResolverRunLoop(this);
        this.transport = transport;
    }

    /**
     * Adds a {@link ResolverListener} to the {@link List} of {@link ResolverListener}s.
     *
     * @param listener the {@link ResolverListener} to be added
     */
    public void addResolverListener(ResolverListener listener) {
        synchronized (listenersMonitor) {
            listeners.add(listener);
        }
    }

    /**
     * Creates a new {@link Resolution}; the {@link ResolutionConfiguration} is copied and therefore cannot be changed after creation of the {@link
     * Resolution}.
     *
     * @param resolutionConfiguration the {@link ResolutionConfiguration} to configure the {@link Resolution} with
     * @return the {@link Resolution} created
     */
    public Resolution createResolution(ResolutionConfiguration resolutionConfiguration) {
        return (new Resolution(this, resolutionConfiguration, transport));
    }

    @Override
    public void handleMessage(Message queueEvent) {
        synchronized (resolutionsMonitor) {
            switch (queueEvent.arg1) {
                case ResolverEvent.RESOLUTION_START_REQUESTED: {
                    Resolution resolution = (Resolution) queueEvent.obj;

                    if (currentResolutions.contains(resolution)) {
                        Logger.log.beaconResolveState(resolution.configuration.getScanEvent(), "request already running, not stating a new one");
                        return;
                    }

                    currentResolutions.add(resolution);
                    resolution.queryServer();

                    break;
                }
                default: {
                    throw new IllegalArgumentException("unhandled default case");
                }
            }
        }
    }

    void onResolutionFailed(Resolution resolution, Throwable cause) {
        synchronized (listenersMonitor) {
            for (ResolverListener listener : listeners) {
                listener.onResolutionFailed(resolution, cause);
            }
        }
        currentResolutions.remove(resolution);
    }

    void onResolutionFinished(Resolution resolution, List<BeaconEvent> beaconEvents) {
        synchronized (listenersMonitor) {
            for (ResolverListener listener : listeners) {
                listener.onResolutionsFinished(beaconEvents);
            }
        }
        currentResolutions.remove(resolution);
    }

    /**
     * Removes a {@link ResolverListener} from the {@link List} of {@link ResolverListener}s.
     *
     * @param listener the {@link ResolverListener} to be removed
     */
    public void removeResolverListener(ResolverListener listener) {
        synchronized (listenersMonitor) {
            listeners.remove(listener);
        }
    }

    /**
     * Starts a {@link Resolution}.
     *
     * @param resolution the {@link Resolution} to be started
     */
    public void startResolution(Resolution resolution) {
        runLoop.add(ResolverEvent.asMessage(ResolverEvent.RESOLUTION_START_REQUESTED, resolution));
    }

    public void retry(ResolutionConfiguration configuration) {
        Resolution resolution = currentResolutions.get(configuration.getScanEvent());
        if (resolution == null) {
            resolution = createResolution(configuration);
            Logger.log.beaconResolveState(configuration.getScanEvent(), "creating a new resolution, we have been in the background for too long");
        }

        resolution.configuration.retry++;
        Logger.log.beaconResolveState(resolution.configuration.getScanEvent(), "performing the retry No." + resolution.configuration.retry);
        resolution.queryServer();
    }
}
