package dk.mudlogic;

import dk.mudlogic.cloudmon.client.client_v3.client.v3Client;
import dk.mudlogic.cloudmon.v3config.ClientUpdater;
import dk.mudlogic.cloudmon.v3config.CloudMon_LoadClientConfig;
import dk.mudlogic.tools.config.GroupConfig;
import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.files.File_Reader;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Created by soren.pedersen on 01-08-2016.
 */
public class Main2 {

    public static LogTracer log = new LogFactory().tracer();

    public static GroupConfig MAIN_CONFIG;
    public static CloudMon_LoadClientConfig LOAD_CONFIG;

    public static boolean isAlive = true;

    public static void main(String[] args) {
        log.trace("Starting CloudMon Server");

        run();
    }

    public static void run() {

        Main2.MAIN_CONFIG = file_config();

        //Setup database connection
        MSSql sql = new MSSql(Main2.MAIN_CONFIG.group("database").get("url").toString(),
                Main2.MAIN_CONFIG.group("database").get("username").toString(),
                Main2.MAIN_CONFIG.group("database").get("password").toString(),
                Main2.MAIN_CONFIG.group("database").get("database").toString());

        try {
            //Start database connection
            sql.connect();

            //Setup client config loading
            CloudMon_LoadClientConfig loadClient = new CloudMon_LoadClientConfig(sql,Main2.MAIN_CONFIG);
            //Add client loader to global config
            Main2.LOAD_CONFIG = loadClient;
            //Load initial client config
            v3Client[] clients = loadClient.toArray();

            log.trace("Loaded " + clients.length + " clients");

            boolean isAlive = true;
            while(Main2.isAlive) {
                clients = new ClientUpdater(loadClient.getClients(),clients).getClients();

                for (int i = 0; i < clients.length; i++) {
                    clients[i].run();
                }

                try {
                    Thread.sleep(100);
                }
                catch(Exception e) { e.printStackTrace(); }
            }

        } //No initial database connection, exit
        catch(Exception e) {
            log.error("No connection to database!");
            log.error(e.getMessage());
            e.printStackTrace();
        }

    }

    /** Loads the initial config file
     *
     * @return GroupConfig
     */
    public static GroupConfig file_config() {
        //log.trace("setup config data");

        GroupConfig MAIN_CONFIG;

        try {
            File_Reader read = new File_Reader( "c:/CloudMon/config.json" );//"c:/CloudMon/config.json"
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
            MAIN_CONFIG = null;
            log.error(e.getMessage());
        }

        return MAIN_CONFIG;
    }

}
