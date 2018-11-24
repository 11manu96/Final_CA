package edu.rice.comp504.model.res;

import java.util.List;

public class RoomNotificationResponse extends AResponse {
    private int roomId;
    private List<String> notifications;
    public RoomNotificationResponse(int roomId, List<String> notifications) {
        super("RoomNotifications");
        this.roomId = roomId;
        this.notifications = notifications;
    }
}
