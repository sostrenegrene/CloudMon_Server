package dk.mudlogic.cloudmon.client.client_v3.client;

import dk.mudlogic.Main2;
import dk.mudlogic.cloudmon.client.client_v3.process.v3Command_Process;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.callback.*;
import dk.mudlogic.tools.time.TimeHandler;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Hashtable;

/** v3CommandGroup manages commands
 * Handles exec of command processes
 *
 * Created by soren.pedersen on 08-09-2016.
 */
public class v3CommandGroup extends CallbackHandler {

    private LogTracer log = new LogFactory().tracer();

    private v3Identifier SID = new v3Identifier();
    private final int GROUP_ID;
    private final String GROUP_NAME;
    private final int CLIENT_ID;

    private int group_hashcode = 0;

    private ArrayList<v3Command> COMMANDS = new ArrayList<>();
    private Hashtable<Integer,v3Command_Process> COMMAND_THREADS = new Hashtable<>();
    private int commandCount = 0;
    private TimeHandler time = new TimeHandler();

    private CallbackHandler CLIENT_CALLBACK;

    /** Constructor
     *
     * @param id int
     * @param name String
     * @param client_id int
     */
    public v3CommandGroup(int id, String name,int client_id) {
        //super();

        this.GROUP_ID = id;
        this.GROUP_NAME = name;
        this.CLIENT_ID = client_id;

        SID.set_ClientID(client_id);
        SID.set_GroupID(id);
        SID.set_Name(name);

        //log.trace("Loaded " + get_CommandList().length + " commands in " + this.GROUP_NAME);

        updateGroupHashcode();
    }

    public void updateGroupHashcode() {
        //this.group_hashcode = this.hashCode();
        SID.set_Hashcode( this.hashCode() );
    }

    public int getGroupHashcode() {
        //return group_hashcode;
        return SID.get_Hashcode();
    }

    public int commandCount(int i) {
        if (i != 0) { commandCount = i; }

        return commandCount;
    }

    public v3Command getCommand(int id) {

        v3Command out = null;
        v3Command[] c = this.COMMANDS.toArray(new v3Command[this.COMMANDS.size()]);
        for (int i=0; i<c.length; i++) {

            if (c[i].getIDs().get_CommandID() == id) {
                out = c[i];
            }
        }

        return out;
    }

    public void update_CommandList(v3Command[] commands) {

        for (int i=0; i<commands.length; i++) {
            update_Command(commands[i]);
        }

    }

    /** Replaces the cached command with updated command from process
     *
     * @param command v3Command
     */
    private void update_Command(v3Command command) {

        //Get command list
        v3Command[] commands = get_CommandList();

        /*
        //Fetch updated command settings
        v3Command newCommand = Main2.LOAD_CONFIG.getCommand(command.getClientID(),command.getGroupID(),command.getCommandID());
        //Add Result data
        newCommand.setResult( command.getResult() );
        //Update last changed timestamp
        newCommand.last_changed( command.last_changed() );
        */

        //log.info("Updated: " + command.get_str("process_name") + " to: " + newCommand.get_str("process_name")+"-"+newCommand.get_str("check_interval")+"s-on:"+newCommand.get_str("is_active")+"-mail:"+newCommand.get_str("mail_active"));

        //update command to newCommand
        //command = newCommand;
        //}

        //Check if there's any commands
        if (commands.length > 0) {

            //Remove cached command
            remove_Command(command);

            //Add updated command
            this.COMMANDS.add(command);

        }
        //If there's no commands in the list
        else {
            //Add the command
            this.COMMANDS.add(command);
        }

    }

    /** Updates replaces the cached command with updated command from process
     *
     * @param command v3Command
     */
    private void remove_Command(v3Command command) {

        v3Command[] commands = get_CommandList();

        //Get next command
        for (int i=0; i<commands.length; i++) {

            //Check if the command matches
            //if (command.get_int("id") == commands[i].get_int("id")) {
            if (command.getIDs().get_CommandID() == commands[i].getIDs().get_CommandID()) {

                //log.warning("Command remove: " + command.get_str("process_name") + " " + time.unixTimeDiff( command.last_changed() ));
                //remove the command from list
                this.COMMANDS.remove(i);

            }

        }

    }

