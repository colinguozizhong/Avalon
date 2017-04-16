package avalon.group;

import avalon.data.ConfigSystem;
import avalon.extend.BaseGameResponder;
import avalon.extend.Recorder;
import avalon.main.MessageChecker;
import avalon.tool.APIRateLimit;
import avalon.tool.APISurvivePool;
import avalon.tool.ConstantPool;
import avalon.tool.VariablePool;
import avalon.util.GroupMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.LongStream;

import static avalon.tool.ObjectCaster.toLongArray;
import static avalon.tool.ObjectCaster.toStringArray;

/**
 * Created by Eldath Ray on 2017/3/30.
 *
 * @author Eldath Ray
 */
public class MainGroupMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MainGroupMessageHandler.class);
    private static final Map<Pattern, BaseGroupMessageResponder> apiList = new LinkedHashMap<>();
    private static final Map<Pattern, BaseGameResponder> gameApiList = new LinkedHashMap<>();
    private static MainGroupMessageHandler instance = new MainGroupMessageHandler();

    private static long[] adminUid = toLongArray(ConfigSystem
            .getInstance().getConfigArray("Admin_Uid"));
    private static LongStream adminUidStream = Arrays.stream(adminUid);

    private static long[] followGroup = toLongArray(ConfigSystem
            .getInstance().getConfigArray("Follow_Group_Uid"));
    private static LongStream followGroupStream = Arrays.stream(followGroup);

    private static long[] gameModeAllowedGroup = toLongArray(ConfigSystem
            .getInstance().getConfigArray("Game_Mode_Enabled_Group_Uid"));
    private static long[] recordGroup = toLongArray(ConfigSystem
            .getInstance().getConfigArray("Record_Group_Uid"));
    private static long[] blackListPeople = toLongArray(ConfigSystem
            .getInstance().getConfigArray("BlackList_Uid"));
    private static Map<Long, Integer> blackListPeopleMap = new HashMap<>();
    private static String[] blockList = toStringArray(ConfigSystem
            .getInstance().getConfigArray("Block_Words"));
    private static final int punishFrequency = (int) ConfigSystem.getInstance()
            .getConfig("Block_Words_Punish_Frequency");
    private static final APIRateLimit cooling = new APIRateLimit(3000L);

    static Map<Pattern, BaseGroupMessageResponder> getApiList() {
        return apiList;
    }

    public static MainGroupMessageHandler getInstance() {
        return instance;
    }

    private MainGroupMessageHandler() {
        for (long thisBlackPeople : blackListPeople)
            blackListPeopleMap.put(thisBlackPeople, punishFrequency + 1);
    }

    static BaseGroupMessageResponder getGroupResponderByKeyword(String keyword) {
        for (Map.Entry<Pattern, BaseGroupMessageResponder> patternAPIEntry : apiList.entrySet()) {
            Pattern key = patternAPIEntry.getKey();
            BaseGroupMessageResponder value = patternAPIEntry.getValue();
            if (key.matcher(keyword).find())
                return value;
        }
        return null;
    }

    public void handle(GroupMessage message) {
        Recorder.getInstance().recodeGroupMessage(message);
        long groupUid = message.getGroupUid();
        String content = message.getContent();
        String lowerContent = content.toLowerCase();
        String sender = message.getSenderNickName();
        long senderUid = message.getSenderUid();
        boolean admin = adminUidStream.anyMatch(e -> e == senderUid);
        if (followGroupStream.anyMatch(e -> e == groupUid)) {
            if (!preCheck(lowerContent)) return;
            if (!admin && ConstantPool.Setting.Block_Words_Punishment_Mode_Enabled)
                if (blackListPeopleMap.containsKey(senderUid))
                    if (blackListPeopleMap.get(senderUid) >= punishFrequency) {
                        message.response("@" + sender +
                                " 您的帐号由于发送过多不允许关键字，现已被屏蔽~o(╯□╰)o！");
                        return;
                    } else blackListPeopleMap.put(senderUid, 0);
            for (String thisBlockString : blockList)
                if (content.replace(" ", "").contains(thisBlockString)) {
                    String notice = "您发送的消息含有不允许的关键词！";
                    if (ConstantPool.Setting.Block_Words_Punishment_Mode_Enabled) {
                        notice = "您发送的消息含有不允许的关键词，注意：" + punishFrequency +
                                "次发送不允许关键词后帐号将被屏蔽！⊙﹏⊙!";
                        blackListPlus(senderUid);
                    }
                    message.response("@" + sender + " " + notice);
                    return;
                }
            for (Map.Entry<Pattern, BaseGroupMessageResponder> patternAPIEntry : apiList.entrySet()) {
                BaseGroupMessageResponder value = patternAPIEntry.getValue();
                if (doCheck(patternAPIEntry.getKey(), value, message)) {
                    if (!MessageChecker.checkEncode(message))
                        value.doPost(message);
                    return;
                }
            }
            if (ConstantPool.GameMode.IsEnabled)
                if (Arrays.stream(gameModeAllowedGroup).anyMatch(e -> e == groupUid))
                    for (Map.Entry<Pattern, BaseGameResponder> gameApiListEntry : gameApiList.entrySet())
                        if (gameApiListEntry.getKey().matcher(content).find()) {
                            if (!MessageChecker.checkEncode(message))
                                gameApiListEntry.getValue().doPost(message);
                            return;
                        }
        }
        if (Arrays.stream(recordGroup).anyMatch(e -> e == groupUid))
            Recorder.getInstance().recodeGroupMessage(message);
    }

    private boolean doCheck(Pattern key, BaseGroupMessageResponder value, GroupMessage groupMessage) {
        String lowerContent = groupMessage.getContent().toLowerCase();
        long time = groupMessage.getTimeLong();
        String sender = groupMessage.getSenderNickName();
        if (!key.matcher(lowerContent).find()) return false;
        if (!cooling.trySet(time)) {
            if (!VariablePool.Limit_Noticed) {
                groupMessage.response("@" + sender +
                        " 对不起，您的指令超频。3s内仅能有一次指令输入，未到3s内的输入将被忽略。注意：此消息仅会显示一次。");
                VariablePool.Limit_Noticed = true;
            }
            return false;
        }
        if (!APISurvivePool.getInstance().isSurvive(value)) {
            if (!APISurvivePool.getInstance().isNoticed(value)) {
                groupMessage.response("@" + sender + " 对不起，您调用的指令响应器目前已被停止；" +
                        "注意：此消息仅会显示一次。");
                APISurvivePool.getInstance().setNoticed(value);
            }
            return false;
        }
        return true;
    }

    private boolean preCheck(String lowerContent) {
        return gameApiList.keySet().stream().anyMatch(e -> e.matcher(lowerContent).find()) ||
                apiList.keySet().stream().anyMatch(e -> e.matcher(lowerContent).find());
    }

    private void blackListPlus(long senderUid) {
        int pastValue = blackListPeopleMap.get(senderUid);
        blackListPeopleMap.put(senderUid, ++pastValue);
    }

    static long[] getAdminUid() {
        return adminUid;
        //FIXME 是否需要adminUid.clone()以防止数据修改？
    }

    public static long[] getFollowGroup() {
        return followGroup;
    }

    static int getPunishFrequency() {
        return punishFrequency;
    }

    static Map<Long, Integer> getSetBlackListPeopleMap() {
        return blackListPeopleMap;
    }
}
