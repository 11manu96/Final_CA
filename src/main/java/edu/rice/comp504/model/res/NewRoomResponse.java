package edu.rice.comp504.model.res;

/**
 * NewRoomResponse when create a new chat room
 */
public class NewRoomResponse extends AResponse {
    //private String type;
    private int roomId;
    private String roomName;
    private int ownerId;

    public NewRoomResponse(int roomId, String roomName, int ownerId) {
        super("NewRoomResponse");
        this.roomId = roomId;
        this.roomName = roomName;
        this.ownerId = ownerId;
    }

}
