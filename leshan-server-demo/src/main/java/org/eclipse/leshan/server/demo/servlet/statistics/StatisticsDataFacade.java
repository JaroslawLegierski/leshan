package org.eclipse.leshan.server.demo.servlet.statistics;

import static org.eclipse.leshan.server.demo.servlet.statistics.ConnectionStatistics.EndpointData;
import static org.eclipse.leshan.server.demo.servlet.statistics.ConnectionStatistics.MessageType;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Message;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.elements.EndpointContext;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationService;
import org.eclipse.leshan.server.registration.RegistrationServiceImpl;

public class StatisticsDataFacade {
    private final RegistrationServiceImpl registrationService;

    public StatisticsDataFacade(RegistrationService registry) {
        if (registry instanceof RegistrationServiceImpl) {
            this.registrationService = (RegistrationServiceImpl) registry;
        } else {
            this.registrationService = null;
        }
    }

    Map<String, String> reportMessage(MessageType type, Message message, EndpointContext endpointContext,
            Map<String, EndpointData> tokenEndpointMap) {
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
        messageData.put("uri", message.getOptions().getUriString());

        if (message instanceof Request) {
            messageData.put("coapCode", ((Request) message).getCode().toString());
        } else if (message instanceof Response) {
            String coapCode =
                    ((Response) message).getCode().toString() + " (" + ((Response) message).getCode().name() + ")";
            messageData.put("coapCode", (coapCode));
        }

        String endpoint = endpointContext.getPeerIdentity().getName();
        if (endpoint != null) {
            messageData.put("endpoint", endpoint);
        } else {
            retrieveEndpointName(message, endpointContext, tokenEndpointMap, messageData);
        }

        endpointContext.entries().entrySet().stream()
                .filter(entry -> entry.getKey().toString().equals("DTLS_SEQUENCE_NUMBER")).findFirst()
                .ifPresent(entry -> messageData.put("dtlsSequenceNumber", entry.getValue().toString()));

        return messageData;
    }

    private void retrieveEndpointName(Message message, EndpointContext endpointContext,
            Map<String, EndpointData> tokenEndpointMap, Map<String, String> messageData) {
        Registration registration = null;
        if (registrationService != null) {
            InetSocketAddress peerAddress = endpointContext.getPeerAddress();
            registration = registrationService.getStore().getRegistrationByAdress(peerAddress);
            if (registration == null && message.getOptions().getUriPath().size() >= 2) {
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
            //In this case we know it's registration request or de-registration response or empty message.
            Optional<String> endpointOptional = message.getOptions().getUriQuery().stream()
                    .filter(item -> item.startsWith("ep=")).findFirst();
            if (endpointOptional.isPresent()) {
                //This is registration request, it carries endpoint name inside its URI query
                String endpoint = endpointOptional.get().substring(3);
                messageData.put("endpoint", endpoint);
            } else {
                EndpointData endpointData = tokenEndpointMap.get(message.getTokenString());
                if (endpointData != null) {
                    //This a de-registration response - it doesn't contain any information about the endpoint,
                    //so we have to retrieve it from cache using its token
                    messageData.put("endpoint", endpointData.getEndpoint());
                    tokenEndpointMap.remove(message.getTokenString());
                }
            }
        }
    }
}
