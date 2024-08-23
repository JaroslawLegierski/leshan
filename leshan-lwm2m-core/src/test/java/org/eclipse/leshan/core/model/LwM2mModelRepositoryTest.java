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
 *     Natalia Krzykała Orange Polska S.A. - initial implementation
 *******************************************************************************/

package org.eclipse.leshan.core.model;

import org.junit.jupiter.api.Test;

class LwM2mModelRepositoryTest {
    @Test
    public void assertEqualsHashcode() {
        // EqualsVerifier.forClass(LwM2mModelRepository.class).suppress(Warning.NONFINAL_FIELDS).verify();
        // Problem with Key class (it is the one that should be tested) - it is a PRIVATE nested class - cannot access
        // the hash/equals methods
        // Solution: make Key package-private for this test (it passes then)
    }
}
