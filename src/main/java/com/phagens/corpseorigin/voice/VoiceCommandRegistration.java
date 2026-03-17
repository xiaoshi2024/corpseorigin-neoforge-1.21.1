package com.phagens.corpseorigin.voice;

import com.phagens.corpseorigin.CorpseOrigin;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * 语音命令注册
 */
@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class VoiceCommandRegistration {
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        VoiceConfigCommand.register(event.getDispatcher());
        CorpseOrigin.LOGGER.info("注册语音配置命令");
    }
}
