/*******************************************************************************
 * Copyright (c) 2021 Sierra Wireless and others.
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
package org.eclipse.leshan.demo.cli.converters;

import java.security.cert.X509Certificate;

import org.eclipse.leshan.core.security.util.SecurityUtil;

import picocli.CommandLine.ITypeConverter;

public class X509CertificateChainConverter implements ITypeConverter<X509Certificate[]> {

    @Override
    public X509Certificate[] convert(String value) throws Exception {
        return SecurityUtil.certificateChain.readFromFile(value);
    }
}
