package com.swayam.demo.rmi.shared.jini.servlet;

import java.io.IOException;
import java.io.Serializable;
import java.util.NoSuchElementException;

import net.jini.core.constraint.InvocationConstraints;
import net.jini.jeri.Endpoint;
import net.jini.jeri.OutboundRequest;
import net.jini.jeri.OutboundRequestIterator;

public class ServletBasedEndpoint implements Endpoint, Serializable {

    private static final long serialVersionUID = 1L;

    public ServletBasedEndpoint() {
    }

    @Override
    public OutboundRequestIterator newRequest(InvocationConstraints constraints) {
        return new OutboundRequestIterator() {

            public OutboundRequest next() throws IOException {
                throw new NoSuchElementException();
            }

            public boolean hasNext() {
                return false;
            }
        };
    }

}
