/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.milkbowl.vault.economy.Economy
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.RegisteredServiceProvider
 *  org.bukkit.plugin.java.JavaPlugin
 */
package senyu.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import senyu.shop.AhCmd;
import senyu.shop.Db;
import senyu.shop.ShopAdminCmd;
import senyu.shop.ShopCmd;

public class SenyuShopPlugin
extends JavaPlugin {
    public Db db;
    public Economy eco;
    public String ecoName = "";
    public int pageSize;
    public int maxListings;
    public int expireDays;
    /* 拍卖成交累进税:按卖家成交时的余额档收税,税金销毁回收 */
    public boolean taxEnabled = false;
    public final List<long[]> taxBrackets = new ArrayList<long[]>();
    public String shopCmd = "shop";
    public final Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();
    private ShopCmd shopExecutor;
    private AhCmd ahExecutor;

    public void onEnable() {
        this.saveDefaultConfig();
        FileConfiguration fileConfiguration = this.getConfig();
        this.pageSize = Math.max(1, fileConfiguration.getInt("page-size", 24));
        this.maxListings = Math.max(1, fileConfiguration.getInt("max-listings-per-player", 10));
        this.expireDays = Math.max(1, fileConfiguration.getInt("expire-days", 7));
        this.loadTaxConfig(fileConfiguration);
        try {
            this.db = new Db(this);
            this.db.open();
        }
        catch (Exception exception) {
            this.getLogger().severe("\u6570\u636e\u5e93\u521d\u59cb\u5316\u5931\u8d25: " + exception.getMessage());
            Bukkit.getPluginManager().disablePlugin((Plugin)this);
            return;
        }
        this.setupEconomy();
        this.shopExecutor = new ShopCmd(this);
        this.ahExecutor = new AhCmd(this);
        ShopAdminCmd shopAdminCmd = new ShopAdminCmd(this);
        this.getCommand("shop").setExecutor((CommandExecutor)this.shopExecutor);
        this.getCommand("ah").setExecutor((CommandExecutor)this.ahExecutor);
        this.getCommand("ah").setTabCompleter((TabCompleter)this.ahExecutor);
        this.getCommand("shopadmin").setExecutor((CommandExecutor)shopAdminCmd);
        this.getCommand("shopadmin").setTabCompleter((TabCompleter)shopAdminCmd);
        ShopChestCmd shopChestCmd = new ShopChestCmd(this);
        org.bukkit.command.PluginCommand pluginCommand2 = this.getCommand("shopchest");
        pluginCommand2.setExecutor((CommandExecutor)shopChestCmd);
        pluginCommand2.setTabCompleter((TabCompleter)shopChestCmd);
        Bukkit.getPluginManager().registerEvents((org.bukkit.event.Listener)shopChestCmd, (Plugin)this);
        this.getLogger().info("SenyuShop 1.1.2: \u7bb1\u5b50\u5546\u5e97 /shopchest \u5df2\u4e0a\u7ebf(\u76ee\u5f55\u2192\u5206\u7c7b\u5546\u54c1\u2192\u5de6\u952e\u4e70\u53f3\u952e\u5356)");
        Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)this, () -> {
            try {
                this.db.sweepExpired();
            }
            catch (Exception exception) {}
        }, 200L, 12000L);
        this.getLogger().info("\u68ee\u5c7f\u5546\u5e97 SenyuShop \u5df2\u542f\u52a8" + (String)(this.eco != null ? "(\u7ecf\u6d4e:" + this.ecoName + ")" : "(\u672a\u627e\u5230\u7ecf\u6d4e\u63d2\u4ef6,\u4ec5\u53ef\u6d4f\u89c8)"));
    }

    private void loadTaxConfig(FileConfiguration fileConfiguration) {
        this.taxEnabled = fileConfiguration.getBoolean("tax-enabled", false);
        this.taxBrackets.clear();
        for (String line : fileConfiguration.getStringList("tax-brackets")) {
            String[] parts = line.split(":");
            if (parts.length < 2) {
                continue;
            }
            try {
                this.taxBrackets.add(new long[]{Long.parseLong(parts[0].trim()), (long)Double.parseDouble(parts[1].trim())});
            }
            catch (NumberFormatException numberFormatException) {
                /* 忽略非法档位配置 */
            }
        }
        if (this.taxEnabled && this.taxBrackets.isEmpty()) {
            this.taxEnabled = false;
        }
        if (this.taxEnabled) {
            this.getLogger().info("拍卖行累进税已启用,共 " + this.taxBrackets.size() + " 档,税金销毁回收");
        }
    }

    /* 按卖家余额找档位:从上到下取第一个 余额<=上限 的档位;上限 0 表示无上限 */
    public int taxPercentFor(double balance) {
        if (!this.taxEnabled) {
            return 0;
        }
        for (long[] bracket : this.taxBrackets) {
            long cap = bracket[0];
            if (cap <= 0L || balance <= (double)cap) {
                return (int)bracket[1];
            }
        }
        return 0;
    }

    /* 税额向上取整到分,确保不会出现多付/少扣 */
    public double taxAmount(double price, double sellerBalance) {
        int pct = this.taxPercentFor(sellerBalance);
        if (pct <= 0 || price <= 0.0) {
            return 0.0;
        }
        double tax = Math.ceil(price * (double)pct / 100.0 * 100.0) / 100.0;
        return Math.min(tax, price);
    }

    private void setupEconomy() {        if (Bukkit.getPluginManager().getPlugin("VaultSlim") != null) {
            this.ecoName = "VaultSlim";
        } else if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            this.ecoName = "Vault";
        } else {
            return;
        }
        RegisteredServiceProvider registeredServiceProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (registeredServiceProvider == null) {
            this.ecoName = "";
            return;
        }
        this.eco = (Economy)registeredServiceProvider.getProvider();
    }

    public boolean checkCooldown(UUID uUID) {
        long l = System.currentTimeMillis();
        Long l2 = this.cooldowns.get(uUID);
        if (l2 != null && l - l2 < 600L) {
            return false;
        }
        this.cooldowns.put(uUID, l);
        return true;
    }

    public void openShopFor(Player player) {
        if (this.shopExecutor != null) {
            this.shopExecutor.openMain(player);
        }
    }

    public void onDisable() {
        if (this.db != null) {
            this.db.close();
        }
    }
}

