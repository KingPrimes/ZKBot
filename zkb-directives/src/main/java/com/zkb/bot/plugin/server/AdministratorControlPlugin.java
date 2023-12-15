package com.zkb.bot.plugin.server;


import com.mikuac.shiro.annotation.PrivateMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.zkb.bot.domain.BotAdmins;
import com.zkb.bot.enums.AdminControlEnum;
import com.zkb.bot.server.IBotAdminsServer;
import com.zkb.bot.utils.ChackVersionToUpdateJar;
import com.zkb.bot.utils.Msg;
import com.zkb.common.load.LoadConfig;
import com.zkb.common.utils.file.FileUtils;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Locale;

import static com.zkb.bot.enums.AdminControlEnum.*;

@Component
@Shiro
public class AdministratorControlPlugin extends BotPlugin {

    private static final Logger log = LoggerFactory.getLogger(LoadConfig.class);
    private static final String HTML_PATH = "./ZKBotHtml";
    @Autowired
    IBotAdminsServer adminsServer;

    @PrivateMessageHandler
    public int privateMessageHandler(Bot bot, PrivateMessageEvent event) {
        if (event.getRawMessage().trim().isEmpty()) {
            return MESSAGE_IGNORE;
        }

        if (adminsServer.checkIsAdmin(new BotAdmins(bot.getSelfId(), event.getUserId()), true)) {

            if (UPDATE_HTML.getType().equals(event.getRawMessage().toLowerCase(Locale.ROOT))) {
                bot.sendPrivateMsg(event.getUserId(), "正在准备更新Html模板，请稍后...", false);
                try {
                    File file = new File(HTML_PATH);
                    if (FileUtils.delAllFile(HTML_PATH)) {
                        Git.cloneRepository()
                                .setURI("https://gitee.com/KingPrime/ZKBotHtml.git")
                                .setDirectory(file)
                                .call();
                        bot.sendPrivateMsg(event.getUserId(), "模板更新完毕！", false);
                        return MESSAGE_BLOCK;
                    } else {
                        bot.sendPrivateMsg(event.getUserId(), "模板更新失败，请手动删除HTML文件夹。\n并重启程序", false);
                        return MESSAGE_BLOCK;
                    }
                } catch (Exception e) {
                    log.error("下载Html文件失败：{}", e.getMessage());
                    bot.sendPrivateMsg(event.getUserId(), "模板更新失败，错误信息：" + e.getMessage(), false);
                    return MESSAGE_BLOCK;
                }
            }

            if (TYPE_CODE.getType().equals(event.getRawMessage())) {
                Msg msg = new Msg();
                for (AdminControlEnum value : values()) {
                    msg.text(value.getType() + "\n");
                }
                bot.sendPrivateMsg(event.getUserId(), msg.build(), false);
            }

            if (UPDATE_JAR.getType().equals(event.getMessage())) {
                ChackVersionToUpdateJar.update(bot, event.getUserId());
            }
            if (CHACK_VERSION.getType().equals(event.getMessage())) {
                ChackVersionToUpdateJar.chackVersion(bot, event.getUserId());
            }

        } else {
            bot.sendGroupMsg(event.getUserId(), "您不是顶级管理员无法进行此操作！", false);
        }
        return super.onPrivateMessage(bot, event);
    }
}
