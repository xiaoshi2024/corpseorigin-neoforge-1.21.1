package com.phagens.corpseorigin;

import com.mojang.logging.LogUtils;
import com.phagens.corpseorigin.GongFU.GongFaZL.BaseGongFaItem;
import com.phagens.corpseorigin.GongFU.GongFaZL.GongFaData;
import com.phagens.corpseorigin.GongFU.GongFaZL.GongFaDataFactory;
import com.phagens.corpseorigin.GongFU.JsonLoader.GongFaJsonLoader;
import com.phagens.corpseorigin.GongFU.MenuTypeRegister;
import com.phagens.corpseorigin.GongFU.PackGongFu.NetworkPaketGL;
import com.phagens.corpseorigin.event.player.playerDie;
import com.phagens.corpseorigin.player.CorpsePlayerAttachment;
import com.phagens.corpseorigin.register.*;
import com.phagens.corpseorigin.skill.CorpseEvolutionTree;
import com.phagens.corpseorigin.skill.CorpseSkillTree;
import com.phagens.corpseorigin.skill.CorpseSkills;
import com.phagens.corpseorigin.skill.SkillAttachment;
import com.phagens.corpseorigin.skill.SkillEventHandler;
import com.phagens.corpseorigin.voice.VoiceCommandRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.phagens.corpseorigin.register.Moditems.QI_XING_GUAN_ITEM;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CorpseOrigin.MODID)
public class CorpseOrigin {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "corpseorigin";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
   /*
   // Create a Deferred Register to hold Blocks which will all be registered under the "corpseorigin" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "corpseorigin" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // Creates a new Block with the id "corpseorigin:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "corpseorigin:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // Creates a new food item with the id "corpseorigin:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));
*/
   // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "corpseorigin" namespace
   public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);


    // Creates a creative tab with the id "corpseorigin:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CORPSE_ORIGIN_TAB = CREATIVE_MODE_TABS.register("corpse_origin_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.corpseorigin")) //The language key for title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> QI_XING_GUAN_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                BaseGongFaItem leiXi = (BaseGongFaItem) Moditems.LEI_XI_GONG_FA.get();
                output.accept(QI_XING_GUAN_ITEM.get());
                output.accept(Moditems.BYWATER_BUCKET.get());
                output.accept(Moditems.BYWATER_BOTTLE.get());
                output.accept(Moditems.S_AGENT.get());
                output.accept(Moditems.NULL_S_AGENT.get());

                // 添加刷怪蛋
                output.accept(Moditems.LOWER_LEVEL_ZB_SPAWN_EGG.get());
                output.accept(Moditems.LONGYOU_SPAWN_EGG.get());
                output.accept(Moditems.ZBR_FISH_SPAWN_EGG.get());
                output.accept(Moditems.KAIWEINAI_SPAWN_EGG.get());

                addGongFaItemsToCreativeTab(output);

            }).build());
    /**
     * 将 JSON 加载的所有功法物品添加到创造标签页
     */
    private static void addGongFaItemsToCreativeTab(CreativeModeTab.Output output) {
        BaseGongFaItem leiXi = (BaseGongFaItem) Moditems.LEI_XI_GONG_FA.get();

        // 获取所有加载的功法数据
        Map<String, GongFaData> allData = GongFaJsonLoader.getAllGongFaData();
        if (allData.isEmpty()) {
            // 如果 JSON 还没加载，使用默认数据（向后兼容）
            CorpseOrigin.LOGGER.warn("JSON 功法数据为空");
        }

        // 按稀有度和层数排序，确保显示顺序一致
        List<GongFaData> sortedData = new ArrayList<>(allData.values());
        // 先按稀有度排序
        // 稀有度相同按层数排序
        sortedData.sort(Comparator.comparingInt(GongFaData::getRarity).thenComparing(GongFaData::getCeng));

        // 添加所有功法物品到创造标签页
        for (GongFaData data : sortedData) {
            try {
                ItemStack stack = GongFaDataFactory.createGongFaItem(leiXi, data.getRarity(), data.getCeng());
                output.accept(stack);
                CorpseOrigin.LOGGER.debug("添加功法物品到创造标签页：{} (稀有度：{}, 层数：{})",
                        data.getTypeId(), data.getRarity(), data.getCeng());
            } catch (Exception e) {
                CorpseOrigin.LOGGER.error("创建功法物品失败：{}", data.getTypeId(), e);
            }
        }
        CorpseOrigin.LOGGER.info("已从 JSON 加载 {} 个功法物品到创造标签页", sortedData.size());
    }

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CorpseOrigin(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        //json解析
        NeoForge.EVENT_BUS.addListener(CorpseOrigin::addReloadListeners);

        // 先注册附件系统 - 重要！
        SkillAttachment.ATTACHMENT_TYPES.register(modEventBus);

        Moditems.ITEMS.register(modEventBus);
        BlockRegistry.Blocks.register(modEventBus);
        BlockEntityRegistry.BLOCK_ENTITIES.register(modEventBus);
        EntityRegistry.ENTITIES.register(modEventBus);
        EffectRegister.MOB_EFFECTS.register(modEventBus);
        MenuTypeRegister.MENUS.register(modEventBus);
        CorpsePlayerAttachment.ATTACHMENT_TYPES.register(modEventBus);

        // 注册音效
        ModSounds.register(modEventBus);

        // 初始化技能系统
        CorpseSkills.init();
        CorpseEvolutionTree.init();
        CorpseSkillTree.init();

        NetworkPaketGL.registerPackets(modEventBus);

        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        //死亡移除
        NeoForge.EVENT_BUS.register(playerDie.class);

        // 注册技能事件处理器
        NeoForge.EVENT_BUS.register(SkillEventHandler.class);

        // 注册语音命令
        NeoForge.EVENT_BUS.register(VoiceCommandRegistration.class);

        // 初始化内置语音触发系统（仅在客户端）
        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.phagens.corpseorigin.voice.VoiceTriggerIntegration.init(modEventBus, modContainer);
        }

        // Register ourselves for server and other game events we are interested in.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private static void addReloadListeners(AddReloadListenerEvent event) {
        GongFaJsonLoader.register(event);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));


    }


//    // 将示例块项添加到构建块标签页中
//    private void addCreative(BuildCreativeModeTabContentsEvent event) {
//        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
//            event.accept(QI_XING_GUAN_ITEM);
//        }
//    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
