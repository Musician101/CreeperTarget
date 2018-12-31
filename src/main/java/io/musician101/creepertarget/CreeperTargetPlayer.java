package io.musician101.creepertarget;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;

public class CreeperTargetPlayer extends EntityAINearestAttackableTarget<EntityPlayer> {

    public CreeperTargetPlayer(EntityCreeper creeper) {
        super(creeper, EntityPlayer.class, true);
    }

    private Optional<EntityPlayer> getPriorityTarget() {
        return CreeperTarget.INSTANCE.getTargets().stream().map(taskOwner.world::getPlayerEntityByName).filter(Objects::nonNull).filter(EntitySelectors.NOT_SPECTATING).filter(ep -> !ep.isCreative()).findFirst();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return getPriorityTarget().map(entityPlayer -> targetEntity.getUniqueID().equals(entityPlayer.getUniqueID())).filter(b -> {
            if (CreeperTarget.INSTANCE.ignoreSuperCreepers()) {
                return ((EntityCreeper) taskOwner).getPowered();
            }

            return b;
        }).orElse(super.shouldContinueExecuting());
    }

    @Override
    public boolean shouldExecute() {
        Optional<EntityPlayer> target = getPriorityTarget();
        if (target.isPresent()) {
            targetEntity = target.get();
            shouldCheckSight = false;
            taskOwner.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(100);
            return true;
        }

        taskOwner.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16);
        shouldCheckSight = true;
        return super.shouldExecute();
    }
}
