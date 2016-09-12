package dk.mudlogic.cloudmon.client_v3.client;

import java.util.Hashtable;

/**
 * Created by soren.pedersen on 08-09-2016.
 */
public class v3Command {

    private final int COMMAND_ID;

    private long THREAD_ID      = 0;
    private int LAST_CHANGED    = 0;
    private int LAST_NOTIFY     = 0;//Notify is used for mail and other notifications

    private Hashtable<String,String> command_table;

    public v3Command(Hashtable<String,String> command_table) {

        this.command_table = command_table;

        this.COMMAND_ID = get_int("id");
    }

    public void thread_id(long id) {
        this.THREAD_ID = id;
    }
    public long thread_id() {
        return THREAD_ID;
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

}
