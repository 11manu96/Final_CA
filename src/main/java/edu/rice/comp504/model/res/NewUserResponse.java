package edu.rice.comp504.model.res;

public class NewUserResponse extends AResponse {
    private int userId;
    private String userName;

    public NewUserResponse(int userId, String userName) {
        super("NewUser");
        this.userId = userId;
        this.userName = userName;
    }

}
