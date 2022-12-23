package org.eclipse.leshan.client.javacoap.endpoint;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.observe.ObserveHealth;
import org.eclipse.californium.core.server.MessageDeliverer;

import com.mbed.coap.server.CoapServer;
import com.mbed.coap.server.CoapServerBuilder;
import com.mbed.coap.server.CoapServerBuilder.CoapServerBuilderForUdp;

public class LeshanCoapServer {

    CoapServer coapServer;

    private static final String MARK = "CoAP";

//    private final Configuration config;
    private MessageDeliverer deliverer;
    private ObserveHealth observeHealth;
    private final List<Endpoint> endpoints;
//    private final List<EndpointObserver> defaultObservers;
//    private final List<CounterStatisticManager> statistics;
    private ScheduledExecutorService executor;
    private ScheduledExecutorService secondaryExecutor;
    private boolean detachExecutor;
    private volatile boolean running;
    private volatile String tag;

    public LeshanCoapServer(CoapServer coapServer, List<Endpoint> endpoints) {
        // super(transport, dispatcher, inboundService, stopAll);
        this.endpoints = new CopyOnWriteArrayList();
        this.coapServer = coapServer;

    }

    public static CoapServerBuilderForUdp builder() {
        return CoapServerBuilder.newBuilder();
    }

    public List<Endpoint> getEndpoints() {
        return this.endpoints;
    }

    public Endpoint getEndpoint(int port) {
        Endpoint endpoint = null;
        Iterator i$ = this.endpoints.iterator();

        while (i$.hasNext()) {
            Endpoint ep = (Endpoint) i$.next();
            if (ep.getAddress().getPort() == port) {
                endpoint = ep;
            }
        }

        return endpoint;
    }

    public void start() {
        try {
            coapServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        coapServer.stop();
    }
}
