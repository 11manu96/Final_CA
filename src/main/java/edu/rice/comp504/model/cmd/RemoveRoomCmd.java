package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.res.UserRoomResponse;

public class RemoveRoomCmd implements IUserCmd {
    private ChatRoom chatRoom;

    /**
     * Constructor.
     * @param chatRoom chatRoom
     */
    public RemoveRoomCmd(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    /**
     * Execute the command.
     * @param context user
     */
    @Override
    public void execute(User context) {
        // only send message to live sessions
        if (this.chatRoom.getDispatcher().containsSession(context.getSession())) {
            if (context.getJoinedRoomIds().contains(this.chatRoom.getId()) ||
                    context.getAvailableRoomIds().contains(this.chatRoom.getId())) {
                context.removeRoom(this.chatRoom);
                DispatcherAdapter.notifyClient(context, new UserRoomResponse(context.getId(),
                        context.getJoinedRoomIds(), context.getAvailableRoomIds()));
            }
        }
    }
}
