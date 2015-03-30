package com.swayam.demo.rmi.api.shared;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.SocketFactory;

import com.sun.jini.jeri.internal.http.HttpClientSocketFactory;
import com.sun.jini.logging.Levels;
import com.sun.jini.logging.LogUtil;

/**
 * SocketFactory -> HttpClientSocketFactory adapter.
 **/
final class SocketFactoryAdapter implements HttpClientSocketFactory {

    private static final Logger logger = Logger.getLogger("net.jini.jeri.http.client");

    private final SocketFactory sf;

    SocketFactoryAdapter(SocketFactory sf) {
        this.sf = sf;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket socket = connectToHost(host, port);

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "connected socket {0}", socket);
        }

        setSocketOptions(socket);

        return socket;
    }

    @Override
    public Socket createTunnelSocket(Socket s) throws IOException {
        // proxy tunneling never used
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a socket connected to the specified host and port, according to
     * the specified constraints. If the host name resolves to multiple
     * addresses, attempts to connect to each of them in order until one
     * succeeds.
     **/
    private Socket connectToHost(String host, int port) throws IOException {
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException uhe) {
            try {
                /*
                 * Creating the InetSocketAddress attempts to resolve the host
                 * again; in J2SE 5.0, there is a factory method for creating an
                 * unresolved InetSocketAddress directly.
                 */
                return connectToSocketAddress(new InetSocketAddress(host, port));
            } catch (IOException e) {
                if (logger.isLoggable(Levels.FAILED)) {
                    LogUtil.logThrow(logger, Levels.FAILED, SocketFactoryAdapter.class, "connectToHost", "exception connecting to unresolved host {0}", new Object[] { host + ":"
                            + port }, e);
                }
                throw e;
            } catch (SecurityException e) {
                if (logger.isLoggable(Levels.FAILED)) {
                    LogUtil.logThrow(logger, Levels.FAILED, SocketFactoryAdapter.class, "connectToHost", "exception connecting to unresolved host {0}", new Object[] { host + ":"
                            + port }, e);
                }
                throw e;
            }
        } catch (SecurityException e) {
            if (logger.isLoggable(Levels.FAILED)) {
                LogUtil.logThrow(logger, Levels.FAILED, SocketFactoryAdapter.class, "connectToHost", "exception resolving host {0}", new Object[] { host }, e);
            }
            throw e;
        }
        IOException lastIOException = null;
        SecurityException lastSecurityException = null;
        for (int i = 0; i < addresses.length; i++) {
            SocketAddress socketAddress = new InetSocketAddress(addresses[i], port);
            try {
                return connectToSocketAddress(socketAddress);
            } catch (IOException e) {
                if (logger.isLoggable(Levels.HANDLED)) {
                    LogUtil.logThrow(logger, Levels.HANDLED, SocketFactoryAdapter.class, "connectToHost", "exception connecting to {0}", new Object[] { socketAddress }, e);
                }
                lastIOException = e;
                if (e instanceof SocketTimeoutException) {
                    break;
                }
            } catch (SecurityException e) {
                if (logger.isLoggable(Levels.HANDLED)) {
                    LogUtil.logThrow(logger, Levels.HANDLED, SocketFactoryAdapter.class, "connectToHost", "exception connecting to {0}", new Object[] { socketAddress }, e);
                }
                lastSecurityException = e;
            }
        }
        if (lastIOException != null) {
            if (logger.isLoggable(Levels.FAILED)) {
                LogUtil.logThrow(logger, Levels.FAILED, SocketFactoryAdapter.class, "connectToHost", "exception connecting to {0}", new Object[] { host + ":" + port },
                        lastIOException);
            }
            throw lastIOException;
        }
        assert lastSecurityException != null;
        if (logger.isLoggable(Levels.FAILED)) {
            LogUtil.logThrow(logger, Levels.FAILED, SocketFactoryAdapter.class, "connectToHost", "exception connecting to {0}", new Object[] { host + ":" + port },
                    lastSecurityException);
        }
        throw lastSecurityException;
    }

    /**
     * Returns a socket connected to the specified address, with a timeout
     * governed by the specified constraints.
     **/
    private Socket connectToSocketAddress(SocketAddress socketAddress) throws IOException {
        int timeout = 0;

        Socket socket = newSocket();
        boolean ok = false;
        try {
            socket.connect(socketAddress, timeout);
            ok = true;
            return socket;
        } finally {
            if (!ok) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Returns a new unconnected socket, using this endpoint's socket factory if
     * non-null.
     **/
    private Socket newSocket() throws IOException {
        Socket socket;
        if (sf != null) {
            socket = sf.createSocket();
        } else {
            socket = new Socket();
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, (sf == null ? "created socket {0}" : "created socket {0} using factory {1}"), new Object[] { socket, sf });
        }

        return socket;
    }

    /**
     * Attempts to set desired socket options for a connected socket
     * (TCP_NODELAY and SO_KEEPALIVE); ignores SocketException.
     **/
    static void setSocketOptions(Socket socket) {
        try {
            socket.setTcpNoDelay(true);
        } catch (SocketException e) {
            if (logger.isLoggable(Levels.HANDLED)) {
                LogUtil.logThrow(logger, Levels.HANDLED, JettyEndpoint2.class, "setSocketOptions", "exception setting TCP_NODELAY on socket {0}", new Object[] { socket }, e);
            }
        }
        try {
            socket.setKeepAlive(true);
        } catch (SocketException e) {
            if (logger.isLoggable(Levels.HANDLED)) {
                LogUtil.logThrow(logger, Levels.HANDLED, JettyEndpoint2.class, "setSocketOptions", "exception setting SO_KEEPALIVE on socket {0}", new Object[] { socket }, e);
            }
        }
    }

}