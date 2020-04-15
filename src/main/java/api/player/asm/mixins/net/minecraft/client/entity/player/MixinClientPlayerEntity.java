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
package api.player.asm.mixins.net.minecraft.client.entity.player;

import api.player.asm.interfaces.IClientPlayerEntity;
import api.player.asm.interfaces.IClientPlayerEntityAccessor;
import api.player.client.ClientPlayerAPI;
import api.player.client.ClientPlayerEntityBase;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tags.Tag;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
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

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity implements IClientPlayerEntity, IClientPlayerEntityAccessor
{
    @Mutable
    @Shadow
    @Final
    public ClientPlayNetHandler connection;
    @Shadow
    @Final
    protected Minecraft mc;
    private ClientPlayerAPI clientPlayerAPI;
    private boolean callReal;

    public MixinClientPlayerEntity(ClientWorld clientWorld, GameProfile gameProfile)
    {
        super(clientWorld, gameProfile);
    }

    @Override
    public ClientPlayerAPI getClientPlayerAPI()
    {
        return this.clientPlayerAPI;
    }

    @Override
    public ClientPlayerEntityBase getClientPlayerBase(String baseId)
    {
        return ClientPlayerAPI.getClientPlayerBase(this, baseId);
    }

    @Override
    public Set<String> getClientPlayerBaseIds()
    {
        return ClientPlayerAPI.getClientPlayerBaseIds(this);
    }

    @Override
    public Object dynamic(String key, Object[] parameters)
    {
        return ClientPlayerAPI.dynamic(this, key, parameters);
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;connection:Lnet/minecraft/client/network/play/ClientPlayNetHandler;"))
    public void beforeInit(ClientPlayerEntity redirectedClientPlayerEntity, ClientPlayNetHandler redirectedConnection, Minecraft minecraft, ClientWorld clientWorld, ClientPlayNetHandler clientPlayNetHandler, StatisticsManager statisticsManager, ClientRecipeBook clientRecipeBook)
    {
        this.clientPlayerAPI = ClientPlayerAPI.create(this);
        ClientPlayerAPI.beforeLocalConstructing(this, minecraft, clientWorld, redirectedConnection, statisticsManager, clientRecipeBook);
        this.connection = redirectedConnection;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void afterInit(Minecraft minecraft, ClientWorld clientWorld, ClientPlayNetHandler clientPlayNetHandler, StatisticsManager statisticsManager, ClientRecipeBook clientRecipeBook, CallbackInfo callbackInfo)
    {
        ClientPlayerAPI.afterLocalConstructing(this, minecraft, clientWorld, clientPlayNetHandler, statisticsManager, clientRecipeBook);
    }

    // ############################################################################

    @Override
    public void addExhaustion(float exhaustion)
    {
        ClientPlayerAPI.addExhaustion(this, exhaustion);
    }

    @Override
    public void superAddExhaustion(float exhaustion)
    {
        super.addExhaustion(exhaustion);
    }

    // ############################################################################

    @Override
    public void addExperienceLevel(int levels)
    {
        ClientPlayerAPI.addExperienceLevel(this, levels);
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
        ClientPlayerAPI.addMovementStat(this, x, y, z);
    }

    @Override
    public void superAddMovementStat(double x, double y, double z)
    {
        super.addMovementStat(x, y, z);
    }

    // ############################################################################

    @Override
    public void addStat(@Nonnull Stat<?> stat, int amount)
    {
        ClientPlayerAPI.addStat(this, stat, amount);
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
        return ClientPlayerAPI.areEyesInFluid(this, fluid, checkChunkLoaded);
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
            ClientPlayerAPI.beforeAttackEntityFrom(callbackInfo, this, source, amount);
        }
        this.callReal = false;
    }

    @Inject(method = "attackEntityFrom", at = @At("RETURN"))
    public void afterAttackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callbackInfo)
    {
        ClientPlayerAPI.afterAttackEntityFrom(this, source, amount);
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

    @Override
    public void attackTargetEntityWithCurrentItem(@Nonnull Entity targetEntity)
    {
        ClientPlayerAPI.attackTargetEntityWithCurrentItem(this, targetEntity);
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
        return ClientPlayerAPI.canBreatheUnderwater(this);
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
        return ClientPlayerAPI.canHarvestBlock(this, state);
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
        return ClientPlayerAPI.canPlayerEdit(this, pos, facing, stack);
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
        return ClientPlayerAPI.canTriggerWalking(this);
    }

    @Override
    public boolean superCanTriggerWalking()
    {
        return super.canTriggerWalking();
    }

    // ############################################################################

    @Shadow
    public abstract void closeScreen();

    @Inject(method = "closeScreen", at = @At("HEAD"), cancellable = true)
    public void beforeCloseScreen(CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ClientPlayerAPI.beforeCloseScreen(callbackInfo, this);
        }
        this.callReal = false;
    }

    @Inject(method = "closeScreen", at = @At("RETURN"))
    public void afterCloseScreen(CallbackInfo callbackInfo)
    {
        ClientPlayerAPI.afterCloseScreen(this);
    }

    @Override
    public void realCloseScreen()
    {
        this.callReal = true;
        this.closeScreen();
    }

    @Override
    public void superCloseScreen()
    {
        super.closeScreen();
    }

    // ############################################################################

    @Shadow
    protected abstract void damageEntity(@Nonnull DamageSource source, float amount);

    @Inject(method = "damageEntity", at = @At("HEAD"), cancellable = true)
    protected void beforeDamageEntity(DamageSource source, float amount, CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ClientPlayerAPI.beforeDamageEntity(callbackInfo, this, source, amount);
        }
        this.callReal = false;
    }

    @Inject(method = "damageEntity", at = @At("RETURN"))
    protected void afterDamageEntity(DamageSource source, float amount, CallbackInfo callbackInfo)
    {
        ClientPlayerAPI.afterDamageEntity(this, source, amount);
    }

    @Override
    public void realDamageEntity(DamageSource source, float amount)
    {
        this.callReal = true;
        this.damageEntity(source, amount);
    }

    @Override
    public void superDamageEntity(DamageSource source, float amount)
    {
        super.damageEntity(source, amount);
    }

    // ############################################################################

    @Override
    public ItemEntity dropItem(@Nonnull ItemStack droppedItem, boolean dropAround, boolean traceItem)
    {
        return ClientPlayerAPI.dropItem(this, droppedItem, dropAround, traceItem);
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
        return ClientPlayerAPI.getAIMoveSpeed(this);
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
        return ClientPlayerAPI.getBrightness(this);
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
        return ClientPlayerAPI.getDigSpeed(this, state, pos);
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
        return ClientPlayerAPI.getDistanceSq(this, x, y, z);
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
        return ClientPlayerAPI.getDistanceSqToEntity(this, entity);
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
        return ClientPlayerAPI.getDistanceSqVec(this, pos);
    }

    @Override
    public double superGetDistanceSq(Vec3d pos)
    {
        return super.getDistanceSq(pos);
    }

    // ############################################################################

    @Override
    public float getFovModifier()
    {
        return ClientPlayerAPI.getFovModifier(this);
    }

    @Override
    public float superGetFovModifier()
    {
        return super.getFovModifier();
    }

    // ############################################################################

    @Override
    protected SoundEvent getHurtSound(@Nonnull DamageSource source)
    {
        return ClientPlayerAPI.getHurtSound(this, source);
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
        return ClientPlayerAPI.getName(this);
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
        return ClientPlayerAPI.getSize(this, pose);
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
        return ClientPlayerAPI.getSleepTimer(this);
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
        return ClientPlayerAPI.getStandingEyeHeight(this, pose, size);
    }

    @Override
    public float superGetStandingEyeHeight(Pose pose, EntitySize size)
    {
        return super.getStandingEyeHeight(pose, size);
    }

    // ############################################################################

    @Override
    public void giveExperiencePoints(int points)
    {
        ClientPlayerAPI.giveExperiencePoints(this, points);
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
        return ClientPlayerAPI.handleWaterMovement(this);
    }

    @Override
    public boolean superHandleWaterMovement()
    {
        return super.handleWaterMovement();
    }

    // ############################################################################

    @Shadow
    public abstract void heal(float amount);

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    public void beforeHeal(float amount, CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ClientPlayerAPI.beforeHeal(callbackInfo, this, amount);
        }
        this.callReal = false;
    }

    @Inject(method = "heal", at = @At("RETURN"))
    public void afterHeal(float amount, CallbackInfo callbackInfo)
    {
        ClientPlayerAPI.afterHeal(this, amount);
    }

    @Override
    public void realHeal(float amount)
    {
        this.callReal = true;
        this.heal(amount);
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
        return ClientPlayerAPI.isEntityInsideOpaqueBlock(this);
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
        return ClientPlayerAPI.isInWater(this);
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
        return ClientPlayerAPI.isOnLadder(this);
    }

    @Override
    public boolean superIsOnLadder()
    {
        return super.isOnLadder();
    }

    // ############################################################################

    @Shadow
    public abstract boolean isShiftKeyDown();

    @Inject(method = "isShiftKeyDown", at = @At("HEAD"), cancellable = true)
    public void beforeIsShiftKeyDown(CallbackInfoReturnable<Boolean> callbackInfo)
    {
        if (!this.callReal) {
            ClientPlayerAPI.beforeIsShiftKeyDown(callbackInfo, this);
        }
        this.callReal = false;
    }

    @Inject(method = "isShiftKeyDown", at = @At("HEAD"))
    public void afterIsShiftKeyDown(CallbackInfoReturnable<Boolean> callbackInfo)
    {
        ClientPlayerAPI.afterIsShiftKeyDown(this);
    }

    @Override
    public boolean realIsShiftKeyDown()
    {
        this.callReal = true;
        return this.isShiftKeyDown();
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
        return ClientPlayerAPI.isSleeping(this);
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
        return ClientPlayerAPI.isSprinting(this);
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
        ClientPlayerAPI.jump(this);
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
        ClientPlayerAPI.knockBack(this, entity, strength, xRatio, zRatio);
    }

    @Override
    public void superKnockBack(Entity entity, float strength, double xRatio, double zRatio)
    {
        super.knockBack(entity, strength, xRatio, zRatio);
    }

    // ############################################################################

    @Shadow
    public abstract void livingTick();

    @Inject(method = "livingTick", at = @At("HEAD"), cancellable = true)
    public void beforeLivingTick(CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ClientPlayerAPI.beforeLivingTick(callbackInfo, this);
        }
        this.callReal = false;
    }

    @Inject(method = "livingTick", at = @At("RETURN"))
    public void afterLivingTick(CallbackInfo callbackInfo)
    {
        ClientPlayerAPI.afterLivingTick(this);
    }

    @Override
    public void realLivingTick()
    {
        this.callReal = true;
        this.livingTick();
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
        ClientPlayerAPI.move(this, type, pos);
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
        ClientPlayerAPI.moveRelative(this, friction, relative);
    }

    @Override
    public void superMoveRelative(float friction, Vec3d relative)
    {
        super.moveRelative(friction, relative);
    }

    // ############################################################################

    @Override
    public void onDeath(@Nonnull DamageSource cause)
    {
        ClientPlayerAPI.onDeath(this, cause);
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
        ClientPlayerAPI.onKillEntity(this, entity);
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
        return ClientPlayerAPI.onLivingFall(this, distance, damageMultiplier);
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
        ClientPlayerAPI.onStruckByLightning(this, lightningBolt);
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
        return ClientPlayerAPI.pick(this, blockReachDistance, partialTicks, anyFluid);
    }

    @Override
    public RayTraceResult superPick(double blockReachDistance, float partialTicks, boolean anyFluid)
    {
        return super.pick(blockReachDistance, partialTicks, anyFluid);
    }

    // ############################################################################

    @Override
    protected void playStepSound(@Nonnull BlockPos pos, @Nonnull BlockState block)
    {
        ClientPlayerAPI.playStepSound(this, pos, block);
    }

    @Override
    public void superPlayStepSound(BlockPos pos, BlockState block)
    {
        super.playStepSound(pos, block);
    }

    // ############################################################################

    @Shadow
    protected abstract void pushOutOfBlocks(double x, double y, double z);

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    protected void beforePushOutOfBlocks(double x, double y, double z, CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ClientPlayerAPI.beforePushOutOfBlocks(callbackInfo, this, x, y, z);
        }
        this.callReal = false;
    }

    @Inject(method = "pushOutOfBlocks", at = @At("RETURN"))
    protected void afterPushOutOfBlocks(double x, double y, double z, CallbackInfo callbackInfo)
    {
        ClientPlayerAPI.afterPushOutOfBlocks(this, x, y, z);
    }

    @Override
    public void realPushOutOfBlocks(double x, double y, double z)
    {
        this.callReal = true;
        this.pushOutOfBlocks(x, y, z);
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
        ClientPlayerAPI.read(this, compound);
    }

    @Override
    public void superRead(CompoundNBT compound)
    {
        super.read(compound);
    }

    // ############################################################################

    @Override
    public void recalculateSize()
    {
        ClientPlayerAPI.recalculateSize(this);
    }

    @Override
    public void superRecalculateSize()
    {
        super.recalculateSize();
    }

    // ############################################################################

    @Override
    public void remove()
    {
        ClientPlayerAPI.remove(this);
    }

    @Override
    public void superRemove()
    {
        super.remove();
    }

    // ############################################################################

    @Shadow
    public abstract void respawnPlayer();

    @Inject(method = "respawnPlayer", at = @At("HEAD"), cancellable = true)
    public void beforeRespawnPlayer(CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ClientPlayerAPI.beforeRespawnPlayer(callbackInfo, this);
        }
        this.callReal = false;
    }

    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    public void afterRespawnPlayer(CallbackInfo callbackInfo)
    {
        ClientPlayerAPI.afterRespawnPlayer(this);
    }

    @Override
    public void realRespawnPlayer()
    {
        this.callReal = true;
        this.respawnPlayer();
    }

    @Override
    public void superRespawnPlayer()
    {
        super.respawnPlayer();
    }

    // ############################################################################

    @Shadow
    public abstract void setPlayerSPHealth(float health);

    @Inject(method = "setPlayerSPHealth", at = @At("HEAD"), cancellable = true)
    public void beforeSetPlayerSPHealth(float health, CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ClientPlayerAPI.beforeSetPlayerSPHealth(callbackInfo, this, health);
        }
        this.callReal = false;
    }

    @Inject(method = "setPlayerSPHealth", at = @At("RETURN"))
    public void afterSetPlayerSPHealth(float health, CallbackInfo callbackInfo)
    {
        ClientPlayerAPI.afterSetPlayerSPHealth(this, health);
    }

    @Override
    public void realSetPlayerSPHealth(float health)
    {
        this.callReal = true;
        this.setPlayerSPHealth(health);
    }

    // ############################################################################

    @Override
    public void setPosition(double x, double y, double z)
    {
        ClientPlayerAPI.setPosition(this, x, y, z);
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
        ClientPlayerAPI.setPositionAndRotation(this, x, y, z, yaw, pitch);
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
        ClientPlayerAPI.setSneaking(this, sneaking);
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
        ClientPlayerAPI.setSprinting(this, sprinting);
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
            ClientPlayerAPI.beforeStartRiding(callbackInfo, this, entity, force);
        }
        this.callReal = false;
    }

    @Inject(method = "startRiding", at = @At("RETURN"))
    public void afterStartRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> callbackInfo)
    {
        ClientPlayerAPI.afterStartRiding(this, entity, force);
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
            ClientPlayerAPI.beforeSwingArm(callbackInfo, this, hand);
        }
        this.callReal = false;
    }

    @Inject(method = "swingArm", at = @At("RETURN"))
    public void afterSwingArm(Hand hand, CallbackInfo callbackInfo)
    {
        ClientPlayerAPI.afterSwingArm(this, hand);
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
            ClientPlayerAPI.beforeTick(callbackInfo, this);
        }
        this.callReal = false;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void afterTick(CallbackInfo callbackInfo)
    {
        ClientPlayerAPI.afterTick(this);
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
        ClientPlayerAPI.travel(this, pos);
    }

    @Override
    public void superTravel(Vec3d pos)
    {
        super.travel(pos);
    }

    // ############################################################################

    @Override
    @Nonnull
    public Either<SleepResult, Unit> trySleep(@Nonnull BlockPos at)
    {
        return ClientPlayerAPI.trySleep(this, at);
    }

    @Override
    public Either<PlayerEntity.SleepResult, Unit> superTrySleep(BlockPos at)
    {
        return super.trySleep(at);
    }

    // ############################################################################

    @Shadow
    public abstract void updateEntityActionState();

    @Inject(method = "updateEntityActionState", at = @At("HEAD"), cancellable = true)
    protected void beforeUpdateEntityActionState(CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ClientPlayerAPI.beforeUpdateEntityActionState(callbackInfo, this);
        }
        this.callReal = false;
    }

    @Inject(method = "updateEntityActionState", at = @At("RETURN"))
    protected void afterUpdateEntityActionState(CallbackInfo callbackInfo)
    {
        ClientPlayerAPI.afterUpdateEntityActionState(this);
    }

    @Override
    public void realUpdateEntityActionState()
    {
        this.callReal = true;
        this.updateEntityActionState();
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
        ClientPlayerAPI.updatePotionEffects(this);
    }

    @Override
    public void superUpdatePotionEffects()
    {
        super.updatePotionEffects();
    }

    // ############################################################################

    @Shadow
    public abstract void updateRidden();

    @Inject(method = "updateRidden", at = @At("HEAD"), cancellable = true)
    public void beforeUpdateRidden(CallbackInfo callbackInfo)
    {
        if (!this.callReal) {
            ClientPlayerAPI.beforeUpdateRidden(callbackInfo, this);
        }
        this.callReal = false;
    }

    @Inject(method = "updateRidden", at = @At("RETURN"))
    public void afterUpdateRidden(CallbackInfo callbackInfo)
    {
        ClientPlayerAPI.afterUpdateRidden(this);
    }

    @Override
    public void realUpdateRidden()
    {
        this.callReal = true;
        this.updateRidden();
    }

    @Override
    public void superUpdateRidden()
    {
        super.updateRidden();
    }

    // ############################################################################

    @Override
    public void func_225652_a_(boolean immediately, boolean updateWorldFlag) // wakeUpPlayer
    {
        ClientPlayerAPI.wakeUpPlayer(this, immediately, updateWorldFlag);
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
        return ClientPlayerAPI.writeWithoutTypeId(this, compound);
    }

    @Override
    public CompoundNBT superWriteWithoutTypeId(CompoundNBT compound)
    {
        return super.writeWithoutTypeId(compound);
    }

    // ############################################################################

    @Override
    public Minecraft getMinecraft()
    {
        return this.mc;
    }

    @Override
    public Vec3d getMotionMultiplier()
    {
        return this.motionMultiplier;
    }
}
