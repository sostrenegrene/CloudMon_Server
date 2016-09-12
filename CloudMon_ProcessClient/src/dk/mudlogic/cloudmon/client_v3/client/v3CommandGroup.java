package dk.mudlogic.cloudmon.client_v3.client;

import dk.mudlogic.cloudmon.client_v3.process.v3Command_Process;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

import java.util.AbstractList;
import java.util.ArrayList;

/**
 * Created by soren.pedersen on 08-09-2016.
 */
public class v3CommandGroup {

    private LogTracer log = new LogFactory().tracer();

    private final int GROUP_ID;
    private final String GROUP_NAME;
    private final int CLIENT_ID;

    private AbstractList<v3Command> COMMANDS = new ArrayList<>();

    public v3CommandGroup(int id, String name,int client_id) {
        this.GROUP_ID = id;
        this.GROUP_NAME = name;
        this.CLIENT_ID = client_id;
    }

    public String getName() {
        return this.GROUP_NAME;
    }

    public int getID() {
        return this.GROUP_ID;
    }

    public int getClientID() { return this.CLIENT_ID; }

    public void add_Command(v3Command command) {
        COMMANDS.add(command);
    }

    public v3Command[] get_CommandList() {
        return this.COMMANDS.toArray(new v3Command[this.COMMANDS.size()]);
    }

    /** Runs the group through all commands
     * to start new processes
     *
     * @param c v3Client | Reference to parent class for callback
     */
    public void run(v3Client c) {
        v3Command[] commands = this.COMMANDS.toArray(new v3Command[this.COMMANDS.size()]);
        log.trace("Loaded " + commands.length + " commands in group " + this.GROUP_NAME + " in client " + c.getName());

        for (int i=0; i<commands.length; i++) {
            v3Command command = commands[i];

            //TODO exec process
            new v3Command_Process(c,command);
        }

    }


}
