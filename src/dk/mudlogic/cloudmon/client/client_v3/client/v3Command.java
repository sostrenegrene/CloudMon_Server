package dk.mudlogic.cloudmon.client.client_v3.client;

import java.util.Hashtable;

/**
 * Created by soren.pedersen on 08-09-2016.
 */
public class v3Command {

    private v3Identifier SID = new v3Identifier();

    private final int CLIENT_ID;
    private final int GROUP_ID;
    private final int COMMAND_ID;

    private int LAST_CHANGED    = 0;
    private int LAST_NOTIFY     = 0;//Notify is used for mail and other notifications

    private Hashtable<String,String> command_table;

    private v3CommandResult RESULT = null;

    public v3Command(Hashtable<String,String> command_table) {

        this.command_table = command_table;

        this.CLIENT_ID = get_int("client_id");
        this.GROUP_ID = get_int("command_group_id");
        this.COMMAND_ID = get_int("id");

        SID.set_ClientID( get_int("client_id") );
        SID.set_GroupID( get_int("command_group_id") );
        SID.set_CommandID( get_int("id") );
        SID.set_Name( get_str("process_name") );

        SID.set_Hashcode(this.hashCode());
    }

    /** Returns a standard identifier for clients, groups and commands
     *
     * @return v3Identifier
     */
    public v3Identifier getIDs() { return SID; }

    public int getClientID() {
        return CLIENT_ID;
    }

    public int getGroupID() {
        return GROUP_ID;
    }

    public int getCommandID() {
        return COMMAND_ID;
    }

    public void last_changed(int time) {
        this.LAST_CHANGED = time;
    }

    public int last_changed() {
        return this.LAST_CHANGED;
    }

    public void last_notify(int time) {
        this.LAST_NOTIFY = time;
    }

    public int last_notify() {
        return this.LAST_NOTIFY;
    }

    public String get_str(String name) {
        return command_table.get(name);
    }

    public int get_int(String name) {
        return Integer.parseInt( get_str(name) );
    }

    public double get_double(String name) {
        return Double.parseDouble( get_str(name) );
    }

    public long get_long(String name) { return Long.parseLong( get_str(name) ); }

    public void update(Hashtable<String,String> command_table) {
        this.command_table = command_table;
    }

    public void setResult(v3CommandResult result) {
        RESULT = result;

        SID.set_Hashcode(this.hashCode());
    }

    public v3CommandResult getResult() {
        return RESULT;
    }

}
