package me.heojin.economy.listener;

import me.heojin.economy.EconomyPlugin;
import me.heojin.economy.gui.EconomyGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import java.util.List;

public class GUIListener implements Listener {
    private final EconomyPlugin plugin;

    public GUIListener(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        // 1. Economy GUI Protection
        if (title.equals(EconomyGUI.GUI_NAME)) {
            event.setCancelled(true);
            return;
        }

        // 2. Shop GUI Logic
        if (title.equals(me.heojin.economy.gui.ShopGUI.SHOP_NAME)) {
            event.setCancelled(true);
            
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;
            if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player)) return;

            org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getWhoClicked();
            org.bukkit.inventory.ItemStack clickedItem = event.getCurrentItem();
            
            // Item Meta Check
            if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasLore()) return;

            // Get Price from Lore
            List<String> lore = clickedItem.getItemMeta().getLore();
            double price = 0;
            for (String line : lore) {
                if (line.contains("가격:")) {
                    String priceStr = org.bukkit.ChatColor.stripColor(line).replace("가격:", "").replace("원", "").replace(",", "").trim();
                    try {
                        price = Double.parseDouble(priceStr);
                    } catch (NumberFormatException ignored) {}
                    break;
                }
            }

            if (price <= 0) return;

            // Process Purchase
            me.heojin.economy.manager.EconomyManager manager = plugin.getEconomyManager();
            if (manager.getBalance(player.getUniqueId()) >= price) {
                manager.withdrawBalance(player.getUniqueId(), price);
                
                // Give Item (Remove price lore before giving)
                org.bukkit.inventory.ItemStack boughtItem = clickedItem.clone();
                org.bukkit.inventory.meta.ItemMeta meta = boughtItem.getItemMeta();
                meta.setLore(null); // Clear lore for the actual item
                boughtItem.setItemMeta(meta);
                boughtItem.setAmount(1);
                
                player.getInventory().addItem(boughtItem);
                player.sendMessage("§a[상점] §f아이템을 §e" + String.format("%,.0f", price) + "원§f에 구매했습니다!");
            } else {
                player.sendMessage("§c[상점] §f잔액이 부족합니다.");
            }
        }

        // 3. Stock GUI Logic
        if (title.equals("§0주식 시장")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;
            if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player)) return;

            org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getWhoClicked();
            org.bukkit.inventory.ItemStack clickedItem = event.getCurrentItem();
            if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

            String displayName = clickedItem.getItemMeta().getDisplayName();
            me.heojin.economy.manager.StockManager stockManager = plugin.getStockManager();
            
            me.heojin.economy.manager.StockManager.StockData targetStock = null;
            for (me.heojin.economy.manager.StockManager.StockData stock : stockManager.getStocks().values()) {
                if (displayName.equals("§e" + stock.name)) {
                    targetStock = stock;
                    break;
                }
            }

            if (targetStock == null) return;

            if (event.isShiftClick()) {
                // Sell All
                int owned = stockManager.getOwnedShares(player.getUniqueId(), targetStock.id);
                if (owned > 0) {
                    stockManager.sellStock(player.getUniqueId(), targetStock.id, owned);
                    player.sendMessage("§e[주식] §f" + targetStock.name + " 주식을 §a전량(" + owned + "주)§f 매도했습니다.");
                } else {
                    player.sendMessage("§c[주식] §f보유 중인 주식이 없습니다.");
                }
            } else if (event.isLeftClick()) {
                // Buy 1
                if (stockManager.buyStock(player.getUniqueId(), targetStock.id, 1)) {
                    player.sendMessage("§e[주식] §f" + targetStock.name + " 주식 1주를 §a매수§f했습니다.");
                } else {
                    player.sendMessage("§c[주식] §f잔액이 부족합니다.");
                }
            } else if (event.isRightClick()) {
                // Sell 1
                if (stockManager.sellStock(player.getUniqueId(), targetStock.id, 1)) {
                    player.sendMessage("§e[주식] §f" + targetStock.name + " 주식 1주를 §c매도§f했습니다.");
                } else {
                    player.sendMessage("§c[주식] §f보유 중인 주식이 없습니다.");
                }
            }
            
            // Refresh GUI
            me.heojin.economy.gui.StockGUI.openStockGUI(player, plugin);
        }

        // 4. Sell GUI Logic
        if (title.equals(me.heojin.economy.gui.SellGUI.SELL_NAME)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;
            if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player)) return;

            org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getWhoClicked();
            org.bukkit.inventory.ItemStack clickedItem = event.getCurrentItem();
            
            if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasLore()) return;

            // Get Selling Price
            List<String> lore = clickedItem.getItemMeta().getLore();
            double unitPrice = 0;
            for (String line : lore) {
                if (line.contains("판매가:")) {
                    String priceStr = org.bukkit.ChatColor.stripColor(line).replace("개당 판매가:", "").replace("원", "").replace(",", "").trim();
                    try {
                        unitPrice = Double.parseDouble(priceStr);
                    } catch (NumberFormatException ignored) {}
                    break;
                }
            }

            if (unitPrice <= 0) return;

            // Find items in player inventory
            org.bukkit.Material targetMaterial = clickedItem.getType();
            int totalAmount = 0;
            for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == targetMaterial) {
                    totalAmount += item.getAmount();
                    player.getInventory().remove(item);
                }
            }

            if (totalAmount > 0) {
                double totalIncome = unitPrice * totalAmount;
                plugin.getEconomyManager().addBalance(player.getUniqueId(), totalIncome);
                plugin.getEconomyManager().saveData();
                player.sendMessage("§a[판매] §e" + targetMaterial.name() + " §f" + totalAmount + "개를 판매하여 §e" + String.format("%,.0f", totalIncome) + "원§f을 벌었습니다!");
            } else {
                player.sendMessage("§c[판매] §f인벤토리에 판매할 아이템이 없습니다.");
            }
        }
    }
}
