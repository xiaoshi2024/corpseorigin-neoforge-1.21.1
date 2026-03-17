package com.phagens.corpseorigin.voice;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 简化版语音触发系统
 * 使用语音模板匹配，玩家可以录制自己的语音
 */
public class SimpleVoiceTrigger {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    
    // 音量阈值（用于检测是否有声音）
    private static final double VOLUME_THRESHOLD = 0.02;
    // 最小录音时间（毫秒）
    private static final int MIN_RECORDING_TIME = 800;
    // 最大录音时间（毫秒）
    private static final int MAX_RECORDING_TIME = 3000;
    
    private static SimpleVoiceTrigger instance;
    private final ExecutorService executor;
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private TargetDataLine microphone;
    
    // 技能触发回调
    private SkillTriggerCallback callback;
    
    public interface SkillTriggerCallback {
        void onTrigger(String skillId);
    }
    
    private SimpleVoiceTrigger() {
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "SimpleVoiceTrigger");
            t.setDaemon(true);
            return t;
        });
    }
    
    public static synchronized SimpleVoiceTrigger getInstance() {
        if (instance == null) {
            instance = new SimpleVoiceTrigger();
        }
        return instance;
    }
    
    /**
     * 设置触发回调
     */
    public void setCallback(SkillTriggerCallback callback) {
        this.callback = callback;
    }
    
    /**
     * 开始录音
     */
    public void startRecording() {
        if (isRecording.get()) {
            return;
        }
        
        executor.submit(() -> {
            try {
                isRecording.set(true);
                recordAudio();
            } catch (Exception e) {
                LOGGER.error("Error during voice recording", e);
            } finally {
                isRecording.set(false);
            }
        });
    }
    
    /**
     * 停止录音
     */
    public void stopRecording() {
        isRecording.set(false);
        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }
    }
    
    /**
     * 是否正在录音
     */
    public boolean isRecording() {
        return isRecording.get();
    }
    
    /**
     * 录制音频
     */
    private void recordAudio() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(
                SAMPLE_RATE,
                SAMPLE_SIZE_IN_BITS,
                CHANNELS,
                SIGNED,
                BIG_ENDIAN
        );
        
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            LOGGER.error("Microphone not supported");
            return;
        }
        
        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        long startTime = System.currentTimeMillis();
        
        LOGGER.info("Started voice recording");
        
        // 显示提示
        showMessage("message.corpseorigin.voice_recording_start");
        
        while (isRecording.get()) {
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            // 检查是否超过最大录音时间
            if (System.currentTimeMillis() - startTime > MAX_RECORDING_TIME) {
                break;
            }
        }
        
        microphone.stop();
        microphone.close();
        
        long recordingTime = System.currentTimeMillis() - startTime;
        byte[] audioData = outputStream.toByteArray();
        
        LOGGER.info("Voice recording finished: {} ms, {} bytes", recordingTime, audioData.length);
        
        // 处理录音
        processRecording(audioData, recordingTime);
    }
    
    /**
     * 处理录音数据
     */
    private void processRecording(byte[] audioData, long recordingTime) {
        if (recordingTime < MIN_RECORDING_TIME) {
            LOGGER.warn("Recording too short: {} ms", recordingTime);
            showMessage("message.corpseorigin.voice_too_short");
            return;
        }
        
        // 计算平均音量
        double volume = calculateVolume(audioData);
        LOGGER.info("Recording volume: {}", volume);
        
        if (volume < VOLUME_THRESHOLD) {
            LOGGER.warn("Recording volume too low: {}", volume);
            showMessage("message.corpseorigin.voice_too_short");
            return;
        }
        
        // 使用语音模板匹配
        VoiceTemplateManager templateManager = VoiceTemplateManager.getInstance();
        
        if (!templateManager.hasTemplates()) {
            // 没有模板，使用默认的音量匹配
            LOGGER.warn("No voice templates found, using default volume matching");
            String matchedKeyword = matchByVolume(audioData);
            if (matchedKeyword != null && callback != null) {
                showVoiceRecognized(matchedKeyword);
                callback.onTrigger(matchedKeyword);
            }
            return;
        }
        
        // 使用模板匹配
        String matchedSkill = templateManager.matchVoice(audioData);
        
        if (matchedSkill != null && callback != null) {
            LOGGER.info("Voice matched template: {}", matchedSkill);
            showVoiceRecognized(matchedSkill);
            callback.onTrigger(matchedSkill);
        } else {
            LOGGER.warn("No template matched");
            showMessage("message.corpseorigin.voice_not_recognized");
        }
    }
    
    /**
     * 基于音量匹配（备用方案）
     */
    private String matchByVolume(byte[] audioData) {
        // 提取简单特征：分段音量
        int segments = 5;
        int bytesPerSegment = audioData.length / segments;
        double[] segmentVolumes = new double[segments];
        
        for (int i = 0; i < segments; i++) {
            int start = i * bytesPerSegment;
            int end = Math.min(start + bytesPerSegment, audioData.length);
            byte[] segment = new byte[end - start];
            System.arraycopy(audioData, start, segment, 0, segment.length);
            segmentVolumes[i] = calculateVolume(segment);
        }
        
        // 计算平均音量
        double avgVolume = 0;
        for (double v : segmentVolumes) {
            avgVolume += v;
        }
        avgVolume /= segments;
        
        // 计算音量趋势
        double trend = segmentVolumes[segments - 1] - segmentVolumes[0];
        
        LOGGER.info("Volume matching - avg: {}, trend: {}", 
                String.format("%.4f", avgVolume),
                String.format("%.4f", trend));
        
        // 根据音量特征匹配技能
        if (avgVolume > 0.08) {
            return "berserk";
        } else if (avgVolume > 0.06 && trend > 0.01) {
            return "giant_strength";
        } else if (avgVolume > 0.05) {
            return "sharp_claws";
        } else {
            return "berserk";
        }
    }
    
    /**
     * 计算音频音量
     */
    private double calculateVolume(byte[] audioData) {
        if (audioData.length == 0) return 0.0;
        
        int bytesPerSample = SAMPLE_SIZE_IN_BITS / 8;
        int sampleCount = audioData.length / bytesPerSample;
        
        double sum = 0;
        for (int i = 0; i < sampleCount; i++) {
            int byteIndex = i * bytesPerSample;
            int low = audioData[byteIndex] & 0xff;
            int high = audioData[byteIndex + 1] << 8;
            short sample = (short) (high | low);
            sum += Math.abs(sample);
        }
        
        double average = sum / sampleCount;
        return average / 32768.0; // 归一化到 0-1
    }
    
    /**
     * 显示识别的语音文字
     */
    private void showVoiceRecognized(String skillName) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.execute(() -> {
                String displayName = getSkillDisplayName(skillName);
                minecraft.player.displayClientMessage(
                        Component.literal("§e[语音] §f" + displayName), true);
            });
        }
    }
    
    /**
     * 获取技能的显示名称
     */
    private String getSkillDisplayName(String skillName) {
        return switch (skillName.toLowerCase()) {
            case "berserk" -> "§c§l狂化";
            case "hardened_skin" -> "§7硬化皮肤";
            case "sharp_claws" -> "§4锐利爪牙";
            case "devour_enhancement" -> "§2吞噬强化";
            case "evolution_sense" -> "§a进化感知";
            case "giant_strength" -> "§c巨人力量";
            case "heavy_strike" -> "§8重击";
            case "swift_movement" -> "§b迅捷移动";
            case "leap" -> "§a飞跃";
            case "evasion" -> "§e闪避";
            case "venom" -> "§2剧毒";
            case "regeneration" -> "§d再生";
            case "fear_aura" -> "§5恐惧光环";
            case "immortal_body" -> "§6不朽之身";
            case "corpse_king_power" -> "§4§l尸王之力";
            case "shadow_strike" -> "§8暗影突袭";
            default -> skillName;
        };
    }
    
    /**
     * 显示消息
     */
    private void showMessage(String translationKey) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.execute(() -> {
                minecraft.player.displayClientMessage(
                        Component.translatable(translationKey), true);
            });
        }
    }
}
