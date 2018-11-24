package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.DispatcherAdapter;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;
import edu.rice.comp504.model.res.NewRoomResponse;
import edu.rice.comp504.model.res.UserRoomResponse;

public class AddRoomCmd implements IUserCmd {
    private ChatRoom chatRoom;

    /**
     * Constructor.
     * @param chatRoom chatRoom
     */
    public AddRoomCmd(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    /**
     * Execute the command.
     * @param context user
     */
    @Override
    public void execute(User context) {
        // all users get room mapping
        DispatcherAdapter.notifyClient(context, new NewRoomResponse(this.chatRoom.getId(),
                this.chatRoom.getName(), this.chatRoom.getOwner().getId()));
        // all qualifying users update chat rooms list
        if (this.chatRoom.applyFilter(context)) {
            context.addRoom(this.chatRoom);
            if (context == chatRoom.getOwner()) {
                context.moveToJoined(this.chatRoom);
            }
            DispatcherAdapter.notifyClient(context, new UserRoomResponse(context.getId(),
                    context.getJoinedRoomIds(), context.getAvailableRoomIds()));
        }
    }
}
