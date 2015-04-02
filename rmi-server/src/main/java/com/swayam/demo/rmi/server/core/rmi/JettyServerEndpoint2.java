/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.swayam.demo.rmi.server.core.rmi;

import java.io.IOException;
import java.util.logging.Logger;

import net.jini.core.constraint.InvocationConstraints;
import net.jini.io.UnsupportedConstraintException;
import net.jini.jeri.Endpoint;
import net.jini.jeri.ServerEndpoint;
import net.jini.security.Security;

import com.sun.jini.thread.Executor;
import com.sun.jini.thread.GetThreadPoolAction;
import com.swayam.demo.rmi.api.shared.JettyEndpoint2;

public final class JettyServerEndpoint2 implements ServerEndpoint {

    static final Executor systemThreadPool = (Executor) Security.doPrivileged(new GetThreadPoolAction(false));

    /** server transport logger */
    static final Logger logger = Logger.getLogger("net.jini.jeri.http.server");

    /** local host name to fill in to corresponding HttpEndpoints */
    private final String host;
    /** port to listen on */
    private final int port;

    public static JettyServerEndpoint2 getInstance(String host, int port) {
        return new JettyServerEndpoint2(host, port);
    }

    private JettyServerEndpoint2(String host, int port) {
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("port number out of range: " + port);
        }
        this.host = host;
        this.port = port;

    }

    public InvocationConstraints checkConstraints(InvocationConstraints constraints) throws UnsupportedConstraintException {
        return InvocationConstraints.EMPTY;
    }

    public Endpoint enumerateListenEndpoints(ListenContext listenContext) throws IOException {

        ListenEndpointImpl listenEndpoint = new ListenEndpointImpl(port);

        listenContext.addListenEndpoint(listenEndpoint);

        return JettyEndpoint2.getInstance(host, port);
    }

}
