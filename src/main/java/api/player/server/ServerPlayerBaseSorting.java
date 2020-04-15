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

import java.util.HashMap;
import java.util.Map;

public final class ServerPlayerBaseSorting
{
    private String[] beforeLocalConstructingSuperiors;
    private String[] beforeLocalConstructingInferiors;
    private String[] afterLocalConstructingSuperiors;
    private String[] afterLocalConstructingInferiors;
    // ############################################################################
    private Map<String, String[]> dynamicBeforeSuperiors;
    private Map<String, String[]> dynamicBeforeInferiors;
    private Map<String, String[]> dynamicOverrideSuperiors;
    private Map<String, String[]> dynamicOverrideInferiors;
    private Map<String, String[]> dynamicAfterSuperiors;
    private Map<String, String[]> dynamicAfterInferiors;
    // ############################################################################
    private String[] beforeAddExhaustionSuperiors;
    private String[] beforeAddExhaustionInferiors;
    private String[] overrideAddExhaustionSuperiors;
    private String[] overrideAddExhaustionInferiors;
    private String[] afterAddExhaustionSuperiors;
    private String[] afterAddExhaustionInferiors;
    // ############################################################################
    private String[] beforeAddExperienceLevelSuperiors;
    private String[] beforeAddExperienceLevelInferiors;
    private String[] overrideAddExperienceLevelSuperiors;
    private String[] overrideAddExperienceLevelInferiors;
    private String[] afterAddExperienceLevelSuperiors;
    private String[] afterAddExperienceLevelInferiors;
    // ############################################################################
    private String[] beforeAddMovementStatSuperiors;
    private String[] beforeAddMovementStatInferiors;
    private String[] overrideAddMovementStatSuperiors;
    private String[] overrideAddMovementStatInferiors;
    private String[] afterAddMovementStatSuperiors;
    private String[] afterAddMovementStatInferiors;
    // ############################################################################
    private String[] beforeAddStatSuperiors;
    private String[] beforeAddStatInferiors;
    private String[] overrideAddStatSuperiors;
    private String[] overrideAddStatInferiors;
    private String[] afterAddStatSuperiors;
    private String[] afterAddStatInferiors;
    // ############################################################################
    private String[] beforeAreEyesInFluidSuperiors;
    private String[] beforeAreEyesInFluidInferiors;
    private String[] overrideAreEyesInFluidSuperiors;
    private String[] overrideAreEyesInFluidInferiors;
    private String[] afterAreEyesInFluidSuperiors;
    private String[] afterAreEyesInFluidInferiors;
    // ############################################################################
    private String[] beforeAttackEntityFromSuperiors;
    private String[] beforeAttackEntityFromInferiors;
    private String[] overrideAttackEntityFromSuperiors;
    private String[] overrideAttackEntityFromInferiors;
    private String[] afterAttackEntityFromSuperiors;
    private String[] afterAttackEntityFromInferiors;
    // ############################################################################
    private String[] beforeAttackTargetEntityWithCurrentItemSuperiors;
    private String[] beforeAttackTargetEntityWithCurrentItemInferiors;
    private String[] overrideAttackTargetEntityWithCurrentItemSuperiors;
    private String[] overrideAttackTargetEntityWithCurrentItemInferiors;
    private String[] afterAttackTargetEntityWithCurrentItemSuperiors;
    private String[] afterAttackTargetEntityWithCurrentItemInferiors;
    // ############################################################################
    private String[] beforeCanBreatheUnderwaterSuperiors;
    private String[] beforeCanBreatheUnderwaterInferiors;
    private String[] overrideCanBreatheUnderwaterSuperiors;
    private String[] overrideCanBreatheUnderwaterInferiors;
    private String[] afterCanBreatheUnderwaterSuperiors;
    private String[] afterCanBreatheUnderwaterInferiors;
    // ############################################################################
    private String[] beforeCanHarvestBlockSuperiors;
    private String[] beforeCanHarvestBlockInferiors;
    private String[] overrideCanHarvestBlockSuperiors;
    private String[] overrideCanHarvestBlockInferiors;
    private String[] afterCanHarvestBlockSuperiors;
    private String[] afterCanHarvestBlockInferiors;
    // ############################################################################
    private String[] beforeCanPlayerEditSuperiors;
    private String[] beforeCanPlayerEditInferiors;
    private String[] overrideCanPlayerEditSuperiors;
    private String[] overrideCanPlayerEditInferiors;
    private String[] afterCanPlayerEditSuperiors;
    private String[] afterCanPlayerEditInferiors;
    // ############################################################################
    private String[] beforeCanTriggerWalkingSuperiors;
    private String[] beforeCanTriggerWalkingInferiors;
    private String[] overrideCanTriggerWalkingSuperiors;
    private String[] overrideCanTriggerWalkingInferiors;
    private String[] afterCanTriggerWalkingSuperiors;
    private String[] afterCanTriggerWalkingInferiors;
    // ############################################################################
    private String[] beforeCopyFromSuperiors;
    private String[] beforeCopyFromInferiors;
    private String[] overrideCopyFromSuperiors;
    private String[] overrideCopyFromInferiors;
    private String[] afterCopyFromSuperiors;
    private String[] afterCopyFromInferiors;
    // ############################################################################
    private String[] beforeDamageEntitySuperiors;
    private String[] beforeDamageEntityInferiors;
    private String[] overrideDamageEntitySuperiors;
    private String[] overrideDamageEntityInferiors;
    private String[] afterDamageEntitySuperiors;
    private String[] afterDamageEntityInferiors;
    // ############################################################################
    private String[] beforeDropItemSuperiors;
    private String[] beforeDropItemInferiors;
    private String[] overrideDropItemSuperiors;
    private String[] overrideDropItemInferiors;
    private String[] afterDropItemSuperiors;
    private String[] afterDropItemInferiors;
    // ############################################################################
    private String[] beforeGetAIMoveSpeedSuperiors;
    private String[] beforeGetAIMoveSpeedInferiors;
    private String[] overrideGetAIMoveSpeedSuperiors;
    private String[] overrideGetAIMoveSpeedInferiors;
    private String[] afterGetAIMoveSpeedSuperiors;
    private String[] afterGetAIMoveSpeedInferiors;
    // ############################################################################
    private String[] beforeGetBrightnessSuperiors;
    private String[] beforeGetBrightnessInferiors;
    private String[] overrideGetBrightnessSuperiors;
    private String[] overrideGetBrightnessInferiors;
    private String[] afterGetBrightnessSuperiors;
    private String[] afterGetBrightnessInferiors;
    // ############################################################################
    private String[] beforeGetDigSpeedSuperiors;
    private String[] beforeGetDigSpeedInferiors;
    private String[] overrideGetDigSpeedSuperiors;
    private String[] overrideGetDigSpeedInferiors;
    private String[] afterGetDigSpeedSuperiors;
    private String[] afterGetDigSpeedInferiors;
    // ############################################################################
    private String[] beforeGetDistanceSqSuperiors;
    private String[] beforeGetDistanceSqInferiors;
    private String[] overrideGetDistanceSqSuperiors;
    private String[] overrideGetDistanceSqInferiors;
    private String[] afterGetDistanceSqSuperiors;
    private String[] afterGetDistanceSqInferiors;
    // ############################################################################
    private String[] beforeGetDistanceSqToEntitySuperiors;
    private String[] beforeGetDistanceSqToEntityInferiors;
    private String[] overrideGetDistanceSqToEntitySuperiors;
    private String[] overrideGetDistanceSqToEntityInferiors;
    private String[] afterGetDistanceSqToEntitySuperiors;
    private String[] afterGetDistanceSqToEntityInferiors;
    // ############################################################################
    private String[] beforeGetDistanceSqVecSuperiors;
    private String[] beforeGetDistanceSqVecInferiors;
    private String[] overrideGetDistanceSqVecSuperiors;
    private String[] overrideGetDistanceSqVecInferiors;
    private String[] afterGetDistanceSqVecSuperiors;
    private String[] afterGetDistanceSqVecInferiors;
    // ############################################################################
    private String[] beforeGetHurtSoundSuperiors;
    private String[] beforeGetHurtSoundInferiors;
    private String[] overrideGetHurtSoundSuperiors;
    private String[] overrideGetHurtSoundInferiors;
    private String[] afterGetHurtSoundSuperiors;
    private String[] afterGetHurtSoundInferiors;
    // ############################################################################
    private String[] beforeGetNameSuperiors;
    private String[] beforeGetNameInferiors;
    private String[] overrideGetNameSuperiors;
    private String[] overrideGetNameInferiors;
    private String[] afterGetNameSuperiors;
    private String[] afterGetNameInferiors;
    // ############################################################################
    private String[] beforeGetSizeSuperiors;
    private String[] beforeGetSizeInferiors;
    private String[] overrideGetSizeSuperiors;
    private String[] overrideGetSizeInferiors;
    private String[] afterGetSizeSuperiors;
    private String[] afterGetSizeInferiors;
    // ############################################################################
    private String[] beforeGetSleepTimerSuperiors;
    private String[] beforeGetSleepTimerInferiors;
    private String[] overrideGetSleepTimerSuperiors;
    private String[] overrideGetSleepTimerInferiors;
    private String[] afterGetSleepTimerSuperiors;
    private String[] afterGetSleepTimerInferiors;
    // ############################################################################
    private String[] beforeGetStandingEyeHeightSuperiors;
    private String[] beforeGetStandingEyeHeightInferiors;
    private String[] overrideGetStandingEyeHeightSuperiors;
    private String[] overrideGetStandingEyeHeightInferiors;
    private String[] afterGetStandingEyeHeightSuperiors;
    private String[] afterGetStandingEyeHeightInferiors;
    // ############################################################################
    private String[] beforeGiveExperiencePointsSuperiors;
    private String[] beforeGiveExperiencePointsInferiors;
    private String[] overrideGiveExperiencePointsSuperiors;
    private String[] overrideGiveExperiencePointsInferiors;
    private String[] afterGiveExperiencePointsSuperiors;
    private String[] afterGiveExperiencePointsInferiors;
    // ############################################################################
    private String[] beforeHandleWaterMovementSuperiors;
    private String[] beforeHandleWaterMovementInferiors;
    private String[] overrideHandleWaterMovementSuperiors;
    private String[] overrideHandleWaterMovementInferiors;
    private String[] afterHandleWaterMovementSuperiors;
    private String[] afterHandleWaterMovementInferiors;
    // ############################################################################
    private String[] beforeHealSuperiors;
    private String[] beforeHealInferiors;
    private String[] overrideHealSuperiors;
    private String[] overrideHealInferiors;
    private String[] afterHealSuperiors;
    private String[] afterHealInferiors;
    // ############################################################################
    private String[] beforeIsEntityInsideOpaqueBlockSuperiors;
    private String[] beforeIsEntityInsideOpaqueBlockInferiors;
    private String[] overrideIsEntityInsideOpaqueBlockSuperiors;
    private String[] overrideIsEntityInsideOpaqueBlockInferiors;
    private String[] afterIsEntityInsideOpaqueBlockSuperiors;
    private String[] afterIsEntityInsideOpaqueBlockInferiors;
    // ############################################################################
    private String[] beforeIsInWaterSuperiors;
    private String[] beforeIsInWaterInferiors;
    private String[] overrideIsInWaterSuperiors;
    private String[] overrideIsInWaterInferiors;
    private String[] afterIsInWaterSuperiors;
    private String[] afterIsInWaterInferiors;
    // ############################################################################
    private String[] beforeIsOnLadderSuperiors;
    private String[] beforeIsOnLadderInferiors;
    private String[] overrideIsOnLadderSuperiors;
    private String[] overrideIsOnLadderInferiors;
    private String[] afterIsOnLadderSuperiors;
    private String[] afterIsOnLadderInferiors;
    // ############################################################################
    private String[] beforeIsShiftKeyDownSuperiors;
    private String[] beforeIsShiftKeyDownInferiors;
    private String[] overrideIsShiftKeyDownSuperiors;
    private String[] overrideIsShiftKeyDownInferiors;
    private String[] afterIsShiftKeyDownSuperiors;
    private String[] afterIsShiftKeyDownInferiors;
    // ############################################################################
    private String[] beforeIsSleepingSuperiors;
    private String[] beforeIsSleepingInferiors;
    private String[] overrideIsSleepingSuperiors;
    private String[] overrideIsSleepingInferiors;
    private String[] afterIsSleepingSuperiors;
    private String[] afterIsSleepingInferiors;
    // ############################################################################
    private String[] beforeIsSprintingSuperiors;
    private String[] beforeIsSprintingInferiors;
    private String[] overrideIsSprintingSuperiors;
    private String[] overrideIsSprintingInferiors;
    private String[] afterIsSprintingSuperiors;
    private String[] afterIsSprintingInferiors;
    // ############################################################################
    private String[] beforeJumpSuperiors;
    private String[] beforeJumpInferiors;
    private String[] overrideJumpSuperiors;
    private String[] overrideJumpInferiors;
    private String[] afterJumpSuperiors;
    private String[] afterJumpInferiors;
    // ############################################################################
    private String[] beforeKnockBackSuperiors;
    private String[] beforeKnockBackInferiors;
    private String[] overrideKnockBackSuperiors;
    private String[] overrideKnockBackInferiors;
    private String[] afterKnockBackSuperiors;
    private String[] afterKnockBackInferiors;
    // ############################################################################
    private String[] beforeLivingTickSuperiors;
    private String[] beforeLivingTickInferiors;
    private String[] overrideLivingTickSuperiors;
    private String[] overrideLivingTickInferiors;
    private String[] afterLivingTickSuperiors;
    private String[] afterLivingTickInferiors;
    // ############################################################################
    private String[] beforeMoveSuperiors;
    private String[] beforeMoveInferiors;
    private String[] overrideMoveSuperiors;
    private String[] overrideMoveInferiors;
    private String[] afterMoveSuperiors;
    private String[] afterMoveInferiors;
    // ############################################################################
    private String[] beforeMoveRelativeSuperiors;
    private String[] beforeMoveRelativeInferiors;
    private String[] overrideMoveRelativeSuperiors;
    private String[] overrideMoveRelativeInferiors;
    private String[] afterMoveRelativeSuperiors;
    private String[] afterMoveRelativeInferiors;
    // ############################################################################
    private String[] beforeOnDeathSuperiors;
    private String[] beforeOnDeathInferiors;
    private String[] overrideOnDeathSuperiors;
    private String[] overrideOnDeathInferiors;
    private String[] afterOnDeathSuperiors;
    private String[] afterOnDeathInferiors;
    // ############################################################################
    private String[] beforeOnKillEntitySuperiors;
    private String[] beforeOnKillEntityInferiors;
    private String[] overrideOnKillEntitySuperiors;
    private String[] overrideOnKillEntityInferiors;
    private String[] afterOnKillEntitySuperiors;
    private String[] afterOnKillEntityInferiors;
    // ############################################################################
    private String[] beforeOnLivingFallSuperiors;
    private String[] beforeOnLivingFallInferiors;
    private String[] overrideOnLivingFallSuperiors;
    private String[] overrideOnLivingFallInferiors;
    private String[] afterOnLivingFallSuperiors;
    private String[] afterOnLivingFallInferiors;
    // ############################################################################
    private String[] beforeOnStruckByLightningSuperiors;
    private String[] beforeOnStruckByLightningInferiors;
    private String[] overrideOnStruckByLightningSuperiors;
    private String[] overrideOnStruckByLightningInferiors;
    private String[] afterOnStruckByLightningSuperiors;
    private String[] afterOnStruckByLightningInferiors;
    // ############################################################################
    private String[] beforePickSuperiors;
    private String[] beforePickInferiors;
    private String[] overridePickSuperiors;
    private String[] overridePickInferiors;
    private String[] afterPickSuperiors;
    private String[] afterPickInferiors;
    // ############################################################################
    private String[] beforePlayerTickSuperiors;
    private String[] beforePlayerTickInferiors;
    private String[] overridePlayerTickSuperiors;
    private String[] overridePlayerTickInferiors;
    private String[] afterPlayerTickSuperiors;
    private String[] afterPlayerTickInferiors;
    // ############################################################################
    private String[] beforePlayStepSoundSuperiors;
    private String[] beforePlayStepSoundInferiors;
    private String[] overridePlayStepSoundSuperiors;
    private String[] overridePlayStepSoundInferiors;
    private String[] afterPlayStepSoundSuperiors;
    private String[] afterPlayStepSoundInferiors;
    // ############################################################################
    private String[] beforePushOutOfBlocksSuperiors;
    private String[] beforePushOutOfBlocksInferiors;
    private String[] overridePushOutOfBlocksSuperiors;
    private String[] overridePushOutOfBlocksInferiors;
    private String[] afterPushOutOfBlocksSuperiors;
    private String[] afterPushOutOfBlocksInferiors;
    // ############################################################################
    private String[] beforeReadSuperiors;
    private String[] beforeReadInferiors;
    private String[] overrideReadSuperiors;
    private String[] overrideReadInferiors;
    private String[] afterReadSuperiors;
    private String[] afterReadInferiors;
    // ############################################################################
    private String[] beforeRemoveSuperiors;
    private String[] beforeRemoveInferiors;
    private String[] overrideRemoveSuperiors;
    private String[] overrideRemoveInferiors;
    private String[] afterRemoveSuperiors;
    private String[] afterRemoveInferiors;
    // ############################################################################
    private String[] beforeSetEntityActionStateSuperiors;
    private String[] beforeSetEntityActionStateInferiors;
    private String[] overrideSetEntityActionStateSuperiors;
    private String[] overrideSetEntityActionStateInferiors;
    private String[] afterSetEntityActionStateSuperiors;
    private String[] afterSetEntityActionStateInferiors;
    // ############################################################################
    private String[] beforeSetPositionSuperiors;
    private String[] beforeSetPositionInferiors;
    private String[] overrideSetPositionSuperiors;
    private String[] overrideSetPositionInferiors;
    private String[] afterSetPositionSuperiors;
    private String[] afterSetPositionInferiors;
    // ############################################################################
    private String[] beforeSetPositionAndRotationSuperiors;
    private String[] beforeSetPositionAndRotationInferiors;
    private String[] overrideSetPositionAndRotationSuperiors;
    private String[] overrideSetPositionAndRotationInferiors;
    private String[] afterSetPositionAndRotationSuperiors;
    private String[] afterSetPositionAndRotationInferiors;
    // ############################################################################
    private String[] beforeSetSneakingSuperiors;
    private String[] beforeSetSneakingInferiors;
    private String[] overrideSetSneakingSuperiors;
    private String[] overrideSetSneakingInferiors;
    private String[] afterSetSneakingSuperiors;
    private String[] afterSetSneakingInferiors;
    // ############################################################################
    private String[] beforeSetSprintingSuperiors;
    private String[] beforeSetSprintingInferiors;
    private String[] overrideSetSprintingSuperiors;
    private String[] overrideSetSprintingInferiors;
    private String[] afterSetSprintingSuperiors;
    private String[] afterSetSprintingInferiors;
    // ############################################################################
    private String[] beforeSwingArmSuperiors;
    private String[] beforeSwingArmInferiors;
    private String[] overrideSwingArmSuperiors;
    private String[] overrideSwingArmInferiors;
    private String[] afterSwingArmSuperiors;
    private String[] afterSwingArmInferiors;
    // ############################################################################
    private String[] beforeTickSuperiors;
    private String[] beforeTickInferiors;
    private String[] overrideTickSuperiors;
    private String[] overrideTickInferiors;
    private String[] afterTickSuperiors;
    private String[] afterTickInferiors;
    // ############################################################################
    private String[] beforeTravelSuperiors;
    private String[] beforeTravelInferiors;
    private String[] overrideTravelSuperiors;
    private String[] overrideTravelInferiors;
    private String[] afterTravelSuperiors;
    private String[] afterTravelInferiors;
    // ############################################################################
    private String[] beforeTrySleepSuperiors;
    private String[] beforeTrySleepInferiors;
    private String[] overrideTrySleepSuperiors;
    private String[] overrideTrySleepInferiors;
    private String[] afterTrySleepSuperiors;
    private String[] afterTrySleepInferiors;
    // ############################################################################
    private String[] beforeUpdateEntityActionStateSuperiors;
    private String[] beforeUpdateEntityActionStateInferiors;
    private String[] overrideUpdateEntityActionStateSuperiors;
    private String[] overrideUpdateEntityActionStateInferiors;
    private String[] afterUpdateEntityActionStateSuperiors;
    private String[] afterUpdateEntityActionStateInferiors;
    // ############################################################################
    private String[] beforeUpdatePotionEffectsSuperiors;
    private String[] beforeUpdatePotionEffectsInferiors;
    private String[] overrideUpdatePotionEffectsSuperiors;
    private String[] overrideUpdatePotionEffectsInferiors;
    private String[] afterUpdatePotionEffectsSuperiors;
    private String[] afterUpdatePotionEffectsInferiors;
    // ############################################################################
    private String[] beforeUpdateRiddenSuperiors;
    private String[] beforeUpdateRiddenInferiors;
    private String[] overrideUpdateRiddenSuperiors;
    private String[] overrideUpdateRiddenInferiors;
    private String[] afterUpdateRiddenSuperiors;
    private String[] afterUpdateRiddenInferiors;
    // ############################################################################
    private String[] beforeWakeUpPlayerSuperiors;
    private String[] beforeWakeUpPlayerInferiors;
    private String[] overrideWakeUpPlayerSuperiors;
    private String[] overrideWakeUpPlayerInferiors;
    private String[] afterWakeUpPlayerSuperiors;
    private String[] afterWakeUpPlayerInferiors;
    // ############################################################################
    private String[] beforeWriteWithoutTypeIdSuperiors;
    private String[] beforeWriteWithoutTypeIdInferiors;
    private String[] overrideWriteWithoutTypeIdSuperiors;
    private String[] overrideWriteWithoutTypeIdInferiors;
    private String[] afterWriteWithoutTypeIdSuperiors;
    private String[] afterWriteWithoutTypeIdInferiors;

