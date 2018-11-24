package edu.rice.comp504.model.res;
import java.util.Map;

public class RoomUsersResponse extends AResponse {
    private int roomId;
    private Map<Integer, String> users;

    public RoomUsersResponse(int roomId, Map<Integer, String> users) {
        super("RoomUsersResponse");
        this.roomId = roomId;
        this.users = users;
    }
}
