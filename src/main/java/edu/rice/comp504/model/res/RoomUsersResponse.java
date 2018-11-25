package edu.rice.comp504.model.res;

import java.util.Map;

/**
 * Response when sending room users list.
 */
public class RoomUsersResponse extends AResponse {
    private int roomId;
    private Map<Integer, String> users;

    /**
     * Constructor.
     * @param roomId room id
     * @param users users in room
     */
    public RoomUsersResponse(int roomId, Map<Integer, String> users) {
        super("RoomUsers");
        this.roomId = roomId;
        this.users = users;
    }
}
