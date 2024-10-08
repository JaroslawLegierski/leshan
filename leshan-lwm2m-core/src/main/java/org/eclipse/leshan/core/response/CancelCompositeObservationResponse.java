/*******************************************************************************
 * Copyright (c) 2021 Orange.
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
 *     Michał Wadowski (Orange) - Add Cancel Composite-Observation feature.
 *******************************************************************************/
package org.eclipse.leshan.core.response;

import java.util.Map;

import org.eclipse.leshan.core.ResponseCode;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.TimestampedLwM2mNodes;
import org.eclipse.leshan.core.observation.CompositeObservation;

public class CancelCompositeObservationResponse extends ObserveCompositeResponse {

    public CancelCompositeObservationResponse(ResponseCode code, Map<LwM2mPath, LwM2mNode> content,
            TimestampedLwM2mNodes timestampedValues, CompositeObservation observation, String errorMessage,
            Object coapResponse) {
        super(code, content, timestampedValues, null, observation, errorMessage, coapResponse);
    }

    @Override
    public String toString() {
        if (errorMessage != null)
            return String.format("CancelCompositeObservationResponse [code=%s, errorMessage=%s]", code, errorMessage);
        else if (timestampedValues != null)
            return String.format(
                    "CancelCompositeObservationResponse [code=%s, content=%s, observation=%s, timestampedValues= %d values]",
                    code, content, observation, timestampedValues.getTimestamps().size());
        else
            return String.format("CancelCompositeObservationResponse [code=%s,  content=%s, observation=%s]", code,
                    observation, content);
    }
}
