// ==================================================================
// This file is part of Player API.
//
// Player API is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// Player API is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License and the GNU General Public License along with Player API.
// If not, see <http://www.gnu.org/licenses/>.
// ==================================================================
package api.player.client;

import api.player.asm.interfaces.IClientPlayerEntity;
import com.mojang.datafixers.util.Either;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeBook;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tags.Tag;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public abstract class ClientPlayerEntityBase
{
    private final ClientPlayerAPI internalClientPlayerAPI;
    protected final IClientPlayerEntity iClientPlayerEntity;
    protected final ClientPlayerEntity playerEntity;

    public ClientPlayerEntityBase(ClientPlayerAPI playerAPI)
    {
        this.internalClientPlayerAPI = playerAPI;
        this.iClientPlayerEntity = playerAPI.iClientPlayerEntity;
        this.playerEntity = (ClientPlayerEntity) playerAPI.iClientPlayerEntity;
    }

    public Object dynamic(String key, Object[] parameters)
    {
        return this.internalClientPlayerAPI.dynamicOverwritten(key, parameters, this);
    }

    @Override
    public final int hashCode()
    {
        return super.hashCode();
    }

    public void beforeBaseAttach(boolean onTheFly)
    {
    }

    public void afterBaseAttach(boolean onTheFly)
    {
    }

    public void beforeLocalConstructing(Minecraft paramMinecraft, World paramWorld, ClientPlayNetHandler paramNetHandlerPlayClient, StatisticsManager paramStatisticsManager, RecipeBook paramRecipeBook)
    {
    }

    public void afterLocalConstructing(Minecraft paramMinecraft, World paramWorld, ClientPlayNetHandler paramNetHandlerPlayClient, StatisticsManager paramStatisticsManager, RecipeBook paramRecipeBook)
    {
    }

    public void beforeBaseDetach(boolean onTheFly)
    {
    }

    public void afterBaseDetach(boolean onTheFly)
    {
    }

    // ############################################################################

    public void beforeAddExhaustion(float exhaustion)
    {
    }

    public void addExhaustion(float exhaustion)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenAddExhaustion(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superAddExhaustion(exhaustion);
        } else if (overwritten != this) {
            overwritten.addExhaustion(exhaustion);
        }
    }

    public void afterAddExhaustion(float exhaustion)
    {
    }

    // ############################################################################

    public void beforeAddExperienceLevel(int levels)
    {
    }

    public void addExperienceLevel(int levels)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenAddExperienceLevel(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superAddExperienceLevel(levels);
        } else if (overwritten != this) {
            overwritten.addExperienceLevel(levels);
        }
    }

    public void afterAddExperienceLevel(int levels)
    {
    }

    // ############################################################################

    public void beforeAddMovementStat(double x, double y, double z)
    {
    }

    public void addMovementStat(double x, double y, double z)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenAddMovementStat(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superAddMovementStat(x, y, z);
        } else if (overwritten != this) {
            overwritten.addMovementStat(x, y, z);
        }
    }

    public void afterAddMovementStat(double x, double y, double z)
    {
    }

    // ############################################################################

    public void beforeAddStat(Stat<?> stat, int amount)
    {
    }

    public void addStat(Stat<?> stat, int amount)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenAddStat(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superAddStat(stat, amount);
        } else if (overwritten != this) {
            overwritten.addStat(stat, amount);
        }
    }

    public void afterAddStat(Stat<?> stat, int amount)
    {
    }

    // ############################################################################

    public void beforeAreEyesInFluid(Tag<Fluid> fluid, boolean checkChunkLoaded)
    {
    }

    public boolean areEyesInFluid(Tag<Fluid> fluid, boolean checkChunkLoaded)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenAreEyesInFluid(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superAreEyesInFluid(fluid, checkChunkLoaded);
        } else if (overwritten != this) {
            return overwritten.areEyesInFluid(fluid, checkChunkLoaded);
        } else {
            return false;
        }
    }

    public void afterAreEyesInFluid(Tag<Fluid> fluid, boolean checkChunkLoaded)
    {
    }

    // ############################################################################

    public void beforeAttackEntityFrom(DamageSource source, float amount)
    {
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenAttackEntityFrom(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.realAttackEntityFrom(source, amount);
        } else if (overwritten != this) {
            return overwritten.attackEntityFrom(source, amount);
        } else {
            return false;
        }
    }

    public void afterAttackEntityFrom(DamageSource source, float amount)
    {
    }

    // ############################################################################

    public void beforeAttackTargetEntityWithCurrentItem(Entity targetEntity)
    {
    }

    public void attackTargetEntityWithCurrentItem(Entity targetEntity)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenAttackTargetEntityWithCurrentItem(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superAttackTargetEntityWithCurrentItem(targetEntity);
        } else if (overwritten != this) {
            overwritten.attackTargetEntityWithCurrentItem(targetEntity);
        }
    }

    public void afterAttackTargetEntityWithCurrentItem(Entity targetEntity)
    {
    }

    // ############################################################################

    public void beforeCanBreatheUnderwater()
    {
    }

    public boolean canBreatheUnderwater()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenCanBreatheUnderwater(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superCanBreatheUnderwater();
        } else if (overwritten != this) {
            return overwritten.canBreatheUnderwater();
        } else {
            return false;
        }
    }

    public void afterCanBreatheUnderwater()
    {
    }

    // ############################################################################

    public void beforeCanHarvestBlock(BlockState state)
    {
    }

    public boolean canHarvestBlock(BlockState state)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenCanHarvestBlock(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superCanHarvestBlock(state);
        } else if (overwritten != this) {
            return overwritten.canHarvestBlock(state);
        } else {
            return false;
        }
    }

    public void afterCanHarvestBlock(BlockState state)
    {
    }

    // ############################################################################

    public void beforeCanPlayerEdit(BlockPos pos, Direction facing, ItemStack stack)
    {
    }

    public boolean canPlayerEdit(BlockPos pos, Direction facing, ItemStack stack)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenCanPlayerEdit(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superCanPlayerEdit(pos, facing, stack);
        } else if (overwritten != this) {
            return overwritten.canPlayerEdit(pos, facing, stack);
        } else {
            return false;
        }
    }

    public void afterCanPlayerEdit(BlockPos pos, Direction facing, ItemStack stack)
    {
    }

    // ############################################################################

    public void beforeCanTriggerWalking()
    {
    }

    public boolean canTriggerWalking()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenCanTriggerWalking(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superCanTriggerWalking();
        } else if (overwritten != this) {
            return overwritten.canTriggerWalking();
        } else {
            return false;
        }
    }

    public void afterCanTriggerWalking()
    {
    }

    // ############################################################################

    public void beforeCloseScreen()
    {
    }

    public void closeScreen()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenCloseScreen(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.realCloseScreen();
        } else if (overwritten != this) {
            overwritten.closeScreen();
        }
    }

    public void afterCloseScreen()
    {
    }

    // ############################################################################

    public void beforeDamageEntity(DamageSource source, float amount)
    {
    }

    public void damageEntity(DamageSource source, float amount)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenDamageEntity(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.realDamageEntity(source, amount);
        } else if (overwritten != this) {
            overwritten.damageEntity(source, amount);
        }
    }

    public void afterDamageEntity(DamageSource source, float amount)
    {
    }

    // ############################################################################

    public void beforeDropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem)
    {
    }

    public ItemEntity dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenDropItem(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superDropItem(droppedItem, dropAround, traceItem);
        } else if (overwritten != this) {
            return overwritten.dropItem(droppedItem, dropAround, traceItem);
        } else {
            return null;
        }
    }

    public void afterDropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem)
    {
    }

    // ############################################################################

    public void beforeGetAIMoveSpeed()
    {
    }

    public float getAIMoveSpeed()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenGetAIMoveSpeed(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superGetAIMoveSpeed();
        } else if (overwritten != this) {
            return overwritten.getAIMoveSpeed();
        } else {
            return 0;
        }
    }

    public void afterGetAIMoveSpeed()
    {
    }

    // ############################################################################

    public void beforeGetBrightness()
    {
    }

    public float getBrightness()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenGetBrightness(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superGetBrightness();
        } else if (overwritten != this) {
            return overwritten.getBrightness();
        } else {
            return 0;
        }
    }

    public void afterGetBrightness()
    {
    }

    // ############################################################################

    public void beforeGetDigSpeed(BlockState state, BlockPos pos)
    {
    }

    public float getDigSpeed(BlockState state, BlockPos pos)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenGetDigSpeed(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superGetDigSpeed(state, pos);
        } else if (overwritten != this) {
            return overwritten.getDigSpeed(state, pos);
        } else {
            return 0;
        }
    }

    public void afterGetDigSpeed(BlockState state, BlockPos pos)
    {
    }

    // ############################################################################

    public void beforeGetDistanceSq(double x, double y, double z)
    {
    }

    public double getDistanceSq(double x, double y, double z)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenGetDistanceSq(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superGetDistanceSq(x, y, z);
        } else if (overwritten != this) {
            return overwritten.getDistanceSq(x, y, z);
        } else {
            return 0;
        }
    }

    public void afterGetDistanceSq(double x, double y, double z)
    {
    }

    // ############################################################################

    public void beforeGetDistanceSqToEntity(Entity entity)
    {
    }

    public double getDistanceSqToEntity(Entity entity)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenGetDistanceSqToEntity(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superGetDistanceSq(entity);
        } else if (overwritten != this) {
            return overwritten.getDistanceSqToEntity(entity);
        } else {
            return 0;
        }
    }

    public void afterGetDistanceSqToEntity(Entity entity)
    {
    }

    // ############################################################################

    public void beforeGetDistanceSqVec(Vec3d pos)
    {
    }

    public double getDistanceSqVec(Vec3d pos)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenGetDistanceSqVec(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superGetDistanceSq(pos);
        } else if (overwritten != this) {
            return overwritten.getDistanceSqVec(pos);
        } else {
            return 0;
        }
    }

    public void afterGetDistanceSqVec(Vec3d pos)
    {
    }

    // ############################################################################

    public void beforeGetFovModifier()
    {
    }

    public float getFovModifier()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenGetFovModifier(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superGetFovModifier();
        } else if (overwritten != this) {
            return overwritten.getFovModifier();
        } else {
            return 0;
        }
    }

    public void afterGetFovModifier()
    {
    }

    // ############################################################################

    public void beforeGetHurtSound(DamageSource source)
    {
    }

    public SoundEvent getHurtSound(DamageSource source)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenGetHurtSound(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superGetHurtSound(source);
        } else if (overwritten != this) {
            return overwritten.getHurtSound(source);
        } else {
            return null;
        }
    }

    public void afterGetHurtSound(DamageSource source)
    {
    }

    // ############################################################################

    public void beforeGetName()
    {
    }

    public ITextComponent getName()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenGetName(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superGetName();
        } else if (overwritten != this) {
            return overwritten.getName();
        } else {
            return null;
        }
    }

    public void afterGetName()
    {
    }

    // ############################################################################

    public void beforeGetSize(Pose pose)
    {
    }

    public EntitySize getSize(Pose pose)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenGetSize(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superGetSize(pose);
        } else if (overwritten != this) {
            return overwritten.getSize(pose);
        } else {
            return null;
        }
    }

    public void afterGetSize(Pose pose)
    {
    }

    // ############################################################################

    public void beforeGetSleepTimer()
    {
    }

    public int getSleepTimer()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenGetSleepTimer(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superGetSleepTimer();
        } else if (overwritten != this) {
            return overwritten.getSleepTimer();
        } else {
            return 0;
        }
    }

    public void afterGetSleepTimer()
    {
    }

    // ############################################################################

    public void beforeGetStandingEyeHeight(Pose pose, EntitySize size)
    {
    }

    public float getStandingEyeHeight(Pose pose, EntitySize size)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenGetStandingEyeHeight(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superGetStandingEyeHeight(pose, size);
        } else if (overwritten != this) {
            return overwritten.getStandingEyeHeight(pose, size);
        } else {
            return 0;
        }
    }

    public void afterGetStandingEyeHeight(Pose pose, EntitySize size)
    {
    }

    // ############################################################################

    public void beforeGiveExperiencePoints(int points)
    {
    }

    public void giveExperiencePoints(int points)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenGiveExperiencePoints(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superGiveExperiencePoints(points);
        } else if (overwritten != this) {
            overwritten.giveExperiencePoints(points);
        }
    }

    public void afterGiveExperiencePoints(int points)
    {
    }

    // ############################################################################

    public void beforeHandleWaterMovement()
    {
    }

    public boolean handleWaterMovement()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenHandleWaterMovement(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superHandleWaterMovement();
        } else if (overwritten != this) {
            return overwritten.handleWaterMovement();
        } else {
            return false;
        }
    }

    public void afterHandleWaterMovement()
    {
    }

    // ############################################################################

    public void beforeHeal(float amount)
    {
    }

    public void heal(float amount)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenHeal(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.realHeal(amount);
        } else if (overwritten != this) {
            overwritten.heal(amount);
        }
    }

    public void afterHeal(float amount)
    {
    }

    // ############################################################################

    public void beforeIsEntityInsideOpaqueBlock()
    {
    }

    public boolean isEntityInsideOpaqueBlock()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenIsEntityInsideOpaqueBlock(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superIsEntityInsideOpaqueBlock();
        } else if (overwritten != this) {
            return overwritten.isEntityInsideOpaqueBlock();
        } else {
            return false;
        }
    }

    public void afterIsEntityInsideOpaqueBlock()
    {
    }

    // ############################################################################

    public void beforeIsInWater()
    {
    }

    public boolean isInWater()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenIsInWater(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superIsInWater();
        } else if (overwritten != this) {
            return overwritten.isInWater();
        } else {
            return false;
        }
    }

    public void afterIsInWater()
    {
    }

    // ############################################################################

    public void beforeIsOnLadder()
    {
    }

    public boolean isOnLadder()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenIsOnLadder(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superIsOnLadder();
        } else if (overwritten != this) {
            return overwritten.isOnLadder();
        } else {
            return false;
        }
    }

    public void afterIsOnLadder()
    {
    }

    // ############################################################################

    public void beforeIsShiftKeyDown()
    {
    }

    public boolean isShiftKeyDown()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenIsShiftKeyDown(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.realIsShiftKeyDown();
        } else if (overwritten != this) {
            return overwritten.isShiftKeyDown();
        } else {
            return false;
        }
    }

    public void afterIsShiftKeyDown()
    {
    }

    // ############################################################################

    public void beforeIsSleeping()
    {
    }

    public boolean isSleeping()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenIsSleeping(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superIsSleeping();
        } else if (overwritten != this) {
            return overwritten.isSleeping();
        } else {
            return false;
        }
    }

    public void afterIsSleeping()
    {
    }

    // ############################################################################

    public void beforeIsSprinting()
    {
    }

    public boolean isSprinting()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenIsSprinting(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superIsSprinting();
        } else if (overwritten != this) {
            return overwritten.isSprinting();
        } else {
            return false;
        }
    }

    public void afterIsSprinting()
    {
    }

    // ############################################################################

    public void beforeJump()
    {
    }

    public void jump()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenJump(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superJump();
        } else if (overwritten != this) {
            overwritten.jump();
        }
    }

    public void afterJump()
    {
    }

    // ############################################################################

    public void beforeKnockBack(Entity entity, float strength, double xRatio, double zRatio)
    {
    }

    public void knockBack(Entity entity, float strength, double xRatio, double zRatio)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenKnockBack(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superKnockBack(entity, strength, xRatio, zRatio);
        } else if (overwritten != this) {
            overwritten.knockBack(entity, strength, xRatio, zRatio);
        }
    }

    public void afterKnockBack(Entity entity, float strength, double xRatio, double zRatio)
    {
    }

    // ############################################################################

    public void beforeLivingTick()
    {
    }

    public void livingTick()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenLivingTick(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.realLivingTick();
        } else if (overwritten != this) {
            overwritten.livingTick();
        }
    }

    public void afterLivingTick()
    {
    }

    // ############################################################################

    public void beforeMove(MoverType type, Vec3d pos)
    {
    }

    public void move(MoverType type, Vec3d pos)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenMove(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superMove(type, pos);
        } else if (overwritten != this) {
            overwritten.move(type, pos);
        }
    }

    public void afterMove(MoverType type, Vec3d pos)
    {
    }

    // ############################################################################

    public void beforeMoveRelative(float friction, Vec3d relative)
    {
    }

    public void moveRelative(float friction, Vec3d relative)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenMoveRelative(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superMoveRelative(friction, relative);
        } else if (overwritten != this) {
            overwritten.moveRelative(friction, relative);
        }
    }

    public void afterMoveRelative(float friction, Vec3d relative)
    {
    }

    // ############################################################################

    public void beforeOnDeath(DamageSource cause)
    {
    }

    public void onDeath(DamageSource cause)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenOnDeath(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superOnDeath(cause);
        } else if (overwritten != this) {
            overwritten.onDeath(cause);
        }
    }

    public void afterOnDeath(DamageSource cause)
    {
    }

    // ############################################################################

    public void beforeOnKillEntity(LivingEntity entity)
    {
    }

    public void onKillEntity(LivingEntity entity)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenOnKillEntity(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superOnKillEntity(entity);
        } else if (overwritten != this) {
            overwritten.onKillEntity(entity);
        }
    }

    public void afterOnKillEntity(LivingEntity entity)
    {
    }

    // ############################################################################

    public void beforeOnLivingFall(float distance, float damageMultiplier)
    {
    }

    public boolean onLivingFall(float distance, float damageMultiplier)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenOnLivingFall(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superOnLivingFall(distance, damageMultiplier);
        } else if (overwritten != this) {
            return overwritten.onLivingFall(distance, damageMultiplier);
        } else {
            return false;
        }
    }

    public void afterOnLivingFall(float distance, float damageMultiplier)
    {
    }

    // ############################################################################

    public void beforeOnStruckByLightning(LightningBoltEntity lightningBolt)
    {
    }

    public void onStruckByLightning(LightningBoltEntity lightningBolt)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenOnStruckByLightning(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superOnStruckByLightning(lightningBolt);
        } else if (overwritten != this) {
            overwritten.onStruckByLightning(lightningBolt);
        }
    }

    public void afterOnStruckByLightning(LightningBoltEntity lightningBolt)
    {
    }

    // ############################################################################

    public void beforePick(double blockReachDistance, float partialTicks, boolean anyFluid)
    {
    }

    public RayTraceResult pick(double blockReachDistance, float partialTicks, boolean anyFluid)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenPick(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superPick(blockReachDistance, partialTicks, anyFluid);
        } else if (overwritten != this) {
            return overwritten.pick(blockReachDistance, partialTicks, anyFluid);
        } else {
            return null;
        }
    }

    public void afterPick(double blockReachDistance, float partialTicks, boolean anyFluid)
    {
    }

    // ############################################################################

    public void beforePlayStepSound(BlockPos pos, BlockState block)
    {
    }

    public void playStepSound(BlockPos pos, BlockState block)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenPlayStepSound(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superPlayStepSound(pos, block);
        } else if (overwritten != this) {
            overwritten.playStepSound(pos, block);
        }
    }

    public void afterPlayStepSound(BlockPos pos, BlockState block)
    {
    }

    // ############################################################################

    public void beforePushOutOfBlocks(double x, double y, double z)
    {
    }

    public void pushOutOfBlocks(double x, double y, double z)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenPushOutOfBlocks(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.realPushOutOfBlocks(x, y, z);
        } else if (overwritten != this) {
            overwritten.pushOutOfBlocks(x, y, z);
        }
    }

    public void afterPushOutOfBlocks(double x, double y, double z)
    {
    }

    // ############################################################################

    public void beforeRead(CompoundNBT compound)
    {
    }

    public void read(CompoundNBT compound)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenRead(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superRead(compound);
        } else if (overwritten != this) {
            overwritten.read(compound);
        }
    }

    public void afterRead(CompoundNBT compound)
    {
    }

    // ############################################################################

    public void beforeRecalculateSize()
    {
    }

    public void recalculateSize()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenRecalculateSize(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superRecalculateSize();
        } else if (overwritten != this) {
            overwritten.recalculateSize();
        }
    }

    public void afterRecalculateSize()
    {
    }

    // ############################################################################

    public void beforeRemove()
    {
    }

    public void remove()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenRemove(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superRemove();
        } else if (overwritten != this) {
            overwritten.remove();
        }
    }

    public void afterRemove()
    {
    }

    // ############################################################################

    public void beforeRespawnPlayer()
    {
    }

    public void respawnPlayer()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenRespawnPlayer(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.realRespawnPlayer();
        } else if (overwritten != this) {
            overwritten.respawnPlayer();
        }
    }

    public void afterRespawnPlayer()
    {
    }

    // ############################################################################

    public void beforeSetPlayerSPHealth(float health)
    {
    }

    public void setPlayerSPHealth(float health)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenSetPlayerSPHealth(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.realSetPlayerSPHealth(health);
        } else if (overwritten != this) {
            overwritten.setPlayerSPHealth(health);
        }
    }

    public void afterSetPlayerSPHealth(float health)
    {
    }

    // ############################################################################

    public void beforeSetPosition(double x, double y, double z)
    {
    }

    public void setPosition(double x, double y, double z)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenSetPosition(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superSetPosition(x, y, z);
        } else if (overwritten != this) {
            overwritten.setPosition(x, y, z);
        }
    }

    public void afterSetPosition(double x, double y, double z)
    {
    }

    // ############################################################################

    public void beforeSetPositionAndRotation(double x, double y, double z, float yaw, float pitch)
    {
    }

    public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenSetPositionAndRotation(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superSetPositionAndRotation(x, y, z, yaw, pitch);
        } else if (overwritten != this) {
            overwritten.setPositionAndRotation(x, y, z, yaw, pitch);
        }
    }

    public void afterSetPositionAndRotation(double x, double y, double z, float yaw, float pitch)
    {
    }

    // ############################################################################

    public void beforeSetSneaking(boolean sneaking)
    {
    }

    public void setSneaking(boolean sneaking)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenSetSneaking(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superSetSneaking(sneaking);
        } else if (overwritten != this) {
            overwritten.setSneaking(sneaking);
        }
    }

    public void afterSetSneaking(boolean sneaking)
    {
    }

    // ############################################################################

    public void beforeSetSprinting(boolean sprinting)
    {
    }

    public void setSprinting(boolean sprinting)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenSetSprinting(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superSetSprinting(sprinting);
        } else if (overwritten != this) {
            overwritten.setSprinting(sprinting);
        }
    }

    public void afterSetSprinting(boolean sprinting)
    {
    }

    // ############################################################################

    public void beforeStartRiding(Entity entity, boolean force)
    {
    }

    public boolean startRiding(Entity entity, boolean force)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenStartRiding(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.realStartRiding(entity, force);
        } else if (overwritten != this) {
            return overwritten.startRiding(entity, force);
        } else {
            return false;
        }
    }

    public void afterStartRiding(Entity entity, boolean force)
    {
    }

    // ############################################################################

    public void beforeSwingArm(Hand hand)
    {
    }

    public void swingArm(Hand hand)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenSwingArm(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.realSwingArm(hand);
        } else if (overwritten != this) {
            overwritten.swingArm(hand);
        }
    }

    public void afterSwingArm(Hand hand)
    {
    }

    // ############################################################################

    public void beforeTick()
    {
    }

    public void tick()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenTick(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.realTick();
        } else if (overwritten != this) {
            overwritten.tick();
        }
    }

    public void afterTick()
    {
    }

    // ############################################################################

    public void beforeTravel(Vec3d pos)
    {
    }

    public void travel(Vec3d pos)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenTravel(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superTravel(pos);
        } else if (overwritten != this) {
            overwritten.travel(pos);
        }
    }

    public void afterTravel(Vec3d pos)
    {
    }

    // ############################################################################

    public void beforeTrySleep(BlockPos at)
    {
    }

    public Either<PlayerEntity.SleepResult, Unit> trySleep(BlockPos at)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenTrySleep(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superTrySleep(at);
        } else if (overwritten != this) {
            return overwritten.trySleep(at);
        } else {
            return null;
        }
    }

    public void afterTrySleep(BlockPos at)
    {
    }

    // ############################################################################

    public void beforeUpdateEntityActionState()
    {
    }

    public void updateEntityActionState()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenUpdateEntityActionState(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.realUpdateEntityActionState();
        } else if (overwritten != this) {
            overwritten.updateEntityActionState();
        }
    }

    public void afterUpdateEntityActionState()
    {
    }

    // ############################################################################

    public void beforeUpdatePotionEffects()
    {
    }

    public void updatePotionEffects()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenUpdatePotionEffects(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superUpdatePotionEffects();
        } else if (overwritten != this) {
            overwritten.updatePotionEffects();
        }
    }

    public void afterUpdatePotionEffects()
    {
    }

    // ############################################################################

    public void beforeUpdateRidden()
    {
    }

    public void updateRidden()
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenUpdateRidden(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.realUpdateRidden();
        } else if (overwritten != this) {
            overwritten.updateRidden();
        }
    }

    public void afterUpdateRidden()
    {
    }

    // ############################################################################

    public void beforeWakeUpPlayer(boolean immediately, boolean updateWorldFlag)
    {
    }

    public void wakeUpPlayer(boolean immediately, boolean updateWorldFlag)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenWakeUpPlayer(this);

        if (overwritten == null) {
            this.iClientPlayerEntity.superWakeUpPlayer(immediately, updateWorldFlag);
        } else if (overwritten != this) {
            overwritten.wakeUpPlayer(immediately, updateWorldFlag);
        }
    }

    public void afterWakeUpPlayer(boolean immediately, boolean updateWorldFlag)
    {
    }

    // ############################################################################

    public void beforeWriteWithoutTypeId(CompoundNBT compound)
    {
    }

    public CompoundNBT writeWithoutTypeId(CompoundNBT compound)
    {
        ClientPlayerEntityBase overwritten = this.internalClientPlayerAPI.getOverwrittenWriteWithoutTypeId(this);

        if (overwritten == null) {
            return this.iClientPlayerEntity.superWriteWithoutTypeId(compound);
        } else if (overwritten != this) {
            return overwritten.writeWithoutTypeId(compound);
        } else {
            return null;
        }
    }

    public void afterWriteWithoutTypeId(CompoundNBT compound)
    {
    }
}
