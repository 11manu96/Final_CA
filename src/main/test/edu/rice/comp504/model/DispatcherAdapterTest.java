package edu.rice.comp504.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.Message;
import edu.rice.comp504.model.obj.User;
import junit.framework.TestCase;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.api.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Test the basic functions in DispatchAdapter. *Run app first before running tests.*
 */
@org.eclipse.jetty.websocket.api.annotations.WebSocket
public class DispatcherAdapterTest extends TestCase {

    /**
     * A user must initiate the creation of a chat room
     */
    public void testLoadRoom() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        //user0
        Session session0 = getSession();
        User user0 = loadUser0(session0, adapter);

        //chatRoom0
        ChatRoom chatRoom0 = loadRoom0(session0, adapter);

        assertEquals("check chatRoom name", "chatRoom0", chatRoom0.getName());
        assertEquals("check chatRoom owner", user0, chatRoom0.getOwner());

        assertEquals("check the number of chatRoom users", 1, chatRoom0.getUsers().size());
        assertEquals("check chatRoom users", "user0", chatRoom0.getUsers().get(0));
    }


    /**
     * A user can only send a message to someone else in the same chat room as them
     */
    public void testSendMessage() {
        //send a message to someone else in the same chat room as them

        DispatcherAdapter adapter = new DispatcherAdapter();

        //user0
        Session session0 = getSession();
        User user0 = loadUser0(session0, adapter);

        //chatRoom0
        ChatRoom chatRoom0 = loadRoom0(session0, adapter);

        //user1
        Session session1 = getSession();
        User user1 = loadUser1(session1, adapter);

        //user1 joins chatRoom0
        adapter.joinRoom(session1, getJoinRoomJson(0).toString());

        //user0 sends message to user1
        String message01 = "User0 sends message to user1.";
        JsonObject jo = getSendMessageJson(0, message01, "1");
        adapter.sendMessage(session0, jo.toString());

        //check the message has been sent
        Map<String, List<Message>> chatHistory0 = chatRoom0.getChatHistory();
        List<Message> filterHistory0 = chatHistory0.get("0&1");
        assertEquals("check the message01 has been sent", message01, filterHistory0.get(filterHistory0.size() - 1).getMessage());


        //send a message to someone else not in the same chat room as them

        //user2
        Session session2 = getSession();
        jo = getLoadUserJson("user2", 25, "North America", "Rice");
        User user2 = adapter.loadUser(session2, jo.toString());

        //chatRoom1
        ChatRoom chatRoom1 = loadRoom1(session2, adapter);

        //user0 sends message to user2
        String message02 = "User0 sends message to user2.";
        jo = getSendMessageJson(0, message02, "2");
        adapter.sendMessage(session0, jo.toString());

        //check the message has been sent
        Map<String, List<Message>> chatHistory1 = chatRoom1.getChatHistory();
        List<Message> filterChatHistory1 = chatHistory1.get("0&2");
        assertEquals("check the message02 hasn't been sent", null, filterChatHistory1);


    }

    /**
     * A user should be notified that their message has been received
     */
    public void testReceiveMessageNotification() {

        DispatcherAdapter adapter = new DispatcherAdapter();
        JsonObject jo;

        //user0
        Session session0 = getSession();
        User user0 = loadUser0(session0, adapter);

        //charRoom0
        ChatRoom chatRoom0 = loadRoom0(session0, adapter);

        //user1
        Session session1 = getSession();
        User user1 = loadUser1(session1, adapter);

        //user1 joins chatRoom0
        adapter.joinRoom(session1, getJoinRoomJson(0).toString());

        //user0 sends message to user1
        String message01 = "User0 sends message to user1.";
        jo = getSendMessageJson(0, message01, "1");
        adapter.sendMessage(session0, jo.toString());
        adapter.ackMessage(session0, getAckMessageJson(0).toString());

        //check the message is received, message will be set to green to notify the sender
        Map<String, List<Message>> chatHistory0 = chatRoom0.getChatHistory();
        List<Message> filterChatHistory0 = chatHistory0.get("0&1");
        assertEquals("check user0 is notified that his message has been received", true, filterChatHistory0.get(0).getIsReceived());
    }

    /**
     * A user may be in multiple chat rooms
     */
    public void testUserInMultipleRooms() {

        DispatcherAdapter adapter = new DispatcherAdapter();

        //user0
        Session session0 = getSession();
        User user0 = loadUser0(session0, adapter);

        //chatRoom0
        ChatRoom chatRoom0 = loadRoom0(session0, adapter);

        //chatRoom1
        ChatRoom chatRoom1 = loadRoom1(session0, adapter);

        assertEquals("check the user in multiple chat rooms", 2, user0.getJoinedRoomIds().size());
    }

    /**
     * A user can choose to exit one chat room or all chat rooms
     */
    public void testExitRooms() {
        //exit one chat room

        DispatcherAdapter adapter = new DispatcherAdapter();

        //user0
        Session session0 = getSession();
        User user0 = loadUser0(session0, adapter);

        //chatRoom0
        ChatRoom chatRoom0 = loadRoom0(session0, adapter);

        JsonObject jo = getLeaveRoomJson("0");
        adapter.leaveRoom(session0, jo.toString());
        assertEquals("exit one chat room", 0, user0.getJoinedRoomIds().size());


        //exit all chat rooms

        //create charRoom0 again
        chatRoom0 = loadRoom0(session0, adapter);

        //chatRoom1
        ChatRoom chatRoom1 = loadRoom1(session0, adapter);

        jo = getLeaveRoomJson("All");
        adapter.leaveRoom(session0, jo.toString());
        assertEquals("exit all chat rooms", 0, user0.getJoinedRoomIds().size());
    }

    /**
     * One of the users in the chat must be the owner
     */
    public void testOwner() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        //user0
        Session session0 = getSession();
        User user0 = loadUser0(session0, adapter);

        //chatRoom0
        ChatRoom chatRoom0 = loadRoom0(session0, adapter);

        //user1
        Session session1 = getSession();
        User user1 = loadUser1(session1, adapter);

        //user1 joins chatRoom0
        adapter.joinRoom(session1, getJoinRoomJson(0).toString());

        assertEquals("check user0 is the owner", user0, chatRoom0.getOwner());
    }

    /**
     * Owners can restrict the type of user that can join the chat room
     * This test contains test 'A user can join a chat room if they are qualified to join'
     */
    public void testOwnerRestrictType() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        //user0
        Session session0 = getSession();
        User user0 = loadUser0(session0, adapter);

        //chatRoom0
        ChatRoom chatRoom0 = loadRoom0(session0, adapter);

        //user1
        Session session1 = getSession();
        User user1 = loadUser1(session1, adapter);

        //user1 joins chatRoom0
        adapter.joinRoom(session1, getJoinRoomJson(0).toString());
        assertEquals("user1 can join chatRoom0", true, chatRoom0.getUsers().containsKey(1));

        //user2
        Session session2 = getSession();
        JsonObject jo = getLoadUserJson("user2", 25, "North America", "Duke");
        User user2 = adapter.loadUser(session2, jo.toString());

        //user2 joins chatRoom0
        adapter.joinRoom(session2, getJoinRoomJson(0).toString());
        assertEquals("user2 cannot join chatRoom0", false, chatRoom0.getUsers().containsKey(2));
    }

    /**
     * A user will forcibly be removed from all chat rooms if the user uses the word “hate” in a message
     */
    public void testUseHateRemove() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        //user0
        Session session0 = getSession();
        User user0 = loadUser0(session0, adapter);

        //chatRoom0
        ChatRoom chatRoom0 = loadRoom0(session0, adapter);

        //user1
        Session session1 = getSession();
        User user1 = loadUser1(session1, adapter);

        //chatRoom1
        ChatRoom chatRoom1 = loadRoom1(session1, adapter);

        //user1 joins chatRoom0
        adapter.joinRoom(session1, getJoinRoomJson(0).toString());

        //user0 sends message to user1
        String message10 = "User1 sends a message to user0 which contains 'hate'.";
        JsonObject jo = getSendMessageJson(0, message10, "0");
        adapter.sendMessage(session1, jo.toString());

        assertEquals("user1 is removed from all chat rooms", 0, user1.getJoinedRoomIds().size());
    }

    /**
     * Only owners can send a single message to all users in the chat room
     */
    public void testSendToAll() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        //user0
        Session session0 = getSession();
        User user0 = loadUser0(session0, adapter);

        //chatRoom0
        ChatRoom chatRoom0 = loadRoom0(session0, adapter);

        //user1
        Session session1 = getSession();
        User user1 = loadUser1(session1, adapter);

        //user1 joins chatRoom0
        adapter.joinRoom(session1, getJoinRoomJson(0).toString());

        //user2
        Session session2 = getSession();
        JsonObject jo = getLoadUserJson("user2", 25, "North America", "Rice");
        User user2 = adapter.loadUser(session2, jo.toString());

        //user2 joins chatRoom0
        adapter.joinRoom(session2, getJoinRoomJson(0).toString());

        //user0 sends message to all users
        String message0All = "User0 sends message to all users";
        jo = getSendMessageJson(0, message0All, "All");
        adapter.sendMessage(session0, jo.toString());

        //check the message has been sent
        Map<String, List<Message>> chatHistory = chatRoom0.getChatHistory();
        List<Message> filterHistory01 = chatHistory.get("0&1");
        assertEquals("check the message0All has been sent to user1", message0All, filterHistory01.get(filterHistory01.size() - 1).getMessage());
        List<Message> filterHistory02 = chatHistory.get("0&2");
        assertEquals("check the message0All has been sent to user2", message0All, filterHistory02.get(filterHistory02.size() - 1).getMessage());

        //user1 sends message to all users
        String message1All = "User2 sends message to all users";
        jo = getSendMessageJson(0, message1All, "All");
        adapter.sendMessage(session1, jo.toString());

        //check the message hasn't been sent
        chatHistory = chatRoom0.getChatHistory();
        List<Message> filterHistory10 = chatHistory.get("0&1");
        assertEquals("check the message1All hasn't been sent to user0", false,  message1All.equals(filterHistory10.get(filterHistory10.size() - 1).getMessage()));
        List<Message> filterHistory12 = chatHistory.get("1&2");
        assertEquals("check the message1All hasn't been sent to user2", true, filterHistory12 == null);
    }

    /**
     * A user can determine who is in the chat room
     */
    public void testDetermineUsers() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        //user0
        Session session0 = getSession();
        User user0 = loadUser0(session0, adapter);

        //chatRoom0
        ChatRoom chatRoom0 = loadRoom0(session0, adapter);

        //user1
        Session session1 = getSession();
        User user1 = loadUser1(session1, adapter);

        //user1 joins chatRoom0
        adapter.joinRoom(session1, getJoinRoomJson(0).toString());

        assertEquals("user0 can see there are two users in the chat room", 2, chatRoom0.getUsers().size());
        assertEquals("user0 can see user0 is in the chat room", "user0", chatRoom0.getUsers().get(0));
        assertEquals("user0 can see user1 is in the chat room", "user1", chatRoom0.getUsers().get(1));
    }

    /**
     * A user can determine what chat rooms they have joined and what rooms they can join
     */
    public void testDetermineRooms() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        //user0
        Session session0 = getSession();
        User user0 = loadUser0(session0, adapter);

        //chatRoom0
        ChatRoom chatRoom0 = loadRoom0(session0, adapter);

        //user1
        Session session1 = getSession();
        User user1 = loadUser1(session1, adapter);

        //chatRoom1
        ChatRoom chatRoom1 = loadRoom1(session1, adapter);

        assertEquals("user0 can determine how many chat rooms they have joined", 1, user0.getJoinedRoomIds().size());
        assertEquals("user0 can determine what chat rooms they have joined", true, 0 == user0.getJoinedRoomIds().get(0));

        assertEquals("user0 can determine how many rooms they can join", 1, user0.getAvailableRoomIds().size());
        assertEquals("user0 can determine what rooms they can join", true, 1 == user0.getAvailableRoomIds().get(0));
    }

    /**
     * users only see the messages they are suppose to view
     */
    public void testFilterMessages() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        //user0
        Session session0 = getSession();
        User user0 = loadUser0(session0, adapter);

        //chatRoom0
        ChatRoom chatRoom0 = loadRoom0(session0, adapter);

        //user1
        Session session1 = getSession();
        User user1 = loadUser1(session1, adapter);

        //user1 joins chatRoom0
        adapter.joinRoom(session1, getJoinRoomJson(0).toString());

        //user2
        Session session2 = getSession();
        JsonObject jo = getLoadUserJson("user2", 25, "North America", "Rice");
        User user2 = adapter.loadUser(session2, jo.toString());

        //user2 joins chatRoom0
        adapter.joinRoom(session2, getJoinRoomJson(0).toString());

        //user0 sends message to user1
        String message01 = "User0 sends message to user1";
        jo = getSendMessageJson(0, message01, "1");
        adapter.sendMessage(session0, jo.toString());

        //check user2 cannot see message01
        assertEquals("user2 cannot see message01", null, chatRoom0.getChatHistory().get("0&2"));

        //user0 sends message to user2
        String message02 = "User0 sends message to user2";
        jo = getSendMessageJson(0, message02, "2");
        adapter.sendMessage(session0, jo.toString());

        //check user2 cannot see message02
        assertEquals("user2 can see message02", message02, chatRoom0.getChatHistory().get("0&2").get(0).getMessage());
    }

    /**
     *  Messages appear in the proper order and on separate lines
     */
    public void testProperOrderMessages() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        //user0
        Session session0 = getSession();
        User user0 = loadUser0(session0, adapter);

        //chatRoom0
        ChatRoom chatRoom0 = loadRoom0(session0, adapter);

        //user1
        Session session1 = getSession();
        User user1 = loadUser1(session1, adapter);

        //user1 joins chatRoom0
        adapter.joinRoom(session1, getJoinRoomJson(0).toString());

        //user0 sends message to user1
        String message0 = "User0 sends message0 to user1";
        JsonObject jo = getSendMessageJson(0, message0, "1");
        adapter.sendMessage(session0, jo.toString());

        String message1 = "User0 sends message1 to user1";
        jo = getSendMessageJson(0, message1, "1");
        adapter.sendMessage(session0, jo.toString());

        assertEquals("message0 is the first message in the chatHistoryList", message0, chatRoom0.getChatHistory().get("0&1").get(0).getMessage());
        assertEquals("message1 is the second message in the chatHistoryList", message1, chatRoom0.getChatHistory().get("0&1").get(1).getMessage());
    }

    /**
     * Clear to remaining users why a user left the room
     */
    public void testLeftReason() {
        DispatcherAdapter adapter = new DispatcherAdapter();

        //user0
        Session session0 = getSession();
        User user0 = loadUser0(session0, adapter);

        //chatRoom0
        ChatRoom chatRoom0 = loadRoom0(session0, adapter);

        //user1
        Session session1 = getSession();
        User user1 = loadUser1(session1, adapter);

        //user1 joins chatRoom0
        adapter.joinRoom(session1, getJoinRoomJson(0).toString());

        //user1 leave chatRoom0
        adapter.leaveRoom(session1, getLeaveRoomJson("0").toString());

        //check chatRoom0 has notification
        List<String> notifications = chatRoom0.getNotifications();
        assertEquals("chatRoom0 has notification", "user1 left chatRoom0", notifications.get(notifications.size() - 1));
    }

    /**
     * get a new session
     * @return the session
     */
    private Session getSession() {
        WebSocketClient webSocketClient = new WebSocketClient();
        try {
            webSocketClient.start();

            URI uri = URI.create("ws://" + "localhost" + ":" + "4567" + "/chatapp");
            Session session = webSocketClient.connect(this, uri, new ClientUpgradeRequest()).get();
            return session;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * load user0
     * @param session the user session
     * @param adapter the DispatchAdapter
     * @return the user object
     */
    private User loadUser0(Session session, DispatcherAdapter adapter) {

        JsonObject jo = getLoadUserJson("user0", 20, "North America", "Rice");
        return adapter.loadUser(session, jo.toString());
    }

    /**
     * load chatRoom0
     * @param session the owner session
     * @param adapter the DispatchAdapter
     * @return the chatRoom object
     */
    private ChatRoom loadRoom0(Session session, DispatcherAdapter adapter) {
        List<String> locationList = Arrays.asList("North America", "South America");
        List<String> schoolList = Arrays.asList("Rice", "Harvard");
        JsonObject jo = getLoadRoomJson("chatRoom0", 18, 30, locationList, schoolList);
        return adapter.loadRoom(session, jo.toString());
    }

    /**
     * load user1
     * @param session the user session
     * @param adapter the DispatchAdapter
     * @return the user object
     */
    private User loadUser1(Session session, DispatcherAdapter adapter) {

        JsonObject jo = getLoadUserJson("user1", 22, "North America", "Harvard");
        return adapter.loadUser(session, jo.toString());
    }

    /**
     * load chatRoom1
     * @param session the owner session
     * @param adapter the DispatchAdapter
     * @return the chatRoom object
     */
    private ChatRoom loadRoom1(Session session, DispatcherAdapter adapter) {
        List<String> locationList = Arrays.asList("North America");
        List<String> schoolList = Arrays.asList("Rice", "Harvard");
        JsonObject jo = getLoadRoomJson("chatRoom1", 18, 30, locationList, schoolList);
        return adapter.loadRoom(session, jo.toString());
    }

    /**
     * get the json object for load chat room
     * @param roomName the chat room name
     * @param ageLower the chat room ageLower
     * @param ageUpper the chat room ageUpper
     * @param locationaList the list of location
     * @param schoolList the list of school
     * @return the json object
     */
    private JsonObject getLoadRoomJson(String roomName, int ageLower, int ageUpper, List<String> locationaList, List<String> schoolList) {
        JsonObject body = new JsonObject();
        body.addProperty("roomName", roomName);
        body.addProperty("ageLower", ageLower);
        body.addProperty("ageUpper", ageUpper);

        JsonArray location = new JsonArray();
        for (int i = 0; i < locationaList.size(); i++)
            location.add(locationaList.get(i));
        body.add("location", location);

        JsonArray school = new JsonArray();
        for (int i = 0; i < schoolList.size(); i++)
            school.add(schoolList.get(i));
        body.add("school", school);

        JsonObject jo = new JsonObject();
        jo.addProperty("type", "create");
        jo.add("body", body);

        return jo;
    }

    /**
     * get the json object for join chat room
     * @param roomId the chat room id
     * @return the json object
     */
    private JsonObject getJoinRoomJson(int roomId) {
        JsonObject body = new JsonObject();
        body.addProperty("roomId", roomId);

        JsonObject jo = new JsonObject();
        jo.addProperty("type", "join");
        jo.add("body", body);

        return jo;
    }

    /**
     * get the json object for send message
     * @param roomId the chat room id
     * @param message the message
     * @param receiverId the receiver id
     * @return the json object
     */
    private JsonObject getSendMessageJson(int roomId, String message, String receiverId) {
        JsonObject body = new JsonObject();
        body.addProperty("roomId", roomId);
        body.addProperty("message", message);
        body.addProperty("receiverId",receiverId);

        JsonObject jo = new JsonObject();
        jo.addProperty("type", "send");
        jo.add("body", body);

        return jo;
    }

    /**
     * get the json object for load user
     * @param name the user name
     * @param age the user age
     * @param location the user location
     * @param school the user school
     * @return the json object
     */
    private JsonObject getLoadUserJson(String name, int age, String location, String school) {
        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("age", age);
        body.addProperty("location", location);
        body.addProperty("school", school);

        JsonObject jo = new JsonObject();
        jo.addProperty("type", "login");
        jo.add("body", body);

        return jo;
    }

    /**
     * get the json object for leave chat room
     * @param roomId the chat room id
     * @return the json object
     */
    private JsonObject getLeaveRoomJson(String roomId) {
        JsonObject body = new JsonObject();
        body.addProperty("roomId", roomId);

        JsonObject jo = new JsonObject();
        jo.addProperty("type", "leave");
        jo.add("body", body);

        return jo;
    }

    /**
     * get the json object for leave chat room
     * @param messageId the message id
     * @return the json object
     */
    private JsonObject getAckMessageJson(int messageId) {
        JsonObject body = new JsonObject();
        body.addProperty("messageId", messageId);

        JsonObject jo = new JsonObject();
        jo.addProperty("type", "ack");
        jo.add("body", body);

        return jo;
    }
}