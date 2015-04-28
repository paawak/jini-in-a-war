package com.swayam.demo.rmi.server.core.web;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jini.io.MarshalInputStream;
import net.jini.io.MarshalOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sun.jini.jeri.internal.runtime.Util;
import com.swayam.demo.rmi.server.core.rmi.ServiceExporter;

@Controller
public class RmiWebController implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(RmiWebController.class);

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @RequestMapping(value = ServiceExporter.REMOTE_METHOD_INVOCATION_URI)
    public void handleRemoteMethodInvocation(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("reading input parameters...");
        Object result;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (MarshalInputStream mis = new MarshalInputStream(request.getInputStream(), cl, false, cl, Collections.emptyList());) {
            String implClassName = (String) mis.readObject();
            Class<?> implClass = cl.loadClass(implClassName);
            long methodHash = mis.readLong();
            Method method = getMethod(implClass, methodHash);
            Object[] args = unmarshalArguments(method, mis);
            Object remoteImpl = applicationContext.getBean(implClass);
            LOG.info("invoking remote method...");
            result = method.invoke(remoteImpl, args);
        }

        LOG.info("writing results...");
        try (MarshalOutputStream mos = new MarshalOutputStream(response.getOutputStream(), Collections.emptyList());) {
            mos.writeObject(result);
            mos.flush();
        }
    }

    private Method getMethod(Class<?> implClass, long methodHash) {
        for (Method method : implClass.getMethods()) {
            if (Util.getMethodHash(method) == methodHash) {
                return method;
            }
        }
        throw new IllegalArgumentException("No corresponding method found for hash: " + methodHash);
    }

    private Object[] unmarshalArguments(Method method, ObjectInputStream in) throws IOException, ClassNotFoundException {
        Class<?>[] types = method.getParameterTypes();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            args[i] = Util.unmarshalValue(types[i], in);
        }
        return args;
    }

}
