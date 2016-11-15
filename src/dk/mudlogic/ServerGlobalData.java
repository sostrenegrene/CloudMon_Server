package dk.mudlogic;

import dk.mudlogic.cloudmon.client.client_v2.CloudMon_v2Client;
import dk.mudlogic.cloudmon.client.client_v2.v2ProcessConfig;
import dk.mudlogic.cloudmon.config.CloudMon_ConfigThread;
import dk.mudlogic.cloudmon.dbstore.store.DB_ProcessReturnData;
import dk.mudlogic.cloudmon.datamanager.mail.MailMan;
import dk.mudlogic.tools.config.GroupConfig;
import dk.mudlogic.tools.database.MSSql;

import java.util.ArrayList;

/**
 * Created by soren.pedersen on 05-07-2016.
 */
public class ServerGlobalData {

    public static String config_file;
    public static GroupConfig MAIN_CONFIG;
    public static CloudMon_ConfigThread CONFIG_THREAD;

    public static v2ProcessConfig[] COMMAND_GROUPS;
    public static MSSql LOCAL_SQL = null;
    public static DB_ProcessReturnData PROCESS_RETURN_DATA;
    public static MailMan MAILMAN;

    public static ArrayList<CloudMon_v2Client> CLIENTS = new ArrayList<>();


}
