package dk.mudlogic.cloudmon.datamanager.mail;

import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/** Sends e-mail to SMTP server
 *
 * Created by soren.pedersen on 04-10-2016.
 */
public class v2MailMan {

    private LogTracer log = new LogFactory(v2MailMan.class.getName()).tracer();

    private String reply_to = "";
    private String hostname = "";


    /** Class constructor
     * Setup server hostname and reply address
     *
     * @param hostname String
     * @param reply_to String
     */
    public v2MailMan(String hostname,String reply_to) {
        this.hostname = hostname;
        this.reply_to = reply_to;
    }

    /** Send e-mail
     *
     * @param send_to String
     * @param subject String
     * @param message String
     */
    public void send(String send_to,String subject,String message) {

        String smtpHostServer = this.hostname;

        String emailID = send_to;

        Properties props = System.getProperties();

        props.put("mail.smtp.host", smtpHostServer);

        Session session = Session.getInstance(props, null);

        sendEmail(session, emailID,subject, message);

    }

    /** Setup mail headers and encoding
     * Send e-mail to recipient
     *
     * @param session String
     * @param toEmail String
     * @param subject String
     * @param message String
     */
    private void sendEmail(Session session, String toEmail, String subject, String message){
        log.warning("Sending mail to: " + session.getProperty("mail.smtp.host") + toEmail + ": " + subject);
        try
        {
            MimeMessage msg = new MimeMessage(session);
            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress(this.reply_to, "No-Reply"));

            msg.setReplyTo(InternetAddress.parse(this.reply_to, false));

            msg.setSubject(subject, "UTF-8");

            msg.setText(message, "UTF-8");

            msg.setSentDate(new Date());

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

            Transport.send(msg);
        }
        catch (Exception e) {
            //e.printStackTrace();

            //log.error(e.getMessage());
            log.warning("Could not send E-mail: " + e.getMessage());
            log.warning(toEmail + ": " + subject);
            e.printStackTrace();
        }
    }


}