    public void set_ClientCallBack(CallbackHandler callBack) {
        this.CLIENT_CALLBACK = callBack;
    }

    public boolean is_ready(v3Command command) {

        if ( time.unixTimeDiff( command.last_changed()) >= command.get_int("check_interval") ) {
            //log.warning(command.get_str("process_name") + ": " + time.unixTimeDiff( command.last_changed())+" > " + command.get_int("check_interval"));

            return true;
        }
        else {
            //log.trace(command.get_str("process_name") + ": " +time.unixTimeDiff( command.last_changed())+" < " + command.get_int("check_interval"));

            return false;
        }

    }

    /** Returns a standard identifier for clients, groups and commands
     *
     * @return v3Identifier
     */
    public v3Identifier getIDs() { return SID; }

    /** Returns group name
     *
     * @return Stirng
     */
    public String getName() {
        return this.GROUP_NAME;
    }

    /** Returns group id
     *
     * @return int
     */
    public int getID() {
        return this.GROUP_ID;
    }

    /** Returns id of parent client
     *
     * @return int
     */
    public int getClientID() { return this.CLIENT_ID; }

    /** Add command to group
     *
     * @param command v3Command
     */
    public void add_Command(v3Command command) {
        COMMANDS.add(command);
    }

    /** Get array with v3Command list
     *
     * @return
     */
    public v3Command[] get_CommandList() {
        return this.COMMANDS.toArray(new v3Command[this.COMMANDS.size()]);
    }

    /** Runs the group through all commands
     * to start new processes
     *
     * @param c v3Client | Reference to parent class for callback
     */
    public void run(v3Client c) {
        //Get array with all commands in this group
        v3Command[] commands = get_CommandList(); //this.COMMANDS.toArray(new v3Command[this.COMMANDS.size()]);

        //log.trace( this.getName() + " Run with size: " + commands.length );

        //If there's any commands to exec
        if (commands.length > 0) {

            //Exec all commands in list
            for (int i = 0; i < commands.length; i++) {
                v3Command command = commands[i];

                //Fetch updated command settings
                v3Command newCommand = Main2.LOAD_CONFIG.getCommand(command.getClientID(),command.getGroupID(),command.getCommandID());

                try {
                    //Add Result data
                    newCommand.setResult(command.getResult());

                    //Update last changed timestamp
                    newCommand.last_changed( command.last_changed() );

                    //Update command
                    update_Command(newCommand);
                    //Set command to newCommand
                    command = newCommand;

                    //If command is ready to run
                    if (is_ready(command)) {
                        log.info(command.get_str("process_name")+" ready interval: " + command.get_str("check_interval"));

                        //Remove command from list
                        //It will be added when process finishes and does a callback to this
                        remove_Command( command );

                        //Exec command with callback to this.CallbackHandler
                        COMMAND_THREADS.put(command.getCommandID(), new v3Command_Process(command, this));
                    }

                } catch(Exception e) { }
            }
        }
        else {
            //log.warning(this.GROUP_NAME + " currently has no commands to execute");
        }

    }

    public void killAll() {
        log.warning("Killng all command processes in " + getName());

        v3Command_Process[] v3p = COMMAND_THREADS.values().toArray(new v3Command_Process[COMMAND_THREADS.size()]);
        for (int i=0; i<v3p.length; i++) {
            v3p[i].kill();
        }

    }

    /** Catches callbacks to this.CallbackHandler
     *
     * @param name String
     * @param obj Object
     */
    @Override
    public void catch_Callback(String name,Object obj) {
        name = name.trim();
        //log.trace("Callback: " + name);

        switch(name) {
            case "v3Command":
                v3Command command = (v3Command) obj;
                //log.trace("Returned " + command.get_str("process_name") + " to group " + this.getName());

                //Send to client, to be updated in changelog and return data
                if ( command.getResult() != null) {
                    CLIENT_CALLBACK.callback("v3Command", command);
                }

                try {
                    //Kill the thread if it's still running
                    //Thread should be closed no matter what, when command is finished
                    COMMAND_THREADS.get(command.getCommandID()).kill();
                    //Remove the thread from list
                    COMMAND_THREADS.remove(command.getCommandID());
                }
                catch(Exception e) {}

                update_Command( command );
                break;
        }

    }

}