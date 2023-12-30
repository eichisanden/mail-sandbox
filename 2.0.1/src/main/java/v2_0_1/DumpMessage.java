package v2_0_1;

import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.ParseException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class DumpMessage {
    public static void dumpPart(Part p) throws MessagingException, IOException {
        dumpEnvelope((Message)p);

        String ct = p.getContentType();
        try {
            pr("CONTENT-TYPE: " + (new ContentType(ct)));
        } catch (ParseException pex) {
            pr("BAD CONTENT-TYPE: " + ct);
        }
        String filename = p.getFileName();
        if (filename != null)
            pr("FILENAME: " + filename);

        /*
         * Using isMimeType to determine the content type avoids
         * fetching the actual content data until we need it.
         */
        if (p.isMimeType("text/plain")) {
            pr("This is plain text");
            pr("---------------------------");
            System.out.println((String)p.getContent());
        } else if (p.isMimeType("multipart/*")) {
            pr("This is a Multipart");
            pr("---------------------------");
            Multipart mp = (Multipart)p.getContent();
            level++;
            int count = mp.getCount();
            for (int i = 0; i < count; i++)
                dumpPart(mp.getBodyPart(i));
            level--;
        } else if (p.isMimeType("message/rfc822")) {
            pr("This is a Nested Message");
            pr("---------------------------");
            level++;
            dumpPart((Part)p.getContent());
            level--;
        } else {
            Object o = p.getContent();
            if (o instanceof String) {
                pr("This is a string");
                pr("---------------------------");
                System.out.println((String)o);
            } else if (o instanceof InputStream is) {
                pr("This is just an input stream");
                pr("---------------------------");
                int c;
                while ((c = is.read()) != -1)
                    System.out.write(c);
            } else {
                pr("This is an unknown type");
                pr("---------------------------");
                pr(o.toString());
            }
        }

        /*
         * If we're saving attachments, write out anything that
         * looks like an attachment into an appropriately named
         * file.  Don't overwrite existing files to prevent
         * mistakes.
         */
        if (level != 0 && p instanceof MimeBodyPart &&
                !p.isMimeType("multipart/*")) {
            String disp = p.getDisposition();
            // many mailers don't include a Content-Disposition
            if (disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT)) {
                if (filename == null)
                    filename = "Attachment";
                pr("Saving attachment to file " + filename);
                try {
                    File f = new File(filename);
                    if (f.exists())
                        // XXX - could try a series of names
                        throw new IOException("file exists");
                    ((MimeBodyPart)p).saveFile(f);
                } catch (IOException ex) {
                    pr("Failed to save attachment: " + ex);
                }
                pr("---------------------------");
            }
        }
    }

    public static void dumpEnvelope(Message m) throws MessagingException {
        pr("This is the message envelope");
        pr("---------------------------");
        Address[] a;
        // FROM
        if ((a = m.getFrom()) != null) {
            for (Address address : a) pr("FROM: " + address.toString());
        }

        // REPLY TO
        if ((a = m.getReplyTo()) != null) {
            for (Address address : a) pr("REPLY TO: " + address.toString());
        }

        // TO
        if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
            for (Address address : a) {
                pr("TO: " + address.toString());
                InternetAddress ia = (InternetAddress) address;
                if (ia.isGroup()) {
                    InternetAddress[] aa = ia.getGroup(false);
                    for (InternetAddress internetAddress : aa) pr("  GROUP: " + internetAddress.toString());
                }
            }
        }

        // SUBJECT
        pr("SUBJECT: " + m.getSubject());

        // DATE
        Date d = m.getSentDate();
        pr("SendDate: " +
                (d != null ? d.toString() : "UNKNOWN"));

        // FLAGS
        StringBuilder sb = getFlagsString(m);
        pr("FLAGS: " + sb);

        // X-MAILER
        String[] hdrs = m.getHeader("X-Mailer");
        if (hdrs != null)
            pr("X-Mailer: " + hdrs[0]);
        else
            pr("X-Mailer NOT available");
    }

    private static StringBuilder getFlagsString(Message m) throws MessagingException {
        Flags flags = m.getFlags();
        StringBuilder sb = new StringBuilder();
        Flags.Flag[] sf = flags.getSystemFlags(); // get the system flags

        boolean first = true;
        for (Flags.Flag flag : sf) {
            String s;
            if (flag == Flags.Flag.ANSWERED)
                s = "\\Answered";
            else if (flag == Flags.Flag.DELETED)
                s = "\\Deleted";
            else if (flag == Flags.Flag.DRAFT)
                s = "\\Draft";
            else if (flag == Flags.Flag.FLAGGED)
                s = "\\Flagged";
            else if (flag == Flags.Flag.RECENT)
                s = "\\Recent";
            else if (flag == Flags.Flag.SEEN)
                s = "\\Seen";
            else
                continue;       // skip it
            if (first)
                first = false;
            else
                sb.append(' ');
            sb.append(s);
        }

        String[] uf = flags.getUserFlags(); // get the user flag strings
        for (String s : uf) {
            if (first)
                first = false;
            else
                sb.append(' ');
            sb.append(s);
        }
        return sb;
    }

    static String indentStr = "                                               ";
    static int level = 0;

    /**
     * Print a, possibly indented, string.
     */
    public static void pr(String s) {
        System.out.print(indentStr.substring(0, level * 2));
        System.out.println(s);
    }
}
