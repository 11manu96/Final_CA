package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.User;

/**
 * The Interface IUserCmd provides an interface whenever Observable like Dispatcher or ChatRoom want to notify observers (users)
 */
public interface IUserCmd {
    /**
     * Execute is the function such that all command will execute once the command is passed to observer's update
     * @context a user which the command will operate on
     */
    void execute(User context);
}
