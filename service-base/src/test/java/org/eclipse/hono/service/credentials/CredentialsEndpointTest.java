/**
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial creation
 *
 */

package org.eclipse.hono.service.credentials;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.proton.ProtonQoS;
import io.vertx.proton.ProtonReceiver;
import io.vertx.proton.ProtonSender;

import org.eclipse.hono.util.ResourceIdentifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.eclipse.hono.util.CredentialsConstants.CREDENTIALS_ENDPOINT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests
 */
@RunWith(MockitoJUnitRunner.class)
public class CredentialsEndpointTest {
    private static final String DEFAULT_TENANT = "DEFAULT_TENANT";
    private static final String AUTH_ID        = "billie";

    private static final ResourceIdentifier resource = ResourceIdentifier.from(CREDENTIALS_ENDPOINT, DEFAULT_TENANT, null);

    @Mock private EventBus eventBus;
    @Mock private Vertx    vertx;

    private ProtonReceiver receiver;
    private ProtonSender sender;


    private CredentialsEndpoint endpoint;

    @Before
    public void setUp() throws Exception {
        when(vertx.eventBus()).thenReturn(eventBus);
        receiver = mock(ProtonReceiver.class);
        when(receiver.handler(any())).thenReturn(receiver);
        when(receiver.closeHandler(any())).thenReturn(receiver);
        when(receiver.setAutoAccept(any(Boolean.class))).thenReturn(receiver);
        when(receiver.setPrefetch(any(Integer.class))).thenReturn(receiver);
        when(receiver.setQoS(any(ProtonQoS.class))).thenReturn(receiver);
        sender = mock(ProtonSender.class);
        endpoint = new CredentialsEndpoint(vertx);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testForbidAtMostOnceQoS() {

        when(receiver.getRemoteQoS()).thenReturn(ProtonQoS.AT_MOST_ONCE);
        endpoint.onLinkAttach(receiver, resource);

        verify(receiver).close();
    }

    @Test
    public void testOpenLink() {

        when(receiver.getRemoteQoS()).thenReturn(ProtonQoS.AT_LEAST_ONCE);
        endpoint.onLinkAttach(receiver, resource);

        verify(receiver).open();
        verify(receiver,never()).close();

    }

    @Test
    public void testLinkClosedIfReplyAddressIsMissing() {

        endpoint.onLinkAttach(sender, resource);

        verify(sender).setCondition(any());
        verify(sender).close();
    }
}
