package dk.mudlogic.cloudmon.store;

import dk.mudlogic.ServerGlobalData;
import dk.mudlogic.cloudmon.client_v2.v2ProcessCommand;
import dk.mudlogic.tools.config.GroupConfig;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by soren.pedersen on 03-08-2016.
 */
public class SendMail {

    private LogTracer log = new LogFactory().tracer();

    public SendMail(GroupConfig main_config, String type, v2ProcessCommand pTable, boolean is_failed,String reason) {
        log.setTracerTitle(SendMail.class);
        log.warning("Sending mail for: "+pTable.get_str("process_name"));

        //String to = "support@sostrenegrene.com";
        String to = main_config.group("mail").get("mail_to").toString();
        String subject = statusStr(is_failed) + ": " + pTable.get_str("process_name") + "/" + pTable.get_str("client_name")+" "+type;
        String message = type + "<br>\r\n";
        message += "Status has changed<br>\r\n";
        message += "Client: "+pTable.get_str("client_name")+"<br>\r\n";
        message += "Process: " + pTable.get_str("process_type") + "/" + pTable.get_str("process_name")+"<br>\r\n";
        message += "Status: " + statusStr(is_failed) + "<br>\r\n";
        message += "Status: " + reason + "<br>\r\n";

        log.warning(subject);

        try {
            to = URLEncoder.encode(to,"UTF-8");
            subject = URLEncoder.encode(subject,"UTF-8");
            message = URLEncoder.encode(message,"UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ServerGlobalData.MAILMAN.send(to,subject,message);
    }



    private String statusStr(boolean status) {
        if (status) { return "FAILED"; }
        else { return "OK"; }
    }

}
