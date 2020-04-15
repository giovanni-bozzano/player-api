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
package api.player.server;

import api.player.PlayerAPI;
import api.player.asm.interfaces.IClientPlayerEntity;
import api.player.asm.interfaces.IServerPlayerEntity;
import api.player.client.ClientPlayerAPI;
import api.player.client.ClientPlayerEntityBase;
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
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

public final class ServerPlayerAPI
{
    private final static Class<?>[] Class = new Class[]{ServerPlayerAPI.class};
    private final static Class<?>[] Classes = new Class[]{ServerPlayerAPI.class, String.class};
    private static boolean isCreated;
    private static final Logger logger = Logger.getLogger("ServerPlayerAPI");
    protected final IServerPlayerEntity player;
    private final static Set<String> keys = new HashSet<>();
    private final static Map<String, String> keysToVirtualIds = new HashMap<>();
    private final static Set<Class<?>> dynamicTypes = new HashSet<>();
    private final static Map<Class<?>, Map<String, Method>> virtualDynamicHookMethods = new HashMap<>();
    private final static Map<Class<?>, Map<String, Method>> beforeDynamicHookMethods = new HashMap<>();
    private final static Map<Class<?>, Map<String, Method>> overrideDynamicHookMethods = new HashMap<>();
    private final static Map<Class<?>, Map<String, Method>> afterDynamicHookMethods = new HashMap<>();
    private final static List<String> beforeLocalConstructingHookTypes = new LinkedList<>();
    private final static List<String> afterLocalConstructingHookTypes = new LinkedList<>();
    private static final Map<String, List<String>> beforeDynamicHookTypes = new Hashtable<>(0);
    private static final Map<String, List<String>> overrideDynamicHookTypes = new Hashtable<>(0);
    private static final Map<String, List<String>> afterDynamicHookTypes = new Hashtable<>(0);
    private ServerPlayerEntityBase[] beforeLocalConstructingHooks;
    private ServerPlayerEntityBase[] afterLocalConstructingHooks;
    private final Map<ServerPlayerEntityBase, String> baseObjectsToId = new Hashtable<>();
    private final Map<String, ServerPlayerEntityBase> allBaseObjects = new Hashtable<>();
    private final Set<String> unmodifiableAllBaseIds = Collections.unmodifiableSet(this.allBaseObjects.keySet());
    private static final Map<String, Constructor<?>> allBaseConstructors = new Hashtable<>();
    private static final Set<String> unmodifiableAllIds = Collections.unmodifiableSet(allBaseConstructors.keySet());
    private static final Map<String, String[]> allBaseBeforeLocalConstructingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeLocalConstructingInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterLocalConstructingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterLocalConstructingInferiors = new Hashtable<>(0);
    private static final Map<String, Map<String, String[]>> allBaseBeforeDynamicSuperiors = new Hashtable<>(0);
    private static final Map<String, Map<String, String[]>> allBaseBeforeDynamicInferiors = new Hashtable<>(0);
    private static final Map<String, Map<String, String[]>> allBaseOverrideDynamicSuperiors = new Hashtable<>(0);
    private static final Map<String, Map<String, String[]>> allBaseOverrideDynamicInferiors = new Hashtable<>(0);
    private static final Map<String, Map<String, String[]>> allBaseAfterDynamicSuperiors = new Hashtable<>(0);
    private static final Map<String, Map<String, String[]>> allBaseAfterDynamicInferiors = new Hashtable<>(0);
    private static boolean initialized = false;

    private static void log(String text)
    {
        System.out.println(text);
        logger.fine(text);
    }

    public static void register(String id, Class<?> baseClass)
    {
        register(id, baseClass, null);
    }

    public static void register(String id, Class<?> baseClass, ServerPlayerBaseSorting baseSorting)
    {
        try {
            register(baseClass, id, baseSorting);
        } catch (RuntimeException exception) {
            if (id != null) {
                log("Server Player: failed to register id '" + id + "'");
            } else {
                log("Server Player: failed to register ServerPlayerBase");
            }

            throw exception;
        }
    }

    private static void register(Class<?> baseClass, String id, ServerPlayerBaseSorting baseSorting)
    {
        if (!isCreated) {
            try {
                Method mandatory = ServerPlayerEntity.class.getMethod("getServerPlayerBase", String.class);
                if (mandatory.getReturnType() != ServerPlayerEntityBase.class) {
                    throw new NoSuchMethodException(ServerPlayerEntityBase.class.getName() + " " + ServerPlayerEntity.class.getName() + ".getServerPlayerBase(" + String.class.getName() + ")");
                }
            } catch (NoSuchMethodException exception) {
                String[] errorMessageParts = new String[]{
                        "========================================",
                        "The API \"Server Player\" version " + PlayerAPI.VERSION + " of the mod \"Player API " + PlayerAPI.VERSION + "\" cannot be created!",
                        "----------------------------------------",
                        "Mandatory member method \"{0} getServerPlayerBase({3})\" not found in class \"{1}\".",
                        "There are three scenarios this can happen:",
                        "* Minecraft Forge is missing a Player API which Minecraft version matches its own.",
                        "  Download and install the latest Player API for the Minecraft version you were trying to run.",
                        "* The code of the class \"{2}\" of Player API has been modified beyond recognition by another Minecraft Forge mod.",
                        "  Try temporary uninstallation of other mods to find the culprit and uninstall it permanently to fix this specific problem.",
                        "* Player API has not been installed correctly.",
                        "  Uninstall Player API and install it again following the installation instructions.",
                        "========================================"
                };

                String baseEntityPlayerMPClassName = ServerPlayerEntityBase.class.getName();
                String targetClassName = ServerPlayerEntity.class.getName();
                String targetClassFileName = targetClassName.replace(".", File.separator);
                String stringClassName = String.class.getName();

                for (int i = 0; i < errorMessageParts.length; i++) {
                    errorMessageParts[i] = MessageFormat.format(errorMessageParts[i], baseEntityPlayerMPClassName, targetClassName, targetClassFileName, stringClassName);
                }

                for (String errorMessagePart : errorMessageParts) {
                    logger.severe(errorMessagePart);
                }

                for (String errorMessagePart : errorMessageParts) {
                    System.err.println(errorMessagePart);
                }

                StringBuilder errorMessage = new StringBuilder("\n\n");
                for (String errorMessagePart : errorMessageParts) {
                    errorMessage.append("\t").append(errorMessagePart).append("\n");
                }

                throw new RuntimeException(errorMessage.toString(), exception);
            }

            log("Server Player " + PlayerAPI.VERSION + " Created");
            isCreated = true;
        }

        if (id == null) {
            throw new NullPointerException("Argument 'id' can not be null");
        }
        if (baseClass == null) {
            throw new NullPointerException("Argument 'baseClass' can not be null");
        }

        Constructor<?> alreadyRegistered = allBaseConstructors.get(id);
        if (alreadyRegistered != null) {
            throw new IllegalArgumentException("The class '" + baseClass.getName() + "' can not be registered with the id '" + id + "' because the class '" + alreadyRegistered.getDeclaringClass().getName() + "' has already been registered with the same id");
        }

        Constructor<?> baseConstructor;
        try {
            baseConstructor = baseClass.getDeclaredConstructor(Classes);
        } catch (Throwable t) {
            try {
                baseConstructor = baseClass.getDeclaredConstructor(Class);
            } catch (Throwable s) {
                throw new IllegalArgumentException("Can not find necessary constructor with one argument of type '" + ServerPlayerAPI.class.getName() + "' and eventually a second argument of type 'String' in the class '" + baseClass.getName() + "'", t);
            }
        }

        allBaseConstructors.put(id, baseConstructor);

        if (baseSorting != null) {
            addSorting(id, allBaseBeforeLocalConstructingSuperiors, baseSorting.getBeforeLocalConstructingSuperiors());
            addSorting(id, allBaseBeforeLocalConstructingInferiors, baseSorting.getBeforeLocalConstructingInferiors());
            addSorting(id, allBaseAfterLocalConstructingSuperiors, baseSorting.getAfterLocalConstructingSuperiors());
            addSorting(id, allBaseAfterLocalConstructingInferiors, baseSorting.getAfterLocalConstructingInferiors());

            addDynamicSorting(id, allBaseBeforeDynamicSuperiors, baseSorting.getDynamicBeforeSuperiors());
            addDynamicSorting(id, allBaseBeforeDynamicInferiors, baseSorting.getDynamicBeforeInferiors());
            addDynamicSorting(id, allBaseOverrideDynamicSuperiors, baseSorting.getDynamicOverrideSuperiors());
            addDynamicSorting(id, allBaseOverrideDynamicInferiors, baseSorting.getDynamicOverrideInferiors());
            addDynamicSorting(id, allBaseAfterDynamicSuperiors, baseSorting.getDynamicAfterSuperiors());
            addDynamicSorting(id, allBaseAfterDynamicInferiors, baseSorting.getDynamicAfterInferiors());

            addSorting(id, allBaseBeforeAddExhaustionSuperiors, baseSorting.getBeforeAddExhaustionSuperiors());
            addSorting(id, allBaseBeforeAddExhaustionInferiors, baseSorting.getBeforeAddExhaustionInferiors());
            addSorting(id, allBaseOverrideAddExhaustionSuperiors, baseSorting.getOverrideAddExhaustionSuperiors());
            addSorting(id, allBaseOverrideAddExhaustionInferiors, baseSorting.getOverrideAddExhaustionInferiors());
            addSorting(id, allBaseAfterAddExhaustionSuperiors, baseSorting.getAfterAddExhaustionSuperiors());
            addSorting(id, allBaseAfterAddExhaustionInferiors, baseSorting.getAfterAddExhaustionInferiors());

            addSorting(id, allBaseBeforeAddExperienceLevelSuperiors, baseSorting.getBeforeAddExperienceLevelSuperiors());
            addSorting(id, allBaseBeforeAddExperienceLevelInferiors, baseSorting.getBeforeAddExperienceLevelInferiors());
            addSorting(id, allBaseOverrideAddExperienceLevelSuperiors, baseSorting.getOverrideAddExperienceLevelSuperiors());
            addSorting(id, allBaseOverrideAddExperienceLevelInferiors, baseSorting.getOverrideAddExperienceLevelInferiors());
            addSorting(id, allBaseAfterAddExperienceLevelSuperiors, baseSorting.getAfterAddExperienceLevelSuperiors());
            addSorting(id, allBaseAfterAddExperienceLevelInferiors, baseSorting.getAfterAddExperienceLevelInferiors());

            addSorting(id, allBaseBeforeAddMovementStatSuperiors, baseSorting.getBeforeAddMovementStatSuperiors());
            addSorting(id, allBaseBeforeAddMovementStatInferiors, baseSorting.getBeforeAddMovementStatInferiors());
            addSorting(id, allBaseOverrideAddMovementStatSuperiors, baseSorting.getOverrideAddMovementStatSuperiors());
            addSorting(id, allBaseOverrideAddMovementStatInferiors, baseSorting.getOverrideAddMovementStatInferiors());
            addSorting(id, allBaseAfterAddMovementStatSuperiors, baseSorting.getAfterAddMovementStatSuperiors());
            addSorting(id, allBaseAfterAddMovementStatInferiors, baseSorting.getAfterAddMovementStatInferiors());

            addSorting(id, allBaseBeforeAddStatSuperiors, baseSorting.getBeforeAddStatSuperiors());
            addSorting(id, allBaseBeforeAddStatInferiors, baseSorting.getBeforeAddStatInferiors());
            addSorting(id, allBaseOverrideAddStatSuperiors, baseSorting.getOverrideAddStatSuperiors());
            addSorting(id, allBaseOverrideAddStatInferiors, baseSorting.getOverrideAddStatInferiors());
            addSorting(id, allBaseAfterAddStatSuperiors, baseSorting.getAfterAddStatSuperiors());
            addSorting(id, allBaseAfterAddStatInferiors, baseSorting.getAfterAddStatInferiors());

            addSorting(id, allBaseBeforeAreEyesInFluidSuperiors, baseSorting.getBeforeAreEyesInFluidSuperiors());
            addSorting(id, allBaseBeforeAreEyesInFluidInferiors, baseSorting.getBeforeAreEyesInFluidInferiors());
            addSorting(id, allBaseOverrideAreEyesInFluidSuperiors, baseSorting.getOverrideAreEyesInFluidSuperiors());
            addSorting(id, allBaseOverrideAreEyesInFluidInferiors, baseSorting.getOverrideAreEyesInFluidInferiors());
            addSorting(id, allBaseAfterAreEyesInFluidSuperiors, baseSorting.getAfterAreEyesInFluidSuperiors());
            addSorting(id, allBaseAfterAreEyesInFluidInferiors, baseSorting.getAfterAreEyesInFluidInferiors());

            addSorting(id, allBaseBeforeAttackEntityFromSuperiors, baseSorting.getBeforeAttackEntityFromSuperiors());
            addSorting(id, allBaseBeforeAttackEntityFromInferiors, baseSorting.getBeforeAttackEntityFromInferiors());
            addSorting(id, allBaseOverrideAttackEntityFromSuperiors, baseSorting.getOverrideAttackEntityFromSuperiors());
            addSorting(id, allBaseOverrideAttackEntityFromInferiors, baseSorting.getOverrideAttackEntityFromInferiors());
            addSorting(id, allBaseAfterAttackEntityFromSuperiors, baseSorting.getAfterAttackEntityFromSuperiors());
            addSorting(id, allBaseAfterAttackEntityFromInferiors, baseSorting.getAfterAttackEntityFromInferiors());

            addSorting(id, allBaseBeforeAttackTargetEntityWithCurrentItemSuperiors, baseSorting.getBeforeAttackTargetEntityWithCurrentItemSuperiors());
            addSorting(id, allBaseBeforeAttackTargetEntityWithCurrentItemInferiors, baseSorting.getBeforeAttackTargetEntityWithCurrentItemInferiors());
            addSorting(id, allBaseOverrideAttackTargetEntityWithCurrentItemSuperiors, baseSorting.getOverrideAttackTargetEntityWithCurrentItemSuperiors());
            addSorting(id, allBaseOverrideAttackTargetEntityWithCurrentItemInferiors, baseSorting.getOverrideAttackTargetEntityWithCurrentItemInferiors());
            addSorting(id, allBaseAfterAttackTargetEntityWithCurrentItemSuperiors, baseSorting.getAfterAttackTargetEntityWithCurrentItemSuperiors());
            addSorting(id, allBaseAfterAttackTargetEntityWithCurrentItemInferiors, baseSorting.getAfterAttackTargetEntityWithCurrentItemInferiors());

            addSorting(id, allBaseBeforeCanBreatheUnderwaterSuperiors, baseSorting.getBeforeCanBreatheUnderwaterSuperiors());
            addSorting(id, allBaseBeforeCanBreatheUnderwaterInferiors, baseSorting.getBeforeCanBreatheUnderwaterInferiors());
            addSorting(id, allBaseOverrideCanBreatheUnderwaterSuperiors, baseSorting.getOverrideCanBreatheUnderwaterSuperiors());
            addSorting(id, allBaseOverrideCanBreatheUnderwaterInferiors, baseSorting.getOverrideCanBreatheUnderwaterInferiors());
            addSorting(id, allBaseAfterCanBreatheUnderwaterSuperiors, baseSorting.getAfterCanBreatheUnderwaterSuperiors());
            addSorting(id, allBaseAfterCanBreatheUnderwaterInferiors, baseSorting.getAfterCanBreatheUnderwaterInferiors());

            addSorting(id, allBaseBeforeCanHarvestBlockSuperiors, baseSorting.getBeforeCanHarvestBlockSuperiors());
            addSorting(id, allBaseBeforeCanHarvestBlockInferiors, baseSorting.getBeforeCanHarvestBlockInferiors());
            addSorting(id, allBaseOverrideCanHarvestBlockSuperiors, baseSorting.getOverrideCanHarvestBlockSuperiors());
            addSorting(id, allBaseOverrideCanHarvestBlockInferiors, baseSorting.getOverrideCanHarvestBlockInferiors());
            addSorting(id, allBaseAfterCanHarvestBlockSuperiors, baseSorting.getAfterCanHarvestBlockSuperiors());
            addSorting(id, allBaseAfterCanHarvestBlockInferiors, baseSorting.getAfterCanHarvestBlockInferiors());

            addSorting(id, allBaseBeforeCanPlayerEditSuperiors, baseSorting.getBeforeCanPlayerEditSuperiors());
            addSorting(id, allBaseBeforeCanPlayerEditInferiors, baseSorting.getBeforeCanPlayerEditInferiors());
            addSorting(id, allBaseOverrideCanPlayerEditSuperiors, baseSorting.getOverrideCanPlayerEditSuperiors());
            addSorting(id, allBaseOverrideCanPlayerEditInferiors, baseSorting.getOverrideCanPlayerEditInferiors());
            addSorting(id, allBaseAfterCanPlayerEditSuperiors, baseSorting.getAfterCanPlayerEditSuperiors());
            addSorting(id, allBaseAfterCanPlayerEditInferiors, baseSorting.getAfterCanPlayerEditInferiors());

            addSorting(id, allBaseBeforeCanTriggerWalkingSuperiors, baseSorting.getBeforeCanTriggerWalkingSuperiors());
            addSorting(id, allBaseBeforeCanTriggerWalkingInferiors, baseSorting.getBeforeCanTriggerWalkingInferiors());
            addSorting(id, allBaseOverrideCanTriggerWalkingSuperiors, baseSorting.getOverrideCanTriggerWalkingSuperiors());
            addSorting(id, allBaseOverrideCanTriggerWalkingInferiors, baseSorting.getOverrideCanTriggerWalkingInferiors());
            addSorting(id, allBaseAfterCanTriggerWalkingSuperiors, baseSorting.getAfterCanTriggerWalkingSuperiors());
            addSorting(id, allBaseAfterCanTriggerWalkingInferiors, baseSorting.getAfterCanTriggerWalkingInferiors());

            addSorting(id, allBaseBeforeCopyFromSuperiors, baseSorting.getBeforeCopyFromSuperiors());
            addSorting(id, allBaseBeforeCopyFromInferiors, baseSorting.getBeforeCopyFromInferiors());
            addSorting(id, allBaseOverrideCopyFromSuperiors, baseSorting.getOverrideCopyFromSuperiors());
            addSorting(id, allBaseOverrideCopyFromInferiors, baseSorting.getOverrideCopyFromInferiors());
            addSorting(id, allBaseAfterCopyFromSuperiors, baseSorting.getAfterCopyFromSuperiors());
            addSorting(id, allBaseAfterCopyFromInferiors, baseSorting.getAfterCopyFromInferiors());

            addSorting(id, allBaseBeforeDamageEntitySuperiors, baseSorting.getBeforeDamageEntitySuperiors());
            addSorting(id, allBaseBeforeDamageEntityInferiors, baseSorting.getBeforeDamageEntityInferiors());
            addSorting(id, allBaseOverrideDamageEntitySuperiors, baseSorting.getOverrideDamageEntitySuperiors());
            addSorting(id, allBaseOverrideDamageEntityInferiors, baseSorting.getOverrideDamageEntityInferiors());
            addSorting(id, allBaseAfterDamageEntitySuperiors, baseSorting.getAfterDamageEntitySuperiors());
            addSorting(id, allBaseAfterDamageEntityInferiors, baseSorting.getAfterDamageEntityInferiors());

            addSorting(id, allBaseBeforeDropItemSuperiors, baseSorting.getBeforeDropItemSuperiors());
            addSorting(id, allBaseBeforeDropItemInferiors, baseSorting.getBeforeDropItemInferiors());
            addSorting(id, allBaseOverrideDropItemSuperiors, baseSorting.getOverrideDropItemSuperiors());
            addSorting(id, allBaseOverrideDropItemInferiors, baseSorting.getOverrideDropItemInferiors());
            addSorting(id, allBaseAfterDropItemSuperiors, baseSorting.getAfterDropItemSuperiors());
            addSorting(id, allBaseAfterDropItemInferiors, baseSorting.getAfterDropItemInferiors());

            addSorting(id, allBaseBeforeGetAIMoveSpeedSuperiors, baseSorting.getBeforeGetAIMoveSpeedSuperiors());
            addSorting(id, allBaseBeforeGetAIMoveSpeedInferiors, baseSorting.getBeforeGetAIMoveSpeedInferiors());
            addSorting(id, allBaseOverrideGetAIMoveSpeedSuperiors, baseSorting.getOverrideGetAIMoveSpeedSuperiors());
            addSorting(id, allBaseOverrideGetAIMoveSpeedInferiors, baseSorting.getOverrideGetAIMoveSpeedInferiors());
            addSorting(id, allBaseAfterGetAIMoveSpeedSuperiors, baseSorting.getAfterGetAIMoveSpeedSuperiors());
            addSorting(id, allBaseAfterGetAIMoveSpeedInferiors, baseSorting.getAfterGetAIMoveSpeedInferiors());

            addSorting(id, allBaseBeforeGetBrightnessSuperiors, baseSorting.getBeforeGetBrightnessSuperiors());
            addSorting(id, allBaseBeforeGetBrightnessInferiors, baseSorting.getBeforeGetBrightnessInferiors());
            addSorting(id, allBaseOverrideGetBrightnessSuperiors, baseSorting.getOverrideGetBrightnessSuperiors());
            addSorting(id, allBaseOverrideGetBrightnessInferiors, baseSorting.getOverrideGetBrightnessInferiors());
            addSorting(id, allBaseAfterGetBrightnessSuperiors, baseSorting.getAfterGetBrightnessSuperiors());
            addSorting(id, allBaseAfterGetBrightnessInferiors, baseSorting.getAfterGetBrightnessInferiors());

            addSorting(id, allBaseBeforeGetDigSpeedSuperiors, baseSorting.getBeforeGetDigSpeedSuperiors());
            addSorting(id, allBaseBeforeGetDigSpeedInferiors, baseSorting.getBeforeGetDigSpeedInferiors());
            addSorting(id, allBaseOverrideGetDigSpeedSuperiors, baseSorting.getOverrideGetDigSpeedSuperiors());
            addSorting(id, allBaseOverrideGetDigSpeedInferiors, baseSorting.getOverrideGetDigSpeedInferiors());
            addSorting(id, allBaseAfterGetDigSpeedSuperiors, baseSorting.getAfterGetDigSpeedSuperiors());
            addSorting(id, allBaseAfterGetDigSpeedInferiors, baseSorting.getAfterGetDigSpeedInferiors());

            addSorting(id, allBaseBeforeGetDistanceSqSuperiors, baseSorting.getBeforeGetDistanceSqSuperiors());
            addSorting(id, allBaseBeforeGetDistanceSqInferiors, baseSorting.getBeforeGetDistanceSqInferiors());
            addSorting(id, allBaseOverrideGetDistanceSqSuperiors, baseSorting.getOverrideGetDistanceSqSuperiors());
            addSorting(id, allBaseOverrideGetDistanceSqInferiors, baseSorting.getOverrideGetDistanceSqInferiors());
            addSorting(id, allBaseAfterGetDistanceSqSuperiors, baseSorting.getAfterGetDistanceSqSuperiors());
            addSorting(id, allBaseAfterGetDistanceSqInferiors, baseSorting.getAfterGetDistanceSqInferiors());

            addSorting(id, allBaseBeforeGetDistanceSqToEntitySuperiors, baseSorting.getBeforeGetDistanceSqToEntitySuperiors());
            addSorting(id, allBaseBeforeGetDistanceSqToEntityInferiors, baseSorting.getBeforeGetDistanceSqToEntityInferiors());
            addSorting(id, allBaseOverrideGetDistanceSqToEntitySuperiors, baseSorting.getOverrideGetDistanceSqToEntitySuperiors());
            addSorting(id, allBaseOverrideGetDistanceSqToEntityInferiors, baseSorting.getOverrideGetDistanceSqToEntityInferiors());
            addSorting(id, allBaseAfterGetDistanceSqToEntitySuperiors, baseSorting.getAfterGetDistanceSqToEntitySuperiors());
            addSorting(id, allBaseAfterGetDistanceSqToEntityInferiors, baseSorting.getAfterGetDistanceSqToEntityInferiors());

            addSorting(id, allBaseBeforeGetDistanceSqVecSuperiors, baseSorting.getBeforeGetDistanceSqVecSuperiors());
            addSorting(id, allBaseBeforeGetDistanceSqVecInferiors, baseSorting.getBeforeGetDistanceSqVecInferiors());
            addSorting(id, allBaseOverrideGetDistanceSqVecSuperiors, baseSorting.getOverrideGetDistanceSqVecSuperiors());
            addSorting(id, allBaseOverrideGetDistanceSqVecInferiors, baseSorting.getOverrideGetDistanceSqVecInferiors());
            addSorting(id, allBaseAfterGetDistanceSqVecSuperiors, baseSorting.getAfterGetDistanceSqVecSuperiors());
            addSorting(id, allBaseAfterGetDistanceSqVecInferiors, baseSorting.getAfterGetDistanceSqVecInferiors());

            addSorting(id, allBaseBeforeGetHurtSoundSuperiors, baseSorting.getBeforeGetHurtSoundSuperiors());
            addSorting(id, allBaseBeforeGetHurtSoundInferiors, baseSorting.getBeforeGetHurtSoundInferiors());
            addSorting(id, allBaseOverrideGetHurtSoundSuperiors, baseSorting.getOverrideGetHurtSoundSuperiors());
            addSorting(id, allBaseOverrideGetHurtSoundInferiors, baseSorting.getOverrideGetHurtSoundInferiors());
            addSorting(id, allBaseAfterGetHurtSoundSuperiors, baseSorting.getAfterGetHurtSoundSuperiors());
            addSorting(id, allBaseAfterGetHurtSoundInferiors, baseSorting.getAfterGetHurtSoundInferiors());

            addSorting(id, allBaseBeforeGetNameSuperiors, baseSorting.getBeforeGetNameSuperiors());
            addSorting(id, allBaseBeforeGetNameInferiors, baseSorting.getBeforeGetNameInferiors());
            addSorting(id, allBaseOverrideGetNameSuperiors, baseSorting.getOverrideGetNameSuperiors());
            addSorting(id, allBaseOverrideGetNameInferiors, baseSorting.getOverrideGetNameInferiors());
            addSorting(id, allBaseAfterGetNameSuperiors, baseSorting.getAfterGetNameSuperiors());
            addSorting(id, allBaseAfterGetNameInferiors, baseSorting.getAfterGetNameInferiors());

            addSorting(id, allBaseBeforeGetSizeSuperiors, baseSorting.getBeforeGetSizeSuperiors());
            addSorting(id, allBaseBeforeGetSizeInferiors, baseSorting.getBeforeGetSizeInferiors());
            addSorting(id, allBaseOverrideGetSizeSuperiors, baseSorting.getOverrideGetSizeSuperiors());
            addSorting(id, allBaseOverrideGetSizeInferiors, baseSorting.getOverrideGetSizeInferiors());
            addSorting(id, allBaseAfterGetSizeSuperiors, baseSorting.getAfterGetSizeSuperiors());
            addSorting(id, allBaseAfterGetSizeInferiors, baseSorting.getAfterGetSizeInferiors());

            addSorting(id, allBaseBeforeGetSleepTimerSuperiors, baseSorting.getBeforeGetSleepTimerSuperiors());
            addSorting(id, allBaseBeforeGetSleepTimerInferiors, baseSorting.getBeforeGetSleepTimerInferiors());
            addSorting(id, allBaseOverrideGetSleepTimerSuperiors, baseSorting.getOverrideGetSleepTimerSuperiors());
            addSorting(id, allBaseOverrideGetSleepTimerInferiors, baseSorting.getOverrideGetSleepTimerInferiors());
            addSorting(id, allBaseAfterGetSleepTimerSuperiors, baseSorting.getAfterGetSleepTimerSuperiors());
            addSorting(id, allBaseAfterGetSleepTimerInferiors, baseSorting.getAfterGetSleepTimerInferiors());

            addSorting(id, allBaseBeforeGetStandingEyeHeightSuperiors, baseSorting.getBeforeGetStandingEyeHeightSuperiors());
            addSorting(id, allBaseBeforeGetStandingEyeHeightInferiors, baseSorting.getBeforeGetStandingEyeHeightInferiors());
            addSorting(id, allBaseOverrideGetStandingEyeHeightSuperiors, baseSorting.getOverrideGetStandingEyeHeightSuperiors());
            addSorting(id, allBaseOverrideGetStandingEyeHeightInferiors, baseSorting.getOverrideGetStandingEyeHeightInferiors());
            addSorting(id, allBaseAfterGetStandingEyeHeightSuperiors, baseSorting.getAfterGetStandingEyeHeightSuperiors());
            addSorting(id, allBaseAfterGetStandingEyeHeightInferiors, baseSorting.getAfterGetStandingEyeHeightInferiors());

            addSorting(id, allBaseBeforeGiveExperiencePointsSuperiors, baseSorting.getBeforeGiveExperiencePointsSuperiors());
            addSorting(id, allBaseBeforeGiveExperiencePointsInferiors, baseSorting.getBeforeGiveExperiencePointsInferiors());
            addSorting(id, allBaseOverrideGiveExperiencePointsSuperiors, baseSorting.getOverrideGiveExperiencePointsSuperiors());
            addSorting(id, allBaseOverrideGiveExperiencePointsInferiors, baseSorting.getOverrideGiveExperiencePointsInferiors());
            addSorting(id, allBaseAfterGiveExperiencePointsSuperiors, baseSorting.getAfterGiveExperiencePointsSuperiors());
            addSorting(id, allBaseAfterGiveExperiencePointsInferiors, baseSorting.getAfterGiveExperiencePointsInferiors());

            addSorting(id, allBaseBeforeHandleWaterMovementSuperiors, baseSorting.getBeforeHandleWaterMovementSuperiors());
            addSorting(id, allBaseBeforeHandleWaterMovementInferiors, baseSorting.getBeforeHandleWaterMovementInferiors());
            addSorting(id, allBaseOverrideHandleWaterMovementSuperiors, baseSorting.getOverrideHandleWaterMovementSuperiors());
            addSorting(id, allBaseOverrideHandleWaterMovementInferiors, baseSorting.getOverrideHandleWaterMovementInferiors());
            addSorting(id, allBaseAfterHandleWaterMovementSuperiors, baseSorting.getAfterHandleWaterMovementSuperiors());
            addSorting(id, allBaseAfterHandleWaterMovementInferiors, baseSorting.getAfterHandleWaterMovementInferiors());

            addSorting(id, allBaseBeforeHealSuperiors, baseSorting.getBeforeHealSuperiors());
            addSorting(id, allBaseBeforeHealInferiors, baseSorting.getBeforeHealInferiors());
            addSorting(id, allBaseOverrideHealSuperiors, baseSorting.getOverrideHealSuperiors());
            addSorting(id, allBaseOverrideHealInferiors, baseSorting.getOverrideHealInferiors());
            addSorting(id, allBaseAfterHealSuperiors, baseSorting.getAfterHealSuperiors());
            addSorting(id, allBaseAfterHealInferiors, baseSorting.getAfterHealInferiors());

            addSorting(id, allBaseBeforeIsEntityInsideOpaqueBlockSuperiors, baseSorting.getBeforeIsEntityInsideOpaqueBlockSuperiors());
            addSorting(id, allBaseBeforeIsEntityInsideOpaqueBlockInferiors, baseSorting.getBeforeIsEntityInsideOpaqueBlockInferiors());
            addSorting(id, allBaseOverrideIsEntityInsideOpaqueBlockSuperiors, baseSorting.getOverrideIsEntityInsideOpaqueBlockSuperiors());
            addSorting(id, allBaseOverrideIsEntityInsideOpaqueBlockInferiors, baseSorting.getOverrideIsEntityInsideOpaqueBlockInferiors());
            addSorting(id, allBaseAfterIsEntityInsideOpaqueBlockSuperiors, baseSorting.getAfterIsEntityInsideOpaqueBlockSuperiors());
            addSorting(id, allBaseAfterIsEntityInsideOpaqueBlockInferiors, baseSorting.getAfterIsEntityInsideOpaqueBlockInferiors());

            addSorting(id, allBaseBeforeIsInWaterSuperiors, baseSorting.getBeforeIsInWaterSuperiors());
            addSorting(id, allBaseBeforeIsInWaterInferiors, baseSorting.getBeforeIsInWaterInferiors());
            addSorting(id, allBaseOverrideIsInWaterSuperiors, baseSorting.getOverrideIsInWaterSuperiors());
            addSorting(id, allBaseOverrideIsInWaterInferiors, baseSorting.getOverrideIsInWaterInferiors());
            addSorting(id, allBaseAfterIsInWaterSuperiors, baseSorting.getAfterIsInWaterSuperiors());
            addSorting(id, allBaseAfterIsInWaterInferiors, baseSorting.getAfterIsInWaterInferiors());

            addSorting(id, allBaseBeforeIsOnLadderSuperiors, baseSorting.getBeforeIsOnLadderSuperiors());
            addSorting(id, allBaseBeforeIsOnLadderInferiors, baseSorting.getBeforeIsOnLadderInferiors());
            addSorting(id, allBaseOverrideIsOnLadderSuperiors, baseSorting.getOverrideIsOnLadderSuperiors());
            addSorting(id, allBaseOverrideIsOnLadderInferiors, baseSorting.getOverrideIsOnLadderInferiors());
            addSorting(id, allBaseAfterIsOnLadderSuperiors, baseSorting.getAfterIsOnLadderSuperiors());
            addSorting(id, allBaseAfterIsOnLadderInferiors, baseSorting.getAfterIsOnLadderInferiors());

            addSorting(id, allBaseBeforeIsShiftKeyDownSuperiors, baseSorting.getBeforeIsShiftKeyDownSuperiors());
            addSorting(id, allBaseBeforeIsShiftKeyDownInferiors, baseSorting.getBeforeIsShiftKeyDownInferiors());
            addSorting(id, allBaseOverrideIsShiftKeyDownSuperiors, baseSorting.getOverrideIsShiftKeyDownSuperiors());
            addSorting(id, allBaseOverrideIsShiftKeyDownInferiors, baseSorting.getOverrideIsShiftKeyDownInferiors());
            addSorting(id, allBaseAfterIsShiftKeyDownSuperiors, baseSorting.getAfterIsShiftKeyDownSuperiors());
            addSorting(id, allBaseAfterIsShiftKeyDownInferiors, baseSorting.getAfterIsShiftKeyDownInferiors());

            addSorting(id, allBaseBeforeIsSleepingSuperiors, baseSorting.getBeforeIsSleepingSuperiors());
            addSorting(id, allBaseBeforeIsSleepingInferiors, baseSorting.getBeforeIsSleepingInferiors());
            addSorting(id, allBaseOverrideIsSleepingSuperiors, baseSorting.getOverrideIsSleepingSuperiors());
            addSorting(id, allBaseOverrideIsSleepingInferiors, baseSorting.getOverrideIsSleepingInferiors());
            addSorting(id, allBaseAfterIsSleepingSuperiors, baseSorting.getAfterIsSleepingSuperiors());
            addSorting(id, allBaseAfterIsSleepingInferiors, baseSorting.getAfterIsSleepingInferiors());

            addSorting(id, allBaseBeforeIsSprintingSuperiors, baseSorting.getBeforeIsSprintingSuperiors());
            addSorting(id, allBaseBeforeIsSprintingInferiors, baseSorting.getBeforeIsSprintingInferiors());
            addSorting(id, allBaseOverrideIsSprintingSuperiors, baseSorting.getOverrideIsSprintingSuperiors());
            addSorting(id, allBaseOverrideIsSprintingInferiors, baseSorting.getOverrideIsSprintingInferiors());
            addSorting(id, allBaseAfterIsSprintingSuperiors, baseSorting.getAfterIsSprintingSuperiors());
            addSorting(id, allBaseAfterIsSprintingInferiors, baseSorting.getAfterIsSprintingInferiors());

            addSorting(id, allBaseBeforeJumpSuperiors, baseSorting.getBeforeJumpSuperiors());
            addSorting(id, allBaseBeforeJumpInferiors, baseSorting.getBeforeJumpInferiors());
            addSorting(id, allBaseOverrideJumpSuperiors, baseSorting.getOverrideJumpSuperiors());
            addSorting(id, allBaseOverrideJumpInferiors, baseSorting.getOverrideJumpInferiors());
            addSorting(id, allBaseAfterJumpSuperiors, baseSorting.getAfterJumpSuperiors());
            addSorting(id, allBaseAfterJumpInferiors, baseSorting.getAfterJumpInferiors());

            addSorting(id, allBaseBeforeKnockBackSuperiors, baseSorting.getBeforeKnockBackSuperiors());
            addSorting(id, allBaseBeforeKnockBackInferiors, baseSorting.getBeforeKnockBackInferiors());
            addSorting(id, allBaseOverrideKnockBackSuperiors, baseSorting.getOverrideKnockBackSuperiors());
            addSorting(id, allBaseOverrideKnockBackInferiors, baseSorting.getOverrideKnockBackInferiors());
            addSorting(id, allBaseAfterKnockBackSuperiors, baseSorting.getAfterKnockBackSuperiors());
            addSorting(id, allBaseAfterKnockBackInferiors, baseSorting.getAfterKnockBackInferiors());

            addSorting(id, allBaseBeforeLivingTickSuperiors, baseSorting.getBeforeLivingTickSuperiors());
            addSorting(id, allBaseBeforeLivingTickInferiors, baseSorting.getBeforeLivingTickInferiors());
            addSorting(id, allBaseOverrideLivingTickSuperiors, baseSorting.getOverrideLivingTickSuperiors());
            addSorting(id, allBaseOverrideLivingTickInferiors, baseSorting.getOverrideLivingTickInferiors());
            addSorting(id, allBaseAfterLivingTickSuperiors, baseSorting.getAfterLivingTickSuperiors());
            addSorting(id, allBaseAfterLivingTickInferiors, baseSorting.getAfterLivingTickInferiors());

            addSorting(id, allBaseBeforeMoveSuperiors, baseSorting.getBeforeMoveSuperiors());
            addSorting(id, allBaseBeforeMoveInferiors, baseSorting.getBeforeMoveInferiors());
            addSorting(id, allBaseOverrideMoveSuperiors, baseSorting.getOverrideMoveSuperiors());
            addSorting(id, allBaseOverrideMoveInferiors, baseSorting.getOverrideMoveInferiors());
            addSorting(id, allBaseAfterMoveSuperiors, baseSorting.getAfterMoveSuperiors());
            addSorting(id, allBaseAfterMoveInferiors, baseSorting.getAfterMoveInferiors());

            addSorting(id, allBaseBeforeMoveRelativeSuperiors, baseSorting.getBeforeMoveRelativeSuperiors());
            addSorting(id, allBaseBeforeMoveRelativeInferiors, baseSorting.getBeforeMoveRelativeInferiors());
            addSorting(id, allBaseOverrideMoveRelativeSuperiors, baseSorting.getOverrideMoveRelativeSuperiors());
            addSorting(id, allBaseOverrideMoveRelativeInferiors, baseSorting.getOverrideMoveRelativeInferiors());
            addSorting(id, allBaseAfterMoveRelativeSuperiors, baseSorting.getAfterMoveRelativeSuperiors());
            addSorting(id, allBaseAfterMoveRelativeInferiors, baseSorting.getAfterMoveRelativeInferiors());

            addSorting(id, allBaseBeforeOnDeathSuperiors, baseSorting.getBeforeOnDeathSuperiors());
            addSorting(id, allBaseBeforeOnDeathInferiors, baseSorting.getBeforeOnDeathInferiors());
            addSorting(id, allBaseOverrideOnDeathSuperiors, baseSorting.getOverrideOnDeathSuperiors());
            addSorting(id, allBaseOverrideOnDeathInferiors, baseSorting.getOverrideOnDeathInferiors());
            addSorting(id, allBaseAfterOnDeathSuperiors, baseSorting.getAfterOnDeathSuperiors());
            addSorting(id, allBaseAfterOnDeathInferiors, baseSorting.getAfterOnDeathInferiors());

            addSorting(id, allBaseBeforeOnKillEntitySuperiors, baseSorting.getBeforeOnKillEntitySuperiors());
            addSorting(id, allBaseBeforeOnKillEntityInferiors, baseSorting.getBeforeOnKillEntityInferiors());
            addSorting(id, allBaseOverrideOnKillEntitySuperiors, baseSorting.getOverrideOnKillEntitySuperiors());
            addSorting(id, allBaseOverrideOnKillEntityInferiors, baseSorting.getOverrideOnKillEntityInferiors());
            addSorting(id, allBaseAfterOnKillEntitySuperiors, baseSorting.getAfterOnKillEntitySuperiors());
            addSorting(id, allBaseAfterOnKillEntityInferiors, baseSorting.getAfterOnKillEntityInferiors());

            addSorting(id, allBaseBeforeOnLivingFallSuperiors, baseSorting.getBeforeOnLivingFallSuperiors());
            addSorting(id, allBaseBeforeOnLivingFallInferiors, baseSorting.getBeforeOnLivingFallInferiors());
            addSorting(id, allBaseOverrideOnLivingFallSuperiors, baseSorting.getOverrideOnLivingFallSuperiors());
            addSorting(id, allBaseOverrideOnLivingFallInferiors, baseSorting.getOverrideOnLivingFallInferiors());
            addSorting(id, allBaseAfterOnLivingFallSuperiors, baseSorting.getAfterOnLivingFallSuperiors());
            addSorting(id, allBaseAfterOnLivingFallInferiors, baseSorting.getAfterOnLivingFallInferiors());

            addSorting(id, allBaseBeforeOnStruckByLightningSuperiors, baseSorting.getBeforeOnStruckByLightningSuperiors());
            addSorting(id, allBaseBeforeOnStruckByLightningInferiors, baseSorting.getBeforeOnStruckByLightningInferiors());
            addSorting(id, allBaseOverrideOnStruckByLightningSuperiors, baseSorting.getOverrideOnStruckByLightningSuperiors());
            addSorting(id, allBaseOverrideOnStruckByLightningInferiors, baseSorting.getOverrideOnStruckByLightningInferiors());
            addSorting(id, allBaseAfterOnStruckByLightningSuperiors, baseSorting.getAfterOnStruckByLightningSuperiors());
            addSorting(id, allBaseAfterOnStruckByLightningInferiors, baseSorting.getAfterOnStruckByLightningInferiors());

            addSorting(id, allBaseBeforePickSuperiors, baseSorting.getBeforePickSuperiors());
            addSorting(id, allBaseBeforePickInferiors, baseSorting.getBeforePickInferiors());
            addSorting(id, allBaseOverridePickSuperiors, baseSorting.getOverridePickSuperiors());
            addSorting(id, allBaseOverridePickInferiors, baseSorting.getOverridePickInferiors());
            addSorting(id, allBaseAfterPickSuperiors, baseSorting.getAfterPickSuperiors());
            addSorting(id, allBaseAfterPickInferiors, baseSorting.getAfterPickInferiors());

            addSorting(id, allBaseBeforePlayerTickSuperiors, baseSorting.getBeforePlayerTickSuperiors());
            addSorting(id, allBaseBeforePlayerTickInferiors, baseSorting.getBeforePlayerTickInferiors());
            addSorting(id, allBaseOverridePlayerTickSuperiors, baseSorting.getOverridePlayerTickSuperiors());
            addSorting(id, allBaseOverridePlayerTickInferiors, baseSorting.getOverridePlayerTickInferiors());
            addSorting(id, allBaseAfterPlayerTickSuperiors, baseSorting.getAfterPlayerTickSuperiors());
            addSorting(id, allBaseAfterPlayerTickInferiors, baseSorting.getAfterPlayerTickInferiors());

            addSorting(id, allBaseBeforePlayStepSoundSuperiors, baseSorting.getBeforePlayStepSoundSuperiors());
            addSorting(id, allBaseBeforePlayStepSoundInferiors, baseSorting.getBeforePlayStepSoundInferiors());
            addSorting(id, allBaseOverridePlayStepSoundSuperiors, baseSorting.getOverridePlayStepSoundSuperiors());
            addSorting(id, allBaseOverridePlayStepSoundInferiors, baseSorting.getOverridePlayStepSoundInferiors());
            addSorting(id, allBaseAfterPlayStepSoundSuperiors, baseSorting.getAfterPlayStepSoundSuperiors());
            addSorting(id, allBaseAfterPlayStepSoundInferiors, baseSorting.getAfterPlayStepSoundInferiors());

            addSorting(id, allBaseBeforePushOutOfBlocksSuperiors, baseSorting.getBeforePushOutOfBlocksSuperiors());
            addSorting(id, allBaseBeforePushOutOfBlocksInferiors, baseSorting.getBeforePushOutOfBlocksInferiors());
            addSorting(id, allBaseOverridePushOutOfBlocksSuperiors, baseSorting.getOverridePushOutOfBlocksSuperiors());
            addSorting(id, allBaseOverridePushOutOfBlocksInferiors, baseSorting.getOverridePushOutOfBlocksInferiors());
            addSorting(id, allBaseAfterPushOutOfBlocksSuperiors, baseSorting.getAfterPushOutOfBlocksSuperiors());
            addSorting(id, allBaseAfterPushOutOfBlocksInferiors, baseSorting.getAfterPushOutOfBlocksInferiors());

            addSorting(id, allBaseBeforeReadSuperiors, baseSorting.getBeforeReadSuperiors());
            addSorting(id, allBaseBeforeReadInferiors, baseSorting.getBeforeReadInferiors());
            addSorting(id, allBaseOverrideReadSuperiors, baseSorting.getOverrideReadSuperiors());
            addSorting(id, allBaseOverrideReadInferiors, baseSorting.getOverrideReadInferiors());
            addSorting(id, allBaseAfterReadSuperiors, baseSorting.getAfterReadSuperiors());
            addSorting(id, allBaseAfterReadInferiors, baseSorting.getAfterReadInferiors());

            addSorting(id, allBaseBeforeRemoveSuperiors, baseSorting.getBeforeRemoveSuperiors());
            addSorting(id, allBaseBeforeRemoveInferiors, baseSorting.getBeforeRemoveInferiors());
            addSorting(id, allBaseOverrideRemoveSuperiors, baseSorting.getOverrideRemoveSuperiors());
            addSorting(id, allBaseOverrideRemoveInferiors, baseSorting.getOverrideRemoveInferiors());
            addSorting(id, allBaseAfterRemoveSuperiors, baseSorting.getAfterRemoveSuperiors());
            addSorting(id, allBaseAfterRemoveInferiors, baseSorting.getAfterRemoveInferiors());

            addSorting(id, allBaseBeforeSetEntityActionStateSuperiors, baseSorting.getBeforeSetEntityActionStateSuperiors());
            addSorting(id, allBaseBeforeSetEntityActionStateInferiors, baseSorting.getBeforeSetEntityActionStateInferiors());
            addSorting(id, allBaseOverrideSetEntityActionStateSuperiors, baseSorting.getOverrideSetEntityActionStateSuperiors());
            addSorting(id, allBaseOverrideSetEntityActionStateInferiors, baseSorting.getOverrideSetEntityActionStateInferiors());
            addSorting(id, allBaseAfterSetEntityActionStateSuperiors, baseSorting.getAfterSetEntityActionStateSuperiors());
            addSorting(id, allBaseAfterSetEntityActionStateInferiors, baseSorting.getAfterSetEntityActionStateInferiors());

            addSorting(id, allBaseBeforeSetPositionSuperiors, baseSorting.getBeforeSetPositionSuperiors());
            addSorting(id, allBaseBeforeSetPositionInferiors, baseSorting.getBeforeSetPositionInferiors());
            addSorting(id, allBaseOverrideSetPositionSuperiors, baseSorting.getOverrideSetPositionSuperiors());
            addSorting(id, allBaseOverrideSetPositionInferiors, baseSorting.getOverrideSetPositionInferiors());
            addSorting(id, allBaseAfterSetPositionSuperiors, baseSorting.getAfterSetPositionSuperiors());
            addSorting(id, allBaseAfterSetPositionInferiors, baseSorting.getAfterSetPositionInferiors());

            addSorting(id, allBaseBeforeSetPositionAndRotationSuperiors, baseSorting.getBeforeSetPositionAndRotationSuperiors());
            addSorting(id, allBaseBeforeSetPositionAndRotationInferiors, baseSorting.getBeforeSetPositionAndRotationInferiors());
            addSorting(id, allBaseOverrideSetPositionAndRotationSuperiors, baseSorting.getOverrideSetPositionAndRotationSuperiors());
            addSorting(id, allBaseOverrideSetPositionAndRotationInferiors, baseSorting.getOverrideSetPositionAndRotationInferiors());
            addSorting(id, allBaseAfterSetPositionAndRotationSuperiors, baseSorting.getAfterSetPositionAndRotationSuperiors());
            addSorting(id, allBaseAfterSetPositionAndRotationInferiors, baseSorting.getAfterSetPositionAndRotationInferiors());

            addSorting(id, allBaseBeforeSetSneakingSuperiors, baseSorting.getBeforeSetSneakingSuperiors());
            addSorting(id, allBaseBeforeSetSneakingInferiors, baseSorting.getBeforeSetSneakingInferiors());
            addSorting(id, allBaseOverrideSetSneakingSuperiors, baseSorting.getOverrideSetSneakingSuperiors());
            addSorting(id, allBaseOverrideSetSneakingInferiors, baseSorting.getOverrideSetSneakingInferiors());
            addSorting(id, allBaseAfterSetSneakingSuperiors, baseSorting.getAfterSetSneakingSuperiors());
            addSorting(id, allBaseAfterSetSneakingInferiors, baseSorting.getAfterSetSneakingInferiors());

            addSorting(id, allBaseBeforeSetSprintingSuperiors, baseSorting.getBeforeSetSprintingSuperiors());
            addSorting(id, allBaseBeforeSetSprintingInferiors, baseSorting.getBeforeSetSprintingInferiors());
            addSorting(id, allBaseOverrideSetSprintingSuperiors, baseSorting.getOverrideSetSprintingSuperiors());
            addSorting(id, allBaseOverrideSetSprintingInferiors, baseSorting.getOverrideSetSprintingInferiors());
            addSorting(id, allBaseAfterSetSprintingSuperiors, baseSorting.getAfterSetSprintingSuperiors());
            addSorting(id, allBaseAfterSetSprintingInferiors, baseSorting.getAfterSetSprintingInferiors());

            addSorting(id, allBaseBeforeSwingArmSuperiors, baseSorting.getBeforeSwingArmSuperiors());
            addSorting(id, allBaseBeforeSwingArmInferiors, baseSorting.getBeforeSwingArmInferiors());
            addSorting(id, allBaseOverrideSwingArmSuperiors, baseSorting.getOverrideSwingArmSuperiors());
            addSorting(id, allBaseOverrideSwingArmInferiors, baseSorting.getOverrideSwingArmInferiors());
            addSorting(id, allBaseAfterSwingArmSuperiors, baseSorting.getAfterSwingArmSuperiors());
            addSorting(id, allBaseAfterSwingArmInferiors, baseSorting.getAfterSwingArmInferiors());

            addSorting(id, allBaseBeforeTickSuperiors, baseSorting.getBeforeTickSuperiors());
            addSorting(id, allBaseBeforeTickInferiors, baseSorting.getBeforeTickInferiors());
            addSorting(id, allBaseOverrideTickSuperiors, baseSorting.getOverrideTickSuperiors());
            addSorting(id, allBaseOverrideTickInferiors, baseSorting.getOverrideTickInferiors());
            addSorting(id, allBaseAfterTickSuperiors, baseSorting.getAfterTickSuperiors());
            addSorting(id, allBaseAfterTickInferiors, baseSorting.getAfterTickInferiors());

            addSorting(id, allBaseBeforeTravelSuperiors, baseSorting.getBeforeTravelSuperiors());
            addSorting(id, allBaseBeforeTravelInferiors, baseSorting.getBeforeTravelInferiors());
            addSorting(id, allBaseOverrideTravelSuperiors, baseSorting.getOverrideTravelSuperiors());
            addSorting(id, allBaseOverrideTravelInferiors, baseSorting.getOverrideTravelInferiors());
            addSorting(id, allBaseAfterTravelSuperiors, baseSorting.getAfterTravelSuperiors());
            addSorting(id, allBaseAfterTravelInferiors, baseSorting.getAfterTravelInferiors());

            addSorting(id, allBaseBeforeTrySleepSuperiors, baseSorting.getBeforeTrySleepSuperiors());
            addSorting(id, allBaseBeforeTrySleepInferiors, baseSorting.getBeforeTrySleepInferiors());
            addSorting(id, allBaseOverrideTrySleepSuperiors, baseSorting.getOverrideTrySleepSuperiors());
            addSorting(id, allBaseOverrideTrySleepInferiors, baseSorting.getOverrideTrySleepInferiors());
            addSorting(id, allBaseAfterTrySleepSuperiors, baseSorting.getAfterTrySleepSuperiors());
            addSorting(id, allBaseAfterTrySleepInferiors, baseSorting.getAfterTrySleepInferiors());

            addSorting(id, allBaseBeforeUpdateEntityActionStateSuperiors, baseSorting.getBeforeUpdateEntityActionStateSuperiors());
            addSorting(id, allBaseBeforeUpdateEntityActionStateInferiors, baseSorting.getBeforeUpdateEntityActionStateInferiors());
            addSorting(id, allBaseOverrideUpdateEntityActionStateSuperiors, baseSorting.getOverrideUpdateEntityActionStateSuperiors());
            addSorting(id, allBaseOverrideUpdateEntityActionStateInferiors, baseSorting.getOverrideUpdateEntityActionStateInferiors());
            addSorting(id, allBaseAfterUpdateEntityActionStateSuperiors, baseSorting.getAfterUpdateEntityActionStateSuperiors());
            addSorting(id, allBaseAfterUpdateEntityActionStateInferiors, baseSorting.getAfterUpdateEntityActionStateInferiors());

            addSorting(id, allBaseBeforeUpdatePotionEffectsSuperiors, baseSorting.getBeforeUpdatePotionEffectsSuperiors());
            addSorting(id, allBaseBeforeUpdatePotionEffectsInferiors, baseSorting.getBeforeUpdatePotionEffectsInferiors());
            addSorting(id, allBaseOverrideUpdatePotionEffectsSuperiors, baseSorting.getOverrideUpdatePotionEffectsSuperiors());
            addSorting(id, allBaseOverrideUpdatePotionEffectsInferiors, baseSorting.getOverrideUpdatePotionEffectsInferiors());
            addSorting(id, allBaseAfterUpdatePotionEffectsSuperiors, baseSorting.getAfterUpdatePotionEffectsSuperiors());
            addSorting(id, allBaseAfterUpdatePotionEffectsInferiors, baseSorting.getAfterUpdatePotionEffectsInferiors());

            addSorting(id, allBaseBeforeUpdateRiddenSuperiors, baseSorting.getBeforeUpdateRiddenSuperiors());
            addSorting(id, allBaseBeforeUpdateRiddenInferiors, baseSorting.getBeforeUpdateRiddenInferiors());
            addSorting(id, allBaseOverrideUpdateRiddenSuperiors, baseSorting.getOverrideUpdateRiddenSuperiors());
            addSorting(id, allBaseOverrideUpdateRiddenInferiors, baseSorting.getOverrideUpdateRiddenInferiors());
            addSorting(id, allBaseAfterUpdateRiddenSuperiors, baseSorting.getAfterUpdateRiddenSuperiors());
            addSorting(id, allBaseAfterUpdateRiddenInferiors, baseSorting.getAfterUpdateRiddenInferiors());

            addSorting(id, allBaseBeforeWakeUpPlayerSuperiors, baseSorting.getBeforeWakeUpPlayerSuperiors());
            addSorting(id, allBaseBeforeWakeUpPlayerInferiors, baseSorting.getBeforeWakeUpPlayerInferiors());
            addSorting(id, allBaseOverrideWakeUpPlayerSuperiors, baseSorting.getOverrideWakeUpPlayerSuperiors());
            addSorting(id, allBaseOverrideWakeUpPlayerInferiors, baseSorting.getOverrideWakeUpPlayerInferiors());
            addSorting(id, allBaseAfterWakeUpPlayerSuperiors, baseSorting.getAfterWakeUpPlayerSuperiors());
            addSorting(id, allBaseAfterWakeUpPlayerInferiors, baseSorting.getAfterWakeUpPlayerInferiors());

            addSorting(id, allBaseBeforeWriteWithoutTypeIdSuperiors, baseSorting.getBeforeWriteWithoutTypeIdSuperiors());
            addSorting(id, allBaseBeforeWriteWithoutTypeIdInferiors, baseSorting.getBeforeWriteWithoutTypeIdInferiors());
            addSorting(id, allBaseOverrideWriteWithoutTypeIdSuperiors, baseSorting.getOverrideWriteWithoutTypeIdSuperiors());
            addSorting(id, allBaseOverrideWriteWithoutTypeIdInferiors, baseSorting.getOverrideWriteWithoutTypeIdInferiors());
            addSorting(id, allBaseAfterWriteWithoutTypeIdSuperiors, baseSorting.getAfterWriteWithoutTypeIdSuperiors());
            addSorting(id, allBaseAfterWriteWithoutTypeIdInferiors, baseSorting.getAfterWriteWithoutTypeIdInferiors());
        }

        addMethod(id, baseClass, beforeLocalConstructingHookTypes, "beforeLocalConstructing", MinecraftServer.class, ServerWorld.class, GameProfile.class, PlayerInteractionManager.class);
        addMethod(id, baseClass, afterLocalConstructingHookTypes, "afterLocalConstructing", MinecraftServer.class, ServerWorld.class, GameProfile.class, PlayerInteractionManager.class);

        addMethod(id, baseClass, beforeAddExhaustionHookTypes, "beforeAddExhaustion", float.class);
        addMethod(id, baseClass, overrideAddExhaustionHookTypes, "addExhaustion", float.class);
        addMethod(id, baseClass, afterAddExhaustionHookTypes, "afterAddExhaustion", float.class);

        addMethod(id, baseClass, beforeAddExperienceLevelHookTypes, "beforeAddExperienceLevel", int.class);
        addMethod(id, baseClass, overrideAddExperienceLevelHookTypes, "addExperienceLevel", int.class);
        addMethod(id, baseClass, afterAddExperienceLevelHookTypes, "afterAddExperienceLevel", int.class);

        addMethod(id, baseClass, beforeAddMovementStatHookTypes, "beforeAddMovementStat", double.class, double.class, double.class);
        addMethod(id, baseClass, overrideAddMovementStatHookTypes, "addMovementStat", double.class, double.class, double.class);
        addMethod(id, baseClass, afterAddMovementStatHookTypes, "afterAddMovementStat", double.class, double.class, double.class);

        addMethod(id, baseClass, beforeAddStatHookTypes, "beforeAddStat", Stat.class, int.class);
        addMethod(id, baseClass, overrideAddStatHookTypes, "addStat", Stat.class, int.class);
        addMethod(id, baseClass, afterAddStatHookTypes, "afterAddStat", Stat.class, int.class);

        addMethod(id, baseClass, beforeAreEyesInFluidHookTypes, "beforeAreEyesInFluid", Tag.class, boolean.class);
        addMethod(id, baseClass, overrideAreEyesInFluidHookTypes, "areEyesInFluid", Tag.class, boolean.class);
        addMethod(id, baseClass, afterAreEyesInFluidHookTypes, "afterAreEyesInFluid", Tag.class, boolean.class);

        addMethod(id, baseClass, beforeAttackEntityFromHookTypes, "beforeAttackEntityFrom", DamageSource.class, float.class);
        addMethod(id, baseClass, overrideAttackEntityFromHookTypes, "attackEntityFrom", DamageSource.class, float.class);
        addMethod(id, baseClass, afterAttackEntityFromHookTypes, "afterAttackEntityFrom", DamageSource.class, float.class);

        addMethod(id, baseClass, beforeAttackTargetEntityWithCurrentItemHookTypes, "beforeAttackTargetEntityWithCurrentItem", Entity.class);
        addMethod(id, baseClass, overrideAttackTargetEntityWithCurrentItemHookTypes, "attackTargetEntityWithCurrentItem", Entity.class);
        addMethod(id, baseClass, afterAttackTargetEntityWithCurrentItemHookTypes, "afterAttackTargetEntityWithCurrentItem", Entity.class);

        addMethod(id, baseClass, beforeCanBreatheUnderwaterHookTypes, "beforeCanBreatheUnderwater");
        addMethod(id, baseClass, overrideCanBreatheUnderwaterHookTypes, "canBreatheUnderwater");
        addMethod(id, baseClass, afterCanBreatheUnderwaterHookTypes, "afterCanBreatheUnderwater");

        addMethod(id, baseClass, beforeCanHarvestBlockHookTypes, "beforeCanHarvestBlock", BlockState.class);
        addMethod(id, baseClass, overrideCanHarvestBlockHookTypes, "canHarvestBlock", BlockState.class);
        addMethod(id, baseClass, afterCanHarvestBlockHookTypes, "afterCanHarvestBlock", BlockState.class);

        addMethod(id, baseClass, beforeCanPlayerEditHookTypes, "beforeCanPlayerEdit", BlockPos.class, Direction.class, ItemStack.class);
        addMethod(id, baseClass, overrideCanPlayerEditHookTypes, "canPlayerEdit", BlockPos.class, Direction.class, ItemStack.class);
        addMethod(id, baseClass, afterCanPlayerEditHookTypes, "afterCanPlayerEdit", BlockPos.class, Direction.class, ItemStack.class);

        addMethod(id, baseClass, beforeCanTriggerWalkingHookTypes, "beforeCanTriggerWalking");
        addMethod(id, baseClass, overrideCanTriggerWalkingHookTypes, "canTriggerWalking");
        addMethod(id, baseClass, afterCanTriggerWalkingHookTypes, "afterCanTriggerWalking");

        addMethod(id, baseClass, beforeCopyFromHookTypes, "beforeCopyFrom", ServerPlayerEntity.class, boolean.class);
        addMethod(id, baseClass, overrideCopyFromHookTypes, "copyFrom", ServerPlayerEntity.class, boolean.class);
        addMethod(id, baseClass, afterCopyFromHookTypes, "afterCopyFrom", ServerPlayerEntity.class, boolean.class);

        addMethod(id, baseClass, beforeDamageEntityHookTypes, "beforeDamageEntity", DamageSource.class, float.class);
        addMethod(id, baseClass, overrideDamageEntityHookTypes, "damageEntity", DamageSource.class, float.class);
        addMethod(id, baseClass, afterDamageEntityHookTypes, "afterDamageEntity", DamageSource.class, float.class);

        addMethod(id, baseClass, beforeDropItemHookTypes, "beforeDropItem", ItemStack.class, boolean.class, boolean.class);
        addMethod(id, baseClass, overrideDropItemHookTypes, "dropItem", ItemStack.class, boolean.class, boolean.class);
        addMethod(id, baseClass, afterDropItemHookTypes, "afterDropItem", ItemStack.class, boolean.class, boolean.class);

        addMethod(id, baseClass, beforeGetAIMoveSpeedHookTypes, "beforeGetAIMoveSpeed");
        addMethod(id, baseClass, overrideGetAIMoveSpeedHookTypes, "getAIMoveSpeed");
        addMethod(id, baseClass, afterGetAIMoveSpeedHookTypes, "afterGetAIMoveSpeed");

        addMethod(id, baseClass, beforeGetBrightnessHookTypes, "beforeGetBrightness");
        addMethod(id, baseClass, overrideGetBrightnessHookTypes, "getBrightness");
        addMethod(id, baseClass, afterGetBrightnessHookTypes, "afterGetBrightness");

        addMethod(id, baseClass, beforeGetDigSpeedHookTypes, "beforeGetDigSpeed", BlockState.class, BlockPos.class);
        addMethod(id, baseClass, overrideGetDigSpeedHookTypes, "getDigSpeed", BlockState.class, BlockPos.class);
        addMethod(id, baseClass, afterGetDigSpeedHookTypes, "afterGetDigSpeed", BlockState.class, BlockPos.class);

        addMethod(id, baseClass, beforeGetDistanceSqHookTypes, "beforeGetDistanceSq", double.class, double.class, double.class);
        addMethod(id, baseClass, overrideGetDistanceSqHookTypes, "getDistanceSq", double.class, double.class, double.class);
        addMethod(id, baseClass, afterGetDistanceSqHookTypes, "afterGetDistanceSq", double.class, double.class, double.class);

        addMethod(id, baseClass, beforeGetDistanceSqToEntityHookTypes, "beforeGetDistanceSqToEntity", Entity.class);
        addMethod(id, baseClass, overrideGetDistanceSqToEntityHookTypes, "getDistanceSqToEntity", Entity.class);
        addMethod(id, baseClass, afterGetDistanceSqToEntityHookTypes, "afterGetDistanceSqToEntity", Entity.class);

        addMethod(id, baseClass, beforeGetDistanceSqVecHookTypes, "beforeGetDistanceSqVec", Vec3d.class);
        addMethod(id, baseClass, overrideGetDistanceSqVecHookTypes, "getDistanceSqVec", Vec3d.class);
        addMethod(id, baseClass, afterGetDistanceSqVecHookTypes, "afterGetDistanceSqVec", Vec3d.class);

        addMethod(id, baseClass, beforeGetHurtSoundHookTypes, "beforeGetHurtSound", DamageSource.class);
        addMethod(id, baseClass, overrideGetHurtSoundHookTypes, "getHurtSound", DamageSource.class);
        addMethod(id, baseClass, afterGetHurtSoundHookTypes, "afterGetHurtSound", DamageSource.class);

        addMethod(id, baseClass, beforeGetNameHookTypes, "beforeGetName");
        addMethod(id, baseClass, overrideGetNameHookTypes, "getName");
        addMethod(id, baseClass, afterGetNameHookTypes, "afterGetName");

        addMethod(id, baseClass, beforeGetSizeHookTypes, "beforeGetSize", Pose.class);
        addMethod(id, baseClass, overrideGetSizeHookTypes, "getSize", Pose.class);
        addMethod(id, baseClass, afterGetSizeHookTypes, "afterGetSize", Pose.class);

        addMethod(id, baseClass, beforeGetSleepTimerHookTypes, "beforeGetSleepTimer");
        addMethod(id, baseClass, overrideGetSleepTimerHookTypes, "getSleepTimer");
        addMethod(id, baseClass, afterGetSleepTimerHookTypes, "afterGetSleepTimer");

        addMethod(id, baseClass, beforeGetStandingEyeHeightHookTypes, "beforeGetStandingEyeHeight", Pose.class, EntitySize.class);
        addMethod(id, baseClass, overrideGetStandingEyeHeightHookTypes, "getStandingEyeHeight", Pose.class, EntitySize.class);
        addMethod(id, baseClass, afterGetStandingEyeHeightHookTypes, "afterGetStandingEyeHeight", Pose.class, EntitySize.class);

        addMethod(id, baseClass, beforeGiveExperiencePointsHookTypes, "beforeGiveExperiencePoints", int.class);
        addMethod(id, baseClass, overrideGiveExperiencePointsHookTypes, "giveExperiencePoints", int.class);
        addMethod(id, baseClass, afterGiveExperiencePointsHookTypes, "afterGiveExperiencePoints", int.class);

        addMethod(id, baseClass, beforeHandleWaterMovementHookTypes, "beforeHandleWaterMovement");
        addMethod(id, baseClass, overrideHandleWaterMovementHookTypes, "handleWaterMovement");
        addMethod(id, baseClass, afterHandleWaterMovementHookTypes, "afterHandleWaterMovement");

        addMethod(id, baseClass, beforeHealHookTypes, "beforeHeal", float.class);
        addMethod(id, baseClass, overrideHealHookTypes, "heal", float.class);
        addMethod(id, baseClass, afterHealHookTypes, "afterHeal", float.class);

        addMethod(id, baseClass, beforeIsEntityInsideOpaqueBlockHookTypes, "beforeIsEntityInsideOpaqueBlock");
        addMethod(id, baseClass, overrideIsEntityInsideOpaqueBlockHookTypes, "isEntityInsideOpaqueBlock");
        addMethod(id, baseClass, afterIsEntityInsideOpaqueBlockHookTypes, "afterIsEntityInsideOpaqueBlock");

        addMethod(id, baseClass, beforeIsInWaterHookTypes, "beforeIsInWater");
        addMethod(id, baseClass, overrideIsInWaterHookTypes, "isInWater");
        addMethod(id, baseClass, afterIsInWaterHookTypes, "afterIsInWater");

        addMethod(id, baseClass, beforeIsOnLadderHookTypes, "beforeIsOnLadder");
        addMethod(id, baseClass, overrideIsOnLadderHookTypes, "isOnLadder");
        addMethod(id, baseClass, afterIsOnLadderHookTypes, "afterIsOnLadder");

        addMethod(id, baseClass, beforeIsShiftKeyDownHookTypes, "beforeIsShiftKeyDown");
        addMethod(id, baseClass, overrideIsShiftKeyDownHookTypes, "isShiftKeyDown");
        addMethod(id, baseClass, afterIsShiftKeyDownHookTypes, "afterIsShiftKeyDown");

        addMethod(id, baseClass, beforeIsSleepingHookTypes, "beforeIsSleeping");
        addMethod(id, baseClass, overrideIsSleepingHookTypes, "isSleeping");
        addMethod(id, baseClass, afterIsSleepingHookTypes, "afterIsSleeping");

        addMethod(id, baseClass, beforeIsSprintingHookTypes, "beforeIsSprinting");
        addMethod(id, baseClass, overrideIsSprintingHookTypes, "isSprinting");
        addMethod(id, baseClass, afterIsSprintingHookTypes, "afterIsSprinting");

        addMethod(id, baseClass, beforeJumpHookTypes, "beforeJump");
        addMethod(id, baseClass, overrideJumpHookTypes, "jump");
        addMethod(id, baseClass, afterJumpHookTypes, "afterJump");

        addMethod(id, baseClass, beforeKnockBackHookTypes, "beforeKnockBack", Entity.class, float.class, double.class, double.class);
        addMethod(id, baseClass, overrideKnockBackHookTypes, "knockBack", Entity.class, float.class, double.class, double.class);
        addMethod(id, baseClass, afterKnockBackHookTypes, "afterKnockBack", Entity.class, float.class, double.class, double.class);

        addMethod(id, baseClass, beforeLivingTickHookTypes, "beforeLivingTick");
        addMethod(id, baseClass, overrideLivingTickHookTypes, "livingTick");
        addMethod(id, baseClass, afterLivingTickHookTypes, "afterLivingTick");

        addMethod(id, baseClass, beforeMoveHookTypes, "beforeMove", MoverType.class, Vec3d.class);
        addMethod(id, baseClass, overrideMoveHookTypes, "move", MoverType.class, Vec3d.class);
        addMethod(id, baseClass, afterMoveHookTypes, "afterMove", MoverType.class, Vec3d.class);

        addMethod(id, baseClass, beforeMoveRelativeHookTypes, "beforeMoveRelative", float.class, Vec3d.class);
        addMethod(id, baseClass, overrideMoveRelativeHookTypes, "moveRelative", float.class, Vec3d.class);
        addMethod(id, baseClass, afterMoveRelativeHookTypes, "afterMoveRelative", float.class, Vec3d.class);

        addMethod(id, baseClass, beforeOnDeathHookTypes, "beforeOnDeath", DamageSource.class);
        addMethod(id, baseClass, overrideOnDeathHookTypes, "onDeath", DamageSource.class);
        addMethod(id, baseClass, afterOnDeathHookTypes, "afterOnDeath", DamageSource.class);

        addMethod(id, baseClass, beforeOnKillEntityHookTypes, "beforeOnKillEntity", LivingEntity.class);
        addMethod(id, baseClass, overrideOnKillEntityHookTypes, "onKillEntity", LivingEntity.class);
        addMethod(id, baseClass, afterOnKillEntityHookTypes, "afterOnKillEntity", LivingEntity.class);

        addMethod(id, baseClass, beforeOnLivingFallHookTypes, "beforeOnLivingFall", float.class, float.class);
        addMethod(id, baseClass, overrideOnLivingFallHookTypes, "onLivingFall", float.class, float.class);
        addMethod(id, baseClass, afterOnLivingFallHookTypes, "afterOnLivingFall", float.class, float.class);

        addMethod(id, baseClass, beforeOnStruckByLightningHookTypes, "beforeOnStruckByLightning", LightningBoltEntity.class);
        addMethod(id, baseClass, overrideOnStruckByLightningHookTypes, "onStruckByLightning", LightningBoltEntity.class);
        addMethod(id, baseClass, afterOnStruckByLightningHookTypes, "afterOnStruckByLightning", LightningBoltEntity.class);

        addMethod(id, baseClass, beforePickHookTypes, "beforePick", double.class, float.class, boolean.class);
        addMethod(id, baseClass, overridePickHookTypes, "pick", double.class, float.class, boolean.class);
        addMethod(id, baseClass, afterPickHookTypes, "afterPick", double.class, float.class, boolean.class);

        addMethod(id, baseClass, beforePlayerTickHookTypes, "beforePlayerTick");
        addMethod(id, baseClass, overridePlayerTickHookTypes, "playerTick");
        addMethod(id, baseClass, afterPlayerTickHookTypes, "afterPlayerTick");

        addMethod(id, baseClass, beforePlayStepSoundHookTypes, "beforePlayStepSound", BlockPos.class, BlockState.class);
        addMethod(id, baseClass, overridePlayStepSoundHookTypes, "playStepSound", BlockPos.class, BlockState.class);
        addMethod(id, baseClass, afterPlayStepSoundHookTypes, "afterPlayStepSound", BlockPos.class, BlockState.class);

        addMethod(id, baseClass, beforePushOutOfBlocksHookTypes, "beforePushOutOfBlocks", double.class, double.class, double.class);
        addMethod(id, baseClass, overridePushOutOfBlocksHookTypes, "pushOutOfBlocks", double.class, double.class, double.class);
        addMethod(id, baseClass, afterPushOutOfBlocksHookTypes, "afterPushOutOfBlocks", double.class, double.class, double.class);

        addMethod(id, baseClass, beforeReadHookTypes, "beforeRead", CompoundNBT.class);
        addMethod(id, baseClass, overrideReadHookTypes, "read", CompoundNBT.class);
        addMethod(id, baseClass, afterReadHookTypes, "afterRead", CompoundNBT.class);

        addMethod(id, baseClass, beforeRemoveHookTypes, "beforeRemove");
        addMethod(id, baseClass, overrideRemoveHookTypes, "remove");
        addMethod(id, baseClass, afterRemoveHookTypes, "afterRemove");

        addMethod(id, baseClass, beforeSetEntityActionStateHookTypes, "beforeSetEntityActionState", float.class, float.class, boolean.class, boolean.class);
        addMethod(id, baseClass, overrideSetEntityActionStateHookTypes, "setEntityActionState", float.class, float.class, boolean.class, boolean.class);
        addMethod(id, baseClass, afterSetEntityActionStateHookTypes, "afterSetEntityActionState", float.class, float.class, boolean.class, boolean.class);

        addMethod(id, baseClass, beforeSetPositionHookTypes, "beforeSetPosition", double.class, double.class, double.class);
        addMethod(id, baseClass, overrideSetPositionHookTypes, "setPosition", double.class, double.class, double.class);
        addMethod(id, baseClass, afterSetPositionHookTypes, "afterSetPosition", double.class, double.class, double.class);

        addMethod(id, baseClass, beforeSetPositionAndRotationHookTypes, "beforeSetPositionAndRotation", double.class, double.class, double.class, float.class, float.class);
        addMethod(id, baseClass, overrideSetPositionAndRotationHookTypes, "setPositionAndRotation", double.class, double.class, double.class, float.class, float.class);
        addMethod(id, baseClass, afterSetPositionAndRotationHookTypes, "afterSetPositionAndRotation", double.class, double.class, double.class, float.class, float.class);

        addMethod(id, baseClass, beforeSetSneakingHookTypes, "beforeSetSneaking", boolean.class);
        addMethod(id, baseClass, overrideSetSneakingHookTypes, "setSneaking", boolean.class);
        addMethod(id, baseClass, afterSetSneakingHookTypes, "afterSetSneaking", boolean.class);

        addMethod(id, baseClass, beforeSetSprintingHookTypes, "beforeSetSprinting", boolean.class);
        addMethod(id, baseClass, overrideSetSprintingHookTypes, "setSprinting", boolean.class);
        addMethod(id, baseClass, afterSetSprintingHookTypes, "afterSetSprinting", boolean.class);

        addMethod(id, baseClass, beforeStartRidingHookTypes, "beforeStartRiding", Entity.class, boolean.class);
        addMethod(id, baseClass, overrideStartRidingHookTypes, "startRiding", Entity.class, boolean.class);
        addMethod(id, baseClass, afterStartRidingHookTypes, "afterStartRiding", Entity.class, boolean.class);

        addMethod(id, baseClass, beforeSwingArmHookTypes, "beforeSwingArm", Hand.class);
        addMethod(id, baseClass, overrideSwingArmHookTypes, "swingArm", Hand.class);
        addMethod(id, baseClass, afterSwingArmHookTypes, "afterSwingArm", Hand.class);

        addMethod(id, baseClass, beforeTickHookTypes, "beforeTick");
        addMethod(id, baseClass, overrideTickHookTypes, "tick");
        addMethod(id, baseClass, afterTickHookTypes, "afterTick");

        addMethod(id, baseClass, beforeTravelHookTypes, "beforeTravel", Vec3d.class);
        addMethod(id, baseClass, overrideTravelHookTypes, "travel", Vec3d.class);
        addMethod(id, baseClass, afterTravelHookTypes, "afterTravel", Vec3d.class);

        addMethod(id, baseClass, beforeTrySleepHookTypes, "beforeTrySleep", BlockPos.class);
        addMethod(id, baseClass, overrideTrySleepHookTypes, "trySleep", BlockPos.class);
        addMethod(id, baseClass, afterTrySleepHookTypes, "afterTrySleep", BlockPos.class);

        addMethod(id, baseClass, beforeUpdateEntityActionStateHookTypes, "beforeUpdateEntityActionState");
        addMethod(id, baseClass, overrideUpdateEntityActionStateHookTypes, "updateEntityActionState");
        addMethod(id, baseClass, afterUpdateEntityActionStateHookTypes, "afterUpdateEntityActionState");

        addMethod(id, baseClass, beforeUpdatePotionEffectsHookTypes, "beforeUpdatePotionEffects");
        addMethod(id, baseClass, overrideUpdatePotionEffectsHookTypes, "updatePotionEffects");
        addMethod(id, baseClass, afterUpdatePotionEffectsHookTypes, "afterUpdatePotionEffects");

        addMethod(id, baseClass, beforeUpdateRiddenHookTypes, "beforeUpdateRidden");
        addMethod(id, baseClass, overrideUpdateRiddenHookTypes, "updateRidden");
        addMethod(id, baseClass, afterUpdateRiddenHookTypes, "afterUpdateRidden");

        addMethod(id, baseClass, beforeWakeUpPlayerHookTypes, "beforeWakeUpPlayer", boolean.class, boolean.class);
        addMethod(id, baseClass, overrideWakeUpPlayerHookTypes, "wakeUpPlayer", boolean.class, boolean.class);
        addMethod(id, baseClass, afterWakeUpPlayerHookTypes, "afterWakeUpPlayer", boolean.class, boolean.class);

        addMethod(id, baseClass, beforeWriteWithoutTypeIdHookTypes, "beforeWriteWithoutTypeId", CompoundNBT.class);
        addMethod(id, baseClass, overrideWriteWithoutTypeIdHookTypes, "writeWithoutTypeId", CompoundNBT.class);
        addMethod(id, baseClass, afterWriteWithoutTypeIdHookTypes, "afterWriteWithoutTypeId", CompoundNBT.class);

        addDynamicMethods(id, baseClass);

        addDynamicKeys(id, baseClass, beforeDynamicHookMethods, beforeDynamicHookTypes);
        addDynamicKeys(id, baseClass, overrideDynamicHookMethods, overrideDynamicHookTypes);
        addDynamicKeys(id, baseClass, afterDynamicHookMethods, afterDynamicHookTypes);

        initialize();

        for (IServerPlayerEntity instance : getAllInstancesList()) {
            instance.getServerPlayerAPI().attachServerPlayerBase(id);
        }

        System.out.println("Server Player: registered " + id);
        logger.fine("Server Player: registered class '" + baseClass.getName() + "' with id '" + id + "'");

        initialized = false;
    }

    public static boolean unregister(String id)
    {
        if (id == null) {
            return false;
        }

        Constructor<?> constructor = allBaseConstructors.remove(id);
        if (constructor == null) {
            return false;
        }

        for (IServerPlayerEntity instance : getAllInstancesList()) {
            instance.getServerPlayerAPI().detachServerPlayerBase(id);
        }

        beforeLocalConstructingHookTypes.remove(id);
        afterLocalConstructingHookTypes.remove(id);

        allBaseBeforeAddExhaustionSuperiors.remove(id);
        allBaseBeforeAddExhaustionInferiors.remove(id);
        allBaseOverrideAddExhaustionSuperiors.remove(id);
        allBaseOverrideAddExhaustionInferiors.remove(id);
        allBaseAfterAddExhaustionSuperiors.remove(id);
        allBaseAfterAddExhaustionInferiors.remove(id);

        beforeAddExhaustionHookTypes.remove(id);
        overrideAddExhaustionHookTypes.remove(id);
        afterAddExhaustionHookTypes.remove(id);

        allBaseBeforeAddExperienceLevelSuperiors.remove(id);
        allBaseBeforeAddExperienceLevelInferiors.remove(id);
        allBaseOverrideAddExperienceLevelSuperiors.remove(id);
        allBaseOverrideAddExperienceLevelInferiors.remove(id);
        allBaseAfterAddExperienceLevelSuperiors.remove(id);
        allBaseAfterAddExperienceLevelInferiors.remove(id);

        beforeAddExperienceLevelHookTypes.remove(id);
        overrideAddExperienceLevelHookTypes.remove(id);
        afterAddExperienceLevelHookTypes.remove(id);

        allBaseBeforeAddMovementStatSuperiors.remove(id);
        allBaseBeforeAddMovementStatInferiors.remove(id);
        allBaseOverrideAddMovementStatSuperiors.remove(id);
        allBaseOverrideAddMovementStatInferiors.remove(id);
        allBaseAfterAddMovementStatSuperiors.remove(id);
        allBaseAfterAddMovementStatInferiors.remove(id);

        beforeAddMovementStatHookTypes.remove(id);
        overrideAddMovementStatHookTypes.remove(id);
        afterAddMovementStatHookTypes.remove(id);

        allBaseBeforeAddStatSuperiors.remove(id);
        allBaseBeforeAddStatInferiors.remove(id);
        allBaseOverrideAddStatSuperiors.remove(id);
        allBaseOverrideAddStatInferiors.remove(id);
        allBaseAfterAddStatSuperiors.remove(id);
        allBaseAfterAddStatInferiors.remove(id);

        beforeAddStatHookTypes.remove(id);
        overrideAddStatHookTypes.remove(id);
        afterAddStatHookTypes.remove(id);

        allBaseBeforeAreEyesInFluidSuperiors.remove(id);
        allBaseBeforeAreEyesInFluidInferiors.remove(id);
        allBaseOverrideAreEyesInFluidSuperiors.remove(id);
        allBaseOverrideAreEyesInFluidInferiors.remove(id);
        allBaseAfterAreEyesInFluidSuperiors.remove(id);
        allBaseAfterAreEyesInFluidInferiors.remove(id);

        beforeAreEyesInFluidHookTypes.remove(id);
        overrideAreEyesInFluidHookTypes.remove(id);
        afterAreEyesInFluidHookTypes.remove(id);

        allBaseBeforeAttackEntityFromSuperiors.remove(id);
        allBaseBeforeAttackEntityFromInferiors.remove(id);
        allBaseOverrideAttackEntityFromSuperiors.remove(id);
        allBaseOverrideAttackEntityFromInferiors.remove(id);
        allBaseAfterAttackEntityFromSuperiors.remove(id);
        allBaseAfterAttackEntityFromInferiors.remove(id);

        beforeAttackEntityFromHookTypes.remove(id);
        overrideAttackEntityFromHookTypes.remove(id);
        afterAttackEntityFromHookTypes.remove(id);

        allBaseBeforeAttackTargetEntityWithCurrentItemSuperiors.remove(id);
        allBaseBeforeAttackTargetEntityWithCurrentItemInferiors.remove(id);
        allBaseOverrideAttackTargetEntityWithCurrentItemSuperiors.remove(id);
        allBaseOverrideAttackTargetEntityWithCurrentItemInferiors.remove(id);
        allBaseAfterAttackTargetEntityWithCurrentItemSuperiors.remove(id);
        allBaseAfterAttackTargetEntityWithCurrentItemInferiors.remove(id);

        beforeAttackTargetEntityWithCurrentItemHookTypes.remove(id);
        overrideAttackTargetEntityWithCurrentItemHookTypes.remove(id);
        afterAttackTargetEntityWithCurrentItemHookTypes.remove(id);

        allBaseBeforeCanBreatheUnderwaterSuperiors.remove(id);
        allBaseBeforeCanBreatheUnderwaterInferiors.remove(id);
        allBaseOverrideCanBreatheUnderwaterSuperiors.remove(id);
        allBaseOverrideCanBreatheUnderwaterInferiors.remove(id);
        allBaseAfterCanBreatheUnderwaterSuperiors.remove(id);
        allBaseAfterCanBreatheUnderwaterInferiors.remove(id);

        beforeCanBreatheUnderwaterHookTypes.remove(id);
        overrideCanBreatheUnderwaterHookTypes.remove(id);
        afterCanBreatheUnderwaterHookTypes.remove(id);

        allBaseBeforeCanHarvestBlockSuperiors.remove(id);
        allBaseBeforeCanHarvestBlockInferiors.remove(id);
        allBaseOverrideCanHarvestBlockSuperiors.remove(id);
        allBaseOverrideCanHarvestBlockInferiors.remove(id);
        allBaseAfterCanHarvestBlockSuperiors.remove(id);
        allBaseAfterCanHarvestBlockInferiors.remove(id);

        beforeCanHarvestBlockHookTypes.remove(id);
        overrideCanHarvestBlockHookTypes.remove(id);
        afterCanHarvestBlockHookTypes.remove(id);

        allBaseBeforeCanPlayerEditSuperiors.remove(id);
        allBaseBeforeCanPlayerEditInferiors.remove(id);
        allBaseOverrideCanPlayerEditSuperiors.remove(id);
        allBaseOverrideCanPlayerEditInferiors.remove(id);
        allBaseAfterCanPlayerEditSuperiors.remove(id);
        allBaseAfterCanPlayerEditInferiors.remove(id);

        beforeCanPlayerEditHookTypes.remove(id);
        overrideCanPlayerEditHookTypes.remove(id);
        afterCanPlayerEditHookTypes.remove(id);

        allBaseBeforeCanTriggerWalkingSuperiors.remove(id);
        allBaseBeforeCanTriggerWalkingInferiors.remove(id);
        allBaseOverrideCanTriggerWalkingSuperiors.remove(id);
        allBaseOverrideCanTriggerWalkingInferiors.remove(id);
        allBaseAfterCanTriggerWalkingSuperiors.remove(id);
        allBaseAfterCanTriggerWalkingInferiors.remove(id);

        beforeCanTriggerWalkingHookTypes.remove(id);
        overrideCanTriggerWalkingHookTypes.remove(id);
        afterCanTriggerWalkingHookTypes.remove(id);

        allBaseBeforeCopyFromSuperiors.remove(id);
        allBaseBeforeCopyFromInferiors.remove(id);
        allBaseOverrideCopyFromSuperiors.remove(id);
        allBaseOverrideCopyFromInferiors.remove(id);
        allBaseAfterCopyFromSuperiors.remove(id);
        allBaseAfterCopyFromInferiors.remove(id);

        beforeCopyFromHookTypes.remove(id);
        overrideCopyFromHookTypes.remove(id);
        afterCopyFromHookTypes.remove(id);

        allBaseBeforeDamageEntitySuperiors.remove(id);
        allBaseBeforeDamageEntityInferiors.remove(id);
        allBaseOverrideDamageEntitySuperiors.remove(id);
        allBaseOverrideDamageEntityInferiors.remove(id);
        allBaseAfterDamageEntitySuperiors.remove(id);
        allBaseAfterDamageEntityInferiors.remove(id);

        beforeDamageEntityHookTypes.remove(id);
        overrideDamageEntityHookTypes.remove(id);
        afterDamageEntityHookTypes.remove(id);

        allBaseBeforeDropItemSuperiors.remove(id);
        allBaseBeforeDropItemInferiors.remove(id);
        allBaseOverrideDropItemSuperiors.remove(id);
        allBaseOverrideDropItemInferiors.remove(id);
        allBaseAfterDropItemSuperiors.remove(id);
        allBaseAfterDropItemInferiors.remove(id);

        beforeDropItemHookTypes.remove(id);
        overrideDropItemHookTypes.remove(id);
        afterDropItemHookTypes.remove(id);

        allBaseBeforeGetAIMoveSpeedSuperiors.remove(id);
        allBaseBeforeGetAIMoveSpeedInferiors.remove(id);
        allBaseOverrideGetAIMoveSpeedSuperiors.remove(id);
        allBaseOverrideGetAIMoveSpeedInferiors.remove(id);
        allBaseAfterGetAIMoveSpeedSuperiors.remove(id);
        allBaseAfterGetAIMoveSpeedInferiors.remove(id);

        beforeGetAIMoveSpeedHookTypes.remove(id);
        overrideGetAIMoveSpeedHookTypes.remove(id);
        afterGetAIMoveSpeedHookTypes.remove(id);

        allBaseBeforeGetBrightnessSuperiors.remove(id);
        allBaseBeforeGetBrightnessInferiors.remove(id);
        allBaseOverrideGetBrightnessSuperiors.remove(id);
        allBaseOverrideGetBrightnessInferiors.remove(id);
        allBaseAfterGetBrightnessSuperiors.remove(id);
        allBaseAfterGetBrightnessInferiors.remove(id);

        beforeGetBrightnessHookTypes.remove(id);
        overrideGetBrightnessHookTypes.remove(id);
        afterGetBrightnessHookTypes.remove(id);

        allBaseBeforeGetDigSpeedSuperiors.remove(id);
        allBaseBeforeGetDigSpeedInferiors.remove(id);
        allBaseOverrideGetDigSpeedSuperiors.remove(id);
        allBaseOverrideGetDigSpeedInferiors.remove(id);
        allBaseAfterGetDigSpeedSuperiors.remove(id);
        allBaseAfterGetDigSpeedInferiors.remove(id);

        beforeGetDigSpeedHookTypes.remove(id);
        overrideGetDigSpeedHookTypes.remove(id);
        afterGetDigSpeedHookTypes.remove(id);

        allBaseBeforeGetDistanceSqSuperiors.remove(id);
        allBaseBeforeGetDistanceSqInferiors.remove(id);
        allBaseOverrideGetDistanceSqSuperiors.remove(id);
        allBaseOverrideGetDistanceSqInferiors.remove(id);
        allBaseAfterGetDistanceSqSuperiors.remove(id);
        allBaseAfterGetDistanceSqInferiors.remove(id);

        beforeGetDistanceSqHookTypes.remove(id);
        overrideGetDistanceSqHookTypes.remove(id);
        afterGetDistanceSqHookTypes.remove(id);

        allBaseBeforeGetDistanceSqToEntitySuperiors.remove(id);
        allBaseBeforeGetDistanceSqToEntityInferiors.remove(id);
        allBaseOverrideGetDistanceSqToEntitySuperiors.remove(id);
        allBaseOverrideGetDistanceSqToEntityInferiors.remove(id);
        allBaseAfterGetDistanceSqToEntitySuperiors.remove(id);
        allBaseAfterGetDistanceSqToEntityInferiors.remove(id);

        beforeGetDistanceSqToEntityHookTypes.remove(id);
        overrideGetDistanceSqToEntityHookTypes.remove(id);
        afterGetDistanceSqToEntityHookTypes.remove(id);

        allBaseBeforeGetDistanceSqVecSuperiors.remove(id);
        allBaseBeforeGetDistanceSqVecInferiors.remove(id);
        allBaseOverrideGetDistanceSqVecSuperiors.remove(id);
        allBaseOverrideGetDistanceSqVecInferiors.remove(id);
        allBaseAfterGetDistanceSqVecSuperiors.remove(id);
        allBaseAfterGetDistanceSqVecInferiors.remove(id);

        beforeGetDistanceSqVecHookTypes.remove(id);
        overrideGetDistanceSqVecHookTypes.remove(id);
        afterGetDistanceSqVecHookTypes.remove(id);

        allBaseBeforeGetHurtSoundSuperiors.remove(id);
        allBaseBeforeGetHurtSoundInferiors.remove(id);
        allBaseOverrideGetHurtSoundSuperiors.remove(id);
        allBaseOverrideGetHurtSoundInferiors.remove(id);
        allBaseAfterGetHurtSoundSuperiors.remove(id);
        allBaseAfterGetHurtSoundInferiors.remove(id);

        beforeGetHurtSoundHookTypes.remove(id);
        overrideGetHurtSoundHookTypes.remove(id);
        afterGetHurtSoundHookTypes.remove(id);

        allBaseBeforeGetNameSuperiors.remove(id);
        allBaseBeforeGetNameInferiors.remove(id);
        allBaseOverrideGetNameSuperiors.remove(id);
        allBaseOverrideGetNameInferiors.remove(id);
        allBaseAfterGetNameSuperiors.remove(id);
        allBaseAfterGetNameInferiors.remove(id);

        beforeGetNameHookTypes.remove(id);
        overrideGetNameHookTypes.remove(id);
        afterGetNameHookTypes.remove(id);

        allBaseBeforeGetSizeSuperiors.remove(id);
        allBaseBeforeGetSizeInferiors.remove(id);
        allBaseOverrideGetSizeSuperiors.remove(id);
        allBaseOverrideGetSizeInferiors.remove(id);
        allBaseAfterGetSizeSuperiors.remove(id);
        allBaseAfterGetSizeInferiors.remove(id);

        beforeGetSizeHookTypes.remove(id);
        overrideGetSizeHookTypes.remove(id);
        afterGetSizeHookTypes.remove(id);

        allBaseBeforeGetSleepTimerSuperiors.remove(id);
        allBaseBeforeGetSleepTimerInferiors.remove(id);
        allBaseOverrideGetSleepTimerSuperiors.remove(id);
        allBaseOverrideGetSleepTimerInferiors.remove(id);
        allBaseAfterGetSleepTimerSuperiors.remove(id);
        allBaseAfterGetSleepTimerInferiors.remove(id);

        beforeGetSleepTimerHookTypes.remove(id);
        overrideGetSleepTimerHookTypes.remove(id);
        afterGetSleepTimerHookTypes.remove(id);

        allBaseBeforeGetStandingEyeHeightSuperiors.remove(id);
        allBaseBeforeGetStandingEyeHeightInferiors.remove(id);
        allBaseOverrideGetStandingEyeHeightSuperiors.remove(id);
        allBaseOverrideGetStandingEyeHeightInferiors.remove(id);
        allBaseAfterGetStandingEyeHeightSuperiors.remove(id);
        allBaseAfterGetStandingEyeHeightInferiors.remove(id);

        beforeGetStandingEyeHeightHookTypes.remove(id);
        overrideGetStandingEyeHeightHookTypes.remove(id);
        afterGetStandingEyeHeightHookTypes.remove(id);

        allBaseBeforeGiveExperiencePointsSuperiors.remove(id);
        allBaseBeforeGiveExperiencePointsInferiors.remove(id);
        allBaseOverrideGiveExperiencePointsSuperiors.remove(id);
        allBaseOverrideGiveExperiencePointsInferiors.remove(id);
        allBaseAfterGiveExperiencePointsSuperiors.remove(id);
        allBaseAfterGiveExperiencePointsInferiors.remove(id);

        beforeGiveExperiencePointsHookTypes.remove(id);
        overrideGiveExperiencePointsHookTypes.remove(id);
        afterGiveExperiencePointsHookTypes.remove(id);

        allBaseBeforeHandleWaterMovementSuperiors.remove(id);
        allBaseBeforeHandleWaterMovementInferiors.remove(id);
        allBaseOverrideHandleWaterMovementSuperiors.remove(id);
        allBaseOverrideHandleWaterMovementInferiors.remove(id);
        allBaseAfterHandleWaterMovementSuperiors.remove(id);
        allBaseAfterHandleWaterMovementInferiors.remove(id);

        beforeHandleWaterMovementHookTypes.remove(id);
        overrideHandleWaterMovementHookTypes.remove(id);
        afterHandleWaterMovementHookTypes.remove(id);

        allBaseBeforeHealSuperiors.remove(id);
        allBaseBeforeHealInferiors.remove(id);
        allBaseOverrideHealSuperiors.remove(id);
        allBaseOverrideHealInferiors.remove(id);
        allBaseAfterHealSuperiors.remove(id);
        allBaseAfterHealInferiors.remove(id);

        beforeHealHookTypes.remove(id);
        overrideHealHookTypes.remove(id);
        afterHealHookTypes.remove(id);

        allBaseBeforeIsEntityInsideOpaqueBlockSuperiors.remove(id);
        allBaseBeforeIsEntityInsideOpaqueBlockInferiors.remove(id);
        allBaseOverrideIsEntityInsideOpaqueBlockSuperiors.remove(id);
        allBaseOverrideIsEntityInsideOpaqueBlockInferiors.remove(id);
        allBaseAfterIsEntityInsideOpaqueBlockSuperiors.remove(id);
        allBaseAfterIsEntityInsideOpaqueBlockInferiors.remove(id);

        beforeIsEntityInsideOpaqueBlockHookTypes.remove(id);
        overrideIsEntityInsideOpaqueBlockHookTypes.remove(id);
        afterIsEntityInsideOpaqueBlockHookTypes.remove(id);

        allBaseBeforeIsInWaterSuperiors.remove(id);
        allBaseBeforeIsInWaterInferiors.remove(id);
        allBaseOverrideIsInWaterSuperiors.remove(id);
        allBaseOverrideIsInWaterInferiors.remove(id);
        allBaseAfterIsInWaterSuperiors.remove(id);
        allBaseAfterIsInWaterInferiors.remove(id);

        beforeIsInWaterHookTypes.remove(id);
        overrideIsInWaterHookTypes.remove(id);
        afterIsInWaterHookTypes.remove(id);

        allBaseBeforeIsOnLadderSuperiors.remove(id);
        allBaseBeforeIsOnLadderInferiors.remove(id);
        allBaseOverrideIsOnLadderSuperiors.remove(id);
        allBaseOverrideIsOnLadderInferiors.remove(id);
        allBaseAfterIsOnLadderSuperiors.remove(id);
        allBaseAfterIsOnLadderInferiors.remove(id);

        beforeIsOnLadderHookTypes.remove(id);
        overrideIsOnLadderHookTypes.remove(id);
        afterIsOnLadderHookTypes.remove(id);

        allBaseBeforeIsShiftKeyDownSuperiors.remove(id);
        allBaseBeforeIsShiftKeyDownInferiors.remove(id);
        allBaseOverrideIsShiftKeyDownSuperiors.remove(id);
        allBaseOverrideIsShiftKeyDownInferiors.remove(id);
        allBaseAfterIsShiftKeyDownSuperiors.remove(id);
        allBaseAfterIsShiftKeyDownInferiors.remove(id);

        beforeIsShiftKeyDownHookTypes.remove(id);
        overrideIsShiftKeyDownHookTypes.remove(id);
        afterIsShiftKeyDownHookTypes.remove(id);

        allBaseBeforeIsSleepingSuperiors.remove(id);
        allBaseBeforeIsSleepingInferiors.remove(id);
        allBaseOverrideIsSleepingSuperiors.remove(id);
        allBaseOverrideIsSleepingInferiors.remove(id);
        allBaseAfterIsSleepingSuperiors.remove(id);
        allBaseAfterIsSleepingInferiors.remove(id);

        beforeIsSleepingHookTypes.remove(id);
        overrideIsSleepingHookTypes.remove(id);
        afterIsSleepingHookTypes.remove(id);

        allBaseBeforeIsSprintingSuperiors.remove(id);
        allBaseBeforeIsSprintingInferiors.remove(id);
        allBaseOverrideIsSprintingSuperiors.remove(id);
        allBaseOverrideIsSprintingInferiors.remove(id);
        allBaseAfterIsSprintingSuperiors.remove(id);
        allBaseAfterIsSprintingInferiors.remove(id);

        beforeIsSprintingHookTypes.remove(id);
        overrideIsSprintingHookTypes.remove(id);
        afterIsSprintingHookTypes.remove(id);

        allBaseBeforeJumpSuperiors.remove(id);
        allBaseBeforeJumpInferiors.remove(id);
        allBaseOverrideJumpSuperiors.remove(id);
        allBaseOverrideJumpInferiors.remove(id);
        allBaseAfterJumpSuperiors.remove(id);
        allBaseAfterJumpInferiors.remove(id);

        beforeJumpHookTypes.remove(id);
        overrideJumpHookTypes.remove(id);
        afterJumpHookTypes.remove(id);

        allBaseBeforeKnockBackSuperiors.remove(id);
        allBaseBeforeKnockBackInferiors.remove(id);
        allBaseOverrideKnockBackSuperiors.remove(id);
        allBaseOverrideKnockBackInferiors.remove(id);
        allBaseAfterKnockBackSuperiors.remove(id);
        allBaseAfterKnockBackInferiors.remove(id);

        beforeKnockBackHookTypes.remove(id);
        overrideKnockBackHookTypes.remove(id);
        afterKnockBackHookTypes.remove(id);

        allBaseBeforeLivingTickSuperiors.remove(id);
        allBaseBeforeLivingTickInferiors.remove(id);
        allBaseOverrideLivingTickSuperiors.remove(id);
        allBaseOverrideLivingTickInferiors.remove(id);
        allBaseAfterLivingTickSuperiors.remove(id);
        allBaseAfterLivingTickInferiors.remove(id);

        beforeLivingTickHookTypes.remove(id);
        overrideLivingTickHookTypes.remove(id);
        afterLivingTickHookTypes.remove(id);

        allBaseBeforeMoveSuperiors.remove(id);
        allBaseBeforeMoveInferiors.remove(id);
        allBaseOverrideMoveSuperiors.remove(id);
        allBaseOverrideMoveInferiors.remove(id);
        allBaseAfterMoveSuperiors.remove(id);
        allBaseAfterMoveInferiors.remove(id);

        beforeMoveHookTypes.remove(id);
        overrideMoveHookTypes.remove(id);
        afterMoveHookTypes.remove(id);

        allBaseBeforeMoveRelativeSuperiors.remove(id);
        allBaseBeforeMoveRelativeInferiors.remove(id);
        allBaseOverrideMoveRelativeSuperiors.remove(id);
        allBaseOverrideMoveRelativeInferiors.remove(id);
        allBaseAfterMoveRelativeSuperiors.remove(id);
        allBaseAfterMoveRelativeInferiors.remove(id);

        beforeMoveRelativeHookTypes.remove(id);
        overrideMoveRelativeHookTypes.remove(id);
        afterMoveRelativeHookTypes.remove(id);

        allBaseBeforeOnDeathSuperiors.remove(id);
        allBaseBeforeOnDeathInferiors.remove(id);
        allBaseOverrideOnDeathSuperiors.remove(id);
        allBaseOverrideOnDeathInferiors.remove(id);
        allBaseAfterOnDeathSuperiors.remove(id);
        allBaseAfterOnDeathInferiors.remove(id);

        beforeOnDeathHookTypes.remove(id);
        overrideOnDeathHookTypes.remove(id);
        afterOnDeathHookTypes.remove(id);

        allBaseBeforeOnKillEntitySuperiors.remove(id);
        allBaseBeforeOnKillEntityInferiors.remove(id);
        allBaseOverrideOnKillEntitySuperiors.remove(id);
        allBaseOverrideOnKillEntityInferiors.remove(id);
        allBaseAfterOnKillEntitySuperiors.remove(id);
        allBaseAfterOnKillEntityInferiors.remove(id);

        beforeOnKillEntityHookTypes.remove(id);
        overrideOnKillEntityHookTypes.remove(id);
        afterOnKillEntityHookTypes.remove(id);

        allBaseBeforeOnLivingFallSuperiors.remove(id);
        allBaseBeforeOnLivingFallInferiors.remove(id);
        allBaseOverrideOnLivingFallSuperiors.remove(id);
        allBaseOverrideOnLivingFallInferiors.remove(id);
        allBaseAfterOnLivingFallSuperiors.remove(id);
        allBaseAfterOnLivingFallInferiors.remove(id);

        beforeOnLivingFallHookTypes.remove(id);
        overrideOnLivingFallHookTypes.remove(id);
        afterOnLivingFallHookTypes.remove(id);

        allBaseBeforeOnStruckByLightningSuperiors.remove(id);
        allBaseBeforeOnStruckByLightningInferiors.remove(id);
        allBaseOverrideOnStruckByLightningSuperiors.remove(id);
        allBaseOverrideOnStruckByLightningInferiors.remove(id);
        allBaseAfterOnStruckByLightningSuperiors.remove(id);
        allBaseAfterOnStruckByLightningInferiors.remove(id);

        beforeOnStruckByLightningHookTypes.remove(id);
        overrideOnStruckByLightningHookTypes.remove(id);
        afterOnStruckByLightningHookTypes.remove(id);

        allBaseBeforePickSuperiors.remove(id);
        allBaseBeforePickInferiors.remove(id);
        allBaseOverridePickSuperiors.remove(id);
        allBaseOverridePickInferiors.remove(id);
        allBaseAfterPickSuperiors.remove(id);
        allBaseAfterPickInferiors.remove(id);

        beforePickHookTypes.remove(id);
        overridePickHookTypes.remove(id);
        afterPickHookTypes.remove(id);

        allBaseBeforePlayerTickSuperiors.remove(id);
        allBaseBeforePlayerTickInferiors.remove(id);
        allBaseOverridePlayerTickSuperiors.remove(id);
        allBaseOverridePlayerTickInferiors.remove(id);
        allBaseAfterPlayerTickSuperiors.remove(id);
        allBaseAfterPlayerTickInferiors.remove(id);

        beforePlayerTickHookTypes.remove(id);
        overridePlayerTickHookTypes.remove(id);
        afterPlayerTickHookTypes.remove(id);

        allBaseBeforePlayStepSoundSuperiors.remove(id);
        allBaseBeforePlayStepSoundInferiors.remove(id);
        allBaseOverridePlayStepSoundSuperiors.remove(id);
        allBaseOverridePlayStepSoundInferiors.remove(id);
        allBaseAfterPlayStepSoundSuperiors.remove(id);
        allBaseAfterPlayStepSoundInferiors.remove(id);

        beforePlayStepSoundHookTypes.remove(id);
        overridePlayStepSoundHookTypes.remove(id);
        afterPlayStepSoundHookTypes.remove(id);

        allBaseBeforePushOutOfBlocksSuperiors.remove(id);
        allBaseBeforePushOutOfBlocksInferiors.remove(id);
        allBaseOverridePushOutOfBlocksSuperiors.remove(id);
        allBaseOverridePushOutOfBlocksInferiors.remove(id);
        allBaseAfterPushOutOfBlocksSuperiors.remove(id);
        allBaseAfterPushOutOfBlocksInferiors.remove(id);

        beforePushOutOfBlocksHookTypes.remove(id);
        overridePushOutOfBlocksHookTypes.remove(id);
        afterPushOutOfBlocksHookTypes.remove(id);

        allBaseBeforeReadSuperiors.remove(id);
        allBaseBeforeReadInferiors.remove(id);
        allBaseOverrideReadSuperiors.remove(id);
        allBaseOverrideReadInferiors.remove(id);
        allBaseAfterReadSuperiors.remove(id);
        allBaseAfterReadInferiors.remove(id);

        beforeReadHookTypes.remove(id);
        overrideReadHookTypes.remove(id);
        afterReadHookTypes.remove(id);

        allBaseBeforeRemoveSuperiors.remove(id);
        allBaseBeforeRemoveInferiors.remove(id);
        allBaseOverrideRemoveSuperiors.remove(id);
        allBaseOverrideRemoveInferiors.remove(id);
        allBaseAfterRemoveSuperiors.remove(id);
        allBaseAfterRemoveInferiors.remove(id);

        beforeRemoveHookTypes.remove(id);
        overrideRemoveHookTypes.remove(id);
        afterRemoveHookTypes.remove(id);

        allBaseBeforeSetEntityActionStateSuperiors.remove(id);
        allBaseBeforeSetEntityActionStateInferiors.remove(id);
        allBaseOverrideSetEntityActionStateSuperiors.remove(id);
        allBaseOverrideSetEntityActionStateInferiors.remove(id);
        allBaseAfterSetEntityActionStateSuperiors.remove(id);
        allBaseAfterSetEntityActionStateInferiors.remove(id);

        beforeSetEntityActionStateHookTypes.remove(id);
        overrideSetEntityActionStateHookTypes.remove(id);
        afterSetEntityActionStateHookTypes.remove(id);

        allBaseBeforeSetPositionSuperiors.remove(id);
        allBaseBeforeSetPositionInferiors.remove(id);
        allBaseOverrideSetPositionSuperiors.remove(id);
        allBaseOverrideSetPositionInferiors.remove(id);
        allBaseAfterSetPositionSuperiors.remove(id);
        allBaseAfterSetPositionInferiors.remove(id);

        beforeSetPositionHookTypes.remove(id);
        overrideSetPositionHookTypes.remove(id);
        afterSetPositionHookTypes.remove(id);

        allBaseBeforeSetPositionAndRotationSuperiors.remove(id);
        allBaseBeforeSetPositionAndRotationInferiors.remove(id);
        allBaseOverrideSetPositionAndRotationSuperiors.remove(id);
        allBaseOverrideSetPositionAndRotationInferiors.remove(id);
        allBaseAfterSetPositionAndRotationSuperiors.remove(id);
        allBaseAfterSetPositionAndRotationInferiors.remove(id);

        beforeSetPositionAndRotationHookTypes.remove(id);
        overrideSetPositionAndRotationHookTypes.remove(id);
        afterSetPositionAndRotationHookTypes.remove(id);

        allBaseBeforeSetSneakingSuperiors.remove(id);
        allBaseBeforeSetSneakingInferiors.remove(id);
        allBaseOverrideSetSneakingSuperiors.remove(id);
        allBaseOverrideSetSneakingInferiors.remove(id);
        allBaseAfterSetSneakingSuperiors.remove(id);
        allBaseAfterSetSneakingInferiors.remove(id);

        beforeSetSneakingHookTypes.remove(id);
        overrideSetSneakingHookTypes.remove(id);
        afterSetSneakingHookTypes.remove(id);

        allBaseBeforeSetSprintingSuperiors.remove(id);
        allBaseBeforeSetSprintingInferiors.remove(id);
        allBaseOverrideSetSprintingSuperiors.remove(id);
        allBaseOverrideSetSprintingInferiors.remove(id);
        allBaseAfterSetSprintingSuperiors.remove(id);
        allBaseAfterSetSprintingInferiors.remove(id);

        beforeSetSprintingHookTypes.remove(id);
        overrideSetSprintingHookTypes.remove(id);
        afterSetSprintingHookTypes.remove(id);

        allBaseBeforeStartRidingSuperiors.remove(id);
        allBaseBeforeStartRidingInferiors.remove(id);
        allBaseOverrideStartRidingSuperiors.remove(id);
        allBaseOverrideStartRidingInferiors.remove(id);
        allBaseAfterStartRidingSuperiors.remove(id);
        allBaseAfterStartRidingInferiors.remove(id);

        beforeStartRidingHookTypes.remove(id);
        overrideStartRidingHookTypes.remove(id);
        afterStartRidingHookTypes.remove(id);

        allBaseBeforeSwingArmSuperiors.remove(id);
        allBaseBeforeSwingArmInferiors.remove(id);
        allBaseOverrideSwingArmSuperiors.remove(id);
        allBaseOverrideSwingArmInferiors.remove(id);
        allBaseAfterSwingArmSuperiors.remove(id);
        allBaseAfterSwingArmInferiors.remove(id);

        beforeSwingArmHookTypes.remove(id);
        overrideSwingArmHookTypes.remove(id);
        afterSwingArmHookTypes.remove(id);

        allBaseBeforeTickSuperiors.remove(id);
        allBaseBeforeTickInferiors.remove(id);
        allBaseOverrideTickSuperiors.remove(id);
        allBaseOverrideTickInferiors.remove(id);
        allBaseAfterTickSuperiors.remove(id);
        allBaseAfterTickInferiors.remove(id);

        beforeTickHookTypes.remove(id);
        overrideTickHookTypes.remove(id);
        afterTickHookTypes.remove(id);

        allBaseBeforeTravelSuperiors.remove(id);
        allBaseBeforeTravelInferiors.remove(id);
        allBaseOverrideTravelSuperiors.remove(id);
        allBaseOverrideTravelInferiors.remove(id);
        allBaseAfterTravelSuperiors.remove(id);
        allBaseAfterTravelInferiors.remove(id);

        beforeTravelHookTypes.remove(id);
        overrideTravelHookTypes.remove(id);
        afterTravelHookTypes.remove(id);

        allBaseBeforeTrySleepSuperiors.remove(id);
        allBaseBeforeTrySleepInferiors.remove(id);
        allBaseOverrideTrySleepSuperiors.remove(id);
        allBaseOverrideTrySleepInferiors.remove(id);
        allBaseAfterTrySleepSuperiors.remove(id);
        allBaseAfterTrySleepInferiors.remove(id);

        beforeTrySleepHookTypes.remove(id);
        overrideTrySleepHookTypes.remove(id);
        afterTrySleepHookTypes.remove(id);

        allBaseBeforeUpdateEntityActionStateSuperiors.remove(id);
        allBaseBeforeUpdateEntityActionStateInferiors.remove(id);
        allBaseOverrideUpdateEntityActionStateSuperiors.remove(id);
        allBaseOverrideUpdateEntityActionStateInferiors.remove(id);
        allBaseAfterUpdateEntityActionStateSuperiors.remove(id);
        allBaseAfterUpdateEntityActionStateInferiors.remove(id);

        beforeUpdateEntityActionStateHookTypes.remove(id);
        overrideUpdateEntityActionStateHookTypes.remove(id);
        afterUpdateEntityActionStateHookTypes.remove(id);

        allBaseBeforeUpdatePotionEffectsSuperiors.remove(id);
        allBaseBeforeUpdatePotionEffectsInferiors.remove(id);
        allBaseOverrideUpdatePotionEffectsSuperiors.remove(id);
        allBaseOverrideUpdatePotionEffectsInferiors.remove(id);
        allBaseAfterUpdatePotionEffectsSuperiors.remove(id);
        allBaseAfterUpdatePotionEffectsInferiors.remove(id);

        beforeUpdatePotionEffectsHookTypes.remove(id);
        overrideUpdatePotionEffectsHookTypes.remove(id);
        afterUpdatePotionEffectsHookTypes.remove(id);

        allBaseBeforeUpdateRiddenSuperiors.remove(id);
        allBaseBeforeUpdateRiddenInferiors.remove(id);
        allBaseOverrideUpdateRiddenSuperiors.remove(id);
        allBaseOverrideUpdateRiddenInferiors.remove(id);
        allBaseAfterUpdateRiddenSuperiors.remove(id);
        allBaseAfterUpdateRiddenInferiors.remove(id);

        beforeUpdateRiddenHookTypes.remove(id);
        overrideUpdateRiddenHookTypes.remove(id);
        afterUpdateRiddenHookTypes.remove(id);

        allBaseBeforeWakeUpPlayerSuperiors.remove(id);
        allBaseBeforeWakeUpPlayerInferiors.remove(id);
        allBaseOverrideWakeUpPlayerSuperiors.remove(id);
        allBaseOverrideWakeUpPlayerInferiors.remove(id);
        allBaseAfterWakeUpPlayerSuperiors.remove(id);
        allBaseAfterWakeUpPlayerInferiors.remove(id);

        beforeWakeUpPlayerHookTypes.remove(id);
        overrideWakeUpPlayerHookTypes.remove(id);
        afterWakeUpPlayerHookTypes.remove(id);

        allBaseBeforeWriteWithoutTypeIdSuperiors.remove(id);
        allBaseBeforeWriteWithoutTypeIdInferiors.remove(id);
        allBaseOverrideWriteWithoutTypeIdSuperiors.remove(id);
        allBaseOverrideWriteWithoutTypeIdInferiors.remove(id);
        allBaseAfterWriteWithoutTypeIdSuperiors.remove(id);
        allBaseAfterWriteWithoutTypeIdInferiors.remove(id);

        beforeWriteWithoutTypeIdHookTypes.remove(id);
        overrideWriteWithoutTypeIdHookTypes.remove(id);
        afterWriteWithoutTypeIdHookTypes.remove(id);

        for (IServerPlayerEntity instance : getAllInstancesList()) {
            instance.getServerPlayerAPI().updateServerPlayerBases();
        }

        Iterator<String> iterator = keysToVirtualIds.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (keysToVirtualIds.get(key).equals(id)) {
                keysToVirtualIds.remove(key);
            }
        }

        boolean otherFound = false;
        Class<?> type = constructor.getDeclaringClass();

        iterator = allBaseConstructors.keySet().iterator();
        while (iterator.hasNext()) {
            String otherId = iterator.next();
            Class<?> otherType = allBaseConstructors.get(otherId).getDeclaringClass();
            if (!otherId.equals(id) && otherType.equals(type)) {
                otherFound = true;
                break;
            }
        }

        if (!otherFound) {
            dynamicTypes.remove(type);

            virtualDynamicHookMethods.remove(type);

            beforeDynamicHookMethods.remove(type);
            overrideDynamicHookMethods.remove(type);
            afterDynamicHookMethods.remove(type);
        }

        removeDynamicHookTypes(id, beforeDynamicHookTypes);
        removeDynamicHookTypes(id, overrideDynamicHookTypes);
        removeDynamicHookTypes(id, afterDynamicHookTypes);

        allBaseBeforeDynamicSuperiors.remove(id);
        allBaseBeforeDynamicInferiors.remove(id);
        allBaseOverrideDynamicSuperiors.remove(id);
        allBaseOverrideDynamicInferiors.remove(id);
        allBaseAfterDynamicSuperiors.remove(id);
        allBaseAfterDynamicInferiors.remove(id);

        log("ServerPlayerAPI: unregistered id '" + id + "'");

        return true;
    }

    public static void removeDynamicHookTypes(String id, Map<String, List<String>> map)
    {
        for (String s : map.keySet()) {
            map.get(s).remove(id);
        }
    }

    public static Set<String> getRegisteredIds()
    {
        return unmodifiableAllIds;
    }

    private static void addSorting(String id, Map<String, String[]> map, String[] values)
    {
        if (values != null && values.length > 0) {
            map.put(id, values);
        }
    }

    private static void addDynamicSorting(String id, Map<String, Map<String, String[]>> map, Map<String, String[]> values)
    {
        if (values != null && values.size() > 0) {
            map.put(id, values);
        }
    }

    private static void addMethod(String id, Class<?> baseClass, List<String> list, String methodName, Class<?>... _parameterTypes)
    {
        try {
            Method method = baseClass.getMethod(methodName, _parameterTypes);
            boolean isOverridden = method.getDeclaringClass() != ServerPlayerEntityBase.class;
            if (isOverridden) {
                list.add(id);
            }
        } catch (Exception e) {
            throw new RuntimeException("Can not reflect method '" + methodName + "' of class '" + baseClass.getName() + "'", e);
        }
    }

    private static void addDynamicMethods(String id, Class<?> baseClass)
    {
        if (!dynamicTypes.add(baseClass)) {
            return;
        }

        Map<String, Method> virtuals = null;
        Map<String, Method> befores = null;
        Map<String, Method> overrides = null;
        Map<String, Method> afters = null;

        Method[] methods = baseClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getDeclaringClass() != baseClass) {
                continue;
            }

            int modifiers = method.getModifiers();
            if (Modifier.isAbstract(modifiers)) {
                continue;
            }

            if (Modifier.isStatic(modifiers)) {
                continue;
            }

            String name = method.getName();
            if (name.length() < 7 || !name.substring(0, 7).equalsIgnoreCase("dynamic")) {
                continue;
            } else {
                name = name.substring(7);
            }

            while (name.charAt(0) == '_') {
                name = name.substring(1);
            }

            boolean before = false;
            boolean virtual = false;
            boolean override = false;
            boolean after = false;

            if (name.substring(0, 7).equalsIgnoreCase("virtual")) {
                virtual = true;
                name = name.substring(7);
            } else {
                if (name.length() >= 8 && name.substring(0, 8).equalsIgnoreCase("override")) {
                    name = name.substring(8);
                    override = true;
                } else if (name.substring(0, 6).equalsIgnoreCase("before")) {
                    before = true;
                    name = name.substring(6);
                } else if (name.substring(0, 5).equalsIgnoreCase("after")) {
                    after = true;
                    name = name.substring(5);
                }
            }

            if (name.length() >= 1 && (before || virtual || override || after)) {
                name = name.substring(0, 1).toLowerCase() + name.substring(1);
            }

            while (name.charAt(0) == '_') {
                name = name.substring(1);
            }

            keys.add(name);

            if (virtual) {
                if (keysToVirtualIds.containsKey(name)) {
                    throw new RuntimeException("Can not process more than one dynamic virtual method");
                }

                keysToVirtualIds.put(name, id);
                virtuals = addDynamicMethod(name, method, virtuals);
            } else if (before) {
                befores = addDynamicMethod(name, method, befores);
            } else if (after) {
                afters = addDynamicMethod(name, method, afters);
            } else {
                overrides = addDynamicMethod(name, method, overrides);
            }
        }

        if (virtuals != null) {
            virtualDynamicHookMethods.put(baseClass, virtuals);
        }
        if (befores != null) {
            beforeDynamicHookMethods.put(baseClass, befores);
        }
        if (overrides != null) {
            overrideDynamicHookMethods.put(baseClass, overrides);
        }
        if (afters != null) {
            afterDynamicHookMethods.put(baseClass, afters);
        }
    }

    private static void addDynamicKeys(String id, Class<?> baseClass, Map<Class<?>, Map<String, Method>> dynamicHookMethods, Map<String, List<String>> dynamicHookTypes)
    {
        Map<String, Method> methods = dynamicHookMethods.get(baseClass);
        if (methods == null || methods.size() == 0) {
            return;
        }

        for (String key : methods.keySet()) {
            if (!dynamicHookTypes.containsKey(key)) {
                dynamicHookTypes.put(key, new ArrayList<>(1));
            }
            dynamicHookTypes.get(key).add(id);
        }
    }

    private static Map<String, Method> addDynamicMethod(String key, Method method, Map<String, Method> methods)
    {
        if (methods == null) {
            methods = new HashMap<>();
        }
        if (methods.containsKey(key)) {
            throw new RuntimeException("method with key '" + key + "' already exists");
        }
        methods.put(key, method);
        return methods;
    }

    public static ServerPlayerAPI create(IServerPlayerEntity serverPlayer)
    {
        if (allBaseConstructors.size() > 0 && !initialized) {
            initialize();
        }
        return new ServerPlayerAPI(serverPlayer);
    }

    private static void initialize()
    {
        sortBases(beforeLocalConstructingHookTypes, allBaseBeforeLocalConstructingSuperiors, allBaseBeforeLocalConstructingInferiors, "beforeLocalConstructing");
        sortBases(afterLocalConstructingHookTypes, allBaseAfterLocalConstructingSuperiors, allBaseAfterLocalConstructingInferiors, "afterLocalConstructing");

        for (String key : keys) {
            sortDynamicBases(beforeDynamicHookTypes, allBaseBeforeDynamicSuperiors, allBaseBeforeDynamicInferiors, key);
            sortDynamicBases(overrideDynamicHookTypes, allBaseOverrideDynamicSuperiors, allBaseOverrideDynamicInferiors, key);
            sortDynamicBases(afterDynamicHookTypes, allBaseAfterDynamicSuperiors, allBaseAfterDynamicInferiors, key);
        }

        sortBases(beforeAddExhaustionHookTypes, allBaseBeforeAddExhaustionSuperiors, allBaseBeforeAddExhaustionInferiors, "beforeAddExhaustion");
        sortBases(overrideAddExhaustionHookTypes, allBaseOverrideAddExhaustionSuperiors, allBaseOverrideAddExhaustionInferiors, "overrideAddExhaustion");
        sortBases(afterAddExhaustionHookTypes, allBaseAfterAddExhaustionSuperiors, allBaseAfterAddExhaustionInferiors, "afterAddExhaustion");

        sortBases(beforeAddExperienceLevelHookTypes, allBaseBeforeAddExperienceLevelSuperiors, allBaseBeforeAddExperienceLevelInferiors, "beforeAddExperienceLevel");
        sortBases(overrideAddExperienceLevelHookTypes, allBaseOverrideAddExperienceLevelSuperiors, allBaseOverrideAddExperienceLevelInferiors, "overrideAddExperienceLevel");
        sortBases(afterAddExperienceLevelHookTypes, allBaseAfterAddExperienceLevelSuperiors, allBaseAfterAddExperienceLevelInferiors, "afterAddExperienceLevel");

        sortBases(beforeAddMovementStatHookTypes, allBaseBeforeAddMovementStatSuperiors, allBaseBeforeAddMovementStatInferiors, "beforeAddMovementStat");
        sortBases(overrideAddMovementStatHookTypes, allBaseOverrideAddMovementStatSuperiors, allBaseOverrideAddMovementStatInferiors, "overrideAddMovementStat");
        sortBases(afterAddMovementStatHookTypes, allBaseAfterAddMovementStatSuperiors, allBaseAfterAddMovementStatInferiors, "afterAddMovementStat");

        sortBases(beforeAddStatHookTypes, allBaseBeforeAddStatSuperiors, allBaseBeforeAddStatInferiors, "beforeAddStat");
        sortBases(overrideAddStatHookTypes, allBaseOverrideAddStatSuperiors, allBaseOverrideAddStatInferiors, "overrideAddStat");
        sortBases(afterAddStatHookTypes, allBaseAfterAddStatSuperiors, allBaseAfterAddStatInferiors, "afterAddStat");

        sortBases(beforeAreEyesInFluidHookTypes, allBaseBeforeAreEyesInFluidSuperiors, allBaseBeforeAreEyesInFluidInferiors, "beforeAreEyesInFluid");
        sortBases(overrideAreEyesInFluidHookTypes, allBaseOverrideAreEyesInFluidSuperiors, allBaseOverrideAreEyesInFluidInferiors, "overrideAreEyesInFluid");
        sortBases(afterAreEyesInFluidHookTypes, allBaseAfterAreEyesInFluidSuperiors, allBaseAfterAreEyesInFluidInferiors, "afterAreEyesInFluid");

        sortBases(beforeAttackEntityFromHookTypes, allBaseBeforeAttackEntityFromSuperiors, allBaseBeforeAttackEntityFromInferiors, "beforeAttackEntityFrom");
        sortBases(overrideAttackEntityFromHookTypes, allBaseOverrideAttackEntityFromSuperiors, allBaseOverrideAttackEntityFromInferiors, "overrideAttackEntityFrom");
        sortBases(afterAttackEntityFromHookTypes, allBaseAfterAttackEntityFromSuperiors, allBaseAfterAttackEntityFromInferiors, "afterAttackEntityFrom");

        sortBases(beforeAttackTargetEntityWithCurrentItemHookTypes, allBaseBeforeAttackTargetEntityWithCurrentItemSuperiors, allBaseBeforeAttackTargetEntityWithCurrentItemInferiors, "beforeAttackTargetEntityWithCurrentItem");
        sortBases(overrideAttackTargetEntityWithCurrentItemHookTypes, allBaseOverrideAttackTargetEntityWithCurrentItemSuperiors, allBaseOverrideAttackTargetEntityWithCurrentItemInferiors, "overrideAttackTargetEntityWithCurrentItem");
        sortBases(afterAttackTargetEntityWithCurrentItemHookTypes, allBaseAfterAttackTargetEntityWithCurrentItemSuperiors, allBaseAfterAttackTargetEntityWithCurrentItemInferiors, "afterAttackTargetEntityWithCurrentItem");

        sortBases(beforeCanBreatheUnderwaterHookTypes, allBaseBeforeCanBreatheUnderwaterSuperiors, allBaseBeforeCanBreatheUnderwaterInferiors, "beforeCanBreatheUnderwater");
        sortBases(overrideCanBreatheUnderwaterHookTypes, allBaseOverrideCanBreatheUnderwaterSuperiors, allBaseOverrideCanBreatheUnderwaterInferiors, "overrideCanBreatheUnderwater");
        sortBases(afterCanBreatheUnderwaterHookTypes, allBaseAfterCanBreatheUnderwaterSuperiors, allBaseAfterCanBreatheUnderwaterInferiors, "afterCanBreatheUnderwater");

        sortBases(beforeCanHarvestBlockHookTypes, allBaseBeforeCanHarvestBlockSuperiors, allBaseBeforeCanHarvestBlockInferiors, "beforeCanHarvestBlock");
        sortBases(overrideCanHarvestBlockHookTypes, allBaseOverrideCanHarvestBlockSuperiors, allBaseOverrideCanHarvestBlockInferiors, "overrideCanHarvestBlock");
        sortBases(afterCanHarvestBlockHookTypes, allBaseAfterCanHarvestBlockSuperiors, allBaseAfterCanHarvestBlockInferiors, "afterCanHarvestBlock");

        sortBases(beforeCanPlayerEditHookTypes, allBaseBeforeCanPlayerEditSuperiors, allBaseBeforeCanPlayerEditInferiors, "beforeCanPlayerEdit");
        sortBases(overrideCanPlayerEditHookTypes, allBaseOverrideCanPlayerEditSuperiors, allBaseOverrideCanPlayerEditInferiors, "overrideCanPlayerEdit");
        sortBases(afterCanPlayerEditHookTypes, allBaseAfterCanPlayerEditSuperiors, allBaseAfterCanPlayerEditInferiors, "afterCanPlayerEdit");

        sortBases(beforeCanTriggerWalkingHookTypes, allBaseBeforeCanTriggerWalkingSuperiors, allBaseBeforeCanTriggerWalkingInferiors, "beforeCanTriggerWalking");
        sortBases(overrideCanTriggerWalkingHookTypes, allBaseOverrideCanTriggerWalkingSuperiors, allBaseOverrideCanTriggerWalkingInferiors, "overrideCanTriggerWalking");
        sortBases(afterCanTriggerWalkingHookTypes, allBaseAfterCanTriggerWalkingSuperiors, allBaseAfterCanTriggerWalkingInferiors, "afterCanTriggerWalking");

        sortBases(beforeCopyFromHookTypes, allBaseBeforeCopyFromSuperiors, allBaseBeforeCopyFromInferiors, "beforeCopyFrom");
        sortBases(overrideCopyFromHookTypes, allBaseOverrideCopyFromSuperiors, allBaseOverrideCopyFromInferiors, "overrideCopyFrom");
        sortBases(afterCopyFromHookTypes, allBaseAfterCopyFromSuperiors, allBaseAfterCopyFromInferiors, "afterCopyFrom");

        sortBases(beforeDamageEntityHookTypes, allBaseBeforeDamageEntitySuperiors, allBaseBeforeDamageEntityInferiors, "beforeDamageEntity");
        sortBases(overrideDamageEntityHookTypes, allBaseOverrideDamageEntitySuperiors, allBaseOverrideDamageEntityInferiors, "overrideDamageEntity");
        sortBases(afterDamageEntityHookTypes, allBaseAfterDamageEntitySuperiors, allBaseAfterDamageEntityInferiors, "afterDamageEntity");

        sortBases(beforeDropItemHookTypes, allBaseBeforeDropItemSuperiors, allBaseBeforeDropItemInferiors, "beforeDropItem");
        sortBases(overrideDropItemHookTypes, allBaseOverrideDropItemSuperiors, allBaseOverrideDropItemInferiors, "overrideDropItem");
        sortBases(afterDropItemHookTypes, allBaseAfterDropItemSuperiors, allBaseAfterDropItemInferiors, "afterDropItem");

        sortBases(beforeGetAIMoveSpeedHookTypes, allBaseBeforeGetAIMoveSpeedSuperiors, allBaseBeforeGetAIMoveSpeedInferiors, "beforeGetAIMoveSpeed");
        sortBases(overrideGetAIMoveSpeedHookTypes, allBaseOverrideGetAIMoveSpeedSuperiors, allBaseOverrideGetAIMoveSpeedInferiors, "overrideGetAIMoveSpeed");
        sortBases(afterGetAIMoveSpeedHookTypes, allBaseAfterGetAIMoveSpeedSuperiors, allBaseAfterGetAIMoveSpeedInferiors, "afterGetAIMoveSpeed");

        sortBases(beforeGetBrightnessHookTypes, allBaseBeforeGetBrightnessSuperiors, allBaseBeforeGetBrightnessInferiors, "beforeGetBrightness");
        sortBases(overrideGetBrightnessHookTypes, allBaseOverrideGetBrightnessSuperiors, allBaseOverrideGetBrightnessInferiors, "overrideGetBrightness");
        sortBases(afterGetBrightnessHookTypes, allBaseAfterGetBrightnessSuperiors, allBaseAfterGetBrightnessInferiors, "afterGetBrightness");

        sortBases(beforeGetDigSpeedHookTypes, allBaseBeforeGetDigSpeedSuperiors, allBaseBeforeGetDigSpeedInferiors, "beforeGetDigSpeed");
        sortBases(overrideGetDigSpeedHookTypes, allBaseOverrideGetDigSpeedSuperiors, allBaseOverrideGetDigSpeedInferiors, "overrideGetDigSpeed");
        sortBases(afterGetDigSpeedHookTypes, allBaseAfterGetDigSpeedSuperiors, allBaseAfterGetDigSpeedInferiors, "afterGetDigSpeed");

        sortBases(beforeGetDistanceSqHookTypes, allBaseBeforeGetDistanceSqSuperiors, allBaseBeforeGetDistanceSqInferiors, "beforeGetDistanceSq");
        sortBases(overrideGetDistanceSqHookTypes, allBaseOverrideGetDistanceSqSuperiors, allBaseOverrideGetDistanceSqInferiors, "overrideGetDistanceSq");
        sortBases(afterGetDistanceSqHookTypes, allBaseAfterGetDistanceSqSuperiors, allBaseAfterGetDistanceSqInferiors, "afterGetDistanceSq");

        sortBases(beforeGetDistanceSqToEntityHookTypes, allBaseBeforeGetDistanceSqToEntitySuperiors, allBaseBeforeGetDistanceSqToEntityInferiors, "beforeGetDistanceSqToEntity");
        sortBases(overrideGetDistanceSqToEntityHookTypes, allBaseOverrideGetDistanceSqToEntitySuperiors, allBaseOverrideGetDistanceSqToEntityInferiors, "overrideGetDistanceSqToEntity");
        sortBases(afterGetDistanceSqToEntityHookTypes, allBaseAfterGetDistanceSqToEntitySuperiors, allBaseAfterGetDistanceSqToEntityInferiors, "afterGetDistanceSqToEntity");

        sortBases(beforeGetDistanceSqVecHookTypes, allBaseBeforeGetDistanceSqVecSuperiors, allBaseBeforeGetDistanceSqVecInferiors, "beforeGetDistanceSqVec");
        sortBases(overrideGetDistanceSqVecHookTypes, allBaseOverrideGetDistanceSqVecSuperiors, allBaseOverrideGetDistanceSqVecInferiors, "overrideGetDistanceSqVec");
        sortBases(afterGetDistanceSqVecHookTypes, allBaseAfterGetDistanceSqVecSuperiors, allBaseAfterGetDistanceSqVecInferiors, "afterGetDistanceSqVec");

        sortBases(beforeGetHurtSoundHookTypes, allBaseBeforeGetHurtSoundSuperiors, allBaseBeforeGetHurtSoundInferiors, "beforeGetHurtSound");
        sortBases(overrideGetHurtSoundHookTypes, allBaseOverrideGetHurtSoundSuperiors, allBaseOverrideGetHurtSoundInferiors, "overrideGetHurtSound");
        sortBases(afterGetHurtSoundHookTypes, allBaseAfterGetHurtSoundSuperiors, allBaseAfterGetHurtSoundInferiors, "afterGetHurtSound");

        sortBases(beforeGetNameHookTypes, allBaseBeforeGetNameSuperiors, allBaseBeforeGetNameInferiors, "beforeGetName");
        sortBases(overrideGetNameHookTypes, allBaseOverrideGetNameSuperiors, allBaseOverrideGetNameInferiors, "overrideGetName");
        sortBases(afterGetNameHookTypes, allBaseAfterGetNameSuperiors, allBaseAfterGetNameInferiors, "afterGetName");

        sortBases(beforeGetSizeHookTypes, allBaseBeforeGetSizeSuperiors, allBaseBeforeGetSizeInferiors, "beforeGetSize");
        sortBases(overrideGetSizeHookTypes, allBaseOverrideGetSizeSuperiors, allBaseOverrideGetSizeInferiors, "overrideGetSize");
        sortBases(afterGetSizeHookTypes, allBaseAfterGetSizeSuperiors, allBaseAfterGetSizeInferiors, "afterGetSize");

        sortBases(beforeGetSleepTimerHookTypes, allBaseBeforeGetSleepTimerSuperiors, allBaseBeforeGetSleepTimerInferiors, "beforeGetSleepTimer");
        sortBases(overrideGetSleepTimerHookTypes, allBaseOverrideGetSleepTimerSuperiors, allBaseOverrideGetSleepTimerInferiors, "overrideGetSleepTimer");
        sortBases(afterGetSleepTimerHookTypes, allBaseAfterGetSleepTimerSuperiors, allBaseAfterGetSleepTimerInferiors, "afterGetSleepTimer");

        sortBases(beforeGetStandingEyeHeightHookTypes, allBaseBeforeGetStandingEyeHeightSuperiors, allBaseBeforeGetStandingEyeHeightInferiors, "beforeGetStandingEyeHeight");
        sortBases(overrideGetStandingEyeHeightHookTypes, allBaseOverrideGetStandingEyeHeightSuperiors, allBaseOverrideGetStandingEyeHeightInferiors, "overrideGetStandingEyeHeight");
        sortBases(afterGetStandingEyeHeightHookTypes, allBaseAfterGetStandingEyeHeightSuperiors, allBaseAfterGetStandingEyeHeightInferiors, "afterGetStandingEyeHeight");

        sortBases(beforeGiveExperiencePointsHookTypes, allBaseBeforeGiveExperiencePointsSuperiors, allBaseBeforeGiveExperiencePointsInferiors, "beforeGiveExperiencePoints");
        sortBases(overrideGiveExperiencePointsHookTypes, allBaseOverrideGiveExperiencePointsSuperiors, allBaseOverrideGiveExperiencePointsInferiors, "overrideGiveExperiencePoints");
        sortBases(afterGiveExperiencePointsHookTypes, allBaseAfterGiveExperiencePointsSuperiors, allBaseAfterGiveExperiencePointsInferiors, "afterGiveExperiencePoints");

        sortBases(beforeHandleWaterMovementHookTypes, allBaseBeforeHandleWaterMovementSuperiors, allBaseBeforeHandleWaterMovementInferiors, "beforeHandleWaterMovement");
        sortBases(overrideHandleWaterMovementHookTypes, allBaseOverrideHandleWaterMovementSuperiors, allBaseOverrideHandleWaterMovementInferiors, "overrideHandleWaterMovement");
        sortBases(afterHandleWaterMovementHookTypes, allBaseAfterHandleWaterMovementSuperiors, allBaseAfterHandleWaterMovementInferiors, "afterHandleWaterMovement");

        sortBases(beforeHealHookTypes, allBaseBeforeHealSuperiors, allBaseBeforeHealInferiors, "beforeHeal");
        sortBases(overrideHealHookTypes, allBaseOverrideHealSuperiors, allBaseOverrideHealInferiors, "overrideHeal");
        sortBases(afterHealHookTypes, allBaseAfterHealSuperiors, allBaseAfterHealInferiors, "afterHeal");

        sortBases(beforeIsEntityInsideOpaqueBlockHookTypes, allBaseBeforeIsEntityInsideOpaqueBlockSuperiors, allBaseBeforeIsEntityInsideOpaqueBlockInferiors, "beforeIsEntityInsideOpaqueBlock");
        sortBases(overrideIsEntityInsideOpaqueBlockHookTypes, allBaseOverrideIsEntityInsideOpaqueBlockSuperiors, allBaseOverrideIsEntityInsideOpaqueBlockInferiors, "overrideIsEntityInsideOpaqueBlock");
        sortBases(afterIsEntityInsideOpaqueBlockHookTypes, allBaseAfterIsEntityInsideOpaqueBlockSuperiors, allBaseAfterIsEntityInsideOpaqueBlockInferiors, "afterIsEntityInsideOpaqueBlock");

        sortBases(beforeIsInWaterHookTypes, allBaseBeforeIsInWaterSuperiors, allBaseBeforeIsInWaterInferiors, "beforeIsInWater");
        sortBases(overrideIsInWaterHookTypes, allBaseOverrideIsInWaterSuperiors, allBaseOverrideIsInWaterInferiors, "overrideIsInWater");
        sortBases(afterIsInWaterHookTypes, allBaseAfterIsInWaterSuperiors, allBaseAfterIsInWaterInferiors, "afterIsInWater");

        sortBases(beforeIsOnLadderHookTypes, allBaseBeforeIsOnLadderSuperiors, allBaseBeforeIsOnLadderInferiors, "beforeIsOnLadder");
        sortBases(overrideIsOnLadderHookTypes, allBaseOverrideIsOnLadderSuperiors, allBaseOverrideIsOnLadderInferiors, "overrideIsOnLadder");
        sortBases(afterIsOnLadderHookTypes, allBaseAfterIsOnLadderSuperiors, allBaseAfterIsOnLadderInferiors, "afterIsOnLadder");

        sortBases(beforeIsShiftKeyDownHookTypes, allBaseBeforeIsShiftKeyDownSuperiors, allBaseBeforeIsShiftKeyDownInferiors, "beforeIsShiftKeyDown");
        sortBases(overrideIsShiftKeyDownHookTypes, allBaseOverrideIsShiftKeyDownSuperiors, allBaseOverrideIsShiftKeyDownInferiors, "overrideIsShiftKeyDown");
        sortBases(afterIsShiftKeyDownHookTypes, allBaseAfterIsShiftKeyDownSuperiors, allBaseAfterIsShiftKeyDownInferiors, "afterIsShiftKeyDown");

        sortBases(beforeIsSleepingHookTypes, allBaseBeforeIsSleepingSuperiors, allBaseBeforeIsSleepingInferiors, "beforeIsSleeping");
        sortBases(overrideIsSleepingHookTypes, allBaseOverrideIsSleepingSuperiors, allBaseOverrideIsSleepingInferiors, "overrideIsSleeping");
        sortBases(afterIsSleepingHookTypes, allBaseAfterIsSleepingSuperiors, allBaseAfterIsSleepingInferiors, "afterIsSleeping");

        sortBases(beforeIsSprintingHookTypes, allBaseBeforeIsSprintingSuperiors, allBaseBeforeIsSprintingInferiors, "beforeIsSprinting");
        sortBases(overrideIsSprintingHookTypes, allBaseOverrideIsSprintingSuperiors, allBaseOverrideIsSprintingInferiors, "overrideIsSprinting");
        sortBases(afterIsSprintingHookTypes, allBaseAfterIsSprintingSuperiors, allBaseAfterIsSprintingInferiors, "afterIsSprinting");

        sortBases(beforeJumpHookTypes, allBaseBeforeJumpSuperiors, allBaseBeforeJumpInferiors, "beforeJump");
        sortBases(overrideJumpHookTypes, allBaseOverrideJumpSuperiors, allBaseOverrideJumpInferiors, "overrideJump");
        sortBases(afterJumpHookTypes, allBaseAfterJumpSuperiors, allBaseAfterJumpInferiors, "afterJump");

        sortBases(beforeKnockBackHookTypes, allBaseBeforeKnockBackSuperiors, allBaseBeforeKnockBackInferiors, "beforeKnockBack");
        sortBases(overrideKnockBackHookTypes, allBaseOverrideKnockBackSuperiors, allBaseOverrideKnockBackInferiors, "overrideKnockBack");
        sortBases(afterKnockBackHookTypes, allBaseAfterKnockBackSuperiors, allBaseAfterKnockBackInferiors, "afterKnockBack");

        sortBases(beforeLivingTickHookTypes, allBaseBeforeLivingTickSuperiors, allBaseBeforeLivingTickInferiors, "beforeLivingTick");
        sortBases(overrideLivingTickHookTypes, allBaseOverrideLivingTickSuperiors, allBaseOverrideLivingTickInferiors, "overrideLivingTick");
        sortBases(afterLivingTickHookTypes, allBaseAfterLivingTickSuperiors, allBaseAfterLivingTickInferiors, "afterLivingTick");

        sortBases(beforeMoveHookTypes, allBaseBeforeMoveSuperiors, allBaseBeforeMoveInferiors, "beforeMove");
        sortBases(overrideMoveHookTypes, allBaseOverrideMoveSuperiors, allBaseOverrideMoveInferiors, "overrideMove");
        sortBases(afterMoveHookTypes, allBaseAfterMoveSuperiors, allBaseAfterMoveInferiors, "afterMove");

        sortBases(beforeMoveRelativeHookTypes, allBaseBeforeMoveRelativeSuperiors, allBaseBeforeMoveRelativeInferiors, "beforeMoveRelative");
        sortBases(overrideMoveRelativeHookTypes, allBaseOverrideMoveRelativeSuperiors, allBaseOverrideMoveRelativeInferiors, "overrideMoveRelative");
        sortBases(afterMoveRelativeHookTypes, allBaseAfterMoveRelativeSuperiors, allBaseAfterMoveRelativeInferiors, "afterMoveRelative");

        sortBases(beforeOnDeathHookTypes, allBaseBeforeOnDeathSuperiors, allBaseBeforeOnDeathInferiors, "beforeOnDeath");
        sortBases(overrideOnDeathHookTypes, allBaseOverrideOnDeathSuperiors, allBaseOverrideOnDeathInferiors, "overrideOnDeath");
        sortBases(afterOnDeathHookTypes, allBaseAfterOnDeathSuperiors, allBaseAfterOnDeathInferiors, "afterOnDeath");

        sortBases(beforeOnKillEntityHookTypes, allBaseBeforeOnKillEntitySuperiors, allBaseBeforeOnKillEntityInferiors, "beforeOnKillEntity");
        sortBases(overrideOnKillEntityHookTypes, allBaseOverrideOnKillEntitySuperiors, allBaseOverrideOnKillEntityInferiors, "overrideOnKillEntity");
        sortBases(afterOnKillEntityHookTypes, allBaseAfterOnKillEntitySuperiors, allBaseAfterOnKillEntityInferiors, "afterOnKillEntity");

        sortBases(beforeOnLivingFallHookTypes, allBaseBeforeOnLivingFallSuperiors, allBaseBeforeOnLivingFallInferiors, "beforeOnLivingFall");
        sortBases(overrideOnLivingFallHookTypes, allBaseOverrideOnLivingFallSuperiors, allBaseOverrideOnLivingFallInferiors, "overrideOnLivingFall");
        sortBases(afterOnLivingFallHookTypes, allBaseAfterOnLivingFallSuperiors, allBaseAfterOnLivingFallInferiors, "afterOnLivingFall");

        sortBases(beforeOnStruckByLightningHookTypes, allBaseBeforeOnStruckByLightningSuperiors, allBaseBeforeOnStruckByLightningInferiors, "beforeOnStruckByLightning");
        sortBases(overrideOnStruckByLightningHookTypes, allBaseOverrideOnStruckByLightningSuperiors, allBaseOverrideOnStruckByLightningInferiors, "overrideOnStruckByLightning");
        sortBases(afterOnStruckByLightningHookTypes, allBaseAfterOnStruckByLightningSuperiors, allBaseAfterOnStruckByLightningInferiors, "afterOnStruckByLightning");

        sortBases(beforePickHookTypes, allBaseBeforePickSuperiors, allBaseBeforePickInferiors, "beforePick");
        sortBases(overridePickHookTypes, allBaseOverridePickSuperiors, allBaseOverridePickInferiors, "overridePick");
        sortBases(afterPickHookTypes, allBaseAfterPickSuperiors, allBaseAfterPickInferiors, "afterPick");

        sortBases(beforePlayerTickHookTypes, allBaseBeforePlayerTickSuperiors, allBaseBeforePlayerTickInferiors, "beforePlayerTick");
        sortBases(overridePlayerTickHookTypes, allBaseOverridePlayerTickSuperiors, allBaseOverridePlayerTickInferiors, "overridePlayerTick");
        sortBases(afterPlayerTickHookTypes, allBaseAfterPlayerTickSuperiors, allBaseAfterPlayerTickInferiors, "afterPlayerTick");

        sortBases(beforePlayStepSoundHookTypes, allBaseBeforePlayStepSoundSuperiors, allBaseBeforePlayStepSoundInferiors, "beforePlayStepSound");
        sortBases(overridePlayStepSoundHookTypes, allBaseOverridePlayStepSoundSuperiors, allBaseOverridePlayStepSoundInferiors, "overridePlayStepSound");
        sortBases(afterPlayStepSoundHookTypes, allBaseAfterPlayStepSoundSuperiors, allBaseAfterPlayStepSoundInferiors, "afterPlayStepSound");

        sortBases(beforePushOutOfBlocksHookTypes, allBaseBeforePushOutOfBlocksSuperiors, allBaseBeforePushOutOfBlocksInferiors, "beforePushOutOfBlocks");
        sortBases(overridePushOutOfBlocksHookTypes, allBaseOverridePushOutOfBlocksSuperiors, allBaseOverridePushOutOfBlocksInferiors, "overridePushOutOfBlocks");
        sortBases(afterPushOutOfBlocksHookTypes, allBaseAfterPushOutOfBlocksSuperiors, allBaseAfterPushOutOfBlocksInferiors, "afterPushOutOfBlocks");

        sortBases(beforeReadHookTypes, allBaseBeforeReadSuperiors, allBaseBeforeReadInferiors, "beforeRead");
        sortBases(overrideReadHookTypes, allBaseOverrideReadSuperiors, allBaseOverrideReadInferiors, "overrideRead");
        sortBases(afterReadHookTypes, allBaseAfterReadSuperiors, allBaseAfterReadInferiors, "afterRead");

        sortBases(beforeRemoveHookTypes, allBaseBeforeRemoveSuperiors, allBaseBeforeRemoveInferiors, "beforeRemove");
        sortBases(overrideRemoveHookTypes, allBaseOverrideRemoveSuperiors, allBaseOverrideRemoveInferiors, "overrideRemove");
        sortBases(afterRemoveHookTypes, allBaseAfterRemoveSuperiors, allBaseAfterRemoveInferiors, "afterRemove");

        sortBases(beforeSetEntityActionStateHookTypes, allBaseBeforeSetEntityActionStateSuperiors, allBaseBeforeSetEntityActionStateInferiors, "beforeSetEntityActionState");
        sortBases(overrideSetEntityActionStateHookTypes, allBaseOverrideSetEntityActionStateSuperiors, allBaseOverrideSetEntityActionStateInferiors, "overrideSetEntityActionState");
        sortBases(afterSetEntityActionStateHookTypes, allBaseAfterSetEntityActionStateSuperiors, allBaseAfterSetEntityActionStateInferiors, "afterSetEntityActionState");

        sortBases(beforeSetPositionHookTypes, allBaseBeforeSetPositionSuperiors, allBaseBeforeSetPositionInferiors, "beforeSetPosition");
        sortBases(overrideSetPositionHookTypes, allBaseOverrideSetPositionSuperiors, allBaseOverrideSetPositionInferiors, "overrideSetPosition");
        sortBases(afterSetPositionHookTypes, allBaseAfterSetPositionSuperiors, allBaseAfterSetPositionInferiors, "afterSetPosition");

        sortBases(beforeSetPositionAndRotationHookTypes, allBaseBeforeSetPositionAndRotationSuperiors, allBaseBeforeSetPositionAndRotationInferiors, "beforeSetPositionAndRotation");
        sortBases(overrideSetPositionAndRotationHookTypes, allBaseOverrideSetPositionAndRotationSuperiors, allBaseOverrideSetPositionAndRotationInferiors, "overrideSetPositionAndRotation");
        sortBases(afterSetPositionAndRotationHookTypes, allBaseAfterSetPositionAndRotationSuperiors, allBaseAfterSetPositionAndRotationInferiors, "afterSetPositionAndRotation");

        sortBases(beforeSetSneakingHookTypes, allBaseBeforeSetSneakingSuperiors, allBaseBeforeSetSneakingInferiors, "beforeSetSneaking");
        sortBases(overrideSetSneakingHookTypes, allBaseOverrideSetSneakingSuperiors, allBaseOverrideSetSneakingInferiors, "overrideSetSneaking");
        sortBases(afterSetSneakingHookTypes, allBaseAfterSetSneakingSuperiors, allBaseAfterSetSneakingInferiors, "afterSetSneaking");

        sortBases(beforeSetSprintingHookTypes, allBaseBeforeSetSprintingSuperiors, allBaseBeforeSetSprintingInferiors, "beforeSetSprinting");
        sortBases(overrideSetSprintingHookTypes, allBaseOverrideSetSprintingSuperiors, allBaseOverrideSetSprintingInferiors, "overrideSetSprinting");
        sortBases(afterSetSprintingHookTypes, allBaseAfterSetSprintingSuperiors, allBaseAfterSetSprintingInferiors, "afterSetSprinting");

        sortBases(beforeStartRidingHookTypes, allBaseBeforeStartRidingSuperiors, allBaseBeforeStartRidingInferiors, "beforeStartRiding");
        sortBases(overrideStartRidingHookTypes, allBaseOverrideStartRidingSuperiors, allBaseOverrideStartRidingInferiors, "overrideStartRiding");
        sortBases(afterStartRidingHookTypes, allBaseAfterStartRidingSuperiors, allBaseAfterStartRidingInferiors, "afterStartRiding");

        sortBases(beforeSwingArmHookTypes, allBaseBeforeSwingArmSuperiors, allBaseBeforeSwingArmInferiors, "beforeSwingArm");
        sortBases(overrideSwingArmHookTypes, allBaseOverrideSwingArmSuperiors, allBaseOverrideSwingArmInferiors, "overrideSwingArm");
        sortBases(afterSwingArmHookTypes, allBaseAfterSwingArmSuperiors, allBaseAfterSwingArmInferiors, "afterSwingArm");

        sortBases(beforeTickHookTypes, allBaseBeforeTickSuperiors, allBaseBeforeTickInferiors, "beforeTick");
        sortBases(overrideTickHookTypes, allBaseOverrideTickSuperiors, allBaseOverrideTickInferiors, "overrideTick");
        sortBases(afterTickHookTypes, allBaseAfterTickSuperiors, allBaseAfterTickInferiors, "afterTick");

        sortBases(beforeTravelHookTypes, allBaseBeforeTravelSuperiors, allBaseBeforeTravelInferiors, "beforeTravel");
        sortBases(overrideTravelHookTypes, allBaseOverrideTravelSuperiors, allBaseOverrideTravelInferiors, "overrideTravel");
        sortBases(afterTravelHookTypes, allBaseAfterTravelSuperiors, allBaseAfterTravelInferiors, "afterTravel");

        sortBases(beforeTrySleepHookTypes, allBaseBeforeTrySleepSuperiors, allBaseBeforeTrySleepInferiors, "beforeTrySleep");
        sortBases(overrideTrySleepHookTypes, allBaseOverrideTrySleepSuperiors, allBaseOverrideTrySleepInferiors, "overrideTrySleep");
        sortBases(afterTrySleepHookTypes, allBaseAfterTrySleepSuperiors, allBaseAfterTrySleepInferiors, "afterTrySleep");

        sortBases(beforeUpdateEntityActionStateHookTypes, allBaseBeforeUpdateEntityActionStateSuperiors, allBaseBeforeUpdateEntityActionStateInferiors, "beforeUpdateEntityActionState");
        sortBases(overrideUpdateEntityActionStateHookTypes, allBaseOverrideUpdateEntityActionStateSuperiors, allBaseOverrideUpdateEntityActionStateInferiors, "overrideUpdateEntityActionState");
        sortBases(afterUpdateEntityActionStateHookTypes, allBaseAfterUpdateEntityActionStateSuperiors, allBaseAfterUpdateEntityActionStateInferiors, "afterUpdateEntityActionState");

        sortBases(beforeUpdatePotionEffectsHookTypes, allBaseBeforeUpdatePotionEffectsSuperiors, allBaseBeforeUpdatePotionEffectsInferiors, "beforeUpdatePotionEffects");
        sortBases(overrideUpdatePotionEffectsHookTypes, allBaseOverrideUpdatePotionEffectsSuperiors, allBaseOverrideUpdatePotionEffectsInferiors, "overrideUpdatePotionEffects");
        sortBases(afterUpdatePotionEffectsHookTypes, allBaseAfterUpdatePotionEffectsSuperiors, allBaseAfterUpdatePotionEffectsInferiors, "afterUpdatePotionEffects");

        sortBases(beforeUpdateRiddenHookTypes, allBaseBeforeUpdateRiddenSuperiors, allBaseBeforeUpdateRiddenInferiors, "beforeUpdateRidden");
        sortBases(overrideUpdateRiddenHookTypes, allBaseOverrideUpdateRiddenSuperiors, allBaseOverrideUpdateRiddenInferiors, "overrideUpdateRidden");
        sortBases(afterUpdateRiddenHookTypes, allBaseAfterUpdateRiddenSuperiors, allBaseAfterUpdateRiddenInferiors, "afterUpdateRidden");

        sortBases(beforeWakeUpPlayerHookTypes, allBaseBeforeWakeUpPlayerSuperiors, allBaseBeforeWakeUpPlayerInferiors, "beforeWakeUpPlayer");
        sortBases(overrideWakeUpPlayerHookTypes, allBaseOverrideWakeUpPlayerSuperiors, allBaseOverrideWakeUpPlayerInferiors, "overrideWakeUpPlayer");
        sortBases(afterWakeUpPlayerHookTypes, allBaseAfterWakeUpPlayerSuperiors, allBaseAfterWakeUpPlayerInferiors, "afterWakeUpPlayer");

        sortBases(beforeWriteWithoutTypeIdHookTypes, allBaseBeforeWriteWithoutTypeIdSuperiors, allBaseBeforeWriteWithoutTypeIdInferiors, "beforeWriteWithoutTypeId");
        sortBases(overrideWriteWithoutTypeIdHookTypes, allBaseOverrideWriteWithoutTypeIdSuperiors, allBaseOverrideWriteWithoutTypeIdInferiors, "overrideWriteWithoutTypeId");
        sortBases(afterWriteWithoutTypeIdHookTypes, allBaseAfterWriteWithoutTypeIdSuperiors, allBaseAfterWriteWithoutTypeIdInferiors, "afterWriteWithoutTypeId");

        initialized = true;
    }

    private static List<IServerPlayerEntity> getAllInstancesList()
    {
        List<IServerPlayerEntity> result = new ArrayList<>();
        Object entityPlayerList;
        try {
            Object minecraftServer = ServerLifecycleHooks.getCurrentServer();
            entityPlayerList = minecraftServer != null ? MinecraftServer.class.getMethod("func_184103_al").invoke(minecraftServer) : null;
        } catch (Exception obfuscatedException) {
            try {
                Object minecraftServer = ServerLifecycleHooks.getCurrentServer();
                entityPlayerList = minecraftServer != null ? MinecraftServer.class.getMethod("getPlayerList").invoke(minecraftServer) : null;
            } catch (Exception deobfuscatedException) {
                throw new RuntimeException("Unable to acquire list of current server players.", obfuscatedException);
            }
        }
        if (entityPlayerList != null) {
            for (Object entityPlayer : (List<?>) entityPlayerList) {
                result.add((IServerPlayerEntity) entityPlayer);
            }
        }
        return result;
    }

    public static ServerPlayerEntity[] getAllInstances()
    {
        return getAllInstancesList().stream().map(instance -> (ServerPlayerEntity) instance).toArray(ServerPlayerEntity[]::new);
    }

    public static void beforeLocalConstructing(IServerPlayerEntity serverPlayer, MinecraftServer paramMinecraftServer, ServerWorld paramWorldServer, GameProfile paramGameProfile, PlayerInteractionManager paramPlayerInteractionManager)
    {
        ServerPlayerAPI serverPlayerAPI = serverPlayer.getServerPlayerAPI();
        if (serverPlayerAPI != null) {
            serverPlayerAPI.load();
        }

        if (serverPlayerAPI != null) {
            serverPlayerAPI.beforeLocalConstructing(paramMinecraftServer, paramWorldServer, paramGameProfile, paramPlayerInteractionManager);
        }
    }

    public static void afterLocalConstructing(IServerPlayerEntity serverPlayer, MinecraftServer paramMinecraftServer, ServerWorld paramWorldServer, GameProfile paramGameProfile, PlayerInteractionManager paramPlayerInteractionManager)
    {
        ServerPlayerAPI serverPlayerAPI = serverPlayer.getServerPlayerAPI();
        if (serverPlayerAPI != null) {
            serverPlayerAPI.afterLocalConstructing(paramMinecraftServer, paramWorldServer, paramGameProfile, paramPlayerInteractionManager);
        }
    }

    public static ServerPlayerEntityBase getServerPlayerBase(IServerPlayerEntity serverPlayer, String baseId)
    {
        ServerPlayerAPI serverPlayerAPI = serverPlayer.getServerPlayerAPI();
        if (serverPlayerAPI != null) {
            return serverPlayerAPI.getServerPlayerBase(baseId);
        }
        return null;
    }

    public static Set<String> getServerPlayerBaseIds(IServerPlayerEntity serverPlayer)
    {
        ServerPlayerAPI serverPlayerAPI = serverPlayer.getServerPlayerAPI();
        Set<String> result;
        if (serverPlayerAPI != null) {
            result = serverPlayerAPI.getServerPlayerBaseIds();
        } else {
            result = Collections.emptySet();
        }
        return result;
    }

    public static Object dynamic(IServerPlayerEntity serverPlayer, String key, Object[] parameters)
    {
        ServerPlayerAPI serverPlayerAPI = serverPlayer.getServerPlayerAPI();
        if (serverPlayerAPI != null) {
            return serverPlayerAPI.dynamic(key, parameters);
        }
        return null;
    }

    private static void sortBases(List<String> list, Map<String, String[]> allBaseSuperiors, Map<String, String[]> allBaseInferiors, String methodName)
    {
        new ServerPlayerBaseSorter(list, allBaseSuperiors, allBaseInferiors, methodName).Sort();
    }

    private final static Map<String, String[]> EmptySortMap = Collections.unmodifiableMap(new HashMap<>());

    private static void sortDynamicBases(Map<String, List<String>> lists, Map<String, Map<String, String[]>> allBaseSuperiors, Map<String, Map<String, String[]>> allBaseInferiors, String key)
    {
        List<String> types = lists.get(key);
        if (types != null && types.size() > 1) {
            sortBases(types, getDynamicSorters(key, types, allBaseSuperiors), getDynamicSorters(key, types, allBaseInferiors), key);
        }
    }

    private static Map<String, String[]> getDynamicSorters(String key, List<String> toSort, Map<String, Map<String, String[]>> allBaseValues)
    {
        Map<String, String[]> superiors = null;

        for (String id : toSort) {
            Map<String, String[]> idSuperiors = allBaseValues.get(id);
            if (idSuperiors == null) {
                continue;
            }

            String[] keySuperiorIds = idSuperiors.get(key);
            if (keySuperiorIds != null && keySuperiorIds.length > 0) {
                if (superiors == null) {
                    superiors = new HashMap<>(1);
                }
                superiors.put(id, keySuperiorIds);
            }
        }

        return superiors != null ? superiors : EmptySortMap;
    }

    private ServerPlayerAPI(IServerPlayerEntity player)
    {
        this.player = player;
    }

    private void load()
    {
        Iterator<String> iterator = allBaseConstructors.keySet().iterator();
        while (iterator.hasNext()) {
            String id = iterator.next();
            ServerPlayerEntityBase toAttach = this.createServerPlayerBase(id);
            toAttach.beforeBaseAttach(false);
            this.allBaseObjects.put(id, toAttach);
            this.baseObjectsToId.put(toAttach, id);
        }

        this.beforeLocalConstructingHooks = this.create(beforeLocalConstructingHookTypes);
        this.afterLocalConstructingHooks = this.create(afterLocalConstructingHookTypes);

        this.updateServerPlayerBases();

        iterator = this.allBaseObjects.keySet().iterator();
        while (iterator.hasNext()) {
            this.allBaseObjects.get(iterator.next()).afterBaseAttach(false);
        }
    }

    private ServerPlayerEntityBase createServerPlayerBase(String id)
    {
        Constructor<?> constructor = allBaseConstructors.get(id);

        ServerPlayerEntityBase base;
        try {
            if (constructor.getParameterTypes().length == 1) {
                base = (ServerPlayerEntityBase) constructor.newInstance(this);
            } else {
                base = (ServerPlayerEntityBase) constructor.newInstance(this, id);
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception while creating a ServerPlayerBase of type '" + constructor.getDeclaringClass() + "'", e);
        }
        return base;
    }

    private void updateServerPlayerBases()
    {
        this.beforeAddExhaustionHooks = this.create(beforeAddExhaustionHookTypes);
        this.overrideAddExhaustionHooks = this.create(overrideAddExhaustionHookTypes);
        this.afterAddExhaustionHooks = this.create(afterAddExhaustionHookTypes);
        this.isAddExhaustionModded = this.beforeAddExhaustionHooks != null || this.overrideAddExhaustionHooks != null || this.afterAddExhaustionHooks != null;

        this.beforeAddExperienceLevelHooks = this.create(beforeAddExperienceLevelHookTypes);
        this.overrideAddExperienceLevelHooks = this.create(overrideAddExperienceLevelHookTypes);
        this.afterAddExperienceLevelHooks = this.create(afterAddExperienceLevelHookTypes);
        this.isAddExperienceLevelModded = this.beforeAddExperienceLevelHooks != null || this.overrideAddExperienceLevelHooks != null || this.afterAddExperienceLevelHooks != null;

        this.beforeAddMovementStatHooks = this.create(beforeAddMovementStatHookTypes);
        this.overrideAddMovementStatHooks = this.create(overrideAddMovementStatHookTypes);
        this.afterAddMovementStatHooks = this.create(afterAddMovementStatHookTypes);
        this.isAddMovementStatModded = this.beforeAddMovementStatHooks != null || this.overrideAddMovementStatHooks != null || this.afterAddMovementStatHooks != null;

        this.beforeAddStatHooks = this.create(beforeAddStatHookTypes);
        this.overrideAddStatHooks = this.create(overrideAddStatHookTypes);
        this.afterAddStatHooks = this.create(afterAddStatHookTypes);
        this.isAddStatModded = this.beforeAddStatHooks != null || this.overrideAddStatHooks != null || this.afterAddStatHooks != null;

        this.beforeAreEyesInFluidHooks = this.create(beforeAreEyesInFluidHookTypes);
        this.overrideAreEyesInFluidHooks = this.create(overrideAreEyesInFluidHookTypes);
        this.afterAreEyesInFluidHooks = this.create(afterAreEyesInFluidHookTypes);
        this.isAreEyesInFluidModded = this.beforeAreEyesInFluidHooks != null || this.overrideAreEyesInFluidHooks != null || this.afterAreEyesInFluidHooks != null;

        this.beforeAttackEntityFromHooks = this.create(beforeAttackEntityFromHookTypes);
        this.overrideAttackEntityFromHooks = this.create(overrideAttackEntityFromHookTypes);
        this.afterAttackEntityFromHooks = this.create(afterAttackEntityFromHookTypes);
        this.isAttackEntityFromModded = this.beforeAttackEntityFromHooks != null || this.overrideAttackEntityFromHooks != null || this.afterAttackEntityFromHooks != null;

        this.beforeAttackTargetEntityWithCurrentItemHooks = this.create(beforeAttackTargetEntityWithCurrentItemHookTypes);
        this.overrideAttackTargetEntityWithCurrentItemHooks = this.create(overrideAttackTargetEntityWithCurrentItemHookTypes);
        this.afterAttackTargetEntityWithCurrentItemHooks = this.create(afterAttackTargetEntityWithCurrentItemHookTypes);
        this.isAttackTargetEntityWithCurrentItemModded = this.beforeAttackTargetEntityWithCurrentItemHooks != null || this.overrideAttackTargetEntityWithCurrentItemHooks != null || this.afterAttackTargetEntityWithCurrentItemHooks != null;

        this.beforeCanBreatheUnderwaterHooks = this.create(beforeCanBreatheUnderwaterHookTypes);
        this.overrideCanBreatheUnderwaterHooks = this.create(overrideCanBreatheUnderwaterHookTypes);
        this.afterCanBreatheUnderwaterHooks = this.create(afterCanBreatheUnderwaterHookTypes);
        this.isCanBreatheUnderwaterModded = this.beforeCanBreatheUnderwaterHooks != null || this.overrideCanBreatheUnderwaterHooks != null || this.afterCanBreatheUnderwaterHooks != null;

        this.beforeCanHarvestBlockHooks = this.create(beforeCanHarvestBlockHookTypes);
        this.overrideCanHarvestBlockHooks = this.create(overrideCanHarvestBlockHookTypes);
        this.afterCanHarvestBlockHooks = this.create(afterCanHarvestBlockHookTypes);
        this.isCanHarvestBlockModded = this.beforeCanHarvestBlockHooks != null || this.overrideCanHarvestBlockHooks != null || this.afterCanHarvestBlockHooks != null;

        this.beforeCanPlayerEditHooks = this.create(beforeCanPlayerEditHookTypes);
        this.overrideCanPlayerEditHooks = this.create(overrideCanPlayerEditHookTypes);
        this.afterCanPlayerEditHooks = this.create(afterCanPlayerEditHookTypes);
        this.isCanPlayerEditModded = this.beforeCanPlayerEditHooks != null || this.overrideCanPlayerEditHooks != null || this.afterCanPlayerEditHooks != null;

        this.beforeCanTriggerWalkingHooks = this.create(beforeCanTriggerWalkingHookTypes);
        this.overrideCanTriggerWalkingHooks = this.create(overrideCanTriggerWalkingHookTypes);
        this.afterCanTriggerWalkingHooks = this.create(afterCanTriggerWalkingHookTypes);
        this.isCanTriggerWalkingModded = this.beforeCanTriggerWalkingHooks != null || this.overrideCanTriggerWalkingHooks != null || this.afterCanTriggerWalkingHooks != null;

        this.beforeCopyFromHooks = this.create(beforeCopyFromHookTypes);
        this.overrideCopyFromHooks = this.create(overrideCopyFromHookTypes);
        this.afterCopyFromHooks = this.create(afterCopyFromHookTypes);
        this.isCopyFromModded = this.beforeCopyFromHooks != null || this.overrideCopyFromHooks != null || this.afterCopyFromHooks != null;

        this.beforeDamageEntityHooks = this.create(beforeDamageEntityHookTypes);
        this.overrideDamageEntityHooks = this.create(overrideDamageEntityHookTypes);
        this.afterDamageEntityHooks = this.create(afterDamageEntityHookTypes);
        this.isDamageEntityModded = this.beforeDamageEntityHooks != null || this.overrideDamageEntityHooks != null || this.afterDamageEntityHooks != null;

        this.beforeDropItemHooks = this.create(beforeDropItemHookTypes);
        this.overrideDropItemHooks = this.create(overrideDropItemHookTypes);
        this.afterDropItemHooks = this.create(afterDropItemHookTypes);
        this.isDropItemModded = this.beforeDropItemHooks != null || this.overrideDropItemHooks != null || this.afterDropItemHooks != null;

        this.beforeGetAIMoveSpeedHooks = this.create(beforeGetAIMoveSpeedHookTypes);
        this.overrideGetAIMoveSpeedHooks = this.create(overrideGetAIMoveSpeedHookTypes);
        this.afterGetAIMoveSpeedHooks = this.create(afterGetAIMoveSpeedHookTypes);
        this.isGetAIMoveSpeedModded = this.beforeGetAIMoveSpeedHooks != null || this.overrideGetAIMoveSpeedHooks != null || this.afterGetAIMoveSpeedHooks != null;

        this.beforeGetBrightnessHooks = this.create(beforeGetBrightnessHookTypes);
        this.overrideGetBrightnessHooks = this.create(overrideGetBrightnessHookTypes);
        this.afterGetBrightnessHooks = this.create(afterGetBrightnessHookTypes);
        this.isGetBrightnessModded = this.beforeGetBrightnessHooks != null || this.overrideGetBrightnessHooks != null || this.afterGetBrightnessHooks != null;

        this.beforeGetDigSpeedHooks = this.create(beforeGetDigSpeedHookTypes);
        this.overrideGetDigSpeedHooks = this.create(overrideGetDigSpeedHookTypes);
        this.afterGetDigSpeedHooks = this.create(afterGetDigSpeedHookTypes);
        this.isGetDigSpeedModded = this.beforeGetDigSpeedHooks != null || this.overrideGetDigSpeedHooks != null || this.afterGetDigSpeedHooks != null;

        this.beforeGetDistanceSqHooks = this.create(beforeGetDistanceSqHookTypes);
        this.overrideGetDistanceSqHooks = this.create(overrideGetDistanceSqHookTypes);
        this.afterGetDistanceSqHooks = this.create(afterGetDistanceSqHookTypes);
        this.isGetDistanceSqModded = this.beforeGetDistanceSqHooks != null || this.overrideGetDistanceSqHooks != null || this.afterGetDistanceSqHooks != null;

        this.beforeGetDistanceSqToEntityHooks = this.create(beforeGetDistanceSqToEntityHookTypes);
        this.overrideGetDistanceSqToEntityHooks = this.create(overrideGetDistanceSqToEntityHookTypes);
        this.afterGetDistanceSqToEntityHooks = this.create(afterGetDistanceSqToEntityHookTypes);
        this.isGetDistanceSqToEntityModded = this.beforeGetDistanceSqToEntityHooks != null || this.overrideGetDistanceSqToEntityHooks != null || this.afterGetDistanceSqToEntityHooks != null;

        this.beforeGetDistanceSqVecHooks = this.create(beforeGetDistanceSqVecHookTypes);
        this.overrideGetDistanceSqVecHooks = this.create(overrideGetDistanceSqVecHookTypes);
        this.afterGetDistanceSqVecHooks = this.create(afterGetDistanceSqVecHookTypes);
        this.isGetDistanceSqVecModded = this.beforeGetDistanceSqVecHooks != null || this.overrideGetDistanceSqVecHooks != null || this.afterGetDistanceSqVecHooks != null;

        this.beforeGetHurtSoundHooks = this.create(beforeGetHurtSoundHookTypes);
        this.overrideGetHurtSoundHooks = this.create(overrideGetHurtSoundHookTypes);
        this.afterGetHurtSoundHooks = this.create(afterGetHurtSoundHookTypes);
        this.isGetHurtSoundModded = this.beforeGetHurtSoundHooks != null || this.overrideGetHurtSoundHooks != null || this.afterGetHurtSoundHooks != null;

        this.beforeGetNameHooks = this.create(beforeGetNameHookTypes);
        this.overrideGetNameHooks = this.create(overrideGetNameHookTypes);
        this.afterGetNameHooks = this.create(afterGetNameHookTypes);
        this.isGetNameModded = this.beforeGetNameHooks != null || this.overrideGetNameHooks != null || this.afterGetNameHooks != null;

        this.beforeGetSizeHooks = this.create(beforeGetSizeHookTypes);
        this.overrideGetSizeHooks = this.create(overrideGetSizeHookTypes);
        this.afterGetSizeHooks = this.create(afterGetSizeHookTypes);
        this.isGetSizeModded = this.beforeGetSizeHooks != null || this.overrideGetSizeHooks != null || this.afterGetSizeHooks != null;

        this.beforeGetSleepTimerHooks = this.create(beforeGetSleepTimerHookTypes);
        this.overrideGetSleepTimerHooks = this.create(overrideGetSleepTimerHookTypes);
        this.afterGetSleepTimerHooks = this.create(afterGetSleepTimerHookTypes);
        this.isGetSleepTimerModded = this.beforeGetSleepTimerHooks != null || this.overrideGetSleepTimerHooks != null || this.afterGetSleepTimerHooks != null;

        this.beforeGetStandingEyeHeightHooks = this.create(beforeGetStandingEyeHeightHookTypes);
        this.overrideGetStandingEyeHeightHooks = this.create(overrideGetStandingEyeHeightHookTypes);
        this.afterGetStandingEyeHeightHooks = this.create(afterGetStandingEyeHeightHookTypes);
        this.isGetStandingEyeHeightModded = this.beforeGetStandingEyeHeightHooks != null || this.overrideGetStandingEyeHeightHooks != null || this.afterGetStandingEyeHeightHooks != null;

        this.beforeGiveExperiencePointsHooks = this.create(beforeGiveExperiencePointsHookTypes);
        this.overrideGiveExperiencePointsHooks = this.create(overrideGiveExperiencePointsHookTypes);
        this.afterGiveExperiencePointsHooks = this.create(afterGiveExperiencePointsHookTypes);
        this.isGiveExperiencePointsModded = this.beforeGiveExperiencePointsHooks != null || this.overrideGiveExperiencePointsHooks != null || this.afterGiveExperiencePointsHooks != null;

        this.beforeHandleWaterMovementHooks = this.create(beforeHandleWaterMovementHookTypes);
        this.overrideHandleWaterMovementHooks = this.create(overrideHandleWaterMovementHookTypes);
        this.afterHandleWaterMovementHooks = this.create(afterHandleWaterMovementHookTypes);
        this.isHandleWaterMovementModded = this.beforeHandleWaterMovementHooks != null || this.overrideHandleWaterMovementHooks != null || this.afterHandleWaterMovementHooks != null;

        this.beforeHealHooks = this.create(beforeHealHookTypes);
        this.overrideHealHooks = this.create(overrideHealHookTypes);
        this.afterHealHooks = this.create(afterHealHookTypes);
        this.isHealModded = this.beforeHealHooks != null || this.overrideHealHooks != null || this.afterHealHooks != null;

        this.beforeIsEntityInsideOpaqueBlockHooks = this.create(beforeIsEntityInsideOpaqueBlockHookTypes);
        this.overrideIsEntityInsideOpaqueBlockHooks = this.create(overrideIsEntityInsideOpaqueBlockHookTypes);
        this.afterIsEntityInsideOpaqueBlockHooks = this.create(afterIsEntityInsideOpaqueBlockHookTypes);
        this.isIsEntityInsideOpaqueBlockModded = this.beforeIsEntityInsideOpaqueBlockHooks != null || this.overrideIsEntityInsideOpaqueBlockHooks != null || this.afterIsEntityInsideOpaqueBlockHooks != null;

        this.beforeIsInWaterHooks = this.create(beforeIsInWaterHookTypes);
        this.overrideIsInWaterHooks = this.create(overrideIsInWaterHookTypes);
        this.afterIsInWaterHooks = this.create(afterIsInWaterHookTypes);
        this.isIsInWaterModded = this.beforeIsInWaterHooks != null || this.overrideIsInWaterHooks != null || this.afterIsInWaterHooks != null;

        this.beforeIsOnLadderHooks = this.create(beforeIsOnLadderHookTypes);
        this.overrideIsOnLadderHooks = this.create(overrideIsOnLadderHookTypes);
        this.afterIsOnLadderHooks = this.create(afterIsOnLadderHookTypes);
        this.isIsOnLadderModded = this.beforeIsOnLadderHooks != null || this.overrideIsOnLadderHooks != null || this.afterIsOnLadderHooks != null;

        this.beforeIsShiftKeyDownHooks = this.create(beforeIsShiftKeyDownHookTypes);
        this.overrideIsShiftKeyDownHooks = this.create(overrideIsShiftKeyDownHookTypes);
        this.afterIsShiftKeyDownHooks = this.create(afterIsShiftKeyDownHookTypes);
        this.isIsShiftKeyDownModded = this.beforeIsShiftKeyDownHooks != null || this.overrideIsShiftKeyDownHooks != null || this.afterIsShiftKeyDownHooks != null;

        this.beforeIsSleepingHooks = this.create(beforeIsSleepingHookTypes);
        this.overrideIsSleepingHooks = this.create(overrideIsSleepingHookTypes);
        this.afterIsSleepingHooks = this.create(afterIsSleepingHookTypes);
        this.isIsSleepingModded = this.beforeIsSleepingHooks != null || this.overrideIsSleepingHooks != null || this.afterIsSleepingHooks != null;

        this.beforeIsSprintingHooks = this.create(beforeIsSprintingHookTypes);
        this.overrideIsSprintingHooks = this.create(overrideIsSprintingHookTypes);
        this.afterIsSprintingHooks = this.create(afterIsSprintingHookTypes);
        this.isIsSprintingModded = this.beforeIsSprintingHooks != null || this.overrideIsSprintingHooks != null || this.afterIsSprintingHooks != null;

        this.beforeJumpHooks = this.create(beforeJumpHookTypes);
        this.overrideJumpHooks = this.create(overrideJumpHookTypes);
        this.afterJumpHooks = this.create(afterJumpHookTypes);
        this.isJumpModded = this.beforeJumpHooks != null || this.overrideJumpHooks != null || this.afterJumpHooks != null;

        this.beforeKnockBackHooks = this.create(beforeKnockBackHookTypes);
        this.overrideKnockBackHooks = this.create(overrideKnockBackHookTypes);
        this.afterKnockBackHooks = this.create(afterKnockBackHookTypes);
        this.isKnockBackModded = this.beforeKnockBackHooks != null || this.overrideKnockBackHooks != null || this.afterKnockBackHooks != null;

        this.beforeLivingTickHooks = this.create(beforeLivingTickHookTypes);
        this.overrideLivingTickHooks = this.create(overrideLivingTickHookTypes);
        this.afterLivingTickHooks = this.create(afterLivingTickHookTypes);
        this.isLivingTickModded = this.beforeLivingTickHooks != null || this.overrideLivingTickHooks != null || this.afterLivingTickHooks != null;

        this.beforeMoveHooks = this.create(beforeMoveHookTypes);
        this.overrideMoveHooks = this.create(overrideMoveHookTypes);
        this.afterMoveHooks = this.create(afterMoveHookTypes);
        this.isMoveModded = this.beforeMoveHooks != null || this.overrideMoveHooks != null || this.afterMoveHooks != null;

        this.beforeMoveRelativeHooks = this.create(beforeMoveRelativeHookTypes);
        this.overrideMoveRelativeHooks = this.create(overrideMoveRelativeHookTypes);
        this.afterMoveRelativeHooks = this.create(afterMoveRelativeHookTypes);
        this.isMoveRelativeModded = this.beforeMoveRelativeHooks != null || this.overrideMoveRelativeHooks != null || this.afterMoveRelativeHooks != null;

        this.beforeOnDeathHooks = this.create(beforeOnDeathHookTypes);
        this.overrideOnDeathHooks = this.create(overrideOnDeathHookTypes);
        this.afterOnDeathHooks = this.create(afterOnDeathHookTypes);
        this.isOnDeathModded = this.beforeOnDeathHooks != null || this.overrideOnDeathHooks != null || this.afterOnDeathHooks != null;

        this.beforeOnKillEntityHooks = this.create(beforeOnKillEntityHookTypes);
        this.overrideOnKillEntityHooks = this.create(overrideOnKillEntityHookTypes);
        this.afterOnKillEntityHooks = this.create(afterOnKillEntityHookTypes);
        this.isOnKillEntityModded = this.beforeOnKillEntityHooks != null || this.overrideOnKillEntityHooks != null || this.afterOnKillEntityHooks != null;

        this.beforeOnLivingFallHooks = this.create(beforeOnLivingFallHookTypes);
        this.overrideOnLivingFallHooks = this.create(overrideOnLivingFallHookTypes);
        this.afterOnLivingFallHooks = this.create(afterOnLivingFallHookTypes);
        this.isOnLivingFallModded = this.beforeOnLivingFallHooks != null || this.overrideOnLivingFallHooks != null || this.afterOnLivingFallHooks != null;

        this.beforeOnStruckByLightningHooks = this.create(beforeOnStruckByLightningHookTypes);
        this.overrideOnStruckByLightningHooks = this.create(overrideOnStruckByLightningHookTypes);
        this.afterOnStruckByLightningHooks = this.create(afterOnStruckByLightningHookTypes);
        this.isOnStruckByLightningModded = this.beforeOnStruckByLightningHooks != null || this.overrideOnStruckByLightningHooks != null || this.afterOnStruckByLightningHooks != null;

        this.beforePickHooks = this.create(beforePickHookTypes);
        this.overridePickHooks = this.create(overridePickHookTypes);
        this.afterPickHooks = this.create(afterPickHookTypes);
        this.isPickModded = this.beforePickHooks != null || this.overridePickHooks != null || this.afterPickHooks != null;

        this.beforePlayerTickHooks = this.create(beforePlayerTickHookTypes);
        this.overridePlayerTickHooks = this.create(overridePlayerTickHookTypes);
        this.afterPlayerTickHooks = this.create(afterPlayerTickHookTypes);
        this.isPlayerTickModded = this.beforePlayerTickHooks != null || this.overridePlayerTickHooks != null || this.afterPlayerTickHooks != null;

        this.beforePlayStepSoundHooks = this.create(beforePlayStepSoundHookTypes);
        this.overridePlayStepSoundHooks = this.create(overridePlayStepSoundHookTypes);
        this.afterPlayStepSoundHooks = this.create(afterPlayStepSoundHookTypes);
        this.isPlayStepSoundModded = this.beforePlayStepSoundHooks != null || this.overridePlayStepSoundHooks != null || this.afterPlayStepSoundHooks != null;

        this.beforePushOutOfBlocksHooks = this.create(beforePushOutOfBlocksHookTypes);
        this.overridePushOutOfBlocksHooks = this.create(overridePushOutOfBlocksHookTypes);
        this.afterPushOutOfBlocksHooks = this.create(afterPushOutOfBlocksHookTypes);
        this.isPushOutOfBlocksModded = this.beforePushOutOfBlocksHooks != null || this.overridePushOutOfBlocksHooks != null || this.afterPushOutOfBlocksHooks != null;

        this.beforeReadHooks = this.create(beforeReadHookTypes);
        this.overrideReadHooks = this.create(overrideReadHookTypes);
        this.afterReadHooks = this.create(afterReadHookTypes);
        this.isReadModded = this.beforeReadHooks != null || this.overrideReadHooks != null || this.afterReadHooks != null;

        this.beforeRemoveHooks = this.create(beforeRemoveHookTypes);
        this.overrideRemoveHooks = this.create(overrideRemoveHookTypes);
        this.afterRemoveHooks = this.create(afterRemoveHookTypes);
        this.isRemoveModded = this.beforeRemoveHooks != null || this.overrideRemoveHooks != null || this.afterRemoveHooks != null;

        this.beforeSetEntityActionStateHooks = this.create(beforeSetEntityActionStateHookTypes);
        this.overrideSetEntityActionStateHooks = this.create(overrideSetEntityActionStateHookTypes);
        this.afterSetEntityActionStateHooks = this.create(afterSetEntityActionStateHookTypes);
        this.isSetEntityActionStateModded = this.beforeSetEntityActionStateHooks != null || this.overrideSetEntityActionStateHooks != null || this.afterSetEntityActionStateHooks != null;

        this.beforeSetPositionHooks = this.create(beforeSetPositionHookTypes);
        this.overrideSetPositionHooks = this.create(overrideSetPositionHookTypes);
        this.afterSetPositionHooks = this.create(afterSetPositionHookTypes);
        this.isSetPositionModded = this.beforeSetPositionHooks != null || this.overrideSetPositionHooks != null || this.afterSetPositionHooks != null;

        this.beforeSetPositionAndRotationHooks = this.create(beforeSetPositionAndRotationHookTypes);
        this.overrideSetPositionAndRotationHooks = this.create(overrideSetPositionAndRotationHookTypes);
        this.afterSetPositionAndRotationHooks = this.create(afterSetPositionAndRotationHookTypes);
        this.isSetPositionAndRotationModded = this.beforeSetPositionAndRotationHooks != null || this.overrideSetPositionAndRotationHooks != null || this.afterSetPositionAndRotationHooks != null;

        this.beforeSetSneakingHooks = this.create(beforeSetSneakingHookTypes);
        this.overrideSetSneakingHooks = this.create(overrideSetSneakingHookTypes);
        this.afterSetSneakingHooks = this.create(afterSetSneakingHookTypes);
        this.isSetSneakingModded = this.beforeSetSneakingHooks != null || this.overrideSetSneakingHooks != null || this.afterSetSneakingHooks != null;

        this.beforeSetSprintingHooks = this.create(beforeSetSprintingHookTypes);
        this.overrideSetSprintingHooks = this.create(overrideSetSprintingHookTypes);
        this.afterSetSprintingHooks = this.create(afterSetSprintingHookTypes);
        this.isSetSprintingModded = this.beforeSetSprintingHooks != null || this.overrideSetSprintingHooks != null || this.afterSetSprintingHooks != null;

        this.beforeStartRidingHooks = this.create(beforeStartRidingHookTypes);
        this.overrideStartRidingHooks = this.create(overrideStartRidingHookTypes);
        this.afterStartRidingHooks = this.create(afterStartRidingHookTypes);
        this.isStartRidingModded = this.beforeStartRidingHooks != null || this.overrideStartRidingHooks != null || this.afterStartRidingHooks != null;

        this.beforeSwingArmHooks = this.create(beforeSwingArmHookTypes);
        this.overrideSwingArmHooks = this.create(overrideSwingArmHookTypes);
        this.afterSwingArmHooks = this.create(afterSwingArmHookTypes);
        this.isSwingArmModded = this.beforeSwingArmHooks != null || this.overrideSwingArmHooks != null || this.afterSwingArmHooks != null;

        this.beforeTickHooks = this.create(beforeTickHookTypes);
        this.overrideTickHooks = this.create(overrideTickHookTypes);
        this.afterTickHooks = this.create(afterTickHookTypes);
        this.isTickModded = this.beforeTickHooks != null || this.overrideTickHooks != null || this.afterTickHooks != null;

        this.beforeTravelHooks = this.create(beforeTravelHookTypes);
        this.overrideTravelHooks = this.create(overrideTravelHookTypes);
        this.afterTravelHooks = this.create(afterTravelHookTypes);
        this.isTravelModded = this.beforeTravelHooks != null || this.overrideTravelHooks != null || this.afterTravelHooks != null;

        this.beforeTrySleepHooks = this.create(beforeTrySleepHookTypes);
        this.overrideTrySleepHooks = this.create(overrideTrySleepHookTypes);
        this.afterTrySleepHooks = this.create(afterTrySleepHookTypes);
        this.isTrySleepModded = this.beforeTrySleepHooks != null || this.overrideTrySleepHooks != null || this.afterTrySleepHooks != null;

        this.beforeUpdateEntityActionStateHooks = this.create(beforeUpdateEntityActionStateHookTypes);
        this.overrideUpdateEntityActionStateHooks = this.create(overrideUpdateEntityActionStateHookTypes);
        this.afterUpdateEntityActionStateHooks = this.create(afterUpdateEntityActionStateHookTypes);
        this.isUpdateEntityActionStateModded = this.beforeUpdateEntityActionStateHooks != null || this.overrideUpdateEntityActionStateHooks != null || this.afterUpdateEntityActionStateHooks != null;

        this.beforeUpdatePotionEffectsHooks = this.create(beforeUpdatePotionEffectsHookTypes);
        this.overrideUpdatePotionEffectsHooks = this.create(overrideUpdatePotionEffectsHookTypes);
        this.afterUpdatePotionEffectsHooks = this.create(afterUpdatePotionEffectsHookTypes);
        this.isUpdatePotionEffectsModded = this.beforeUpdatePotionEffectsHooks != null || this.overrideUpdatePotionEffectsHooks != null || this.afterUpdatePotionEffectsHooks != null;

        this.beforeUpdateRiddenHooks = this.create(beforeUpdateRiddenHookTypes);
        this.overrideUpdateRiddenHooks = this.create(overrideUpdateRiddenHookTypes);
        this.afterUpdateRiddenHooks = this.create(afterUpdateRiddenHookTypes);
        this.isUpdateRiddenModded = this.beforeUpdateRiddenHooks != null || this.overrideUpdateRiddenHooks != null || this.afterUpdateRiddenHooks != null;

        this.beforeWakeUpPlayerHooks = this.create(beforeWakeUpPlayerHookTypes);
        this.overrideWakeUpPlayerHooks = this.create(overrideWakeUpPlayerHookTypes);
        this.afterWakeUpPlayerHooks = this.create(afterWakeUpPlayerHookTypes);
        this.isWakeUpPlayerModded = this.beforeWakeUpPlayerHooks != null || this.overrideWakeUpPlayerHooks != null || this.afterWakeUpPlayerHooks != null;

        this.beforeWriteWithoutTypeIdHooks = this.create(beforeWriteWithoutTypeIdHookTypes);
        this.overrideWriteWithoutTypeIdHooks = this.create(overrideWriteWithoutTypeIdHookTypes);
        this.afterWriteWithoutTypeIdHooks = this.create(afterWriteWithoutTypeIdHookTypes);
        this.isWriteWithoutTypeIdModded = this.beforeWriteWithoutTypeIdHooks != null || this.overrideWriteWithoutTypeIdHooks != null || this.afterWriteWithoutTypeIdHooks != null;
    }

    private void attachServerPlayerBase(String id)
    {
        ServerPlayerEntityBase toAttach = this.createServerPlayerBase(id);
        toAttach.beforeBaseAttach(true);
        this.allBaseObjects.put(id, toAttach);
        this.updateServerPlayerBases();
        toAttach.afterBaseAttach(true);
    }

    private void detachServerPlayerBase(String id)
    {
        ServerPlayerEntityBase toDetach = this.allBaseObjects.get(id);
        toDetach.beforeBaseDetach(true);
        this.allBaseObjects.remove(id);
        toDetach.afterBaseDetach(true);
    }

    private ServerPlayerEntityBase[] create(List<String> types)
    {
        if (types.isEmpty()) {
            return null;
        }

        ServerPlayerEntityBase[] result = new ServerPlayerEntityBase[types.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.getServerPlayerBase(types.get(i));
        }
        return result;
    }

    private void beforeLocalConstructing(MinecraftServer paramMinecraftServer, ServerWorld paramWorldServer, GameProfile paramGameProfile, PlayerInteractionManager paramPlayerInteractionManager)
    {
        if (this.beforeLocalConstructingHooks != null) {
            for (int i = this.beforeLocalConstructingHooks.length - 1; i >= 0; i--) {
                this.beforeLocalConstructingHooks[i].beforeLocalConstructing(paramMinecraftServer, paramWorldServer, paramGameProfile, paramPlayerInteractionManager);
            }
        }
        this.beforeLocalConstructingHooks = null;
    }

    private void afterLocalConstructing(MinecraftServer paramMinecraftServer, ServerWorld paramWorldServer, GameProfile paramGameProfile, PlayerInteractionManager paramPlayerInteractionManager)
    {
        if (this.afterLocalConstructingHooks != null) {
            for (ServerPlayerEntityBase afterLocalConstructingHook : this.afterLocalConstructingHooks) {
                afterLocalConstructingHook.afterLocalConstructing(paramMinecraftServer, paramWorldServer, paramGameProfile, paramPlayerInteractionManager);
            }
        }
        this.afterLocalConstructingHooks = null;
    }

    public ServerPlayerEntityBase getServerPlayerBase(String id)
    {
        return this.allBaseObjects.get(id);
    }

    public Set<String> getServerPlayerBaseIds()
    {
        return this.unmodifiableAllBaseIds;
    }

    public Object dynamic(String key, Object[] parameters)
    {
        key = key.replace('.', '_').replace(' ', '_');
        this.executeAll(key, parameters, beforeDynamicHookTypes, beforeDynamicHookMethods, true);
        Object result = this.dynamicOverwritten(key, parameters, null);
        this.executeAll(key, parameters, afterDynamicHookTypes, afterDynamicHookMethods, false);
        return result;
    }

    public Object dynamicOverwritten(String key, Object[] parameters, ServerPlayerEntityBase overwriter)
    {
        List<String> overrideIds = overrideDynamicHookTypes.get(key);

        String id = null;
        if (overrideIds != null) {
            if (overwriter != null) {
                id = this.baseObjectsToId.get(overwriter);
                int index = overrideIds.indexOf(id);
                if (index > 0) {
                    id = overrideIds.get(index - 1);
                } else {
                    id = null;
                }
            } else if (overrideIds.size() > 0) {
                id = overrideIds.get(overrideIds.size() - 1);
            }
        }

        Map<Class<?>, Map<String, Method>> methodMap;

        if (id == null) {
            id = keysToVirtualIds.get(key);
            if (id == null) {
                return null;
            }
            methodMap = virtualDynamicHookMethods;
        } else {
            methodMap = overrideDynamicHookMethods;
        }

        Map<String, Method> methods = methodMap.get(allBaseConstructors.get(id).getDeclaringClass());
        if (methods == null) {
            return null;
        }

        Method method = methods.get(key);
        if (method == null) {
            return null;
        }

        return this.execute(this.getServerPlayerBase(id), method, parameters);
    }

    private void executeAll(String key, Object[] parameters, Map<String, List<String>> dynamicHookTypes, Map<Class<?>, Map<String, Method>> dynamicHookMethods, boolean reverse)
    {
        List<String> beforeIds = dynamicHookTypes.get(key);
        if (beforeIds == null) {
            return;
        }

        for (int i = reverse ? beforeIds.size() - 1 : 0; reverse ? i >= 0 : i < beforeIds.size(); i = i + (reverse ? -1 : 1)) {
            String id = beforeIds.get(i);
            ServerPlayerEntityBase base = this.getServerPlayerBase(id);
            Class<?> type = base.getClass();

            Map<String, Method> methods = dynamicHookMethods.get(type);
            if (methods == null) {
                continue;
            }

            Method method = methods.get(key);
            if (method == null) {
                continue;
            }

            this.execute(base, method, parameters);
        }
    }

    private Object execute(ServerPlayerEntityBase base, Method method, Object[] parameters)
    {
        try {
            return method.invoke(base, parameters);
        } catch (Exception e) {
            throw new RuntimeException("Exception while invoking dynamic method", e);
        }
    }

    // ############################################################################

    public static void addExhaustion(IServerPlayerEntity target, float exhaustion)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isAddExhaustionModded) {
            serverPlayerAPI.addExhaustion(exhaustion);
        } else {
            target.superAddExhaustion(exhaustion);
        }
    }

    private void addExhaustion(float exhaustion)
    {
        if (this.beforeAddExhaustionHooks != null) {
            for (int i = this.beforeAddExhaustionHooks.length - 1; i >= 0; i--) {
                this.beforeAddExhaustionHooks[i].beforeAddExhaustion(exhaustion);
            }
        }

        if (this.overrideAddExhaustionHooks != null) {
            this.overrideAddExhaustionHooks[this.overrideAddExhaustionHooks.length - 1].addExhaustion(exhaustion);
        } else {
            this.player.superAddExhaustion(exhaustion);
        }

        if (this.afterAddExhaustionHooks != null) {
            for (ServerPlayerEntityBase afterAddExhaustionHook : this.afterAddExhaustionHooks) {
                afterAddExhaustionHook.afterAddExhaustion(exhaustion);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenAddExhaustion(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideAddExhaustionHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideAddExhaustionHooks.length; i++) {
            if (this.overrideAddExhaustionHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideAddExhaustionHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeAddExhaustionHookTypes = new LinkedList<>();
    private final static List<String> overrideAddExhaustionHookTypes = new LinkedList<>();
    private final static List<String> afterAddExhaustionHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeAddExhaustionHooks;
    private ServerPlayerEntityBase[] overrideAddExhaustionHooks;
    private ServerPlayerEntityBase[] afterAddExhaustionHooks;
    public boolean isAddExhaustionModded;
    private static final Map<String, String[]> allBaseBeforeAddExhaustionSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeAddExhaustionInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideAddExhaustionSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideAddExhaustionInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterAddExhaustionSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterAddExhaustionInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforeAddExperienceLevel(CallbackInfo callbackInfo, IServerPlayerEntity target, int levels)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isAddExperienceLevelModded) {
            serverPlayerAPI.beforeAddExperienceLevel(callbackInfo, levels);
        }
    }

    private void beforeAddExperienceLevel(CallbackInfo callbackInfo, int levels)
    {
        if (this.beforeAddExperienceLevelHooks != null) {
            for (int i = this.beforeAddExperienceLevelHooks.length - 1; i >= 0; i--) {
                this.beforeAddExperienceLevelHooks[i].beforeAddExperienceLevel(levels);
            }
        }

        if (this.overrideAddExperienceLevelHooks != null) {
            this.overrideAddExperienceLevelHooks[this.overrideAddExperienceLevelHooks.length - 1].addExperienceLevel(levels);
            callbackInfo.cancel();
        }
    }

    public static void afterAddExperienceLevel(IServerPlayerEntity target, int levels)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isAddExperienceLevelModded) {
            serverPlayerAPI.afterAddExperienceLevel(levels);
        }
    }

    private void afterAddExperienceLevel(int levels)
    {
        if (this.afterAddExhaustionHooks != null) {
            for (ServerPlayerEntityBase afterAddExperienceLevelHook : this.afterAddExperienceLevelHooks) {
                afterAddExperienceLevelHook.afterAddExperienceLevel(levels);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenAddExperienceLevel(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideAddExperienceLevelHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideAddExperienceLevelHooks.length; i++) {
            if (this.overrideAddExperienceLevelHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideAddExperienceLevelHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeAddExperienceLevelHookTypes = new LinkedList<>();
    private final static List<String> overrideAddExperienceLevelHookTypes = new LinkedList<>();
    private final static List<String> afterAddExperienceLevelHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeAddExperienceLevelHooks;
    private ServerPlayerEntityBase[] overrideAddExperienceLevelHooks;
    private ServerPlayerEntityBase[] afterAddExperienceLevelHooks;
    public boolean isAddExperienceLevelModded;
    private static final Map<String, String[]> allBaseBeforeAddExperienceLevelSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeAddExperienceLevelInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideAddExperienceLevelSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideAddExperienceLevelInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterAddExperienceLevelSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterAddExperienceLevelInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void addMovementStat(IServerPlayerEntity target, double x, double y, double z)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isAddMovementStatModded) {
            serverPlayerAPI.addMovementStat(x, y, z);
        } else {
            target.superAddMovementStat(x, y, z);
        }
    }

    private void addMovementStat(double x, double y, double z)
    {
        if (this.beforeAddMovementStatHooks != null) {
            for (int i = this.beforeAddMovementStatHooks.length - 1; i >= 0; i--) {
                this.beforeAddMovementStatHooks[i].beforeAddMovementStat(x, y, z);
            }
        }

        if (this.overrideAddMovementStatHooks != null) {
            this.overrideAddMovementStatHooks[this.overrideAddMovementStatHooks.length - 1].addMovementStat(x, y, z);
        } else {
            this.player.superAddMovementStat(x, y, z);
        }

        if (this.afterAddMovementStatHooks != null) {
            for (ServerPlayerEntityBase afterAddMovementStatHook : this.afterAddMovementStatHooks) {
                afterAddMovementStatHook.afterAddMovementStat(x, y, z);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenAddMovementStat(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideAddMovementStatHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideAddMovementStatHooks.length; i++) {
            if (this.overrideAddMovementStatHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideAddMovementStatHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeAddMovementStatHookTypes = new LinkedList<>();
    private final static List<String> overrideAddMovementStatHookTypes = new LinkedList<>();
    private final static List<String> afterAddMovementStatHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeAddMovementStatHooks;
    private ServerPlayerEntityBase[] overrideAddMovementStatHooks;
    private ServerPlayerEntityBase[] afterAddMovementStatHooks;
    public boolean isAddMovementStatModded;
    private static final Map<String, String[]> allBaseBeforeAddMovementStatSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeAddMovementStatInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideAddMovementStatSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideAddMovementStatInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterAddMovementStatSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterAddMovementStatInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforeAddStat(CallbackInfo callbackInfo, IServerPlayerEntity target, Stat<?> stat, int amount)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isAddStatModded) {
            serverPlayerAPI.beforeAddStat(callbackInfo, stat, amount);
        }
    }

    private void beforeAddStat(CallbackInfo callbackInfo, Stat<?> stat, int amount)
    {
        if (this.beforeAddStatHooks != null) {
            for (int i = this.beforeAddStatHooks.length - 1; i >= 0; i--) {
                this.beforeAddStatHooks[i].beforeAddStat(stat, amount);
            }
        }

        if (this.overrideAddStatHooks != null) {
            this.overrideAddStatHooks[this.overrideAddStatHooks.length - 1].addStat(stat, amount);
            callbackInfo.cancel();
        }
    }

    public static void afterAddStat(IServerPlayerEntity target, Stat<?> stat, int amount)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isAddStatModded) {
            serverPlayerAPI.afterAddStat(stat, amount);
        }
    }

    private void afterAddStat(Stat<?> stat, int amount)
    {
        if (this.afterAddStatHooks != null) {
            for (ServerPlayerEntityBase afterAddStatHook : this.afterAddStatHooks) {
                afterAddStatHook.afterAddStat(stat, amount);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenAddStat(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideAddStatHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideAddStatHooks.length; i++) {
            if (this.overrideAddStatHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideAddStatHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeAddStatHookTypes = new LinkedList<>();
    private final static List<String> overrideAddStatHookTypes = new LinkedList<>();
    private final static List<String> afterAddStatHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeAddStatHooks;
    private ServerPlayerEntityBase[] overrideAddStatHooks;
    private ServerPlayerEntityBase[] afterAddStatHooks;
    public boolean isAddStatModded;
    private static final Map<String, String[]> allBaseBeforeAddStatSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeAddStatInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideAddStatSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideAddStatInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterAddStatSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterAddStatInferiors = new Hashtable<>(0);

    // ############################################################################

    public static boolean areEyesInFluid(IServerPlayerEntity target, Tag<Fluid> fluid, boolean checkChunkLoaded)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isAreEyesInFluidModded) {
            return serverPlayerAPI.areEyesInFluid(fluid, checkChunkLoaded);
        } else {
            return target.superAreEyesInFluid(fluid, checkChunkLoaded);
        }
    }

    private boolean areEyesInFluid(Tag<Fluid> fluid, boolean checkChunkLoaded)
    {
        if (this.beforeAreEyesInFluidHooks != null) {
            for (int i = this.beforeAreEyesInFluidHooks.length - 1; i >= 0; i--) {
                this.beforeAreEyesInFluidHooks[i].beforeAreEyesInFluid(fluid, checkChunkLoaded);
            }
        }

        boolean result;
        if (this.overrideAreEyesInFluidHooks != null) {
            result = this.overrideAreEyesInFluidHooks[this.overrideAreEyesInFluidHooks.length - 1].areEyesInFluid(fluid, checkChunkLoaded);
        } else {
            result = this.player.superAreEyesInFluid(fluid, checkChunkLoaded);
        }

        if (this.afterAreEyesInFluidHooks != null) {
            for (ServerPlayerEntityBase afterAreEyesInFluidHook : this.afterAreEyesInFluidHooks) {
                afterAreEyesInFluidHook.afterAreEyesInFluid(fluid, checkChunkLoaded);
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenAreEyesInFluid(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideAreEyesInFluidHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideAreEyesInFluidHooks.length; i++) {
            if (this.overrideAreEyesInFluidHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideAreEyesInFluidHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeAreEyesInFluidHookTypes = new LinkedList<>();
    private final static List<String> overrideAreEyesInFluidHookTypes = new LinkedList<>();
    private final static List<String> afterAreEyesInFluidHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeAreEyesInFluidHooks;
    private ServerPlayerEntityBase[] overrideAreEyesInFluidHooks;
    private ServerPlayerEntityBase[] afterAreEyesInFluidHooks;
    public boolean isAreEyesInFluidModded;
    private static final Map<String, String[]> allBaseBeforeAreEyesInFluidSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeAreEyesInFluidInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideAreEyesInFluidSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideAreEyesInFluidInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterAreEyesInFluidSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterAreEyesInFluidInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforeAttackEntityFrom(CallbackInfoReturnable<Boolean> callbackInfo, IServerPlayerEntity target, DamageSource source, float amount)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isAttackEntityFromModded) {
            serverPlayerAPI.beforeAttackEntityFrom(callbackInfo, source, amount);
        }
    }

    private void beforeAttackEntityFrom(CallbackInfoReturnable<Boolean> callbackInfo, DamageSource source, float amount)
    {
        if (this.beforeAttackEntityFromHooks != null) {
            for (int i = this.beforeAttackEntityFromHooks.length - 1; i >= 0; i--) {
                this.beforeAttackEntityFromHooks[i].beforeAttackEntityFrom(source, amount);
            }
        }

        if (this.overrideAttackEntityFromHooks != null) {
            callbackInfo.setReturnValue(this.overrideAttackEntityFromHooks[this.overrideAttackEntityFromHooks.length - 1].attackEntityFrom(source, amount));
            callbackInfo.cancel();
        }
    }

    public static void afterAttackEntityFrom(IServerPlayerEntity target, DamageSource source, float amount)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isAttackEntityFromModded) {
            serverPlayerAPI.afterAttackEntityFrom(source, amount);
        }
    }

    private void afterAttackEntityFrom(DamageSource source, float amount)
    {
        if (this.afterAttackEntityFromHooks != null) {
            for (ServerPlayerEntityBase afterAttackEntityFromHook : this.afterAttackEntityFromHooks) {
                afterAttackEntityFromHook.afterAttackEntityFrom(source, amount);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenAttackEntityFrom(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideAttackEntityFromHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideAttackEntityFromHooks.length; i++) {
            if (this.overrideAttackEntityFromHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideAttackEntityFromHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeAttackEntityFromHookTypes = new LinkedList<>();
    private final static List<String> overrideAttackEntityFromHookTypes = new LinkedList<>();
    private final static List<String> afterAttackEntityFromHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeAttackEntityFromHooks;
    private ServerPlayerEntityBase[] overrideAttackEntityFromHooks;
    private ServerPlayerEntityBase[] afterAttackEntityFromHooks;
    public boolean isAttackEntityFromModded;
    private static final Map<String, String[]> allBaseBeforeAttackEntityFromSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeAttackEntityFromInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideAttackEntityFromSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideAttackEntityFromInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterAttackEntityFromSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterAttackEntityFromInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforeAttackTargetEntityWithCurrentItem(CallbackInfo callbackInfo, IServerPlayerEntity target, Entity targetEntity)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isAttackTargetEntityWithCurrentItemModded) {
            serverPlayerAPI.beforeAttackTargetEntityWithCurrentItem(callbackInfo, targetEntity);
        }
    }

    private void beforeAttackTargetEntityWithCurrentItem(CallbackInfo callbackInfo, Entity targetEntity)
    {
        if (this.beforeAttackTargetEntityWithCurrentItemHooks != null) {
            for (int i = this.beforeAttackTargetEntityWithCurrentItemHooks.length - 1; i >= 0; i--) {
                this.beforeAttackTargetEntityWithCurrentItemHooks[i].beforeAttackTargetEntityWithCurrentItem(targetEntity);
            }
        }

        if (this.overrideAttackTargetEntityWithCurrentItemHooks != null) {
            this.overrideAttackTargetEntityWithCurrentItemHooks[this.overrideAttackTargetEntityWithCurrentItemHooks.length - 1].attackTargetEntityWithCurrentItem(targetEntity);
            callbackInfo.cancel();
        }
    }

    public static void afterAttackTargetEntityWithCurrentItem(IServerPlayerEntity target, Entity targetEntity)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isAttackTargetEntityWithCurrentItemModded) {
            serverPlayerAPI.afterAttackTargetEntityWithCurrentItem(targetEntity);
        }
    }

    private void afterAttackTargetEntityWithCurrentItem(Entity targetEntity)
    {
        if (this.afterAttackTargetEntityWithCurrentItemHooks != null) {
            for (ServerPlayerEntityBase afterAttackTargetEntityWithCurrentItemHook : this.afterAttackTargetEntityWithCurrentItemHooks) {
                afterAttackTargetEntityWithCurrentItemHook.afterAttackTargetEntityWithCurrentItem(targetEntity);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenAttackTargetEntityWithCurrentItem(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideAttackTargetEntityWithCurrentItemHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideAttackTargetEntityWithCurrentItemHooks.length; i++) {
            if (this.overrideAttackTargetEntityWithCurrentItemHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideAttackTargetEntityWithCurrentItemHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeAttackTargetEntityWithCurrentItemHookTypes = new LinkedList<>();
    private final static List<String> overrideAttackTargetEntityWithCurrentItemHookTypes = new LinkedList<>();
    private final static List<String> afterAttackTargetEntityWithCurrentItemHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeAttackTargetEntityWithCurrentItemHooks;
    private ServerPlayerEntityBase[] overrideAttackTargetEntityWithCurrentItemHooks;
    private ServerPlayerEntityBase[] afterAttackTargetEntityWithCurrentItemHooks;
    public boolean isAttackTargetEntityWithCurrentItemModded;
    private static final Map<String, String[]> allBaseBeforeAttackTargetEntityWithCurrentItemSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeAttackTargetEntityWithCurrentItemInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideAttackTargetEntityWithCurrentItemSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideAttackTargetEntityWithCurrentItemInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterAttackTargetEntityWithCurrentItemSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterAttackTargetEntityWithCurrentItemInferiors = new Hashtable<>(0);

    // ############################################################################

    public static boolean canBreatheUnderwater(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isCanBreatheUnderwaterModded) {
            return serverPlayerAPI.canBreatheUnderwater();
        } else {
            return target.superCanBreatheUnderwater();
        }
    }

    private boolean canBreatheUnderwater()
    {
        if (this.beforeCanBreatheUnderwaterHooks != null) {
            for (int i = this.beforeCanBreatheUnderwaterHooks.length - 1; i >= 0; i--) {
                this.beforeCanBreatheUnderwaterHooks[i].beforeCanBreatheUnderwater();
            }
        }

        boolean result;
        if (this.overrideCanBreatheUnderwaterHooks != null) {
            result = this.overrideCanBreatheUnderwaterHooks[this.overrideCanBreatheUnderwaterHooks.length - 1].canBreatheUnderwater();
        } else {
            result = this.player.superCanBreatheUnderwater();
        }

        if (this.afterCanBreatheUnderwaterHooks != null) {
            for (ServerPlayerEntityBase afterCanBreatheUnderwaterHook : this.afterCanBreatheUnderwaterHooks) {
                afterCanBreatheUnderwaterHook.afterCanBreatheUnderwater();
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenCanBreatheUnderwater(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideCanBreatheUnderwaterHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideCanBreatheUnderwaterHooks.length; i++) {
            if (this.overrideCanBreatheUnderwaterHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideCanBreatheUnderwaterHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeCanBreatheUnderwaterHookTypes = new LinkedList<>();
    private final static List<String> overrideCanBreatheUnderwaterHookTypes = new LinkedList<>();
    private final static List<String> afterCanBreatheUnderwaterHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeCanBreatheUnderwaterHooks;
    private ServerPlayerEntityBase[] overrideCanBreatheUnderwaterHooks;
    private ServerPlayerEntityBase[] afterCanBreatheUnderwaterHooks;
    public boolean isCanBreatheUnderwaterModded;
    private static final Map<String, String[]> allBaseBeforeCanBreatheUnderwaterSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeCanBreatheUnderwaterInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideCanBreatheUnderwaterSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideCanBreatheUnderwaterInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterCanBreatheUnderwaterSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterCanBreatheUnderwaterInferiors = new Hashtable<>(0);

    // ############################################################################

    public static boolean canHarvestBlock(IServerPlayerEntity target, BlockState state)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isCanHarvestBlockModded) {
            return serverPlayerAPI.canHarvestBlock(state);
        } else {
            return target.superCanHarvestBlock(state);
        }
    }

    private boolean canHarvestBlock(BlockState state)
    {
        if (this.beforeCanHarvestBlockHooks != null) {
            for (int i = this.beforeCanHarvestBlockHooks.length - 1; i >= 0; i--) {
                this.beforeCanHarvestBlockHooks[i].beforeCanHarvestBlock(state);
            }
        }

        boolean result;
        if (this.overrideCanHarvestBlockHooks != null) {
            result = this.overrideCanHarvestBlockHooks[this.overrideCanHarvestBlockHooks.length - 1].canHarvestBlock(state);
        } else {
            result = this.player.superCanHarvestBlock(state);
        }

        if (this.afterCanHarvestBlockHooks != null) {
            for (ServerPlayerEntityBase afterCanHarvestBlockHook : this.afterCanHarvestBlockHooks) {
                afterCanHarvestBlockHook.afterCanHarvestBlock(state);
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenCanHarvestBlock(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideCanHarvestBlockHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideCanHarvestBlockHooks.length; i++) {
            if (this.overrideCanHarvestBlockHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideCanHarvestBlockHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeCanHarvestBlockHookTypes = new LinkedList<>();
    private final static List<String> overrideCanHarvestBlockHookTypes = new LinkedList<>();
    private final static List<String> afterCanHarvestBlockHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeCanHarvestBlockHooks;
    private ServerPlayerEntityBase[] overrideCanHarvestBlockHooks;
    private ServerPlayerEntityBase[] afterCanHarvestBlockHooks;
    public boolean isCanHarvestBlockModded;
    private static final Map<String, String[]> allBaseBeforeCanHarvestBlockSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeCanHarvestBlockInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideCanHarvestBlockSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideCanHarvestBlockInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterCanHarvestBlockSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterCanHarvestBlockInferiors = new Hashtable<>(0);

    // ############################################################################

    public static boolean canPlayerEdit(IServerPlayerEntity target, BlockPos pos, Direction facing, ItemStack stack)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isCanPlayerEditModded) {
            return serverPlayerAPI.canPlayerEdit(pos, facing, stack);
        } else {
            return target.superCanPlayerEdit(pos, facing, stack);
        }
    }

    private boolean canPlayerEdit(BlockPos pos, Direction facing, ItemStack stack)
    {
        if (this.beforeCanPlayerEditHooks != null) {
            for (int i = this.beforeCanPlayerEditHooks.length - 1; i >= 0; i--) {
                this.beforeCanPlayerEditHooks[i].beforeCanPlayerEdit(pos, facing, stack);
            }
        }

        boolean result;
        if (this.overrideCanPlayerEditHooks != null) {
            result = this.overrideCanPlayerEditHooks[this.overrideCanPlayerEditHooks.length - 1].canPlayerEdit(pos, facing, stack);
        } else {
            result = this.player.superCanPlayerEdit(pos, facing, stack);
        }

        if (this.afterCanPlayerEditHooks != null) {
            for (ServerPlayerEntityBase afterCanPlayerEditHook : this.afterCanPlayerEditHooks) {
                afterCanPlayerEditHook.afterCanPlayerEdit(pos, facing, stack);
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenCanPlayerEdit(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideCanPlayerEditHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideCanPlayerEditHooks.length; i++) {
            if (this.overrideCanPlayerEditHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideCanPlayerEditHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeCanPlayerEditHookTypes = new LinkedList<>();
    private final static List<String> overrideCanPlayerEditHookTypes = new LinkedList<>();
    private final static List<String> afterCanPlayerEditHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeCanPlayerEditHooks;
    private ServerPlayerEntityBase[] overrideCanPlayerEditHooks;
    private ServerPlayerEntityBase[] afterCanPlayerEditHooks;
    public boolean isCanPlayerEditModded;
    private static final Map<String, String[]> allBaseBeforeCanPlayerEditSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeCanPlayerEditInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideCanPlayerEditSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideCanPlayerEditInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterCanPlayerEditSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterCanPlayerEditInferiors = new Hashtable<>(0);

    // ############################################################################

    public static boolean canTriggerWalking(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isCanTriggerWalkingModded) {
            return serverPlayerAPI.canTriggerWalking();
        } else {
            return target.superCanTriggerWalking();
        }
    }

    private boolean canTriggerWalking()
    {
        if (this.beforeCanTriggerWalkingHooks != null) {
            for (int i = this.beforeCanTriggerWalkingHooks.length - 1; i >= 0; i--) {
                this.beforeCanTriggerWalkingHooks[i].beforeCanTriggerWalking();
            }
        }

        boolean result;
        if (this.overrideCanTriggerWalkingHooks != null) {
            result = this.overrideCanTriggerWalkingHooks[this.overrideCanTriggerWalkingHooks.length - 1].canTriggerWalking();
        } else {
            result = this.player.superCanTriggerWalking();
        }

        if (this.afterCanTriggerWalkingHooks != null) {
            for (ServerPlayerEntityBase afterCanTriggerWalkingHook : this.afterCanTriggerWalkingHooks) {
                afterCanTriggerWalkingHook.afterCanTriggerWalking();
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenCanTriggerWalking(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideCanTriggerWalkingHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideCanTriggerWalkingHooks.length; i++) {
            if (this.overrideCanTriggerWalkingHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideCanTriggerWalkingHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeCanTriggerWalkingHookTypes = new LinkedList<>();
    private final static List<String> overrideCanTriggerWalkingHookTypes = new LinkedList<>();
    private final static List<String> afterCanTriggerWalkingHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeCanTriggerWalkingHooks;
    private ServerPlayerEntityBase[] overrideCanTriggerWalkingHooks;
    private ServerPlayerEntityBase[] afterCanTriggerWalkingHooks;
    public boolean isCanTriggerWalkingModded;
    private static final Map<String, String[]> allBaseBeforeCanTriggerWalkingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeCanTriggerWalkingInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideCanTriggerWalkingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideCanTriggerWalkingInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterCanTriggerWalkingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterCanTriggerWalkingInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforeCopyFrom(CallbackInfo callbackInfo, IServerPlayerEntity target, ServerPlayerEntity that, boolean keepEverything)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isCopyFromModded) {
            serverPlayerAPI.beforeCopyFrom(callbackInfo, that, keepEverything);
        }
    }

    private void beforeCopyFrom(CallbackInfo callbackInfo, ServerPlayerEntity that, boolean keepEverything)
    {
        if (this.beforeCopyFromHooks != null) {
            for (int i = this.beforeCopyFromHooks.length - 1; i >= 0; i--) {
                this.beforeCopyFromHooks[i].beforeCopyFrom(that, keepEverything);
            }
        }

        if (this.overrideCopyFromHooks != null) {
            this.overrideCopyFromHooks[this.overrideCopyFromHooks.length - 1].copyFrom(that, keepEverything);
            callbackInfo.cancel();
        }
    }

    public static void afterCopyFrom(IServerPlayerEntity target, ServerPlayerEntity that, boolean keepEverything)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isCopyFromModded) {
            serverPlayerAPI.afterCopyFrom(that, keepEverything);
        }
    }

    private void afterCopyFrom(ServerPlayerEntity that, boolean keepEverything)
    {
        if (this.afterCopyFromHooks != null) {
            for (ServerPlayerEntityBase afterCopyFromHook : this.afterCopyFromHooks) {
                afterCopyFromHook.afterCopyFrom(that, keepEverything);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenCopyFrom(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideCopyFromHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideCopyFromHooks.length; i++) {
            if (this.overrideCopyFromHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideCopyFromHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeCopyFromHookTypes = new LinkedList<>();
    private final static List<String> overrideCopyFromHookTypes = new LinkedList<>();
    private final static List<String> afterCopyFromHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeCopyFromHooks;
    private ServerPlayerEntityBase[] overrideCopyFromHooks;
    private ServerPlayerEntityBase[] afterCopyFromHooks;
    public boolean isCopyFromModded;
    private static final Map<String, String[]> allBaseBeforeCopyFromSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeCopyFromInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideCopyFromSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideCopyFromInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterCopyFromSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterCopyFromInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void damageEntity(IServerPlayerEntity target, DamageSource source, float amount)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isDamageEntityModded) {
            serverPlayerAPI.damageEntity(source, amount);
        } else {
            target.superDamageEntity(source, amount);
        }
    }

    private void damageEntity(DamageSource source, float amount)
    {
        if (this.beforeDamageEntityHooks != null) {
            for (int i = this.beforeDamageEntityHooks.length - 1; i >= 0; i--) {
                this.beforeDamageEntityHooks[i].beforeDamageEntity(source, amount);
            }
        }

        if (this.overrideDamageEntityHooks != null) {
            this.overrideDamageEntityHooks[this.overrideDamageEntityHooks.length - 1].damageEntity(source, amount);
        } else {
            this.player.superDamageEntity(source, amount);
        }

        if (this.afterDamageEntityHooks != null) {
            for (ServerPlayerEntityBase afterDamageEntityHook : this.afterDamageEntityHooks) {
                afterDamageEntityHook.afterDamageEntity(source, amount);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenDamageEntity(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideDamageEntityHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideDamageEntityHooks.length; i++) {
            if (this.overrideDamageEntityHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideDamageEntityHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeDamageEntityHookTypes = new LinkedList<>();
    private final static List<String> overrideDamageEntityHookTypes = new LinkedList<>();
    private final static List<String> afterDamageEntityHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeDamageEntityHooks;
    private ServerPlayerEntityBase[] overrideDamageEntityHooks;
    private ServerPlayerEntityBase[] afterDamageEntityHooks;
    public boolean isDamageEntityModded;
    private static final Map<String, String[]> allBaseBeforeDamageEntitySuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeDamageEntityInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideDamageEntitySuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideDamageEntityInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterDamageEntitySuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterDamageEntityInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforeDropItem(CallbackInfoReturnable<ItemEntity> callbackInfo, IServerPlayerEntity target, ItemStack droppedItem, boolean dropAround, boolean traceItem)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isDropItemModded) {
            serverPlayerAPI.beforeDropItem(callbackInfo, droppedItem, dropAround, traceItem);
        }
    }

    private void beforeDropItem(CallbackInfoReturnable<ItemEntity> callbackInfo, ItemStack droppedItem, boolean dropAround, boolean traceItem)
    {
        if (this.beforeDropItemHooks != null) {
            for (int i = this.beforeDropItemHooks.length - 1; i >= 0; i--) {
                this.beforeDropItemHooks[i].beforeDropItem(droppedItem, dropAround, traceItem);
            }
        }

        if (this.overrideDropItemHooks != null) {
            callbackInfo.setReturnValue(this.overrideDropItemHooks[this.overrideDropItemHooks.length - 1].dropItem(droppedItem, dropAround, traceItem));
            callbackInfo.cancel();
        }
    }

    public static void afterDropItem(IServerPlayerEntity target, ItemStack droppedItem, boolean dropAround, boolean traceItem)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isDropItemModded) {
            serverPlayerAPI.afterDropItem(droppedItem, dropAround, traceItem);
        }
    }

    private void afterDropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem)
    {
        if (this.afterDropItemHooks != null) {
            for (ServerPlayerEntityBase afterDropItemHook : this.afterDropItemHooks) {
                afterDropItemHook.afterDropItem(droppedItem, dropAround, traceItem);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenDropItem(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideDropItemHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideDropItemHooks.length; i++) {
            if (this.overrideDropItemHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideDropItemHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeDropItemHookTypes = new LinkedList<>();
    private final static List<String> overrideDropItemHookTypes = new LinkedList<>();
    private final static List<String> afterDropItemHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeDropItemHooks;
    private ServerPlayerEntityBase[] overrideDropItemHooks;
    private ServerPlayerEntityBase[] afterDropItemHooks;
    public boolean isDropItemModded;
    private static final Map<String, String[]> allBaseBeforeDropItemSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeDropItemInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideDropItemSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideDropItemInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterDropItemSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterDropItemInferiors = new Hashtable<>(0);

    // ############################################################################

    public static float getAIMoveSpeed(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isGetAIMoveSpeedModded) {
            return serverPlayerAPI.getAIMoveSpeed();
        } else {
            return target.superGetAIMoveSpeed();
        }
    }

    private float getAIMoveSpeed()
    {
        if (this.beforeGetAIMoveSpeedHooks != null) {
            for (int i = this.beforeGetAIMoveSpeedHooks.length - 1; i >= 0; i--) {
                this.beforeGetAIMoveSpeedHooks[i].beforeGetAIMoveSpeed();
            }
        }

        float result;
        if (this.overrideGetAIMoveSpeedHooks != null) {
            result = this.overrideGetAIMoveSpeedHooks[this.overrideGetAIMoveSpeedHooks.length - 1].getAIMoveSpeed();
        } else {
            result = this.player.superGetAIMoveSpeed();
        }

        if (this.afterGetAIMoveSpeedHooks != null) {
            for (ServerPlayerEntityBase afterGetAIMoveSpeedHook : this.afterGetAIMoveSpeedHooks) {
                afterGetAIMoveSpeedHook.afterGetAIMoveSpeed();
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenGetAIMoveSpeed(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideGetAIMoveSpeedHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideGetAIMoveSpeedHooks.length; i++) {
            if (this.overrideGetAIMoveSpeedHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideGetAIMoveSpeedHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeGetAIMoveSpeedHookTypes = new LinkedList<>();
    private final static List<String> overrideGetAIMoveSpeedHookTypes = new LinkedList<>();
    private final static List<String> afterGetAIMoveSpeedHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeGetAIMoveSpeedHooks;
    private ServerPlayerEntityBase[] overrideGetAIMoveSpeedHooks;
    private ServerPlayerEntityBase[] afterGetAIMoveSpeedHooks;
    public boolean isGetAIMoveSpeedModded;
    private static final Map<String, String[]> allBaseBeforeGetAIMoveSpeedSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeGetAIMoveSpeedInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetAIMoveSpeedSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetAIMoveSpeedInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetAIMoveSpeedSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetAIMoveSpeedInferiors = new Hashtable<>(0);

    // ############################################################################

    public static float getBrightness(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isGetBrightnessModded) {
            return serverPlayerAPI.getBrightness();
        } else {
            return target.superGetBrightness();
        }
    }

    private float getBrightness()
    {
        if (this.beforeGetBrightnessHooks != null) {
            for (int i = this.beforeGetBrightnessHooks.length - 1; i >= 0; i--) {
                this.beforeGetBrightnessHooks[i].beforeGetBrightness();
            }
        }

        float result;
        if (this.overrideGetBrightnessHooks != null) {
            result = this.overrideGetBrightnessHooks[this.overrideGetBrightnessHooks.length - 1].getBrightness();
        } else {
            result = this.player.superGetBrightness();
        }

        if (this.afterGetBrightnessHooks != null) {
            for (ServerPlayerEntityBase afterGetBrightnessHook : this.afterGetBrightnessHooks) {
                afterGetBrightnessHook.afterGetBrightness();
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenGetBrightness(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideGetBrightnessHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideGetBrightnessHooks.length; i++) {
            if (this.overrideGetBrightnessHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideGetBrightnessHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeGetBrightnessHookTypes = new LinkedList<>();
    private final static List<String> overrideGetBrightnessHookTypes = new LinkedList<>();
    private final static List<String> afterGetBrightnessHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeGetBrightnessHooks;
    private ServerPlayerEntityBase[] overrideGetBrightnessHooks;
    private ServerPlayerEntityBase[] afterGetBrightnessHooks;
    public boolean isGetBrightnessModded;
    private static final Map<String, String[]> allBaseBeforeGetBrightnessSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeGetBrightnessInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetBrightnessSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetBrightnessInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetBrightnessSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetBrightnessInferiors = new Hashtable<>(0);

    // ############################################################################

    public static float getDigSpeed(IServerPlayerEntity target, BlockState state, BlockPos pos)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isGetDigSpeedModded) {
            return serverPlayerAPI.getDigSpeed(state, pos);
        } else {
            return target.superGetDigSpeed(state, pos);
        }
    }

    private float getDigSpeed(BlockState state, BlockPos pos)
    {
        if (this.beforeGetDigSpeedHooks != null) {
            for (int i = this.beforeGetDigSpeedHooks.length - 1; i >= 0; i--) {
                this.beforeGetDigSpeedHooks[i].beforeGetDigSpeed(state, pos);
            }
        }

        float result;
        if (this.overrideGetDigSpeedHooks != null) {
            result = this.overrideGetDigSpeedHooks[this.overrideGetDigSpeedHooks.length - 1].getDigSpeed(state, pos);
        } else {
            result = this.player.superGetDigSpeed(state, pos);
        }

        if (this.afterGetDigSpeedHooks != null) {
            for (ServerPlayerEntityBase afterGetDigSpeedHook : this.afterGetDigSpeedHooks) {
                afterGetDigSpeedHook.afterGetDigSpeed(state, pos);
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenGetDigSpeed(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideGetDigSpeedHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideGetDigSpeedHooks.length; i++) {
            if (this.overrideGetDigSpeedHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideGetDigSpeedHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeGetDigSpeedHookTypes = new LinkedList<>();
    private final static List<String> overrideGetDigSpeedHookTypes = new LinkedList<>();
    private final static List<String> afterGetDigSpeedHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeGetDigSpeedHooks;
    private ServerPlayerEntityBase[] overrideGetDigSpeedHooks;
    private ServerPlayerEntityBase[] afterGetDigSpeedHooks;
    public boolean isGetDigSpeedModded;
    private static final Map<String, String[]> allBaseBeforeGetDigSpeedSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeGetDigSpeedInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetDigSpeedSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetDigSpeedInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetDigSpeedSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetDigSpeedInferiors = new Hashtable<>(0);

    // ############################################################################

    public static double getDistanceSq(IServerPlayerEntity target, double x, double y, double z)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isGetDistanceSqModded) {
            return serverPlayerAPI.getDistanceSq(x, y, z);
        } else {
            return target.superGetDistanceSq(x, y, z);
        }
    }

    private double getDistanceSq(double x, double y, double z)
    {
        if (this.beforeGetDistanceSqHooks != null) {
            for (int i = this.beforeGetDistanceSqHooks.length - 1; i >= 0; i--) {
                this.beforeGetDistanceSqHooks[i].beforeGetDistanceSq(x, y, z);
            }
        }

        double result;
        if (this.overrideGetDistanceSqHooks != null) {
            result = this.overrideGetDistanceSqHooks[this.overrideGetDistanceSqHooks.length - 1].getDistanceSq(x, y, z);
        } else {
            result = this.player.superGetDistanceSq(x, y, z);
        }

        if (this.afterGetDistanceSqHooks != null) {
            for (ServerPlayerEntityBase afterGetDistanceSqHook : this.afterGetDistanceSqHooks) {
                afterGetDistanceSqHook.afterGetDistanceSq(x, y, z);
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenGetDistanceSq(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideGetDistanceSqHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideGetDistanceSqHooks.length; i++) {
            if (this.overrideGetDistanceSqHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideGetDistanceSqHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeGetDistanceSqHookTypes = new LinkedList<>();
    private final static List<String> overrideGetDistanceSqHookTypes = new LinkedList<>();
    private final static List<String> afterGetDistanceSqHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeGetDistanceSqHooks;
    private ServerPlayerEntityBase[] overrideGetDistanceSqHooks;
    private ServerPlayerEntityBase[] afterGetDistanceSqHooks;
    public boolean isGetDistanceSqModded;
    private static final Map<String, String[]> allBaseBeforeGetDistanceSqSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeGetDistanceSqInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetDistanceSqSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetDistanceSqInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetDistanceSqSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetDistanceSqInferiors = new Hashtable<>(0);

    // ############################################################################

    public static double getDistanceSqToEntity(IServerPlayerEntity target, Entity entity)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isGetDistanceSqToEntityModded) {
            return serverPlayerAPI.getDistanceSqToEntity(entity);
        } else {
            return target.superGetDistanceSq(entity);
        }
    }

    private double getDistanceSqToEntity(Entity entity)
    {
        if (this.beforeGetDistanceSqToEntityHooks != null) {
            for (int i = this.beforeGetDistanceSqToEntityHooks.length - 1; i >= 0; i--) {
                this.beforeGetDistanceSqToEntityHooks[i].beforeGetDistanceSqToEntity(entity);
            }
        }

        double result;
        if (this.overrideGetDistanceSqToEntityHooks != null) {
            result = this.overrideGetDistanceSqToEntityHooks[this.overrideGetDistanceSqToEntityHooks.length - 1].getDistanceSqToEntity(entity);
        } else {
            result = this.player.superGetDistanceSq(entity);
        }

        if (this.afterGetDistanceSqToEntityHooks != null) {
            for (ServerPlayerEntityBase afterGetDistanceSqToEntityHook : this.afterGetDistanceSqToEntityHooks) {
                afterGetDistanceSqToEntityHook.afterGetDistanceSqToEntity(entity);
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenGetDistanceSqToEntity(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideGetDistanceSqToEntityHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideGetDistanceSqToEntityHooks.length; i++) {
            if (this.overrideGetDistanceSqToEntityHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideGetDistanceSqToEntityHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeGetDistanceSqToEntityHookTypes = new LinkedList<>();
    private final static List<String> overrideGetDistanceSqToEntityHookTypes = new LinkedList<>();
    private final static List<String> afterGetDistanceSqToEntityHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeGetDistanceSqToEntityHooks;
    private ServerPlayerEntityBase[] overrideGetDistanceSqToEntityHooks;
    private ServerPlayerEntityBase[] afterGetDistanceSqToEntityHooks;
    public boolean isGetDistanceSqToEntityModded;
    private static final Map<String, String[]> allBaseBeforeGetDistanceSqToEntitySuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeGetDistanceSqToEntityInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetDistanceSqToEntitySuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetDistanceSqToEntityInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetDistanceSqToEntitySuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetDistanceSqToEntityInferiors = new Hashtable<>(0);

    // ############################################################################

    public static double getDistanceSqVec(IServerPlayerEntity target, Vec3d pos)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isGetDistanceSqVecModded) {
            return serverPlayerAPI.getDistanceSqVec(pos);
        } else {
            return target.superGetDistanceSq(pos);
        }
    }

    private double getDistanceSqVec(Vec3d pos)
    {
        if (this.beforeGetDistanceSqVecHooks != null) {
            for (int i = this.beforeGetDistanceSqVecHooks.length - 1; i >= 0; i--) {
                this.beforeGetDistanceSqVecHooks[i].beforeGetDistanceSqVec(pos);
            }
        }

        double result;
        if (this.overrideGetDistanceSqVecHooks != null) {
            result = this.overrideGetDistanceSqVecHooks[this.overrideGetDistanceSqVecHooks.length - 1].getDistanceSqVec(pos);
        } else {
            result = this.player.superGetDistanceSq(pos);
        }

        if (this.afterGetDistanceSqVecHooks != null) {
            for (ServerPlayerEntityBase afterGetDistanceSqVecHook : this.afterGetDistanceSqVecHooks) {
                afterGetDistanceSqVecHook.afterGetDistanceSqVec(pos);
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenGetDistanceSqVec(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideGetDistanceSqVecHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideGetDistanceSqVecHooks.length; i++) {
            if (this.overrideGetDistanceSqVecHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideGetDistanceSqVecHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeGetDistanceSqVecHookTypes = new LinkedList<>();
    private final static List<String> overrideGetDistanceSqVecHookTypes = new LinkedList<>();
    private final static List<String> afterGetDistanceSqVecHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeGetDistanceSqVecHooks;
    private ServerPlayerEntityBase[] overrideGetDistanceSqVecHooks;
    private ServerPlayerEntityBase[] afterGetDistanceSqVecHooks;
    public boolean isGetDistanceSqVecModded;
    private static final Map<String, String[]> allBaseBeforeGetDistanceSqVecSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeGetDistanceSqVecInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetDistanceSqVecSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetDistanceSqVecInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetDistanceSqVecSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetDistanceSqVecInferiors = new Hashtable<>(0);

    // ############################################################################

    public static SoundEvent getHurtSound(IServerPlayerEntity target, DamageSource source)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isGetHurtSoundModded) {
            return serverPlayerAPI.getHurtSound(source);
        } else {
            return target.superGetHurtSound(source);
        }
    }

    private SoundEvent getHurtSound(DamageSource source)
    {
        if (this.beforeGetHurtSoundHooks != null) {
            for (int i = this.beforeGetHurtSoundHooks.length - 1; i >= 0; i--) {
                this.beforeGetHurtSoundHooks[i].beforeGetHurtSound(source);
            }
        }

        SoundEvent result;
        if (this.overrideGetHurtSoundHooks != null) {
            result = this.overrideGetHurtSoundHooks[this.overrideGetHurtSoundHooks.length - 1].getHurtSound(source);
        } else {
            result = this.player.superGetHurtSound(source);
        }

        if (this.afterGetHurtSoundHooks != null) {
            for (ServerPlayerEntityBase afterGetHurtSoundHook : this.afterGetHurtSoundHooks) {
                afterGetHurtSoundHook.afterGetHurtSound(source);
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenGetHurtSound(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideGetHurtSoundHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideGetHurtSoundHooks.length; i++) {
            if (this.overrideGetHurtSoundHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideGetHurtSoundHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeGetHurtSoundHookTypes = new LinkedList<>();
    private final static List<String> overrideGetHurtSoundHookTypes = new LinkedList<>();
    private final static List<String> afterGetHurtSoundHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeGetHurtSoundHooks;
    private ServerPlayerEntityBase[] overrideGetHurtSoundHooks;
    private ServerPlayerEntityBase[] afterGetHurtSoundHooks;
    public boolean isGetHurtSoundModded;
    private static final Map<String, String[]> allBaseBeforeGetHurtSoundSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeGetHurtSoundInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetHurtSoundSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetHurtSoundInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetHurtSoundSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetHurtSoundInferiors = new Hashtable<>(0);

    // ############################################################################

    public static ITextComponent getName(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isGetNameModded) {
            return serverPlayerAPI.getName();
        } else {
            return target.superGetName();
        }
    }

    private ITextComponent getName()
    {
        if (this.beforeGetNameHooks != null) {
            for (int i = this.beforeGetNameHooks.length - 1; i >= 0; i--) {
                this.beforeGetNameHooks[i].beforeGetName();
            }
        }

        ITextComponent result;
        if (this.overrideGetNameHooks != null) {
            result = this.overrideGetNameHooks[this.overrideGetNameHooks.length - 1].getName();
        } else {
            result = this.player.superGetName();
        }

        if (this.afterGetNameHooks != null) {
            for (ServerPlayerEntityBase afterGetNameHook : this.afterGetNameHooks) {
                afterGetNameHook.afterGetName();
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenGetName(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideGetNameHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideGetNameHooks.length; i++) {
            if (this.overrideGetNameHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideGetNameHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeGetNameHookTypes = new LinkedList<>();
    private final static List<String> overrideGetNameHookTypes = new LinkedList<>();
    private final static List<String> afterGetNameHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeGetNameHooks;
    private ServerPlayerEntityBase[] overrideGetNameHooks;
    private ServerPlayerEntityBase[] afterGetNameHooks;
    public boolean isGetNameModded;
    private static final Map<String, String[]> allBaseBeforeGetNameSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeGetNameInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetNameSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetNameInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetNameSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetNameInferiors = new Hashtable<>(0);

    // ############################################################################

    public static EntitySize getSize(IServerPlayerEntity target, Pose pose)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isGetSizeModded) {
            return serverPlayerAPI.getSize(pose);
        } else {
            return target.superGetSize(pose);
        }
    }

    private EntitySize getSize(Pose pose)
    {
        if (this.beforeGetSizeHooks != null) {
            for (int i = this.beforeGetSizeHooks.length - 1; i >= 0; i--) {
                this.beforeGetSizeHooks[i].beforeGetSize(pose);
            }
        }

        EntitySize result;
        if (this.overrideGetSizeHooks != null) {
            result = this.overrideGetSizeHooks[this.overrideGetSizeHooks.length - 1].getSize(pose);
        } else {
            result = this.player.superGetSize(pose);
        }

        if (this.afterGetSizeHooks != null) {
            for (ServerPlayerEntityBase afterGetSizeHook : this.afterGetSizeHooks) {
                afterGetSizeHook.afterGetSize(pose);
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenGetSize(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideGetSizeHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideGetSizeHooks.length; i++) {
            if (this.overrideGetSizeHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideGetSizeHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeGetSizeHookTypes = new LinkedList<>();
    private final static List<String> overrideGetSizeHookTypes = new LinkedList<>();
    private final static List<String> afterGetSizeHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeGetSizeHooks;
    private ServerPlayerEntityBase[] overrideGetSizeHooks;
    private ServerPlayerEntityBase[] afterGetSizeHooks;
    public boolean isGetSizeModded;
    private static final Map<String, String[]> allBaseBeforeGetSizeSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeGetSizeInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetSizeSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetSizeInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetSizeSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetSizeInferiors = new Hashtable<>(0);

    // ############################################################################

    public static int getSleepTimer(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isGetSleepTimerModded) {
            return serverPlayerAPI.getSleepTimer();
        } else {
            return target.superGetSleepTimer();
        }
    }

    private int getSleepTimer()
    {
        if (this.beforeGetSleepTimerHooks != null) {
            for (int i = this.beforeGetSleepTimerHooks.length - 1; i >= 0; i--) {
                this.beforeGetSleepTimerHooks[i].beforeGetSleepTimer();
            }
        }

        int result;
        if (this.overrideGetSleepTimerHooks != null) {
            result = this.overrideGetSleepTimerHooks[this.overrideGetSleepTimerHooks.length - 1].getSleepTimer();
        } else {
            result = this.player.superGetSleepTimer();
        }

        if (this.afterGetSleepTimerHooks != null) {
            for (ServerPlayerEntityBase afterGetSleepTimerHook : this.afterGetSleepTimerHooks) {
                afterGetSleepTimerHook.afterGetSleepTimer();
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenGetSleepTimer(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideGetSleepTimerHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideGetSleepTimerHooks.length; i++) {
            if (this.overrideGetSleepTimerHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideGetSleepTimerHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeGetSleepTimerHookTypes = new LinkedList<>();
    private final static List<String> overrideGetSleepTimerHookTypes = new LinkedList<>();
    private final static List<String> afterGetSleepTimerHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeGetSleepTimerHooks;
    private ServerPlayerEntityBase[] overrideGetSleepTimerHooks;
    private ServerPlayerEntityBase[] afterGetSleepTimerHooks;
    public boolean isGetSleepTimerModded;
    private static final Map<String, String[]> allBaseBeforeGetSleepTimerSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeGetSleepTimerInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetSleepTimerSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetSleepTimerInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetSleepTimerSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetSleepTimerInferiors = new Hashtable<>(0);

    // ############################################################################

    public static float getStandingEyeHeight(IServerPlayerEntity target, Pose pose, EntitySize size)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isGetStandingEyeHeightModded) {
            return serverPlayerAPI.getStandingEyeHeight(pose, size);
        } else {
            return target.superGetStandingEyeHeight(pose, size);
        }
    }

    private float getStandingEyeHeight(Pose pose, EntitySize size)
    {
        if (this.beforeGetStandingEyeHeightHooks != null) {
            for (int i = this.beforeGetStandingEyeHeightHooks.length - 1; i >= 0; i--) {
                this.beforeGetStandingEyeHeightHooks[i].beforeGetStandingEyeHeight(pose, size);
            }
        }

        float result;
        if (this.overrideGetStandingEyeHeightHooks != null) {
            result = this.overrideGetStandingEyeHeightHooks[this.overrideGetStandingEyeHeightHooks.length - 1].getStandingEyeHeight(pose, size);
        } else {
            result = this.player.superGetStandingEyeHeight(pose, size);
        }

        if (this.afterGetStandingEyeHeightHooks != null) {
            for (ServerPlayerEntityBase afterGetStandingEyeHeightHook : this.afterGetStandingEyeHeightHooks) {
                afterGetStandingEyeHeightHook.afterGetStandingEyeHeight(pose, size);
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenGetStandingEyeHeight(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideGetStandingEyeHeightHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideGetStandingEyeHeightHooks.length; i++) {
            if (this.overrideGetStandingEyeHeightHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideGetStandingEyeHeightHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeGetStandingEyeHeightHookTypes = new LinkedList<>();
    private final static List<String> overrideGetStandingEyeHeightHookTypes = new LinkedList<>();
    private final static List<String> afterGetStandingEyeHeightHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeGetStandingEyeHeightHooks;
    private ServerPlayerEntityBase[] overrideGetStandingEyeHeightHooks;
    private ServerPlayerEntityBase[] afterGetStandingEyeHeightHooks;
    public boolean isGetStandingEyeHeightModded;
    private static final Map<String, String[]> allBaseBeforeGetStandingEyeHeightSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeGetStandingEyeHeightInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetStandingEyeHeightSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGetStandingEyeHeightInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetStandingEyeHeightSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGetStandingEyeHeightInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforeGiveExperiencePoints(CallbackInfo callbackInfo, IServerPlayerEntity target, int points)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isGiveExperiencePointsModded) {
            serverPlayerAPI.beforeGiveExperiencePoints(callbackInfo, points);
        }
    }

    private void beforeGiveExperiencePoints(CallbackInfo callbackInfo, int points)
    {
        if (this.beforeGiveExperiencePointsHooks != null) {
            for (int i = this.beforeGiveExperiencePointsHooks.length - 1; i >= 0; i--) {
                this.beforeGiveExperiencePointsHooks[i].beforeGiveExperiencePoints(points);
            }
        }

        if (this.overrideGiveExperiencePointsHooks != null) {
            this.overrideGiveExperiencePointsHooks[this.overrideGiveExperiencePointsHooks.length - 1].giveExperiencePoints(points);
            callbackInfo.cancel();
        }
    }

    public static void afterGiveExperiencePoints(IServerPlayerEntity target, int points)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isGiveExperiencePointsModded) {
            serverPlayerAPI.afterGiveExperiencePoints(points);
        }
    }

    private void afterGiveExperiencePoints(int points)
    {
        if (this.afterAddExhaustionHooks != null) {
            for (ServerPlayerEntityBase afterGiveExperiencePointsHook : this.afterGiveExperiencePointsHooks) {
                afterGiveExperiencePointsHook.afterGiveExperiencePoints(points);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenGiveExperiencePoints(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideGiveExperiencePointsHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideGiveExperiencePointsHooks.length; i++) {
            if (this.overrideGiveExperiencePointsHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideGiveExperiencePointsHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeGiveExperiencePointsHookTypes = new LinkedList<>();
    private final static List<String> overrideGiveExperiencePointsHookTypes = new LinkedList<>();
    private final static List<String> afterGiveExperiencePointsHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeGiveExperiencePointsHooks;
    private ServerPlayerEntityBase[] overrideGiveExperiencePointsHooks;
    private ServerPlayerEntityBase[] afterGiveExperiencePointsHooks;
    public boolean isGiveExperiencePointsModded;
    private static final Map<String, String[]> allBaseBeforeGiveExperiencePointsSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeGiveExperiencePointsInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGiveExperiencePointsSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideGiveExperiencePointsInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGiveExperiencePointsSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterGiveExperiencePointsInferiors = new Hashtable<>(0);

    // ############################################################################

    public static boolean handleWaterMovement(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isHandleWaterMovementModded) {
            return serverPlayerAPI.handleWaterMovement();
        } else {
            return target.superHandleWaterMovement();
        }
    }

    private boolean handleWaterMovement()
    {
        if (this.beforeHandleWaterMovementHooks != null) {
            for (int i = this.beforeHandleWaterMovementHooks.length - 1; i >= 0; i--) {
                this.beforeHandleWaterMovementHooks[i].beforeHandleWaterMovement();
            }
        }

        boolean result;
        if (this.overrideHandleWaterMovementHooks != null) {
            result = this.overrideHandleWaterMovementHooks[this.overrideHandleWaterMovementHooks.length - 1].handleWaterMovement();
        } else {
            result = this.player.superHandleWaterMovement();
        }

        if (this.afterHandleWaterMovementHooks != null) {
            for (ServerPlayerEntityBase afterHandleWaterMovementHook : this.afterHandleWaterMovementHooks) {
                afterHandleWaterMovementHook.afterHandleWaterMovement();
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenHandleWaterMovement(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideHandleWaterMovementHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideHandleWaterMovementHooks.length; i++) {
            if (this.overrideHandleWaterMovementHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideHandleWaterMovementHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeHandleWaterMovementHookTypes = new LinkedList<>();
    private final static List<String> overrideHandleWaterMovementHookTypes = new LinkedList<>();
    private final static List<String> afterHandleWaterMovementHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeHandleWaterMovementHooks;
    private ServerPlayerEntityBase[] overrideHandleWaterMovementHooks;
    private ServerPlayerEntityBase[] afterHandleWaterMovementHooks;
    public boolean isHandleWaterMovementModded;
    private static final Map<String, String[]> allBaseBeforeHandleWaterMovementSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeHandleWaterMovementInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideHandleWaterMovementSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideHandleWaterMovementInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterHandleWaterMovementSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterHandleWaterMovementInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void heal(IServerPlayerEntity target, float amount)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isHealModded) {
            serverPlayerAPI.heal(amount);
        } else {
            target.superHeal(amount);
        }
    }

    private void heal(float amount)
    {
        if (this.beforeHealHooks != null) {
            for (int i = this.beforeHealHooks.length - 1; i >= 0; i--) {
                this.beforeHealHooks[i].beforeHeal(amount);
            }
        }

        if (this.overrideHealHooks != null) {
            this.overrideHealHooks[this.overrideHealHooks.length - 1].heal(amount);
        } else {
            this.player.superHeal(amount);
        }

        if (this.afterHealHooks != null) {
            for (ServerPlayerEntityBase afterHealHook : this.afterHealHooks) {
                afterHealHook.afterHeal(amount);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenHeal(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideHealHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideHealHooks.length; i++) {
            if (this.overrideHealHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideHealHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeHealHookTypes = new LinkedList<>();
    private final static List<String> overrideHealHookTypes = new LinkedList<>();
    private final static List<String> afterHealHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeHealHooks;
    private ServerPlayerEntityBase[] overrideHealHooks;
    private ServerPlayerEntityBase[] afterHealHooks;
    public boolean isHealModded;
    private static final Map<String, String[]> allBaseBeforeHealSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeHealInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideHealSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideHealInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterHealSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterHealInferiors = new Hashtable<>(0);

    // ############################################################################

    public static boolean isEntityInsideOpaqueBlock(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isIsEntityInsideOpaqueBlockModded) {
            return serverPlayerAPI.isEntityInsideOpaqueBlock();
        } else {
            return target.superIsEntityInsideOpaqueBlock();
        }
    }

    private boolean isEntityInsideOpaqueBlock()
    {
        if (this.beforeIsEntityInsideOpaqueBlockHooks != null) {
            for (int i = this.beforeIsEntityInsideOpaqueBlockHooks.length - 1; i >= 0; i--) {
                this.beforeIsEntityInsideOpaqueBlockHooks[i].beforeIsEntityInsideOpaqueBlock();
            }
        }

        boolean result;
        if (this.overrideIsEntityInsideOpaqueBlockHooks != null) {
            result = this.overrideIsEntityInsideOpaqueBlockHooks[this.overrideIsEntityInsideOpaqueBlockHooks.length - 1].isEntityInsideOpaqueBlock();
        } else {
            result = this.player.superIsEntityInsideOpaqueBlock();
        }

        if (this.afterIsEntityInsideOpaqueBlockHooks != null) {
            for (ServerPlayerEntityBase afterIsEntityInsideOpaqueBlockHook : this.afterIsEntityInsideOpaqueBlockHooks) {
                afterIsEntityInsideOpaqueBlockHook.afterIsEntityInsideOpaqueBlock();
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenIsEntityInsideOpaqueBlock(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideIsEntityInsideOpaqueBlockHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideIsEntityInsideOpaqueBlockHooks.length; i++) {
            if (this.overrideIsEntityInsideOpaqueBlockHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideIsEntityInsideOpaqueBlockHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeIsEntityInsideOpaqueBlockHookTypes = new LinkedList<>();
    private final static List<String> overrideIsEntityInsideOpaqueBlockHookTypes = new LinkedList<>();
    private final static List<String> afterIsEntityInsideOpaqueBlockHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeIsEntityInsideOpaqueBlockHooks;
    private ServerPlayerEntityBase[] overrideIsEntityInsideOpaqueBlockHooks;
    private ServerPlayerEntityBase[] afterIsEntityInsideOpaqueBlockHooks;
    public boolean isIsEntityInsideOpaqueBlockModded;
    private static final Map<String, String[]> allBaseBeforeIsEntityInsideOpaqueBlockSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeIsEntityInsideOpaqueBlockInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideIsEntityInsideOpaqueBlockSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideIsEntityInsideOpaqueBlockInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterIsEntityInsideOpaqueBlockSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterIsEntityInsideOpaqueBlockInferiors = new Hashtable<>(0);

    // ############################################################################

    public static boolean isInWater(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isIsInWaterModded) {
            return serverPlayerAPI.isInWater();
        } else {
            return target.superIsInWater();
        }
    }

    private boolean isInWater()
    {
        if (this.beforeIsInWaterHooks != null) {
            for (int i = this.beforeIsInWaterHooks.length - 1; i >= 0; i--) {
                this.beforeIsInWaterHooks[i].beforeIsInWater();
            }
        }

        boolean result;
        if (this.overrideIsInWaterHooks != null) {
            result = this.overrideIsInWaterHooks[this.overrideIsInWaterHooks.length - 1].isInWater();
        } else {
            result = this.player.superIsInWater();
        }

        if (this.afterIsInWaterHooks != null) {
            for (ServerPlayerEntityBase afterIsInWaterHook : this.afterIsInWaterHooks) {
                afterIsInWaterHook.afterIsInWater();
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenIsInWater(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideIsInWaterHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideIsInWaterHooks.length; i++) {
            if (this.overrideIsInWaterHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideIsInWaterHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeIsInWaterHookTypes = new LinkedList<>();
    private final static List<String> overrideIsInWaterHookTypes = new LinkedList<>();
    private final static List<String> afterIsInWaterHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeIsInWaterHooks;
    private ServerPlayerEntityBase[] overrideIsInWaterHooks;
    private ServerPlayerEntityBase[] afterIsInWaterHooks;
    public boolean isIsInWaterModded;
    private static final Map<String, String[]> allBaseBeforeIsInWaterSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeIsInWaterInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideIsInWaterSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideIsInWaterInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterIsInWaterSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterIsInWaterInferiors = new Hashtable<>(0);

    // ############################################################################

    public static boolean isOnLadder(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isIsOnLadderModded) {
            return serverPlayerAPI.isOnLadder();
        } else {
            return target.superIsOnLadder();
        }
    }

    private boolean isOnLadder()
    {
        if (this.beforeIsOnLadderHooks != null) {
            for (int i = this.beforeIsOnLadderHooks.length - 1; i >= 0; i--) {
                this.beforeIsOnLadderHooks[i].beforeIsOnLadder();
            }
        }

        boolean result;
        if (this.overrideIsOnLadderHooks != null) {
            result = this.overrideIsOnLadderHooks[this.overrideIsOnLadderHooks.length - 1].isOnLadder();
        } else {
            result = this.player.superIsOnLadder();
        }

        if (this.afterIsOnLadderHooks != null) {
            for (ServerPlayerEntityBase afterIsOnLadderHook : this.afterIsOnLadderHooks) {
                afterIsOnLadderHook.afterIsOnLadder();
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenIsOnLadder(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideIsOnLadderHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideIsOnLadderHooks.length; i++) {
            if (this.overrideIsOnLadderHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideIsOnLadderHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeIsOnLadderHookTypes = new LinkedList<>();
    private final static List<String> overrideIsOnLadderHookTypes = new LinkedList<>();
    private final static List<String> afterIsOnLadderHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeIsOnLadderHooks;
    private ServerPlayerEntityBase[] overrideIsOnLadderHooks;
    private ServerPlayerEntityBase[] afterIsOnLadderHooks;
    public boolean isIsOnLadderModded;
    private static final Map<String, String[]> allBaseBeforeIsOnLadderSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeIsOnLadderInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideIsOnLadderSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideIsOnLadderInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterIsOnLadderSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterIsOnLadderInferiors = new Hashtable<>(0);

    // ############################################################################

    public static boolean isShiftKeyDown(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isIsShiftKeyDownModded) {
            return serverPlayerAPI.isShiftKeyDown();
        } else {
            return target.superIsShiftKeyDown();
        }
    }

    private boolean isShiftKeyDown()
    {
        if (this.beforeIsShiftKeyDownHooks != null) {
            for (int i = this.beforeIsShiftKeyDownHooks.length - 1; i >= 0; i--) {
                this.beforeIsShiftKeyDownHooks[i].beforeIsShiftKeyDown();
            }
        }

        boolean result;
        if (this.overrideIsShiftKeyDownHooks != null) {
            result = this.overrideIsShiftKeyDownHooks[this.overrideIsShiftKeyDownHooks.length - 1].isShiftKeyDown();
        } else {
            result = this.player.superIsShiftKeyDown();
        }

        if (this.afterIsShiftKeyDownHooks != null) {
            for (ServerPlayerEntityBase afterIsShiftKeyDownHook : this.afterIsShiftKeyDownHooks) {
                afterIsShiftKeyDownHook.afterIsShiftKeyDown();
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenIsShiftKeyDown(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideIsShiftKeyDownHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideIsShiftKeyDownHooks.length; i++) {
            if (this.overrideIsShiftKeyDownHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideIsShiftKeyDownHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeIsShiftKeyDownHookTypes = new LinkedList<>();
    private final static List<String> overrideIsShiftKeyDownHookTypes = new LinkedList<>();
    private final static List<String> afterIsShiftKeyDownHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeIsShiftKeyDownHooks;
    private ServerPlayerEntityBase[] overrideIsShiftKeyDownHooks;
    private ServerPlayerEntityBase[] afterIsShiftKeyDownHooks;
    public boolean isIsShiftKeyDownModded;
    private static final Map<String, String[]> allBaseBeforeIsShiftKeyDownSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeIsShiftKeyDownInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideIsShiftKeyDownSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideIsShiftKeyDownInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterIsShiftKeyDownSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterIsShiftKeyDownInferiors = new Hashtable<>(0);

    // ############################################################################

    public static boolean isSleeping(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isIsSleepingModded) {
            return serverPlayerAPI.isSleeping();
        } else {
            return target.superIsSleeping();
        }
    }

    private boolean isSleeping()
    {
        if (this.beforeIsSleepingHooks != null) {
            for (int i = this.beforeIsSleepingHooks.length - 1; i >= 0; i--) {
                this.beforeIsSleepingHooks[i].beforeIsSleeping();
            }
        }

        boolean result;
        if (this.overrideIsSleepingHooks != null) {
            result = this.overrideIsSleepingHooks[this.overrideIsSleepingHooks.length - 1].isSleeping();
        } else {
            result = this.player.superIsSleeping();
        }

        if (this.afterIsSleepingHooks != null) {
            for (ServerPlayerEntityBase afterIsSleepingHook : this.afterIsSleepingHooks) {
                afterIsSleepingHook.afterIsSleeping();
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenIsSleeping(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideIsSleepingHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideIsSleepingHooks.length; i++) {
            if (this.overrideIsSleepingHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideIsSleepingHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeIsSleepingHookTypes = new LinkedList<>();
    private final static List<String> overrideIsSleepingHookTypes = new LinkedList<>();
    private final static List<String> afterIsSleepingHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeIsSleepingHooks;
    private ServerPlayerEntityBase[] overrideIsSleepingHooks;
    private ServerPlayerEntityBase[] afterIsSleepingHooks;
    public boolean isIsSleepingModded;
    private static final Map<String, String[]> allBaseBeforeIsSleepingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeIsSleepingInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideIsSleepingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideIsSleepingInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterIsSleepingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterIsSleepingInferiors = new Hashtable<>(0);

    // ############################################################################

    public static boolean isSprinting(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isIsSprintingModded) {
            return serverPlayerAPI.isSprinting();
        } else {
            return target.superIsSprinting();
        }
    }

    private boolean isSprinting()
    {
        if (this.beforeIsSprintingHooks != null) {
            for (int i = this.beforeIsSprintingHooks.length - 1; i >= 0; i--) {
                this.beforeIsSprintingHooks[i].beforeIsSprinting();
            }
        }

        boolean result;
        if (this.overrideIsSprintingHooks != null) {
            result = this.overrideIsSprintingHooks[this.overrideIsSprintingHooks.length - 1].isSprinting();
        } else {
            result = this.player.superIsSprinting();
        }

        if (this.afterIsSprintingHooks != null) {
            for (ServerPlayerEntityBase afterIsSprintingHook : this.afterIsSprintingHooks) {
                afterIsSprintingHook.afterIsSprinting();
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenIsSprinting(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideIsSprintingHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideIsSprintingHooks.length; i++) {
            if (this.overrideIsSprintingHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideIsSprintingHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeIsSprintingHookTypes = new LinkedList<>();
    private final static List<String> overrideIsSprintingHookTypes = new LinkedList<>();
    private final static List<String> afterIsSprintingHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeIsSprintingHooks;
    private ServerPlayerEntityBase[] overrideIsSprintingHooks;
    private ServerPlayerEntityBase[] afterIsSprintingHooks;
    public boolean isIsSprintingModded;
    private static final Map<String, String[]> allBaseBeforeIsSprintingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeIsSprintingInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideIsSprintingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideIsSprintingInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterIsSprintingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterIsSprintingInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void jump(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isJumpModded) {
            serverPlayerAPI.jump();
        } else {
            target.superJump();
        }
    }

    private void jump()
    {
        if (this.beforeJumpHooks != null) {
            for (int i = this.beforeJumpHooks.length - 1; i >= 0; i--) {
                this.beforeJumpHooks[i].beforeJump();
            }
        }

        if (this.overrideJumpHooks != null) {
            this.overrideJumpHooks[this.overrideJumpHooks.length - 1].jump();
        } else {
            this.player.superJump();
        }

        if (this.afterJumpHooks != null) {
            for (ServerPlayerEntityBase afterJumpHook : this.afterJumpHooks) {
                afterJumpHook.afterJump();
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenJump(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideJumpHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideJumpHooks.length; i++) {
            if (this.overrideJumpHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideJumpHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeJumpHookTypes = new LinkedList<>();
    private final static List<String> overrideJumpHookTypes = new LinkedList<>();
    private final static List<String> afterJumpHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeJumpHooks;
    private ServerPlayerEntityBase[] overrideJumpHooks;
    private ServerPlayerEntityBase[] afterJumpHooks;
    public boolean isJumpModded;
    private static final Map<String, String[]> allBaseBeforeJumpSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeJumpInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideJumpSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideJumpInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterJumpSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterJumpInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void knockBack(IServerPlayerEntity target, Entity entity, float strength, double xRatio, double zRatio)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isKnockBackModded) {
            serverPlayerAPI.knockBack(entity, strength, xRatio, zRatio);
        } else {
            target.superKnockBack(entity, strength, xRatio, zRatio);
        }
    }

    private void knockBack(Entity entity, float strength, double xRatio, double zRatio)
    {
        if (this.beforeKnockBackHooks != null) {
            for (int i = this.beforeKnockBackHooks.length - 1; i >= 0; i--) {
                this.beforeKnockBackHooks[i].beforeKnockBack(entity, strength, xRatio, zRatio);
            }
        }

        if (this.overrideKnockBackHooks != null) {
            this.overrideKnockBackHooks[this.overrideKnockBackHooks.length - 1].knockBack(entity, strength, xRatio, zRatio);
        } else {
            this.player.superKnockBack(entity, strength, xRatio, zRatio);
        }

        if (this.afterKnockBackHooks != null) {
            for (ServerPlayerEntityBase afterKnockBackHook : this.afterKnockBackHooks) {
                afterKnockBackHook.afterKnockBack(entity, strength, xRatio, zRatio);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenKnockBack(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideKnockBackHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideKnockBackHooks.length; i++) {
            if (this.overrideKnockBackHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideKnockBackHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeKnockBackHookTypes = new LinkedList<>();
    private final static List<String> overrideKnockBackHookTypes = new LinkedList<>();
    private final static List<String> afterKnockBackHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeKnockBackHooks;
    private ServerPlayerEntityBase[] overrideKnockBackHooks;
    private ServerPlayerEntityBase[] afterKnockBackHooks;
    public boolean isKnockBackModded;
    private static final Map<String, String[]> allBaseBeforeKnockBackSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeKnockBackInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideKnockBackSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideKnockBackInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterKnockBackSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterKnockBackInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void livingTick(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isLivingTickModded) {
            serverPlayerAPI.livingTick();
        } else {
            target.superLivingTick();
        }
    }

    private void livingTick()
    {
        if (this.beforeLivingTickHooks != null) {
            for (int i = this.beforeLivingTickHooks.length - 1; i >= 0; i--) {
                this.beforeLivingTickHooks[i].beforeLivingTick();
            }
        }

        if (this.overrideLivingTickHooks != null) {
            this.overrideLivingTickHooks[this.overrideLivingTickHooks.length - 1].livingTick();
        } else {
            this.player.superLivingTick();
        }

        if (this.afterLivingTickHooks != null) {
            for (ServerPlayerEntityBase afterLivingTickHook : this.afterLivingTickHooks) {
                afterLivingTickHook.afterLivingTick();
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenLivingTick(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideLivingTickHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideLivingTickHooks.length; i++) {
            if (this.overrideLivingTickHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideLivingTickHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeLivingTickHookTypes = new LinkedList<>();
    private final static List<String> overrideLivingTickHookTypes = new LinkedList<>();
    private final static List<String> afterLivingTickHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeLivingTickHooks;
    private ServerPlayerEntityBase[] overrideLivingTickHooks;
    private ServerPlayerEntityBase[] afterLivingTickHooks;
    public boolean isLivingTickModded;
    private static final Map<String, String[]> allBaseBeforeLivingTickSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeLivingTickInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideLivingTickSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideLivingTickInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterLivingTickSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterLivingTickInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void move(IServerPlayerEntity target, MoverType type, Vec3d pos)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isMoveModded) {
            serverPlayerAPI.move(type, pos);
        } else {
            target.superMove(type, pos);
        }
    }

    private void move(MoverType type, Vec3d pos)
    {
        if (this.beforeMoveHooks != null) {
            for (int i = this.beforeMoveHooks.length - 1; i >= 0; i--) {
                this.beforeMoveHooks[i].beforeMove(type, pos);
            }
        }

        if (this.overrideMoveHooks != null) {
            this.overrideMoveHooks[this.overrideMoveHooks.length - 1].move(type, pos);
        } else {
            this.player.superMove(type, pos);
        }

        if (this.afterMoveHooks != null) {
            for (ServerPlayerEntityBase afterMoveHook : this.afterMoveHooks) {
                afterMoveHook.afterMove(type, pos);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenMove(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideMoveHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideMoveHooks.length; i++) {
            if (this.overrideMoveHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideMoveHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeMoveHookTypes = new LinkedList<>();
    private final static List<String> overrideMoveHookTypes = new LinkedList<>();
    private final static List<String> afterMoveHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeMoveHooks;
    private ServerPlayerEntityBase[] overrideMoveHooks;
    private ServerPlayerEntityBase[] afterMoveHooks;
    public boolean isMoveModded;
    private static final Map<String, String[]> allBaseBeforeMoveSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeMoveInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideMoveSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideMoveInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterMoveSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterMoveInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void moveRelative(IServerPlayerEntity target, float friction, Vec3d relative)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isMoveRelativeModded) {
            serverPlayerAPI.moveRelative(friction, relative);
        } else {
            target.superMoveRelative(friction, relative);
        }
    }

    private void moveRelative(float friction, Vec3d relative)
    {
        if (this.beforeMoveRelativeHooks != null) {
            for (int i = this.beforeMoveRelativeHooks.length - 1; i >= 0; i--) {
                this.beforeMoveRelativeHooks[i].beforeMoveRelative(friction, relative);
            }
        }

        if (this.overrideMoveRelativeHooks != null) {
            this.overrideMoveRelativeHooks[this.overrideMoveRelativeHooks.length - 1].moveRelative(friction, relative);
        } else {
            this.player.superMoveRelative(friction, relative);
        }

        if (this.afterMoveRelativeHooks != null) {
            for (ServerPlayerEntityBase afterMoveRelativeHook : this.afterMoveRelativeHooks) {
                afterMoveRelativeHook.afterMoveRelative(friction, relative);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenMoveRelative(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideMoveRelativeHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideMoveRelativeHooks.length; i++) {
            if (this.overrideMoveRelativeHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideMoveRelativeHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeMoveRelativeHookTypes = new LinkedList<>();
    private final static List<String> overrideMoveRelativeHookTypes = new LinkedList<>();
    private final static List<String> afterMoveRelativeHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeMoveRelativeHooks;
    private ServerPlayerEntityBase[] overrideMoveRelativeHooks;
    private ServerPlayerEntityBase[] afterMoveRelativeHooks;
    public boolean isMoveRelativeModded;
    private static final Map<String, String[]> allBaseBeforeMoveRelativeSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeMoveRelativeInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideMoveRelativeSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideMoveRelativeInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterMoveRelativeSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterMoveRelativeInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforeOnDeath(CallbackInfo callbackInfo, IServerPlayerEntity target, DamageSource cause)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isOnDeathModded) {
            serverPlayerAPI.beforeOnDeath(callbackInfo, cause);
        }
    }

    private void beforeOnDeath(CallbackInfo callbackInfo, DamageSource cause)
    {
        if (this.beforeOnDeathHooks != null) {
            for (int i = this.beforeOnDeathHooks.length - 1; i >= 0; i--) {
                this.beforeOnDeathHooks[i].beforeOnDeath(cause);
            }
        }

        if (this.overrideOnDeathHooks != null) {
            this.overrideOnDeathHooks[this.overrideOnDeathHooks.length - 1].onDeath(cause);
            callbackInfo.cancel();
        }
    }

    public static void afterOnDeath(IServerPlayerEntity target, DamageSource cause)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isOnDeathModded) {
            serverPlayerAPI.afterOnDeath(cause);
        }
    }

    private void afterOnDeath(DamageSource cause)
    {
        if (this.afterOnDeathHooks != null) {
            for (ServerPlayerEntityBase afterOnDeathHook : this.afterOnDeathHooks) {
                afterOnDeathHook.afterOnDeath(cause);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenOnDeath(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideOnDeathHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideOnDeathHooks.length; i++) {
            if (this.overrideOnDeathHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideOnDeathHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeOnDeathHookTypes = new LinkedList<>();
    private final static List<String> overrideOnDeathHookTypes = new LinkedList<>();
    private final static List<String> afterOnDeathHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeOnDeathHooks;
    private ServerPlayerEntityBase[] overrideOnDeathHooks;
    private ServerPlayerEntityBase[] afterOnDeathHooks;
    public boolean isOnDeathModded;
    private static final Map<String, String[]> allBaseBeforeOnDeathSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeOnDeathInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideOnDeathSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideOnDeathInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterOnDeathSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterOnDeathInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void onKillEntity(IServerPlayerEntity target, LivingEntity entity)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isOnKillEntityModded) {
            serverPlayerAPI.onKillEntity(entity);
        } else {
            target.superOnKillEntity(entity);
        }
    }

    private void onKillEntity(LivingEntity entity)
    {
        if (this.beforeOnKillEntityHooks != null) {
            for (int i = this.beforeOnKillEntityHooks.length - 1; i >= 0; i--) {
                this.beforeOnKillEntityHooks[i].beforeOnKillEntity(entity);
            }
        }

        if (this.overrideOnKillEntityHooks != null) {
            this.overrideOnKillEntityHooks[this.overrideOnKillEntityHooks.length - 1].onKillEntity(entity);
        } else {
            this.player.superOnKillEntity(entity);
        }

        if (this.afterOnKillEntityHooks != null) {
            for (ServerPlayerEntityBase afterOnKillEntityHook : this.afterOnKillEntityHooks) {
                afterOnKillEntityHook.afterOnKillEntity(entity);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenOnKillEntity(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideOnKillEntityHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideOnKillEntityHooks.length; i++) {
            if (this.overrideOnKillEntityHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideOnKillEntityHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeOnKillEntityHookTypes = new LinkedList<>();
    private final static List<String> overrideOnKillEntityHookTypes = new LinkedList<>();
    private final static List<String> afterOnKillEntityHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeOnKillEntityHooks;
    private ServerPlayerEntityBase[] overrideOnKillEntityHooks;
    private ServerPlayerEntityBase[] afterOnKillEntityHooks;
    public boolean isOnKillEntityModded;
    private static final Map<String, String[]> allBaseBeforeOnKillEntitySuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeOnKillEntityInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideOnKillEntitySuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideOnKillEntityInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterOnKillEntitySuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterOnKillEntityInferiors = new Hashtable<>(0);

    // ############################################################################

    public static boolean onLivingFall(IServerPlayerEntity target, float distance, float damageMultiplier)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isOnLivingFallModded) {
            return serverPlayerAPI.onLivingFall(distance, damageMultiplier);
        } else {
            return target.superOnLivingFall(distance, damageMultiplier);
        }
    }

    private boolean onLivingFall(float distance, float damageMultiplier)
    {
        if (this.beforeOnLivingFallHooks != null) {
            for (int i = this.beforeOnLivingFallHooks.length - 1; i >= 0; i--) {
                this.beforeOnLivingFallHooks[i].beforeOnLivingFall(distance, damageMultiplier);
            }
        }

        boolean result;
        if (this.overrideOnLivingFallHooks != null) {
            result = this.overrideOnLivingFallHooks[this.overrideOnLivingFallHooks.length - 1].onLivingFall(distance, damageMultiplier);
        } else {
            result = this.player.superOnLivingFall(distance, damageMultiplier);
        }

        if (this.afterOnLivingFallHooks != null) {
            for (ServerPlayerEntityBase afterOnLivingFallHook : this.afterOnLivingFallHooks) {
                afterOnLivingFallHook.afterOnLivingFall(distance, damageMultiplier);
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenOnLivingFall(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideOnLivingFallHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideOnLivingFallHooks.length; i++) {
            if (this.overrideOnLivingFallHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideOnLivingFallHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeOnLivingFallHookTypes = new LinkedList<>();
    private final static List<String> overrideOnLivingFallHookTypes = new LinkedList<>();
    private final static List<String> afterOnLivingFallHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeOnLivingFallHooks;
    private ServerPlayerEntityBase[] overrideOnLivingFallHooks;
    private ServerPlayerEntityBase[] afterOnLivingFallHooks;
    public boolean isOnLivingFallModded;
    private static final Map<String, String[]> allBaseBeforeOnLivingFallSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeOnLivingFallInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideOnLivingFallSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideOnLivingFallInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterOnLivingFallSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterOnLivingFallInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void onStruckByLightning(IServerPlayerEntity target, LightningBoltEntity lightningBolt)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isOnStruckByLightningModded) {
            serverPlayerAPI.onStruckByLightning(lightningBolt);
        } else {
            target.superOnStruckByLightning(lightningBolt);
        }
    }

    private void onStruckByLightning(LightningBoltEntity lightningBolt)
    {
        if (this.beforeOnStruckByLightningHooks != null) {
            for (int i = this.beforeOnStruckByLightningHooks.length - 1; i >= 0; i--) {
                this.beforeOnStruckByLightningHooks[i].beforeOnStruckByLightning(lightningBolt);
            }
        }

        if (this.overrideOnStruckByLightningHooks != null) {
            this.overrideOnStruckByLightningHooks[this.overrideOnStruckByLightningHooks.length - 1].onStruckByLightning(lightningBolt);
        } else {
            this.player.superOnStruckByLightning(lightningBolt);
        }

        if (this.afterOnStruckByLightningHooks != null) {
            for (ServerPlayerEntityBase afterOnStruckByLightningHook : this.afterOnStruckByLightningHooks) {
                afterOnStruckByLightningHook.afterOnStruckByLightning(lightningBolt);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenOnStruckByLightning(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideOnStruckByLightningHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideOnStruckByLightningHooks.length; i++) {
            if (this.overrideOnStruckByLightningHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideOnStruckByLightningHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeOnStruckByLightningHookTypes = new LinkedList<>();
    private final static List<String> overrideOnStruckByLightningHookTypes = new LinkedList<>();
    private final static List<String> afterOnStruckByLightningHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeOnStruckByLightningHooks;
    private ServerPlayerEntityBase[] overrideOnStruckByLightningHooks;
    private ServerPlayerEntityBase[] afterOnStruckByLightningHooks;
    public boolean isOnStruckByLightningModded;
    private static final Map<String, String[]> allBaseBeforeOnStruckByLightningSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeOnStruckByLightningInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideOnStruckByLightningSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideOnStruckByLightningInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterOnStruckByLightningSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterOnStruckByLightningInferiors = new Hashtable<>(0);

    // ############################################################################

    public static RayTraceResult pick(IServerPlayerEntity target, double blockReachDistance, float partialTicks, boolean anyFluid)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isPickModded) {
            return serverPlayerAPI.pick(blockReachDistance, partialTicks, anyFluid);
        } else {
            return target.superPick(blockReachDistance, partialTicks, anyFluid);
        }
    }

    private RayTraceResult pick(double blockReachDistance, float partialTicks, boolean anyFluid)
    {
        if (this.beforePickHooks != null) {
            for (int i = this.beforePickHooks.length - 1; i >= 0; i--) {
                this.beforePickHooks[i].beforePick(blockReachDistance, partialTicks, anyFluid);
            }
        }

        RayTraceResult result;
        if (this.overridePickHooks != null) {
            result = this.overridePickHooks[this.overridePickHooks.length - 1].pick(blockReachDistance, partialTicks, anyFluid);
        } else {
            result = this.player.superPick(blockReachDistance, partialTicks, anyFluid);
        }

        if (this.afterPickHooks != null) {
            for (ServerPlayerEntityBase afterPickHook : this.afterPickHooks) {
                afterPickHook.afterPick(blockReachDistance, partialTicks, anyFluid);
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenPick(ServerPlayerEntityBase overwriter)
    {
        if (this.overridePickHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overridePickHooks.length; i++) {
            if (this.overridePickHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overridePickHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforePickHookTypes = new LinkedList<>();
    private final static List<String> overridePickHookTypes = new LinkedList<>();
    private final static List<String> afterPickHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforePickHooks;
    private ServerPlayerEntityBase[] overridePickHooks;
    private ServerPlayerEntityBase[] afterPickHooks;
    public boolean isPickModded;
    private static final Map<String, String[]> allBaseBeforePickSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforePickInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverridePickSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverridePickInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterPickSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterPickInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforePlayerTick(CallbackInfo callbackInfo, IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isPlayerTickModded) {
            serverPlayerAPI.beforePlayerTick(callbackInfo);
        }
    }

    private void beforePlayerTick(CallbackInfo callbackInfo)
    {
        if (this.beforePlayerTickHooks != null) {
            for (int i = this.beforePlayerTickHooks.length - 1; i >= 0; i--) {
                this.beforePlayerTickHooks[i].beforePlayerTick();
            }
        }

        if (this.overridePlayerTickHooks != null) {
            this.overridePlayerTickHooks[this.overridePlayerTickHooks.length - 1].playerTick();
            callbackInfo.cancel();
        }
    }

    public static void afterPlayerTick(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isPlayerTickModded) {
            serverPlayerAPI.afterPlayerTick();
        }
    }

    private void afterPlayerTick()
    {
        if (this.afterPlayerTickHooks != null) {
            for (ServerPlayerEntityBase afterPlayerTickHook : this.afterPlayerTickHooks) {
                afterPlayerTickHook.afterPlayerTick();
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenPlayerTick(ServerPlayerEntityBase overwriter)
    {
        if (this.overridePlayerTickHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overridePlayerTickHooks.length; i++) {
            if (this.overridePlayerTickHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overridePlayerTickHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforePlayerTickHookTypes = new LinkedList<>();
    private final static List<String> overridePlayerTickHookTypes = new LinkedList<>();
    private final static List<String> afterPlayerTickHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforePlayerTickHooks;
    private ServerPlayerEntityBase[] overridePlayerTickHooks;
    private ServerPlayerEntityBase[] afterPlayerTickHooks;
    public boolean isPlayerTickModded;
    private static final Map<String, String[]> allBaseBeforePlayerTickSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforePlayerTickInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverridePlayerTickSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverridePlayerTickInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterPlayerTickSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterPlayerTickInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void playStepSound(IServerPlayerEntity target, BlockPos pos, BlockState block)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isPlayStepSoundModded) {
            serverPlayerAPI.playStepSound(pos, block);
        } else {
            target.superPlayStepSound(pos, block);
        }
    }

    private void playStepSound(BlockPos pos, BlockState block)
    {
        if (this.beforePlayStepSoundHooks != null) {
            for (int i = this.beforePlayStepSoundHooks.length - 1; i >= 0; i--) {
                this.beforePlayStepSoundHooks[i].beforePlayStepSound(pos, block);
            }
        }

        if (this.overridePlayStepSoundHooks != null) {
            this.overridePlayStepSoundHooks[this.overridePlayStepSoundHooks.length - 1].playStepSound(pos, block);
        } else {
            this.player.superPlayStepSound(pos, block);
        }

        if (this.afterPlayStepSoundHooks != null) {
            for (ServerPlayerEntityBase afterPlayStepSoundHook : this.afterPlayStepSoundHooks) {
                afterPlayStepSoundHook.afterPlayStepSound(pos, block);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenPlayStepSound(ServerPlayerEntityBase overwriter)
    {
        if (this.overridePlayStepSoundHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overridePlayStepSoundHooks.length; i++) {
            if (this.overridePlayStepSoundHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overridePlayStepSoundHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforePlayStepSoundHookTypes = new LinkedList<>();
    private final static List<String> overridePlayStepSoundHookTypes = new LinkedList<>();
    private final static List<String> afterPlayStepSoundHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforePlayStepSoundHooks;
    private ServerPlayerEntityBase[] overridePlayStepSoundHooks;
    private ServerPlayerEntityBase[] afterPlayStepSoundHooks;
    public boolean isPlayStepSoundModded;
    private static final Map<String, String[]> allBaseBeforePlayStepSoundSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforePlayStepSoundInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverridePlayStepSoundSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverridePlayStepSoundInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterPlayStepSoundSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterPlayStepSoundInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void pushOutOfBlocks(IServerPlayerEntity target, double x, double y, double z)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isPushOutOfBlocksModded) {
            serverPlayerAPI.pushOutOfBlocks(x, y, z);
        } else {
            target.superPushOutOfBlocks(x, y, z);
        }
    }

    private void pushOutOfBlocks(double x, double y, double z)
    {
        if (this.beforePushOutOfBlocksHooks != null) {
            for (int i = this.beforePushOutOfBlocksHooks.length - 1; i >= 0; i--) {
                this.beforePushOutOfBlocksHooks[i].beforePushOutOfBlocks(x, y, z);
            }
        }

        if (this.overridePushOutOfBlocksHooks != null) {
            this.overridePushOutOfBlocksHooks[this.overridePushOutOfBlocksHooks.length - 1].pushOutOfBlocks(x, y, z);
        } else {
            this.player.superPushOutOfBlocks(x, y, z);
        }

        if (this.afterPushOutOfBlocksHooks != null) {
            for (ServerPlayerEntityBase afterPushOutOfBlocksHook : this.afterPushOutOfBlocksHooks) {
                afterPushOutOfBlocksHook.afterPushOutOfBlocks(x, y, z);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenPushOutOfBlocks(ServerPlayerEntityBase overwriter)
    {
        if (this.overridePushOutOfBlocksHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overridePushOutOfBlocksHooks.length; i++) {
            if (this.overridePushOutOfBlocksHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overridePushOutOfBlocksHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforePushOutOfBlocksHookTypes = new LinkedList<>();
    private final static List<String> overridePushOutOfBlocksHookTypes = new LinkedList<>();
    private final static List<String> afterPushOutOfBlocksHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforePushOutOfBlocksHooks;
    private ServerPlayerEntityBase[] overridePushOutOfBlocksHooks;
    private ServerPlayerEntityBase[] afterPushOutOfBlocksHooks;
    public boolean isPushOutOfBlocksModded;
    private static final Map<String, String[]> allBaseBeforePushOutOfBlocksSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforePushOutOfBlocksInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverridePushOutOfBlocksSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverridePushOutOfBlocksInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterPushOutOfBlocksSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterPushOutOfBlocksInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void read(IServerPlayerEntity target, CompoundNBT compound)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isReadModded) {
            serverPlayerAPI.read(compound);
        } else {
            target.superRead(compound);
        }
    }

    private void read(CompoundNBT compound)
    {
        if (this.beforeReadHooks != null) {
            for (int i = this.beforeReadHooks.length - 1; i >= 0; i--) {
                this.beforeReadHooks[i].beforeRead(compound);
            }
        }

        if (this.overrideReadHooks != null) {
            this.overrideReadHooks[this.overrideReadHooks.length - 1].read(compound);
        } else {
            this.player.superRead(compound);
        }

        if (this.afterReadHooks != null) {
            for (ServerPlayerEntityBase afterReadHook : this.afterReadHooks) {
                afterReadHook.afterRead(compound);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenRead(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideReadHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideReadHooks.length; i++) {
            if (this.overrideReadHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideReadHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeReadHookTypes = new LinkedList<>();
    private final static List<String> overrideReadHookTypes = new LinkedList<>();
    private final static List<String> afterReadHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeReadHooks;
    private ServerPlayerEntityBase[] overrideReadHooks;
    private ServerPlayerEntityBase[] afterReadHooks;
    public boolean isReadModded;
    private static final Map<String, String[]> allBaseBeforeReadSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeReadInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideReadSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideReadInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterReadSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterReadInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void remove(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isRemoveModded) {
            serverPlayerAPI.remove();
        } else {
            target.superRemove();
        }
    }

    private void remove()
    {
        if (this.beforeRemoveHooks != null) {
            for (int i = this.beforeRemoveHooks.length - 1; i >= 0; i--) {
                this.beforeRemoveHooks[i].beforeRemove();
            }
        }

        if (this.overrideRemoveHooks != null) {
            this.overrideRemoveHooks[this.overrideRemoveHooks.length - 1].remove();
        } else {
            this.player.superRemove();
        }

        if (this.afterRemoveHooks != null) {
            for (ServerPlayerEntityBase afterRemoveHook : this.afterRemoveHooks) {
                afterRemoveHook.afterRemove();
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenRemove(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideRemoveHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideRemoveHooks.length; i++) {
            if (this.overrideRemoveHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideRemoveHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeRemoveHookTypes = new LinkedList<>();
    private final static List<String> overrideRemoveHookTypes = new LinkedList<>();
    private final static List<String> afterRemoveHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeRemoveHooks;
    private ServerPlayerEntityBase[] overrideRemoveHooks;
    private ServerPlayerEntityBase[] afterRemoveHooks;
    public boolean isRemoveModded;
    private static final Map<String, String[]> allBaseBeforeRemoveSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeRemoveInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideRemoveSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideRemoveInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterRemoveSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterRemoveInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforeSetEntityActionState(CallbackInfo callbackInfo, IServerPlayerEntity target, float strafe, float forward, boolean jumping, boolean sneaking)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isSetEntityActionStateModded) {
            serverPlayerAPI.beforeSetEntityActionState(callbackInfo, strafe, forward, jumping, sneaking);
        }
    }

    private void beforeSetEntityActionState(CallbackInfo callbackInfo, float strafe, float forward, boolean jumping, boolean sneaking)
    {
        if (this.beforeSetEntityActionStateHooks != null) {
            for (int i = this.beforeSetEntityActionStateHooks.length - 1; i >= 0; i--) {
                this.beforeSetEntityActionStateHooks[i].beforeSetEntityActionState(strafe, forward, jumping, sneaking);
            }
        }

        if (this.overrideSetEntityActionStateHooks != null) {
            this.overrideSetEntityActionStateHooks[this.overrideSetEntityActionStateHooks.length - 1].setEntityActionState(strafe, forward, jumping, sneaking);
            callbackInfo.cancel();
        }
    }

    public static void afterSetEntityActionState(IServerPlayerEntity target, float strafe, float forward, boolean jumping, boolean sneaking)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isSetEntityActionStateModded) {
            serverPlayerAPI.afterSetEntityActionState(strafe, forward, jumping, sneaking);
        }
    }

    private void afterSetEntityActionState(float strafe, float forward, boolean jumping, boolean sneaking)
    {
        if (this.afterSetEntityActionStateHooks != null) {
            for (ServerPlayerEntityBase afterSetEntityActionStateHook : this.afterSetEntityActionStateHooks) {
                afterSetEntityActionStateHook.afterSetEntityActionState(strafe, forward, jumping, sneaking);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenSetEntityActionState(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideSetEntityActionStateHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideSetEntityActionStateHooks.length; i++) {
            if (this.overrideSetEntityActionStateHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideSetEntityActionStateHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeSetEntityActionStateHookTypes = new LinkedList<>();
    private final static List<String> overrideSetEntityActionStateHookTypes = new LinkedList<>();
    private final static List<String> afterSetEntityActionStateHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeSetEntityActionStateHooks;
    private ServerPlayerEntityBase[] overrideSetEntityActionStateHooks;
    private ServerPlayerEntityBase[] afterSetEntityActionStateHooks;
    public boolean isSetEntityActionStateModded;
    private static final Map<String, String[]> allBaseBeforeSetEntityActionStateSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeSetEntityActionStateInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideSetEntityActionStateSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideSetEntityActionStateInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterSetEntityActionStateSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterSetEntityActionStateInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void setPosition(IServerPlayerEntity target, double x, double y, double z)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isSetPositionModded) {
            serverPlayerAPI.setPosition(x, y, z);
        } else {
            target.superSetPosition(x, y, z);
        }
    }

    private void setPosition(double x, double y, double z)
    {
        if (this.beforeSetPositionHooks != null) {
            for (int i = this.beforeSetPositionHooks.length - 1; i >= 0; i--) {
                this.beforeSetPositionHooks[i].beforeSetPosition(x, y, z);
            }
        }

        if (this.overrideSetPositionHooks != null) {
            this.overrideSetPositionHooks[this.overrideSetPositionHooks.length - 1].setPosition(x, y, z);
        } else {
            this.player.superSetPosition(x, y, z);
        }

        if (this.afterSetPositionHooks != null) {
            for (ServerPlayerEntityBase afterSetPositionHook : this.afterSetPositionHooks) {
                afterSetPositionHook.afterSetPosition(x, y, z);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenSetPosition(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideSetPositionHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideSetPositionHooks.length; i++) {
            if (this.overrideSetPositionHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideSetPositionHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeSetPositionHookTypes = new LinkedList<>();
    private final static List<String> overrideSetPositionHookTypes = new LinkedList<>();
    private final static List<String> afterSetPositionHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeSetPositionHooks;
    private ServerPlayerEntityBase[] overrideSetPositionHooks;
    private ServerPlayerEntityBase[] afterSetPositionHooks;
    public boolean isSetPositionModded;
    private static final Map<String, String[]> allBaseBeforeSetPositionSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeSetPositionInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideSetPositionSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideSetPositionInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterSetPositionSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterSetPositionInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void setPositionAndRotation(IServerPlayerEntity target, double x, double y, double z, float yaw, float pitch)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isSetPositionAndRotationModded) {
            serverPlayerAPI.setPositionAndRotation(x, y, z, yaw, pitch);
        } else {
            target.superSetPositionAndRotation(x, y, z, yaw, pitch);
        }
    }

    private void setPositionAndRotation(double x, double y, double z, float yaw, float pitch)
    {
        if (this.beforeSetPositionAndRotationHooks != null) {
            for (int i = this.beforeSetPositionAndRotationHooks.length - 1; i >= 0; i--) {
                this.beforeSetPositionAndRotationHooks[i].beforeSetPositionAndRotation(x, y, z, yaw, pitch);
            }
        }

        if (this.overrideSetPositionAndRotationHooks != null) {
            this.overrideSetPositionAndRotationHooks[this.overrideSetPositionAndRotationHooks.length - 1].setPositionAndRotation(x, y, z, yaw, pitch);
        } else {
            this.player.superSetPositionAndRotation(x, y, z, yaw, pitch);
        }

        if (this.afterSetPositionAndRotationHooks != null) {
            for (ServerPlayerEntityBase afterSetPositionAndRotationHook : this.afterSetPositionAndRotationHooks) {
                afterSetPositionAndRotationHook.afterSetPositionAndRotation(x, y, z, yaw, pitch);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenSetPositionAndRotation(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideSetPositionAndRotationHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideSetPositionAndRotationHooks.length; i++) {
            if (this.overrideSetPositionAndRotationHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideSetPositionAndRotationHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeSetPositionAndRotationHookTypes = new LinkedList<>();
    private final static List<String> overrideSetPositionAndRotationHookTypes = new LinkedList<>();
    private final static List<String> afterSetPositionAndRotationHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeSetPositionAndRotationHooks;
    private ServerPlayerEntityBase[] overrideSetPositionAndRotationHooks;
    private ServerPlayerEntityBase[] afterSetPositionAndRotationHooks;
    public boolean isSetPositionAndRotationModded;
    private static final Map<String, String[]> allBaseBeforeSetPositionAndRotationSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeSetPositionAndRotationInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideSetPositionAndRotationSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideSetPositionAndRotationInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterSetPositionAndRotationSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterSetPositionAndRotationInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void setSneaking(IServerPlayerEntity target, boolean sneaking)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isSetSneakingModded) {
            serverPlayerAPI.setSneaking(sneaking);
        } else {
            target.superSetSneaking(sneaking);
        }
    }

    private void setSneaking(boolean sneaking)
    {
        if (this.beforeSetSneakingHooks != null) {
            for (int i = this.beforeSetSneakingHooks.length - 1; i >= 0; i--) {
                this.beforeSetSneakingHooks[i].beforeSetSneaking(sneaking);
            }
        }

        if (this.overrideSetSneakingHooks != null) {
            this.overrideSetSneakingHooks[this.overrideSetSneakingHooks.length - 1].setSneaking(sneaking);
        } else {
            this.player.superSetSneaking(sneaking);
        }

        if (this.afterSetSneakingHooks != null) {
            for (ServerPlayerEntityBase afterSetSneakingHook : this.afterSetSneakingHooks) {
                afterSetSneakingHook.afterSetSneaking(sneaking);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenSetSneaking(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideSetSneakingHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideSetSneakingHooks.length; i++) {
            if (this.overrideSetSneakingHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideSetSneakingHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeSetSneakingHookTypes = new LinkedList<>();
    private final static List<String> overrideSetSneakingHookTypes = new LinkedList<>();
    private final static List<String> afterSetSneakingHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeSetSneakingHooks;
    private ServerPlayerEntityBase[] overrideSetSneakingHooks;
    private ServerPlayerEntityBase[] afterSetSneakingHooks;
    public boolean isSetSneakingModded;
    private static final Map<String, String[]> allBaseBeforeSetSneakingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeSetSneakingInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideSetSneakingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideSetSneakingInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterSetSneakingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterSetSneakingInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void setSprinting(IServerPlayerEntity target, boolean sprinting)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isSetSprintingModded) {
            serverPlayerAPI.setSprinting(sprinting);
        } else {
            target.superSetSprinting(sprinting);
        }
    }

    private void setSprinting(boolean sprinting)
    {
        if (this.beforeSetSprintingHooks != null) {
            for (int i = this.beforeSetSprintingHooks.length - 1; i >= 0; i--) {
                this.beforeSetSprintingHooks[i].beforeSetSprinting(sprinting);
            }
        }

        if (this.overrideSetSprintingHooks != null) {
            this.overrideSetSprintingHooks[this.overrideSetSprintingHooks.length - 1].setSprinting(sprinting);
        } else {
            this.player.superSetSprinting(sprinting);
        }

        if (this.afterSetSprintingHooks != null) {
            for (ServerPlayerEntityBase afterSetSprintingHook : this.afterSetSprintingHooks) {
                afterSetSprintingHook.afterSetSprinting(sprinting);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenSetSprinting(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideSetSprintingHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideSetSprintingHooks.length; i++) {
            if (this.overrideSetSprintingHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideSetSprintingHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeSetSprintingHookTypes = new LinkedList<>();
    private final static List<String> overrideSetSprintingHookTypes = new LinkedList<>();
    private final static List<String> afterSetSprintingHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeSetSprintingHooks;
    private ServerPlayerEntityBase[] overrideSetSprintingHooks;
    private ServerPlayerEntityBase[] afterSetSprintingHooks;
    public boolean isSetSprintingModded;
    private static final Map<String, String[]> allBaseBeforeSetSprintingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeSetSprintingInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideSetSprintingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideSetSprintingInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterSetSprintingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterSetSprintingInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforeStartRiding(CallbackInfoReturnable<Boolean> callbackInfo, IServerPlayerEntity target, Entity entity, boolean force)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isStartRidingModded) {
            serverPlayerAPI.beforeStartRiding(callbackInfo, entity, force);
        }
    }

    private void beforeStartRiding(CallbackInfoReturnable<Boolean> callbackInfo, Entity entity, boolean force)
    {
        if (this.beforeStartRidingHooks != null) {
            for (int i = this.beforeStartRidingHooks.length - 1; i >= 0; i--) {
                this.beforeStartRidingHooks[i].beforeStartRiding(entity, force);
            }
        }

        if (this.overrideStartRidingHooks != null) {
            callbackInfo.setReturnValue(this.overrideStartRidingHooks[this.overrideStartRidingHooks.length - 1].startRiding(entity, force));
            callbackInfo.cancel();
        }
    }

    public static void afterStartRiding(IServerPlayerEntity target, Entity entity, boolean force)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isStartRidingModded) {
            serverPlayerAPI.afterStartRiding(entity, force);
        }
    }

    private void afterStartRiding(Entity entity, boolean force)
    {
        if (this.afterStartRidingHooks != null) {
            for (ServerPlayerEntityBase afterStartRidingHook : this.afterStartRidingHooks) {
                afterStartRidingHook.afterStartRiding(entity, force);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenStartRiding(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideStartRidingHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideStartRidingHooks.length; i++) {
            if (this.overrideStartRidingHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideStartRidingHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeStartRidingHookTypes = new LinkedList<>();
    private final static List<String> overrideStartRidingHookTypes = new LinkedList<>();
    private final static List<String> afterStartRidingHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeStartRidingHooks;
    private ServerPlayerEntityBase[] overrideStartRidingHooks;
    private ServerPlayerEntityBase[] afterStartRidingHooks;
    public boolean isStartRidingModded;
    private static final Map<String, String[]> allBaseBeforeStartRidingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeStartRidingInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideStartRidingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideStartRidingInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterStartRidingSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterStartRidingInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforeSwingArm(CallbackInfo callbackInfo, IServerPlayerEntity target, Hand hand)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isSwingArmModded) {
            serverPlayerAPI.beforeSwingArm(callbackInfo, hand);
        }
    }

    private void beforeSwingArm(CallbackInfo callbackInfo, Hand hand)
    {
        if (this.beforeSwingArmHooks != null) {
            for (int i = this.beforeSwingArmHooks.length - 1; i >= 0; i--) {
                this.beforeSwingArmHooks[i].beforeSwingArm(hand);
            }
        }

        if (this.overrideSwingArmHooks != null) {
            this.overrideSwingArmHooks[this.overrideSwingArmHooks.length - 1].swingArm(hand);
            callbackInfo.cancel();
        }
    }

    public static void afterSwingArm(IServerPlayerEntity target, Hand hand)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isSwingArmModded) {
            serverPlayerAPI.afterSwingArm(hand);
        }
    }

    private void afterSwingArm(Hand hand)
    {
        if (this.afterSwingArmHooks != null) {
            for (ServerPlayerEntityBase afterSwingArmHook : this.afterSwingArmHooks) {
                afterSwingArmHook.afterSwingArm(hand);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenSwingArm(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideSwingArmHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideSwingArmHooks.length; i++) {
            if (this.overrideSwingArmHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideSwingArmHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeSwingArmHookTypes = new LinkedList<>();
    private final static List<String> overrideSwingArmHookTypes = new LinkedList<>();
    private final static List<String> afterSwingArmHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeSwingArmHooks;
    private ServerPlayerEntityBase[] overrideSwingArmHooks;
    private ServerPlayerEntityBase[] afterSwingArmHooks;
    public boolean isSwingArmModded;
    private static final Map<String, String[]> allBaseBeforeSwingArmSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeSwingArmInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideSwingArmSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideSwingArmInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterSwingArmSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterSwingArmInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforeTick(CallbackInfo callbackInfo, IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isTickModded) {
            serverPlayerAPI.beforeTick(callbackInfo);
        }
    }

    private void beforeTick(CallbackInfo callbackInfo)
    {
        if (this.beforeTickHooks != null) {
            for (int i = this.beforeTickHooks.length - 1; i >= 0; i--) {
                this.beforeTickHooks[i].beforeTick();
            }
        }

        if (this.overrideTickHooks != null) {
            this.overrideTickHooks[this.overrideTickHooks.length - 1].tick();
            callbackInfo.cancel();
        }
    }

    public static void afterTick(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isTickModded) {
            serverPlayerAPI.afterTick();
        }
    }

    private void afterTick()
    {
        if (this.afterTickHooks != null) {
            for (ServerPlayerEntityBase afterTickHook : this.afterTickHooks) {
                afterTickHook.afterTick();
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenTick(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideTickHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideTickHooks.length; i++) {
            if (this.overrideTickHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideTickHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeTickHookTypes = new LinkedList<>();
    private final static List<String> overrideTickHookTypes = new LinkedList<>();
    private final static List<String> afterTickHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeTickHooks;
    private ServerPlayerEntityBase[] overrideTickHooks;
    private ServerPlayerEntityBase[] afterTickHooks;
    public boolean isTickModded;
    private static final Map<String, String[]> allBaseBeforeTickSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeTickInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideTickSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideTickInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterTickSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterTickInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void travel(IServerPlayerEntity target, Vec3d pos)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isTravelModded) {
            serverPlayerAPI.travel(pos);
        } else {
            target.superTravel(pos);
        }
    }

    private void travel(Vec3d pos)
    {
        if (this.beforeTravelHooks != null) {
            for (int i = this.beforeTravelHooks.length - 1; i >= 0; i--) {
                this.beforeTravelHooks[i].beforeTravel(pos);
            }
        }

        if (this.overrideTravelHooks != null) {
            this.overrideTravelHooks[this.overrideTravelHooks.length - 1].travel(pos);
        } else {
            this.player.superTravel(pos);
        }

        if (this.afterTravelHooks != null) {
            for (ServerPlayerEntityBase afterTravelHook : this.afterTravelHooks) {
                afterTravelHook.afterTravel(pos);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenTravel(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideTravelHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideTravelHooks.length; i++) {
            if (this.overrideTravelHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideTravelHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeTravelHookTypes = new LinkedList<>();
    private final static List<String> overrideTravelHookTypes = new LinkedList<>();
    private final static List<String> afterTravelHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeTravelHooks;
    private ServerPlayerEntityBase[] overrideTravelHooks;
    private ServerPlayerEntityBase[] afterTravelHooks;
    public boolean isTravelModded;
    private static final Map<String, String[]> allBaseBeforeTravelSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeTravelInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideTravelSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideTravelInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterTravelSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterTravelInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforeTrySleep(CallbackInfoReturnable<Either<PlayerEntity.SleepResult, Unit>> callbackInfo, IServerPlayerEntity target, BlockPos at)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isTrySleepModded) {
            serverPlayerAPI.beforeTrySleep(callbackInfo, at);
        }
    }

    private void beforeTrySleep(CallbackInfoReturnable<Either<PlayerEntity.SleepResult, Unit>> callbackInfo, BlockPos at)
    {
        if (this.beforeTrySleepHooks != null) {
            for (int i = this.beforeTrySleepHooks.length - 1; i >= 0; i--) {
                this.beforeTrySleepHooks[i].beforeTrySleep(at);
            }
        }

        if (this.overrideTrySleepHooks != null) {
            callbackInfo.setReturnValue(this.overrideTrySleepHooks[this.overrideTrySleepHooks.length - 1].trySleep(at));
            callbackInfo.cancel();
        }
    }

    public static void afterTrySleep(IServerPlayerEntity target, BlockPos at)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isTrySleepModded) {
            serverPlayerAPI.afterTrySleep(at);
        }
    }

    private void afterTrySleep(BlockPos at)
    {
        if (this.afterTrySleepHooks != null) {
            for (ServerPlayerEntityBase afterTrySleepHook : this.afterTrySleepHooks) {
                afterTrySleepHook.afterTrySleep(at);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenTrySleep(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideTrySleepHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideTrySleepHooks.length; i++) {
            if (this.overrideTrySleepHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideTrySleepHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeTrySleepHookTypes = new LinkedList<>();
    private final static List<String> overrideTrySleepHookTypes = new LinkedList<>();
    private final static List<String> afterTrySleepHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeTrySleepHooks;
    private ServerPlayerEntityBase[] overrideTrySleepHooks;
    private ServerPlayerEntityBase[] afterTrySleepHooks;
    public boolean isTrySleepModded;
    private static final Map<String, String[]> allBaseBeforeTrySleepSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeTrySleepInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideTrySleepSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideTrySleepInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterTrySleepSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterTrySleepInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void updateEntityActionState(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isUpdateEntityActionStateModded) {
            serverPlayerAPI.updateEntityActionState();
        } else {
            target.superUpdateEntityActionState();
        }
    }

    private void updateEntityActionState()
    {
        if (this.beforeUpdateEntityActionStateHooks != null) {
            for (int i = this.beforeUpdateEntityActionStateHooks.length - 1; i >= 0; i--) {
                this.beforeUpdateEntityActionStateHooks[i].beforeUpdateEntityActionState();
            }
        }

        if (this.overrideUpdateEntityActionStateHooks != null) {
            this.overrideUpdateEntityActionStateHooks[this.overrideUpdateEntityActionStateHooks.length - 1].updateEntityActionState();
        } else {
            this.player.superUpdateEntityActionState();
        }

        if (this.afterUpdateEntityActionStateHooks != null) {
            for (ServerPlayerEntityBase afterUpdateEntityActionStateHook : this.afterUpdateEntityActionStateHooks) {
                afterUpdateEntityActionStateHook.afterUpdateEntityActionState();
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenUpdateEntityActionState(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideUpdateEntityActionStateHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideUpdateEntityActionStateHooks.length; i++) {
            if (this.overrideUpdateEntityActionStateHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideUpdateEntityActionStateHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeUpdateEntityActionStateHookTypes = new LinkedList<>();
    private final static List<String> overrideUpdateEntityActionStateHookTypes = new LinkedList<>();
    private final static List<String> afterUpdateEntityActionStateHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeUpdateEntityActionStateHooks;
    private ServerPlayerEntityBase[] overrideUpdateEntityActionStateHooks;
    private ServerPlayerEntityBase[] afterUpdateEntityActionStateHooks;
    public boolean isUpdateEntityActionStateModded;
    private static final Map<String, String[]> allBaseBeforeUpdateEntityActionStateSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeUpdateEntityActionStateInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideUpdateEntityActionStateSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideUpdateEntityActionStateInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterUpdateEntityActionStateSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterUpdateEntityActionStateInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void updatePotionEffects(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isUpdatePotionEffectsModded) {
            serverPlayerAPI.updatePotionEffects();
        } else {
            target.superUpdatePotionEffects();
        }
    }

    private void updatePotionEffects()
    {
        if (this.beforeUpdatePotionEffectsHooks != null) {
            for (int i = this.beforeUpdatePotionEffectsHooks.length - 1; i >= 0; i--) {
                this.beforeUpdatePotionEffectsHooks[i].beforeUpdatePotionEffects();
            }
        }

        if (this.overrideUpdatePotionEffectsHooks != null) {
            this.overrideUpdatePotionEffectsHooks[this.overrideUpdatePotionEffectsHooks.length - 1].updatePotionEffects();
        } else {
            this.player.superUpdatePotionEffects();
        }

        if (this.afterUpdatePotionEffectsHooks != null) {
            for (ServerPlayerEntityBase afterUpdatePotionEffectsHook : this.afterUpdatePotionEffectsHooks) {
                afterUpdatePotionEffectsHook.afterUpdatePotionEffects();
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenUpdatePotionEffects(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideUpdatePotionEffectsHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideUpdatePotionEffectsHooks.length; i++) {
            if (this.overrideUpdatePotionEffectsHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideUpdatePotionEffectsHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeUpdatePotionEffectsHookTypes = new LinkedList<>();
    private final static List<String> overrideUpdatePotionEffectsHookTypes = new LinkedList<>();
    private final static List<String> afterUpdatePotionEffectsHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeUpdatePotionEffectsHooks;
    private ServerPlayerEntityBase[] overrideUpdatePotionEffectsHooks;
    private ServerPlayerEntityBase[] afterUpdatePotionEffectsHooks;
    public boolean isUpdatePotionEffectsModded;
    private static final Map<String, String[]> allBaseBeforeUpdatePotionEffectsSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeUpdatePotionEffectsInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideUpdatePotionEffectsSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideUpdatePotionEffectsInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterUpdatePotionEffectsSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterUpdatePotionEffectsInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void updateRidden(IServerPlayerEntity target)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isUpdateRiddenModded) {
            serverPlayerAPI.updateRidden();
        } else {
            target.superUpdateRidden();
        }
    }

    private void updateRidden()
    {
        if (this.beforeUpdateRiddenHooks != null) {
            for (int i = this.beforeUpdateRiddenHooks.length - 1; i >= 0; i--) {
                this.beforeUpdateRiddenHooks[i].beforeUpdateRidden();
            }
        }

        if (this.overrideUpdateRiddenHooks != null) {
            this.overrideUpdateRiddenHooks[this.overrideUpdateRiddenHooks.length - 1].updateRidden();
        } else {
            this.player.superUpdateRidden();
        }

        if (this.afterUpdateRiddenHooks != null) {
            for (ServerPlayerEntityBase afterUpdateRiddenHook : this.afterUpdateRiddenHooks) {
                afterUpdateRiddenHook.afterUpdateRidden();
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenUpdateRidden(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideUpdateRiddenHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideUpdateRiddenHooks.length; i++) {
            if (this.overrideUpdateRiddenHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideUpdateRiddenHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeUpdateRiddenHookTypes = new LinkedList<>();
    private final static List<String> overrideUpdateRiddenHookTypes = new LinkedList<>();
    private final static List<String> afterUpdateRiddenHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeUpdateRiddenHooks;
    private ServerPlayerEntityBase[] overrideUpdateRiddenHooks;
    private ServerPlayerEntityBase[] afterUpdateRiddenHooks;
    public boolean isUpdateRiddenModded;
    private static final Map<String, String[]> allBaseBeforeUpdateRiddenSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeUpdateRiddenInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideUpdateRiddenSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideUpdateRiddenInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterUpdateRiddenSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterUpdateRiddenInferiors = new Hashtable<>(0);

    // ############################################################################

    public static void beforeWakeUpPlayer(CallbackInfo callbackInfo, IServerPlayerEntity target, boolean immediately, boolean updateWorldFlag)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isWakeUpPlayerModded) {
            serverPlayerAPI.beforeWakeUpPlayer(callbackInfo, immediately, updateWorldFlag);
        }
    }

    private void beforeWakeUpPlayer(CallbackInfo callbackInfo, boolean immediately, boolean updateWorldFlag)
    {
        if (this.beforeWakeUpPlayerHooks != null) {
            for (int i = this.beforeWakeUpPlayerHooks.length - 1; i >= 0; i--) {
                this.beforeWakeUpPlayerHooks[i].beforeWakeUpPlayer(immediately, updateWorldFlag);
            }
        }

        if (this.overrideWakeUpPlayerHooks != null) {
            this.overrideWakeUpPlayerHooks[this.overrideWakeUpPlayerHooks.length - 1].wakeUpPlayer(immediately, updateWorldFlag);
            callbackInfo.cancel();
        }
    }

    public static void afterWakeUpPlayer(IServerPlayerEntity target, boolean immediately, boolean updateWorldFlag)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isWakeUpPlayerModded) {
            serverPlayerAPI.afterWakeUpPlayer(immediately, updateWorldFlag);
        }
    }

    private void afterWakeUpPlayer(boolean immediately, boolean updateWorldFlag)
    {
        if (this.afterWakeUpPlayerHooks != null) {
            for (ServerPlayerEntityBase afterWakeUpPlayerHook : this.afterWakeUpPlayerHooks) {
                afterWakeUpPlayerHook.afterWakeUpPlayer(immediately, updateWorldFlag);
            }
        }
    }

    protected ServerPlayerEntityBase getOverwrittenWakeUpPlayer(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideWakeUpPlayerHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideWakeUpPlayerHooks.length; i++) {
            if (this.overrideWakeUpPlayerHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideWakeUpPlayerHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeWakeUpPlayerHookTypes = new LinkedList<>();
    private final static List<String> overrideWakeUpPlayerHookTypes = new LinkedList<>();
    private final static List<String> afterWakeUpPlayerHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeWakeUpPlayerHooks;
    private ServerPlayerEntityBase[] overrideWakeUpPlayerHooks;
    private ServerPlayerEntityBase[] afterWakeUpPlayerHooks;
    public boolean isWakeUpPlayerModded;
    private static final Map<String, String[]> allBaseBeforeWakeUpPlayerSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeWakeUpPlayerInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideWakeUpPlayerSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideWakeUpPlayerInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterWakeUpPlayerSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterWakeUpPlayerInferiors = new Hashtable<>(0);

    // ############################################################################

    public static CompoundNBT writeWithoutTypeId(IServerPlayerEntity target, CompoundNBT compound)
    {
        ServerPlayerAPI serverPlayerAPI = target.getServerPlayerAPI();
        if (serverPlayerAPI != null && serverPlayerAPI.isWriteWithoutTypeIdModded) {
            return serverPlayerAPI.writeWithoutTypeId(compound);
        } else {
            return target.superWriteWithoutTypeId(compound);
        }
    }

    private CompoundNBT writeWithoutTypeId(CompoundNBT compound)
    {
        if (this.beforeWriteWithoutTypeIdHooks != null) {
            for (int i = this.beforeWriteWithoutTypeIdHooks.length - 1; i >= 0; i--) {
                this.beforeWriteWithoutTypeIdHooks[i].beforeWriteWithoutTypeId(compound);
            }
        }

        CompoundNBT result;
        if (this.overrideWriteWithoutTypeIdHooks != null) {
            result = this.overrideWriteWithoutTypeIdHooks[this.overrideWriteWithoutTypeIdHooks.length - 1].writeWithoutTypeId(compound);
        } else {
            result = this.player.superWriteWithoutTypeId(compound);
        }

        if (this.afterWriteWithoutTypeIdHooks != null) {
            for (ServerPlayerEntityBase afterWriteWithoutTypeIdHook : this.afterWriteWithoutTypeIdHooks) {
                afterWriteWithoutTypeIdHook.afterWriteWithoutTypeId(compound);
            }
        }

        return result;
    }

    protected ServerPlayerEntityBase getOverwrittenWriteWithoutTypeId(ServerPlayerEntityBase overwriter)
    {
        if (this.overrideWriteWithoutTypeIdHooks == null) {
            return overwriter;
        }

        for (int i = 0; i < this.overrideWriteWithoutTypeIdHooks.length; i++) {
            if (this.overrideWriteWithoutTypeIdHooks[i] == overwriter) {
                if (i == 0) {
                    return null;
                } else {
                    return this.overrideWriteWithoutTypeIdHooks[i - 1];
                }
            }
        }

        return overwriter;
    }

    private final static List<String> beforeWriteWithoutTypeIdHookTypes = new LinkedList<>();
    private final static List<String> overrideWriteWithoutTypeIdHookTypes = new LinkedList<>();
    private final static List<String> afterWriteWithoutTypeIdHookTypes = new LinkedList<>();
    private ServerPlayerEntityBase[] beforeWriteWithoutTypeIdHooks;
    private ServerPlayerEntityBase[] overrideWriteWithoutTypeIdHooks;
    private ServerPlayerEntityBase[] afterWriteWithoutTypeIdHooks;
    public boolean isWriteWithoutTypeIdModded;
    private static final Map<String, String[]> allBaseBeforeWriteWithoutTypeIdSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseBeforeWriteWithoutTypeIdInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideWriteWithoutTypeIdSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseOverrideWriteWithoutTypeIdInferiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterWriteWithoutTypeIdSuperiors = new Hashtable<>(0);
    private static final Map<String, String[]> allBaseAfterWriteWithoutTypeIdInferiors = new Hashtable<>(0);
}
