package dk.mudlogic.cloudmon.client.client_v3.client;

import dk.mudlogic.cloudmon.dbstore.changelog.Changelog;
import dk.mudlogic.cloudmon.dbstore.returndata.Returndata;
import dk.mudlogic.tools.callback.CallbackHandler;
import dk.mudlogic.tools.config.GroupConfig;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

import java.util.Hashtable;

/** Client is main container for commands
 * It handles it's list of dk.mudlogic.client_v3.client.CommandGroups.
 * It handles saving of return data from dk.mudlogic.client_v3.process.v3Command_Process
 * Return data is caught in the callback from dk.mudlogic.tools.CallbackHandler
 *
 * Created by soren.pedersen on 09-09-2016.
 */
public class v3Client extends CallbackHandler {

    private LogTracer log = new LogFactory().tracer();

    private v3Identifier SID = new v3Identifier();
    private final int CLIENT_ID;
    private final String CLIENT_NAME;

    private Hashtable<Integer,v3CommandGroup> GROUPS = new Hashtable<>();

    private Changelog CHANGELOG;
    private Returndata RETURN_DATA;

    private GroupConfig config;

    /** Constructor
     *
     * @param id int
     * @param name String
     * @param clog Changelog
     * @param rdata Returndata
     */
    public v3Client(int id,String name,Changelog clog,Returndata rdata) {
        log.setTracerTitle(v3Client.class);

        this.CLIENT_ID = id;
        this.CLIENT_NAME = name;
        SID.set_ClientID(id);
        SID.set_Name(name);

        this.CHANGELOG = clog;
        this.RETURN_DATA = rdata;

        //log.trace(this.CLIENT_NAME);
    }

    public void setConfig(GroupConfig config) {
        this.config = config;
    }

    /** Returns a standard identifier for clients, groups and commands
     *
     * @return v3Identifier
     */
    public v3Identifier getIDs() { return SID; }

    /** Returns name of client
     *
     * @return String
     */
    public String getName() {
        return this.CLIENT_NAME;
    }

    /** Returns id of client
     *
     * @return
     */
    public int getID() {
        return this.CLIENT_ID;
    }

    /** Add a new CommandGroup to client
     *
     * @param group v3CommandGroup
     */
    public void add_Group(v3CommandGroup group) {
        group.set_ClientCallBack( this );
        GROUPS.put(group.getID(),group);
    }

    /** Returns group matching id
     *
     * @param id int
     * @return v3CommandGroup
     */
    public v3CommandGroup getGroup(int id) {
        try {return GROUPS.get(id); }
        catch(Exception e) { return null; }
    }

    /** Executes the client
     * Run through all v3CommandGroup's stored in this client
     *
     */
    public void run() {
        //Get groups as array
        v3CommandGroup[] groups = this.GROUPS.values().toArray(new v3CommandGroup[this.GROUPS.values().size()]);

        //Get next group
        for (int i=0; i<groups.length; i++) {
            v3CommandGroup group = groups[i];

            //Exec group
            group.run(this);
        }

    }

    /** Catches callbacks to this.CallbackHandler
     *
     * @param name String
     * @param obj Object
     */
    @Override
    public void catch_Callback(String name, Object obj) {

        switch(name) {

            case "v3Command":
                //Cast obj to type v3Command
                v3Command command = (v3Command) obj;

                //Debug result data
                //log.trace(command.getResult().getResult());

                //Get string of status bool
                String status = Boolean.toString(command.getResult().hasErrors());

                //Update changelog if command result has changed
                int changelog_type = CHANGELOG.has_changed(command.getCommandID(),
                                                            status,
                                                            command.getResult().getResultHash());

                //If changelog is either new status or changed data on status
                if (changelog_type != Changelog.NO_NEW_STATUS) {

                    //If command has result or error data
                    if ( command.getResult().hasResult() || command.getResult().hasErrors() ) {

                        //Save return data from result
                        RETURN_DATA.save(command.getClientID(),
                                command.getGroupID(),
                                command.getCommandID(),
                                command.getResult().getParsedResult(),
                                command.getResult().getErrorMessages());

                    }

                        //Update changelog status for command
                        CHANGELOG.status(CLIENT_ID,
                                command.getCommandID(),
                                status,
                                command.getResult().getResultHash(), changelog_type);

                    //If changelog is new status, send mail notify
                    if (changelog_type == Changelog.NEW_STATUS) {

                        try {

                            //Send mail notify
                            v3Client_Notify notify = new v3Client_Notify(config.group("mail").get("mail_host").toString(), config.group("mail").get("mail_from").toString());
                            notify.notify_email(command, config.group("mail").get("mail_to").toString() );
                        }
                        catch(Exception e) {
                            log.error( e.getMessage() );
                            e.printStackTrace();
                        }
                    }
                }

                break;

        }

    }
}