import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.markenwerk.utils.mail.dkim.Canonicalization;
import net.markenwerk.utils.mail.dkim.DkimMessage;
import net.markenwerk.utils.mail.dkim.DkimSigner;
import net.markenwerk.utils.mail.dkim.SigningAlgorithm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

public class DkimSendMail {
    public static void sendMail(String from, String to) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "3025");
        Session session = Session.getInstance(props, null);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("DKIM test");
        message.setContent("Helloworld!\r\n", "text/plain; charset=UTF-8");
        DkimSigner dkimSigner = getSigner(from, "example.com", "selector");
        Transport.send(new DkimMessage(message, dkimSigner));
    }

    static DkimSigner getSigner(String from, String signingDomain, String selector)  throws Exception {
        DkimSigner dkimSigner = new DkimSigner(signingDomain, selector, getDkimPrivateKeyFileForSender());
        dkimSigner.setIdentity(from);
        dkimSigner.setHeaderCanonicalization(Canonicalization.SIMPLE);
        dkimSigner.setBodyCanonicalization(Canonicalization.RELAXED);
        dkimSigner.setSigningAlgorithm(SigningAlgorithm.SHA256_WITH_RSA);
        dkimSigner.setLengthParam(true);
        dkimSigner.setCopyHeaderFields(false);
        dkimSigner.setCheckDomainKey(false);
        return dkimSigner;
    }

    static RSAPrivateKey getDkimPrivateKeyFileForSender() throws Exception {
        KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");

        // RSA秘密鍵からDER形式に変換する
        var path = Path.of(Objects.requireNonNull(DkimSendMail.class.getClassLoader().getResource("dkim.pem")).toURI());
        String pem;
        try (var lines = Files.lines(path)) {
            pem = lines.filter(line -> !line.startsWith("-----"))
                    .map(line -> line.replaceAll("\\s", ""))
                    .collect(Collectors.joining());
        }
        var der = Base64.getDecoder().decode(pem);
        return (RSAPrivateKey) rsaKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(der));
    }
}
