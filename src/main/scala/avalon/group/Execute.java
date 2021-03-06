package avalon.group;

import avalon.util.GroupConfig;
import avalon.util.GroupMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Created by Eldath Ray on 2017/6/11 0011.
 *
 * @author Eldath Ray
 */
public class Execute implements GroupMessageResponder {
	private static final Logger logger = LoggerFactory.getLogger(Execute.class);
	private static final Execute instance = new Execute();

	@Override
	public void doPost(GroupMessage message, GroupConfig groupConfig) {
/*
		//
		String content = message.getContent();
		String[] lines = content.split("\n");
		String[] split = lines[0].split(" ");
		if (split.length < 3) {
			message.response(AT(message) + " 指令不合法 ⊙﹏⊙!");
			return;
		}
		for (Pattern thisDangerCommand : dangerCommand) {
			for (String thisLine : lines)
				if (thisDangerCommand.matcher(thisLine.trim()
						.toLowerCase()
						.replaceAll("[\\pP\\p{Punct}]", "")).matches()) {
					message.response(AT(message) + " 代码中包含危险字，不允许执行！若有疑问请艾特Eldath~");
					return;
				}
		}
		StringBuilder code = new StringBuilder();
		for (int i = 1; i < lines.length; i++)
			code.append(lines[i]).append("\n");

		String languageString = split[2].trim().toLowerCase();
		ExecutiveLanguage lang = ExecutiveLanguagePool.get(languageString);
		if (lang != null) {
			Map<String, Object> result;
			try {
				result = Executive.execute(lang, code.toString());
			} catch (Exception e) {
				logger.warn("exception thrown while execute code: " + e.toString());
				message.response("Oh no! 代码执行失败：未知错误");
				return;
			}
			String errorOrOut = (String) result.get("out/error");
			if ((boolean) result.get("error"))
				message.response(AT(message) + " Oops! 您的代码执行错误! 错误如下：\n" + errorOrOut);
			else
				message.response(AT(message) + " Yap! 提交的代码已经执行完成。执行输出：\n" + errorOrOut);
		} else {
			message.response(AT(message) + " 指定的语言目前暂不支持~ 抱歉`(*>﹏<*)′");
		}*/
	}

	public static Execute getInstance() {
		return instance;
	}

	@Override
	public String getHelpMessage() {
		return "avalon execute <语言，目前仅支持Python>{换行}<程序，支持多行>：执行指定语言的程序并返回输出";
	}

	@Override
	public Pattern getKeyWordRegex() {
		return Pattern.compile("avalon execute \\w+");
	}

	@Override
	public GroupMessageResponder instance() {
		return null;
	}
}
