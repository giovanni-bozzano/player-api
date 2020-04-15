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
package api.player.asm.interfaces;

import api.player.client.ClientPlayerAPI;
import api.player.client.ClientPlayerEntityBase;
import com.mojang.datafixers.util.Either;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stat;
import net.minecraft.tags.Tag;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;

import java.util.Set;

public interface IClientPlayerEntity
{
    ClientPlayerAPI getClientPlayerAPI();

    ClientPlayerEntityBase getClientPlayerBase(String baseId);

    Set<String> getClientPlayerBaseIds();

    Object dynamic(String key, Object[] parameters);

    void superAddExhaustion(float exhaustion);

    void superAddExperienceLevel(int levels);

    void superAddMovementStat(double x, double y, double z);

    void superAddStat(Stat<?> stat, int amount);

    boolean superAreEyesInFluid(Tag<Fluid> fluid, boolean checkChunkLoaded);

    boolean realAttackEntityFrom(DamageSource source, float amount);

    boolean superAttackEntityFrom(DamageSource source, float amount);

    void superAttackTargetEntityWithCurrentItem(Entity targetEntity);

    boolean superCanBreatheUnderwater();

    boolean superCanHarvestBlock(BlockState state);

    boolean superCanPlayerEdit(BlockPos pos, Direction facing, ItemStack stack);

    boolean superCanTriggerWalking();

    void realCloseScreen();

    void superCloseScreen();

    void realDamageEntity(DamageSource source, float amount);

    void superDamageEntity(DamageSource source, float amount);

    ItemEntity superDropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem);

    float superGetAIMoveSpeed();

    float superGetBrightness();

    float superGetDigSpeed(BlockState state, BlockPos pos);

    double superGetDistanceSq(double x, double y, double z);

    double superGetDistanceSq(Entity entity);

    double superGetDistanceSq(Vec3d pos);

    float superGetFovModifier();

    SoundEvent superGetHurtSound(DamageSource source);

    ITextComponent superGetName();

    EntitySize superGetSize(Pose pose);

    int superGetSleepTimer();

    float superGetStandingEyeHeight(Pose pose, EntitySize size);

    void superGiveExperiencePoints(int points);

    boolean superHandleWaterMovement();

    void realHeal(float amount);

    void superHeal(float amount);

    boolean superIsEntityInsideOpaqueBlock();

    boolean superIsInWater();

    boolean superIsOnLadder();

    boolean realIsShiftKeyDown();

    boolean superIsShiftKeyDown();

    boolean superIsSleeping();

    boolean superIsSprinting();

    void superJump();

    void superKnockBack(Entity entity, float strength, double xRatio, double zRatio);

    void realLivingTick();

    void superLivingTick();

    void superMove(MoverType type, Vec3d pos);

    void superMoveRelative(float friction, Vec3d relative);

    void superOnDeath(DamageSource cause);

    void superOnKillEntity(LivingEntity entity);

    boolean superOnLivingFall(float distance, float damageMultiplier);

    void superOnStruckByLightning(LightningBoltEntity lightningBolt);

    RayTraceResult superPick(double blockReachDistance, float partialTicks, boolean anyFluid);

    void superPlayStepSound(BlockPos pos, BlockState block);

    void realPushOutOfBlocks(double x, double y, double z);

    void superPushOutOfBlocks(double x, double y, double z);

    void superRead(CompoundNBT compound);

    void superRecalculateSize();

    void superRemove();

    void realRespawnPlayer();

    void superRespawnPlayer();

    void realSetPlayerSPHealth(float health);

    void superSetPosition(double x, double y, double z);

    void superSetPositionAndRotation(double x, double y, double z, float yaw, float pitch);

    void superSetSneaking(boolean sneaking);

    void superSetSprinting(boolean sprinting);

    boolean realStartRiding(Entity entity, boolean force);

    boolean superStartRiding(Entity entity, boolean force);

    void realSwingArm(Hand hand);

    void superSwingArm(Hand hand);

    void realTick();

    void superTick();

    void superTravel(Vec3d pos);

    Either<PlayerEntity.SleepResult, Unit> superTrySleep(BlockPos at);

    void realUpdateEntityActionState();

    void superUpdateEntityActionState();

    void superUpdatePotionEffects();

    void realUpdateRidden();

    void superUpdateRidden();

    void superWakeUpPlayer(boolean immediately, boolean updateWorldFlag);

    CompoundNBT superWriteWithoutTypeId(CompoundNBT compound);
}
