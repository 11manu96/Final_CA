package edu.rice.comp504.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
        this.users = new ConcurrentHashMap<>();
        this.rooms = new ConcurrentHashMap<>();
        this.messages = new ConcurrentHashMap<>();
        this.userIdFromSession = new ConcurrentHashMap<>();
    }

    /**
     * Allocate a user id for a new session.
     *
     * @param session the new session
     */
    public void newSession(Session session) {
        userIdFromSession.put(session, nextUserId);
        nextUserId++;
    }

    /**
     * Get the user if from a session.
     *
     * @param session the session
     * @return the user id binding with session
     */
    public int getUserIdFromSession(Session session) {
        return this.userIdFromSession.get(session);
    }

    /**
     * Determine whether the session exists.
     *
     * @param session the session
     * @return whether the session is still connected or not
     */
    public boolean containsSession(Session session) {
        return this.userIdFromSession.containsKey(session);
    }

    /**
     * Load a user into the environment.
     *
     * @param session the session that requests to called the method
     * @param body    of format "name age location school"
     * @return the new user that has been loaded
     */
    public User loadUser(Session session, String body) {
        // parse body
        JsonObject jo = new JsonParser().parse(body).getAsJsonObject().getAsJsonObject("body");
        String name = jo.get("name").getAsString();
        int age = jo.get("age").getAsInt();
        String location = jo.get("location").getAsString();
        String school = jo.get("school").getAsString();

        // create user
        newSession(session);
        User newUser = new User(getUserIdFromSession(session), session, name, age, location, school, null);

        // check rooms available to user
        for (ChatRoom room : rooms.values()) {
            if (room.applyFilter(newUser)) {
                newUser.addRoom(room);
            }
        }

        // add user object to lookup
        users.put(nextUserId, newUser);

        // send responses to new user
        NewUserResponse newUserResponse = new NewUserResponse(nextUserId, name);
        notifyClient(newUser, newUserResponse);
        UserRoomResponse userRoomResponse = new UserRoomResponse(nextUserId, newUser.getJoinedRoomIds(),
                newUser.getAvailableRoomIds());
        notifyClient(newUser, userRoomResponse);

        nextUserId++;
        return newUser;
    }

    /**
     * Load a room into the environment.
     *
     * @param session the session that requests to called the method
     * @param body    of format "name ageLower ageUpper {[location],}*{[location]} {[school],}*{[school]}"
     * @return the new room that has been loaded
     */
    public ChatRoom loadRoom(Session session, String body) {
        // parse body
        JsonObject jo = new JsonParser().parse(body).getAsJsonObject().getAsJsonObject("body");
        String roomName = jo.get("roomName").getAsString();
        int ageLower = jo.get("ageLower").getAsInt();
        int ageUpper = jo.get("ageUpper").getAsInt();
        String[] locations = new String[((JsonArray) jo.get("location")).size()];
        String[] schools = new String[((JsonArray) jo.get("school")).size()];
        for (int i = 0; i < ((JsonArray) jo.get("location")).size(); i++) {
            locations[i] = ((JsonArray) jo.get("location")).get(i).getAsString();
        }
        for (int i = 0; i < ((JsonArray) jo.get("school")).size(); i++) {
            schools[i] = ((JsonArray) jo.get("school")).get(i).getAsString();
        }

        // get owner
        int ownerId = getUserIdFromSession(session);
        User owner = this.users.get(ownerId);

        // create chatroom
        ChatRoom newRoom = new ChatRoom(this.nextRoomId, roomName, owner, ageLower, ageUpper,
                locations, schools, this);

        // check if the owner is eligible to join the room
        if (newRoom.applyFilter(owner)) {
            // add room to all rooms list
            this.rooms.put(nextRoomId, newRoom);

            // add owner to room
            owner.addRoom(newRoom);
            owner.moveToJoined(newRoom);

            // send update rooms list response to all qualifying users
            for (User notifyUser : this.users.values()) {
                notifyClient(owner, new NewRoomResponse(nextRoomId, roomName, ownerId));
                if (newRoom.applyFilter(notifyUser)) {
                    notifyUser.addRoom(newRoom);
                    notifyClient(notifyUser, new UserRoomResponse(notifyUser.getId(),
                            notifyUser.getJoinedRoomIds(), notifyUser.getAvailableRoomIds()));
                }
            }

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
     *
     * @param userId the id of the user to be removed
     */
    public void unloadUser(int userId) {
        User user = this.users.get(userId);
        String reason = user.getName() + "closed the session.";

        for (int roomId : user.getJoinedRoomIds()) {
            ChatRoom chatRoom = this.rooms.get(roomId);

            // leave room
            chatRoom.removeUser(user, reason);

            // notification response
            RoomNotificationResponse roomNotificationResponse = new RoomNotificationResponse(chatRoom.getNotifications());

            // roomuserlist response
            RoomUsersResponse roomUsersResponse = new RoomUsersResponse(chatRoom.getId(), chatRoom.getUsers());

            // notify all users in room
            for (Integer notifyId : chatRoom.getUsers().keySet()) {
                User notifyUser = this.users.get(notifyId);
                notifyClient(notifyUser, roomUsersResponse);
                notifyClient(notifyUser, roomNotificationResponse);
            }
        }
        users.remove(userId);
    }

    /**
     * Remove a room with given roomId from the environment.
     *
     * @param roomId the id of the chat room to be removed
     */
    public void unloadRoom(int roomId) {

    }

    /**
     * Make a user join a chat room.
     *
     * @param session the session that requests to called the method
     * @param body    of format "roomId"
     */
    public void joinRoom(Session session, String body) {
        JsonObject jo = new JsonParser().parse(body).getAsJsonObject().getAsJsonObject("body");
        int roomId = jo.get("roomId").getAsInt();

        ChatRoom chatRoom = this.rooms.get(roomId);
        User user = this.users.get(getUserIdFromSession(session));

        boolean userValid = chatRoom.applyFilter(user);
        if (userValid) {
            chatRoom.addUser(user);

            // TODO: move this logic into chat room class
            user.moveToJoined(chatRoom);

            //userrooomlist response
            UserRoomResponse userRoomResponse = new UserRoomResponse(userIdFromSession.get(session),
                    user.getJoinedRoomIds(), user.getAvailableRoomIds());
            notifyClient(user, userRoomResponse);

            //notification response
            RoomNotificationResponse roomNotificationResponse = new RoomNotificationResponse(chatRoom.getNotifications());
            //roomuserlist response
            RoomUsersResponse roomUsersResponse = new RoomUsersResponse(chatRoom.getId(), chatRoom.getUsers());

            // notify all users in room
            for (Map.Entry pair : chatRoom.getUsers().entrySet()) {
                User notifyUser = this.users.get(pair.getKey());
                notifyClient(notifyUser, roomUsersResponse);
                notifyClient(notifyUser, roomNotificationResponse);
            }


        } else {
            //TODO notify the user is not valid.
        }
    }

    /**
     * Make a user volunteer to leave a chat room.
     *
     * @param session the session that requests to called the method
     * @param body    of format "roomId"
     */
    public void leaveRoom(Session session, String body) {
        // parse body
        JsonObject jo = new JsonParser().parse(body).getAsJsonObject().getAsJsonObject("body");

        // get room
        int roomId = jo.get("roomId").getAsInt();
        ChatRoom chatRoom = this.rooms.get(roomId);

        // get user
        User user = this.users.get(userIdFromSession.get(session));

        // TODO: move this logic into chat room class
        // leave room
        chatRoom.removeUser(user, user.getName() + " left the room");
        user.moveToAvailable(chatRoom);

        // userrooomlist response
        UserRoomResponse userRoomResponse = new UserRoomResponse(userIdFromSession.get(session),
                user.getJoinedRoomIds(), user.getAvailableRoomIds());
        notifyClient(user, userRoomResponse);

        // notification response
        RoomNotificationResponse roomNotificationResponse = new RoomNotificationResponse(chatRoom.getNotifications());
        // roomuserlist response
        RoomUsersResponse roomUsersResponse = new RoomUsersResponse(chatRoom.getId(), chatRoom.getUsers());

        // notify all users in room
        for (Map.Entry pair : chatRoom.getUsers().entrySet()) {
            User notifyUser = this.users.get(pair.getKey());
            notifyClient(notifyUser, roomUsersResponse);
            notifyClient(notifyUser, roomNotificationResponse);
        }
    }

    /**
     * Make modification on chat room filer by the owner.
     *
     * @param session the session of the chat room owner
     * @param body    of format "roomId lower upper {[location],}*{[location]} {[school],}*{[school]}"
     */
    public void modifyRoom(Session session, String body) {

    }

    /**
     * A sender sends a string message to a receiver.
     *
     * @param session the session of the message sender
     * @param body    of format "roomId receiverId rawMessage"
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

        //check if message has word "hate"
        int result = rawMessage.indexOf("hate");
        boolean illegalMessage = false;
        if (result >= 0) {
            illegalMessage = true;
        }

        //if the message content is illegal, quit user from all chatRooms
        if (illegalMessage) {
            //leave all joined room
            for (int i : sender.getJoinedRoomIds()) {
                rooms.get(i).removeUser(sender, sender.getName()+"illegal message");
                sender.moveToAvailable(chatRoom);


                //notification response
                RoomNotificationResponse roomNotificationResponse = new RoomNotificationResponse(rooms.get(i).getNotifications());
                //roomuserlist response
                RoomUsersResponse roomUsersResponse = new RoomUsersResponse(rooms.get(i).getId(), rooms.get(i).getUsers());

                // notify all users in room
                for (Map.Entry pair : chatRoom.getUsers().entrySet()) {
                    User notifyUser = this.users.get(pair.getKey());
                    notifyClient(notifyUser, roomUsersResponse);
                    notifyClient(notifyUser, roomNotificationResponse);
                }

            }

            //remove all room from available room list
            for (int i : sender.getAvailableRoomIds()) {
                sender.removeRoom(rooms.get(i));
            }

            //userrooomlist response
            UserRoomResponse userRoomResponse = new UserRoomResponse(userIdFromSession.get(session),
                    sender.getJoinedRoomIds(), sender.getAvailableRoomIds());
            notifyClient(sender, userRoomResponse);

        }

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
            UserChatHistoryResponse userChatHistoryResponse = new UserChatHistoryResponse(chatHistory);
            notifyClient(receiver, userChatHistoryResponse);

        }
    }

    /**
     * Acknowledge the message from the receiver.
     *
     * @param session the session of the message receiver
     * @param body    of format "msgId"
     */
    public void ackMessage(Session session, String body) {

    }

    /**
     * Send query result from controller to front end.
     *
     * @param session the session that requests to called the method
     * @param body    of format "type roomId [senderId] [receiverId]"
     */
    public void query(Session session, String body) {
        JsonObject jo = new JsonParser().parse(body).getAsJsonObject().getAsJsonObject("body");
        String query = jo.get("query").getAsString();

        switch (query) {
            case "roomUsers":
                int roomId = jo.get("roomId").getAsInt();
                ChatRoom chatRoom = this.rooms.get(roomId);
                RoomUsersResponse roomUsersResponse = new RoomUsersResponse(chatRoom.getId(), chatRoom.getUsers());
                notifyClient(session, roomUsersResponse);
                break;
            default:
                break;
        }
    }

    /**
     * Notify the client for refreshing.
     *
     * @param user     user expected to receive the notification
     * @param response the information for notifying
     */
    public static void notifyClient(User user, AResponse response) {
        notifyClient(user.getSession(), response);
    }


    /**
     * Notify session about the message.
     *
     * @param session  the session to notify
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
     *
     * @param roomId the id of the chat room
     * @return all chat room members, mapping from user id to user name
     */
    private Map<Integer, String> getUsers(int roomId) {
        return this.rooms.get(roomId).getUsers();
    }

    /**
     * Get notifications in the chat room.
     *
     * @param roomId the id of the chat room
     * @return notifications of the chat room
     */
    private List<String> getNotifications(int roomId) {
        return this.rooms.get(roomId).getNotifications();
    }

    /**
     * Get chat history between user A and user B (commutative).
     *
     * @param roomId  the id of the chat room
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
