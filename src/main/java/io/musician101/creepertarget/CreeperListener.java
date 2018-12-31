package io.musician101.creepertarget;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CreeperListener {

    @SubscribeEvent
    public void onSpawn(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof EntityCreeper)) {
            return;
        }

        EntityCreeper creeper = (EntityCreeper) entity;
        EntityAIBase task = creeper.targetTasks.taskEntries.iterator().next().action;
        creeper.targetTasks.removeTask(task);
        creeper.targetTasks.addTask(1, new CreeperTargetPlayer(creeper));
    }
}
