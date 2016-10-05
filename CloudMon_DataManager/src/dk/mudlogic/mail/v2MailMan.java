package dk.mudlogic.mail;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 * Created by soren.pedersen on 04-10-2016.
 */
public class v2MailMan {

    private String reply_to = "";
    private String hostname = "";


    public v2MailMan(String hostname,String reply_to) {
        this.hostname = hostname;
        this.reply_to = reply_to;
    }

    public void send(String send_to,String subject,String message) {

        String smtpHostServer = this.hostname;

        String emailID = send_to;

        Properties props = System.getProperties();

        props.put("mail.smtp.host", smtpHostServer);

        Session session = Session.getInstance(props, null);

        //sendEmail(session, emailID,"SimpleEmail Testing Subject", "SimpleEmail Testing Body");
        sendEmail(session, emailID,subject, message);

    }

    private void sendEmail(Session session, String toEmail, String subject, String body){
        try
        {
            MimeMessage msg = new MimeMessage(session);
            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            //msg.addHeader("Content-Transfer-Encoding", "8bit");

            //msg.setFrom(new InternetAddress("no_reply@journaldev.com", "NoReply-JD"));
            msg.setFrom(new InternetAddress(this.reply_to, "No-Reply"));

            msg.setReplyTo(InternetAddress.parse(this.reply_to, false));

            msg.setSubject(subject, "UTF-8");

            msg.setText(body, "UTF-8");

            msg.setSentDate(new Date());

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

            Transport.send(msg);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
