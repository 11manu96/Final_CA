package edu.rice.comp504.model.res;

import java.util.List;

/**
 * Response when sending user room lists.
 */
public class UserRoomResponse extends AResponse {
    private int userId;
    private List<Integer> joinedRoomIds;
    private List<Integer> availableRoomIds;

    /**
     * Constructor.
     * @param userId user id
     * @param joinedRoomIds user joined rooms
     * @param availableRoomIds user eligible rooms
     */
    public UserRoomResponse(int userId, List<Integer> joinedRoomIds, List<Integer> availableRoomIds) {
        super("UserRoom");
        this.userId = userId;
        this.joinedRoomIds = joinedRoomIds;
        this.availableRoomIds = availableRoomIds;
    }
}
