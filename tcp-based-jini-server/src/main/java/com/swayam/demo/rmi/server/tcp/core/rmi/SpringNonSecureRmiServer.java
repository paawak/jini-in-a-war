package com.swayam.demo.rmi.server.tcp.core.rmi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sun.jini.start.ServiceStarter;
import com.swayam.demo.rmi.server.tcp.core.reggie.ReggieStarterConfiguration;

public class SpringNonSecureRmiServer {

    private static final Logger LOG = LoggerFactory
            .getLogger(SpringNonSecureRmiServer.class);

    public static void main(String[] args) throws InterruptedException {

        CountDownLatch signalToStartServerThread = new CountDownLatch(1);

        Thread reggieStarterThread = new Thread(new ReggieStarter(
                signalToStartServerThread));
        reggieStarterThread.setName(ReggieStarter.class.getSimpleName());
        reggieStarterThread.start();

        Thread rmiServerStarterThread = new Thread(new RMIServerStarter(
                signalToStartServerThread));
        rmiServerStarterThread.setName(RMIServerStarter.class.getSimpleName());
        rmiServerStarterThread.start();

    }

    private static class ReggieStarter implements Runnable {

        private final CountDownLatch signalToStartServerThread;

        public ReggieStarter(CountDownLatch signalToStartServerThread) {
            this.signalToStartServerThread = signalToStartServerThread;
        }

        @Override
        public void run() {
            String policyFilePath = System.getProperty("user.home")
                    + "/jini/policy.all";

            LOG.info("Starting with the policy file {}", policyFilePath);

            System.setProperty("java.security.policy", policyFilePath);

            // try {
            // new ConfigurationFile(
            // new String[] {
            // "D:/personal/code/jini-in-a-war/jini-in-a-war/jini-server/src/main/resources/jeri-reggie.config"
            // });
            // } catch (ConfigurationException e) {
            // LOG.error("Reggie did not start", e);
            // throw new RuntimeException(e);
            // }

            try {
                ServiceStarter
                        .main(new ReggieStarterConfiguration(
                                new URL(
                                        "file://" + System.getProperty("user.home")
                                                + "/jini/jeri-reggie.config")));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                LOG.error("Reggie was interrupted while starting up", e);
                throw new RuntimeException(e);
            }

            LOG.info("Started Reggie successfully");

            signalToStartServerThread.countDown();

        }
    }

    private static class RMIServerStarter implements Runnable {

        private final CountDownLatch signalToStartServerThread;

        public RMIServerStarter(CountDownLatch signalToStartServerThread) {
            this.signalToStartServerThread = signalToStartServerThread;
        }

        @Override
        public void run() {

            try {
                signalToStartServerThread.await();
            } catch (InterruptedException e) {
                LOG.error(
                        "RMI Server was interrupted while waiting for Reggie to start",
                        e);
                throw new RuntimeException(e);
            }

            try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                    "server-application.xml")) {
                LOG.info("The RMIServer is ready");
            }
        }

    }

}
