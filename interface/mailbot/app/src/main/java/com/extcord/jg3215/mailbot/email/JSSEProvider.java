package com.extcord.jg3215.mailbot.email;

import java.security.AccessController;
import java.security.Provider;

/*
 * NAME:        JSSEProvider.java
 * PURPOSE:     The Java Secure Socket Extension (JSSE) enables secure Internet communications. This
 *              script selects HarmonyJSSE as our secure socket extension and defines all the necessary
 *              details.
 *
 * AUTHORS:     Ifeanyi Chinweze, Javi Geis
 * NOTES:
 * REVISION:    19/12/2018
 */

public final class JSSEProvider extends Provider {

    public JSSEProvider() {
        super("HarmonyJSSE", 1.0, "Harmony JSSE Provider");
        AccessController.doPrivileged(new java.security.PrivilegedAction<Void>() {
            public Void run() {
                put("SSLContext.TLS", "org.apache.harmony.xnet.provider.jsse.SSLContextImpl");
                put("Alg.Alias.SSLContext.TLSv1", "TLS");
                put("KeyManagerFactory.X509", "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl");
                put("TrustManagerFactory.X509", "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl");
                return null;
            }
        });
    }
}
