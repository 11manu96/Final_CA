package edu.rice.comp504.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.rice.comp504.model.DispatcherAdapter;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Create a web socket for the server.
 */
@WebSocket
public class WebSocketController {

    /**
     * Open user's session.
     *
     * @param user The user whose session is opened.
     */
    @OnWebSocketConnect
    public void onConnect(Session user) {

    }

    /**
     * Send a message.
     *
     * @param user    The session user sending the message.
     * @param message The message to be sent.
     */
    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        DispatcherAdapter dis = ChatAppController.getDispatcher();
        JsonObject jo = new JsonParser().parse(message).getAsJsonObject();
        System.out.println(jo);
        String cmd = jo.get("type").getAsString();

        switch (cmd) {
            case "login":
                dis.loadUser(user, message);
                break;
            case "create":
                dis.loadRoom(user, message);
                break;
            case "join":
                dis.joinRoom(user, message);
                break;
            case "send":
                dis.sendMessage(user, message);
                break;
            case "leave":
                dis.leaveRoom(user, message);
                break;
            case "query":
                dis.query(user, message);
                break;
            case "ack":
                dis.ackMessage(user, message);
                break;
            default:
                break;
        }
    }

    /**
     * Close the user's session.
     *
     * @param user The use whose session is closed.
     */
    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        DispatcherAdapter dis = ChatAppController.getDispatcher();

        int userId = dis.getUserIdFromSession(user);
        dis.unloadUser(userId);
    }
}
