package com.phagens.corpseorigin.voice;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 语音触发配置
 */
public class VoiceTriggerConfig {
    public static final ModConfigSpec SPEC;
    public static final VoiceTriggerConfig INSTANCE;

    public final ModConfigSpec.DoubleValue similarityThreshold;
    public final ModConfigSpec.DoubleValue silenceThreshold;
    public final ModConfigSpec.IntValue bufferSize;
    public final ModConfigSpec.IntValue minFramesForMatch;
    public final ModConfigSpec.EnumValue<Mode> continuousMonitoring;

    public enum Mode {
        OFF,
        ON
    }

    static {
        Pair<VoiceTriggerConfig, ModConfigSpec> pair = new ModConfigSpec.Builder()
                .configure(VoiceTriggerConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    private VoiceTriggerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Voice Trigger Configuration").push("voice");

        similarityThreshold = builder
                .comment("Similarity threshold (considered a match if similarity exceeds this value)")
                .defineInRange("similarityThreshold", 0.75, 0.0, 1.0);

        silenceThreshold = builder
                .comment("Silence detection threshold (dB)")
                .defineInRange("silenceThreshold", -43.0, -100.0, 0.0);

        bufferSize = builder
                .comment("Buffer size (bytes)")
                .defineInRange("bufferSize", 4096, 1024, 16384);

        minFramesForMatch = builder
                .comment("Minimum frame for match")
                .defineInRange("minFramesForMatch", 3, 1, 16);

        continuousMonitoring = builder
                .comment("Continuously monitor microphone input and trigger automatically")
                .defineEnum("continuousMonitoring", Mode.ON);

        builder.pop();
    }
}
