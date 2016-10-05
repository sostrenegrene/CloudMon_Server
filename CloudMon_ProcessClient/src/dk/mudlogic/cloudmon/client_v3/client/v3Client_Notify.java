package dk.mudlogic.cloudmon.client_v3.client;

import dk.mudlogic.mail.v2MailMan;

/** The notifier, handles messages sent as mails ect.
 *
 *
 * Created by soren.pedersen on 04-10-2016.
 */
public class v3Client_Notify {

    private v2MailMan mailMan;

    public v3Client_Notify(String hostname,String send_to) {
        mailMan = new v2MailMan(hostname,send_to);
    }

    public void notify_email(v3Command command) {

        int err_min_len = 60;//Length of message for subject
        String err_min;

        if (command.getResult().getErrorMessages().length() > err_min_len) { err_min = command.getResult().getErrorMessages().substring(0,err_min_len) + "..."; }
        else { err_min = command.getResult().getErrorMessages(); }

        String subject = "";
        subject += boolToString( command.getResult().hasErrors() ) + ": ";
        subject += command.get_str("client_name") + "/";
        subject += command.get_str("process_name") + ": ";
        subject += err_min;

        String message = "";
        message += "Client: " + command.get_str("client_name") + "\r\n";
        message += "Group: " + command.get_str("group_name") + "\r\n";
        message += "Command: " + command.get_str("process_name") + "\r\n";
        message += "Status: " + boolToString( command.getResult().hasErrors() ) + "\r\n";
        message += "Error: " + command.getResult().getErrorMessages() + "\r\n";

        mailMan.send("support@sostrenegrene.com",subject,message);
    }

    private String boolToString(boolean b) {
        if (b) { return "FAILED"; }
        else { return "OK"; }
    }
}
