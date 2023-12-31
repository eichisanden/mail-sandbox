import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

public class SendMail {
    public static void sendMail(String to, String subject, String body) throws Exception {
        Email email = EmailBuilder
                .startingBlank()
                .to(to)
                .from("from@example.com")
                .withReplyTo("from@example.com")
                .withSubject(subject)
                .withPlainText(body)
                .buildEmail();
        Mailer mailer = MailerBuilder
                .withSMTPServer("localhost", 3025)
                .buildMailer();
        mailer.sendMail(email);
        mailer.close();
    }
}
