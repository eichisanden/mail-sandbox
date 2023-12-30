package v1_6_7;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SendMailTest {
    private GreenMail greenMail;

    @BeforeEach
    void setUp() {
        greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();
    }

    @AfterEach
    void tearDown() {
        greenMail.stop();
    }

    @Test
    void sendMailShouldNotThrowExceptionWhenParametersAreValid() throws MessagingException {
        assertDoesNotThrow(() -> SendMail.sendMail("recipient@example.com", "Hello", "This is a test email."));

        greenMail.waitForIncomingEmail(1);
        String subject = greenMail.getReceivedMessages()[0].getSubject();
        assertEquals("Hello", subject);
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
        assertEquals("test aa a", subject);
    }

    @Test
    void sendMailShouldThrowExceptionWhenBodyIsNull() {
        assertThrows(NullPointerException.class, () -> SendMail.sendMail("recipient@example.com", "Hello", null));
    }
}