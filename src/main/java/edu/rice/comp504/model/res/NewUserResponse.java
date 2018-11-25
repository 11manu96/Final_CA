package edu.rice.comp504.model.res;

/**
 * Response when creating a new user.
 */
public class NewUserResponse extends AResponse {
    private int userId;
    private String userName;

    /**
     * Constructor.
     * @param userId user id
     * @param userName user name
     */
    public NewUserResponse(int userId, String userName) {
        super("NewUser");
        this.userId = userId;
        this.userName = userName;
    }

}
