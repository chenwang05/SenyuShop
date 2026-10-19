package senyu.shop;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 森屿商店 - 箱子界面整套版
 * /shopchest          : 分类目录(箱子)
 * /shopchest <分类>   : 直接进入分类商品页(箱子)
 * 商品页: 左键买1 / Shift+左键买一叠 / 右键卖1 / Shift+右键卖光背包
 */
public final class ShopChestCmd implements Listener, CommandExecutor, TabCompleter {

    private static final LegacyComponentSerializer LS = LegacyComponentSerializer.legacyAmpersand();
    private static final int PER_PAGE = 45;
    private static final String HUI = "\u56de\u6536\u4e13\u573a";
    private static final String SELL_ALL = "all";

    static final class Holder implements InventoryHolder {
        Inventory inv;
        String kind;          // catalog | items
        String cat;
        int page;
        List<Db.ShopItem> items;

        @Override
        public Inventory getInventory() {
            return inv;
        }
    }

    private final SenyuShopPlugin plugin;

    public ShopChestCmd(SenyuShopPlugin plugin) {
        this.plugin = plugin;
    }

    /* ---------------- 命令 ---------------- */

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("\u4ec5\u73a9\u5bb6\u53ef\u7528");
            return true;
        }
        if (!player.hasPermission("senyushop.use")) {
            Util.err(player, "\u6ca1\u6709\u6743\u9650");
            return true;
        }
        if (args.length > 0) {
            String cat = resolveCat(args[0]);
            if (cat != null) {
                openItems(player, cat, 0);
                return true;
            }
        }
        openCatalog(player);
        return true;
    }

    private String resolveCat(String raw) {
        if (raw.equalsIgnoreCase("huishou") || raw.equalsIgnoreCase("sell") || raw.equalsIgnoreCase("recycle")) {
            return HUI;
        }
        if (raw.equals(HUI)) return HUI;
        for (String c : ShopCmd.CATEGORIES) {
            if (c.equals(raw)) return c;
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            String a = args[0];
            for (String s : ShopCmd.CATEGORIES) {
                if (s.startsWith(a)) out.add(s);
            }
            if (HUI.startsWith(a) || "huishou".startsWith(a.toLowerCase())) out.add(HUI);
            return out;
        }
        return List.of();
    }

    /* ---------------- 目录页 ---------------- */

    public void openCatalog(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Db.ShopItem> list;
            try {
                list = plugin.db.listShopItems();
            } catch (Exception ex) {
                Bukkit.getScheduler().runTask(plugin, () -> Util.err(player, "\u8bfb\u53d6\u5546\u5e97\u6570\u636e\u5931\u8d25"));
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                Holder h = new Holder();
                h.kind = "catalog";
                Inventory inv = Bukkit.createInventory(h, 27, c("&6\u68ee\u5c7f\u5546\u5e97"));
                h.inv = inv;
                for (int i = 0; i < 27; i++) {
                    inv.setItem(i, filler());
                }
                inv.setItem(10, catButton(Material.BRICKS, "&6\u5efa\u7b51\u65b9\u5757", ShopCmd.CATEGORIES.get(0), list));
                inv.setItem(11, catButton(Material.GRASS_BLOCK, "&2\u81ea\u7136\u65b9\u5757", ShopCmd.CATEGORIES.get(1), list));
                inv.setItem(12, catButton(Material.COOKED_BEEF, "&c\u98df\u7269", ShopCmd.CATEGORIES.get(2), list));
                inv.setItem(13, catButton(Material.CHEST, "&e\u5176\u4ed6", ShopCmd.CATEGORIES.get(3), list));
                inv.setItem(14, recycleButton(list));
                inv.setItem(16, mk(Material.GOLD_INGOT, "&6\u62cd\u5355\u884c", l("&7\u5168\u670d\u73a9\u5bb6\u81ea\u7531\u4ea4\u6613\u5e7f\u573a", "&e\u70b9\u51fb\u524d\u5f80 (/ah)")));
                inv.setItem(22, mk(Material.BARRIER, "&e\u5173\u95ed", null));
                player.openInventory(inv);
            });
        });
    }

    private ItemStack catButton(Material mat, String label, String cat, List<Db.ShopItem> list) {
        long n = list.stream().filter(it -> cat.equals(it.category())).count();
        return mk(mat, label, l("&7\u5546\u54c1\u6570\u91cf: &f" + n + " \u79cd", "&e\u5de6\u952e\u8fdb\u5165\u5206\u7c7b"));
    }

    private ItemStack recycleButton(List<Db.ShopItem> list) {
        long n = list.stream().filter(it -> it.sell() > 0.0).count();
        return mk(Material.LIME_DYE, "&a\u267b " + HUI, l("&7\u53ea\u770b\u53ef\u56de\u6536\u51fa\u552e\u7684\u5546\u54c1", "&7\u53ef\u56de\u6536: &f" + n + " \u79cd", "&e\u5de6\u952e\u8fdb\u5165"));
    }

    /* ---------------- 商品页 ---------------- */

    public void openItems(Player player, String cat, int page) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Db.ShopItem> list;
            try {
                list = plugin.db.listShopItems();
            } catch (Exception ex) {
                Bukkit.getScheduler().runTask(plugin, () -> Util.err(player, "\u8bfb\u53d6\u5546\u5e97\u6570\u636e\u5931\u8d25"));
                return;
            }
            List<Db.ShopItem> filtered = new ArrayList<>();
            for (Db.ShopItem it : list) {
                if (HUI.equals(cat)) {
                    if (it.sell() > 0.0) filtered.add(it);
                } else if (cat.equals(it.category())) {
                    filtered.add(it);
                }
            }
            final List<Db.ShopItem> fl = filtered;
            final int pg = page;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                if (fl.isEmpty()) {
                    if (HUI.equals(cat)) {
                        Util.msg(player, "\u8fd8\u6ca1\u6709\u53ef\u56de\u6536\u7684\u5546\u54c1(\u56de\u6536\u4ef7 > 0 \u624d\u4f1a\u51fa\u73b0)", NamedTextColor.YELLOW);
                    } else {
                        Util.msg(player, "\u300c" + cat + "\u300d\u8fd8\u6ca1\u6709\u5546\u54c1,\u7ba1\u7406\u5458\u53ef\u7528 /shopadmin addhand \u4e0a\u67b6", NamedTextColor.YELLOW);
                    }
                    return;
                }
                int pages = (fl.size() + PER_PAGE - 1) / PER_PAGE;
                int p = Math.max(0, Math.min(pg, pages - 1));
                Holder h = new Holder();
                h.kind = "items";
                h.cat = cat;
                h.page = p;
                h.items = fl;
                String title = HUI.equals(cat) ? "\u56de\u6536\u4e13\u573a" : cat;
                Inventory inv = Bukkit.createInventory(h, 54, c("&6\u68ee\u5c7f\u5546\u5e97 &8\u00bb &f" + title + " &8(" + (p + 1) + "/" + pages + ")"));
                h.inv = inv;
                for (int i = p * PER_PAGE; i < Math.min((p + 1) * PER_PAGE, fl.size()); i++) {
                    inv.setItem(i - p * PER_PAGE, icon(fl.get(i)));
                }
                if (p > 0) {
                    inv.setItem(45, mk(Material.ARROW, "&7\u00ab \u4e0a\u4e00\u9875", null));
                }
                inv.setItem(47, mk(Material.PAPER, "&f\u7b2c " + (p + 1) + " / " + pages + " \u9875", l("&7\u672c\u5206\u7c7b\u5171 " + fl.size() + " \u79cd\u5546\u54c1", "&e\u70b9\u6b64\u5237\u65b0\u9875\u9762")));
                inv.setItem(49, mk(Material.LODESTONE, "&a\u00ab \u8fd4\u56de\u5206\u7c7b", null));
                inv.setItem(51, mk(Material.BARRIER, "&e\u5173\u95ed", null));
                if (p < pages - 1) {
                    inv.setItem(53, mk(Material.ARROW, "&7\u4e0b\u4e00\u9875 \u00bb", null));
                }
                player.openInventory(inv);
            });
        });
    }

    private ItemStack icon(Db.ShopItem it) {
        ItemStack icon = it.item().clone();
        icon.setAmount(1);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            String nm = Util.plainName(it.item());
            meta.displayName(c("&f" + nm));
            List<Component> lore = new ArrayList<>();
            lore.add(c(it.buy() > 0.0 ? "&6\u4e70\u5165: &e\u00a5" + Util.money(it.buy()) + " /\u4e2a" : "&7\u4e0d\u53ef\u8d2d\u4e70"));
            lore.add(c(it.sell() > 0.0 ? "&a\u56de\u6536: &e\u00a5" + Util.money(it.sell()) + " /\u4e2a" : "&7\u4e0d\u53ef\u56de\u6536"));
            if (it.buy() > 0.0) {
                lore.add(c("&e\u5de6\u952e\u4e70 1 \u00b7 Shift+\u5de6\u952e\u4e70\u4e00\u53e0"));
            }
            if (it.sell() > 0.0) {
                lore.add(c("&c\u53f3\u952e\u5356 1 \u00b7 Shift+\u53f3\u952e\u5356\u5149\u80cc\u5305"));
            }
            meta.lore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    /* ---------------- 事件 ---------------- */

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder() instanceof Holder h)) {
            return;
        }
        e.setCancelled(true);
        if (e.getClickedInventory() != top) {
            return;
        }
        int s = e.getRawSlot();
        if ("catalog".equals(h.kind)) {
            switch (s) {
                case 10 -> openItems(player, ShopCmd.CATEGORIES.get(0), 0);
                case 11 -> openItems(player, ShopCmd.CATEGORIES.get(1), 0);
                case 12 -> openItems(player, ShopCmd.CATEGORIES.get(2), 0);
                case 13 -> openItems(player, ShopCmd.CATEGORIES.get(3), 0);
                case 14 -> openItems(player, HUI, 0);
                case 16 -> {
                    player.closeInventory();
                    player.chat("/ah");
                }
                case 22 -> player.closeInventory();
                default -> { }
            }
            return;
        }
        if (s <= 44) {
            int idx = h.page * PER_PAGE + s;
            if (idx >= h.items.size()) {
                return;
            }
            Db.ShopItem it = h.items.get(idx);
            boolean right = e.isRightClick();
            boolean shift = e.isShiftClick();
            if (!right) {
                buy(player, it, shift ? it.item().getMaxStackSize() : 1);
            } else {
                sell(player, it, shift ? SELL_ALL : "1");
            }
            return;
        }
        switch (s) {
            case 45 -> openItems(player, h.cat, h.page - 1);
            case 47 -> openItems(player, h.cat, h.page);
            case 49 -> openCatalog(player);
            case 51 -> player.closeInventory();
            case 53 -> openItems(player, h.cat, h.page + 1);
            default -> { }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof Holder) {
            e.setCancelled(true);
        }
    }

    /* ---------------- 交易 ---------------- */

    private void buy(Player player, Db.ShopItem it, int want) {
        if (it.buy() <= 0.0) {
            Util.err(player, "\u8be5\u5546\u54c1\u4e0d\u53ef\u8d2d\u4e70");
            return;
        }
        Economy economy = plugin.eco;
        if (economy == null) {
            Util.err(player, "\u7ecf\u6d4e\u7cfb\u7edf\u4e0d\u53ef\u7528");
            return;
        }
        double price = it.buy();
        ItemStack unit = it.item().clone();
        unit.setAmount(1);
        int fits = fits(player, unit, want);
        if (fits <= 0) {
            Util.err(player, "\u80cc\u5305\u5df2\u6ee1,\u4e00\u4e2a\u90fd\u88c5\u4e0d\u4e0b");
            return;
        }
        int afford = (int) Math.floor(playerBalance(player, economy) / price);
        if (afford <= 0) {
            Util.err(player, "\u4f59\u989d\u4e0d\u8db3(\u5355\u4ef7 \u00a5" + Util.money(price) + ")");
            return;
        }
        int n = Math.min(fits, afford);
        double cost = price * n;
        EconomyResponse resp = economy.withdrawPlayer(player, cost);
        if (!resp.transactionSuccess()) {
            Util.err(player, "\u6263\u6b3e\u5931\u8d25: " + resp.errorMessage);
            return;
        }
        give(player, unit, n);
        if (n < want) {
            Util.msg(player, "\u5b9e\u9645\u8d2d\u4e70 " + n + " \u4e2a(\u53d7\u80cc\u5305/\u4f59\u989d\u9650\u5236),\u82b1\u8d39 \u00a5" + Util.money(cost), NamedTextColor.YELLOW);
        } else {
            Util.ok(player, "\u5df2\u8d2d\u4e70 " + Util.plainName(it.item()) + " \u00d7" + n + ",\u82b1\u8d39 \u00a5" + Util.money(cost));
        }
    }

    private void sell(Player player, Db.ShopItem it, String want) {
        if (it.sell() <= 0.0) {
            Util.err(player, "\u8be5\u5546\u54c1\u4e0d\u56de\u6536");
            return;
        }
        Economy economy = plugin.eco;
        if (economy == null) {
            Util.err(player, "\u7ecf\u6d4e\u7cfb\u7edf\u4e0d\u53ef\u7528");
            return;
        }
        ItemStack unit = it.item().clone();
        unit.setAmount(1);
        var inv = player.getInventory();
        int total = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getItem(i);
            if (s != null && s.isSimilar(unit)) {
                total += s.getAmount();
            }
        }
        if (total <= 0) {
            Util.err(player, "\u80cc\u5305\u91cc\u6ca1\u6709\u53ef\u56de\u6536\u7684 " + Util.plainName(it.item()));
            return;
        }
        int take = SELL_ALL.equals(want) ? total : Math.min(1, total);
        int remaining = take;
        for (int i = 0; i < 36 && remaining > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s != null && s.isSimilar(unit)) {
                int amt = Math.min(s.getAmount(), remaining);
                if (amt >= s.getAmount()) {
                    inv.setItem(i, null);
                } else {
                    s.setAmount(s.getAmount() - amt);
                }
                remaining -= amt;
            }
        }
        double pay = it.sell() * take;
        EconomyResponse resp = economy.depositPlayer(player, pay);
        if (!resp.transactionSuccess()) {
            give(player, unit, take);
            Util.err(player, "\u5165\u8d26\u5931\u8d25,\u7269\u54c1\u5df2\u9000\u56de: " + resp.errorMessage);
            return;
        }
        Util.ok(player, "\u5df2\u56de\u6536 " + Util.plainName(it.item()) + " \u00d7" + take + ",\u83b7\u5f97 \u00a5" + Util.money(pay));
    }

    private double playerBalance(Player player, Economy economy) {
        return economy.getBalance(player);
    }

    private int fits(Player player, ItemStack unit, int want) {
        int max = unit.getMaxStackSize();
        int room = 0;
        for (ItemStack s : player.getInventory().getStorageContents()) {
            if (s == null || s.getType().isAir()) {
                room += max;
            } else if (s.isSimilar(unit)) {
                room += max - s.getAmount();
            }
            if (room >= want) {
                break;
            }
        }
        return Math.min(room, want);
    }

    private void give(Player player, ItemStack unit, int n) {
        while (n > 0) {
            int part = Math.min(unit.getMaxStackSize(), n);
            ItemStack stack = unit.clone();
            stack.setAmount(part);
            player.getInventory().addItem(stack);
            n -= part;
        }
    }

    /* ---------------- 外观工具 ---------------- */

    private static Component c(String s) {
        return LS.deserialize(s);
    }

    private List<Component> l(String... ss) {
        List<Component> out = new ArrayList<>();
        for (String s : ss) {
            out.add(c(s));
        }
        return out;
    }

    private ItemStack mk(Material mat, String name, List<Component> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.displayName(c(name));
            if (lore != null && !lore.isEmpty()) {
                meta.lore(lore);
            }
            it.setItemMeta(meta);
        }
        return it;
    }

    private ItemStack filler() {
        return mk(Material.GRAY_STAINED_GLASS_PANE, "&r", null);
    }
}
