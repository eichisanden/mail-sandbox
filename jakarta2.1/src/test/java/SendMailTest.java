import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SendMailTest {
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Test
    void sendMailShouldNotThrowExceptionWhenParametersAreValid() {
        assertDoesNotThrow(() -> SendMail.sendMail("recipient@example.com", "Hello", "This is a test email."));
    }

    @Test
    void sendMailShouldThrowExceptionWhenToEmailIsInvalid() {
        assertThrows(AddressException.class,
                () -> SendMail.sendMail("invalid-email.@example.com", "Hello", "This is a test email."));
    }

    @Test
    void sendMailShouldThrowExceptionWhenSubjectIsNull() throws MessagingException {
        assertDoesNotThrow(() -> SendMail.sendMail("recipient@example.com", "test\n\naa\na", "This is a test email."));
        greenMail.waitForIncomingEmail(1);
        String subject = greenMail.getReceivedMessages()[0].getSubject();
        assert subject.equals("test aa a");
    }

    @Test
    void sendMailShouldThrowExceptionWhenBodyIsNull() {
        assertThrows(NullPointerException.class, () -> SendMail.sendMail("recipient@example.com", "Hello", null));
    }
}