package edu.rice.comp504.model.obj;

import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.cmd.JoinRoomCmd;
import edu.rice.comp504.model.cmd.LeaveRoomCmd;

import java.util.*;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Chatroom class defines a chat room object and private fields of a chat room.
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

    // Maps user id to the user name
    private Map<Integer, String> userNameFromUserId;

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

        this.userNameFromUserId = new ConcurrentHashMap<>();
        this.notifications = new LinkedList<>();
        this.chatHistory = new ConcurrentHashMap<>();
    }

    /**
     * Get the chat room id.
     * @return the chat room id
     * */
    public int getId() {
        return this.id;
    }

    /**
     * Get the chat room name.
     * @return the chat room name
     * */
    public String getName() {
        return this.name;
    }

    /**
     * Get the chat room owner.
     * @return a User object which is the owner of the chat room
     * */
    public User getOwner() {
        return this.owner;
    }

    /**
     * Get a list of notifications.
     * @return notification list
     * */
    public List<String> getNotifications() {
        return this.notifications;
    }

    /**
     * Get the chat history between two users.
     * @return chat history
     * */
    public Map<String, List<Message>> getChatHistory() {
        return this.chatHistory;
    }

    /**
     * Return dispatch adapter.
     * @return dispatch adapter
     */
    public DispatcherAdapter getDispatcher() {
        return this.dis;
    }

    /**
     * Return users in the chat room.
     * @return users map
     */
    public Map<Integer, String> getUsers() {
        return this.userNameFromUserId;
    }

    /**
     * Check if user satisfy the age, location and school restriction.
     * @return boolean value indicating whether the user is eligible to join the room
     */
    public boolean applyFilter(User user) {
        int userAge = user.getAge();
        String userLocation = user.getLocation();
        String userSchool = user.getSchool();
        List<String> locationsList = Arrays.asList(this.locations);
        List<String> schoolsList = Arrays.asList(this.schools);
        boolean userValid = userAge >= this.ageLowerBound && userAge <= this.ageUpperBound
                            && locationsList.contains(userLocation)
                            && schoolsList.contains(userSchool);

        return userValid;
    }

    /**
     * Modify the current room age, location or school restriction.
     * Then apply the new restriction to all users in the chat room.
     * @param lower min age
     * @param upper max age
     * @param locations location restriction
     * @param schools school restriction
     */
    public void modifyFilter(int lower, int upper, String[] locations, String[] schools) {
    }

    /**
     * If user satisfy all restrictions and has the room in his available room list.
     * Create a user joined notification message and then add user into the observer list.
     * @param user user being added
     * @return whether user was added successfully
     */
    public boolean addUser(User user) {
        // user available rooms only contains eligible rooms
        if (user.getAvailableRoomIds().contains(this.id)) {
            this.userNameFromUserId.put(user.getId(), user.getName());
            addObserver(user);
            user.moveToJoined(this);

            this.notifications.add(user.getName() + " joined " + this.getName());

            JoinRoomCmd joinRoomCmd = new JoinRoomCmd(this, user);
            setChanged();
            notifyObservers(joinRoomCmd);

            return true;
        }
        return false;
    }

    /**
     * Remove user from the chat room. Set notification indicating the user left reason.
     * Delete user from observer list.
     * @param user user to remove
     * @param reason reason to remove user
     * @return whether removal was successful
     */
    public boolean removeUser(User user, String reason) {
        int userid = user.getId();
        if (this.userNameFromUserId.containsKey(userid)) {
            this.userNameFromUserId.remove(userid);
            user.moveToAvailable(this);

            this.notifications.add(reason);

            LeaveRoomCmd leaveRoomCmd = new LeaveRoomCmd(this, user);
            setChanged();
            notifyObservers(leaveRoomCmd);

            // if the user is owner, unload the chat room
            if (user == this.owner) {
                this.dis.unloadRoom(this.id);
            }
            deleteObserver(user);

            return true;
        }
        return false;
    }

    /**
     * Append chat message into chat history list.
     * Map the single message body with key value (senderID&receiverID).
     * @param sender sender user
     * @param receiver receiver user
     * @param message message to send
     */
    public void storeMessage(User sender, User receiver, Message message) {
        int userAId = sender.getId();
        int userBId = receiver.getId();
        String combineId = userAId < userBId ?
                String.valueOf(userAId) + "&" + String.valueOf(userBId) : String.valueOf(userBId) + "&" + String.valueOf(userAId);

        if (!this.chatHistory.containsKey(combineId)) {
            this.chatHistory.put(combineId, new ArrayList<>());
        }
        this.chatHistory.get(combineId).add(message);
    }

    /**
     * Parse the key and remove chat history related to user.
     * @param user user
     */
    private void freeChatHistory(User user) {
    }
}