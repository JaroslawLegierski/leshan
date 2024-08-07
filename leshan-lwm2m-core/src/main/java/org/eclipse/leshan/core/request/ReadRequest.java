/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.core.request;

import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.request.exception.InvalidRequestException;
import org.eclipse.leshan.core.response.ReadResponse;

/**
 * A Lightweight M2M request for retrieving the values of resources from a LWM2M Client.
 *
 * The request can be used to retrieve the value(s) of one or all attributes of one particular or all instances of a
 * particular object type.
 */
public class ReadRequest extends AbstractSimpleDownlinkRequest<ReadResponse>
        implements DownlinkDeviceManagementRequest<ReadResponse> {

    private final ContentFormat format;

    /**
     * Creates a request for reading all instances of a particular object from a client.
     *
     * @param objectId the object ID of the resource
     */
    public ReadRequest(int objectId) {
        this(null, newPath(objectId), null);
    }

    /**
     * Creates a request for reading all instances of a particular object from a client.
     *
     * @param format the desired format for the response (TLV or JSON)
     * @param objectId the object ID of the resource
     */
    public ReadRequest(ContentFormat format, int objectId) {
        this(format, newPath(objectId), null);
    }

    /**
     * Creates a request for reading a particular object instance from a client.
     *
     * @param objectId the object ID of the resource
     * @param objectInstanceId the object instance ID
     */
    public ReadRequest(int objectId, int objectInstanceId) {
        this(null, newPath(objectId, objectInstanceId), null);
    }

    /**
     * Creates a request for reading a particular object instance from a client.
     *
     * @param format the desired format for the response (TLV or JSON)
     * @param objectId the object ID of the resource
     * @param objectInstanceId the object instance ID
     */
    public ReadRequest(ContentFormat format, int objectId, int objectInstanceId) {
        this(format, newPath(objectId, objectInstanceId), null);
    }

    /**
     * Creates a request for reading a specific resource from a client.
     *
     * @param objectId the object ID of the resource
     * @param objectInstanceId the object instance ID
     * @param resourceId the (individual) resource's ID
     */
    public ReadRequest(int objectId, int objectInstanceId, int resourceId) {
        this(null, newPath(objectId, objectInstanceId, resourceId), null);
    }

    /**
     * Creates a request for reading a specific resource from a client.
     *
     * @param objectId the object ID of the resource
     * @param objectInstanceId the object instance ID
     * @param resourceId the (individual) resource's ID
     * @param resourceInstanceId the resource instance's ID
     */
    public ReadRequest(int objectId, int objectInstanceId, int resourceId, int resourceInstanceId) {
        this(null, newPath(objectId, objectInstanceId, resourceId, resourceInstanceId), null);
    }

    /**
     * Creates a request for reading a specific resource from a client.
     *
     * @param format the desired format for the response (TLV, JSON, TEXT or OPAQUE)
     * @param objectId the object ID of the resource
     * @param objectInstanceId the object instance ID
     * @param resourceId the (individual) resource's ID
     */
    public ReadRequest(ContentFormat format, int objectId, int objectInstanceId, int resourceId) {
        this(format, newPath(objectId, objectInstanceId, resourceId), null);
    }

    /**
     * Creates a request for reading a specific resource from a client.
     *
     * @param format the desired format for the response (TLV, JSON, TEXT or OPAQUE)
     * @param objectId the object ID of the resource
     * @param objectInstanceId the object instance ID
     * @param resourceId the (individual) resource's ID
     * @param resourceInstanceId the resource instance's ID
     */
    public ReadRequest(ContentFormat format, int objectId, int objectInstanceId, int resourceId,
            int resourceInstanceId) {
        this(format, newPath(objectId, objectInstanceId, resourceId, resourceInstanceId), null);
    }

    /**
     * Create a request for reading an object/instance/resource targeted by a specific path.
     *
     * @param path the path to the LWM2M node to read
     * @throws IllegalArgumentException if the target path is not valid
     */
    public ReadRequest(String path) {
        this(null, newPath(path), null);
    }

    /**
     * Create a request for reading an object/instance/resource targeted by a specific path.
     *
     * @param format the desired format for the response
     * @param path the path to the LWM2M node to read
     * @throws IllegalArgumentException if the target path is not valid
     */
    public ReadRequest(ContentFormat format, String path) {
        this(format, newPath(path), null);
    }

    /**
     * Create a request for reading an object/instance/resource targeted by a specific path.
     *
     * @param format the desired format for the response
     * @param path the path to the LWM2M node to read
     * @param coapRequest the underlying request
     *
     * @throws IllegalArgumentException if the target path is not valid
     */
    public ReadRequest(ContentFormat format, String path, Object coapRequest) {
        this(format, newPath(path), coapRequest);
    }

    /**
     * Create a request for reading an object/instance/resource targeted by a specific path.
     * <p>
     * This constructor is mainly for internal purpore.
     *
     * @param format the desired format for the response
     * @param target the path to the LWM2M node to read
     * @param coapRequest the underlying request
     *
     * @throws IllegalArgumentException if the target path is not valid
     */
    public ReadRequest(ContentFormat format, LwM2mPath target, Object coapRequest) {
        super(target, coapRequest);
        if (target.isRoot())
            throw new InvalidRequestException("Read request cannot target root path");
        this.format = format;
    }

    /**
     * @return the desired format of the resource to read
     */
    public ContentFormat getContentFormat() {
        return format;
    }

    @Override
    public final String toString() {
        return String.format("ReadRequest [path=%s format=%s]", getPath(), format);
    }

    @Override
    public void accept(DownlinkRequestVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(DownlinkDeviceManagementRequestVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((format == null) ? 0 : format.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReadRequest other = (ReadRequest) obj;
        if (format != other.format)
            return false;
        return true;
    }
}
