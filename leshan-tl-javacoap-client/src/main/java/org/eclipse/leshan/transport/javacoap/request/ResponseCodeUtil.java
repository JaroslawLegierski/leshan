/*******************************************************************************
 * Copyright (c) 2023 Sierra Wireless and others.
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

package org.eclipse.leshan.transport.javacoap.request;

import org.eclipse.leshan.core.ResponseCode;
import org.eclipse.leshan.core.util.Validate;

import com.mbed.coap.packet.Code;

public class ResponseCodeUtil {



    public static ResponseCode toLwM2mResponseCode(Code coapResponseCode) {
        return ResponseCode.fromCode(coapResponseCode.getHttpCode());
    }


    public static int toCoapCode(int lwm2mCode) {
        int codeClass = lwm2mCode / 100;
        int codeDetail = lwm2mCode % 100;
        if (codeClass > 7 || codeDetail > 31)
            throw new IllegalArgumentException("Could not be translated into a valid COAP code");

        return codeClass << 5 | codeDetail;
    }

    public static Code toCoapResponseCode(ResponseCode Lwm2mResponseCode) {
        Validate.notNull(Lwm2mResponseCode);
        Code result = Code.valueOf(toCoapCode(Lwm2mResponseCode.getCode()));
        if (result == null) {
            throw new IllegalArgumentException("Unknown CoAP code for LWM2M response: " + Lwm2mResponseCode);
        }
        return result;
    }
}
