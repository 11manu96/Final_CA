package edu.rice.comp504.model.res;

import edu.rice.comp504.model.obj.Message;

import java.util.List;

public class UserChatHistoryResponse extends AResponse {
    List<Message> chatHistory;

    /**
     * Constructor.
     */
    public UserChatHistoryResponse(List<Message> chatHistory) {
        super("UserChatHistoryResponse");
        this.chatHistory = chatHistory;
    }

}
