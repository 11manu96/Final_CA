package edu.rice.comp504.model.res;

import java.util.List;

public class RoomNotificationResponse extends AResponse {
    private int roomid;
    private List<String> notifications;
    public RoomNotificationResponse(List<String> notifications) {
        super("RoomNotifications");
        this.notifications = notifications;
    }
}
