package dk.mudlogic.cloudmon.client_v2;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by soren.pedersen on 09-07-2016.
 *
 * Holds config values for client processes
 */
public class v2ProcessConfig {

    private int client_id;
    private String client_name;
    private String client_url;
    private String group_name;

    private ArrayList<v2ProcessCommand> commands = new ArrayList<>();

    private v2ProcessCommand[] output_list;
    private int list_iterator = 0;

    public v2ProcessConfig(int client_id,String client_name, String client_url, String group_name) {
        this.client_id = client_id;
        this.client_name = client_name;
        this.client_url = client_url;
        this.group_name = group_name;
    }

    public void add_command(Hashtable h) {
        //Set updated timestamp and thread id
        //on every command to 0 when adding them

        v2ProcessCommand v2p = new v2ProcessCommand(this.commands.size(),h);

        this.commands.add(v2p);

    }

    public int getClientID() {
        return this.client_id;
    }

    public String getClientName() {
        return this.client_name;
    }

    public String getClientURL() {
        return this.client_url;
    }

    public String getGroupName() {
        return this.group_name;
    }

    /** Return all command tables as an array
     *
     * @return v2ProcessCommand[]
     */
    public v2ProcessCommand[] getCommands() {
        return commands.toArray(new v2ProcessCommand[commands.size()]);
    }

    /** Checks if the iterator has new item
     * if load next item
     * if not reset list and load next item
     */
    public v2ProcessCommand getNextItem() {
        v2ProcessCommand out;

        if ( list_iterator < commands.size() ) {
            out = commands.get(list_iterator);
            list_iterator++;
        }
        else {
            list_iterator = 0;
            out = commands.get(list_iterator);
        }

        return out;
    }

    /** Update the row from the current iterator position
     *
     * @param command v2ProcessCommand
     */
    public void update_row(v2ProcessCommand command) {
        commands.set(command.getProcessID(),command);
    }

}
