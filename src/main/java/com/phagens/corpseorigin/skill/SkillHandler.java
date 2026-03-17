package com.phagens.corpseorigin.skill;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.network.SyncSkillDataPacket;
import com.phagens.corpseorigin.player.PlayerCorpseData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * 技能处理器实现 - 管理玩家的技能状态
 */
public class SkillHandler implements ISkillHandler {
    
    private static final String NBT_LEARNED_SKILLS = "learned_skills";
    private static final String NBT_EVOLUTION_POINTS = "evolution_points";
    private static final String NBT_COOLDOWNS = "cooldowns";
    private static final String NBT_ACTIVE_SKILLS = "active_skills";
    
    private final Player player;
    private final Set<ResourceLocation> learnedSkills = new HashSet<>();
    private int evolutionPoints = 0;
    private final Map<ResourceLocation, Integer> cooldowns = new HashMap<>();
    private final Set<ResourceLocation> activeSkills = new HashSet<>();
    
    private boolean dirty = false;
    
    public SkillHandler(Player player) {
        this.player = player;
    }
    
    /**
     * 获取或创建玩家的技能处理器
     */
    public static ISkillHandler getOrCreate(Player player) {
        // 从玩家数据附件中获取
        return player.getData(SkillAttachment.SKILL_HANDLER);
    }
    
    @Override
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public Set<ISkill> getLearnedSkills() {
        Set<ISkill> skills = new HashSet<>();
        for (ResourceLocation id : learnedSkills) {
            ISkill skill = SkillManager.getInstance().getSkill(id);
            if (skill != null) {
                skills.add(skill);
            }
        }
        return Collections.unmodifiableSet(skills);
    }
    
    @Override
    public Set<ResourceLocation> getLearnedSkillIds() {
        return Collections.unmodifiableSet(learnedSkills);
    }
    
    @Override
    public boolean hasLearned(ISkill skill) {
        return learnedSkills.contains(skill.getId());
    }
    
    @Override
    public boolean hasLearned(ResourceLocation skillId) {
        return learnedSkills.contains(skillId);
    }
    
    @Override
    public LearnResult learnSkill(ISkill skill) {
        if (skill == null) {
            return LearnResult.UNKNOWN_SKILL;
        }
        
        // 检查是否已学习
        if (hasLearned(skill)) {
            return LearnResult.ALREADY_LEARNED;
        }
        
        // 检查是否是尸兄
        if (!PlayerCorpseData.isCorpse(player)) {
            return LearnResult.NOT_CORPSE;
        }
        
        // 检查等级要求
        int playerLevel = PlayerCorpseData.getEvolutionLevel(player);
        if (playerLevel < skill.getRequiredLevel()) {
            return LearnResult.LEVEL_TOO_LOW;
        }
        
        // 检查前置技能
        for (ResourceLocation prereq : skill.getPrerequisites()) {
            if (!hasLearned(prereq)) {
                return LearnResult.MISSING_PREREQUISITES;
            }
        }
        
        // 检查进化点数
        if (evolutionPoints < skill.getCost()) {
            return LearnResult.INSUFFICIENT_POINTS;
        }
        
        // 检查互斥技能
        for (ISkill learnedSkill : getLearnedSkills()) {
            if (isMutuallyExclusive(skill, learnedSkill)) {
                return LearnResult.MUTUALLY_EXCLUSIVE;
            }
        }
        
        // 学习技能
        try {
            learnedSkills.add(skill.getId());
            evolutionPoints -= skill.getCost();
            skill.onLearn(player);
            
            // 如果是被动技能，立即激活
            if (skill.isPassive()) {
                activeSkills.add(skill.getId());
                skill.onActivate(player);
            }
            
            dirty = true;
            syncToClient();
            
            CorpseOrigin.LOGGER.debug("玩家 {} 学习了技能 {}", player.getName().getString(), skill.getId());
            return LearnResult.SUCCESS;
        } catch (Exception e) {
            CorpseOrigin.LOGGER.error("学习技能 {} 时发生错误", skill.getId(), e);
            return LearnResult.ERROR;
        }
    }
    
    @Override
    public LearnResult learnSkill(ResourceLocation skillId) {
        ISkill skill = SkillManager.getInstance().getSkill(skillId);
        if (skill == null) {
            return LearnResult.UNKNOWN_SKILL;
        }
        return learnSkill(skill);
    }
    
