package org.eclipse.leshan.server.demo.servlet.statistics;

import static org.eclipse.leshan.server.demo.servlet.statistics.ConnectionStatistics.MessageType;

import java.net.InetSocketAddress;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.californium.core.coap.Message;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.elements.EndpointContext;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationService;
import org.eclipse.leshan.server.registration.RegistrationServiceImpl;

public class StatisticsDataExtractorImpl implements StatisticsDataExtractor {

    private final RegistrationServiceImpl registrationService;

    public StatisticsDataExtractorImpl(RegistrationService registry) {
        if (registry instanceof RegistrationServiceImpl) {
            this.registrationService = (RegistrationServiceImpl) registry;
        } else {
            this.registrationService = null;
        }
    }

    @Override public Map<String, ?> extractData(Message message, MessageType type, EndpointContext endpointContext) {
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

        String endpointName = getEndpointName(message, endpointContext);
        if (endpointName != null) {
            messageData.put("endpoint", endpointName);
        }

        endpointContext.entries().entrySet().stream()
                .filter(entry -> entry.getKey().toString().equals("*DTLS_READ_SEQUENCE_NUMBER")).findFirst()
                .ifPresent(entry -> messageData.put("dtlsSequenceNumber", entry.getValue().toString()));

        return messageData;
    }

    private String getEndpointName(Message message, EndpointContext endpointContext) {
        Principal peerIdentity = endpointContext.getPeerIdentity();
        if (peerIdentity != null && peerIdentity.getName() != null) {
            return peerIdentity.getName();
        } else if (registrationService != null) {
            InetSocketAddress peerAddress = endpointContext.getPeerAddress();
            Registration registration = registrationService.getStore().getRegistrationByAdress(peerAddress);
            if (registration == null && message.getOptions().getUriPath().size() >= 2) {
                registration = registrationService.getStore().getRegistration(message.getOptions().getUriPath().get(1));
            }
            if (registration != null) {
                return registration.getEndpoint();
            }
        }
        return null;
    }
}
