package dk.mudlogic.cloudmon.client_v2;

import java.util.Hashtable;

/**
 * Created by soren.pedersen on 12-07-2016.
 */
public class v2ProcessCommand {

    private final int pID;

    private final int client_id;
    private final int group_id;
    private final int command_id;

    private long thread_id = 0;
    private int last_changed = 0;

    private Hashtable<String,String> command_table;

    public v2ProcessCommand(int process_id,Hashtable<String,String> command_table) {
        this.pID = process_id;
        this.command_table = command_table;

        this.client_id  = get_int("client_id");
        this.group_id   = get_int("command_group_id");
        this.command_id = get_int("id");
    }

    public int getProcessID() {
        return pID;
    }

    public int getClientID() {
        return client_id;
    }

    public int getGroupID() {
        return group_id;
    }

    public int getCommandID() {
        return command_id;
    }

    public long threadID() {
        return thread_id;
    }

    public void threadID(long id) {
        thread_id = id;
    }

    public int lastChanged() { return last_changed; }

    public void lastChanged(int time) {
        last_changed = time;
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
}
