package edu.rice.comp504.model.res;

import edu.rice.comp504.model.obj.Message;

import java.util.List;

/**
 * Response when sending user chat history.
 */
public class UserChatHistoryResponse extends AResponse {
    List<Message> chatHistory;

    /**
     * Constructor.
     * @param chatHistory list of messages
     */
    public UserChatHistoryResponse(List<Message> chatHistory) {
        super("UserChatHistory");
        this.chatHistory = chatHistory;
    }

}
