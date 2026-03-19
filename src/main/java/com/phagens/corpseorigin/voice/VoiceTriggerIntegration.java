package com.phagens.corpseorigin.voice;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import com.phagens.corpseorigin.skill.ISkill;
import com.phagens.corpseorigin.skill.ISkillHandler;
import com.phagens.corpseorigin.skill.SkillAttachment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.HashMap;
import java.util.Map;

/**
 * VoiceTrigger 集成 - 内置语音触发系统（简化版）
 * 基于 Java 内置音频 API，不使用深度学习
 */
@OnlyIn(Dist.CLIENT)
public class VoiceTriggerIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 语音监听按键（按住V键进行语音录制）
     */
    public static final KeyMapping VOICE_LISTEN = new KeyMapping(
            "key.corpseorigin.voice_listen",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.corpseorigin"
    );

    private static boolean isVoiceListenKeyPressed = false;

    // 技能触发映射
    private static final Map<String, SkillTrigger> skillTriggers = new HashMap<>();

    /**
     * 技能触发信息
     */
    public static class SkillTrigger {
        public final String skillId;
        public final String voiceKeyword;

        public SkillTrigger(String skillId, String voiceKeyword) {
            this.skillId = skillId;
            this.voiceKeyword = voiceKeyword;
        }
    }

    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing simplified VoiceTrigger integration");

        // 注册配置
        modContainer.registerConfig(ModConfig.Type.CLIENT, VoiceTriggerConfig.SPEC);

        // 注册事件监听器
        modEventBus.addListener(VoiceTriggerIntegration::clientSetup);
        modEventBus.addListener(VoiceTriggerIntegration::registerKeyMapping);

        // 注册游戏事件监听器
        NeoForge.EVENT_BUS.addListener(VoiceTriggerIntegration::onClientTick);

        // 设置技能触发回调
        SimpleVoiceTrigger.getInstance().setCallback(VoiceTriggerIntegration::onVoiceSkillTriggered);

        LOGGER.info("VoiceTrigger integration initialized successfully");
    }

    private static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(VOICE_LISTEN);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Setting up simplified VoiceTrigger client components");
    }

    /**
     * 注册技能语音触发
     *
     * @param skillId     技能ID
     * @param voiceKeyword 语音关键词（用于识别）
     */
    public static void registerSkillTrigger(String skillId, String voiceKeyword) {
        skillTriggers.put(skillId, new SkillTrigger(skillId, voiceKeyword));
        LOGGER.info("Registered voice trigger for skill '{}': keyword='{}'", skillId, voiceKeyword);
    }

    /**
     * 当语音触发技能时调用
     */
    private static void onVoiceSkillTriggered(String keyword) {
        LOGGER.info("Voice skill triggered with keyword: {}", keyword);
        
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            LOGGER.warn("Player is null, cannot trigger skill");
            return;
        }

        // 获取玩家技能处理器
        ISkillHandler skillHandler = SkillAttachment.getSkillHandler(minecraft.player);
        if (skillHandler == null) {
            LOGGER.warn("Skill handler is null, cannot trigger skill");
            return;
        }

        // 查找匹配的技能
        String skillId = findSkillByKeyword(keyword);
        LOGGER.info("Found skill ID: {} for keyword: {}", skillId, keyword);
        
        if (skillId == null) {
            LOGGER.warn("Voice keyword '{}' not matched to any skill", keyword);
            minecraft.player.displayClientMessage(
                    Component.translatable("message.corpseorigin.voice_not_recognized"), true);
            return;
        }

        // 将字符串技能ID转换为ResourceLocation
        ResourceLocation skillRL = ResourceLocation.tryParse(skillId);
        if (skillRL == null) {
            LOGGER.warn("Invalid skill ID format: '{}'", skillId);
            return;
        }

        // 检查玩家是否已学习该技能
        if (!skillHandler.hasLearned(skillRL)) {
            LOGGER.warn("Voice triggered skill '{}' but player doesn't have this skill", skillId);
            minecraft.player.displayClientMessage(
                    Component.translatable("message.corpseorigin.voice_skill_not_unlocked"), true);
            return;
        }

        // 触发技能
        if (skillHandler.activateSkill(skillRL)) {
            LOGGER.info("Successfully activated skill '{}' via voice trigger", skillId);
            // 获取技能名称用于显示
            ISkill skill = null;
            for (ISkill s : skillHandler.getLearnedSkills()) {
                if (s.getId().equals(skillRL)) {
                    skill = s;
                    break;
                }
            }
            String skillName = skill != null ? skill.getName().getString() : skillId;
            minecraft.player.displayClientMessage(
                    Component.translatable("message.corpseorigin.voice_skill_activated", skillName), true);
        } else {
            LOGGER.warn("Failed to activate skill '{}' via voice trigger", skillId);
            minecraft.player.displayClientMessage(
                    Component.translatable("message.corpseorigin.voice_skill_failed"), true);
        }
    }

    /**
     * 根据关键词查找技能ID
     */
    private static String findSkillByKeyword(String keyword) {
        // 技能关键词到技能ID的映射
        Map<String, String> keywordToSkillId = new HashMap<>();
        keywordToSkillId.put("硬化皮肤", "hardened_skin");
        keywordToSkillId.put("利爪", "sharp_claws");
        keywordToSkillId.put("吞噬", "devour_enhancement");
        keywordToSkillId.put("感知", "evolution_sense");
        keywordToSkillId.put("巨力", "giant_strength");
        keywordToSkillId.put("狂暴", "berserk");
        keywordToSkillId.put("重击", "heavy_strike");
        keywordToSkillId.put("疾行", "swift_movement");
        keywordToSkillId.put("跳跃", "leap");
        keywordToSkillId.put("闪避", "evasion");
        keywordToSkillId.put("毒液", "venom");
        keywordToSkillId.put("再生", "regeneration");
        keywordToSkillId.put("恐惧", "fear_aura");
        keywordToSkillId.put("不死", "immortal_body");
        keywordToSkillId.put("尸王", "corpse_king_power");
        keywordToSkillId.put("影袭", "shadow_strike");
        
        // 直接匹配关键词
        String skillId = keywordToSkillId.get(keyword);
        if (skillId != null) {
            return "corpseorigin:" + skillId;
        }
        
        // 尝试部分匹配
        for (Map.Entry<String, String> entry : keywordToSkillId.entrySet()) {
            if (keyword.contains(entry.getKey()) || entry.getKey().contains(keyword)) {
                return "corpseorigin:" + entry.getValue();
            }
        }
        
        // 默认返回
        return "corpseorigin:" + keyword;
    }

    /**
     * 客户端tick处理
     */
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        // 确保不在任何 GUI 界面中
        if (minecraft.screen == null) {
            boolean keyPressed = VOICE_LISTEN.isDown();

            if (keyPressed && !isVoiceListenKeyPressed) {
                // 按键刚被按下，开始录音
                isVoiceListenKeyPressed = true;
                SimpleVoiceTrigger.getInstance().startRecording();
                LOGGER.info("Voice listen key pressed, started recording");
            } else if (!keyPressed && isVoiceListenKeyPressed) {
                // 按键刚被释放，停止录音
                isVoiceListenKeyPressed = false;
                SimpleVoiceTrigger.getInstance().stopRecording();
                LOGGER.info("Voice listen key released, stopped recording");
            }
        } else {
            // 如果打开了 GUI，释放按键状态并停止录音
            if (isVoiceListenKeyPressed) {
                isVoiceListenKeyPressed = false;
                SimpleVoiceTrigger.getInstance().stopRecording();
            }
        }
    }

    /**
     * 检查语音系统是否可用
     */
    public static boolean isVoiceSystemAvailable() {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            boolean available = AudioSystem.isLineSupported(info);
            LOGGER.info("Voice system availability check: {}", available);
            return available;
        } catch (Exception e) {
            LOGGER.error("Error checking voice system availability", e);
            return false;
        }
    }
}
