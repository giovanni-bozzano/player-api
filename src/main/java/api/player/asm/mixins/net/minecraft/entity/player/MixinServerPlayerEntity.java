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
package api.player.asm.mixins.net.minecraft.entity.player;

import api.player.asm.interfaces.IServerPlayerEntity;
import api.player.asm.interfaces.IServerPlayerEntityAccessor;
import api.player.server.ServerPlayerAPI;
import api.player.server.ServerPlayerEntityBase;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.stats.Stat;
import net.minecraft.tags.Tag;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements IServerPlayerEntity, IServerPlayerEntityAccessor
{
    @Mutable
    @Shadow
    @Final
    public PlayerInteractionManager interactionManager;
    private ServerPlayerAPI serverPlayerAPI;
    private boolean callReal;

    public MixinServerPlayerEntity(World world, GameProfile gameProfile)
    {
        super(world, gameProfile);
    }

    @Override
    public ServerPlayerAPI getServerPlayerAPI()
    {
        return this.serverPlayerAPI;
    }

    @Override
    public ServerPlayerEntityBase getServerPlayerBase(String baseId)
    {
        return ServerPlayerAPI.getServerPlayerBase(this, baseId);
    }

    @Override
    public Set<String> getServerPlayerBaseIds()
    {
        return ServerPlayerAPI.getServerPlayerBaseIds(this);
    }

    @Override
    public Object dynamic(String key, Object[] parameters)
    {
        return ServerPlayerAPI.dynamic(this, key, parameters);
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/ServerPlayerEntity;interactionManager:Lnet/minecraft/server/management/PlayerInteractionManager;"))
    public void beforeInit(ServerPlayerEntity redirectedServerPlayerEntity, PlayerInteractionManager redirectedInteractionManager, MinecraftServer minecraftServer, ServerWorld serverWorld, GameProfile gameProfile, PlayerInteractionManager playerInteractionManager)
    {
        this.serverPlayerAPI = ServerPlayerAPI.create(this);
        ServerPlayerAPI.beforeLocalConstructing(this, minecraftServer, serverWorld, gameProfile, playerInteractionManager);
        this.interactionManager = redirectedInteractionManager;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void afterInit(MinecraftServer minecraftServer, ServerWorld serverWorld, GameProfile gameProfile, PlayerInteractionManager playerInteractionManager, CallbackInfo callbackInfo)
    {
        ServerPlayerAPI.afterLocalConstructing(this, minecraftServer, serverWorld, gameProfile, playerInteractionManager);
    }

    // ############################################################################

    @Override
    public void addExhaustion(float exhaustion)
    {
        ServerPlayerAPI.addExhaustion(this, exhaustion);
    }

    @Override
    public void superAddExhaustion(float exhaustion)
    {
        super.addExhaustion(exhaustion);
    }

    // ############################################################################

    @Shadow
    public abstract void addExperienceLevel(int levels);

    @Inject(method = "addExperienceLevel", at = @At("HEAD"), cancellable = true)
    public void beforeAddExperienceLevel(int levels, CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforeAddExperienceLevel(callbackInfo, this, levels);
        }
        this.callReal = false;
    }

    @Inject(method = "addExperienceLevel", at = @At("RETURN"))
    public void afterAddExperienceLevel(int levels, CallbackInfo callbackInfo)
    {
        ServerPlayerAPI.afterAddExperienceLevel(this, levels);
    }

    @Override
    public void realAddExperienceLevel(int levels)
    {
        this.callReal = true;
        this.addExperienceLevel(levels);
    }

    @Override
    public void superAddExperienceLevel(int levels)
    {
        super.addExperienceLevel(levels);
    }

    // ############################################################################

    @Override
    public void addMovementStat(double x, double y, double z)
    {
        ServerPlayerAPI.addMovementStat(this, x, y, z);
    }

    @Override
    public void superAddMovementStat(double x, double y, double z)
    {
        super.addMovementStat(x, y, z);
    }

    // ############################################################################

    @Shadow
    public abstract void addStat(@Nonnull Stat<?> stat, int amount);

    @Inject(method = "addStat", at = @At("HEAD"), cancellable = true)
    public void beforeAddStat(Stat<?> stat, int amount, CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforeAddStat(callbackInfo, this, stat, amount);
        }
        this.callReal = false;
    }

    @Inject(method = "addStat", at = @At("RETURN"))
    public void afterAddStat(Stat<?> stat, int amount, CallbackInfo callbackInfo)
    {
        ServerPlayerAPI.afterAddStat(this, stat, amount);
    }

    @Override
    public void realAddStat(Stat<?> stat, int amount)
    {
        this.callReal = true;
        this.addStat(stat, amount);
    }

    @Override
    public void superAddStat(Stat<?> stat, int amount)
    {
        super.addStat(stat, amount);
    }

    // ############################################################################

    @Override
    public boolean areEyesInFluid(@Nonnull Tag<Fluid> fluid, boolean checkChunkLoaded)
    {
        return ServerPlayerAPI.areEyesInFluid(this, fluid, checkChunkLoaded);
    }

    @Override
    public boolean superAreEyesInFluid(Tag<Fluid> fluid, boolean checkChunkLoaded)
    {
        return super.areEyesInFluid(fluid, checkChunkLoaded);
    }

    // ############################################################################

    @Shadow
    public abstract boolean attackEntityFrom(@Nonnull DamageSource source, float amount);

    @Inject(method = "attackEntityFrom", at = @At("HEAD"), cancellable = true)
    public void beforeAttackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforeAttackEntityFrom(callbackInfo, this, source, amount);
        }
        this.callReal = false;
    }

    @Inject(method = "attackEntityFrom", at = @At("RETURN"))
    public void afterAttackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callbackInfo)
    {
        ServerPlayerAPI.afterAttackEntityFrom(this, source, amount);
    }

    @Override
    public boolean realAttackEntityFrom(DamageSource source, float amount)
    {
        this.callReal = true;
        return this.attackEntityFrom(source, amount);
    }

    @Override
    public boolean superAttackEntityFrom(DamageSource source, float amount)
    {
        return super.attackEntityFrom(source, amount);
    }

    // ############################################################################

    @Shadow
    public abstract void attackTargetEntityWithCurrentItem(@Nonnull Entity targetEntity);

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"), cancellable = true)
    public void beforeAttackTargetEntityWithCurrentItem(Entity targetEntity, CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforeAttackTargetEntityWithCurrentItem(callbackInfo, this, targetEntity);
        }
        this.callReal = false;
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("RETURN"))
    public void afterAttackTargetEntityWithCurrentItem(Entity targetEntity, CallbackInfo callbackInfo)
    {
        ServerPlayerAPI.afterAttackTargetEntityWithCurrentItem(this, targetEntity);
    }

    @Override
    public void realAttackTargetEntityWithCurrentItem(Entity targetEntity)
    {
        this.callReal = true;
        this.attackTargetEntityWithCurrentItem(targetEntity);
    }

    @Override
    public void superAttackTargetEntityWithCurrentItem(Entity targetEntity)
    {
        super.attackTargetEntityWithCurrentItem(targetEntity);
    }

    // ############################################################################

    @Override
    public boolean canBreatheUnderwater()
    {
        return ServerPlayerAPI.canBreatheUnderwater(this);
    }

    @Override
    public boolean superCanBreatheUnderwater()
    {
        return super.canBreatheUnderwater();
    }

    // ############################################################################

    @Override
    public boolean canHarvestBlock(@Nonnull BlockState state)
    {
        return ServerPlayerAPI.canHarvestBlock(this, state);
    }

    @Override
    public boolean superCanHarvestBlock(BlockState state)
    {
        return super.canHarvestBlock(state);
    }

    // ############################################################################

    @Override
    public boolean canPlayerEdit(@Nonnull BlockPos pos, @Nonnull Direction facing, @Nonnull ItemStack stack)
    {
        return ServerPlayerAPI.canPlayerEdit(this, pos, facing, stack);
    }

    @Override
    public boolean superCanPlayerEdit(BlockPos pos, Direction facing, ItemStack stack)
    {
        return super.canPlayerEdit(pos, facing, stack);
    }

    // ############################################################################

    @Override
    protected boolean canTriggerWalking()
    {
        return ServerPlayerAPI.canTriggerWalking(this);
    }

    @Override
    public boolean superCanTriggerWalking()
    {
        return super.canTriggerWalking();
    }

    // ############################################################################

    @Shadow
    public abstract void copyFrom(ServerPlayerEntity that, boolean keepEverything);

    @Inject(method = "copyFrom", at = @At("HEAD"), cancellable = true)
    public void beforeCopyFrom(ServerPlayerEntity that, boolean keepEverything, CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforeCopyFrom(callbackInfo, this, that, keepEverything);
        }
        this.callReal = false;
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    public void afterCopyFrom(ServerPlayerEntity that, boolean keepEverything, CallbackInfo callbackInfo)
    {
        ServerPlayerAPI.afterCopyFrom(this, that, keepEverything);
    }

    @Override
    public void realCopyFrom(ServerPlayerEntity that, boolean keepEverything)
    {
        this.callReal = true;
        this.copyFrom(that, keepEverything);
    }

    // ############################################################################

    @Override
    protected void damageEntity(@Nonnull DamageSource source, float amount)
    {
        ServerPlayerAPI.damageEntity(this, source, amount);
    }

    @Override
    public void superDamageEntity(DamageSource source, float amount)
    {
        super.damageEntity(source, amount);
    }

    // ############################################################################

    @Shadow
    public abstract ItemEntity dropItem(@Nonnull ItemStack droppedItem, boolean dropAround, boolean traceItem);

    @Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    public void beforeDropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem, CallbackInfoReturnable<ItemEntity> callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforeDropItem(callbackInfo, this, droppedItem, dropAround, traceItem);
        }
        this.callReal = false;
    }

    @Inject(method = "dropItem", at = @At("RETURN"))
    public void afterDropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem, CallbackInfoReturnable<ItemEntity> callbackInfo)
    {
        ServerPlayerAPI.afterDropItem(this, droppedItem, dropAround, traceItem);
    }

    @Override
    public ItemEntity realDropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem)
    {
        this.callReal = true;
        return this.dropItem(droppedItem, dropAround, traceItem);
    }

    @Override
    public ItemEntity superDropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem)
    {
        return super.dropItem(droppedItem, dropAround, traceItem);
    }

    // ############################################################################

    @Override
    public float getAIMoveSpeed()
    {
        return ServerPlayerAPI.getAIMoveSpeed(this);
    }

    @Override
    public float superGetAIMoveSpeed()
    {
        return super.getAIMoveSpeed();
    }

    // ############################################################################

    @Override
    public float getBrightness()
    {
        return ServerPlayerAPI.getBrightness(this);
    }

    @Override
    public float superGetBrightness()
    {
        return super.getBrightness();
    }

    // ############################################################################

    @Override
    public float getDigSpeed(@Nonnull BlockState state, @Nullable BlockPos pos)
    {
        return ServerPlayerAPI.getDigSpeed(this, state, pos);
    }

    @Override
    public float superGetDigSpeed(BlockState state, BlockPos pos)
    {
        return super.getDigSpeed(state, pos);
    }

    // ############################################################################

    @Override
    public double getDistanceSq(double x, double y, double z)
    {
        return ServerPlayerAPI.getDistanceSq(this, x, y, z);
    }

    @Override
    public double superGetDistanceSq(double x, double y, double z)
    {
        return super.getDistanceSq(x, y, z);
    }

    // ############################################################################

    @Override
    public double getDistanceSq(@Nonnull Entity entity)
    {
        return ServerPlayerAPI.getDistanceSqToEntity(this, entity);
    }

    @Override
    public double superGetDistanceSq(Entity entity)
    {
        return super.getDistanceSq(entity);
    }

    // ############################################################################

    @Override
    public double getDistanceSq(@Nonnull Vec3d pos)
    {
        return ServerPlayerAPI.getDistanceSqVec(this, pos);
    }

    @Override
    public double superGetDistanceSq(Vec3d pos)
    {
        return super.getDistanceSq(pos);
    }

    // ############################################################################

    @Override
    protected SoundEvent getHurtSound(@Nonnull DamageSource source)
    {
        return ServerPlayerAPI.getHurtSound(this, source);
    }

    @Override
    public SoundEvent superGetHurtSound(DamageSource source)
    {
        return super.getHurtSound(source);
    }

    // ############################################################################

    @Override
    @Nonnull
    public ITextComponent getName()
    {
        return ServerPlayerAPI.getName(this);
    }

    @Override
    public ITextComponent superGetName()
    {
        return super.getName();
    }

    // ############################################################################

    @Override
    @Nonnull
    public EntitySize getSize(@Nonnull Pose pose)
    {
        return ServerPlayerAPI.getSize(this, pose);
    }

    @Override
    public EntitySize superGetSize(Pose pose)
    {
        return super.getSize(pose);
    }

    // ############################################################################

    @Override
    public int getSleepTimer()
    {
        return ServerPlayerAPI.getSleepTimer(this);
    }

    @Override
    public int superGetSleepTimer()
    {
        return super.getSleepTimer();
    }

    // ############################################################################

    @Override
    public float getStandingEyeHeight(@Nonnull Pose pose, @Nonnull EntitySize size)
    {
        return ServerPlayerAPI.getStandingEyeHeight(this, pose, size);
    }

    @Override
    public float superGetStandingEyeHeight(Pose pose, EntitySize size)
    {
        return super.getStandingEyeHeight(pose, size);
    }

    // ############################################################################

    @Shadow
    public abstract void giveExperiencePoints(int points);

    @Inject(method = "giveExperiencePoints", at = @At("HEAD"), cancellable = true)
    public void beforeGiveExperiencePoints(int points, CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforeGiveExperiencePoints(callbackInfo, this, points);
        }
        this.callReal = false;
    }

    @Inject(method = "giveExperiencePoints", at = @At("RETURN"))
    public void afterGiveExperiencePoints(int points, CallbackInfo callbackInfo)
    {
        ServerPlayerAPI.afterGiveExperiencePoints(this, points);
    }

    @Override
    public void realGiveExperiencePoints(int points)
    {
        this.callReal = true;
        this.giveExperiencePoints(points);
    }

    @Override
    public void superGiveExperiencePoints(int points)
    {
        super.giveExperiencePoints(points);
    }

    // ############################################################################

    @Override
    public boolean handleWaterMovement()
    {
        return ServerPlayerAPI.handleWaterMovement(this);
    }

    @Override
    public boolean superHandleWaterMovement()
    {
        return super.handleWaterMovement();
    }

    // ############################################################################

    @Override
    public void heal(float amount)
    {
        ServerPlayerAPI.heal(this, amount);
    }

    @Override
    public void superHeal(float amount)
    {
        super.heal(amount);
    }

    // ############################################################################

    @Override
    public boolean isEntityInsideOpaqueBlock()
    {
        return ServerPlayerAPI.isEntityInsideOpaqueBlock(this);
    }

    @Override
    public boolean superIsEntityInsideOpaqueBlock()
    {
        return super.isEntityInsideOpaqueBlock();
    }

    // ############################################################################

    @Override
    public boolean isInWater()
    {
        return ServerPlayerAPI.isInWater(this);
    }

    @Override
    public boolean superIsInWater()
    {
        return super.isInWater();
    }

    // ############################################################################

    @Override
    public boolean isOnLadder()
    {
        return ServerPlayerAPI.isOnLadder(this);
    }

    @Override
    public boolean superIsOnLadder()
    {
        return super.isOnLadder();
    }

    // ############################################################################

    @Override
    public boolean isShiftKeyDown()
    {
        return ServerPlayerAPI.isShiftKeyDown(this);
    }

    @Override
    public boolean superIsShiftKeyDown()
    {
        return super.isShiftKeyDown();
    }

    // ############################################################################

    @Override
    public boolean isSleeping()
    {
        return ServerPlayerAPI.isSleeping(this);
    }

    @Override
    public boolean superIsSleeping()
    {
        return super.isSleeping();
    }

    // ############################################################################

    @Override
    public boolean isSprinting()
    {
        return ServerPlayerAPI.isSprinting(this);
    }

    @Override
    public boolean superIsSprinting()
    {
        return super.isSprinting();
    }

    // ############################################################################

    @Override
    public void jump()
    {
        ServerPlayerAPI.jump(this);
    }

    @Override
    public void superJump()
    {
        super.jump();
    }

    // ############################################################################

    @Override
    public void knockBack(@Nonnull Entity entity, float strength, double xRatio, double zRatio)
    {
        ServerPlayerAPI.knockBack(this, entity, strength, xRatio, zRatio);
    }

    @Override
    public void superKnockBack(Entity entity, float strength, double xRatio, double zRatio)
    {
        super.knockBack(entity, strength, xRatio, zRatio);
    }

    // ############################################################################

    @Override
    public void livingTick()
    {
        ServerPlayerAPI.livingTick(this);
    }

    @Override
    public void superLivingTick()
    {
        super.livingTick();
    }

    // ############################################################################

    @Override
    public void move(@Nonnull MoverType type, @Nonnull Vec3d pos)
    {
        ServerPlayerAPI.move(this, type, pos);
    }

    @Override
    public void superMove(MoverType type, Vec3d pos)
    {
        super.move(type, pos);
    }

    // ############################################################################

    @Override
    public void moveRelative(float friction, @Nonnull Vec3d relative)
    {
        ServerPlayerAPI.moveRelative(this, friction, relative);
    }

    @Override
    public void superMoveRelative(float friction, Vec3d relative)
    {
        super.moveRelative(friction, relative);
    }

    // ############################################################################

    @Shadow
    public abstract void onDeath(@Nonnull DamageSource cause);

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    public void beforeOnDeath(DamageSource cause, CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforeOnDeath(callbackInfo, this, cause);
        }
        this.callReal = false;
    }

    @Inject(method = "onDeath", at = @At("RETURN"))
    public void afterOnDeath(DamageSource cause, CallbackInfo callbackInfo)
    {
        ServerPlayerAPI.afterOnDeath(this, cause);
    }

    @Override
    public void realOnDeath(DamageSource cause)
    {
        this.callReal = true;
        this.onDeath(cause);
    }

    @Override
    public void superOnDeath(DamageSource cause)
    {
        super.onDeath(cause);
    }

    // ############################################################################

    @Override
    public void onKillEntity(@Nonnull LivingEntity entity)
    {
        ServerPlayerAPI.onKillEntity(this, entity);
    }

    @Override
    public void superOnKillEntity(LivingEntity entity)
    {
        super.onKillEntity(entity);
    }

    // ############################################################################

    @Override
    public boolean onLivingFall(float distance, float damageMultiplier)
    {
        return ServerPlayerAPI.onLivingFall(this, distance, damageMultiplier);
    }

    @Override
    public boolean superOnLivingFall(float distance, float damageMultiplier)
    {
        return super.onLivingFall(distance, damageMultiplier);
    }

    // ############################################################################

    @Override
    public void onStruckByLightning(@Nonnull LightningBoltEntity lightningBolt)
    {
        ServerPlayerAPI.onStruckByLightning(this, lightningBolt);
    }

    @Override
    public void superOnStruckByLightning(LightningBoltEntity lightningBolt)
    {
        super.onStruckByLightning(lightningBolt);
    }

    // ############################################################################

    @Override
    @Nonnull
    public RayTraceResult pick(double blockReachDistance, float partialTicks, boolean anyFluid)
    {
        return ServerPlayerAPI.pick(this, blockReachDistance, partialTicks, anyFluid);
    }

    @Override
    public RayTraceResult superPick(double blockReachDistance, float partialTicks, boolean anyFluid)
    {
        return super.pick(blockReachDistance, partialTicks, anyFluid);
    }

    // ############################################################################

    @Shadow
    public abstract void playerTick();

    @Inject(method = "playerTick", at = @At("HEAD"), cancellable = true)
    public void beforePlayerTick(CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforePlayerTick(callbackInfo, this);
        }
        this.callReal = false;
    }

    @Inject(method = "playerTick", at = @At("RETURN"))
    public void afterPlayerTick(CallbackInfo callbackInfo)
    {
        ServerPlayerAPI.afterPlayerTick(this);
    }

    @Override
    public void realPlayerTick()
    {
        this.callReal = true;
        this.playerTick();
    }

    // ############################################################################

    @Override
    protected void playStepSound(@Nonnull BlockPos pos, @Nonnull BlockState block)
    {
        ServerPlayerAPI.playStepSound(this, pos, block);
    }

    @Override
    public void superPlayStepSound(BlockPos pos, BlockState block)
    {
        super.playStepSound(pos, block);
    }

    // ############################################################################

    @Override
    protected void pushOutOfBlocks(double x, double y, double z)
    {
        ServerPlayerAPI.pushOutOfBlocks(this, x, y, z);
    }

    @Override
    public void superPushOutOfBlocks(double x, double y, double z)
    {
        super.pushOutOfBlocks(x, y, z);
    }

    // ############################################################################

    @Override
    public void read(@Nonnull CompoundNBT compound)
    {
        ServerPlayerAPI.read(this, compound);
    }

    @Override
    public void superRead(CompoundNBT compound)
    {
        super.read(compound);
    }

    // ############################################################################

    @Override
    public void remove()
    {
        ServerPlayerAPI.remove(this);
    }

    @Override
    public void superRemove()
    {
        super.remove();
    }

    // ############################################################################

    @Shadow
    public abstract void setEntityActionState(float strafe, float forward, boolean jumping, boolean sneaking);

    @Inject(method = "setEntityActionState", at = @At("HEAD"), cancellable = true)
    protected void beforeSetEntityActionState(float strafe, float forward, boolean jumping, boolean sneaking, CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforeSetEntityActionState(callbackInfo, this, strafe, forward, jumping, sneaking);
        }
        this.callReal = false;
    }

    @Inject(method = "setEntityActionState", at = @At("RETURN"))
    protected void afterSetEntityActionState(float strafe, float forward, boolean jumping, boolean sneaking, CallbackInfo callbackInfo)
    {
        ServerPlayerAPI.afterSetEntityActionState(this, strafe, forward, jumping, sneaking);
    }

    @Override
    public void realSetEntityActionState(float strafe, float forward, boolean jumping, boolean sneaking)
    {
        this.callReal = true;
        this.setEntityActionState(strafe, forward, jumping, sneaking);
    }

    // ############################################################################

    @Override
    public void setPosition(double x, double y, double z)
    {
        ServerPlayerAPI.setPosition(this, x, y, z);
    }

    @Override
    public void superSetPosition(double x, double y, double z)
    {
        super.setPosition(x, y, z);
    }

    // ############################################################################

    @Override
    public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch)
    {
        ServerPlayerAPI.setPositionAndRotation(this, x, y, z, yaw, pitch);
    }

    @Override
    public void superSetPositionAndRotation(double x, double y, double z, float yaw, float pitch)
    {
        super.setPositionAndRotation(x, y, z, yaw, pitch);
    }

    // ############################################################################

    @Override
    public void setSneaking(boolean sneaking)
    {
        ServerPlayerAPI.setSneaking(this, sneaking);
    }

    @Override
    public void superSetSneaking(boolean sneaking)
    {
        super.setSneaking(sneaking);
    }

    // ############################################################################

    @Override
    public void setSprinting(boolean sprinting)
    {
        ServerPlayerAPI.setSprinting(this, sprinting);
    }

    @Override
    public void superSetSprinting(boolean sprinting)
    {
        super.setSprinting(sprinting);
    }

    // ############################################################################

    @Shadow
    public abstract boolean startRiding(@Nonnull Entity entity, boolean force);

    @Inject(method = "startRiding", at = @At("HEAD"), cancellable = true)
    public void beforeStartRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforeStartRiding(callbackInfo, this, entity, force);
        }
        this.callReal = false;
    }

    @Inject(method = "startRiding", at = @At("RETURN"))
    public void afterStartRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> callbackInfo)
    {
        ServerPlayerAPI.afterStartRiding(this, entity, force);
    }

    @Override
    public boolean realStartRiding(Entity entity, boolean force)
    {
        this.callReal = true;
        return this.startRiding(entity, force);
    }

    @Override
    public boolean superStartRiding(Entity entity, boolean force)
    {
        return super.startRiding(entity, force);
    }

    // ############################################################################

    @Shadow
    public abstract void swingArm(@Nonnull Hand hand);

    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    public void beforeSwingArm(Hand hand, CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforeSwingArm(callbackInfo, this, hand);
        }
        this.callReal = false;
    }

    @Inject(method = "swingArm", at = @At("RETURN"))
    public void afterSwingArm(Hand hand, CallbackInfo callbackInfo)
    {
        ServerPlayerAPI.afterSwingArm(this, hand);
    }

    @Override
    public void realSwingArm(Hand hand)
    {
        this.callReal = true;
        this.swingArm(hand);
    }

    @Override
    public void superSwingArm(Hand hand)
    {
        super.swingArm(hand);
    }

    // ############################################################################

    @Shadow
    public abstract void tick();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void beforeTick(CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforeTick(callbackInfo, this);
        }
        this.callReal = false;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void afterTick(CallbackInfo callbackInfo)
    {
        ServerPlayerAPI.afterTick(this);
    }

    @Override
    public void realTick()
    {
        this.callReal = true;
        this.tick();
    }

    @Override
    public void superTick()
    {
        super.tick();
    }

    // ############################################################################

    @Override
    public void travel(@Nonnull Vec3d pos)
    {
        ServerPlayerAPI.travel(this, pos);
    }

    @Override
    public void superTravel(Vec3d pos)
    {
        super.travel(pos);
    }

    // ############################################################################

    @Shadow
    @Nonnull
    public abstract Either<SleepResult, Unit> trySleep(@Nonnull BlockPos at);

    @Inject(method = "trySleep", at = @At("HEAD"), cancellable = true)
    public void beforeTrySleep(BlockPos at, CallbackInfoReturnable<Either<PlayerEntity.SleepResult, Unit>> callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforeTrySleep(callbackInfo, this, at);
        }
        this.callReal = false;
    }

    @Inject(method = "trySleep", at = @At("RETURN"))
    public void afterTrySleep(BlockPos at, CallbackInfoReturnable<Either<PlayerEntity.SleepResult, Unit>> callbackInfo)
    {
        ServerPlayerAPI.afterTrySleep(this, at);
    }

    @Override
    public Either<PlayerEntity.SleepResult, Unit> realTrySleep(BlockPos at)
    {
        this.callReal = true;
        return this.trySleep(at);
    }

    @Override
    public Either<PlayerEntity.SleepResult, Unit> superTrySleep(BlockPos at)
    {
        return super.trySleep(at);
    }

    // ############################################################################

    @Override
    protected void updateEntityActionState()
    {
        ServerPlayerAPI.updateEntityActionState(this);
    }

    @Override
    public void superUpdateEntityActionState()
    {
        super.updateEntityActionState();
    }

    // ############################################################################

    @Override
    protected void updatePotionEffects()
    {
        ServerPlayerAPI.updatePotionEffects(this);
    }

    @Override
    public void superUpdatePotionEffects()
    {
        super.updatePotionEffects();
    }

    // ############################################################################

    @Override
    public void updateRidden()
    {
        ServerPlayerAPI.updateRidden(this);
    }

    @Override
    public void superUpdateRidden()
    {
        super.updateRidden();
    }

    // ############################################################################

    @Shadow
    public abstract void func_225652_a_(boolean immediately, boolean updateWorldFlag);

    @Inject(method = "func_225652_a_", at = @At("HEAD"), cancellable = true)
    public void beforeWakeUpPlayer(boolean immediately, boolean updateWorldFlag, CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ServerPlayerAPI.beforeWakeUpPlayer(callbackInfo, this, immediately, updateWorldFlag);
        }
        this.callReal = false;
    }

    @Inject(method = "func_225652_a_", at = @At("RETURN"))
    public void afterWakeUpPlayer(boolean immediately, boolean updateWorldFlag, CallbackInfo callbackInfo)
    {
        ServerPlayerAPI.afterWakeUpPlayer(this, immediately, updateWorldFlag);
    }

    @Override
    public void realWakeUpPlayer(boolean immediately, boolean updateWorldFlag)
    {
        this.callReal = true;
        this.func_225652_a_(immediately, updateWorldFlag);
    }

    @Override
    public void superWakeUpPlayer(boolean immediately, boolean updateWorldFlag)
    {
        super.func_225652_a_(immediately, updateWorldFlag);
    }

    // ############################################################################

    @Override
    @Nonnull
    public CompoundNBT writeWithoutTypeId(@Nonnull CompoundNBT compound)
    {
        return ServerPlayerAPI.writeWithoutTypeId(this, compound);
    }

    @Override
    public CompoundNBT superWriteWithoutTypeId(CompoundNBT compound)
    {
        return super.writeWithoutTypeId(compound);
    }

    // ############################################################################

    @Override
    public Vec3d getMotionMultiplier()
    {
        return this.motionMultiplier;
    }
}
