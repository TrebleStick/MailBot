package com.extcord.jg3215.mailbot.email;

import android.util.Log;

import com.extcord.jg3215.mailbot.database.LockerItem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Random;

/*
 * NAME:        eMailService.java
 * PURPOSE:     eMailService defines and guides all the background functionality of sending e-mails.
 *              Starts by defining the GMail host as the e-mailing host for the application, and
 *              carries the necessary authentication steps and security measures. This is all sim-
 *              plified so the function sendMail can be called, with all the specifications of the
 *              e-mail (i.e. body text, address, subject...) in a simple way from any other activity.
 *
 * AUTHORS:     Ifeanyi Chinweze, Javi Geis
 * NOTES:
 * REVISION:    19/12/2018
 */

public class eMailService extends javax.mail.Authenticator {

    private String mailHost = "smtp.gmail.com";
    private String user;
    private String password;
    private Session session;

    private final static String TAG = "eMailService";

    static {
        Security.addProvider(new JSSEProvider());
    }

    public eMailService(String user, String password) {
        this.user = user;
        this.password = password;

        // Properties are like hashMaps where the key and value are strings
        Properties props = new Properties();

        // Set Gmail as host
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailHost);

        // Puts object key and object value in property
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body, String senderMail, String recipientMail) throws Exception {
        try {

            MimeMessage message = new MimeMessage(session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));

            message.setSubject(subject);
            message.setText(body);
            message.setDataHandler(handler);

            // Should only be one recipient for each mail
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientMail));

            Transport.send(message);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null) {
                return "application/octet-stream";
            } else {
                return type;
            }
        }

        // the 'throws' bit specifies the likely exception to occur in this method
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not supported");
        }
    }
}
