package edu.rice.comp504.model.obj;
/*
The Message class defines a message object and private fileds of a message
*/
public class Message {
    private int id;

    private int roomId;
    private int senderId;
    private int receiverId;
    private String message;

    private boolean isReceived;

    /**
     * Constructor.
     * @param id is the message id
     * @param roomId the id number for current chat room
     * @param senderId the id number for message sender
     * @param receiverId the id number for message receiver
     * @param message the body of the message
     */
    public Message(int id, int roomId, int senderId, int receiverId, String message) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.isReceived = false;
    }

    /**
     * Get the message id
     * @return the message id number
     * */
    public int getId() {
        return this.id;
    }

    /**
     * Get the room id
     * @return the current chat room id number
     * */
    public int getRoomId() {
        return this.roomId;
    }

    /**
     * Get the message sender id
     * @return the message sender id number
     * */
    public int getSenderId() {
        return this.senderId;
    }

    /**
     * Get the message receiver id
     * @return the message receiver id number
     * */
    public int getReceiverId() {
        return this.receiverId;
    }

    /**
     * Get the current message
     * @return the message body in string
     * */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the receive status of message
     * @return the boolean value
     * */
    public boolean getIsReceived() {
        return this.isReceived;
    }

    /**
     * Set the isReceived to be a boolean value
     * @param val boolean value to be True or False
     * */
    public void setIsReceived(boolean val) {
        this.isReceived = val;
    }
}
