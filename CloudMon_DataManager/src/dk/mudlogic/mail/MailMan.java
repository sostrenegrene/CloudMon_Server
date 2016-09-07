package dk.mudlogic.mail;

import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.web.http.HttpQuery;

/**
 * Created by soren.pedersen on 03-08-2016.
 */
public class MailMan {

    private LogTracer log = new LogFactory().tracer();
    private String send_url;
    private boolean is_active;

    public MailMan(String send_url) {
        log.setTracerTitle(MailMan.class);

        this.send_url = send_url;
        this.is_active = true;
    }

    /** Can be used to switch on/off mail sending
     * while testing
     *
     * @param b boolean
     */
    public void isActive(boolean b) {
        this.is_active = b;

        log.trace("Send mail is " + b);
    }

    public void send(String to,String subject,String message) {

        if (this.is_active) {
            HttpQuery http = new HttpQuery();
            http.post(send_url, "mail_toname=&mail_tomail=" + to + "&mail_subject=" + subject + "&mail_message=" + message);
        }
        else {
            log.warning("Send mail is off");
        }

    }

}
