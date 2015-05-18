package com.swayam.demo.rmi.server.servlet.core.jini.servlet;

import net.jini.core.constraint.InvocationConstraints;
import net.jini.jeri.Endpoint;
import net.jini.jeri.ServerEndpoint;

import com.swayam.demo.rmi.shared.jini.servlet.ServletBasedEndpoint;

public class ServletBasedServerEndpoint implements ServerEndpoint {

    public ServletBasedServerEndpoint() {
    }

    @Override
    public InvocationConstraints checkConstraints(InvocationConstraints constraints) {
        return InvocationConstraints.EMPTY;
    }

    @Override
    public Endpoint enumerateListenEndpoints(ListenContext listenContext) {
        return new ServletBasedEndpoint();
    }

}
