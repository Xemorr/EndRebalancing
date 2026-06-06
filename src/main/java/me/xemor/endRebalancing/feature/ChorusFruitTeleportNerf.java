package me.xemor.endRebalancing.feature;

import me.xemor.endRebalancing.PluginConfig;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class ChorusFruitTeleportNerf implements Listener {

    private final PluginConfig.ToggleConfig config;

    public ChorusFruitTeleportNerf(PluginConfig.ToggleConfig config) {
        this.config = config;
    }

    @EventHandler
    public void onChorusTeleport(PlayerTeleportEvent e) {
        if (!config.enabled()) return;
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.CONSUMABLE_EFFECT) return;

        Location to = e.getTo();
        Location from = e.getFrom();
        Vector fromTo = to.toVector().subtract(from.toVector());
        World world = to.getWorld();
        RayTraceResult result = world.rayTraceBlocks(from, fromTo.normalize(), from.distance(to), FluidCollisionMode.NEVER);
        if (result != null && result.getHitBlock() != null) {
            to.setX(result.getHitBlock().getX());
            to.setY(result.getHitBlock().getY());
            to.setZ(result.getHitBlock().getZ());
            to.subtract(blockify(fromTo));
        }
    }

    private Vector blockify(Vector vector) {
        if (vector.getX() > 0) vector.setX(1);
        else if (vector.getX() < 0) vector.setX(-1);

        if (vector.getY() > 0) vector.setY(1);
        else if (vector.getY() < 0) vector.setY(-1);

        if (vector.getZ() > 0) vector.setZ(1);
        else if (vector.getY() < 0) vector.setZ(-1);

        return vector;
    }
}
