package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.res.RoomNotificationResponse;
import edu.rice.comp504.model.res.RoomUsersResponse;
import edu.rice.comp504.model.res.UserRoomResponse;

/**
 * Command to execute when joining a room.
 */
public class JoinRoomCmd implements IUserCmd {
    private ChatRoom chatRoom;
    private User user;

    /**
     * Constructor.
     * @param chatRoom chat room user is joining
     * @param user user joining chat room
     */
    public JoinRoomCmd(ChatRoom chatRoom, User user) {
        this.chatRoom = chatRoom;
        this.user = user;
    }

    /**
     * Execute the command.
     * @param context user
     */
    @Override
    public void execute(User context) {
        // if this user is joining, then update rooms lists
        if (this.user == context) {
            UserRoomResponse userRoomResponse = new UserRoomResponse(this.user.getId(),
                    this.user.getJoinedRoomIds(), this.user.getAvailableRoomIds());
            DispatcherAdapter.notifyClient(context, userRoomResponse);
        }
        // all users update room users list
        RoomUsersResponse roomUsersResponse = new RoomUsersResponse(this.chatRoom.getId(), this.chatRoom.getUsers());
        DispatcherAdapter.notifyClient(context, roomUsersResponse);

        RoomNotificationResponse roomNotificationResponse = new RoomNotificationResponse(this.chatRoom.getId(),
                this.chatRoom.getNotifications());
        DispatcherAdapter.notifyClient(context, roomNotificationResponse);
    }
}
