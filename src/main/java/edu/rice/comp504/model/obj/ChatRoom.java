package edu.rice.comp504.model.obj;

import edu.rice.comp504.model.DispatcherAdapter;

import java.util.*;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
/*
The Chatroom class defines a chat room object and private fileds of a chat room
*/
public class ChatRoom extends Observable {

    private int id;
    private String name;
    private User owner;
    private int ageLowerBound;
    private int ageUpperBound;
    private String[] locations;
    private String[] schools;

    private DispatcherAdapter dis;

    // notifications contain why the user left, etc.
    private List<String> notifications;

    // Maps key("smallId&largeId") to list of chat history strings
    private Map<String, List<Message>> chatHistory;

    /**
     * Constructor.
     * @param id the identity number of the chat room
     * @param name the name of the chat room
     * @param owner the chat room owner
     * @param lower the lower bound of age restriction
     * @param upper the upper bound of age restriction
     * @param locations the location restriction
     * @param schools the school restriction
     * @param dispatcher the adapter
     */
    public ChatRoom(int id, String name, User owner,
                    int lower, int upper, String[] locations, String[] schools,
                    DispatcherAdapter dispatcher) {
        this.id = id;

        this.name = name;
        this.owner = owner;

        this.ageLowerBound = lower;
        this.ageUpperBound = upper;
        this.locations = locations;
        this.schools = schools;

        this.dis = dispatcher;

        this.notifications = new LinkedList<>();
        this.chatHistory = new ConcurrentHashMap<>();
    }

    /**
     * Get the chat room id
     * @return the chat room id
     * */
    public int getId() {
        return this.id;
    }

    /**
     * Get the chat room name
     * @return the chat room name
     * */
    public String getName() {
        return this.name;
    }

    /**
     * Get the chat room owner
     * @return a User object which is the owner of the chat room
     * */
    public User getOwner() {
        return this.owner;
    }

    /**
     * Get a list of notifications
     * @return notification list
     * */
    public List<String> getNotifications() {
        return this.notifications;
    }

    /**
     * Get the chat history between two users
     * @return chat history
     * */
    public Map<String, List<Message>> getChatHistory() {
        return this.chatHistory;
    }

    /**
     * @return the dispatcher
     * */
    public DispatcherAdapter getDispatcher() {
        return this.dis;
    }

    /**
     * Return users in the chat room
     */
    public Map<Integer, String> getUsers() {
        return null;
    }

    /**
     * Check if user satisfy the age, location and school restriction
     * @return boolean value indicating whether the user is eligible to join the room
     */
    public boolean applyFilter(User user) {
        return false;
    }

    /**
     * Modify the current room age, location or school restriction
     * Then apply the new restriction to all users in the chat room
     */
    public void modifyFilter(int lower, int upper, String[] locations, String[] schools) {

    }

    /**
     * If user satisfy all restrictions and has the room in his available room list
     * Create a user joined notification message and then add user into the observer list
     */
    public boolean addUser(User user) {
        return false;
    }

    /**
     * Remove user from the chat room
     * Set notification indicating the user left reason
     * Delete user from observer list
     */
    public boolean removeUser(User user, String reason) {
        return false;
    }

    /**
     * Append chat message into chat history list
     * Map the single message body with key value (senderID&receiverID)
     */
    public void storeMessage(User sender, User receiver, Message message) {

    }

    /**
     * Parse the key and remove chat history related to user
     */
    private void freeChatHistory(User user) {
        // TODO: parse the key and remove chat history related to user
    }
}
