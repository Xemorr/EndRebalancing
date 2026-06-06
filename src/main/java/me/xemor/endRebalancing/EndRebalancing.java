package me.xemor.endRebalancing;

import me.xemor.endRebalancing.feature.*;
import me.xemor.foliahacks.FoliaHacks;
import org.bukkit.plugin.java.JavaPlugin;
import space.arim.morepaperlib.scheduling.GracefulScheduling;

import java.io.File;

public final class EndRebalancing extends JavaPlugin {

    private PluginConfig config;
    private FoliaHacks foliaHacks;

    @Override
    public void onEnable() {
        saveResource("config.yml", false);
        config = PluginConfig.load(new File(getDataFolder(), "config.yml"));
        foliaHacks = new FoliaHacks(this);

        if (config.dragon().enabled()) {
            register(new DragonPhaseFeature(config.dragon()));
            register(new DragonFireballNerfFeature(config.dragon()));
            register(new DragonSpawnFeature(config.dragon()));
            register(new DragonDamageFeature(this, config.dragon(), getScheduler()));
            register(new DragonDeathFeature(config.dragon().topDamagers()));
        }

        if (config.elytra().enabled()) {
            register(new ElytraFeature(this, config.elytra(), getScheduler()));
        }

        if (config.shulkerBox().enabled()) {
            register(new ShulkerBoxChecks(config.shulkerBox()));
        }

        if (config.chorusFruit().enabled()) {
            register(new ChorusFruitTeleportNerf(config.chorusFruit()));
        }
    }

    private void register(org.bukkit.event.Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public GracefulScheduling getScheduler() {
        return foliaHacks.getScheduling();
    }
}
