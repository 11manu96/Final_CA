package edu.rice.comp504.model.cmd;

import edu.rice.comp504.model.obj.User;

import java.util.Map;

public class CollectNamesCmd implements IUserCmd {
    private static Map<Integer, String> names;
    private static CollectNamesCmd singletoncollectnamescmd;
    private CollectNamesCmd() {
        this.names=null;
    }
    public static CollectNamesCmd makeCmd() {
        if (singletoncollectnamescmd == null ) {
            singletoncollectnamescmd = new CollectNamesCmd();
        }

        return singletoncollectnamescmd;
    }

    @Override
    public void execute(User context) {
        names.put(context.getId(),context.getName());
    }

    public static Map<Integer, String> getNames() {
        return names;
    }
}
