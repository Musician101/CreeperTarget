package io.musician101.creepertarget;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;

public class CreeperTargetPlayer extends NearestAttackableTargetGoal<PlayerEntity> {

    public CreeperTargetPlayer(CreeperEntity creeper) {
        super(creeper, PlayerEntity.class, true);
    }

    private Optional<? extends PlayerEntity> getPriorityTarget() {
        return CreeperTarget.instance().getTargets().stream().map(name -> goalOwner.world.getPlayers().stream().filter(player -> player.getName().getUnformattedComponentText().equalsIgnoreCase(name)).findFirst().orElse(null)).filter(Objects::nonNull).filter(EntityPredicates.NOT_SPECTATING).filter(ep -> !ep.isCreative()).findFirst();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return getPriorityTarget().map(entityPlayer -> target.getUniqueID().equals(entityPlayer.getUniqueID())).filter(b -> {
            if (CreeperTarget.instance().ignoreSuperCreepers()) {
                return ((CreeperEntity) goalOwner).getPowered();
            }

            return b;
        }).orElse(super.shouldContinueExecuting());
    }

    @Override
    public boolean shouldExecute() {
        Optional<? extends PlayerEntity> target = getPriorityTarget();
        if (target.isPresent()) {
            this.target = target.get();
            goalOwner.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(100);
            return true;
        }

        goalOwner.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16);
        return super.shouldExecute();
    }
}
