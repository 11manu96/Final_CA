package edu.rice.comp504.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.rice.comp504.model.cmd.LeaveRoomCmd;
import org.eclipse.jetty.websocket.api.Session;

import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.Message;
import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.res.*;

public class DispatcherAdapter extends Observable {

    private int nextUserId;
    private int nextRoomId;
    private int nextMessageId;

    // Maps user id to the user
    private Map<Integer, User> users;

    // Maps room id to the chat room
    private Map<Integer, ChatRoom> rooms;

    // Maps message id to the message
    private Map<Integer, Message> messages;

    // Maps session to user id
    private Map<Session, Integer> userIdFromSession;

    /**
     * Constructor, initializing all private fields.
     */
    public DispatcherAdapter() {
        this.nextRoomId = 0;
        this.nextUserId = 0;
        this.nextMessageId = 0;
        this.users = new ConcurrentHashMap();
        this.rooms = new ConcurrentHashMap();
        this.messages = new ConcurrentHashMap();
        this.userIdFromSession = new ConcurrentHashMap();
    }

    /**
     * Allocate a user id for a new session.
     * @param session the new session
     */
    public void newSession(Session session) {

    }

    /**
     * Get the user if from a session.
     * @param session the session
     * @return the user id binding with session
     */
    public int getUserIdFromSession(Session session) {
        return this.userIdFromSession.get(session);
    }

    /**
     * Determine whether the session exists.
     * @param session the session
     * @return whether the session is still connected or not
     */
    public boolean containsSession(Session session) {
        return this.userIdFromSession.containsKey(session);
    }

    /**
     * Load a user into the environment.
     * @param session the session that requests to called the method
     * @param body of format "name age location school"
     * @return the new user that has been loaded
     */
    public User loadUser(Session session, String body) {
        return null;
    }

    /**
     * Load a room into the environment.
     * @param session the session that requests to called the method
     * @param body of format "name ageLower ageUpper {[location],}*{[location]} {[school],}*{[school]}"
     * @return the new room that has been loaded
     */
    public ChatRoom loadRoom(Session session, String body) {
        // parse the body
        JsonObject jo = new JsonParser().parse(body).getAsJsonObject().getAsJsonObject("body");
        String roomName = jo.get("roomName").getAsString();
        int ageLower = jo.get("ageLower").getAsInt();
        int ageUpper = jo.get("ageUpper").getAsInt();
        String[] locations = jo.get("location").getAsString().split(",");
        String[] schools = jo.get("school").getAsString().split(",");
        // get owner
        int ownerId = getUserIdFromSession(session);
        User owner = this.users.get(ownerId);
        // create chatroom
        ChatRoom newRoom = new ChatRoom(this.nextRoomId, roomName, owner,
                ageLower, ageUpper, locations, schools, this);
        // check if the owner is valid to join the room
        boolean ownerValid = newRoom.applyFilter(owner);
        if (ownerValid) {
            owner.addRoom(newRoom);
            owner.moveToJoined(newRoom);
            this.rooms.put(nextRoomId, newRoom);
            // create response
            NewRoomResponse newRoomResponse = new NewRoomResponse("NewRoom", nextRoomId, roomName, ownerId);
            // notify the owner
            notifyClient(owner, newRoomResponse);
            // increase nextRoomId
            nextRoomId++;
            return newRoom;
        } else {
            // TODO: notify the owner he is invalid
            return null;
        }
    }

    /**
     * Remove a user with given userId from the environment.
     * @param userId the id of the user to be removed
     */
    public void unloadUser(int userId) {

    }

    /**
     * Remove a room with given roomId from the environment.
     * @param roomId the id of the chat room to be removed
     */
    public void unloadRoom(int roomId) {

    }

    /**
     * Make a user join a chat room.
     * @param session the session that requests to called the method
     * @param body of format "roomId"
     */
    public void joinRoom(Session session, String body) {

    }

    /**
     * Make a user volunteer to leave a chat room.
     * @param session the session that requests to called the method
     * @param body of format "roomId"
     */
    public void leaveRoom(Session session, String body) {
        //parsebody
        JsonObject jo = new JsonParser().parse(body).getAsJsonObject().getAsJsonObject(body);

        //getroom
        int roomId = jo.get("roomId").getAsInt();
        ChatRoom chatRoom = this.rooms.get(roomId);

        //getuser
        User user = this.users.get(userIdFromSession.get(session));

        //leaveroom
        setChanged();
        notifyObservers(new LeaveRoomCmd(chatRoom, user));

        //notification response
        RoomNotificationResponse roomNotificationResponse = new RoomNotificationResponse("RoomNotifications", chatRoom.getNotifications());
        notifyClient(user, roomNotificationResponse);

        //userrooomlist response
        UserRoomResponse userRoomResponse = new UserRoomResponse("UserRooms", userIdFromSession.get(session), user.getJoinedRoomIds(), user.getAvailableRoomIds());
        notifyClient(user, userRoomResponse);

        //roomuserlist response
        RoomUsersResponse roomUsersResponse = new RoomUsersResponse("RoomUsers", chatRoom.getId(), chatRoom.getUsers());
        notifyClient(user, roomUsersResponse);




    }

