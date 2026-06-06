package me.xemor.endRebalancing.feature;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import me.xemor.endRebalancing.PluginConfig;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import space.arim.morepaperlib.scheduling.GracefulScheduling;

import java.util.concurrent.atomic.AtomicInteger;

public class ElytraFeature implements Listener {

    private final JavaPlugin plugin;
    private final PluginConfig.ElytraConfig config;
    private final GracefulScheduling scheduling;

    public ElytraFeature(JavaPlugin plugin, PluginConfig.ElytraConfig config, GracefulScheduling scheduling) {
        this.plugin = plugin;
        this.config = config;
        this.scheduling = scheduling;
    }

    @EventHandler
    public void onElytra(EntityToggleGlideEvent e) {
        if (!e.isGliding() || !(e.getEntity() instanceof Player player)) return;
        scheduling.entitySpecificScheduler(player).runAtFixedRate(task -> {
            if (!player.isOnline() || !player.isGliding()) {
                task.cancel();
                return;
            }
            player.setVelocity(player.getVelocity().multiply(config.frictionMultiplier()));
            if (player.getVelocity().lengthSquared() >= config.maxVelocitySquared()) {
                player.setGliding(false);
                player.setVelocity(new Vector(0, -0.5, 0));
            }
        }, () -> {}, 1L, 1L);
    }

    @EventHandler
    public void onFireworkBoost(PlayerElytraBoostEvent e) {
        Player player = e.getPlayer();

        if (!config.allowFireworks()) {
            e.setCancelled(true);
            e.setShouldConsume(false);
            return;
        }

        if (config.fireworkBoostMultiplier() != 1) {
            ItemMeta itemMeta = e.getFirework().getItem().getItemMeta();
            FireworkMeta fireworkMeta = (FireworkMeta) itemMeta;
            int fireworkPower = fireworkMeta.getPower();
            int fireworkFlightDuration = 10 * (2 + fireworkPower) + 1;
            e.setCancelled(true);
            e.getItemStack().setAmount(e.getItemStack().getAmount() - 1);
            AtomicInteger atomicInteger = new AtomicInteger(0);
            scheduling.entitySpecificScheduler(player).runAtFixedRate(task -> {
                if (atomicInteger.get() >= fireworkFlightDuration) {
                    task.cancel();
                    return;
                }
                atomicInteger.incrementAndGet();
                Vector normalizedEyeVector = player.getEyeLocation().getDirection().normalize();
                normalizedEyeVector.multiply(5D / 3).multiply(config.fireworkBoostMultiplier());
                player.setVelocity(normalizedEyeVector);
                Location location = player.getLocation();
                World world = player.getWorld();
                world.spawnParticle(Particle.FIREWORK, location, 1, 0, 0, 0, 0);
            }, () -> {}, 1L, 1L);
        }
    }
}
