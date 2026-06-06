package me.xemor.endRebalancing.feature;

import me.xemor.endRebalancing.PluginConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.*;

public class DragonDeathFeature implements Listener {

    private final PluginConfig.TopDamagersConfig config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public DragonDeathFeature(PluginConfig.TopDamagersConfig config) {
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    public void onDragonDeath(EntityDeathEvent e) {
        if (!config.enabled()) return;
        if (!(e.getEntity() instanceof EnderDragon dragon)) return;
        if (dragon.getMetadata("damagers").isEmpty()) return;

        LinkedHashMap<UUID, Double> damagers =
                (LinkedHashMap<UUID, Double>) dragon.getMetadata("damagers").get(0).value();

        List<Map.Entry<UUID, Double>> sorted = damagers.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .toList();

        for (int i = 1; i <= Math.min(config.count(), sorted.size()); i++) {
            Player player = Bukkit.getPlayer(sorted.get(i - 1).getKey());
            if (player == null) continue;

            Bukkit.broadcast(miniMessage.deserialize(
                    config.broadcastMessage(),
                    Placeholder.unparsed("name", player.getName()),
                    Placeholder.unparsed("position", String.valueOf(i))
            ));

            for (String command : config.commandsForPosition(i)) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%name%", player.getName()));
            }
        }
    }
}