    @Override
    public boolean forgetSkill(ISkill skill) {
        if (skill == null || !hasLearned(skill)) {
            return false;
        }
        
        // 返还进化点数
        learnedSkills.remove(skill.getId());
        evolutionPoints += skill.getCost();
        
        // 如果技能处于激活状态，停用它
        if (activeSkills.contains(skill.getId())) {
            activeSkills.remove(skill.getId());
        }
        
        // 移除冷却
        cooldowns.remove(skill.getId());
        
        skill.onForget(player);
        
        dirty = true;
        syncToClient();
        
        CorpseOrigin.LOGGER.debug("玩家 {} 遗忘了技能 {}", player.getName().getString(), skill.getId());
        return true;
    }
    
    @Override
    public boolean forgetSkill(ResourceLocation skillId) {
        ISkill skill = SkillManager.getInstance().getSkill(skillId);
        if (skill == null) {
            return false;
        }
        return forgetSkill(skill);
    }
    
    @Override
    public void forgetAllSkills() {
        // 返还所有进化点数
        for (ISkill skill : getLearnedSkills()) {
            evolutionPoints += skill.getCost();
            skill.onForget(player);
        }
        
        learnedSkills.clear();
        activeSkills.clear();
        cooldowns.clear();
        
        dirty = true;
        syncToClient();
        
        CorpseOrigin.LOGGER.debug("玩家 {} 遗忘了所有技能", player.getName().getString());
    }
    
    @Override
    public boolean canLearn(ISkill skill) {
        if (skill == null) return false;
        if (hasLearned(skill)) return false;
        if (!PlayerCorpseData.isCorpse(player)) return false;
        if (PlayerCorpseData.getEvolutionLevel(player) < skill.getRequiredLevel()) return false;
        if (evolutionPoints < skill.getCost()) return false;
        
        // 检查前置技能
        for (ResourceLocation prereq : skill.getPrerequisites()) {
            if (!hasLearned(prereq)) return false;
        }
        
        // 检查互斥
        for (ISkill learnedSkill : getLearnedSkills()) {
            if (isMutuallyExclusive(skill, learnedSkill)) return false;
        }
        
        return true;
    }
    
    @Override
    public int getEvolutionPoints() {
        return evolutionPoints;
    }
    
    @Override
    public int getUsedEvolutionPoints() {
        int used = 0;
        for (ISkill skill : getLearnedSkills()) {
            used += skill.getCost();
        }
        return used;
    }
    
    @Override
    public int getTotalEvolutionPoints() {
        return evolutionPoints + getUsedEvolutionPoints();
    }
    
    @Override
    public void addEvolutionPoints(int points) {
        if (points > 0) {
            evolutionPoints += points;
            dirty = true;
            syncToClient();
            CorpseOrigin.LOGGER.debug("玩家 {} 获得 {} 进化点数", player.getName().getString(), points);
        }
    }
    
    @Override
    public boolean consumeEvolutionPoints(int points) {
        if (points <= 0) return true;
        if (evolutionPoints >= points) {
            evolutionPoints -= points;
            dirty = true;
            syncToClient();
            return true;
        }
        return false;
    }
    
    @Override
    public void setEvolutionPoints(int points) {
        this.evolutionPoints = Math.max(0, points);
        dirty = true;
        syncToClient();
    }
    
    @Override
    public List<ISkill> getActiveSkills() {
        List<ISkill> skills = new ArrayList<>();
        for (ResourceLocation id : activeSkills) {
            ISkill skill = SkillManager.getInstance().getSkill(id);
            if (skill != null) {
                skills.add(skill);
            }
        }
        return Collections.unmodifiableList(skills);
    }
    
    @Override
    public boolean activateSkill(ISkill skill) {
        if (skill == null || !hasLearned(skill)) {
            return false;
        }
        
        // 检查冷却
        if (isOnCooldown(skill)) {
            return false;
        }
        
        // 激活技能
        skill.onActivate(player);
        
        // 设置冷却
        if (skill.getCooldown() > 0) {
            setCooldown(skill, skill.getCooldown());
        }
        
        // 如果是持续技能，添加到激活列表
        if (skill.getDuration() > 0) {
            activeSkills.add(skill.getId());
        }
        
        dirty = true;
        return true;
    }
    
    @Override
    public boolean activateSkill(ResourceLocation skillId) {
        ISkill skill = SkillManager.getInstance().getSkill(skillId);
        if (skill == null) {
            return false;
        }
        return activateSkill(skill);
    }
    
    @Override
    public int getCooldownRemaining(ISkill skill) {
        return cooldowns.getOrDefault(skill.getId(), 0);
    }
    
    @Override
    public int getCooldownRemaining(ResourceLocation skillId) {
        return cooldowns.getOrDefault(skillId, 0);
    }
    
