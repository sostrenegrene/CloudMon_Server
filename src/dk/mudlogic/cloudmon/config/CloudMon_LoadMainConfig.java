package dk.mudlogic.cloudmon.config;

import dk.mudlogic.Main;
import dk.mudlogic.tools.config.GroupConfig;
import dk.mudlogic.tools.files.File_Reader;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Created by soren.pedersen on 08-09-2016.
 */
public class CloudMon_LoadMainConfig {

    private LogTracer log = new LogFactory().tracer();

    private String CONFIG_FILE;
    private GroupConfig CONFIG;

    public CloudMon_LoadMainConfig(String config_file) {
        log.setTracerTitle(CloudMon_PrepareConfig.class);

        this.CONFIG_FILE = config_file;

        load();
    }

    private void load() {
        CONFIG = setup_config();
    }

    public GroupConfig get_Config() {
        load();

        return CONFIG;
    }

    private GroupConfig setup_config() {
        //log.trace("setup config data");

        GroupConfig config;
        try {
            File_Reader read = new File_Reader( this.CONFIG_FILE );//"c:/CloudMon/config.json"
            JSONObject jobj = (JSONObject) new JSONParser().parse( read.readfile() );

            config = new GroupConfig("Config_"+Main.class.getSimpleName());

            JSONObject db = (JSONObject) jobj.get("database");
            JSONObject server = (JSONObject) jobj.get("server");
            JSONObject mail = (JSONObject) jobj.get("mail");

            String[] db_keys = (String[]) db.keySet().toArray(new String[db.keySet().size()]);
            String[] db_values = (String[]) db.values().toArray(new String[db.values().size()]);
            config.generate_group("database",db_keys,db_values);

            String[] server_keys = (String[]) server.keySet().toArray(new String[server.keySet().size()]);
            String[] server_values = (String[]) server.values().toArray(new String[server.values().size()]);
            config.generate_group("server",server_keys,server_values);

            String[] mail_keys = (String[]) mail.keySet().toArray(new String[mail.keySet().size()]);
            String[] mail_values = (String[]) mail.values().toArray(new String[mail.values().size()]);
            config.generate_group("mail",mail_keys,mail_values);
        }
        catch(Exception e) {
            log.error(e.getMessage());

            config = null;
        }

        return config;
    }

}
