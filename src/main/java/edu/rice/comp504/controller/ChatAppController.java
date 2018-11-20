package edu.rice.comp504.controller;

import org.eclipse.jetty.websocket.api.Session;

import edu.rice.comp504.model.res.AResponse;
import edu.rice.comp504.model.DispatcherAdapter;

/**
 * The chat app controller communicates with all the clients on the web socket.
 */
public class ChatAppController {

    private static DispatcherAdapter dispatcher = new DispatcherAdapter();

    /**
     * Get the chat app dispatcher.
     * @return the dispatcher of chat app
     */
    public static DispatcherAdapter getDispatcher() {
        return dispatcher;
    }

    /**
     * Chat App entry point.
     * @param args The command line arguments
     */
    public static void main(String[] args) {

    }

    /**
     * Notify session about the message.
     * @param user the session to notify
     * @param response the notification information
     */
    public static void notify(Session user, AResponse response) {

    }

    /**
     * Get the heroku assigned port number.
     * @return The heroku assigned port number
     */
    private static int getHerokuAssignedPort() {
        return 4567;
    }
}