    @Override
    public void setCooldown(ISkill skill, int cooldown) {
        if (cooldown > 0) {
            cooldowns.put(skill.getId(), cooldown);
        } else {
            cooldowns.remove(skill.getId());
        }
        dirty = true;
    }
    
    @Override
    public boolean isOnCooldown(ISkill skill) {
        return cooldowns.getOrDefault(skill.getId(), 0) > 0;
    }
    
    @Override
    public String getCannotLearnReason(ISkill skill) {
        LearnResult result = learnSkill(skill);
        return result.getMessage();
    }
    
    @Override
    public void syncToClient() {
        if (player instanceof ServerPlayer serverPlayer) {
            SyncSkillDataPacket packet = new SyncSkillDataPacket(
                player.getId(),
                new ArrayList<>(learnedSkills),
                evolutionPoints,
                new HashMap<>(cooldowns)
            );
            PacketDistributor.sendToPlayer(serverPlayer, packet);
        }
    }
    
    /**
     * 从同步数据加载（客户端使用）
     */
    public void loadFromSyncData(List<ResourceLocation> skills, int points, Map<ResourceLocation, Integer> cds) {
        learnedSkills.clear();
        learnedSkills.addAll(skills);
        
        evolutionPoints = points;
        
        cooldowns.clear();
        cooldowns.putAll(cds);
    }
    
    @Override
    public void loadFromNBT(CompoundTag tag) {
        learnedSkills.clear();
        cooldowns.clear();
        activeSkills.clear();
        
        // 加载已学习技能
        if (tag.contains(NBT_LEARNED_SKILLS)) {
            ListTag skillsList = tag.getList(NBT_LEARNED_SKILLS, 8); // 8 = StringTag
            for (int i = 0; i < skillsList.size(); i++) {
                String skillId = skillsList.getString(i);
                learnedSkills.add(ResourceLocation.parse(skillId));
            }
        }
        
        // 加载进化点数
        evolutionPoints = tag.getInt(NBT_EVOLUTION_POINTS);
        
        // 加载冷却
        if (tag.contains(NBT_COOLDOWNS)) {
            CompoundTag cooldownsTag = tag.getCompound(NBT_COOLDOWNS);
            for (String key : cooldownsTag.getAllKeys()) {
                cooldowns.put(ResourceLocation.parse(key), cooldownsTag.getInt(key));
            }
        }
        
        // 加载激活的技能
        if (tag.contains(NBT_ACTIVE_SKILLS)) {
            ListTag activeList = tag.getList(NBT_ACTIVE_SKILLS, 8);
            for (int i = 0; i < activeList.size(); i++) {
                activeSkills.add(ResourceLocation.parse(activeList.getString(i)));
            }
        }
    }
    
    @Override
    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        
        // 保存已学习技能
        ListTag skillsList = new ListTag();
        for (ResourceLocation skillId : learnedSkills) {
            skillsList.add(StringTag.valueOf(skillId.toString()));
        }
        tag.put(NBT_LEARNED_SKILLS, skillsList);
        
        // 保存进化点数
        tag.putInt(NBT_EVOLUTION_POINTS, evolutionPoints);
        
        // 保存冷却
        CompoundTag cooldownsTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Integer> entry : cooldowns.entrySet()) {
            cooldownsTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put(NBT_COOLDOWNS, cooldownsTag);
        
        // 保存激活的技能
        ListTag activeList = new ListTag();
        for (ResourceLocation skillId : activeSkills) {
            activeList.add(StringTag.valueOf(skillId.toString()));
        }
        tag.put(NBT_ACTIVE_SKILLS, activeList);
        
        return tag;
    }
    
    /**
     * 更新冷却（每tick调用）
     */
    public void updateCooldowns() {
        Iterator<Map.Entry<ResourceLocation, Integer>> iterator = cooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ResourceLocation, Integer> entry = iterator.next();
            int newCooldown = entry.getValue() - 1;
            if (newCooldown <= 0) {
                iterator.remove();
                dirty = true;
            } else {
                entry.setValue(newCooldown);
            }
        }
    }
    
    /**
     * 检查两个技能是否互斥
     */
    private boolean isMutuallyExclusive(ISkill skill1, ISkill skill2) {
        // 检查技能节点互斥
        for (ISkillNode node : SkillManager.getInstance().getAllNodes()) {
            if (node.containsSkill(skill1) && node.containsSkill(skill2)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否需要同步到客户端
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * 标记为已同步
     */
    public void markClean() {
        dirty = false;
    }
}
