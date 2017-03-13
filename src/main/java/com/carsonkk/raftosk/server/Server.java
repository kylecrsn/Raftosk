package main.java.com.carsonkk.raftosk.server;

import main.java.com.carsonkk.raftosk.global.SysLog;

import java.rmi.RemoteException;

// Top-level server functionality
public class Server {
    //region Private members

    private int serverId;
    private StateMachine stateMachine;
    private HandleRPC serverBinding;

    //endregion

    //region Constructors

    public Server(int serverId) {
        this.serverId = serverId;
        this.stateMachine = new StateMachine(this);
        try {
            serverBinding = new HandleRPC(this);
        }
        catch (RemoteException e) {
            SysLog.logger.severe("An issue occurred while creating the binding to the given RMI address/port: " +
                    e.getMessage());
            //e.printStackTrace();
        }
        SysLog.logger.finer("Created new server with server ID " + this.serverId);
    }

    //endregion

    //region Getters/Setters

    public int getServerId() {
        return this.serverId;
    }

    public StateMachine getStateMachine() {
        return this.stateMachine;
    }

    //endregion

    //region Public Methods

    public void initializeServer() {
        SysLog.logger.fine("Entering method");

        // Register server with RMI, use base port + id as port value
        try {
            this.serverBinding.setupConnection();
            SysLog.logger.info("Successfully bound to the specified RMI address/port");
        }
        catch (RemoteException e) {
            SysLog.logger.severe("An issue occurred while binding to the given RMI address/port: " + e.getMessage());
            //e.printStackTrace();
            SysLog.logger.fine("Exiting method");
            return;
        }

        // Setup state to be a follower
        synchronized (this.stateMachine.getCurrentState()) {
            this.stateMachine.setCurrentState(StateType.FOLLOWER);
            SysLog.logger.info("Set state machine role to FOLLOWER");
        }

        SysLog.logger.fine("Exiting method");
    }

    // Wait for the state machine to finish execution and exit
    public void waitForExitState() {
        SysLog.logger.fine("Entering method");

        while (true) {
            try {
                this.stateMachine.runMachine();
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        //SysLog.logger.fine("Exiting method");
    }

    //endregion
}