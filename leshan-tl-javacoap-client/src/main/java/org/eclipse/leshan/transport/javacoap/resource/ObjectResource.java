package org.eclipse.leshan.transport.javacoap.resource;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.leshan.client.californium.LwM2mClientCoapResource;
import org.eclipse.leshan.client.endpoint.ClientEndpointToolbox;
import org.eclipse.leshan.client.request.DownlinkRequestReceiver;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.resource.listener.ObjectListener;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.client.servers.ServerInfo;
import org.eclipse.leshan.core.ResponseCode;
import org.eclipse.leshan.core.node.InvalidLwM2mPathException;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.request.DownlinkRequest;
import org.eclipse.leshan.core.request.Identity;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.request.exception.InvalidRequestException;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.transport.javacoap.request.ResponseCodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mbed.coap.packet.CoapRequest;
import com.mbed.coap.packet.CoapResponse;
import com.mbed.coap.packet.Code;
import com.mbed.coap.packet.Opaque;

public class ObjectResource extends LwM2mCoapResource {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectResource.class);
    private static final ServerIdentity identity = null;
    private static String URI;
    private static String serverurl;
    protected DownlinkRequestReceiver requestReceiver;
    protected ClientEndpointToolbox toolbox;
    private  int shortserverid;


    public ObjectResource(DownlinkRequestReceiver requestReceiver,  String uri,ClientEndpointToolbox toolbox,String serverurl, int  shortserverid ) {
        super(uri);
        this.requestReceiver = requestReceiver;
        this.toolbox = toolbox;
        this.URI=uri;
        this.serverurl=serverurl;
        this.shortserverid= shortserverid;
    }

    public CompletableFuture<CoapResponse> handleGET(CoapRequest coapRequest) {
        LOG.trace("GET received : {}", coapRequest);

        // The LWM2M spec (section 8.2) mandates the usage of confirmable messages
        // TODO how to check is request is confirmable message ?
        //        if (!CON.equals(coapRequest.getType())) {
        //            handleInvalidRequest(coapRequest, "CON CoAP type expected");
        //            return;
        //        }

        CompletableFuture<CoapResponse> coapResponseCompletableFuture = null;

        // handle content format for Read and Observe Request
        ContentFormat requestedContentFormat = ContentFormat.fromCode( coapRequest.options().getAccept());;

        // TODO find  function similar to isValid !!!
        if ( coapRequest!=null) {
            // If an request ask for a specific content format, use it (if we support it)

            if (!toolbox.getEncoder().isSupported(requestedContentFormat)) {

                 return handleInvalidRequest(coapRequest, Code.C406_NOT_ACCEPTABLE.codeToString());
            }
        }
        // Manage Read Request

        ReadRequest readRequest = new ReadRequest(requestedContentFormat, coapRequest.options().getUriPath(), coapRequest);



        Identity identity = getForeignPeerIdentity(coapRequest);

        ; //todo - find better solution for Server Identity!!!!
        ServerIdentity serveridentity = new ServerIdentity(identity, (long) shortserverid, ServerIdentity.Role.LWM2M_SERVER, java.net.URI.create(serverurl));


        ReadResponse response = requestReceiver.requestReceived(serveridentity, readRequest).getResponse();
        if (response.getCode() == org.eclipse.leshan.core.ResponseCode.CONTENT) {
            LwM2mPath path = getPath(URI);
            LwM2mNode content = response.getContent();
            ContentFormat format = getContentFormat(readRequest, requestedContentFormat);


            toolbox.getEncoder().encode(content, format, path,toolbox.getModel());
            Short contentformat= (short) format.getCode();

            ResponseCode lwm2mresponsecode=response.getCode();

            CoapResponse coapResponse = CoapResponse.of(ResponseCodeUtil.toCoapResponseCode(lwm2mresponsecode));
            coapResponse.options().setContentFormat(contentformat);

            coapResponse = coapResponse.payload( Opaque.of(toolbox.getEncoder().encode(content, format, path, toolbox.getModel())));
            CompletableFuture coapResponsecompletablefuture = new CompletableFuture();
            coapResponsecompletablefuture.complete(coapResponse);

            return coapResponsecompletablefuture;
        } else {

            return null;
        }


    }
    protected LwM2mPath getPath(String URI) throws InvalidRequestException {
        try {
            return new LwM2mPath(URI);
        } catch (InvalidLwM2mPathException e) {
            throw new InvalidRequestException(e, "Invalid path : %s", e.getMessage());
        }
    }

    protected ContentFormat getContentFormat(DownlinkRequest<?> request, ContentFormat requestedContentFormat) {
        if (requestedContentFormat != null) {
            // we already check before this content format is supported.
            return requestedContentFormat;
        }

        // TODO TL : should we keep this feature ?
        // ContentFormat format = nodeEnabler.getDefaultEncodingFormat(request);
        // return format == null ? ContentFormat.DEFAULT : format;

        return ContentFormat.DEFAULT;
    }

}