    // ############################################################################

    private Map<String, String[]> setDynamic(String name, String[] names, Map<String, String[]> map)
    {
        if (name == null) {
            throw new IllegalArgumentException("Parameter 'name' may not be null");
        }

        if (names == null) {
            if (map != null) {
                map.remove(name);
            }
            return map;
        }

        if (map == null) {
            map = new HashMap<>();
        }
        map.put(name, names);

        return map;
    }

    // ############################################################################

    public String[] getBeforeLocalConstructingSuperiors()
    {
        return this.beforeLocalConstructingSuperiors;
    }

    public String[] getBeforeLocalConstructingInferiors()
    {
        return this.beforeLocalConstructingInferiors;
    }

    public String[] getAfterLocalConstructingSuperiors()
    {
        return this.afterLocalConstructingSuperiors;
    }

    public String[] getAfterLocalConstructingInferiors()
    {
        return this.afterLocalConstructingInferiors;
    }

    public void setBeforeLocalConstructingSuperiors(String[] value)
    {
        this.beforeLocalConstructingSuperiors = value;
    }

    public void setBeforeLocalConstructingInferiors(String[] value)
    {
        this.beforeLocalConstructingInferiors = value;
    }

    public void setAfterLocalConstructingSuperiors(String[] value)
    {
        this.afterLocalConstructingSuperiors = value;
    }

    public void setAfterLocalConstructingInferiors(String[] value)
    {
        this.afterLocalConstructingInferiors = value;
    }

    // ############################################################################

    public Map<String, String[]> getDynamicBeforeSuperiors()
    {
        return this.dynamicBeforeSuperiors;
    }

    public Map<String, String[]> getDynamicBeforeInferiors()
    {
        return this.dynamicBeforeInferiors;
    }

    public Map<String, String[]> getDynamicOverrideSuperiors()
    {
        return this.dynamicOverrideSuperiors;
    }

    public Map<String, String[]> getDynamicOverrideInferiors()
    {
        return this.dynamicOverrideInferiors;
    }

    public Map<String, String[]> getDynamicAfterSuperiors()
    {
        return this.dynamicAfterSuperiors;
    }

    public Map<String, String[]> getDynamicAfterInferiors()
    {
        return this.dynamicAfterInferiors;
    }

    public void setDynamicBeforeSuperiors(String name, String[] superiors)
    {
        this.dynamicBeforeSuperiors = this.setDynamic(name, superiors, this.dynamicBeforeSuperiors);
    }

    public void setDynamicBeforeInferiors(String name, String[] inferiors)
    {
        this.dynamicBeforeInferiors = this.setDynamic(name, inferiors, this.dynamicBeforeInferiors);
    }

    public void setDynamicOverrideSuperiors(String name, String[] superiors)
    {
        this.dynamicOverrideSuperiors = this.setDynamic(name, superiors, this.dynamicOverrideSuperiors);
    }

    public void setDynamicOverrideInferiors(String name, String[] inferiors)
    {
        this.dynamicOverrideInferiors = this.setDynamic(name, inferiors, this.dynamicOverrideInferiors);
    }

    public void setDynamicAfterSuperiors(String name, String[] superiors)
    {
        this.dynamicAfterSuperiors = this.setDynamic(name, superiors, this.dynamicAfterSuperiors);
    }

    public void setDynamicAfterInferiors(String name, String[] inferiors)
    {
        this.dynamicAfterInferiors = this.setDynamic(name, inferiors, this.dynamicAfterInferiors);
    }

    // ############################################################################

    public String[] getBeforeAddExhaustionSuperiors()
    {
        return this.beforeAddExhaustionSuperiors;
    }

    public String[] getBeforeAddExhaustionInferiors()
    {
        return this.beforeAddExhaustionInferiors;
    }

    public String[] getOverrideAddExhaustionSuperiors()
    {
        return this.overrideAddExhaustionSuperiors;
    }

    public String[] getOverrideAddExhaustionInferiors()
    {
        return this.overrideAddExhaustionInferiors;
    }

    public String[] getAfterAddExhaustionSuperiors()
    {
        return this.afterAddExhaustionSuperiors;
    }

    public String[] getAfterAddExhaustionInferiors()
    {
        return this.afterAddExhaustionInferiors;
    }

    public void setBeforeAddExhaustionSuperiors(String[] value)
    {
        this.beforeAddExhaustionSuperiors = value;
    }

    public void setBeforeAddExhaustionInferiors(String[] value)
    {
        this.beforeAddExhaustionInferiors = value;
    }

    public void setOverrideAddExhaustionSuperiors(String[] value)
    {
        this.overrideAddExhaustionSuperiors = value;
    }

    public void setOverrideAddExhaustionInferiors(String[] value)
    {
        this.overrideAddExhaustionInferiors = value;
    }

    public void setAfterAddExhaustionSuperiors(String[] value)
    {
        this.afterAddExhaustionSuperiors = value;
    }