    /**
     * Make modification on chat room filer by the owner.
     * @param session the session of the chat room owner
     * @param body of format "roomId lower upper {[location],}*{[location]} {[school],}*{[school]}"
     */
    public void modifyRoom(Session session, String body) {

    }

    /**
     * A sender sends a string message to a receiver.
     * @param session the session of the message sender
     * @param body of format "roomId receiverId rawMessage"
     */
    public void sendMessage(Session session, String body) {
        //parse the body
        JsonObject jo = new JsonParser().parse(body).getAsJsonObject().getAsJsonObject("body");
        int roomId = jo.get("roomId").getAsInt();
        String rawMessage = jo.get("message").getAsString();

        //get the sender
        int senderId = userIdFromSession.get(session);
        User sender = users.get(senderId);

        //get the charRoom
        ChatRoom chatRoom = rooms.get(roomId);

        //get receivers
        User owner = chatRoom.getOwner();
        Map<Integer, String> receivers = new HashMap<>();

        if (sender == owner && jo.get("receiverId").getAsString().equals("All")) {
                receivers = chatRoom.getUsers();

        } else {
            int receiverId = jo.get("receiverId").getAsInt();
            receivers.put(receiverId, users.get(receiverId).getName());
        }

        //send message
        for (Map.Entry<Integer, String> entry : receivers.entrySet()) {

            //get the receiver
            int receiverId = entry.getKey();
            User receiver = users.get(receiverId);

            if (receiver == sender)//when the owner sends to all users, won't send to himself
                continue;

            //construct the message
            Message message = new Message(nextMessageId, roomId, senderId, receiverId, rawMessage);
            nextMessageId++;

            //store the message
            chatRoom.storeMessage(sender, receiver, message);

            //get the chatHistory
            List<Message> chatHistory = getChatHistory(roomId, senderId, receiverId);
            chatHistory.add(message);

            //response
            UserChatHistoryResponse userChatHistoryResponse = new UserChatHistoryResponse("DispatcherAdatpter", chatHistory);
            notifyClient(receiver, userChatHistoryResponse);

        }
    }

    /**
     * Acknowledge the message from the receiver.
     * @param session the session of the message receiver
     * @param body of format "msgId"
     */
    public void ackMessage(Session session, String body) {

    }

    /**
     * Send query result from controller to front end.
     * @param session the session that requests to called the method
     * @param body of format "type roomId [senderId] [receiverId]"
     */
    public void query(Session session, String body) {

    }

    /**
     * Notify the client for refreshing.
     * @param user user expected to receive the notification
     * @param response the information for notifying
     */
    public static void notifyClient(User user, AResponse response) {

        notifyClient(user.getSession(), response);

    }


    /**
     * Notify session about the message.
     * @param session the session to notify
     * @param response the notification information
     */
    public static void notifyClient(Session session, AResponse response) {
        try {
            session.getRemote().sendString(String.valueOf(response.toJson()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Get the names of all chat room members.
     * @param roomId the id of the chat room
     * @return all chat room members, mapping from user id to user name
     */
    private Map<Integer, String> getUsers(int roomId) {
        return null;
    }

    /**
     * Get notifications in the chat room.
     * @param roomId the id of the chat room
     * @return notifications of the chat room
     */
    private List<String> getNotifications(int roomId) {
        return null;
    }

    /**
     * Get chat history between user A and user B (commutative).
     * @param roomId the id of the chat room
     * @param userAId the id of user A
     * @param userBId the id of user B
     * @return chat history between user A and user B at a chat room
     */
    private List<Message> getChatHistory(int roomId, int userAId, int userBId) {
        //get all chatHistory
        ChatRoom chatRoom = rooms.get(roomId);
        Map<String, List<Message>> chatHistory = chatRoom.getChatHistory();

        //combine two users
        String combineId = userAId < userBId ?
                String.valueOf(userAId) + String.valueOf(userBId) : String.valueOf(userBId) + String.valueOf(userAId);

        //filter the chatHistory
        for (Map.Entry<String, List<Message>> entry : chatHistory.entrySet()) {
            if (entry.getKey().equals(combineId))
                return entry.getValue();

        }

        return null;
    }
}
