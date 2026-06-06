package me.xemor.endRebalancing.feature;

import me.xemor.endRebalancing.PluginConfig;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;

import java.util.Random;

public class DragonPhaseFeature implements Listener {

    private final PluginConfig.DragonConfig config;
    private final Random random = new Random();

    public DragonPhaseFeature(PluginConfig.DragonConfig config) {
        this.config = config;
    }

    @EventHandler
    public void onEnderDragonPhase(EnderDragonChangePhaseEvent e) {
        if (!config.phaseRandomization().enabled()) return;
        if (e.getNewPhase() == EnderDragon.Phase.FLY_TO_PORTAL) {
            if (random.nextDouble() < config.phaseRandomization().strafeChance()) {
                e.setNewPhase(EnderDragon.Phase.STRAFING);
            }
        }
    }
}
