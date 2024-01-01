import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import net.markenwerk.utils.mail.dkim.Canonicalization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DkimSendMailTest {
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Test
    public void testDkimSendMail() throws Exception {
        // when
        DkimSendMail.sendMail("from@example.com", "to@example.com");

        // wait for incoming email
        greenMail.waitForIncomingEmail(1);
        var message = greenMail.getReceivedMessages()[0];

        // then
        var signatures = message.getHeader("DKIM-Signature");
        assertEquals(signatures.length, 1);
        var map = Arrays.stream(signatures[0].split(";"))
                .map(String::trim)
                .map(x -> x.split("="))
                .collect(Collectors.toMap(x -> x[0], x -> x[1]));
        assertEquals(map.get("a"), "rsa-sha256");
        assertEquals(map.get("q"), "dns/txt");
        assertEquals(map.get("c"), "simple/relaxed");
        assertEquals(map.get("s"), "selector");
        assertEquals(map.get("d"), "example.com");
        assertEquals(map.get("h"), "Content-Transfer-Encoding:Content-Type:MIME-Version:Subject:Message-ID:To:From:Date");
        assertEquals(map.get("i"), "from@example.com");
        System.out.println(map.get("b"));
        System.out.println(map.get("bh"));
    }

    @Test
    public void testSendMail() throws Exception {
        var signer = DkimSendMail.getSigner("from@example.com", "example.com", "selector");
        assertEquals(signer.getIdentity(), "from@example.com");
        assertEquals(signer.getBodyCanonicalization(), Canonicalization.RELAXED);
        assertEquals(signer.getHeaderCanonicalization(), Canonicalization.SIMPLE);
    }

    @Test
    public void testGetDkimPrivateKeyFileForSender() throws Exception {
        var result = DkimSendMail.getDkimPrivateKeyFileForSender();
        assertEquals(result.getAlgorithm(), "RSA");
        assertEquals(result.getFormat(), "PKCS#8");
    }
}
