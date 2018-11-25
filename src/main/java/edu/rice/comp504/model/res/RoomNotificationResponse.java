package edu.rice.comp504.model.res;

import java.util.List;

/**
 * Response when sending a room notification.
 */
public class RoomNotificationResponse extends AResponse {
    private int roomId;
    private List<String> notifications;

    /**
     * Constructor.
     * @param roomId room id
     * @param notifications notifications list
     */
    public RoomNotificationResponse(int roomId, List<String> notifications) {
        super("RoomNotifications");
        this.roomId = roomId;
        this.notifications = notifications;
    }
}
