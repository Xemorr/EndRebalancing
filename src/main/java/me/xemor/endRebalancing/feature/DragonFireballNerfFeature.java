package me.xemor.endRebalancing.feature;

import me.xemor.endRebalancing.PluginConfig;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class DragonFireballNerfFeature implements Listener {

    private final PluginConfig.DragonConfig config;

    public DragonFireballNerfFeature(PluginConfig.DragonConfig config) {
        this.config = config;
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (!config.fireball().enabled()) return;
        if (e.getHitEntity() instanceof EnderDragon && e.getEntity() instanceof DragonFireball) {
            e.setCancelled(true);
        }
    }
}
