package edu.rice.comp504.model.cmd;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import edu.rice.comp504.model.obj.ChatRoom;
import edu.rice.comp504.model.obj.User;

public class JoinRoomCmd implements IUserCmd {
    private ChatRoom chatRoom;
    private User user;

    /**
     * Constructor.
     * @param chatRoom chatRoom
     * @param user user
     */
    public JoinRoomCmd(ChatRoom chatRoom, User user) {
        this.chatRoom = chatRoom;
        this.user = user;
    }


    /**
     * Helper function get user.
     * @return user
     */
    public User getUser(){
        return user;
    }

    /**
     * Helper function get chatRoom.
     * @return chatRoom
     */
    public ChatRoom getChatRoom(){
        return chatRoom;
    }

    /**
     * Helper function set chatRoom.
     * @param chatRoom chatRoom
     */
    public void setChatRoom(ChatRoom chatRoom){
        this.chatRoom = chatRoom;
    }

    /**
     * Helper function set user.
     * @param user user
     */
    public void setUser(User user){
        this.user = user;
    }

    /**
     * Execute the command.
     * @param context user
     */
    @Override
    public void execute(User context) {
        if (context.getId() == user.getId() && chatRoom.applyFilter(context)) {
            chatRoom.addUser(context);
            context.moveToJoined(chatRoom);
        }
    }
}
