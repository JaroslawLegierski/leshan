package org.eclipse.leshan.server.demo.servlet.statistics;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Message;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationService;
import org.eclipse.leshan.server.registration.RegistrationServiceImpl;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ConnectionStatistics {
    private enum MessageType {SEND_REQUEST, SEND_RESPONSE, RECEIVE_REQUEST, RECEIVE_RESPONSE}

    private final Map<String, EndpointData> tokenEndpointMap = new ConcurrentHashMap<>();
    private final List<Map<String, String>> cachedStatistics = new ArrayList<>();
    private final RegistrationServiceImpl registrationService;
    private final StatisticsDataProxy statisticsDataProxy;
    private final Timer periodicFlushTimer;

    public ConnectionStatistics(StatisticsDataProxy statisticsDataProxy, long periodBetweenFlushMs,
            RegistrationService registry) {
        this.statisticsDataProxy = statisticsDataProxy;

        periodicFlushTimer = new Timer();
        periodicFlushTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                flushData();
                clearOldTokenEndpointMapRecords();
            }
        }, periodBetweenFlushMs, periodBetweenFlushMs);

        if (registry instanceof RegistrationServiceImpl) {
            this.registrationService = (RegistrationServiceImpl) registry;
        } else {
            this.registrationService = null;
        }
    }

    private synchronized void putDataInCache(Map<String, String> messageData) {
        cachedStatistics.add(messageData);
    }

    private synchronized void flushData() {
        if (cachedStatistics.isEmpty()) {
            return;
        }
        statisticsDataProxy.collectDataFromCache(cachedStatistics);
        cachedStatistics.clear();
    }

    private void clearOldTokenEndpointMapRecords() {
        //If records sit in cache for more than an hour (adjustable), it's most likely a result
        //of registration error, so they have to be deleted
        long timeThreshold = 3_600_000;
        Set<String> tokens = tokenEndpointMap.entrySet().stream()
                .filter(entry -> System.currentTimeMillis() - entry.getValue().createdAt > timeThreshold)
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        tokens.forEach(tokenEndpointMap::remove);
    }

    private void reportMessage(MessageType type, Message message, InetSocketAddress peerAddress) {
        Map<String, String> messageData = new LinkedHashMap<>();
        messageData.put("messageTime", LocalDateTime.now(ZoneOffset.UTC).toString());
        messageData.put("type", type.toString());
        messageData.put("mid", String.valueOf(message.getMID()));
        messageData.put("msg", message.getType().toString());
        messageData.put("rejected", String.valueOf(message.isRejected()));
        messageData.put("acknowledged", String.valueOf(message.acknowledge()));
        messageData.put("timeout", String.valueOf(message.isTimedOut()));
        messageData.put("duplicated", String.valueOf(message.isDuplicate()));
        messageData.put("token", message.getTokenString());
        messageData.put("timestamp", String.valueOf(message.getNanoTimestamp()));

        Registration registration = null;
        if(registrationService != null) {
            registration = registrationService.getStore().getRegistrationByAdress(peerAddress);
            if(registration == null && message.getOptions().getUriPath().size() >= 2) {
                //In case registration update request comes from a different address than it's known in store
                registration = registrationService.getStore().getRegistration(message.getOptions().getUriPath().get(1));
            }
        }
        if (registration != null) {
            //This is the case for most messages
            messageData.put("endpoint", registration.getEndpoint());
            if (message instanceof Request && ((Request) message).getCode().equals(CoAP.Code.DELETE)) {
                //In this case we know it's de-registration request
                tokenEndpointMap.put(message.getTokenString(),
                        new EndpointData(registration.getEndpoint(), System.currentTimeMillis()));
            }
        } else {
            //In this case we know it's registration request or de-registration response.
            Optional<String> endpointOptional = message.getOptions().getUriQuery().stream()
                    .filter(item -> item.startsWith("ep=")).findFirst();
            if (endpointOptional.isPresent()) {
                //This is registration request, it carries endpoint name inside its URI query
                String endpoint = endpointOptional.get().substring(3);
                messageData.put("endpoint", endpoint);
            } else {
                //This is de-registration response, it doesn't contain any information about the endpoint,
                //so we have to retrieve it from cache using its token
                EndpointData endpointData = tokenEndpointMap.get(message.getTokenString());
                if (endpointData != null) {
                    messageData.put("endpoint", endpointData.endpoint);
                    tokenEndpointMap.remove(message.getTokenString());
                }
            }
        }

        putDataInCache(messageData);
    }

    public void reportSendRequest(Request request, InetSocketAddress peerAddress) {
        reportMessage(MessageType.SEND_REQUEST, request, peerAddress);
    }

    public void reportSendResponse(Response response, InetSocketAddress peerAddress) {
        reportMessage(MessageType.SEND_RESPONSE, response, peerAddress);
    }

    public void reportReceiveRequest(Request request, InetSocketAddress peerAddress) {
        reportMessage(MessageType.RECEIVE_REQUEST, request, peerAddress);
    }

    public void reportReceiveResponse(Response response, InetSocketAddress peerAddress) {
        reportMessage(MessageType.RECEIVE_RESPONSE, response, peerAddress);
    }

    private static class EndpointData {
        private final String endpoint;
        private final long createdAt;

        public EndpointData(String endpoint, long createdAt) {
            this.endpoint = endpoint;
            this.createdAt = createdAt;
        }
    }
}
