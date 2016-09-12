package dk.mudlogic;


import dk.mudlogic.cloudmon.client_v2.CloudMon_v2Client;
import dk.mudlogic.cloudmon.config.CloudMon_ConfigThread;
import dk.mudlogic.cloudmon.config.CloudMon_PrepareConfig;
import dk.mudlogic.cloudmon.store.DB_ProcessReturnData;
import dk.mudlogic.mail.MailMan;
import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.time.TimeHandler;

public class Main {

    static LogTracer log = new LogFactory(Main.class.getName()).tracer();

    public static void main(String[] args) {
        log.setTracerTitle(Main.class);

        init_config();
    }

    /** Loads initial config and connects to server database
     *
     */
    public static void init_config() {

        try {
            ServerGlobalData.config_file = "c:/CloudMon/config.json";//args[0];

            try {
                start_config();

                start_server();
            }
            catch(Exception e) {
                e.printStackTrace();

                closeAndExit();
            }

        }
        catch(Exception e) {
            log.error("No config file path! Please run with: java -jar CloudMon_Server.jar [path-to-config-file]");

            closeAndExit();
        }

    }

    public static void start_server() {
        ServerGlobalData.MAILMAN = new MailMan( ServerGlobalData.MAIN_CONFIG.group("mail").get("mail_url").toString() );
        ServerGlobalData.MAILMAN.isActive( ServerGlobalData.MAIN_CONFIG.group("mail").get("send_mail").toBoolean() );

        //Setup database connection
        start_database();

        start_config();

        start_clients();

    }

    public static void start_clients() {
        log.trace("Start clients");
        //Load all processes
        //load_clients();

        //Check if any clients has been loaded
        if (ServerGlobalData.CLIENTS.size() > 0) {
            TimeHandler t = new TimeHandler();
            int now = t.unixTime();

            //Run all clients
            //while (t.unixTimeDiff(now) < (60 * 120)) {
            while (ServerGlobalData.MAIN_CONFIG.group("server").get("running").toBoolean() == true) {

                CloudMon_v2Client[] clients = ServerGlobalData.CLIENTS.toArray(new CloudMon_v2Client[ServerGlobalData.CLIENTS.size()]);
                for (int i = 0; i <clients.length; i++) {
                    //ServerGlobalData.CLIENTS.get(i).start();
                    clients[i].start();
                }

                configLoader();

                try { Thread.sleep( ServerGlobalData.MAIN_CONFIG.group("server").get("server_interval").toInt() ); }
                catch(Exception e) {}
            }

            //Show warning message that server is stopping
            if (ServerGlobalData.MAIN_CONFIG.group("server").get("running").toBoolean() == false) {
                log.warning("Server set not to run in config file. Stopping!");

                //Close database connection and exit
                closeAndExit();
            }

        }
        //If no process was found
        else {
            log.warning("No clients processes was found!");

            //Close database connection and exit
            closeAndExit();
        }

    }

    public static void configLoader() {
        ServerGlobalData.CONFIG_THREAD.getConfig().setup_config();
        ServerGlobalData.MAIN_CONFIG = ServerGlobalData.CONFIG_THREAD.getConfig().getConfig();
        ServerGlobalData.CLIENTS = ServerGlobalData.CONFIG_THREAD.getConfig().getClients();
    }

    public static void start_config() {
        //log.trace("Start config");

        if ( ServerGlobalData.LOCAL_SQL != null) {
            //Setup new config loader and include sql connection
            CloudMon_PrepareConfig pconfig = new CloudMon_PrepareConfig(ServerGlobalData.LOCAL_SQL,ServerGlobalData.config_file);
            ServerGlobalData.CONFIG_THREAD = new CloudMon_ConfigThread(pconfig);

            //new Thread( ServerGlobalData.CONFIG_THREAD ).start();

            log.trace("Start config with database");
        }
        //If there is no sql connection
        else {
            //Setup new config loader with no sql connection
            CloudMon_PrepareConfig pconfig = new CloudMon_PrepareConfig(ServerGlobalData.config_file);
            ServerGlobalData.CONFIG_THREAD = new CloudMon_ConfigThread(pconfig);

            log.trace("Start basic config");
        }

        configLoader();
    }

    public static void start_database() {
        log.trace("Start database");

        //Setup database connection
        ServerGlobalData.LOCAL_SQL = new MSSql(ServerGlobalData.MAIN_CONFIG.group("database").get("url").toString(),
                                                ServerGlobalData.MAIN_CONFIG.group("database").get("username").toString(),
                                                ServerGlobalData.MAIN_CONFIG.group("database").get("password").toString(),
                                                ServerGlobalData.MAIN_CONFIG.group("database").get("database").toString());

        try {
            //Start database connection
            ServerGlobalData.LOCAL_SQL.connect();

            //Setup return data handler for client processes
            ServerGlobalData.PROCESS_RETURN_DATA = new DB_ProcessReturnData(ServerGlobalData.LOCAL_SQL);

            //load_clients();

        } //No initial database connection, exit
        catch(Exception e) {
            log.error("No connection to database! ");
            log.error(e.getMessage());
            //e.printStackTrace();

            closeAndExit();
        }

    }//ENd start_database

    public static void closeAndExit() {
        try { ServerGlobalData.LOCAL_SQL.closeConn(); }
        catch(Exception e) { log.error(e.getMessage()); }

        //TODO find a way to close prepare config thread

        System.exit(0);
    }

}
