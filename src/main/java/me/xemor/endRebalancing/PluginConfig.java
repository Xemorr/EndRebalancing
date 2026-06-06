package me.xemor.endRebalancing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record PluginConfig(
        DragonConfig dragon,
        ElytraConfig elytra,
        @JsonProperty("drop_shulker_box_contents_on_break") ToggleConfig shulkerBox,
        @JsonProperty("prevent_chorus_fruit_teleportation_through_blocks") ToggleConfig chorusFruit
) {
    public static PluginConfig load(File configFile) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        try {
            return mapper.readValue(configFile, PluginConfig.class);
        } catch (Exception e) {
            throw new EndRebalancingConfigParsingException("Failed to parse plugin configuration", e);
        }
    }

    private static PluginConfig defaults() {
        return new PluginConfig(
                new DragonConfig(true, 200.0, 0.5, 2.0, true, 3,
                        new FireballConfig(true, 2.0, 100, true),
                        new PhaseRandomizationConfig(true, 0.5),
                        new CrystalHardmodeConfig(true, 3, 2, "<red>This crystal requires <hits_remaining> more hit(s) to destroy!"),
                        new TopDamagersConfig(true, 3, "<gold><bold><name></bold> placed #<position> in damage dealt to the Ender Dragon!", Collections.emptyMap())),
                new ElytraConfig(true, 0.99, 4.0, 0.5, true),
                new ToggleConfig(true),
                new ToggleConfig(true)
        );
    }

    public record DragonConfig(
            boolean enabled,
            double spawnHealth,
            double damageReceivedMultiplier,
            double meleeAttackMultiplier,
            boolean preventExplosionDamage,
            int crystalRespawnCount,
            FireballConfig fireball,
            PhaseRandomizationConfig phaseRandomization,
            CrystalHardmodeConfig crystalHardmode,
            TopDamagersConfig topDamagers
    ) {
        public DragonConfig {
            if (fireball == null) fireball = new FireballConfig(true, 2.0, 100, true);
            if (phaseRandomization == null) phaseRandomization = new PhaseRandomizationConfig(true, 0.5);
            if (crystalHardmode == null) crystalHardmode = new CrystalHardmodeConfig(true, 3, 2, "<red>This crystal requires <hits_remaining> more hit(s) to destroy!");
            if (topDamagers == null) topDamagers = new TopDamagersConfig(true, 3, "<gold><bold><name></bold> placed #<position> in damage dealt to the Ender Dragon!", Collections.emptyMap());
        }
    }

    public record FireballConfig(
            boolean enabled,
            double fireballMultiplier,
            int fireballFireTicks,
            boolean endermenTargetVictims
    ) {}

    public record PhaseRandomizationConfig(boolean enabled, double strafeChance) {}

    public record CrystalHardmodeConfig(
            boolean enabled,
            int hitsRequired,
            int additionalRespawnHitsRequired,
            String damagerMessage
    ) {}

    public record TopDamagersConfig(
            boolean enabled,
            int count,
            String broadcastMessage,
            Map<String, List<String>> commands
    ) {
        public TopDamagersConfig {
            if (commands == null) commands = Collections.emptyMap();
        }

        public List<String> commandsForPosition(int position) {
            return commands.getOrDefault(String.valueOf(position), Collections.emptyList());
        }
    }

    public record ElytraConfig(
            boolean enabled,
            double frictionMultiplier,
            double maxVelocitySquared,
            double fireworkBoostMultiplier,
            boolean allowFireworks
    ) {}

    public record ToggleConfig(boolean enabled) {}
}
