package dk.mudlogic.cloudmon.v3config;

import dk.mudlogic.cloudmon.client.client_v3.client.v3Command;
import dk.mudlogic.cloudmon.dbstore.changelog.Changelog;
import dk.mudlogic.cloudmon.client.client_v3.client.v3Client;
import dk.mudlogic.cloudmon.client.client_v3.client.v3CommandGroup;
import dk.mudlogic.cloudmon.dbstore.returndata.Returndata;
import dk.mudlogic.tools.config.GroupConfig;
import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.time.TimeHandler;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by soren.pedersen on 08-09-2016.
 */
public class CloudMon_LoadClientConfig {

    private LogTracer log = new LogFactory().tracer();

    private MSSql SQL;

    private LoadCommandConfig cload;
    private Hashtable<Integer,v3Client> CLIENTS;

    private GroupConfig CONFIG;

    private int client_count = 0;
    private int group_count = 0;
    private int command_count = 0;

    public CloudMon_LoadClientConfig(MSSql sql,GroupConfig config) {
        log.setTracerTitle(CloudMon_LoadClientConfig.class);

        CONFIG = config;

        this.SQL = sql;

        cload = new LoadCommandConfig(SQL);

        CLIENTS = build_Clients();

        new Thread( new RebuildThread(this) ).start();
    }

    public v3Client get_Client(int id) {
        if (CLIENTS.containsKey(id)) {
            return CLIENTS.get(id);
        }
        else {
            return null;
        }
    }

    public v3CommandGroup getCommandGroup(int client_id,int group_id) {

        v3CommandGroup out = null;

        if (CLIENTS.containsKey(client_id)) {
            v3Client c = CLIENTS.get(client_id);

            out = c.getGroup(group_id);
        }

        return out;
    }

    public v3Command getCommand(int client_id,int group_id,int command_id) {

        v3Command out = null;

        if (CLIENTS.containsKey(client_id)) {
            v3Client c = CLIENTS.get(client_id);

            if (c.getGroup(group_id) != null) {

                v3CommandGroup g = c.getGroup(group_id);
                out = g.getCommand(command_id);

            }
        }

        return out;
    }

    public v3Client[] toArray() {
        return CLIENTS.values().toArray(new v3Client[ CLIENTS.size() ] );
    }

    public Hashtable<Integer,v3Client> getClients() {
        return CLIENTS;
    }

    public void rebuild() {
        CLIENTS = build_Clients();
    }

    private Hashtable<Integer,v3Client> build_Clients() {
        log.trace("Building clients");
        client_count = 0;
        group_count = 0;
        command_count = 0;


        //Get rows from sql result
        //ArrayList rows = result.getRows();

        cload.load();
        ArrayList rows = cload.getRows();

        //Generate clients
        Hashtable<Integer,v3Client> clients = new Hashtable<>();

        //Generate clients
        for (int i=0; i<rows.size(); i++) {
            Hashtable client_table = (Hashtable) rows.get(i);

            //Get id, name, group name
            int client_id       = Integer.parseInt((String) client_table.get("client_id"));
            String client_name  = (String) client_table.get("client_name");

            //Setup changelog and returndata handlers
            Changelog clog = new Changelog(SQL);
            Returndata rdata = new Returndata(SQL);

            //Make new client
            v3Client client     = new v3Client(client_id,client_name,clog,rdata);
            client.setConfig(CONFIG);

            //Check that the client does not exist
            if ( !clients.containsKey( client.getID() ) ) {
                //log.trace("Added Client: " + client.getName());

                //Add command groups to client
                client = add_Groups(client,rows);

                //Save client
                clients.put(client.getID(),client);
            }
        }

        client_count = clients.size();

        log.trace("Clients: " + client_count + " Groups: " + group_count + " Commands: " + command_count);

        //Return v3Client[] array
        return clients;
    }

    private v3Client[] make_Clients() {
        log.trace("Building config");

        //Get rows from sql result
        //ArrayList rows = result.getRows();

        cload.load();
        ArrayList rows = cload.getRows();

        //Generate clients
        Hashtable<Integer,v3Client> clients = new Hashtable<>();

        //Generate clients
        for (int i=0; i<rows.size(); i++) {
            Hashtable client_table = (Hashtable) rows.get(i);

            //Get id, name, group name
            int client_id       = Integer.parseInt((String) client_table.get("client_id"));
            String client_name  = (String) client_table.get("client_name");

            //Setup changelog and returndata handlers
            Changelog clog = new Changelog(SQL);
            Returndata rdata = new Returndata(SQL);

            //Make new client
            v3Client client     = new v3Client(client_id,client_name,clog,rdata);

            //Check that the client does not exist
            if ( !clients.containsKey( client.getID() ) ) {
                log.trace("Added Client: " + client.getName());

                //Add command groups to client
                client = add_Groups(client,rows);

                //Save client
                clients.put(client.getID(),client);
            }
        }

        log.trace("Done");

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
                //log.trace("Adding group "+group.getName()+" to "+client.getName() );

                //Add commands to group
                //group = add_Commands(group,rows);
                group = cload.add_Commands(group);
                group.commandCount( group.get_CommandList().length );

                //log.trace("Loaded " + group.get_CommandList().length + " commands in " + group.getName());

                //Save group in client
                client.add_Group(group);

                //Count number of groups and commands
                group_count++;
                command_count += group.get_CommandList().length;
            }

        }

        //Return client
        return client;
    }


    class RebuildThread implements Runnable {

        TimeHandler time = new TimeHandler();
        final int reload_time = 10 * 1000;

        CloudMon_LoadClientConfig LCC;

        public RebuildThread(CloudMon_LoadClientConfig lcc) {
            this.LCC = lcc;
        }

        @Override
        public void run() {

            while(true) {
                sleep(reload_time);

                LCC.rebuild();
            }

        }

        public CloudMon_LoadClientConfig get() {
            return LCC;
        }

        private void sleep(int time) {
            try { Thread.sleep(time); }
            catch (Exception e) {}
        }
    }

}
