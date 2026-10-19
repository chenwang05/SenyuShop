/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.dialog.Dialog
 *  io.papermc.paper.dialog.DialogResponseView
 *  io.papermc.paper.registry.data.dialog.ActionButton
 *  io.papermc.paper.registry.data.dialog.DialogBase
 *  io.papermc.paper.registry.data.dialog.DialogRegistryEntry$Builder
 *  io.papermc.paper.registry.data.dialog.body.DialogBody
 *  io.papermc.paper.registry.data.dialog.input.DialogInput
 *  io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput$OptionEntry
 *  io.papermc.paper.registry.data.dialog.type.DialogType
 *  net.kyori.adventure.dialog.DialogLike
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.plugin.Plugin
 */
package senyu.shop;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import senyu.shop.Db;
import senyu.shop.SenyuShopPlugin;
import senyu.shop.ShopCmd;
import senyu.shop.Util;

public class ShopAdminCmd
implements CommandExecutor,
TabCompleter {
    private final SenyuShopPlugin plugin;

    public ShopAdminCmd(SenyuShopPlugin senyuShopPlugin) {
        this.plugin = senyuShopPlugin;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] stringArray) {
        String string2;
        if (!commandSender.hasPermission("senyushop.admin")) {
            commandSender.sendMessage("\u4ec5\u7ba1\u7406\u5458\u53ef\u7528");
            return true;
        }
        if (stringArray.length == 0) {
            if (commandSender instanceof Player) {
                Player player = (Player)commandSender;
                this.openAssistant(player, 0);
            } else {
                this.help(commandSender);
            }
            return true;
        }
        switch (string2 = stringArray[0].toLowerCase()) {
            case "addhand": 
            case "add": {
                String string3;
                if (stringArray.length < 3) {
                    commandSender.sendMessage("\u7528\u6cd5: /shopadmin addhand <\u4e70\u4ef7> <\u5356\u4ef7> [\u5206\u7c7b](\u4e70\u4ef7\u6216\u5356\u4ef7\u586b 0 \u8868\u793a\u4e0d\u5f00\u653e)");
                    commandSender.sendMessage("\u5206\u7c7b: \u5efa\u7b51\u65b9\u5757 / \u81ea\u7136\u65b9\u5757 / \u98df\u7269 / \u5176\u4ed6(\u9ed8\u8ba4 \u5176\u4ed6)");
                    return true;
                }
                Double d = this.parse(commandSender, stringArray[1]);
                Double d2 = this.parse(commandSender, stringArray[2]);
                if (d == null || d2 == null) {
                    return true;
                }
                if (stringArray.length >= 4 && !ShopCmd.CATEGORIES.contains(stringArray[3])) {
                    commandSender.sendMessage("\u5206\u7c7b\u5fc5\u987b\u662f: \u5efa\u7b51\u65b9\u5757 / \u81ea\u7136\u65b9\u5757 / \u98df\u7269 / \u5176\u4ed6");
                    return true;
                }
                String string4 = string3 = stringArray.length >= 4 ? stringArray[3] : "\u5176\u4ed6";
                if (!(commandSender instanceof Player)) {
                    commandSender.sendMessage("\u4ec5\u73a9\u5bb6\u53ef\u7528(\u9700\u8981\u624b\u6301\u7269\u54c1)");
                    return true;
                }
                Player player = (Player)commandSender;
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack.isEmpty() || itemStack.getType().isAir()) {
                    Util.err(player, "\u624b\u4e0a\u6ca1\u6709\u7269\u54c1,\u62ff\u7740\u8981\u4e0a\u67b6\u7684\u4e1c\u897f\u518d\u6267\u884c");
                    return true;
                }
                ItemStack itemStack2 = itemStack.clone();
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
                    try {
                        this.plugin.db.addShopItem(itemStack2, d, d2, string3);
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.ok(player, "\u5df2\u4e0a\u67b6 " + Util.plainName(itemStack2) + " \u300c" + string3 + "\u300d,\u4e70 \u00a5" + Util.money(d) + " / \u5356 \u00a5" + Util.money(d2) + ",\u7acb\u5373\u751f\u6548(\u65e0\u9700\u91cd\u542f)"));
                    }
                    catch (Exception exception) {
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u4e0a\u67b6\u5931\u8d25:" + exception.getMessage()));
                    }
                });
                break;
            }
            case "rm": {
                if (!(commandSender instanceof Player)) {
                    commandSender.sendMessage("\u4ec5\u73a9\u5bb6\u53ef\u7528");
                    return true;
                }
                Player player = (Player)commandSender;
                this.openRemoveAssistant(player, 0);
                break;
            }
            case "removecat": {
                if (stringArray.length < 2) {
                    commandSender.sendMessage("\u7528\u6cd5: /shopadmin removecat <\u5206\u7c7b> - \u5f39\u7a97\u786e\u8ba4\u540e\u4e0b\u67b6\u8be5\u5206\u7c7b\u5168\u90e8\u5546\u54c1");
                    return true;
                }
                if (!(commandSender instanceof Player)) {
                    commandSender.sendMessage("\u4ec5\u73a9\u5bb6\u53ef\u7528");
                    return true;
                }
                Player player = (Player)commandSender;
                if (!ShopCmd.CATEGORIES.contains(stringArray[1])) {
                    commandSender.sendMessage("\u5206\u7c7b\u5fc5\u987b\u662f: \u5efa\u7b51\u65b9\u5757 / \u81ea\u7136\u65b9\u5757 / \u98df\u7269 / \u5176\u4ed6");
                    return true;
                }
                String string5 = stringArray[1];
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
                    try {
                        List<Db.ShopItem> list = this.plugin.db.listShopItems();
                        int n = 0;
                        for (Db.ShopItem shopItem : list) {
                            if (!shopItem.category().equals(string5)) continue;
                            ++n;
                        }
                        int n2 = n;
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                            if (n2 == 0) {
                                Util.err(player, "\u300c" + string5 + "\u300d\u5206\u7c7b\u4e0b\u6ca1\u6709\u5546\u54c1");
                            } else {
                                this.confirmRemoveCat(player, string5, n2);
                            }
                        });
                    }
                    catch (Exception exception) {
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u8bfb\u53d6\u5546\u5e97\u6570\u636e\u5931\u8d25"));
                    }
                });
                break;
            }
            case "addinv": {
                if (stringArray.length < 3) {
                    commandSender.sendMessage("\u7528\u6cd5: /shopadmin addinv <\u4e70\u4ef7> <\u5356\u4ef7> [\u5206\u7c7b] - \u4e00\u952e\u4e0a\u67b6\u80cc\u5305\u5168\u90e8\u7269\u54c1");
                    return true;
                }
                if (!(commandSender instanceof Player)) {
                    commandSender.sendMessage("\u4ec5\u73a9\u5bb6\u53ef\u7528(\u9700\u8981\u80cc\u5305\u7269\u54c1)");
                    return true;
                }
                Player player = (Player)commandSender;
                Double d = this.parse(commandSender, stringArray[1]);
                Double d3 = this.parse(commandSender, stringArray[2]);
                if (d == null || d3 == null) {
                    return true;
                }
                if (d == 0.0 && d3 == 0.0) {
                    Util.err(player, "\u4e70\u4ef7\u5356\u4ef7\u90fd\u662f 0,\u81f3\u5c11\u586b\u4e00\u4e2a(0 \u8868\u793a\u4e0d\u5f00\u653e)");
                    return true;
                }
                if (stringArray.length >= 4 && !ShopCmd.CATEGORIES.contains(stringArray[3])) {
                    commandSender.sendMessage("\u5206\u7c7b\u5fc5\u987b\u662f: \u5efa\u7b51\u65b9\u5757 / \u81ea\u7136\u65b9\u5757 / \u98df\u7269 / \u5176\u4ed6");
                    return true;
                }
                String string6 = stringArray.length >= 4 ? stringArray[3] : "\u5176\u4ed6";
                ArrayList<Integer> arrayList = new ArrayList<Integer>();
                ArrayList<ItemStack> arrayList2 = new ArrayList<ItemStack>();
                int n = 0;
                while (n < 36) {
                    ItemStack itemStack = player.getInventory().getItem(n);
                    if (itemStack != null && !itemStack.getType().isAir()) {
                        arrayList.add(n);
                        arrayList2.add(itemStack.clone());
                    }
                    ++n;
                }
                if (arrayList2.isEmpty()) {
                    Util.err(player, "\u80cc\u5305\u91cc\u6ca1\u6709\u7269\u54c1");
                    return true;
                }
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
                    try {
                        List<Db.ShopItem> list3 = this.plugin.db.listShopItems();
                        int added = 0;
                        int skipped = 0;
                        ArrayList<Integer> toClear = new ArrayList<Integer>();
                        int n3 = 0;
                        while (n3 < arrayList2.size()) {
                            ItemStack itemStack = (ItemStack)arrayList2.get(n3);
                            boolean bl = false;
                            for (Db.ShopItem shopItem : list3) {
                                if (!shopItem.item().isSimilar(itemStack)) continue;
                                bl = true;
                                break;
                            }
                            if (bl) {
                                ++skipped;
                            } else {
                                this.plugin.db.addShopItem(itemStack, d, d3, string6);
                                toClear.add((Integer)arrayList.get(n3));
                                ++added;
                            }
                            ++n3;
                        }
                        int n4 = added;
                        int n5 = skipped;
                        ArrayList<Integer> arrayList3 = toClear;
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                            Iterator iterator = arrayList3.iterator();
                            while (iterator.hasNext()) {
                                int slot = (Integer)iterator.next();
                                player.getInventory().setItem(slot, null);
                            }
                            Util.ok(player, "\u4e00\u952e\u4e0a\u67b6\u5b8c\u6210: \u65b0\u4e0a\u67b6 " + n4 + " \u4ef6\u300c" + string6 + "\u300d,\u8df3\u8fc7\u91cd\u590d " + n5 + " \u4ef6");
                        });
                    }
                    catch (Exception exception) {
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u6279\u91cf\u4e0a\u67b6\u5931\u8d25: " + exception.getMessage()));
                    }
                });
                break;
            }
            case "setcat": {
                long l;
                if (stringArray.length < 3) {
                    commandSender.sendMessage("\u7528\u6cd5: /shopadmin setcat <id> <\u5206\u7c7b>");
                    commandSender.sendMessage("\u5206\u7c7b: \u5efa\u7b51\u65b9\u5757 / \u81ea\u7136\u65b9\u5757 / \u98df\u7269 / \u5176\u4ed6");
                    return true;
                }
                if (!ShopCmd.CATEGORIES.contains(stringArray[2])) {
                    commandSender.sendMessage("\u5206\u7c7b\u5fc5\u987b\u662f: \u5efa\u7b51\u65b9\u5757 / \u81ea\u7136\u65b9\u5757 / \u98df\u7269 / \u5176\u4ed6");
                    return true;
                }
                try {
                    l = Long.parseLong(stringArray[1]);
                }
                catch (NumberFormatException numberFormatException) {
                    commandSender.sendMessage("\u7f16\u53f7\u5fc5\u987b\u662f\u6570\u5b57: " + stringArray[1]);
                    return true;
                }
                String string7 = stringArray[2];
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
                    try {
                        boolean bl = this.plugin.db.setCategory(l, string7);
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> commandSender.sendMessage(bl ? "\u5df2\u628a #" + l + " \u79fb\u5165\u300c" + string7 + "\u300d" : "\u6ca1\u627e\u5230 #" + l));
                    }
                    catch (Exception exception) {
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> commandSender.sendMessage("\u6539\u5206\u7c7b\u5931\u8d25: " + exception.getMessage()));
                    }
                });
                break;
            }
            case "price": {
                Double d;
                long l;
                if (stringArray.length < 4) {
                    commandSender.sendMessage("\u7528\u6cd5: /shopadmin price <id> <\u4e70\u4ef7> <\u5356\u4ef7>(\u586b - \u8868\u793a\u4e0d\u6539)");
                    return true;
                }
                try {
                    l = Long.parseLong(stringArray[1]);
                }
                catch (NumberFormatException numberFormatException) {
                    commandSender.sendMessage("id \u5fc5\u987b\u662f\u6570\u5b57,\u5148\u7528 list \u67e5\u770b");
                    return true;
                }
                Double d4 = "-".equals(stringArray[2]) ? null : this.parse(commandSender, stringArray[2]);
                Double d5 = d = "-".equals(stringArray[3]) ? null : this.parse(commandSender, stringArray[3]);
                if (!stringArray[2].equals("-") && d4 == null) {
                    return true;
                }
                if (!stringArray[3].equals("-") && d == null) {
                    return true;
                }
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
                    try {
                        this.plugin.db.setPrice(l, d4, d);
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> commandSender.sendMessage("\u5df2\u4fee\u6539 #" + l + " \u7684\u4ef7\u683c,\u7acb\u5373\u751f\u6548"));
                    }
                    catch (Exception exception) {
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> commandSender.sendMessage("\u4fee\u6539\u5931\u8d25:" + exception.getMessage()));
                    }
                });
                break;
            }
            case "remove": {
                long l;
                if (stringArray.length < 2) {
                    commandSender.sendMessage("\u7528\u6cd5: /shopadmin remove <id>");
                    return true;
                }
                try {
                    l = Long.parseLong(stringArray[1]);
                }
                catch (NumberFormatException numberFormatException) {
                    commandSender.sendMessage("id \u5fc5\u987b\u662f\u6570\u5b57");
                    return true;
                }
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
                    try {
                        boolean bl = this.plugin.db.removeShopItem(l);
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> commandSender.sendMessage(bl ? "\u5df2\u4e0b\u67b6 #" + l : "\u6ca1\u627e\u5230 #" + l + ",\u5148\u7528 list \u67e5\u770b"));
                    }
                    catch (Exception exception) {
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> commandSender.sendMessage("\u4e0b\u67b6\u5931\u8d25:" + exception.getMessage()));
                    }
                });
                break;
            }
            case "list": {
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
                    try {
                        List<Db.ShopItem> list = this.plugin.db.listShopItems();
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                            commandSender.sendMessage("=== \u5b98\u65b9\u5546\u5e97(" + list.size() + ") ===");
                            for (Db.ShopItem shopItem : list) {
                                commandSender.sendMessage("#" + shopItem.id() + " " + Util.plainName(shopItem.item()) + " [" + shopItem.category() + "] \u4e70 \u00a5" + Util.money(shopItem.buy()) + " / \u5356 \u00a5" + Util.money(shopItem.sell()));
                            }
                        });
                    }
                    catch (Exception exception) {
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> commandSender.sendMessage("\u8bfb\u53d6\u5931\u8d25:" + exception.getMessage()));
                    }
                });
                break;
            }
            default: {
                this.help(commandSender);
            }
        }
        return true;
    }

    private void help(CommandSender commandSender) {
        commandSender.sendMessage("=== SenyuShop \u7ba1\u7406 ===");
        commandSender.sendMessage("/shopadmin(\u65e0\u53c2\u6570) - \u6253\u5f00\u4e0a\u67b6\u52a9\u624b(\u586b\u4ef7\u683c\u5206\u7c7b,\u70b9\u80cc\u5305\u7269\u54c1\u4e0a\u67b6)");
        commandSender.sendMessage("/shopadmin addinv <\u4e70\u4ef7> <\u5356\u4ef7> [\u5206\u7c7b] - \u4e00\u952e\u4e0a\u67b6\u80cc\u5305\u5168\u90e8\u7269\u54c1");
        commandSender.sendMessage("/shopadmin rm - \u6253\u5f00\u4e0b\u67b6\u52a9\u624b(\u70b9\u5546\u54c1\u9010\u4e2a\u4e0b\u67b6)");
        commandSender.sendMessage("/shopadmin removecat <\u5206\u7c7b> - \u5f39\u7a97\u786e\u8ba4\u540e\u4e00\u952e\u4e0b\u67b6\u8be5\u5206\u7c7b\u5168\u90e8\u5546\u54c1");
        commandSender.sendMessage("/shopadmin addhand <\u4e70\u4ef7> <\u5356\u4ef7> [\u5206\u7c7b] - \u4e0a\u67b6\u624b\u4e0a\u7684\u7269\u54c1(add \u540c\u4e49)");
        commandSender.sendMessage("    \u5206\u7c7b: \u5efa\u7b51\u65b9\u5757 / \u81ea\u7136\u65b9\u5757 / \u98df\u7269 / \u5176\u4ed6(\u9ed8\u8ba4 \u5176\u4ed6)");
        commandSender.sendMessage("/shopadmin setcat <id> <\u5206\u7c7b> - \u6539\u5546\u54c1\u5206\u7c7b");
        commandSender.sendMessage("/shopadmin price <id> <\u4e70\u4ef7> <\u5356\u4ef7> - \u6539\u4ef7(- \u8868\u793a\u4e0d\u6539)");
        commandSender.sendMessage("/shopadmin remove <id> - \u4e0b\u67b6");
        commandSender.sendMessage("/shopadmin list - \u67e5\u770b\u5168\u90e8\u5546\u54c1");
    }

    private List<String> cats(String string) {
        return ShopCmd.CATEGORIES.stream().filter(string2 -> string2.startsWith(string)).toList();
    }

    private Double parse(CommandSender commandSender, String string) {
        try {
            double d = Double.parseDouble(string);
            if (d < 0.0 || Double.isNaN(d) || Double.isInfinite(d)) {
                throw new NumberFormatException();
            }
            return d;
        }
        catch (NumberFormatException numberFormatException) {
            commandSender.sendMessage("\u4ef7\u683c\u5fc5\u987b\u662f \u22650 \u7684\u6570\u5b57:" + string);
            return null;
        }
    }

    public List<String> onTabComplete(CommandSender commandSender, Command command, String string2, String[] stringArray) {
        if (stringArray.length == 1) {
            return List.of("addhand", "add", "price", "setcat", "addinv", "rm", "removecat", "remove", "list").stream().filter(string -> string.startsWith(stringArray[0].toLowerCase())).toList();
        }
        if (stringArray.length == 3 && stringArray[0].equalsIgnoreCase("setcat")) {
            return this.cats(stringArray[2]);
        }
        if (stringArray.length == 2 && stringArray[0].equalsIgnoreCase("removecat")) {
            return this.cats(stringArray[1]);
        }
        if (stringArray.length == 4 && (stringArray[0].equalsIgnoreCase("addhand") || stringArray[0].equalsIgnoreCase("add"))) {
            return this.cats(stringArray[3]);
        }
        return List.of();
    }

    public void openAssistant(Player player, int n) {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            List<Db.ShopItem> list;
            try {
                list = this.plugin.db.listShopItems();
            }
            catch (Exception exception) {
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u8bfb\u53d6\u5546\u5e97\u6570\u636e\u5931\u8d25"));
                return;
            }
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.showAssistant(player, n, list));
        });
    }

    private void showAssistant(Player player, int n, List<Db.ShopItem> list) {
        int n2;
        if (!player.isOnline()) {
            return;
        }
        PlayerInventory playerInventory = player.getInventory();
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        int n3 = 0;
        while (n3 < 36) {
            ItemStack itemStack = playerInventory.getItem(n3);
            if (itemStack != null && !itemStack.getType().isAir()) {
                arrayList.add(n3);
            }
            ++n3;
        }
        if (arrayList.isEmpty()) {
            Util.msg(player, "\u80cc\u5305\u91cc\u6ca1\u6709\u7269\u54c1,\u628a\u8981\u4e0a\u67b6\u7684\u4e1c\u897f\u653e\u8fdb\u80cc\u5305\u518d\u6253\u5f00 /shopadmin", NamedTextColor.YELLOW);
            return;
        }
        n3 = 18;
        int n4 = (arrayList.size() + n3 - 1) / n3;
        if (n < 0) {
            n = 0;
        }
        if (n >= n4) {
            n = n4 - 1;
        }
        int n5 = n;
        ArrayList<DialogInput> arrayList2 = new ArrayList<DialogInput>();
        arrayList2.add(DialogInput.text((String)"buy", (Component)Component.text((String)"\u4e70\u4ef7(0=\u4e0d\u5f00\u653e)")).initial("0").maxLength(9).width(160).build());
        arrayList2.add(DialogInput.text((String)"sell", (Component)Component.text((String)"\u5356\u4ef7(0=\u4e0d\u5f00\u653e)")).initial("0").maxLength(9).width(160).build());
        arrayList2.add(DialogInput.singleOption((String)"cat", (Component)Component.text((String)"\u5206\u7c7b"), List.of(SingleOptionDialogInput.OptionEntry.create((String)"\u5efa\u7b51\u65b9\u5757", (Component)Component.text((String)"\u5efa\u7b51\u65b9\u5757"), (boolean)false), SingleOptionDialogInput.OptionEntry.create((String)"\u81ea\u7136\u65b9\u5757", (Component)Component.text((String)"\u81ea\u7136\u65b9\u5757"), (boolean)false), SingleOptionDialogInput.OptionEntry.create((String)"\u98df\u7269", (Component)Component.text((String)"\u98df\u7269"), (boolean)false), SingleOptionDialogInput.OptionEntry.create((String)"\u5176\u4ed6", (Component)Component.text((String)"\u5176\u4ed6"), (boolean)true))).build());
        ArrayList<ActionButton> arrayList3 = new ArrayList<ActionButton>();
        int n6 = n2 = n5 * n3;
        while (n6 < Math.min(n2 + n3, arrayList.size())) {
            int n7 = (Integer)arrayList.get(n6);
            ItemStack itemStack = playerInventory.getItem(n7);
            arrayList3.add(ActionButton.builder((Component)Util.name(itemStack, NamedTextColor.WHITE)).tooltip((Component)Component.text((String)"\u70b9\u51fb\u4e0a\u67b6(\u7528\u4e0a\u9762\u586b\u7684\u4ef7\u683c/\u5206\u7c7b)", (TextColor)NamedTextColor.GRAY)).action(Util.clickInput(this.plugin, dialogResponseView -> this.addFromSlot(player, n7, (DialogResponseView)dialogResponseView, list, n5))).build());
            ++n6;
        }
        if (n5 > 0) {
            arrayList3.add(ActionButton.builder((Component)Component.text((String)"\u00ab \u4e0a\u4e00\u9875")).action(Util.click(this.plugin, () -> this.openAssistant(player, n5 - 1))).build());
        }
        if (n5 < n4 - 1) {
            arrayList3.add(ActionButton.builder((Component)Component.text((String)"\u4e0b\u4e00\u9875 \u00bb")).action(Util.click(this.plugin, () -> this.openAssistant(player, n5 + 1))).build());
        }
        ActionButton actionButton = ActionButton.builder((Component)Component.text((String)"\u5b8c\u6210")).action(Util.click(this.plugin, () -> {})).build();
        Dialog dialog = Dialog.create(registryBuilderFactory -> {
            DialogRegistryEntry.Builder builder = ((DialogRegistryEntry.Builder)registryBuilderFactory.empty()).base(DialogBase.builder((Component)Component.text((String)("\u4e0a\u67b6\u52a9\u624b " + (n5 + 1) + "/" + n4 + " \u9875(\u80cc\u5305 " + arrayList.size() + " \u683c\u6709\u8d27)"))).inputs(arrayList2).canCloseWithEscape(true).build()).type((DialogType)DialogType.multiAction((List)arrayList3, (ActionButton)actionButton, (int)2));
        });
        player.showDialog((DialogLike)dialog);
    }

    private void addFromSlot(Player player, int n, DialogResponseView dialogResponseView, List<Db.ShopItem> list, int n2) {
        Double d = this.parseDlg(player, dialogResponseView == null ? null : dialogResponseView.getText("buy"));
        Double d2 = this.parseDlg(player, dialogResponseView == null ? null : dialogResponseView.getText("sell"));
        if (d == null || d2 == null) {
            return;
        }
        if (d == 0.0 && d2 == 0.0) {
            Util.err(player, "\u4e70\u4ef7\u5356\u4ef7\u90fd\u662f 0,\u81f3\u5c11\u586b\u4e00\u4e2a(0 \u8868\u793a\u4e0d\u5f00\u653e)");
            return;
        }
        String string = dialogResponseView == null ? null : dialogResponseView.getText("cat");
        String string2 = string != null && ShopCmd.CATEGORIES.contains(string) ? string : "\u5176\u4ed6";
        ItemStack itemStack = player.getInventory().getItem(n);
        if (itemStack == null || itemStack.getType().isAir()) {
            Util.err(player, "\u90a3\u4e00\u683c\u5df2\u7ecf\u7a7a\u4e86");
            this.openAssistant(player, n2);
            return;
        }
        ItemStack itemStack2 = itemStack.clone();
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                for (Db.ShopItem shopItem : list) {
                    if (!shopItem.item().isSimilar(itemStack2)) continue;
                    Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u8fd9\u4e2a\u7269\u54c1\u5df2\u5728\u5546\u5e97(#" + shopItem.id() + " " + Util.plainName(shopItem.item()) + "),\u8df3\u8fc7;\u8981\u91cd\u4e0a\u5148 remove"));
                    return;
                }
                this.plugin.db.addShopItem(itemStack2, d, d2, string2);
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                    player.getInventory().setItem(n, null);
                    Util.ok(player, "\u5df2\u4e0a\u67b6 " + Util.plainName(itemStack2) + " \u300c" + string2 + "\u300d,\u4e70 \u00a5" + Util.money(d) + " / \u5356 \u00a5" + Util.money(d2));
                    this.openAssistant(player, n2);
                });
            }
            catch (Exception exception) {
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u4e0a\u67b6\u5931\u8d25: " + exception.getMessage()));
            }
        });
    }

    private Double parseDlg(Player player, String string) {
        if (string == null || string.isBlank()) {
            return 0.0;
        }
        try {
            double d = Double.parseDouble(string.trim());
            if (d < 0.0 || Double.isNaN(d) || Double.isInfinite(d)) {
                throw new NumberFormatException();
            }
            return d;
        }
        catch (NumberFormatException numberFormatException) {
            Util.err(player, "\u4ef7\u683c\u5fc5\u987b\u662f \u22650 \u7684\u6570\u5b57: " + string);
            return null;
        }
    }

    public void openRemoveAssistant(Player player, int n) {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            List<Db.ShopItem> list;
            try {
                list = this.plugin.db.listShopItems();
            }
            catch (Exception exception) {
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u8bfb\u53d6\u5546\u5e97\u6570\u636e\u5931\u8d25"));
                return;
            }
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.showRemoveAssistant(player, n, list));
        });
    }

    private void showRemoveAssistant(Player player, int n, List<Db.ShopItem> list) {
        if (!player.isOnline()) {
            return;
        }
        if (list.isEmpty()) {
            Util.msg(player, "\u5546\u5e97\u91cc\u8fd8\u6ca1\u6709\u5546\u54c1", NamedTextColor.YELLOW);
            return;
        }
        int n2 = 18;
        int n3 = (list.size() + n2 - 1) / n2;
        if (n < 0) {
            n = 0;
        }
        if (n >= n3) {
            n = n3 - 1;
        }
        int n4 = n;
        ArrayList<ActionButton> arrayList = new ArrayList<ActionButton>();
        int n5 = n4 * n2;
        while (n5 < Math.min(n4 * n2 + n2, list.size())) {
            Db.ShopItem shopItem = list.get(n5);
            arrayList.add(ActionButton.builder((Component)Util.name(shopItem.item(), NamedTextColor.WHITE)).tooltip((Component)Component.text((String)("#" + shopItem.id() + " \u300c" + shopItem.category() + "\u300d\u4e70 \u00a5" + Util.money(shopItem.buy()) + " / \u5356 \u00a5" + Util.money(shopItem.sell()) + " \u2014 \u70b9\u51fb\u4e0b\u67b6"), (TextColor)NamedTextColor.GRAY)).action(Util.click(this.plugin, () -> this.removeOne(player, shopItem.id(), n4))).build());
            ++n5;
        }
        if (n4 > 0) {
            arrayList.add(ActionButton.builder((Component)Component.text((String)"\u00ab \u4e0a\u4e00\u9875")).action(Util.click(this.plugin, () -> this.openRemoveAssistant(player, n4 - 1))).build());
        }
        if (n4 < n3 - 1) {
            arrayList.add(ActionButton.builder((Component)Component.text((String)"\u4e0b\u4e00\u9875 \u00bb")).action(Util.click(this.plugin, () -> this.openRemoveAssistant(player, n4 + 1))).build());
        }
        ActionButton actionButton = ActionButton.builder((Component)Component.text((String)"\u5b8c\u6210")).action(Util.click(this.plugin, () -> {})).build();
        Dialog dialog = Dialog.create(registryBuilderFactory -> {
            DialogRegistryEntry.Builder builder = ((DialogRegistryEntry.Builder)registryBuilderFactory.empty()).base(DialogBase.builder((Component)Component.text((String)("\u4e0b\u67b6\u52a9\u624b " + (n4 + 1) + "/" + n3 + " \u9875(\u5171 " + list.size() + " \u4ef6)"))).canCloseWithEscape(true).build()).type((DialogType)DialogType.multiAction((List)arrayList, (ActionButton)actionButton, (int)2));
        });
        player.showDialog((DialogLike)dialog);
    }

    private void removeOne(Player player, long l, int n) {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                boolean bl = this.plugin.db.removeShopItem(l);
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                    if (bl) {
                        Util.ok(player, "\u5df2\u4e0b\u67b6 #" + l);
                    } else {
                        Util.err(player, "\u4e0b\u67b6\u5931\u8d25(\u53ef\u80fd\u5df2\u88ab\u5220\u9664)");
                    }
                    this.openRemoveAssistant(player, n);
                });
            }
            catch (Exception exception) {
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u4e0b\u67b6\u5931\u8d25: " + exception.getMessage()));
            }
        });
    }

    private void confirmRemoveCat(Player player, String string, int n) {
        ActionButton actionButton = ActionButton.builder((Component)Component.text((String)("\u2714 \u5168\u90e8\u4e0b\u67b6(" + n + " \u4ef6)"), (TextColor)NamedTextColor.RED)).action(Util.click(this.plugin, () -> this.doRemoveCat(player, string))).build();
        ActionButton actionButton2 = ActionButton.builder((Component)Component.text((String)"\u2718 \u53d6\u6d88")).action(Util.click(this.plugin, () -> Util.msg(player, "\u5df2\u53d6\u6d88", NamedTextColor.YELLOW))).build();
        Dialog dialog = Dialog.create(registryBuilderFactory -> {
            DialogRegistryEntry.Builder builder = ((DialogRegistryEntry.Builder)registryBuilderFactory.empty()).base(DialogBase.builder((Component)Component.text((String)("\u786e\u8ba4\u4e0b\u67b6\u5168\u90e8\u300c" + string + "\u300d\u5546\u54c1?"), (TextColor)NamedTextColor.RED)).body(List.of(DialogBody.plainMessage((Component)Component.text((String)("\u8be5\u5206\u7c7b\u5171 " + n + " \u4ef6\u5546\u54c1,\u4e0b\u67b6\u540e\u73a9\u5bb6\u5c06\u65e0\u6cd5\u8d2d\u4e70/\u56de\u6536\u3002\u6b64\u64cd\u4f5c\u4e0d\u53ef\u64a4\u9500!"))))).canCloseWithEscape(true).build()).type((DialogType)DialogType.multiAction(List.of(actionButton, actionButton2), null, (int)2));
        });
        player.showDialog((DialogLike)dialog);
    }

    private void doRemoveCat(Player player, String string) {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                int n = this.plugin.db.deleteByCategory(string);
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.ok(player, "\u5df2\u4e0b\u67b6\u300c" + string + "\u300d\u5168\u90e8 " + n + " \u4ef6\u5546\u54c1"));
            }
            catch (Exception exception) {
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u6279\u91cf\u4e0b\u67b6\u5931\u8d25: " + exception.getMessage()));
            }
        });
    }
}

