package edu.rice.comp504.model.res;

/**
 * Response when creating a new chat room.
 */
public class NewRoomResponse extends AResponse {
    private int roomId;
    private String roomName;
    private int ownerId;

    /**
     * Constructor.
     * @param roomId room id
     * @param roomName room name
     * @param ownerId owner id
     */
    public NewRoomResponse(int roomId, String roomName, int ownerId) {
        super("NewRoom");
        this.roomId = roomId;
        this.roomName = roomName;
        this.ownerId = ownerId;
    }

}
