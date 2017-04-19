package avalon.util;

import avalon.api.util.GroupMessage;
import avalon.group.BaseGroupMessageResponder;

import java.util.regex.Pattern;

/**
 * Created by Eldath Ray on 2017/3/24.
 *
 * @author Eldath Ray
 */
public abstract class BaseGameResponder extends BaseGroupMessageResponder {
    public abstract void doPost(GroupMessage message);

    public String getHelpMessage() {
        return "";
    }

    public abstract Pattern getKeyWordRegex();

    public abstract String getGameHelpMessage();
}
