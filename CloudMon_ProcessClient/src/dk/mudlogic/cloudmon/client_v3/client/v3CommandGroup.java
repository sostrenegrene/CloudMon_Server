package dk.mudlogic.cloudmon.client_v3.client;

import dk.mudlogic.cloudmon.client_v3.process.v3Command_Process;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.callback.*;
import dk.mudlogic.tools.time.TimeHandler;

import java.util.AbstractList;
import java.util.ArrayList;

/** v3CommandGroup manages commands
 * Handles exec of command processes
 *
 * Created by soren.pedersen on 08-09-2016.
 */
public class v3CommandGroup extends CallbackHandler {

    private LogTracer log = new LogFactory().tracer();

    private final int GROUP_ID;
    private final String GROUP_NAME;
    private final int CLIENT_ID;

    private AbstractList<v3Command> COMMANDS = new ArrayList<>();
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

        //log.trace("Loaded " + get_CommandList().length + " commands in " + this.GROUP_NAME);
    }

    /** Replaces the cached command with updated command from process
     *
     * @param command v3Command
     */
    private void update_Command(v3Command command) {

        //Get command list
        v3Command[] commands = get_CommandList();

        //Check if there's any commands
        if (commands.length > 0) {

            //Get next command
            for (int i = 0; i < commands.length; i++) {

                //If command matches
                if (command.get_int("id") == commands[i].get_int("id")) {
                    //log.trace("Command update: " + command.get_str("process_name") + " " + time.unixTimeDiff(command.last_changed()));

                    //Remove cached command
                    remove_Command(command);

                    //Add updated command
                    this.COMMANDS.add(command);
                }

            }

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
            if (command.get_int("id") == commands[i].get_int("id")) {

                //log.trace("Command update: " + command.get_str("process_name") + " " + time.unixTimeDiff( command.last_changed() ));
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
            //log.trace(time.unixTimeDiff( command.last_changed())+" > " + command.get_int("check_interval"));

            return true;
        }
        else {
            //log.trace(time.unixTimeDiff( command.last_changed())+" < " + command.get_int("check_interval"));

            return false;
        }

    }

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

        //If there's any commands to exec
        if (commands.length > 0) {

            //Exec all commands in list
            for (int i = 0; i < commands.length; i++) {
                v3Command command = commands[i];

                //If command is ready to run
                if (is_ready(command)) {

                    //Remove command from list
                    //It will be added when process finishes and does a callback to this
                    remove_Command( command );

                    //Exec command with callback to this.CallbackHandler
                    new v3Command_Process(command, this);
                }
            }
        }
        else {
            //log.warning(this.GROUP_NAME + " currently has no commands to execute");
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

                update_Command( command );

                if ( command.getResult() != null) {
                    CLIENT_CALLBACK.callback("v3Command", command);
                }
                break;
        }

    }

}
