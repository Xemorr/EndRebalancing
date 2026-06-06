package me.xemor.endRebalancing;

import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

import java.lang.reflect.RecordComponent;

public class MetricsHandler {

    private static final int PLUGIN_ID = 31845;

    public MetricsHandler(JavaPlugin plugin, PluginConfig config) {
        Metrics metrics = new Metrics(plugin, PLUGIN_ID);
        registerCharts(metrics, config, "");
    }

    private void registerCharts(Metrics metrics, Record record, String prefix) {
        for (RecordComponent component : record.getClass().getRecordComponents()) {
            String chartId = toChartId(prefix, component.getName());
            try {
                Object value = component.getAccessor().invoke(record);
                if (value instanceof Boolean b) {
                    metrics.addCustomChart(new SimplePie(chartId, () -> b ? "Yes" : "No"));
                } else if (value instanceof Record nested) {
                    registerCharts(metrics, nested, chartId);
                }
            } catch (ReflectiveOperationException ignored) {}
        }
    }

    private String toChartId(String prefix, String fieldName) {
        String snake = fieldName.replaceAll("([A-Z])", "_$1").toLowerCase();
        return prefix.isEmpty() ? snake : prefix + "_" + snake;
    }
}
