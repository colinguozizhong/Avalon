package avalon.tool.database;

import avalon.tool.system.RunningDataSystem;
import avalon.util.FriendMessage;
import avalon.util.GroupMessage;
import org.eclipse.jetty.util.UrlEncoded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;

/**
 * Created by Eldath Ray on 2017/4/19 0019.
 *
 * @author Eldath Ray
 */
public class BasicDatabaseOperator implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(BasicDatabaseOperator.class);
	private static int groupId = Integer.parseInt(RunningDataSystem.getInstance().get("groupId"));
	private static int friendId = Integer.parseInt(RunningDataSystem.getInstance().get("friendId"));

    private static final BasicDatabaseOperator instance = new BasicDatabaseOperator();

    public static BasicDatabaseOperator getInstance() {
        return instance;
    }

    private BasicDatabaseOperator() {
    }

    public boolean add(Statement statement, GroupMessage message) {
        try {
            String value = "('" +
                    message.getTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace("T", " ") +
                    "', " + message.getSenderUid() + ", '" + message.getSenderNickName() +
                    "', " + message.getGroupUid() + ", '" + message.getGroupName() + "', '" +
                    UrlEncoded.encodeString(message.getContent()) + "')";
            statement.executeUpdate(
                    "INSERT INTO group_ (time, senderUid, senderNickname, groupUid, groupName, content) VALUES " + value);
        } catch (SQLException e) {
            logger.warn("Error while saving group message to database: ", e);
            return false;
        }
        return true;
    }

    public boolean add(Statement statement, FriendMessage message) {
        try {
            String value = "('" +
                    message.getTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace("T", " ") +
                    "', " + message.getSenderUid() + ", '" + message.getSenderNickName() + "', '" +
                    UrlEncoded.encodeString(message.getContent()) + "')";
            statement.executeUpdate(
                    "INSERT INTO friend_ (time, senderUid, senderNickname, content) VALUES " + value);
        } catch (SQLException e) {
            logger.warn("Error while saving friend message to SQLite: ", e);
            return false;
        }
        return true;
    }

    public void close() {
	    RunningDataSystem.getInstance().set("groupId", String.valueOf(groupId));
	    RunningDataSystem.getInstance().set("friendId", String.valueOf(friendId));
	    RunningDataSystem.getInstance().save();
    }
}
