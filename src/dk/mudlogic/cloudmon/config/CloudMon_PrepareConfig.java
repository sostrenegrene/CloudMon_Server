package dk.mudlogic.cloudmon.config;

import dk.mudlogic.Main;
import dk.mudlogic.cloudmon.client.client_v2.CloudMon_v2Client;
import dk.mudlogic.cloudmon.client.client_v2.Load_v2ProcessConfig;
import dk.mudlogic.cloudmon.client.client_v2.v2ProcessConfig;
import dk.mudlogic.cloudmon.dbstore.store.DB_ProcessReturnData;
import dk.mudlogic.tools.config.GroupConfig;
import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.files.File_Reader;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;

import static dk.mudlogic.Main.closeAndExit;

/**
 * Created by soren.pedersen on 29-07-2016.
 */
public class CloudMon_PrepareConfig {

    private LogTracer log = new LogFactory().tracer();
    private MSSql sql;
    private String config_file;

    private v2ProcessConfig[] COMMAND_GROUPS;
    private ArrayList<CloudMon_v2Client> CLIENTS = new ArrayList<>();

    private GroupConfig MAIN_CONFIG;

    public CloudMon_PrepareConfig(MSSql sql,String config_file) {
        log.setTracerTitle(CloudMon_PrepareConfig.class);

        this.sql = sql;
        this.config_file = config_file;

        setup_config();
        setup_clients();
    }

    public CloudMon_PrepareConfig(String config_file) {
        log.setTracerTitle(CloudMon_PrepareConfig.class);

        this.config_file = config_file;

        setup_config();
    }

    public GroupConfig getConfig() {
        return MAIN_CONFIG;
    }

    public ArrayList<CloudMon_v2Client> getClients() {
        return CLIENTS;
    }

    public void getClientConfig() {

    }

    /** Loads command list from database
     * Generates array of process clients
     *
     */
    public void setup_clients() {
        //log.trace("setup client data");

        try {
            //Load client command groups
            COMMAND_GROUPS = new Load_v2ProcessConfig(this.sql).getConfig();

            ArrayList<CloudMon_v2Client> tmp = new ArrayList<>();
            //Create client process list
            for (int i = 0; i< COMMAND_GROUPS.length; i++) {
                CloudMon_v2Client client = new CloudMon_v2Client(this.MAIN_CONFIG, new DB_ProcessReturnData(this.sql), COMMAND_GROUPS[i] );
                tmp.add(client);
            }

            CLIENTS = tmp;
            tmp = null;

            log.trace("Loaded " + CLIENTS.size() + " clients");
        }
        //Failed creating process list
        catch(Exception e) {
            log.error("Unable to load client configuration ");
            log.error(e.getMessage());
            e.printStackTrace();

            closeAndExit();
        }

    }

    public void setup_config() {
        //log.trace("setup config data");

        try {
            File_Reader read = new File_Reader( this.config_file );//"c:/CloudMon/config.json"
            JSONObject jobj = (JSONObject) new JSONParser().parse( read.readfile() );

            MAIN_CONFIG = new GroupConfig("Config_"+Main.class.getSimpleName());

            JSONObject db = (JSONObject) jobj.get("database");
            JSONObject server = (JSONObject) jobj.get("server");
            JSONObject mail = (JSONObject) jobj.get("mail");

            String[] db_keys = (String[]) db.keySet().toArray(new String[db.keySet().size()]);
            String[] db_values = (String[]) db.values().toArray(new String[db.values().size()]);
            MAIN_CONFIG.generate_group("database",db_keys,db_values);

            String[] server_keys = (String[]) server.keySet().toArray(new String[server.keySet().size()]);
            String[] server_values = (String[]) server.values().toArray(new String[server.values().size()]);
            MAIN_CONFIG.generate_group("server",server_keys,server_values);

            String[] mail_keys = (String[]) mail.keySet().toArray(new String[mail.keySet().size()]);
            String[] mail_values = (String[]) mail.values().toArray(new String[mail.values().size()]);
            MAIN_CONFIG.generate_group("mail",mail_keys,mail_values);
        }
        catch(Exception e) {
            log.error(e.getMessage());
        }

    }

}
