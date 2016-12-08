package dk.mudlogic.cloudmon.v3config;

import dk.mudlogic.cloudmon.client.client_v3.client.v3Client;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

import java.util.ArrayList;
import java.util.Hashtable;

/** Handles the differences in newly updated clients
 * And the current client list
 * No handling of groups or commands, only adds or removes any changes in clients
 *
 * Created by soren.pedersen on 08-12-2016.
 */
public class ClientUpdater {

    private LogTracer log = new LogFactory().tracer();

    //Newly loaded client list
    private Hashtable<Integer,v3Client> UPDATED_CLIENTS;
    //Currently loaded clients list
    private v3Client[] CURRENT_CLIENTS;
    //Final client list
    private ArrayList<v3Client> CLIENTS = new ArrayList<>();

    public ClientUpdater(Hashtable updated_clients,v3Client[] current_clients) {
        log.setTracerTitle(ClientUpdater.class);

        UPDATED_CLIENTS = updated_clients;
        CURRENT_CLIENTS = current_clients;

        add_Clients();
        remove_Clients();
    }

    public v3Client[] getClients() {
        return CLIENTS.toArray(new v3Client[CLIENTS.size()]);
    }

    /** Finds client that exist in the updated client list
     * but not in the current list and adds them
     *
     */
    private void add_Clients() {
       int added = 0;
        v3Client[] ulist = UPDATED_CLIENTS.values().toArray(new v3Client[UPDATED_CLIENTS.size()]);

        //Get next updated client
        for (v3Client uc : ulist) {
            boolean has_client = false;

            for (v3Client cc : CURRENT_CLIENTS) {

                if (cc.getID() == uc.getID()) { has_client = true; }

            }

            //If updated client does not exist, add it
            if (!has_client) {
                CLIENTS.add(uc);
                added++;
            }
        }

        if (added > 0) { log.trace("Added " + added + " new Clients to runtime"); }
    }

    private void remove_Clients() {
        int removed = 0;

            for (v3Client cc : CURRENT_CLIENTS) {

                if (UPDATED_CLIENTS.containsKey(cc.getID())) {
                    CLIENTS.add(cc);
                }
                else { removed++; }

            }

        if (removed > 0) { log.trace("Removed " + removed + " Clients from runtime"); }
    }

}
