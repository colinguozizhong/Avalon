package avalon.friend;

import avalon.api.util.FriendMessage;
import avalon.extend.Recorder;

/**
 * Created by Eldath Ray on 2017/4/1 0001.
 *
 * @author Eldath Ray
 */
public class MainFriendMessageHandler {
    private static MainFriendMessageHandler ourInstance = new MainFriendMessageHandler();

    public static MainFriendMessageHandler getInstance() {
        return ourInstance;
    }

    public void handle(FriendMessage message) {
//        if (MessageChecker.checkEncode(message)) return;
        Recorder.getInstance().recodeFriendMessage(message);
        //MessageHooker.handle(message);
    }
}
