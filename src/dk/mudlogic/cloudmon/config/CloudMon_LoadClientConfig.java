package dk.mudlogic.cloudmon.config;

import dk.mudlogic.cloudmon.client_v3.client.v3Client;
import dk.mudlogic.cloudmon.client_v3.client.v3CommandGroup;
import dk.mudlogic.cloudmon.client_v3.client.v3Command;
import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.database.SQLResult;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by soren.pedersen on 08-09-2016.
 */
public class CloudMon_LoadClientConfig {

    private LogTracer log = new LogFactory().tracer();

    private MSSql SQL;
    private SQLResult result;

    public CloudMon_LoadClientConfig(MSSql sql) {
        log.setTracerTitle(CloudMon_PrepareConfig.class);

        this.SQL = sql;

        load();
        //setup_clients();
    }

    public v3Client[] get_Clients() {
        return make_Clients();
    }

    private void load() {
        try {
            //Run query and get result
            result = SQL.query(query_commands());

        }
        catch(SQLException e) {
            log.strbuild_add(LogTracer.ERROR,e.getMessage());
            //e.printStackTrace();
        }

    }

    private v3Client[] make_Clients() {
        //Get rows from sql result
        ArrayList rows = result.getRows();

        //Generate clients
        Hashtable<Integer,v3Client> clients = new Hashtable<>();

        //Generate clients
        for (int i=0; i<rows.size(); i++) {
            Hashtable client_table = (Hashtable) rows.get(i);

            //Get id, name, group name
            int client_id       = Integer.parseInt((String) client_table.get("client_id"));
            String client_name  = (String) client_table.get("client_name");
            //Make new client
            v3Client client     = new v3Client(client_id,client_name);

            //Check that the client does not exist
            if ( !clients.containsKey( client.getID() ) ) {
                log.trace("Added Client: " + client.getName());

                //Add command groups to client
                client = add_Groups(client,rows);

                //Save client
                clients.put(client.getID(),client);
            }
        }

        //Return v3Client[] array
        return clients.values().toArray(new v3Client[clients.values().size()]);
    }

    /** Adds command groups to a client
     *
     * @param client v3Client
     * @param rows ArrayList< Hashtable<String,Object> >
     * @return v3Client
     */
    private v3Client add_Groups(v3Client client,ArrayList<Hashtable> rows) {
        Hashtable[] groups = rows.toArray(new Hashtable[rows.size()]);

        //Generate groups
        for (int i=0; i<groups.length; i++) {
            Hashtable<String,Object> group_table = groups[i];

            //Get id, name, group name and client id
            int client_id = Integer.parseInt( (String)group_table.get("client_id") );
            int group_id = Integer.parseInt( (String)group_table.get("command_group_id") );
            String group_name = (String) group_table.get("group_name");

            //Make new group
            v3CommandGroup group = new v3CommandGroup(group_id, group_name,client_id);

            //If clients id matches groups client id and group does not exist
            if ( (group.getClientID() == client.getID()) && (client.getGroup(group.getID()) == null) ) {
                log.trace("Adding group "+group.getName()+" to "+client.getName() );

                //Add commands to group
                group = add_Commands(group,rows);

                //Save group in client
                client.add_Group(group);
            }

        }

        //Return client
        return client;
    }

    private v3CommandGroup add_Commands(v3CommandGroup group, ArrayList<Hashtable> rows) {
        //Generate groups
        for (int i=0; i<rows.size(); i++) {
            Hashtable command_table = rows.get(i);

            int groupID = Integer.parseInt( (String)command_table.get("command_group_id") );
            if ( groupID == group.getID() ) {
                group.add_Command(new v3Command(command_table));
            }

        }

        return group;
    }

    /** Query string
     *
     * @return String | Query
     */
    private String query_commands() {
        String s = "SELECT cpc.*,ccg.group_name AS group_name,ccg.id AS group_id,clients.client_name,clients.id AS client_id,clients.url AS client_url " +
                "FROM cloudmon_process_commands AS cpc " +
                "JOIN cloudmon_process_command_groups AS ccg ON ccg.id = cpc.command_group_id " +
                "JOIN cloudmon_process_clients AS clients ON ccg.client_id = clients.id " +
                "WHERE is_active = 1";

        return s;
    }

    /** Loads command list from database
     * Generates array of process clients
     *
     */
    /*
    public void setup_clients() {
        //log.trace("setup client data");

        try {
            //Load client command groups
            COMMAND_GROUPS = new Load_v2ProcessConfig(this.SQL).getConfig();

            ArrayList<CloudMon_v2Client> tmp = new ArrayList<>();
            //Create client process list
            for (int i = 0; i< COMMAND_GROUPS.length; i++) {
                CloudMon_v2Client client = new CloudMon_v2Client(this.MAIN_CONFIG, new DB_ProcessReturnData(this.SQL), COMMAND_GROUPS[i] );
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
    */

}
