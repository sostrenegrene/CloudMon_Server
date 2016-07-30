package dk.mudlogic.cloudmon.config;

import dk.mudlogic.Main;
import dk.mudlogic.ServerGlobalData;
import dk.mudlogic.cloudmon.client_v2.CloudMon_v2Client;
import dk.mudlogic.cloudmon.client_v2.Load_v2ProcessConfig;
import dk.mudlogic.cloudmon.client_v2.v2ProcessConfig;
import dk.mudlogic.cloudmon.store.DB_ProcessReturnData;
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

    public void setup_clients() {
        //log.trace("setup client data");

        try {
            //Load client command groups
            COMMAND_GROUPS = new Load_v2ProcessConfig(this.sql).getConfig();

            ArrayList<CloudMon_v2Client> tmp = new ArrayList<>();
            //Create client process list
            for (int i = 0; i< COMMAND_GROUPS.length; i++) {
                CloudMon_v2Client client = new CloudMon_v2Client( new DB_ProcessReturnData(this.sql), COMMAND_GROUPS[i] );
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

            String url = (String)db.get("url");
            String database = (String) db.get("database");
            String username = (String) db.get("username");
            String password = (String) db.get("password");

            String running = (String) server.get("running");
            String install_path = (String) server.get("install_path");
            String server_interval = (String) server.get("server_interval");

            MAIN_CONFIG.group("database").put("url",url);
            MAIN_CONFIG.group("database").put("database",database);
            MAIN_CONFIG.group("database").put("username",username);
            MAIN_CONFIG.group("database").put("password",password);

            MAIN_CONFIG.group("server").put("running",running);
            MAIN_CONFIG.group("server").put("install_path",install_path);
            MAIN_CONFIG.group("server").put("server_interval",server_interval);
        }
        catch(Exception e) {
            log.error(e.getMessage());
        }

    }

}
