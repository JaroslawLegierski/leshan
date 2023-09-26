/*******************************************************************************
 * Copyright (c) 2022 Sierra Wireless and others.
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
 *     Jaroslaw Legierski Orange Polska S.A.  - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.core.californium.oscore.cf;

public class OscoreFallback {
    boolean fallbackdetected;

    public boolean isFallbackdetected() {
        return fallbackdetected;
    }

    public void setFallbackdetected(boolean fallbackdetected) {
        this.fallbackdetected = fallbackdetected;
    }

    public OscoreFallback(boolean fallbackdetected) {
        this.fallbackdetected = fallbackdetected;
    }
}
