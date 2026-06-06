package me.xemor.endRebalancing.feature;

import me.xemor.endRebalancing.PluginConfig;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class DragonSpawnFeature implements Listener {

    private final PluginConfig.DragonConfig config;

    public DragonSpawnFeature(PluginConfig.DragonConfig config) {
        this.config = config;
    }

    @EventHandler
    public void onDragonSpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof EnderDragon dragon) {
            dragon.getAttribute(Attribute.MAX_HEALTH).setBaseValue(config.spawnHealth());
            dragon.setHealth(config.spawnHealth());
        }
    }
}