    public void setAfterAddExhaustionInferiors(String[] value)
    {
        this.afterAddExhaustionInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeAddExperienceLevelSuperiors()
    {
        return this.beforeAddExperienceLevelSuperiors;
    }

    public String[] getBeforeAddExperienceLevelInferiors()
    {
        return this.beforeAddExperienceLevelInferiors;
    }

    public String[] getOverrideAddExperienceLevelSuperiors()
    {
        return this.overrideAddExperienceLevelSuperiors;
    }

    public String[] getOverrideAddExperienceLevelInferiors()
    {
        return this.overrideAddExperienceLevelInferiors;
    }

    public String[] getAfterAddExperienceLevelSuperiors()
    {
        return this.afterAddExperienceLevelSuperiors;
    }

    public String[] getAfterAddExperienceLevelInferiors()
    {
        return this.afterAddExperienceLevelInferiors;
    }

    public void setBeforeAddExperienceLevelSuperiors(String[] value)
    {
        this.beforeAddExperienceLevelSuperiors = value;
    }

    public void setBeforeAddExperienceLevelInferiors(String[] value)
    {
        this.beforeAddExperienceLevelInferiors = value;
    }

    public void setOverrideAddExperienceLevelSuperiors(String[] value)
    {
        this.overrideAddExperienceLevelSuperiors = value;
    }

    public void setOverrideAddExperienceLevelInferiors(String[] value)
    {
        this.overrideAddExperienceLevelInferiors = value;
    }

    public void setAfterAddExperienceLevelSuperiors(String[] value)
    {
        this.afterAddExperienceLevelSuperiors = value;
    }

    public void setAfterAddExperienceLevelInferiors(String[] value)
    {
        this.afterAddExperienceLevelInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeAddMovementStatSuperiors()
    {
        return this.beforeAddMovementStatSuperiors;
    }

    public String[] getBeforeAddMovementStatInferiors()
    {
        return this.beforeAddMovementStatInferiors;
    }

    public String[] getOverrideAddMovementStatSuperiors()
    {
        return this.overrideAddMovementStatSuperiors;
    }

    public String[] getOverrideAddMovementStatInferiors()
    {
        return this.overrideAddMovementStatInferiors;
    }

    public String[] getAfterAddMovementStatSuperiors()
    {
        return this.afterAddMovementStatSuperiors;
    }

    public String[] getAfterAddMovementStatInferiors()
    {
        return this.afterAddMovementStatInferiors;
    }

    public void setBeforeAddMovementStatSuperiors(String[] value)
    {
        this.beforeAddMovementStatSuperiors = value;
    }

    public void setBeforeAddMovementStatInferiors(String[] value)
    {
        this.beforeAddMovementStatInferiors = value;
    }

    public void setOverrideAddMovementStatSuperiors(String[] value)
    {
        this.overrideAddMovementStatSuperiors = value;
    }

    public void setOverrideAddMovementStatInferiors(String[] value)
    {
        this.overrideAddMovementStatInferiors = value;
    }

    public void setAfterAddMovementStatSuperiors(String[] value)
    {
        this.afterAddMovementStatSuperiors = value;
    }

    public void setAfterAddMovementStatInferiors(String[] value)
    {
        this.afterAddMovementStatInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeAddStatSuperiors()
    {
        return this.beforeAddStatSuperiors;
    }

    public String[] getBeforeAddStatInferiors()
    {
        return this.beforeAddStatInferiors;
    }

    public String[] getOverrideAddStatSuperiors()
    {
        return this.overrideAddStatSuperiors;
    }

    public String[] getOverrideAddStatInferiors()
    {
        return this.overrideAddStatInferiors;
    }

    public String[] getAfterAddStatSuperiors()
    {
        return this.afterAddStatSuperiors;
    }

    public String[] getAfterAddStatInferiors()
    {
        return this.afterAddStatInferiors;
    }

    public void setBeforeAddStatSuperiors(String[] value)
    {
        this.beforeAddStatSuperiors = value;
    }

    public void setBeforeAddStatInferiors(String[] value)
    {
        this.beforeAddStatInferiors = value;
    }

    public void setOverrideAddStatSuperiors(String[] value)
    {
        this.overrideAddStatSuperiors = value;
    }

    public void setOverrideAddStatInferiors(String[] value)
    {
        this.overrideAddStatInferiors = value;
    }

    public void setAfterAddStatSuperiors(String[] value)
    {
        this.afterAddStatSuperiors = value;
    }

    public void setAfterAddStatInferiors(String[] value)
    {
        this.afterAddStatInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeAreEyesInFluidSuperiors()
    {
        return this.beforeAreEyesInFluidSuperiors;
    }

    public String[] getBeforeAreEyesInFluidInferiors()
    {
        return this.beforeAreEyesInFluidInferiors;
    }

    public String[] getOverrideAreEyesInFluidSuperiors()
    {
        return this.overrideAreEyesInFluidSuperiors;
    }

    public String[] getOverrideAreEyesInFluidInferiors()
    {
        return this.overrideAreEyesInFluidInferiors;
    }

    public String[] getAfterAreEyesInFluidSuperiors()
    {
        return this.afterAreEyesInFluidSuperiors;
    }

    public String[] getAfterAreEyesInFluidInferiors()
    {
        return this.afterAreEyesInFluidInferiors;
    }

    public void setBeforeAreEyesInFluidSuperiors(String[] value)
    {
        this.beforeAreEyesInFluidSuperiors = value;
    }

    public void setBeforeAreEyesInFluidInferiors(String[] value)
    {
        this.beforeAreEyesInFluidInferiors = value;
    }

    public void setOverrideAreEyesInFluidSuperiors(String[] value)
    {
        this.overrideAreEyesInFluidSuperiors = value;
    }

    public void setOverrideAreEyesInFluidInferiors(String[] value)
    {
        this.overrideAreEyesInFluidInferiors = value;
    }

    public void setAfterAreEyesInFluidSuperiors(String[] value)
    {
        this.afterAreEyesInFluidSuperiors = value;
    }

    public void setAfterAreEyesInFluidInferiors(String[] value)
    {
        this.afterAreEyesInFluidInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeAttackEntityFromSuperiors()
    {
        return this.beforeAttackEntityFromSuperiors;
    }

    public String[] getBeforeAttackEntityFromInferiors()
    {
        return this.beforeAttackEntityFromInferiors;
    }

    public String[] getOverrideAttackEntityFromSuperiors()
    {
        return this.overrideAttackEntityFromSuperiors;
    }

    public String[] getOverrideAttackEntityFromInferiors()
    {
        return this.overrideAttackEntityFromInferiors;
    }

    public String[] getAfterAttackEntityFromSuperiors()
    {
        return this.afterAttackEntityFromSuperiors;
    }

    public String[] getAfterAttackEntityFromInferiors()
    {
        return this.afterAttackEntityFromInferiors;
    }

    public void setBeforeAttackEntityFromSuperiors(String[] value)
    {
        this.beforeAttackEntityFromSuperiors = value;
    }

    public void setBeforeAttackEntityFromInferiors(String[] value)
    {
        this.beforeAttackEntityFromInferiors = value;
    }

    public void setOverrideAttackEntityFromSuperiors(String[] value)
    {
        this.overrideAttackEntityFromSuperiors = value;
    }

    public void setOverrideAttackEntityFromInferiors(String[] value)
    {
        this.overrideAttackEntityFromInferiors = value;
    }

    public void setAfterAttackEntityFromSuperiors(String[] value)
    {
        this.afterAttackEntityFromSuperiors = value;
    }

    public void setAfterAttackEntityFromInferiors(String[] value)
    {
        this.afterAttackEntityFromInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeAttackTargetEntityWithCurrentItemSuperiors()
    {
        return this.beforeAttackTargetEntityWithCurrentItemSuperiors;
    }

    public String[] getBeforeAttackTargetEntityWithCurrentItemInferiors()
    {
        return this.beforeAttackTargetEntityWithCurrentItemInferiors;
    }

    public String[] getOverrideAttackTargetEntityWithCurrentItemSuperiors()
    {
        return this.overrideAttackTargetEntityWithCurrentItemSuperiors;
    }

    public String[] getOverrideAttackTargetEntityWithCurrentItemInferiors()
    {
        return this.overrideAttackTargetEntityWithCurrentItemInferiors;
    }

    public String[] getAfterAttackTargetEntityWithCurrentItemSuperiors()
    {
        return this.afterAttackTargetEntityWithCurrentItemSuperiors;
    }

    public String[] getAfterAttackTargetEntityWithCurrentItemInferiors()
    {
        return this.afterAttackTargetEntityWithCurrentItemInferiors;
    }

    public void setBeforeAttackTargetEntityWithCurrentItemSuperiors(String[] value)
    {
        this.beforeAttackTargetEntityWithCurrentItemSuperiors = value;
    }

    public void setBeforeAttackTargetEntityWithCurrentItemInferiors(String[] value)
    {
        this.beforeAttackTargetEntityWithCurrentItemInferiors = value;
    }

    public void setOverrideAttackTargetEntityWithCurrentItemSuperiors(String[] value)
    {
        this.overrideAttackTargetEntityWithCurrentItemSuperiors = value;
    }

    public void setOverrideAttackTargetEntityWithCurrentItemInferiors(String[] value)
    {
        this.overrideAttackTargetEntityWithCurrentItemInferiors = value;
    }

    public void setAfterAttackTargetEntityWithCurrentItemSuperiors(String[] value)
    {
        this.afterAttackTargetEntityWithCurrentItemSuperiors = value;
    }

    public void setAfterAttackTargetEntityWithCurrentItemInferiors(String[] value)
    {
        this.afterAttackTargetEntityWithCurrentItemInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeCanBreatheUnderwaterSuperiors()
    {
        return this.beforeCanBreatheUnderwaterSuperiors;
    }

    public String[] getBeforeCanBreatheUnderwaterInferiors()
    {
        return this.beforeCanBreatheUnderwaterInferiors;
    }

    public String[] getOverrideCanBreatheUnderwaterSuperiors()
    {
        return this.overrideCanBreatheUnderwaterSuperiors;
    }

    public String[] getOverrideCanBreatheUnderwaterInferiors()
    {
        return this.overrideCanBreatheUnderwaterInferiors;
    }

    public String[] getAfterCanBreatheUnderwaterSuperiors()
    {
        return this.afterCanBreatheUnderwaterSuperiors;
    }

    public String[] getAfterCanBreatheUnderwaterInferiors()
    {
        return this.afterCanBreatheUnderwaterInferiors;
    }

    public void setBeforeCanBreatheUnderwaterSuperiors(String[] value)
    {
        this.beforeCanBreatheUnderwaterSuperiors = value;
    }

    public void setBeforeCanBreatheUnderwaterInferiors(String[] value)
    {
        this.beforeCanBreatheUnderwaterInferiors = value;
    }

    public void setOverrideCanBreatheUnderwaterSuperiors(String[] value)
    {
        this.overrideCanBreatheUnderwaterSuperiors = value;
    }

    public void setOverrideCanBreatheUnderwaterInferiors(String[] value)
    {
        this.overrideCanBreatheUnderwaterInferiors = value;
    }

    public void setAfterCanBreatheUnderwaterSuperiors(String[] value)
    {
        this.afterCanBreatheUnderwaterSuperiors = value;
    }

    public void setAfterCanBreatheUnderwaterInferiors(String[] value)
    {
        this.afterCanBreatheUnderwaterInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeCanHarvestBlockSuperiors()
    {
        return this.beforeCanHarvestBlockSuperiors;
    }

    public String[] getBeforeCanHarvestBlockInferiors()
    {
        return this.beforeCanHarvestBlockInferiors;
    }

    public String[] getOverrideCanHarvestBlockSuperiors()
    {
        return this.overrideCanHarvestBlockSuperiors;
    }

    public String[] getOverrideCanHarvestBlockInferiors()
    {
        return this.overrideCanHarvestBlockInferiors;
    }

    public String[] getAfterCanHarvestBlockSuperiors()
    {
        return this.afterCanHarvestBlockSuperiors;
    }

    public String[] getAfterCanHarvestBlockInferiors()
    {
        return this.afterCanHarvestBlockInferiors;
    }

    public void setBeforeCanHarvestBlockSuperiors(String[] value)
    {
        this.beforeCanHarvestBlockSuperiors = value;
    }

    public void setBeforeCanHarvestBlockInferiors(String[] value)
    {
        this.beforeCanHarvestBlockInferiors = value;
    }

    public void setOverrideCanHarvestBlockSuperiors(String[] value)
    {
        this.overrideCanHarvestBlockSuperiors = value;
    }

    public void setOverrideCanHarvestBlockInferiors(String[] value)
    {
        this.overrideCanHarvestBlockInferiors = value;
    }

    public void setAfterCanHarvestBlockSuperiors(String[] value)
    {
        this.afterCanHarvestBlockSuperiors = value;
    }

    public void setAfterCanHarvestBlockInferiors(String[] value)
    {
        this.afterCanHarvestBlockInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeCanPlayerEditSuperiors()
    {
        return this.beforeCanPlayerEditSuperiors;
    }

    public String[] getBeforeCanPlayerEditInferiors()
    {
        return this.beforeCanPlayerEditInferiors;
    }

    public String[] getOverrideCanPlayerEditSuperiors()
    {
        return this.overrideCanPlayerEditSuperiors;
    }

    public String[] getOverrideCanPlayerEditInferiors()
    {
        return this.overrideCanPlayerEditInferiors;
    }

    public String[] getAfterCanPlayerEditSuperiors()
    {
        return this.afterCanPlayerEditSuperiors;
    }

    public String[] getAfterCanPlayerEditInferiors()
    {
        return this.afterCanPlayerEditInferiors;
    }

    public void setBeforeCanPlayerEditSuperiors(String[] value)
    {
        this.beforeCanPlayerEditSuperiors = value;
    }

    public void setBeforeCanPlayerEditInferiors(String[] value)
    {
        this.beforeCanPlayerEditInferiors = value;
    }

    public void setOverrideCanPlayerEditSuperiors(String[] value)
    {
        this.overrideCanPlayerEditSuperiors = value;
    }

    public void setOverrideCanPlayerEditInferiors(String[] value)
    {
        this.overrideCanPlayerEditInferiors = value;
    }

    public void setAfterCanPlayerEditSuperiors(String[] value)
    {
        this.afterCanPlayerEditSuperiors = value;
    }

    public void setAfterCanPlayerEditInferiors(String[] value)
    {
        this.afterCanPlayerEditInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeCanTriggerWalkingSuperiors()
    {
        return this.beforeCanTriggerWalkingSuperiors;
    }

    public String[] getBeforeCanTriggerWalkingInferiors()
    {
        return this.beforeCanTriggerWalkingInferiors;
    }

    public String[] getOverrideCanTriggerWalkingSuperiors()
    {
        return this.overrideCanTriggerWalkingSuperiors;
    }

    public String[] getOverrideCanTriggerWalkingInferiors()
    {
        return this.overrideCanTriggerWalkingInferiors;
    }

    public String[] getAfterCanTriggerWalkingSuperiors()
    {
        return this.afterCanTriggerWalkingSuperiors;
    }

    public String[] getAfterCanTriggerWalkingInferiors()
    {
        return this.afterCanTriggerWalkingInferiors;
    }

    public void setBeforeCanTriggerWalkingSuperiors(String[] value)
    {
        this.beforeCanTriggerWalkingSuperiors = value;
    }

    public void setBeforeCanTriggerWalkingInferiors(String[] value)
    {
        this.beforeCanTriggerWalkingInferiors = value;
    }

    public void setOverrideCanTriggerWalkingSuperiors(String[] value)
    {
        this.overrideCanTriggerWalkingSuperiors = value;
    }

    public void setOverrideCanTriggerWalkingInferiors(String[] value)
    {
        this.overrideCanTriggerWalkingInferiors = value;
    }

    public void setAfterCanTriggerWalkingSuperiors(String[] value)
    {
        this.afterCanTriggerWalkingSuperiors = value;
    }

    public void setAfterCanTriggerWalkingInferiors(String[] value)
    {
        this.afterCanTriggerWalkingInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeCopyFromSuperiors()
    {
        return this.beforeCopyFromSuperiors;
    }

    public String[] getBeforeCopyFromInferiors()
    {
        return this.beforeCopyFromInferiors;
    }

    public String[] getOverrideCopyFromSuperiors()
    {
        return this.overrideCopyFromSuperiors;
    }

    public String[] getOverrideCopyFromInferiors()
    {
        return this.overrideCopyFromInferiors;
    }

    public String[] getAfterCopyFromSuperiors()
    {
        return this.afterCopyFromSuperiors;
    }

    public String[] getAfterCopyFromInferiors()
    {
        return this.afterCopyFromInferiors;
    }

    public void setBeforeCopyFromSuperiors(String[] value)
    {
        this.beforeCopyFromSuperiors = value;
    }

    public void setBeforeCopyFromInferiors(String[] value)
    {
        this.beforeCopyFromInferiors = value;
    }

    public void setOverrideCopyFromSuperiors(String[] value)
    {
        this.overrideCopyFromSuperiors = value;
    }

    public void setOverrideCopyFromInferiors(String[] value)
    {
        this.overrideCopyFromInferiors = value;
    }

    public void setAfterCopyFromSuperiors(String[] value)
    {
        this.afterCopyFromSuperiors = value;
    }

    public void setAfterCopyFromInferiors(String[] value)
    {
        this.afterCopyFromInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeDamageEntitySuperiors()
    {
        return this.beforeDamageEntitySuperiors;
    }

    public String[] getBeforeDamageEntityInferiors()
    {
        return this.beforeDamageEntityInferiors;
    }

    public String[] getOverrideDamageEntitySuperiors()
    {
        return this.overrideDamageEntitySuperiors;
    }

    public String[] getOverrideDamageEntityInferiors()
    {
        return this.overrideDamageEntityInferiors;
    }

    public String[] getAfterDamageEntitySuperiors()
    {
        return this.afterDamageEntitySuperiors;
    }

    public String[] getAfterDamageEntityInferiors()
    {
        return this.afterDamageEntityInferiors;
    }

    public void setBeforeDamageEntitySuperiors(String[] value)
    {
        this.beforeDamageEntitySuperiors = value;
    }

    public void setBeforeDamageEntityInferiors(String[] value)
    {
        this.beforeDamageEntityInferiors = value;
    }

    public void setOverrideDamageEntitySuperiors(String[] value)
    {
        this.overrideDamageEntitySuperiors = value;
    }

    public void setOverrideDamageEntityInferiors(String[] value)
    {
        this.overrideDamageEntityInferiors = value;
    }

    public void setAfterDamageEntitySuperiors(String[] value)
    {
        this.afterDamageEntitySuperiors = value;
    }

    public void setAfterDamageEntityInferiors(String[] value)
    {
        this.afterDamageEntityInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeDropItemSuperiors()
    {
        return this.beforeDropItemSuperiors;
    }

    public String[] getBeforeDropItemInferiors()
    {
        return this.beforeDropItemInferiors;
    }

    public String[] getOverrideDropItemSuperiors()
    {
        return this.overrideDropItemSuperiors;
    }

    public String[] getOverrideDropItemInferiors()
    {
        return this.overrideDropItemInferiors;
    }

    public String[] getAfterDropItemSuperiors()
    {
        return this.afterDropItemSuperiors;
    }

    public String[] getAfterDropItemInferiors()
    {
        return this.afterDropItemInferiors;
    }

    public void setBeforeDropItemSuperiors(String[] value)
    {
        this.beforeDropItemSuperiors = value;
    }

    public void setBeforeDropItemInferiors(String[] value)
    {
        this.beforeDropItemInferiors = value;
    }

    public void setOverrideDropItemSuperiors(String[] value)
    {
        this.overrideDropItemSuperiors = value;
    }

    public void setOverrideDropItemInferiors(String[] value)
    {
        this.overrideDropItemInferiors = value;
    }

    public void setAfterDropItemSuperiors(String[] value)
    {
        this.afterDropItemSuperiors = value;
    }

    public void setAfterDropItemInferiors(String[] value)
    {
        this.afterDropItemInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeGetAIMoveSpeedSuperiors()
    {
        return this.beforeGetAIMoveSpeedSuperiors;
    }

    public String[] getBeforeGetAIMoveSpeedInferiors()
    {
        return this.beforeGetAIMoveSpeedInferiors;
    }

    public String[] getOverrideGetAIMoveSpeedSuperiors()
    {
        return this.overrideGetAIMoveSpeedSuperiors;
    }

    public String[] getOverrideGetAIMoveSpeedInferiors()
    {
        return this.overrideGetAIMoveSpeedInferiors;
    }

    public String[] getAfterGetAIMoveSpeedSuperiors()
    {
        return this.afterGetAIMoveSpeedSuperiors;
    }

    public String[] getAfterGetAIMoveSpeedInferiors()
    {
        return this.afterGetAIMoveSpeedInferiors;
    }

    public void setBeforeGetAIMoveSpeedSuperiors(String[] value)
    {
        this.beforeGetAIMoveSpeedSuperiors = value;
    }

    public void setBeforeGetAIMoveSpeedInferiors(String[] value)
    {
        this.beforeGetAIMoveSpeedInferiors = value;
    }

    public void setOverrideGetAIMoveSpeedSuperiors(String[] value)
    {
        this.overrideGetAIMoveSpeedSuperiors = value;
    }

    public void setOverrideGetAIMoveSpeedInferiors(String[] value)
    {
        this.overrideGetAIMoveSpeedInferiors = value;
    }

    public void setAfterGetAIMoveSpeedSuperiors(String[] value)
    {
        this.afterGetAIMoveSpeedSuperiors = value;
    }

    public void setAfterGetAIMoveSpeedInferiors(String[] value)
    {
        this.afterGetAIMoveSpeedInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeGetBrightnessSuperiors()
    {
        return this.beforeGetBrightnessSuperiors;
    }

    public String[] getBeforeGetBrightnessInferiors()
    {
        return this.beforeGetBrightnessInferiors;
    }

    public String[] getOverrideGetBrightnessSuperiors()
    {
        return this.overrideGetBrightnessSuperiors;
    }

    public String[] getOverrideGetBrightnessInferiors()
    {
        return this.overrideGetBrightnessInferiors;
    }

    public String[] getAfterGetBrightnessSuperiors()
    {
        return this.afterGetBrightnessSuperiors;
    }

    public String[] getAfterGetBrightnessInferiors()
    {
        return this.afterGetBrightnessInferiors;
    }

    public void setBeforeGetBrightnessSuperiors(String[] value)
    {
        this.beforeGetBrightnessSuperiors = value;
    }

    public void setBeforeGetBrightnessInferiors(String[] value)
    {
        this.beforeGetBrightnessInferiors = value;
    }

    public void setOverrideGetBrightnessSuperiors(String[] value)
    {
        this.overrideGetBrightnessSuperiors = value;
    }

    public void setOverrideGetBrightnessInferiors(String[] value)
    {
        this.overrideGetBrightnessInferiors = value;
    }

    public void setAfterGetBrightnessSuperiors(String[] value)
    {
        this.afterGetBrightnessSuperiors = value;
    }

    public void setAfterGetBrightnessInferiors(String[] value)
    {
        this.afterGetBrightnessInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeGetDigSpeedSuperiors()
    {
        return this.beforeGetDigSpeedSuperiors;
    }

    public String[] getBeforeGetDigSpeedInferiors()
    {
        return this.beforeGetDigSpeedInferiors;
    }

    public String[] getOverrideGetDigSpeedSuperiors()
    {
        return this.overrideGetDigSpeedSuperiors;
    }

    public String[] getOverrideGetDigSpeedInferiors()
    {
        return this.overrideGetDigSpeedInferiors;
    }

    public String[] getAfterGetDigSpeedSuperiors()
    {
        return this.afterGetDigSpeedSuperiors;
    }

    public String[] getAfterGetDigSpeedInferiors()
    {
        return this.afterGetDigSpeedInferiors;
    }

    public void setBeforeGetDigSpeedSuperiors(String[] value)
    {
        this.beforeGetDigSpeedSuperiors = value;
    }

    public void setBeforeGetDigSpeedInferiors(String[] value)
    {
        this.beforeGetDigSpeedInferiors = value;
    }

    public void setOverrideGetDigSpeedSuperiors(String[] value)
    {
        this.overrideGetDigSpeedSuperiors = value;
    }

    public void setOverrideGetDigSpeedInferiors(String[] value)
    {
        this.overrideGetDigSpeedInferiors = value;
    }

    public void setAfterGetDigSpeedSuperiors(String[] value)
    {
        this.afterGetDigSpeedSuperiors = value;
    }

    public void setAfterGetDigSpeedInferiors(String[] value)
    {
        this.afterGetDigSpeedInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeGetDistanceSqSuperiors()
    {
        return this.beforeGetDistanceSqSuperiors;
    }

    public String[] getBeforeGetDistanceSqInferiors()
    {
        return this.beforeGetDistanceSqInferiors;
    }

    public String[] getOverrideGetDistanceSqSuperiors()
    {
        return this.overrideGetDistanceSqSuperiors;
    }

    public String[] getOverrideGetDistanceSqInferiors()
    {
        return this.overrideGetDistanceSqInferiors;
    }

    public String[] getAfterGetDistanceSqSuperiors()
    {
        return this.afterGetDistanceSqSuperiors;
    }

    public String[] getAfterGetDistanceSqInferiors()
    {
        return this.afterGetDistanceSqInferiors;
    }

    public void setBeforeGetDistanceSqSuperiors(String[] value)
    {
        this.beforeGetDistanceSqSuperiors = value;
    }

    public void setBeforeGetDistanceSqInferiors(String[] value)
    {
        this.beforeGetDistanceSqInferiors = value;
    }

    public void setOverrideGetDistanceSqSuperiors(String[] value)
    {
        this.overrideGetDistanceSqSuperiors = value;
    }

    public void setOverrideGetDistanceSqInferiors(String[] value)
    {
        this.overrideGetDistanceSqInferiors = value;
    }

    public void setAfterGetDistanceSqSuperiors(String[] value)
    {
        this.afterGetDistanceSqSuperiors = value;
    }

    public void setAfterGetDistanceSqInferiors(String[] value)
    {
        this.afterGetDistanceSqInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeGetDistanceSqToEntitySuperiors()
    {
        return this.beforeGetDistanceSqToEntitySuperiors;
    }

    public String[] getBeforeGetDistanceSqToEntityInferiors()
    {
        return this.beforeGetDistanceSqToEntityInferiors;
    }

    public String[] getOverrideGetDistanceSqToEntitySuperiors()
    {
        return this.overrideGetDistanceSqToEntitySuperiors;
    }

    public String[] getOverrideGetDistanceSqToEntityInferiors()
    {
        return this.overrideGetDistanceSqToEntityInferiors;
    }

    public String[] getAfterGetDistanceSqToEntitySuperiors()
    {
        return this.afterGetDistanceSqToEntitySuperiors;
    }

    public String[] getAfterGetDistanceSqToEntityInferiors()
    {
        return this.afterGetDistanceSqToEntityInferiors;
    }

    public void setBeforeGetDistanceSqToEntitySuperiors(String[] value)
    {
        this.beforeGetDistanceSqToEntitySuperiors = value;
    }

    public void setBeforeGetDistanceSqToEntityInferiors(String[] value)
    {
        this.beforeGetDistanceSqToEntityInferiors = value;
    }

    public void setOverrideGetDistanceSqToEntitySuperiors(String[] value)
    {
        this.overrideGetDistanceSqToEntitySuperiors = value;
    }

    public void setOverrideGetDistanceSqToEntityInferiors(String[] value)
    {
        this.overrideGetDistanceSqToEntityInferiors = value;
    }

    public void setAfterGetDistanceSqToEntitySuperiors(String[] value)
    {
        this.afterGetDistanceSqToEntitySuperiors = value;
    }

    public void setAfterGetDistanceSqToEntityInferiors(String[] value)
    {
        this.afterGetDistanceSqToEntityInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeGetDistanceSqVecSuperiors()
    {
        return this.beforeGetDistanceSqVecSuperiors;
    }

    public String[] getBeforeGetDistanceSqVecInferiors()
    {
        return this.beforeGetDistanceSqVecInferiors;
    }

    public String[] getOverrideGetDistanceSqVecSuperiors()
    {
        return this.overrideGetDistanceSqVecSuperiors;
    }

    public String[] getOverrideGetDistanceSqVecInferiors()
    {
        return this.overrideGetDistanceSqVecInferiors;
    }

    public String[] getAfterGetDistanceSqVecSuperiors()
    {
        return this.afterGetDistanceSqVecSuperiors;
    }

    public String[] getAfterGetDistanceSqVecInferiors()
    {
        return this.afterGetDistanceSqVecInferiors;
    }

    public void setBeforeGetDistanceSqVecSuperiors(String[] value)
    {
        this.beforeGetDistanceSqVecSuperiors = value;
    }

    public void setBeforeGetDistanceSqVecInferiors(String[] value)
    {
        this.beforeGetDistanceSqVecInferiors = value;
    }

    public void setOverrideGetDistanceSqVecSuperiors(String[] value)
    {
        this.overrideGetDistanceSqVecSuperiors = value;
    }

    public void setOverrideGetDistanceSqVecInferiors(String[] value)
    {
        this.overrideGetDistanceSqVecInferiors = value;
    }

    public void setAfterGetDistanceSqVecSuperiors(String[] value)
    {
        this.afterGetDistanceSqVecSuperiors = value;
    }

    public void setAfterGetDistanceSqVecInferiors(String[] value)
    {
        this.afterGetDistanceSqVecInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeGetHurtSoundSuperiors()
    {
        return this.beforeGetHurtSoundSuperiors;
    }

    public String[] getBeforeGetHurtSoundInferiors()
    {
        return this.beforeGetHurtSoundInferiors;
    }

    public String[] getOverrideGetHurtSoundSuperiors()
    {
        return this.overrideGetHurtSoundSuperiors;
    }

    public String[] getOverrideGetHurtSoundInferiors()
    {
        return this.overrideGetHurtSoundInferiors;
    }

    public String[] getAfterGetHurtSoundSuperiors()
    {
        return this.afterGetHurtSoundSuperiors;
    }

    public String[] getAfterGetHurtSoundInferiors()
    {
        return this.afterGetHurtSoundInferiors;
    }

    public void setBeforeGetHurtSoundSuperiors(String[] value)
    {
        this.beforeGetHurtSoundSuperiors = value;
    }

    public void setBeforeGetHurtSoundInferiors(String[] value)
    {
        this.beforeGetHurtSoundInferiors = value;
    }

    public void setOverrideGetHurtSoundSuperiors(String[] value)
    {
        this.overrideGetHurtSoundSuperiors = value;
    }

    public void setOverrideGetHurtSoundInferiors(String[] value)
    {
        this.overrideGetHurtSoundInferiors = value;
    }

    public void setAfterGetHurtSoundSuperiors(String[] value)
    {
        this.afterGetHurtSoundSuperiors = value;
    }

    public void setAfterGetHurtSoundInferiors(String[] value)
    {
        this.afterGetHurtSoundInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeGetNameSuperiors()
    {
        return this.beforeGetNameSuperiors;
    }

    public String[] getBeforeGetNameInferiors()
    {
        return this.beforeGetNameInferiors;
    }

    public String[] getOverrideGetNameSuperiors()
    {
        return this.overrideGetNameSuperiors;
    }

    public String[] getOverrideGetNameInferiors()
    {
        return this.overrideGetNameInferiors;
    }

    public String[] getAfterGetNameSuperiors()
    {
        return this.afterGetNameSuperiors;
    }

    public String[] getAfterGetNameInferiors()
    {
        return this.afterGetNameInferiors;
    }

    public void setBeforeGetNameSuperiors(String[] value)
    {
        this.beforeGetNameSuperiors = value;
    }

    public void setBeforeGetNameInferiors(String[] value)
    {
        this.beforeGetNameInferiors = value;
    }

    public void setOverrideGetNameSuperiors(String[] value)
    {
        this.overrideGetNameSuperiors = value;
    }

    public void setOverrideGetNameInferiors(String[] value)
    {
        this.overrideGetNameInferiors = value;
    }

    public void setAfterGetNameSuperiors(String[] value)
    {
        this.afterGetNameSuperiors = value;
    }

    public void setAfterGetNameInferiors(String[] value)
    {
        this.afterGetNameInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeGetSizeSuperiors()
    {
        return this.beforeGetSizeSuperiors;
    }

    public String[] getBeforeGetSizeInferiors()
    {
        return this.beforeGetSizeInferiors;
    }

    public String[] getOverrideGetSizeSuperiors()
    {
        return this.overrideGetSizeSuperiors;
    }

    public String[] getOverrideGetSizeInferiors()
    {
        return this.overrideGetSizeInferiors;
    }

    public String[] getAfterGetSizeSuperiors()
    {
        return this.afterGetSizeSuperiors;
    }

    public String[] getAfterGetSizeInferiors()
    {
        return this.afterGetSizeInferiors;
    }

    public void setBeforeGetSizeSuperiors(String[] value)
    {
        this.beforeGetSizeSuperiors = value;
    }

    public void setBeforeGetSizeInferiors(String[] value)
    {
        this.beforeGetSizeInferiors = value;
    }

    public void setOverrideGetSizeSuperiors(String[] value)
    {
        this.overrideGetSizeSuperiors = value;
    }

    public void setOverrideGetSizeInferiors(String[] value)
    {
        this.overrideGetSizeInferiors = value;
    }

    public void setAfterGetSizeSuperiors(String[] value)
    {
        this.afterGetSizeSuperiors = value;
    }

    public void setAfterGetSizeInferiors(String[] value)
    {
        this.afterGetSizeInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeGetSleepTimerSuperiors()
    {
        return this.beforeGetSleepTimerSuperiors;
    }

    public String[] getBeforeGetSleepTimerInferiors()
    {
        return this.beforeGetSleepTimerInferiors;
    }

    public String[] getOverrideGetSleepTimerSuperiors()
    {
        return this.overrideGetSleepTimerSuperiors;
    }

    public String[] getOverrideGetSleepTimerInferiors()
    {
        return this.overrideGetSleepTimerInferiors;
    }

    public String[] getAfterGetSleepTimerSuperiors()
    {
        return this.afterGetSleepTimerSuperiors;
    }

    public String[] getAfterGetSleepTimerInferiors()
    {
        return this.afterGetSleepTimerInferiors;
    }

    public void setBeforeGetSleepTimerSuperiors(String[] value)
    {
        this.beforeGetSleepTimerSuperiors = value;
    }

    public void setBeforeGetSleepTimerInferiors(String[] value)
    {
        this.beforeGetSleepTimerInferiors = value;
    }

    public void setOverrideGetSleepTimerSuperiors(String[] value)
    {
        this.overrideGetSleepTimerSuperiors = value;
    }

    public void setOverrideGetSleepTimerInferiors(String[] value)
    {
        this.overrideGetSleepTimerInferiors = value;
    }

    public void setAfterGetSleepTimerSuperiors(String[] value)
    {
        this.afterGetSleepTimerSuperiors = value;
    }

    public void setAfterGetSleepTimerInferiors(String[] value)
    {
        this.afterGetSleepTimerInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeGetStandingEyeHeightSuperiors()
    {
        return this.beforeGetStandingEyeHeightSuperiors;
    }

    public String[] getBeforeGetStandingEyeHeightInferiors()
    {
        return this.beforeGetStandingEyeHeightInferiors;
    }

    public String[] getOverrideGetStandingEyeHeightSuperiors()
    {
        return this.overrideGetStandingEyeHeightSuperiors;
    }

    public String[] getOverrideGetStandingEyeHeightInferiors()
    {
        return this.overrideGetStandingEyeHeightInferiors;
    }

    public String[] getAfterGetStandingEyeHeightSuperiors()
    {
        return this.afterGetStandingEyeHeightSuperiors;
    }

    public String[] getAfterGetStandingEyeHeightInferiors()
    {
        return this.afterGetStandingEyeHeightInferiors;
    }

    public void setBeforeGetStandingEyeHeightSuperiors(String[] value)
    {
        this.beforeGetStandingEyeHeightSuperiors = value;
    }

    public void setBeforeGetStandingEyeHeightInferiors(String[] value)
    {
        this.beforeGetStandingEyeHeightInferiors = value;
    }

    public void setOverrideGetStandingEyeHeightSuperiors(String[] value)
    {
        this.overrideGetStandingEyeHeightSuperiors = value;
    }

    public void setOverrideGetStandingEyeHeightInferiors(String[] value)
    {
        this.overrideGetStandingEyeHeightInferiors = value;
    }

    public void setAfterGetStandingEyeHeightSuperiors(String[] value)
    {
        this.afterGetStandingEyeHeightSuperiors = value;
    }

    public void setAfterGetStandingEyeHeightInferiors(String[] value)
    {
        this.afterGetStandingEyeHeightInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeGiveExperiencePointsSuperiors()
    {
        return this.beforeGiveExperiencePointsSuperiors;
    }

    public String[] getBeforeGiveExperiencePointsInferiors()
    {
        return this.beforeGiveExperiencePointsInferiors;
    }

    public String[] getOverrideGiveExperiencePointsSuperiors()
    {
        return this.overrideGiveExperiencePointsSuperiors;
    }

    public String[] getOverrideGiveExperiencePointsInferiors()
    {
        return this.overrideGiveExperiencePointsInferiors;
    }

    public String[] getAfterGiveExperiencePointsSuperiors()
    {
        return this.afterGiveExperiencePointsSuperiors;
    }

    public String[] getAfterGiveExperiencePointsInferiors()
    {
        return this.afterGiveExperiencePointsInferiors;
    }

    public void setBeforeGiveExperiencePointsSuperiors(String[] value)
    {
        this.beforeGiveExperiencePointsSuperiors = value;
    }

    public void setBeforeGiveExperiencePointsInferiors(String[] value)
    {
        this.beforeGiveExperiencePointsInferiors = value;
    }

    public void setOverrideGiveExperiencePointsSuperiors(String[] value)
    {
        this.overrideGiveExperiencePointsSuperiors = value;
    }

    public void setOverrideGiveExperiencePointsInferiors(String[] value)
    {
        this.overrideGiveExperiencePointsInferiors = value;
    }

    public void setAfterGiveExperiencePointsSuperiors(String[] value)
    {
        this.afterGiveExperiencePointsSuperiors = value;
    }

    public void setAfterGiveExperiencePointsInferiors(String[] value)
    {
        this.afterGiveExperiencePointsInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeHandleWaterMovementSuperiors()
    {
        return this.beforeHandleWaterMovementSuperiors;
    }

    public String[] getBeforeHandleWaterMovementInferiors()
    {
        return this.beforeHandleWaterMovementInferiors;
    }

    public String[] getOverrideHandleWaterMovementSuperiors()
    {
        return this.overrideHandleWaterMovementSuperiors;
    }

    public String[] getOverrideHandleWaterMovementInferiors()
    {
        return this.overrideHandleWaterMovementInferiors;
    }

    public String[] getAfterHandleWaterMovementSuperiors()
    {
        return this.afterHandleWaterMovementSuperiors;
    }

    public String[] getAfterHandleWaterMovementInferiors()
    {
        return this.afterHandleWaterMovementInferiors;
    }

    public void setBeforeHandleWaterMovementSuperiors(String[] value)
    {
        this.beforeHandleWaterMovementSuperiors = value;
    }

    public void setBeforeHandleWaterMovementInferiors(String[] value)
    {
        this.beforeHandleWaterMovementInferiors = value;
    }

    public void setOverrideHandleWaterMovementSuperiors(String[] value)
    {
        this.overrideHandleWaterMovementSuperiors = value;
    }

    public void setOverrideHandleWaterMovementInferiors(String[] value)
    {
        this.overrideHandleWaterMovementInferiors = value;
    }

    public void setAfterHandleWaterMovementSuperiors(String[] value)
    {
        this.afterHandleWaterMovementSuperiors = value;
    }

    public void setAfterHandleWaterMovementInferiors(String[] value)
    {
        this.afterHandleWaterMovementInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeHealSuperiors()
    {
        return this.beforeHealSuperiors;
    }

    public String[] getBeforeHealInferiors()
    {
        return this.beforeHealInferiors;
    }

    public String[] getOverrideHealSuperiors()
    {
        return this.overrideHealSuperiors;
    }

    public String[] getOverrideHealInferiors()
    {
        return this.overrideHealInferiors;
    }

    public String[] getAfterHealSuperiors()
    {
        return this.afterHealSuperiors;
    }

    public String[] getAfterHealInferiors()
    {
        return this.afterHealInferiors;
    }

    public void setBeforeHealSuperiors(String[] value)
    {
        this.beforeHealSuperiors = value;
    }

    public void setBeforeHealInferiors(String[] value)
    {
        this.beforeHealInferiors = value;
    }

    public void setOverrideHealSuperiors(String[] value)
    {
        this.overrideHealSuperiors = value;
    }

    public void setOverrideHealInferiors(String[] value)
    {
        this.overrideHealInferiors = value;
    }

    public void setAfterHealSuperiors(String[] value)
    {
        this.afterHealSuperiors = value;
    }

    public void setAfterHealInferiors(String[] value)
    {
        this.afterHealInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeIsEntityInsideOpaqueBlockSuperiors()
    {
        return this.beforeIsEntityInsideOpaqueBlockSuperiors;
    }

    public String[] getBeforeIsEntityInsideOpaqueBlockInferiors()
    {
        return this.beforeIsEntityInsideOpaqueBlockInferiors;
    }

    public String[] getOverrideIsEntityInsideOpaqueBlockSuperiors()
    {
        return this.overrideIsEntityInsideOpaqueBlockSuperiors;
    }

    public String[] getOverrideIsEntityInsideOpaqueBlockInferiors()
    {
        return this.overrideIsEntityInsideOpaqueBlockInferiors;
    }

    public String[] getAfterIsEntityInsideOpaqueBlockSuperiors()
    {
        return this.afterIsEntityInsideOpaqueBlockSuperiors;
    }

    public String[] getAfterIsEntityInsideOpaqueBlockInferiors()
    {
        return this.afterIsEntityInsideOpaqueBlockInferiors;
    }

    public void setBeforeIsEntityInsideOpaqueBlockSuperiors(String[] value)
    {
        this.beforeIsEntityInsideOpaqueBlockSuperiors = value;
    }

    public void setBeforeIsEntityInsideOpaqueBlockInferiors(String[] value)
    {
        this.beforeIsEntityInsideOpaqueBlockInferiors = value;
    }

    public void setOverrideIsEntityInsideOpaqueBlockSuperiors(String[] value)
    {
        this.overrideIsEntityInsideOpaqueBlockSuperiors = value;
    }

    public void setOverrideIsEntityInsideOpaqueBlockInferiors(String[] value)
    {
        this.overrideIsEntityInsideOpaqueBlockInferiors = value;
    }

    public void setAfterIsEntityInsideOpaqueBlockSuperiors(String[] value)
    {
        this.afterIsEntityInsideOpaqueBlockSuperiors = value;
    }

    public void setAfterIsEntityInsideOpaqueBlockInferiors(String[] value)
    {
        this.afterIsEntityInsideOpaqueBlockInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeIsInWaterSuperiors()
    {
        return this.beforeIsInWaterSuperiors;
    }

    public String[] getBeforeIsInWaterInferiors()
    {
        return this.beforeIsInWaterInferiors;
    }

    public String[] getOverrideIsInWaterSuperiors()
    {
        return this.overrideIsInWaterSuperiors;
    }

    public String[] getOverrideIsInWaterInferiors()
    {
        return this.overrideIsInWaterInferiors;
    }

    public String[] getAfterIsInWaterSuperiors()
    {
        return this.afterIsInWaterSuperiors;
    }

    public String[] getAfterIsInWaterInferiors()
    {
        return this.afterIsInWaterInferiors;
    }

    public void setBeforeIsInWaterSuperiors(String[] value)
    {
        this.beforeIsInWaterSuperiors = value;
    }

    public void setBeforeIsInWaterInferiors(String[] value)
    {
        this.beforeIsInWaterInferiors = value;
    }

    public void setOverrideIsInWaterSuperiors(String[] value)
    {
        this.overrideIsInWaterSuperiors = value;
    }

    public void setOverrideIsInWaterInferiors(String[] value)
    {
        this.overrideIsInWaterInferiors = value;
    }

    public void setAfterIsInWaterSuperiors(String[] value)
    {
        this.afterIsInWaterSuperiors = value;
    }

    public void setAfterIsInWaterInferiors(String[] value)
    {
        this.afterIsInWaterInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeIsOnLadderSuperiors()
    {
        return this.beforeIsOnLadderSuperiors;
    }

    public String[] getBeforeIsOnLadderInferiors()
    {
        return this.beforeIsOnLadderInferiors;
    }

    public String[] getOverrideIsOnLadderSuperiors()
    {
        return this.overrideIsOnLadderSuperiors;
    }

    public String[] getOverrideIsOnLadderInferiors()
    {
        return this.overrideIsOnLadderInferiors;
    }

    public String[] getAfterIsOnLadderSuperiors()
    {
        return this.afterIsOnLadderSuperiors;
    }

    public String[] getAfterIsOnLadderInferiors()
    {
        return this.afterIsOnLadderInferiors;
    }

    public void setBeforeIsOnLadderSuperiors(String[] value)
    {
        this.beforeIsOnLadderSuperiors = value;
    }

    public void setBeforeIsOnLadderInferiors(String[] value)
    {
        this.beforeIsOnLadderInferiors = value;
    }

    public void setOverrideIsOnLadderSuperiors(String[] value)
    {
        this.overrideIsOnLadderSuperiors = value;
    }

    public void setOverrideIsOnLadderInferiors(String[] value)
    {
        this.overrideIsOnLadderInferiors = value;
    }

    public void setAfterIsOnLadderSuperiors(String[] value)
    {
        this.afterIsOnLadderSuperiors = value;
    }

    public void setAfterIsOnLadderInferiors(String[] value)
    {
        this.afterIsOnLadderInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeIsShiftKeyDownSuperiors()
    {
        return this.beforeIsShiftKeyDownSuperiors;
    }

    public String[] getBeforeIsShiftKeyDownInferiors()
    {
        return this.beforeIsShiftKeyDownInferiors;
    }

    public String[] getOverrideIsShiftKeyDownSuperiors()
    {
        return this.overrideIsShiftKeyDownSuperiors;
    }

    public String[] getOverrideIsShiftKeyDownInferiors()
    {
        return this.overrideIsShiftKeyDownInferiors;
    }

    public String[] getAfterIsShiftKeyDownSuperiors()
    {
        return this.afterIsShiftKeyDownSuperiors;
    }

    public String[] getAfterIsShiftKeyDownInferiors()
    {
        return this.afterIsShiftKeyDownInferiors;
    }

    public void setBeforeIsShiftKeyDownSuperiors(String[] value)
    {
        this.beforeIsShiftKeyDownSuperiors = value;
    }

    public void setBeforeIsShiftKeyDownInferiors(String[] value)
    {
        this.beforeIsShiftKeyDownInferiors = value;
    }

    public void setOverrideIsShiftKeyDownSuperiors(String[] value)
    {
        this.overrideIsShiftKeyDownSuperiors = value;
    }

    public void setOverrideIsShiftKeyDownInferiors(String[] value)
    {
        this.overrideIsShiftKeyDownInferiors = value;
    }

    public void setAfterIsShiftKeyDownSuperiors(String[] value)
    {
        this.afterIsShiftKeyDownSuperiors = value;
    }

    public void setAfterIsShiftKeyDownInferiors(String[] value)
    {
        this.afterIsShiftKeyDownInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeIsSleepingSuperiors()
    {
        return this.beforeIsSleepingSuperiors;
    }

    public String[] getBeforeIsSleepingInferiors()
    {
        return this.beforeIsSleepingInferiors;
    }

    public String[] getOverrideIsSleepingSuperiors()
    {
        return this.overrideIsSleepingSuperiors;
    }

    public String[] getOverrideIsSleepingInferiors()
    {
        return this.overrideIsSleepingInferiors;
    }

    public String[] getAfterIsSleepingSuperiors()
    {
        return this.afterIsSleepingSuperiors;
    }

    public String[] getAfterIsSleepingInferiors()
    {
        return this.afterIsSleepingInferiors;
    }

    public void setBeforeIsSleepingSuperiors(String[] value)
    {
        this.beforeIsSleepingSuperiors = value;
    }

    public void setBeforeIsSleepingInferiors(String[] value)
    {
        this.beforeIsSleepingInferiors = value;
    }

    public void setOverrideIsSleepingSuperiors(String[] value)
    {
        this.overrideIsSleepingSuperiors = value;
    }

    public void setOverrideIsSleepingInferiors(String[] value)
    {
        this.overrideIsSleepingInferiors = value;
    }

    public void setAfterIsSleepingSuperiors(String[] value)
    {
        this.afterIsSleepingSuperiors = value;
    }

    public void setAfterIsSleepingInferiors(String[] value)
    {
        this.afterIsSleepingInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeIsSprintingSuperiors()
    {
        return this.beforeIsSprintingSuperiors;
    }

    public String[] getBeforeIsSprintingInferiors()
    {
        return this.beforeIsSprintingInferiors;
    }

    public String[] getOverrideIsSprintingSuperiors()
    {
        return this.overrideIsSprintingSuperiors;
    }

    public String[] getOverrideIsSprintingInferiors()
    {
        return this.overrideIsSprintingInferiors;
    }

    public String[] getAfterIsSprintingSuperiors()
    {
        return this.afterIsSprintingSuperiors;
    }

    public String[] getAfterIsSprintingInferiors()
    {
        return this.afterIsSprintingInferiors;
    }

    public void setBeforeIsSprintingSuperiors(String[] value)
    {
        this.beforeIsSprintingSuperiors = value;
    }

    public void setBeforeIsSprintingInferiors(String[] value)
    {
        this.beforeIsSprintingInferiors = value;
    }

    public void setOverrideIsSprintingSuperiors(String[] value)
    {
        this.overrideIsSprintingSuperiors = value;
    }

    public void setOverrideIsSprintingInferiors(String[] value)
    {
        this.overrideIsSprintingInferiors = value;
    }

    public void setAfterIsSprintingSuperiors(String[] value)
    {
        this.afterIsSprintingSuperiors = value;
    }

    public void setAfterIsSprintingInferiors(String[] value)
    {
        this.afterIsSprintingInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeJumpSuperiors()
    {
        return this.beforeJumpSuperiors;
    }

    public String[] getBeforeJumpInferiors()
    {
        return this.beforeJumpInferiors;
    }

    public String[] getOverrideJumpSuperiors()
    {
        return this.overrideJumpSuperiors;
    }

    public String[] getOverrideJumpInferiors()
    {
        return this.overrideJumpInferiors;
    }

    public String[] getAfterJumpSuperiors()
    {
        return this.afterJumpSuperiors;
    }

    public String[] getAfterJumpInferiors()
    {
        return this.afterJumpInferiors;
    }

    public void setBeforeJumpSuperiors(String[] value)
    {
        this.beforeJumpSuperiors = value;
    }

    public void setBeforeJumpInferiors(String[] value)
    {
        this.beforeJumpInferiors = value;
    }

    public void setOverrideJumpSuperiors(String[] value)
    {
        this.overrideJumpSuperiors = value;
    }

    public void setOverrideJumpInferiors(String[] value)
    {
        this.overrideJumpInferiors = value;
    }

    public void setAfterJumpSuperiors(String[] value)
    {
        this.afterJumpSuperiors = value;
    }

    public void setAfterJumpInferiors(String[] value)
    {
        this.afterJumpInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeKnockBackSuperiors()
    {
        return this.beforeKnockBackSuperiors;
    }

    public String[] getBeforeKnockBackInferiors()
    {
        return this.beforeKnockBackInferiors;
    }

    public String[] getOverrideKnockBackSuperiors()
    {
        return this.overrideKnockBackSuperiors;
    }

    public String[] getOverrideKnockBackInferiors()
    {
        return this.overrideKnockBackInferiors;
    }

    public String[] getAfterKnockBackSuperiors()
    {
        return this.afterKnockBackSuperiors;
    }

    public String[] getAfterKnockBackInferiors()
    {
        return this.afterKnockBackInferiors;
    }

    public void setBeforeKnockBackSuperiors(String[] value)
    {
        this.beforeKnockBackSuperiors = value;
    }

    public void setBeforeKnockBackInferiors(String[] value)
    {
        this.beforeKnockBackInferiors = value;
    }

    public void setOverrideKnockBackSuperiors(String[] value)
    {
        this.overrideKnockBackSuperiors = value;
    }

    public void setOverrideKnockBackInferiors(String[] value)
    {
        this.overrideKnockBackInferiors = value;
    }

    public void setAfterKnockBackSuperiors(String[] value)
    {
        this.afterKnockBackSuperiors = value;
    }

    public void setAfterKnockBackInferiors(String[] value)
    {
        this.afterKnockBackInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeLivingTickSuperiors()
    {
        return this.beforeLivingTickSuperiors;
    }

    public String[] getBeforeLivingTickInferiors()
    {
        return this.beforeLivingTickInferiors;
    }

    public String[] getOverrideLivingTickSuperiors()
    {
        return this.overrideLivingTickSuperiors;
    }

    public String[] getOverrideLivingTickInferiors()
    {
        return this.overrideLivingTickInferiors;
    }

    public String[] getAfterLivingTickSuperiors()
    {
        return this.afterLivingTickSuperiors;
    }

    public String[] getAfterLivingTickInferiors()
    {
        return this.afterLivingTickInferiors;
    }

    public void setBeforeLivingTickSuperiors(String[] value)
    {
        this.beforeLivingTickSuperiors = value;
    }

    public void setBeforeLivingTickInferiors(String[] value)
    {
        this.beforeLivingTickInferiors = value;
    }

    public void setOverrideLivingTickSuperiors(String[] value)
    {
        this.overrideLivingTickSuperiors = value;
    }

    public void setOverrideLivingTickInferiors(String[] value)
    {
        this.overrideLivingTickInferiors = value;
    }

    public void setAfterLivingTickSuperiors(String[] value)
    {
        this.afterLivingTickSuperiors = value;
    }

    public void setAfterLivingTickInferiors(String[] value)
    {
        this.afterLivingTickInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeMoveSuperiors()
    {
        return this.beforeMoveSuperiors;
    }

    public String[] getBeforeMoveInferiors()
    {
        return this.beforeMoveInferiors;
    }

    public String[] getOverrideMoveSuperiors()
    {
        return this.overrideMoveSuperiors;
    }

    public String[] getOverrideMoveInferiors()
    {
        return this.overrideMoveInferiors;
    }

    public String[] getAfterMoveSuperiors()
    {
        return this.afterMoveSuperiors;
    }

    public String[] getAfterMoveInferiors()
    {
        return this.afterMoveInferiors;
    }

    public void setBeforeMoveSuperiors(String[] value)
    {
        this.beforeMoveSuperiors = value;
    }

    public void setBeforeMoveInferiors(String[] value)
    {
        this.beforeMoveInferiors = value;
    }

    public void setOverrideMoveSuperiors(String[] value)
    {
        this.overrideMoveSuperiors = value;
    }

    public void setOverrideMoveInferiors(String[] value)
    {
        this.overrideMoveInferiors = value;
    }

    public void setAfterMoveSuperiors(String[] value)
    {
        this.afterMoveSuperiors = value;
    }

    public void setAfterMoveInferiors(String[] value)
    {
        this.afterMoveInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeMoveRelativeSuperiors()
    {
        return this.beforeMoveRelativeSuperiors;
    }

    public String[] getBeforeMoveRelativeInferiors()
    {
        return this.beforeMoveRelativeInferiors;
    }

    public String[] getOverrideMoveRelativeSuperiors()
    {
        return this.overrideMoveRelativeSuperiors;
    }

    public String[] getOverrideMoveRelativeInferiors()
    {
        return this.overrideMoveRelativeInferiors;
    }

    public String[] getAfterMoveRelativeSuperiors()
    {
        return this.afterMoveRelativeSuperiors;
    }

    public String[] getAfterMoveRelativeInferiors()
    {
        return this.afterMoveRelativeInferiors;
    }

    public void setBeforeMoveRelativeSuperiors(String[] value)
    {
        this.beforeMoveRelativeSuperiors = value;
    }

    public void setBeforeMoveRelativeInferiors(String[] value)
    {
        this.beforeMoveRelativeInferiors = value;
    }

    public void setOverrideMoveRelativeSuperiors(String[] value)
    {
        this.overrideMoveRelativeSuperiors = value;
    }

    public void setOverrideMoveRelativeInferiors(String[] value)
    {
        this.overrideMoveRelativeInferiors = value;
    }

    public void setAfterMoveRelativeSuperiors(String[] value)
    {
        this.afterMoveRelativeSuperiors = value;
    }

    public void setAfterMoveRelativeInferiors(String[] value)
    {
        this.afterMoveRelativeInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeOnDeathSuperiors()
    {
        return this.beforeOnDeathSuperiors;
    }

    public String[] getBeforeOnDeathInferiors()
    {
        return this.beforeOnDeathInferiors;
    }

    public String[] getOverrideOnDeathSuperiors()
    {
        return this.overrideOnDeathSuperiors;
    }

    public String[] getOverrideOnDeathInferiors()
    {
        return this.overrideOnDeathInferiors;
    }

    public String[] getAfterOnDeathSuperiors()
    {
        return this.afterOnDeathSuperiors;
    }

    public String[] getAfterOnDeathInferiors()
    {
        return this.afterOnDeathInferiors;
    }

    public void setBeforeOnDeathSuperiors(String[] value)
    {
        this.beforeOnDeathSuperiors = value;
    }

    public void setBeforeOnDeathInferiors(String[] value)
    {
        this.beforeOnDeathInferiors = value;
    }

    public void setOverrideOnDeathSuperiors(String[] value)
    {
        this.overrideOnDeathSuperiors = value;
    }

    public void setOverrideOnDeathInferiors(String[] value)
    {
        this.overrideOnDeathInferiors = value;
    }

    public void setAfterOnDeathSuperiors(String[] value)
    {
        this.afterOnDeathSuperiors = value;
    }

    public void setAfterOnDeathInferiors(String[] value)
    {
        this.afterOnDeathInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeOnKillEntitySuperiors()
    {
        return this.beforeOnKillEntitySuperiors;
    }

    public String[] getBeforeOnKillEntityInferiors()
    {
        return this.beforeOnKillEntityInferiors;
    }

    public String[] getOverrideOnKillEntitySuperiors()
    {
        return this.overrideOnKillEntitySuperiors;
    }

    public String[] getOverrideOnKillEntityInferiors()
    {
        return this.overrideOnKillEntityInferiors;
    }

    public String[] getAfterOnKillEntitySuperiors()
    {
        return this.afterOnKillEntitySuperiors;
    }

    public String[] getAfterOnKillEntityInferiors()
    {
        return this.afterOnKillEntityInferiors;
    }

    public void setBeforeOnKillEntitySuperiors(String[] value)
    {
        this.beforeOnKillEntitySuperiors = value;
    }

    public void setBeforeOnKillEntityInferiors(String[] value)
    {
        this.beforeOnKillEntityInferiors = value;
    }

    public void setOverrideOnKillEntitySuperiors(String[] value)
    {
        this.overrideOnKillEntitySuperiors = value;
    }

    public void setOverrideOnKillEntityInferiors(String[] value)
    {
        this.overrideOnKillEntityInferiors = value;
    }

    public void setAfterOnKillEntitySuperiors(String[] value)
    {
        this.afterOnKillEntitySuperiors = value;
    }

    public void setAfterOnKillEntityInferiors(String[] value)
    {
        this.afterOnKillEntityInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeOnLivingFallSuperiors()
    {
        return this.beforeOnLivingFallSuperiors;
    }

    public String[] getBeforeOnLivingFallInferiors()
    {
        return this.beforeOnLivingFallInferiors;
    }

    public String[] getOverrideOnLivingFallSuperiors()
    {
        return this.overrideOnLivingFallSuperiors;
    }

    public String[] getOverrideOnLivingFallInferiors()
    {
        return this.overrideOnLivingFallInferiors;
    }

    public String[] getAfterOnLivingFallSuperiors()
    {
        return this.afterOnLivingFallSuperiors;
    }

    public String[] getAfterOnLivingFallInferiors()
    {
        return this.afterOnLivingFallInferiors;
    }

    public void setBeforeOnLivingFallSuperiors(String[] value)
    {
        this.beforeOnLivingFallSuperiors = value;
    }

    public void setBeforeOnLivingFallInferiors(String[] value)
    {
        this.beforeOnLivingFallInferiors = value;
    }

    public void setOverrideOnLivingFallSuperiors(String[] value)
    {
        this.overrideOnLivingFallSuperiors = value;
    }

    public void setOverrideOnLivingFallInferiors(String[] value)
    {
        this.overrideOnLivingFallInferiors = value;
    }

    public void setAfterOnLivingFallSuperiors(String[] value)
    {
        this.afterOnLivingFallSuperiors = value;
    }

    public void setAfterOnLivingFallInferiors(String[] value)
    {
        this.afterOnLivingFallInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeOnStruckByLightningSuperiors()
    {
        return this.beforeOnStruckByLightningSuperiors;
    }

    public String[] getBeforeOnStruckByLightningInferiors()
    {
        return this.beforeOnStruckByLightningInferiors;
    }

    public String[] getOverrideOnStruckByLightningSuperiors()
    {
        return this.overrideOnStruckByLightningSuperiors;
    }

    public String[] getOverrideOnStruckByLightningInferiors()
    {
        return this.overrideOnStruckByLightningInferiors;
    }

    public String[] getAfterOnStruckByLightningSuperiors()
    {
        return this.afterOnStruckByLightningSuperiors;
    }

    public String[] getAfterOnStruckByLightningInferiors()
    {
        return this.afterOnStruckByLightningInferiors;
    }

    public void setBeforeOnStruckByLightningSuperiors(String[] value)
    {
        this.beforeOnStruckByLightningSuperiors = value;
    }

    public void setBeforeOnStruckByLightningInferiors(String[] value)
    {
        this.beforeOnStruckByLightningInferiors = value;
    }

    public void setOverrideOnStruckByLightningSuperiors(String[] value)
    {
        this.overrideOnStruckByLightningSuperiors = value;
    }

    public void setOverrideOnStruckByLightningInferiors(String[] value)
    {
        this.overrideOnStruckByLightningInferiors = value;
    }

    public void setAfterOnStruckByLightningSuperiors(String[] value)
    {
        this.afterOnStruckByLightningSuperiors = value;
    }

    public void setAfterOnStruckByLightningInferiors(String[] value)
    {
        this.afterOnStruckByLightningInferiors = value;
    }

    // ############################################################################

    public String[] getBeforePickSuperiors()
    {
        return this.beforePickSuperiors;
    }

    public String[] getBeforePickInferiors()
    {
        return this.beforePickInferiors;
    }

    public String[] getOverridePickSuperiors()
    {
        return this.overridePickSuperiors;
    }

    public String[] getOverridePickInferiors()
    {
        return this.overridePickInferiors;
    }

    public String[] getAfterPickSuperiors()
    {
        return this.afterPickSuperiors;
    }

    public String[] getAfterPickInferiors()
    {
        return this.afterPickInferiors;
    }

    public void setBeforePickSuperiors(String[] value)
    {
        this.beforePickSuperiors = value;
    }

    public void setBeforePickInferiors(String[] value)
    {
        this.beforePickInferiors = value;
    }

    public void setOverridePickSuperiors(String[] value)
    {
        this.overridePickSuperiors = value;
    }

    public void setOverridePickInferiors(String[] value)
    {
        this.overridePickInferiors = value;
    }

    public void setAfterPickSuperiors(String[] value)
    {
        this.afterPickSuperiors = value;
    }

    public void setAfterPickInferiors(String[] value)
    {
        this.afterPickInferiors = value;
    }

    // ############################################################################

    public String[] getBeforePlayerTickSuperiors()
    {
        return this.beforePlayerTickSuperiors;
    }

    public String[] getBeforePlayerTickInferiors()
    {
        return this.beforePlayerTickInferiors;
    }

    public String[] getOverridePlayerTickSuperiors()
    {
        return this.overridePlayerTickSuperiors;
    }

    public String[] getOverridePlayerTickInferiors()
    {
        return this.overridePlayerTickInferiors;
    }

    public String[] getAfterPlayerTickSuperiors()
    {
        return this.afterPlayerTickSuperiors;
    }

    public String[] getAfterPlayerTickInferiors()
    {
        return this.afterPlayerTickInferiors;
    }

    public void setBeforePlayerTickSuperiors(String[] value)
    {
        this.beforePlayerTickSuperiors = value;
    }

    public void setBeforePlayerTickInferiors(String[] value)
    {
        this.beforePlayerTickInferiors = value;
    }

    public void setOverridePlayerTickSuperiors(String[] value)
    {
        this.overridePlayerTickSuperiors = value;
    }

    public void setOverridePlayerTickInferiors(String[] value)
    {
        this.overridePlayerTickInferiors = value;
    }

    public void setAfterPlayerTickSuperiors(String[] value)
    {
        this.afterPlayerTickSuperiors = value;
    }

    public void setAfterPlayerTickInferiors(String[] value)
    {
        this.afterPlayerTickInferiors = value;
    }

    // ############################################################################

    public String[] getBeforePlayStepSoundSuperiors()
    {
        return this.beforePlayStepSoundSuperiors;
    }

    public String[] getBeforePlayStepSoundInferiors()
    {
        return this.beforePlayStepSoundInferiors;
    }

    public String[] getOverridePlayStepSoundSuperiors()
    {
        return this.overridePlayStepSoundSuperiors;
    }

    public String[] getOverridePlayStepSoundInferiors()
    {
        return this.overridePlayStepSoundInferiors;
    }

    public String[] getAfterPlayStepSoundSuperiors()
    {
        return this.afterPlayStepSoundSuperiors;
    }

    public String[] getAfterPlayStepSoundInferiors()
    {
        return this.afterPlayStepSoundInferiors;
    }

    public void setBeforePlayStepSoundSuperiors(String[] value)
    {
        this.beforePlayStepSoundSuperiors = value;
    }

    public void setBeforePlayStepSoundInferiors(String[] value)
    {
        this.beforePlayStepSoundInferiors = value;
    }

    public void setOverridePlayStepSoundSuperiors(String[] value)
    {
        this.overridePlayStepSoundSuperiors = value;
    }

    public void setOverridePlayStepSoundInferiors(String[] value)
    {
        this.overridePlayStepSoundInferiors = value;
    }

    public void setAfterPlayStepSoundSuperiors(String[] value)
    {
        this.afterPlayStepSoundSuperiors = value;
    }

    public void setAfterPlayStepSoundInferiors(String[] value)
    {
        this.afterPlayStepSoundInferiors = value;
    }

    // ############################################################################

    public String[] getBeforePushOutOfBlocksSuperiors()
    {
        return this.beforePushOutOfBlocksSuperiors;
    }

    public String[] getBeforePushOutOfBlocksInferiors()
    {
        return this.beforePushOutOfBlocksInferiors;
    }

    public String[] getOverridePushOutOfBlocksSuperiors()
    {
        return this.overridePushOutOfBlocksSuperiors;
    }

    public String[] getOverridePushOutOfBlocksInferiors()
    {
        return this.overridePushOutOfBlocksInferiors;
    }

    public String[] getAfterPushOutOfBlocksSuperiors()
    {
        return this.afterPushOutOfBlocksSuperiors;
    }

    public String[] getAfterPushOutOfBlocksInferiors()
    {
        return this.afterPushOutOfBlocksInferiors;
    }

    public void setBeforePushOutOfBlocksSuperiors(String[] value)
    {
        this.beforePushOutOfBlocksSuperiors = value;
    }

    public void setBeforePushOutOfBlocksInferiors(String[] value)
    {
        this.beforePushOutOfBlocksInferiors = value;
    }

    public void setOverridePushOutOfBlocksSuperiors(String[] value)
    {
        this.overridePushOutOfBlocksSuperiors = value;
    }

    public void setOverridePushOutOfBlocksInferiors(String[] value)
    {
        this.overridePushOutOfBlocksInferiors = value;
    }

    public void setAfterPushOutOfBlocksSuperiors(String[] value)
    {
        this.afterPushOutOfBlocksSuperiors = value;
    }

    public void setAfterPushOutOfBlocksInferiors(String[] value)
    {
        this.afterPushOutOfBlocksInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeReadSuperiors()
    {
        return this.beforeReadSuperiors;
    }

    public String[] getBeforeReadInferiors()
    {
        return this.beforeReadInferiors;
    }

    public String[] getOverrideReadSuperiors()
    {
        return this.overrideReadSuperiors;
    }

    public String[] getOverrideReadInferiors()
    {
        return this.overrideReadInferiors;
    }

    public String[] getAfterReadSuperiors()
    {
        return this.afterReadSuperiors;
    }

    public String[] getAfterReadInferiors()
    {
        return this.afterReadInferiors;
    }

    public void setBeforeReadSuperiors(String[] value)
    {
        this.beforeReadSuperiors = value;
    }

    public void setBeforeReadInferiors(String[] value)
    {
        this.beforeReadInferiors = value;
    }

    public void setOverrideReadSuperiors(String[] value)
    {
        this.overrideReadSuperiors = value;
    }

    public void setOverrideReadInferiors(String[] value)
    {
        this.overrideReadInferiors = value;
    }

    public void setAfterReadSuperiors(String[] value)
    {
        this.afterReadSuperiors = value;
    }

    public void setAfterReadInferiors(String[] value)
    {
        this.afterReadInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeRemoveSuperiors()
    {
        return this.beforeRemoveSuperiors;
    }

    public String[] getBeforeRemoveInferiors()
    {
        return this.beforeRemoveInferiors;
    }

    public String[] getOverrideRemoveSuperiors()
    {
        return this.overrideRemoveSuperiors;
    }

    public String[] getOverrideRemoveInferiors()
    {
        return this.overrideRemoveInferiors;
    }

    public String[] getAfterRemoveSuperiors()
    {
        return this.afterRemoveSuperiors;
    }

    public String[] getAfterRemoveInferiors()
    {
        return this.afterRemoveInferiors;
    }

    public void setBeforeRemoveSuperiors(String[] value)
    {
        this.beforeRemoveSuperiors = value;
    }

    public void setBeforeRemoveInferiors(String[] value)
    {
        this.beforeRemoveInferiors = value;
    }

    public void setOverrideRemoveSuperiors(String[] value)
    {
        this.overrideRemoveSuperiors = value;
    }

    public void setOverrideRemoveInferiors(String[] value)
    {
        this.overrideRemoveInferiors = value;
    }

    public void setAfterRemoveSuperiors(String[] value)
    {
        this.afterRemoveSuperiors = value;
    }

    public void setAfterRemoveInferiors(String[] value)
    {
        this.afterRemoveInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeSetEntityActionStateSuperiors()
    {
        return this.beforeSetEntityActionStateSuperiors;
    }

    public String[] getBeforeSetEntityActionStateInferiors()
    {
        return this.beforeSetEntityActionStateInferiors;
    }

    public String[] getOverrideSetEntityActionStateSuperiors()
    {
        return this.overrideSetEntityActionStateSuperiors;
    }

    public String[] getOverrideSetEntityActionStateInferiors()
    {
        return this.overrideSetEntityActionStateInferiors;
    }

    public String[] getAfterSetEntityActionStateSuperiors()
    {
        return this.afterSetEntityActionStateSuperiors;
    }

    public String[] getAfterSetEntityActionStateInferiors()
    {
        return this.afterSetEntityActionStateInferiors;
    }

    public void setBeforeSetEntityActionStateSuperiors(String[] value)
    {
        this.beforeSetEntityActionStateSuperiors = value;
    }

    public void setBeforeSetEntityActionStateInferiors(String[] value)
    {
        this.beforeSetEntityActionStateInferiors = value;
    }

    public void setOverrideSetEntityActionStateSuperiors(String[] value)
    {
        this.overrideSetEntityActionStateSuperiors = value;
    }

    public void setOverrideSetEntityActionStateInferiors(String[] value)
    {
        this.overrideSetEntityActionStateInferiors = value;
    }

    public void setAfterSetEntityActionStateSuperiors(String[] value)
    {
        this.afterSetEntityActionStateSuperiors = value;
    }

    public void setAfterSetEntityActionStateInferiors(String[] value)
    {
        this.afterSetEntityActionStateInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeSetPositionSuperiors()
    {
        return this.beforeSetPositionSuperiors;
    }

    public String[] getBeforeSetPositionInferiors()
    {
        return this.beforeSetPositionInferiors;
    }

    public String[] getOverrideSetPositionSuperiors()
    {
        return this.overrideSetPositionSuperiors;
    }

    public String[] getOverrideSetPositionInferiors()
    {
        return this.overrideSetPositionInferiors;
    }

    public String[] getAfterSetPositionSuperiors()
    {
        return this.afterSetPositionSuperiors;
    }

    public String[] getAfterSetPositionInferiors()
    {
        return this.afterSetPositionInferiors;
    }

    public void setBeforeSetPositionSuperiors(String[] value)
    {
        this.beforeSetPositionSuperiors = value;
    }

    public void setBeforeSetPositionInferiors(String[] value)
    {
        this.beforeSetPositionInferiors = value;
    }

    public void setOverrideSetPositionSuperiors(String[] value)
    {
        this.overrideSetPositionSuperiors = value;
    }

    public void setOverrideSetPositionInferiors(String[] value)
    {
        this.overrideSetPositionInferiors = value;
    }

    public void setAfterSetPositionSuperiors(String[] value)
    {
        this.afterSetPositionSuperiors = value;
    }

    public void setAfterSetPositionInferiors(String[] value)
    {
        this.afterSetPositionInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeSetPositionAndRotationSuperiors()
    {
        return this.beforeSetPositionAndRotationSuperiors;
    }

    public String[] getBeforeSetPositionAndRotationInferiors()
    {
        return this.beforeSetPositionAndRotationInferiors;
    }

    public String[] getOverrideSetPositionAndRotationSuperiors()
    {
        return this.overrideSetPositionAndRotationSuperiors;
    }

    public String[] getOverrideSetPositionAndRotationInferiors()
    {
        return this.overrideSetPositionAndRotationInferiors;
    }

    public String[] getAfterSetPositionAndRotationSuperiors()
    {
        return this.afterSetPositionAndRotationSuperiors;
    }

    public String[] getAfterSetPositionAndRotationInferiors()
    {
        return this.afterSetPositionAndRotationInferiors;
    }

    public void setBeforeSetPositionAndRotationSuperiors(String[] value)
    {
        this.beforeSetPositionAndRotationSuperiors = value;
    }

    public void setBeforeSetPositionAndRotationInferiors(String[] value)
    {
        this.beforeSetPositionAndRotationInferiors = value;
    }

    public void setOverrideSetPositionAndRotationSuperiors(String[] value)
    {
        this.overrideSetPositionAndRotationSuperiors = value;
    }

    public void setOverrideSetPositionAndRotationInferiors(String[] value)
    {
        this.overrideSetPositionAndRotationInferiors = value;
    }

    public void setAfterSetPositionAndRotationSuperiors(String[] value)
    {
        this.afterSetPositionAndRotationSuperiors = value;
    }

    public void setAfterSetPositionAndRotationInferiors(String[] value)
    {
        this.afterSetPositionAndRotationInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeSetSneakingSuperiors()
    {
        return this.beforeSetSneakingSuperiors;
    }

    public String[] getBeforeSetSneakingInferiors()
    {
        return this.beforeSetSneakingInferiors;
    }

    public String[] getOverrideSetSneakingSuperiors()
    {
        return this.overrideSetSneakingSuperiors;
    }

    public String[] getOverrideSetSneakingInferiors()
    {
        return this.overrideSetSneakingInferiors;
    }

    public String[] getAfterSetSneakingSuperiors()
    {
        return this.afterSetSneakingSuperiors;
    }

    public String[] getAfterSetSneakingInferiors()
    {
        return this.afterSetSneakingInferiors;
    }

    public void setBeforeSetSneakingSuperiors(String[] value)
    {
        this.beforeSetSneakingSuperiors = value;
    }

    public void setBeforeSetSneakingInferiors(String[] value)
    {
        this.beforeSetSneakingInferiors = value;
    }

    public void setOverrideSetSneakingSuperiors(String[] value)
    {
        this.overrideSetSneakingSuperiors = value;
    }

    public void setOverrideSetSneakingInferiors(String[] value)
    {
        this.overrideSetSneakingInferiors = value;
    }

    public void setAfterSetSneakingSuperiors(String[] value)
    {
        this.afterSetSneakingSuperiors = value;
    }

    public void setAfterSetSneakingInferiors(String[] value)
    {
        this.afterSetSneakingInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeSetSprintingSuperiors()
    {
        return this.beforeSetSprintingSuperiors;
    }

    public String[] getBeforeSetSprintingInferiors()
    {
        return this.beforeSetSprintingInferiors;
    }

    public String[] getOverrideSetSprintingSuperiors()
    {
        return this.overrideSetSprintingSuperiors;
    }

    public String[] getOverrideSetSprintingInferiors()
    {
        return this.overrideSetSprintingInferiors;
    }

    public String[] getAfterSetSprintingSuperiors()
    {
        return this.afterSetSprintingSuperiors;
    }

    public String[] getAfterSetSprintingInferiors()
    {
        return this.afterSetSprintingInferiors;
    }

    public void setBeforeSetSprintingSuperiors(String[] value)
    {
        this.beforeSetSprintingSuperiors = value;
    }

    public void setBeforeSetSprintingInferiors(String[] value)
    {
        this.beforeSetSprintingInferiors = value;
    }

    public void setOverrideSetSprintingSuperiors(String[] value)
    {
        this.overrideSetSprintingSuperiors = value;
    }

    public void setOverrideSetSprintingInferiors(String[] value)
    {
        this.overrideSetSprintingInferiors = value;
    }

    public void setAfterSetSprintingSuperiors(String[] value)
    {
        this.afterSetSprintingSuperiors = value;
    }

    public void setAfterSetSprintingInferiors(String[] value)
    {
        this.afterSetSprintingInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeSwingArmSuperiors()
    {
        return this.beforeSwingArmSuperiors;
    }

    public String[] getBeforeSwingArmInferiors()
    {
        return this.beforeSwingArmInferiors;
    }

    public String[] getOverrideSwingArmSuperiors()
    {
        return this.overrideSwingArmSuperiors;
    }

    public String[] getOverrideSwingArmInferiors()
    {
        return this.overrideSwingArmInferiors;
    }

    public String[] getAfterSwingArmSuperiors()
    {
        return this.afterSwingArmSuperiors;
    }

    public String[] getAfterSwingArmInferiors()
    {
        return this.afterSwingArmInferiors;
    }

    public void setBeforeSwingArmSuperiors(String[] value)
    {
        this.beforeSwingArmSuperiors = value;
    }

    public void setBeforeSwingArmInferiors(String[] value)
    {
        this.beforeSwingArmInferiors = value;
    }

    public void setOverrideSwingArmSuperiors(String[] value)
    {
        this.overrideSwingArmSuperiors = value;
    }

    public void setOverrideSwingArmInferiors(String[] value)
    {
        this.overrideSwingArmInferiors = value;
    }

    public void setAfterSwingArmSuperiors(String[] value)
    {
        this.afterSwingArmSuperiors = value;
    }

    public void setAfterSwingArmInferiors(String[] value)
    {
        this.afterSwingArmInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeTickSuperiors()
    {
        return this.beforeTickSuperiors;
    }

    public String[] getBeforeTickInferiors()
    {
        return this.beforeTickInferiors;
    }

    public String[] getOverrideTickSuperiors()
    {
        return this.overrideTickSuperiors;
    }

    public String[] getOverrideTickInferiors()
    {
        return this.overrideTickInferiors;
    }

    public String[] getAfterTickSuperiors()
    {
        return this.afterTickSuperiors;
    }

    public String[] getAfterTickInferiors()
    {
        return this.afterTickInferiors;
    }

    public void setBeforeTickSuperiors(String[] value)
    {
        this.beforeTickSuperiors = value;
    }

    public void setBeforeTickInferiors(String[] value)
    {
        this.beforeTickInferiors = value;
    }

    public void setOverrideTickSuperiors(String[] value)
    {
        this.overrideTickSuperiors = value;
    }

    public void setOverrideTickInferiors(String[] value)
    {
        this.overrideTickInferiors = value;
    }

    public void setAfterTickSuperiors(String[] value)
    {
        this.afterTickSuperiors = value;
    }

    public void setAfterTickInferiors(String[] value)
    {
        this.afterTickInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeTravelSuperiors()
    {
        return this.beforeTravelSuperiors;
    }

    public String[] getBeforeTravelInferiors()
    {
        return this.beforeTravelInferiors;
    }

    public String[] getOverrideTravelSuperiors()
    {
        return this.overrideTravelSuperiors;
    }

    public String[] getOverrideTravelInferiors()
    {
        return this.overrideTravelInferiors;
    }

    public String[] getAfterTravelSuperiors()
    {
        return this.afterTravelSuperiors;
    }

    public String[] getAfterTravelInferiors()
    {
        return this.afterTravelInferiors;
    }

    public void setBeforeTravelSuperiors(String[] value)
    {
        this.beforeTravelSuperiors = value;
    }

    public void setBeforeTravelInferiors(String[] value)
    {
        this.beforeTravelInferiors = value;
    }

    public void setOverrideTravelSuperiors(String[] value)
    {
        this.overrideTravelSuperiors = value;
    }

    public void setOverrideTravelInferiors(String[] value)
    {
        this.overrideTravelInferiors = value;
    }

    public void setAfterTravelSuperiors(String[] value)
    {
        this.afterTravelSuperiors = value;
    }

    public void setAfterTravelInferiors(String[] value)
    {
        this.afterTravelInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeTrySleepSuperiors()
    {
        return this.beforeTrySleepSuperiors;
    }

    public String[] getBeforeTrySleepInferiors()
    {
        return this.beforeTrySleepInferiors;
    }

    public String[] getOverrideTrySleepSuperiors()
    {
        return this.overrideTrySleepSuperiors;
    }

    public String[] getOverrideTrySleepInferiors()
    {
        return this.overrideTrySleepInferiors;
    }

    public String[] getAfterTrySleepSuperiors()
    {
        return this.afterTrySleepSuperiors;
    }

    public String[] getAfterTrySleepInferiors()
    {
        return this.afterTrySleepInferiors;
    }

    public void setBeforeTrySleepSuperiors(String[] value)
    {
        this.beforeTrySleepSuperiors = value;
    }

    public void setBeforeTrySleepInferiors(String[] value)
    {
        this.beforeTrySleepInferiors = value;
    }

    public void setOverrideTrySleepSuperiors(String[] value)
    {
        this.overrideTrySleepSuperiors = value;
    }

    public void setOverrideTrySleepInferiors(String[] value)
    {
        this.overrideTrySleepInferiors = value;
    }

    public void setAfterTrySleepSuperiors(String[] value)
    {
        this.afterTrySleepSuperiors = value;
    }

    public void setAfterTrySleepInferiors(String[] value)
    {
        this.afterTrySleepInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeUpdateEntityActionStateSuperiors()
    {
        return this.beforeUpdateEntityActionStateSuperiors;
    }

    public String[] getBeforeUpdateEntityActionStateInferiors()
    {
        return this.beforeUpdateEntityActionStateInferiors;
    }

    public String[] getOverrideUpdateEntityActionStateSuperiors()
    {
        return this.overrideUpdateEntityActionStateSuperiors;
    }

    public String[] getOverrideUpdateEntityActionStateInferiors()
    {
        return this.overrideUpdateEntityActionStateInferiors;
    }

    public String[] getAfterUpdateEntityActionStateSuperiors()
    {
        return this.afterUpdateEntityActionStateSuperiors;
    }

    public String[] getAfterUpdateEntityActionStateInferiors()
    {
        return this.afterUpdateEntityActionStateInferiors;
    }

    public void setBeforeUpdateEntityActionStateSuperiors(String[] value)
    {
        this.beforeUpdateEntityActionStateSuperiors = value;
    }

    public void setBeforeUpdateEntityActionStateInferiors(String[] value)
    {
        this.beforeUpdateEntityActionStateInferiors = value;
    }

    public void setOverrideUpdateEntityActionStateSuperiors(String[] value)
    {
        this.overrideUpdateEntityActionStateSuperiors = value;
    }

    public void setOverrideUpdateEntityActionStateInferiors(String[] value)
    {
        this.overrideUpdateEntityActionStateInferiors = value;
    }

    public void setAfterUpdateEntityActionStateSuperiors(String[] value)
    {
        this.afterUpdateEntityActionStateSuperiors = value;
    }

    public void setAfterUpdateEntityActionStateInferiors(String[] value)
    {
        this.afterUpdateEntityActionStateInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeUpdatePotionEffectsSuperiors()
    {
        return this.beforeUpdatePotionEffectsSuperiors;
    }

    public String[] getBeforeUpdatePotionEffectsInferiors()
    {
        return this.beforeUpdatePotionEffectsInferiors;
    }

    public String[] getOverrideUpdatePotionEffectsSuperiors()
    {
        return this.overrideUpdatePotionEffectsSuperiors;
    }

    public String[] getOverrideUpdatePotionEffectsInferiors()
    {
        return this.overrideUpdatePotionEffectsInferiors;
    }

    public String[] getAfterUpdatePotionEffectsSuperiors()
    {
        return this.afterUpdatePotionEffectsSuperiors;
    }

    public String[] getAfterUpdatePotionEffectsInferiors()
    {
        return this.afterUpdatePotionEffectsInferiors;
    }

    public void setBeforeUpdatePotionEffectsSuperiors(String[] value)
    {
        this.beforeUpdatePotionEffectsSuperiors = value;
    }

    public void setBeforeUpdatePotionEffectsInferiors(String[] value)
    {
        this.beforeUpdatePotionEffectsInferiors = value;
    }

    public void setOverrideUpdatePotionEffectsSuperiors(String[] value)
    {
        this.overrideUpdatePotionEffectsSuperiors = value;
    }

    public void setOverrideUpdatePotionEffectsInferiors(String[] value)
    {
        this.overrideUpdatePotionEffectsInferiors = value;
    }

    public void setAfterUpdatePotionEffectsSuperiors(String[] value)
    {
        this.afterUpdatePotionEffectsSuperiors = value;
    }

    public void setAfterUpdatePotionEffectsInferiors(String[] value)
    {
        this.afterUpdatePotionEffectsInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeUpdateRiddenSuperiors()
    {
        return this.beforeUpdateRiddenSuperiors;
    }

    public String[] getBeforeUpdateRiddenInferiors()
    {
        return this.beforeUpdateRiddenInferiors;
    }

    public String[] getOverrideUpdateRiddenSuperiors()
    {
        return this.overrideUpdateRiddenSuperiors;
    }

    public String[] getOverrideUpdateRiddenInferiors()
    {
        return this.overrideUpdateRiddenInferiors;
    }

    public String[] getAfterUpdateRiddenSuperiors()
    {
        return this.afterUpdateRiddenSuperiors;
    }

    public String[] getAfterUpdateRiddenInferiors()
    {
        return this.afterUpdateRiddenInferiors;
    }

    public void setBeforeUpdateRiddenSuperiors(String[] value)
    {
        this.beforeUpdateRiddenSuperiors = value;
    }

    public void setBeforeUpdateRiddenInferiors(String[] value)
    {
        this.beforeUpdateRiddenInferiors = value;
    }

    public void setOverrideUpdateRiddenSuperiors(String[] value)
    {
        this.overrideUpdateRiddenSuperiors = value;
    }

    public void setOverrideUpdateRiddenInferiors(String[] value)
    {
        this.overrideUpdateRiddenInferiors = value;
    }

    public void setAfterUpdateRiddenSuperiors(String[] value)
    {
        this.afterUpdateRiddenSuperiors = value;
    }

    public void setAfterUpdateRiddenInferiors(String[] value)
    {
        this.afterUpdateRiddenInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeWakeUpPlayerSuperiors()
    {
        return this.beforeWakeUpPlayerSuperiors;
    }

    public String[] getBeforeWakeUpPlayerInferiors()
    {
        return this.beforeWakeUpPlayerInferiors;
    }

    public String[] getOverrideWakeUpPlayerSuperiors()
    {
        return this.overrideWakeUpPlayerSuperiors;
    }

    public String[] getOverrideWakeUpPlayerInferiors()
    {
        return this.overrideWakeUpPlayerInferiors;
    }

    public String[] getAfterWakeUpPlayerSuperiors()
    {
        return this.afterWakeUpPlayerSuperiors;
    }

    public String[] getAfterWakeUpPlayerInferiors()
    {
        return this.afterWakeUpPlayerInferiors;
    }

    public void setBeforeWakeUpPlayerSuperiors(String[] value)
    {
        this.beforeWakeUpPlayerSuperiors = value;
    }

    public void setBeforeWakeUpPlayerInferiors(String[] value)
    {
        this.beforeWakeUpPlayerInferiors = value;
    }

    public void setOverrideWakeUpPlayerSuperiors(String[] value)
    {
        this.overrideWakeUpPlayerSuperiors = value;
    }

    public void setOverrideWakeUpPlayerInferiors(String[] value)
    {
        this.overrideWakeUpPlayerInferiors = value;
    }

    public void setAfterWakeUpPlayerSuperiors(String[] value)
    {
        this.afterWakeUpPlayerSuperiors = value;
    }

    public void setAfterWakeUpPlayerInferiors(String[] value)
    {
        this.afterWakeUpPlayerInferiors = value;
    }

    // ############################################################################

    public String[] getBeforeWriteWithoutTypeIdSuperiors()
    {
        return this.beforeWriteWithoutTypeIdSuperiors;
    }

    public String[] getBeforeWriteWithoutTypeIdInferiors()
    {
        return this.beforeWriteWithoutTypeIdInferiors;
    }

    public String[] getOverrideWriteWithoutTypeIdSuperiors()
    {
        return this.overrideWriteWithoutTypeIdSuperiors;
    }

    public String[] getOverrideWriteWithoutTypeIdInferiors()
    {
        return this.overrideWriteWithoutTypeIdInferiors;
    }

    public String[] getAfterWriteWithoutTypeIdSuperiors()
    {
        return this.afterWriteWithoutTypeIdSuperiors;
    }

    public String[] getAfterWriteWithoutTypeIdInferiors()
    {
        return this.afterWriteWithoutTypeIdInferiors;
    }

    public void setBeforeWriteWithoutTypeIdSuperiors(String[] value)
    {
        this.beforeWriteWithoutTypeIdSuperiors = value;
    }

    public void setBeforeWriteWithoutTypeIdInferiors(String[] value)
    {
        this.beforeWriteWithoutTypeIdInferiors = value;
    }

    public void setOverrideWriteWithoutTypeIdSuperiors(String[] value)
    {
        this.overrideWriteWithoutTypeIdSuperiors = value;
    }

    public void setOverrideWriteWithoutTypeIdInferiors(String[] value)
    {
        this.overrideWriteWithoutTypeIdInferiors = value;
    }

    public void setAfterWriteWithoutTypeIdSuperiors(String[] value)
    {
        this.afterWriteWithoutTypeIdSuperiors = value;
    }

    public void setAfterWriteWithoutTypeIdInferiors(String[] value)
    {
        this.afterWriteWithoutTypeIdInferiors = value;
    }
}
