import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.markenwerk.utils.mail.dkim.Canonicalization;
import net.markenwerk.utils.mail.dkim.DkimException;
import net.markenwerk.utils.mail.dkim.DkimMessage;
import net.markenwerk.utils.mail.dkim.DkimSigner;
import net.markenwerk.utils.mail.dkim.SigningAlgorithm;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;

public class SendMailDkim {
    static final String EXAMPLE_DOMAIN_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCwiFR8iVMZQmvA\n" + //
            "2dbSATCuqg0vUcqZhjcKtXZWhhiCyaX03EseQf8vUx/z1/Fq2J0QyNIoJbz5MSFd\n" + //
            "fFACFrWPzVv3HhHrKgpOT772BYl90tsUF0CcKJtNw9i9P8zeT/REtXXefsJ658dr\n" + //
            "I4riqYXsxDOM+snMZDTe8H61XrektZ8Xljjnh+CpRDk/L7iouDLN865S/R5Kpwwr\n" + //
            "wy7msZ0uxEj9rxKdC9dHlS4hzAQPBq4/AB1cxZV/Jhb1oPFXnPORsShLKG3OWrkv\n" + //
            "phvPw+QrI5ty9S8+HBwM2weztaMMJzXzsaTIOTIDLABGMD10a2nhz/rUFhnFdMql\n" + //
            "xDskK8epAgMBAAECggEAT+HhGVmq2MtNpz7sDqurM96PiNIiM8agMaBbpFuZy9Uj\n" + //
            "2+GzvEVxDCE5XEZjYE4VBPta3f/1u22YA421Rryv/BqZewGIajnj3/wGFZ370YwQ\n" + //
            "xs6e1gsp+86bzUeJlS/pAQ57/+qsZqFrg/fZaSf9Cl7eVPVHS84sfpWlQL1TrJwh\n" + //
            "7fFrnYj8ban7ZeJ2ewJw11NAS1CT2lx8AZxDs4XZuTueAb8YBEqDVxi6baNNaz5B\n" + //
            "ZRZ4pXA3wSd4NAo/CyPL7JKTngvKj0EDilzwtn+v/UUNc2UE18IFsreuGOPlIMIz\n" + //
            "XeVJCM4i/Rw8q6YQjQ31Gj6BaiNHrC3ziGHdFDQ/YQKBgQDoi+C11vhvFTc6FYaJ\n" + //
            "twdHcNsxLG+3FgAqtZal1I4YKPvtU9SDYAGt7JHNrp/JQ72uEoYiAzJ7u2H5afgc\n" + //
            "bxQgI9VIv6R61BIlBWPgoj+dXT8UU05GM5pRU+rV1jXstCItafXqEmQokYTBt7FH\n" + //
            "f+Qko1hH4wAVWD1UYrmTy2dEOwKBgQDCVjqTBrbb8KwHhRzjRxvJ7JBgtfq64RaP\n" + //
            "/S5aqlSTMAAYSP1ZjPzWIEc/heg/6r5/jtEnPMUN3/LbOBPYhZ49+7fSsdkX1CHh\n" + //
            "wfnme3r3e3ZbISZj7IuewqEL8nm4ptI9XfHPuHarkLx/v7YIgUhW69eopX7VIE5S\n" + //
            "pF4ZLAGZawKBgQCXL9m4XzrFfZWaPcRqZIRm/giDv9AhyDvziHuY9MgtDPg9I/NW\n" + //
            "NmThHpzsjEt2HoSDV6e9FTcNGpZoAW09jzoWazWPRok5Egd56jc1JEcXmDgajs6k\n" + //
            "9YeuuFBFFi+Y5MQTooAu3iYf+fvFtheb1JoO9+O9WOgE+IlnA4iDfgp1YQKBgHDb\n" + //
            "OJrnfEvPwWeAkx0dz3J9Mf8nz1q1mq+13TSD1CfB6VwxDwfxS2diSV4Heq+buFNF\n" + //
            "cgryTJFZK6TJNSU5u4LNapwt8h/XbnG3f1OtA+UyyrHzV2MSHKbu6i2Q+8UTA+bl\n" + //
            "Zqc1vZy2qfEaOTFJOuRXg98JdNV6NGHj2E2P88HPAoGBAJlzNZ/GiAuQEi0tBYl/\n" + //
            "9Q1ksZXdHwm1Miin5JqZj5i/z3mQi3eUZ0iQn37Yx8dHGLpuAq0XE2uwBVszjmum\n" + //
            "UdFl2Zzs/qCLcwT4eXTaAQG84rAz18eqA1F8/IzcsxBBN+YD4CxycYYR38Ym1YGp\n" + //
            "Sum3jiUnjKIOvhfkx/aRObkz";

    static MimeMessage dkimSignMessage(MimeMessage message, String from, String signingDomain, String selector)
            throws MessagingException, DkimException, InvalidKeySpecException, NoSuchAlgorithmException {
        DkimSigner dkimSigner = new DkimSigner(signingDomain, selector, getDkimPrivateKeyFileForSender(from));
        dkimSigner.setIdentity(from);
        dkimSigner.setHeaderCanonicalization(Canonicalization.SIMPLE);
        dkimSigner.setBodyCanonicalization(Canonicalization.RELAXED);
        dkimSigner.setSigningAlgorithm(SigningAlgorithm.SHA256_WITH_RSA);
        dkimSigner.setLengthParam(true);
        dkimSigner.setCopyHeaderFields(false);
        dkimSigner.setCheckDomainKey(true);
        return new DkimMessage(message, dkimSigner);
    }

    static RSAPrivateKey getDkimPrivateKeyFileForSender(String from)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] privateKeyBytes = Base64.getDecoder().decode(EXAMPLE_DOMAIN_KEY.replaceAll("\n", ""));
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        return (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
    }

    public static void sendMail(String to, String subject, String body)
            throws MessagingException, IOException, DkimException, InvalidKeySpecException, NoSuchAlgorithmException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "3025");

        Session session = Session.getInstance(props, null);
        String from = "from-email@gmail.com";
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);
        MimeMessage dkimSignedMessage = dkimSignMessage(message, from, "a-1.dev", "test1224");

        DumpMessge.dumpPart(dkimSignedMessage);

        Transport.send(dkimSignedMessage);
    }

}