package me.xemor.endRebalancing.feature;

import me.xemor.endRebalancing.PluginConfig;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;

public class ShulkerBoxChecks implements Listener {

    private final PluginConfig.ToggleConfig config;

    private static final EnumSet<Material> SHULKER_BOXES = EnumSet.of(
            Material.SHULKER_BOX, Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX,
            Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
            Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX,
            Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX
    );

    public ShulkerBoxChecks(PluginConfig.ToggleConfig config) {
        this.config = config;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (!config.enabled()) return;
        dropShulkerBoxItems(e.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (!config.enabled()) return;
        for (Block block : e.getBlocks()) {
            dropShulkerBoxItems(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (!config.enabled()) return;
        for (Block block : e.blockList()) {
            dropShulkerBoxItems(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        if (!config.enabled()) return;
        for (Block block : e.blockList()) {
            dropShulkerBoxItems(block);
        }
    }

    private void dropShulkerBoxItems(Block block) {
        if (!SHULKER_BOXES.contains(block.getType())) return;
        ShulkerBox shulkerBox = (ShulkerBox) block.getState();
        for (ItemStack item : shulkerBox.getInventory()) {
            if (item == null) continue;
            shulkerBox.getWorld().dropItemNaturally(block.getLocation(), item);
        }
        shulkerBox.getInventory().clear();
    }
}
