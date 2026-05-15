package me.heojin.economy.listener;

import me.heojin.economy.EconomyPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class EconomyListener implements Listener {
    private final EconomyPlugin plugin;
    private final Map<Material, Double> blockRewards = new HashMap<>();
    private final Map<EntityType, Double> mobRewards = new HashMap<>();

    public EconomyListener(EconomyPlugin plugin) {
        this.plugin = plugin;
        setupRewards();
    }

    private void setupRewards() {
        // Mining Rewards
        blockRewards.put(Material.DIAMOND_ORE, 1000.0);
        blockRewards.put(Material.DEEPSLATE_DIAMOND_ORE, 1200.0);
        blockRewards.put(Material.EMERALD_ORE, 800.0);
        blockRewards.put(Material.DEEPSLATE_EMERALD_ORE, 1000.0);
        blockRewards.put(Material.GOLD_ORE, 500.0);
        blockRewards.put(Material.DEEPSLATE_GOLD_ORE, 600.0);
        blockRewards.put(Material.IRON_ORE, 200.0);
        blockRewards.put(Material.DEEPSLATE_IRON_ORE, 250.0);
        blockRewards.put(Material.COAL_ORE, 50.0);
        blockRewards.put(Material.DEEPSLATE_COAL_ORE, 60.0);

        // Hunting Rewards
        mobRewards.put(EntityType.ZOMBIE, 100.0);
        mobRewards.put(EntityType.SKELETON, 100.0);
        mobRewards.put(EntityType.CREEPER, 200.0);
        mobRewards.put(EntityType.SPIDER, 100.0);
        mobRewards.put(EntityType.ENDERMAN, 500.0);
        mobRewards.put(EntityType.WITHER_SKELETON, 1000.0);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();

        if (blockRewards.containsKey(material)) {
            double amount = blockRewards.get(material);
            plugin.getEconomyManager().addBalance(player.getUniqueId(), amount);
            player.sendMessage(ChatColor.AQUA + "[경제] " + ChatColor.WHITE + material.name() + "을(를) 채광하여 " + ChatColor.GOLD + String.format("%,.0f", amount) + "원" + ChatColor.WHITE + "을 획득했습니다!");
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        EntityType type = event.getEntityType();
        if (mobRewards.containsKey(type)) {
            double amount = mobRewards.get(type);
            plugin.getEconomyManager().addBalance(killer.getUniqueId(), amount);
            killer.sendMessage(ChatColor.RED + "[경제] " + ChatColor.WHITE + type.name() + "을(를) 처치하여 " + ChatColor.GOLD + String.format("%,.0f", amount) + "원" + ChatColor.WHITE + "을 획득했습니다!");
        }
    }
}
