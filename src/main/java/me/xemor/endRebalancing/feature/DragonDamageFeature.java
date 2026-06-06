package me.xemor.endRebalancing.feature;

import me.xemor.endRebalancing.PluginConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import space.arim.morepaperlib.scheduling.GracefulScheduling;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class DragonDamageFeature implements Listener {

    private final JavaPlugin plugin;
    private final PluginConfig.DragonConfig config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final NamespacedKey crystalRespawns;
    private final NamespacedKey crystalHits;
    private final GracefulScheduling scheduling;

    public DragonDamageFeature(JavaPlugin plugin, PluginConfig.DragonConfig config, GracefulScheduling scheduling) {
        this.plugin = plugin;
        this.config = config;
        this.scheduling = scheduling;
        this.crystalRespawns = new NamespacedKey(plugin, "crystalRespawns");
        this.crystalHits = new NamespacedKey(plugin, "crystalHits");
    }

    public NamespacedKey getCrystalHitsKey() {
        return crystalHits;
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        // Track player damage dealt to dragon
        if (e.getEntity() instanceof EnderDragon dragon && e.getDamager() instanceof Player player) {
            LinkedHashMap<UUID, Double> damagers;
            if (!dragon.getMetadata("damagers").isEmpty()) {
                damagers = (LinkedHashMap<UUID, Double>) dragon.getMetadata("damagers").get(0).value();
            } else {
                damagers = new LinkedHashMap<>();
            }
            damagers.merge(player.getUniqueId(), e.getDamage(), Double::sum);
            dragon.setMetadata("damagers", new FixedMetadataValue(plugin, damagers));
        }

        // Dragon melee multiplier
        if (e.getDamager() instanceof EnderDragon) {
            e.setDamage(e.getDamage() * config.meleeAttackMultiplier());
        }

        // Dragon fireball multiplier
        if (e.getDamager() instanceof DragonFireball) {
            e.setDamage(e.getDamage() * config.fireball().fireballMultiplier());
            e.getEntity().setFireTicks(config.fireball().fireballFireTicks());
        }

        // Dragon acid cloud multiplier + endermen aggro
        if (e.getDamager() instanceof AreaEffectCloud cloud && cloud.getSource() instanceof EnderDragon) {
            e.setDamage(e.getDamage() * config.meleeAttackMultiplier());
            e.getEntity().setFireTicks(config.fireball().fireballFireTicks());
            if (config.fireball().endermenTargetVictims() && e.getEntity() instanceof Player victim) {
                makeEndermenTarget(victim);
            }
        }

        // Crystal hardmode
        if (config.crystalHardmode().enabled() && e.getEntity() instanceof EnderCrystal crystal) {
            if (crystal.hasMetadata("NPC")) return;
            Integer hits = crystal.getPersistentDataContainer().get(crystalHits, PersistentDataType.INTEGER);
            if (hits == null) hits = 0;
            crystal.getPersistentDataContainer().set(crystalHits, PersistentDataType.INTEGER, ++hits);
            if (hits < config.crystalHardmode().hitsRequired()) {
                Player damager = null;
                if (e.getDamager() instanceof Player p) {
                    damager = p;
                } else if (e.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
                    damager = p;
                }
                if (damager != null) {
                    int hitsRemaining = config.crystalHardmode().hitsRequired() - hits;
                    damager.sendMessage(miniMessage.deserialize(
                            config.crystalHardmode().damagerMessage(),
                            Placeholder.unparsed("hits_remaining", String.valueOf(hitsRemaining))
                    ));
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDragonDamageReceived(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof EnderDragon dragon)) return;

        e.setDamage(e.getDamage() * config.damageReceivedMultiplier());

        if (config.preventExplosionDamage()
                && (e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            e.setCancelled(true);
            return;
        }

        // Respawn crystals at evenly-spaced health thresholds determined by crystalRespawnCount.
        // e.g. count=3 → 75%, 50%, 25%; count=4 → 80%, 60%, 40%, 20%
        Integer respawnCount = dragon.getPersistentDataContainer().get(crystalRespawns, PersistentDataType.INTEGER);
        if (respawnCount == null) respawnCount = 0;

        int count = config.crystalRespawnCount();
        double maxHealth = dragon.getAttribute(Attribute.MAX_HEALTH).getValue();
        for (int i = 1; i <= count; i++) {
            if (dragon.getHealth() < (maxHealth / (count + 1)) * (count + 1 - i) && respawnCount < i) {
                respawnCrystals(dragon.getWorld());
                dragon.getPersistentDataContainer().set(crystalRespawns, PersistentDataType.INTEGER, i);
                respawnCount = i;
            }
        }
    }

    private void respawnCrystals(World world) {
        int negativeHits = -config.crystalHardmode().additionalRespawnHitsRequired();
        int blockToPlaceEndCrystal = world.getHighestBlockYAt(3, 0) + 1;
        spawnCrystal(world, 3, blockToPlaceEndCrystal, 0, negativeHits);
        spawnCrystal(world, -3, blockToPlaceEndCrystal, 0, negativeHits);
        spawnCrystal(world, 0, blockToPlaceEndCrystal, 3, negativeHits);
        spawnCrystal(world, 0, blockToPlaceEndCrystal, -3, negativeHits);
    }

    private void spawnCrystal(World world, int x, int y, int z, int initialHits) {
        EnderCrystal crystal = (EnderCrystal) world.spawnEntity(new Location(world, x, y, z), EntityType.END_CRYSTAL);
        crystal.getPersistentDataContainer().set(crystalHits, PersistentDataType.INTEGER, initialHits);
    }

    private void makeEndermenTarget(Player player) {
        scheduling.entitySpecificScheduler(player).run(task -> {
            player.getNearbyEntities(20, 5, 20).stream()
                    .filter(e -> e instanceof Enderman)
                    .forEach(e -> ((Enderman) e).setTarget(player));
        }, () -> {});
    }
}
