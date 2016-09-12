
import dk.mudlogic.Main;

import dk.mudlogic.cloudmon.client_v3.client.v3Client;
import dk.mudlogic.cloudmon.config.CloudMon_LoadClientConfig;
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

    public static void main(String[] args) {

        GroupConfig MAIN_CONFIG = setup_config();

        //Setup database connection
        MSSql sql = new MSSql(MAIN_CONFIG.group("database").get("url").toString(),
                              MAIN_CONFIG.group("database").get("username").toString(),
                              MAIN_CONFIG.group("database").get("password").toString(),
                              MAIN_CONFIG.group("database").get("database").toString());

        try {
            //Start database connection
            sql.connect();

            CloudMon_LoadClientConfig loadClient = new CloudMon_LoadClientConfig(sql);
            v3Client[] clients = loadClient.get_Clients();
            log.trace("Loaded " + clients.length + " clients");
            clients[0].run();
        } //No initial database connection, exit
        catch(Exception e) {
            log.error("No connection to database! ");
            log.error(e.getMessage());
            e.printStackTrace();
        }


    }


    public static GroupConfig setup_config() {
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
