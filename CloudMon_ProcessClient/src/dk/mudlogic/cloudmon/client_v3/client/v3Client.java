package dk.mudlogic.cloudmon.client_v3.client;

import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

import java.util.Hashtable;

/**
 * Created by soren.pedersen on 09-09-2016.
 */
public class v3Client {

    private LogTracer log = new LogFactory().tracer();

    private final int CLIENT_ID;
    private final String CLIENT_NAME;
    private Hashtable<Integer,v3CommandGroup> GROUPS = new Hashtable<>();;

    public v3Client(int id,String name) {
        this.CLIENT_ID = id;
        this.CLIENT_NAME = name;
    }

    public String getName() {
        return this.CLIENT_NAME;
    }

    public int getID() {
        return this.CLIENT_ID;
    }

    public void add_Group(v3CommandGroup group) {
        GROUPS.put(group.getID(),group);
    }

    public v3CommandGroup getGroup(int id) {
        try {return GROUPS.get(id); }
        catch(Exception e) { return null; }
    }

    public void run() {
        v3CommandGroup[] groups = this.GROUPS.values().toArray(new v3CommandGroup[this.GROUPS.values().size()]);
        log.trace("Loaded " + groups.length + " groups for client " + this.CLIENT_NAME);

        for (int i=0; i<groups.length; i++) {
            v3CommandGroup group = groups[i];

            group.run(this);
        }

    }

    //TODO call method to use in command process
    public void resultCallback() {

    }
}
