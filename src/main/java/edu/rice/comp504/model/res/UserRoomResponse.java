package edu.rice.comp504.model.res;

import java.util.List;

public class UserRoomResponse extends AResponse {
    private int userId;
    private List<Integer> joinedRoomIds;
    private List<Integer> availableRoomIds;
    public UserRoomResponse(String type, int userId, List<Integer> joinedRoomIds, List<Integer> availableRoomIds) {
        super(type);
        this.userId = userId;
        this.joinedRoomIds = joinedRoomIds;
        this.availableRoomIds = availableRoomIds;
    }
}
