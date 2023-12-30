package dev.v1_6_7;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.IOException;
import java.util.Properties;

public class SendMail {
    public static void sendMail(String to, String subject, String body) throws MessagingException, IOException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "3025");

        Session session = Session.getInstance(props, null);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from-email@gmail.com"));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        DumpMessage.dumpPart(message);

        Transport.send(message);
    }

}