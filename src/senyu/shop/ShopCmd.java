/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.dialog.Dialog
 *  io.papermc.paper.dialog.DialogResponseView
 *  io.papermc.paper.registry.RegistryBuilderFactory
 *  io.papermc.paper.registry.data.dialog.ActionButton
 *  io.papermc.paper.registry.data.dialog.DialogBase
 *  io.papermc.paper.registry.data.dialog.DialogRegistryEntry$Builder
 *  io.papermc.paper.registry.data.dialog.body.DialogBody
 *  io.papermc.paper.registry.data.dialog.input.DialogInput
 *  io.papermc.paper.registry.data.dialog.input.TextDialogInput
 *  io.papermc.paper.registry.data.dialog.type.DialogType
 *  net.kyori.adventure.dialog.DialogLike
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.milkbowl.vault.economy.EconomyResponse
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.plugin.Plugin
 */
package senyu.shop;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.RegistryBuilderFactory;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import senyu.shop.Db;
import senyu.shop.SenyuShopPlugin;
import senyu.shop.Util;
import senyu.shop.ZhNames;

public class ShopCmd
implements CommandExecutor {
    private final SenyuShopPlugin plugin;
    public static final List<String> CATEGORIES = List.of("\u5efa\u7b51\u65b9\u5757", "\u81ea\u7136\u65b9\u5757", "\u98df\u7269", "\u5176\u4ed6");

    public ShopCmd(SenyuShopPlugin senyuShopPlugin) {
        this.plugin = senyuShopPlugin;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] stringArray) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("\u4ec5\u73a9\u5bb6\u53ef\u7528");
            return true;
        }
        Player player = (Player)commandSender;
        if (!player.hasPermission("senyushop.use")) {
            Util.err(player, "\u6ca1\u6709\u6743\u9650");
            return true;
        }
        if (!this.plugin.checkCooldown(player.getUniqueId())) {
            return true;
        }
        if (stringArray.length > 0) {
            String string2 = stringArray[0].toLowerCase();
            if (string2.equals("search") || string2.equals("s")) {
                String string3;
                String string4 = string3 = stringArray.length > 1 ? String.join((CharSequence)" ", Arrays.asList(stringArray).subList(1, stringArray.length)) : "";
                if (string3.isBlank()) {
                    Util.err(player, "\u7528\u6cd5: /shop search <\u5173\u952e\u8bcd>(\u4e2d\u6587\u540d\u6216\u82f1\u6587\u5982 stone)");
                    return true;
                }
                this.openSearch(player, string3.trim());
                return true;
            }
            if (string2.equals("sell") || string2.equals("recycle")) {
                this.openSell(player, 0);
                return true;
            }
            if (CATEGORIES.contains(stringArray[0]) || string2.equals("\u56de\u6536\u4e13\u573a") || string2.equals("huishou")) {
                this.openCategory(player, stringArray[0]);
                return true;
            }
        }
        this.openMain(player);
        return true;
    }

    public void openMain(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            List<Db.ShopItem> list;
            try {
                list = this.plugin.db.listShopItems();
            }
            catch (Exception exception) {
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u8bfb\u53d6\u5546\u5e97\u6570\u636e\u5931\u8d25"));
                return;
            }
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.showMain(player, list));
        });
    }

    public void openCategory(Player player, String string) {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            List<Db.ShopItem> list;
            try {
                list = this.plugin.db.listShopItems();
            }
            catch (Exception exception) {
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u8bfb\u53d6\u5546\u5e97\u6570\u636e\u5931\u8d25"));
                return;
            }
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.showCategory(player, string, list));
        });
    }

    private void showMain(Player player, List<Db.ShopItem> list) {
        if (!player.isOnline()) {
            return;
        }
        if (list.isEmpty()) {
            Util.msg(player, "\u5546\u5e97\u6682\u65f6\u6ca1\u6709\u5546\u54c1,\u7ba1\u7406\u5458\u53ef\u7528 /shopadmin addhand <\u4e70\u4ef7> <\u5356\u4ef7> [\u5206\u7c7b] \u4e0a\u67b6", NamedTextColor.YELLOW);
            return;
        }
        ArrayList<ActionButton> arrayList = new ArrayList<ActionButton>();
        for (String string : CATEGORIES) {
            long l = list.stream().filter(shopItem -> string.equals(shopItem.category())).count();
            arrayList.add(ActionButton.builder((Component)Component.text((String)(string + " (" + l + ")"))).tooltip((Component)Component.text((String)"\u67e5\u770b\u8be5\u5206\u7c7b\u4e0b\u7684\u5546\u54c1", (TextColor)NamedTextColor.GRAY)).action(Util.click(this.plugin, () -> this.showCategory(player, string, list))).build());
        }
        long l = list.stream().filter(shopItem -> shopItem.sell() > 0.0).count();
        arrayList.add(ActionButton.builder((Component)Component.text((String)("\u267b \u56de\u6536\u4e13\u573a (" + l + ")"))).tooltip((Component)Component.text((String)"\u53ea\u770b\u53ef\u56de\u6536\u51fa\u552e\u7684\u5546\u54c1", (TextColor)NamedTextColor.GRAY)).action(Util.click(this.plugin, () -> this.showCategory(player, "\u56de\u6536\u4e13\u573a", list, 0))).build());
        arrayList.add(ActionButton.builder((Component)Component.text((String)"\ud83d\udd0d \u641c\u7d22", (TextColor)NamedTextColor.AQUA)).tooltip((Component)Component.text((String)"\u6309\u5173\u952e\u8bcd\u67e5\u627e\u5546\u54c1(\u652f\u6301\u4e2d\u6587)", (TextColor)NamedTextColor.GRAY)).action(Util.click(this.plugin, () -> this.showSearch(player, list, null))).build());
        Dialog dialog = Dialog.create(registryBuilderFactory -> {
            DialogRegistryEntry.Builder builder = ((DialogRegistryEntry.Builder)registryBuilderFactory.empty()).base(DialogBase.builder((Component)Component.text((String)("\u68ee\u5c7f\u5546\u5e97 - \u9009\u62e9\u5206\u7c7b(\u5171 " + list.size() + " \u79cd)"))).canCloseWithEscape(true).build()).type((DialogType)DialogType.multiAction((List)arrayList, null, (int)2));
        });
        player.showDialog((DialogLike)dialog);
    }

    private void showCategory(Player player, String string, List<Db.ShopItem> list) {
        this.showCategory(player, string, list, 0);
    }

    private void showCategory(Player player, String string, List<Db.ShopItem> list, int n) {
        if (!player.isOnline()) {
            return;
        }
        String searchKw = null;
        List<Db.ShopItem> list2;
        if (string.startsWith("\u641c\u7d22:")) {
            list2 = list.stream().filter(shopItem -> this.matches((Db.ShopItem)shopItem, string.substring(3).trim().toLowerCase())).toList();
        } else if (string.startsWith("\u7c7b\u641c:")) {
            int n2 = string.indexOf(58, 3);
            String category = string.substring(3, n2);
            String string6 = string.substring(n2 + 1).trim().toLowerCase();
            list2 = category.equals("\u56de\u6536\u4e13\u573a") ? list.stream().filter(shopItem -> shopItem.sell() > 0.0 && this.matches((Db.ShopItem)shopItem, string6)).toList() : list.stream().filter(arg_0 -> this.lambda$11(category, string6, arg_0)).toList();
        } else if (string.equals("\u56de\u6536\u4e13\u573a")) {
            searchKw = "\u56de\u6536\u4e13\u573a";
            list2 = list.stream().filter(shopItem -> shopItem.sell() > 0.0).toList();
        } else {
            searchKw = string;
            list2 = list.stream().filter(shopItem -> string.equals(shopItem.category())).toList();
        }
        if (list2.isEmpty()) {
            if (string.startsWith("\u641c\u7d22:")) {
                Util.msg(player, "\u6ca1\u627e\u5230\u4e0e\u300c" + string.substring(3) + "\u300d\u76f8\u5173\u7684\u5546\u54c1", NamedTextColor.YELLOW);
            } else if (string.startsWith("\u7c7b\u641c:")) {
                int n3 = string.indexOf(58, 3);
                Util.msg(player, "\u300c" + string.substring(3, n3) + "\u300d\u91cc\u6ca1\u627e\u5230\u4e0e\u300c" + string.substring(n3 + 1) + "\u300d\u76f8\u5173\u7684\u5546\u54c1", NamedTextColor.YELLOW);
            } else if (string.equals("\u56de\u6536\u4e13\u573a")) {
                Util.msg(player, "\u8fd8\u6ca1\u6709\u53ef\u56de\u6536\u7684\u5546\u54c1(\u56de\u6536\u4ef7 > 0 \u7684\u624d\u663e\u793a\u5728\u8fd9\u91cc)", NamedTextColor.YELLOW);
            } else {
                Util.msg(player, "\u300c" + string + "\u300d\u8fd8\u6ca1\u6709\u5546\u54c1,\u7ba1\u7406\u5458\u7528 /shopadmin addhand <\u4e70\u4ef7> <\u5356\u4ef7> " + string + " \u4e0a\u67b6", NamedTextColor.YELLOW);
            }
            return;
        }
        String searchKwFinal = searchKw;
        int pageSize = 18;
        int pages = (list2.size() + pageSize - 1) / pageSize;
        int page = Math.max(0, Math.min(n, pages - 1));
        List<ActionButton> buttons = new ArrayList<ActionButton>();
        if (searchKw != null) {
            buttons.add(ActionButton.builder((Component)Component.text((String)"\ud83d\udd0d \u641c\u7d22\u672c\u5206\u7c7b", (TextColor)NamedTextColor.AQUA)).tooltip((Component)Component.text((String)("\u5728\u300c" + searchKw + "\u300d\u91cc\u6309\u5173\u952e\u8bcd\u67e5\u627e(\u652f\u6301\u4e2d\u6587)"), (TextColor)NamedTextColor.GRAY)).action(Util.click(this.plugin, () -> this.showSearch(player, list, searchKwFinal))).build());
        }
        int idx = page * pageSize;
        while (idx < Math.min(page * pageSize + pageSize, list2.size())) {
            Db.ShopItem item = list2.get(idx);
            String tip = item.buy() > 0.0 && item.sell() > 0.0 ? "\u70b9\u51fb\u67e5\u770b \u00b7 \u8d2d\u4e70 \u00a5" + Util.money(item.buy()) + " / \u56de\u6536 \u00a5" + Util.money(item.sell()) : (item.buy() > 0.0 ? "\u70b9\u51fb\u67e5\u770b \u00b7 \u4ec5\u8d2d\u4e70 \u00a5" + Util.money(item.buy()) : "\u70b9\u51fb\u67e5\u770b \u00b7 \u4ec5\u56de\u6536 \u00a5" + Util.money(item.sell()));
            buttons.add(ActionButton.builder((Component)Util.name(item.item(), NamedTextColor.WHITE)).tooltip((Component)Component.text((String)tip, (TextColor)NamedTextColor.GRAY)).action(Util.click(this.plugin, () -> this.openItem(player, item, string, list))).build());
            ++idx;
        }
        if (page > 0) {
            buttons.add(ActionButton.builder((Component)Component.text((String)"\u00ab \u4e0a\u4e00\u9875")).action(Util.click(this.plugin, () -> this.showCategory(player, string, list, page - 1))).build());
        }
        if (page < pages - 1) {
            buttons.add(ActionButton.builder((Component)Component.text((String)"\u4e0b\u4e00\u9875 \u00bb")).action(Util.click(this.plugin, () -> this.showCategory(player, string, list, page + 1))).build());
        }
        ActionButton backButton = ActionButton.builder((Component)Component.text((String)"\u00ab \u8fd4\u56de\u4e3b\u9875")).action(Util.click(this.plugin, () -> this.openMain(player))).build();
        Dialog dialog = Dialog.create(registryBuilderFactory -> {
            DialogRegistryEntry.Builder builder = ((DialogRegistryEntry.Builder)registryBuilderFactory.empty()).base(DialogBase.builder((Component)Component.text((String)("\u68ee\u5c7f\u5546\u5e97 - " + searchKwFinal + "(" + list2.size() + " \u79cd \u00b7 \u7b2c " + (page + 1) + "/" + pages + " \u9875)"))).canCloseWithEscape(true).build()).type((DialogType)DialogType.multiAction((List)buttons, (ActionButton)backButton, (int)2));
        });
        player.showDialog((DialogLike)dialog);
    }

    private boolean matches(Db.ShopItem shopItem, String string) {
        String string2 = shopItem.item().getType().getKey().getKey();
        String string3 = ZhNames.of(string2);
        if (string3 != null && string3.contains(string)) {
            return true;
        }
        return string2.contains(string) || Util.plainName(shopItem.item()).toLowerCase().contains(string);
    }

    private void showSearch(Player player, List<Db.ShopItem> list, String string) {
        if (!player.isOnline()) {
            return;
        }
        ArrayList<TextDialogInput> arrayList = new ArrayList<TextDialogInput>();
        arrayList.add(DialogInput.text((String)"kw", (Component)Component.text((String)"\u5173\u952e\u8bcd(\u652f\u6301\u4e2d\u6587\u7269\u54c1\u540d)")).maxLength(32).width(250).build());
        ArrayList<ActionButton> arrayList2 = new ArrayList<ActionButton>();
        arrayList2.add(Util.inputButton(this.plugin, "\ud83d\udd0d \u641c\u7d22", dialogResponseView -> {
            String string2;
            String string3 = string2 = dialogResponseView == null ? null : dialogResponseView.getText("kw");
            if (string2 == null || string2.isBlank()) {
                Util.err(player, "\u8bf7\u5148\u8f93\u5165\u5173\u952e\u8bcd");
                this.showSearch(player, list, string);
                return;
            }
            string2 = string2.trim();
            if (string == null) {
                this.showCategory(player, "\u641c\u7d22:" + string2, list, 0);
            } else {
                this.showCategory(player, "\u7c7b\u641c:" + string + ":" + string2, list, 0);
            }
        }));
        arrayList2.add(Util.button(this.plugin, "\u00ab \u8fd4\u56de", () -> {
            if (string == null) {
                this.showMain(player, list);
            } else {
                this.showCategory(player, string, list, 0);
            }
        }));
        Object object = string == null ? "\u68ee\u5c7f\u5546\u5e97 - \u641c\u7d22" : "\u68ee\u5c7f\u5546\u5e97 - \u5728\u300c" + string + "\u300d\u4e2d\u641c\u7d22";
        Dialog dialog = Dialog.create(arg_0 -> ShopCmd.lambda$22((String)object, arrayList, arrayList2, arg_0));
        player.showDialog((DialogLike)dialog);
    }

    private void openItem(Player player, Db.ShopItem shopItem, String string, List<Db.ShopItem> list) {
        if (!player.isOnline()) {
            return;
        }
        ArrayList<DialogBody> arrayList = new ArrayList<DialogBody>();
        arrayList.add(DialogBody.item((ItemStack)shopItem.item()).build());
        arrayList.add(DialogBody.plainMessage((Component)Component.text((String)("\u5355\u4ef7 \u00b7 \u8d2d\u4e70 \u00a5" + Util.money(shopItem.buy()) + " / \u56de\u6536 \u00a5" + Util.money(shopItem.sell())), (TextColor)NamedTextColor.GOLD)));
        ArrayList<DialogInput> arrayList2 = new ArrayList<DialogInput>();
        arrayList2.add(DialogInput.text((String)"count", (Component)Component.text((String)"\u6570\u91cf(\u5148\u586b\u6570\u5b57,\u518d\u70b9\u8d2d\u4e70/\u56de\u6536)")).initial("1").maxLength(9).width(250).build());
        ArrayList<ActionButton> arrayList3 = new ArrayList<ActionButton>();
        if (shopItem.buy() > 0.0) {
            arrayList3.add(Util.inputButton(this.plugin, "\u8d2d\u4e70(\u6309\u4e0a\u9762\u6570\u91cf)", dialogResponseView -> this.buy(player, shopItem, (DialogResponseView)dialogResponseView)));
        }
        if (shopItem.sell() > 0.0) {
            arrayList3.add(Util.inputButton(this.plugin, "\u56de\u6536(\u6309\u4e0a\u9762\u6570\u91cf)", dialogResponseView -> this.sell(player, shopItem, (DialogResponseView)dialogResponseView)));
        }
        arrayList3.add(Util.button(this.plugin, "\u00ab \u8fd4\u56de", () -> this.showCategory(player, string, list, 0)));
        Dialog dialog = Dialog.create(registryBuilderFactory -> {
            DialogRegistryEntry.Builder builder = ((DialogRegistryEntry.Builder)registryBuilderFactory.empty()).base(DialogBase.builder((Component)Component.text((String)("\u68ee\u5c7f\u5546\u5e97 - " + Util.plainName(shopItem.item())))).body(arrayList).inputs(arrayList2).canCloseWithEscape(true).build()).type((DialogType)DialogType.multiAction((List)arrayList3, null, (int)2));
        });
        player.showDialog((DialogLike)dialog);
    }

    private int readCount(DialogResponseView dialogResponseView) {
        String string;
        String string2 = string = dialogResponseView == null ? null : dialogResponseView.getText("count");
        if (string == null || string.isBlank()) {
            return 1;
        }
        try {
            return Math.max(0, Integer.parseInt(string.trim()));
        }
        catch (NumberFormatException numberFormatException) {
            return -1;
        }
    }

    private int fits(Player player, ItemStack itemStack, int n) {
        int n2 = itemStack.getMaxStackSize();
        int n3 = 0;
        ItemStack[] itemStackArray = player.getInventory().getStorageContents();
        int n4 = itemStackArray.length;
        int n5 = 0;
        while (n5 < n4) {
            ItemStack itemStack2 = itemStackArray[n5];
            if (itemStack2 == null || itemStack2.getType().isAir()) {
                n3 += n2;
            } else if (itemStack2.isSimilar(itemStack)) {
                n3 += n2 - itemStack2.getAmount();
            }
            if (n3 >= n) break;
            ++n5;
        }
        return Math.min(n3, n);
    }

    private void give(Player player, ItemStack itemStack, int n) {
        int n2 = n;
        while (n2 > 0) {
            int n3 = Math.min(itemStack.getMaxStackSize(), n2);
            ItemStack itemStack2 = itemStack.clone();
            itemStack2.setAmount(n3);
            player.getInventory().addItem(new ItemStack[]{itemStack2});
            n2 -= n3;
        }
    }

    private void buy(Player player, Db.ShopItem shopItem, DialogResponseView dialogResponseView) {
        if (this.plugin.eco == null) {
            Util.err(player, "\u7ecf\u6d4e\u7cfb\u7edf\u4e0d\u53ef\u7528");
            return;
        }
        int n = this.readCount(dialogResponseView);
        if (n < 0) {
            Util.err(player, "\u6570\u91cf\u5fc5\u987b\u662f\u6574\u6570,\u4f8b\u5982 10");
            return;
        }
        if (n == 0) {
            Util.err(player, "\u6570\u91cf\u81f3\u5c11\u662f 1");
            return;
        }
        double d = shopItem.buy();
        ItemStack itemStack = shopItem.item().clone();
        itemStack.setAmount(1);
        int n2 = this.fits(player, itemStack, n);
        if (n2 <= 0) {
            Util.err(player, "\u80cc\u5305\u5df2\u6ee1,\u4e00\u4e2a\u90fd\u88c5\u4e0d\u4e0b");
            return;
        }
        int n3 = (int)Math.floor(this.plugin.eco.getBalance((OfflinePlayer)player) / d);
        if (n3 <= 0) {
            Util.err(player, "\u4f59\u989d\u4e0d\u8db3(\u5355\u4ef7 \u00a5" + Util.money(d) + ")");
            return;
        }
        int n4 = Math.min(n, Math.min(n2, n3));
        double d2 = d * (double)n4;
        EconomyResponse economyResponse = this.plugin.eco.withdrawPlayer((OfflinePlayer)player, d2);
        if (!economyResponse.transactionSuccess()) {
            Util.err(player, "\u6263\u6b3e\u5931\u8d25:" + economyResponse.errorMessage);
            return;
        }
        this.give(player, itemStack, n4);
        if (n4 < n) {
            Util.msg(player, "\u5b9e\u9645\u8d2d\u4e70 " + n4 + " \u4e2a(\u53d7\u80cc\u5305\u7a7a\u95f4/\u4f59\u989d\u9650\u5236),\u82b1\u8d39 \u00a5" + Util.money(d2), NamedTextColor.YELLOW);
        } else {
            Util.ok(player, "\u5df2\u8d2d\u4e70 " + Util.plainName(shopItem.item()) + " \u00d7" + n4 + ",\u82b1\u8d39 \u00a5" + Util.money(d2));
        }
    }

    private void sell(Player player, Db.ShopItem shopItem, DialogResponseView dialogResponseView) {
        int n;
        if (this.plugin.eco == null) {
            Util.err(player, "\u7ecf\u6d4e\u7cfb\u7edf\u4e0d\u53ef\u7528");
            return;
        }
        int n2 = this.readCount(dialogResponseView);
        if (n2 < 0) {
            Util.err(player, "\u6570\u91cf\u5fc5\u987b\u662f\u6574\u6570,\u4f8b\u5982 10");
            return;
        }
        if (n2 == 0) {
            Util.err(player, "\u6570\u91cf\u81f3\u5c11\u662f 1");
            return;
        }
        double d = shopItem.sell();
        ItemStack itemStack = shopItem.item().clone();
        itemStack.setAmount(1);
        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] storageContents = playerInventory.getStorageContents();
        int matching = 0;
        if (storageContents != null) {
            for (ItemStack contentsItem : storageContents) {
                if (contentsItem != null && contentsItem.isSimilar(itemStack)) {
                    matching += contentsItem.getAmount();
                }
            }
        }
        if (matching <= 0) {
            Util.err(player, "\u80cc\u5305\u91cc\u6ca1\u6709\u53ef\u56de\u6536\u7684 " + Util.plainName(shopItem.item()));
            return;
        }
        int take = Math.min(n2, matching);
        int remaining = take;
        int slot = 0;
        while (slot < Math.max(1, storageContents.length) && remaining > 0) {
            ItemStack slotItem = playerInventory.getItem(slot);
            if (slotItem != null && slotItem.isSimilar(itemStack)) {
                int takeAmount = Math.min(slotItem.getAmount(), remaining);
                if (takeAmount >= slotItem.getAmount()) {
                    playerInventory.setItem(slot, null);
                } else {
                    slotItem.setAmount(slotItem.getAmount() - takeAmount);
                }
                remaining -= takeAmount;
            }
            ++slot;
        }
        double d2 = d * (double)take;
        EconomyResponse economyResponse = this.plugin.eco.depositPlayer((OfflinePlayer)player, d2);
        if (!economyResponse.transactionSuccess()) {
            this.give(player, itemStack, take);
            Util.err(player, "\u5165\u8d26\u5931\u8d25,\u7269\u54c1\u5df2\u9000\u56de:" + economyResponse.errorMessage);
            return;
        }
        if (take < n2) {
            Util.msg(player, "\u5b9e\u9645\u56de\u6536 " + take + " \u4e2a(\u80cc\u5305\u91cc\u53ea\u6709\u8fd9\u4e48\u591a),\u83b7\u5f97 \u00a5" + Util.money(d2), NamedTextColor.YELLOW);
        } else {
            Util.ok(player, "\u5df2\u56de\u6536 " + Util.plainName(shopItem.item()) + " \u00d7" + take + ",\u83b7\u5f97 \u00a5" + Util.money(d2));
        }
    }

    public void openSearch(Player player, String string) {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            List<Db.ShopItem> list;
            try {
                list = this.plugin.db.listShopItems();
            }
            catch (Exception exception) {
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u8bfb\u53d6\u5546\u5e97\u6570\u636e\u5931\u8d25"));
                return;
            }
            String string2 = string.toLowerCase();
            ArrayList<Db.ShopItem> arrayList = new ArrayList<Db.ShopItem>();
            for (Db.ShopItem shopItem : list) {
                String string3 = Util.plainName(shopItem.item());
                String string4 = shopItem.item().getType().getKey().value();
                if (!string3.toLowerCase().contains(string2) && !string4.toLowerCase().contains(string2)) continue;
                arrayList.add(shopItem);
            }
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.showSearch(player, string, arrayList));
        });
    }

    private void showSearch(Player player, String string, List<Db.ShopItem> list) {
        if (!player.isOnline()) {
            return;
        }
        if (list.isEmpty()) {
            Util.msg(player, "\u6ca1\u627e\u5230\u548c \"" + string + "\" \u76f8\u5173\u7684\u5546\u54c1(\u53ef\u8bd5\u4e2d\u6587\u540d\u6216\u82f1\u6587\u540d\u5982 stone)", NamedTextColor.YELLOW);
            return;
        }
        ArrayList<ActionButton> arrayList = new ArrayList<ActionButton>();
        for (Db.ShopItem shopItem2 : list) {
            arrayList.add(ActionButton.builder((Component)Util.name(shopItem2.item(), NamedTextColor.WHITE)).tooltip((Component)Component.text((String)("#" + shopItem2.id() + " \u300c" + shopItem2.category() + "\u300d\u4e70 \u00a5" + Util.money(shopItem2.buy()) + " / \u56de\u6536 \u00a5" + Util.money(shopItem2.sell()) + " \u2014 \u70b9\u51fb\u67e5\u770b"), (TextColor)NamedTextColor.GRAY)).action(Util.click(this.plugin, () -> this.openItem(player, shopItem2, shopItem2.category(), list))).build());
        }
        Dialog dialog = Dialog.create(registryBuilderFactory -> {
            DialogRegistryEntry.Builder builder = ((DialogRegistryEntry.Builder)registryBuilderFactory.empty()).base(DialogBase.builder((Component)Component.text((String)("\u641c\u7d22 \"" + string + "\" \u2014 " + list.size() + " \u4ef6\u7ed3\u679c"))).canCloseWithEscape(true).build()).type((DialogType)DialogType.multiAction((List)arrayList, null, (int)2));
        });
        player.showDialog((DialogLike)dialog);
    }

    public void openSell(Player player, int n) {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            List<Db.ShopItem> list;
            try {
                list = this.plugin.db.listShopItems();
            }
            catch (Exception exception) {
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u8bfb\u53d6\u5546\u5e97\u6570\u636e\u5931\u8d25"));
                return;
            }
            ArrayList<Db.ShopItem> arrayList = new ArrayList<Db.ShopItem>();
            for (Db.ShopItem shopItem : list) {
                if (!(shopItem.sell() > 0.0)) continue;
                arrayList.add(shopItem);
            }
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.showSell(player, n, arrayList));
        });
    }

    private void showSell(Player player, int n, List<Db.ShopItem> list) {
        if (!player.isOnline()) {
            return;
        }
        if (list.isEmpty()) {
            Util.msg(player, "\u5546\u5e97\u91cc\u8fd8\u6ca1\u6709\u53ef\u56de\u6536\u7684\u5546\u54c1", NamedTextColor.YELLOW);
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
        PlayerInventory playerInventory = player.getInventory();
        ArrayList<ActionButton> arrayList = new ArrayList<ActionButton>();
        int n5 = n4 * n2;
        while (n5 < Math.min(n4 * n2 + n2, list.size())) {
            Db.ShopItem shopItem = list.get(n5);
            ItemStack itemStack = shopItem.item().clone();
            itemStack.setAmount(1);
            int n6 = 0;
            int n7 = 0;
            while (n7 < 36) {
                ItemStack itemStack2 = playerInventory.getItem(n7);
                if (itemStack2 != null && itemStack2.isSimilar(itemStack)) {
                    n6 += itemStack2.getAmount();
                }
                ++n7;
            }
            arrayList.add(ActionButton.builder((Component)Util.name(shopItem.item(), NamedTextColor.GREEN)).tooltip((Component)Component.text((String)("\u56de\u6536 \u00a5" + Util.money(shopItem.sell()) + "/\u4e2a \u00b7 \u80cc\u5305\u6709 " + n6 + " \u4e2a \u2014 \u70b9\u51fb\u5168\u90e8\u5356\u51fa"), (TextColor)NamedTextColor.GRAY)).action(Util.click(this.plugin, () -> this.recycleAll(player, shopItem, n4))).build());
            ++n5;
        }
        if (n4 > 0) {
            arrayList.add(ActionButton.builder((Component)Component.text((String)"\u00ab \u4e0a\u4e00\u9875")).action(Util.click(this.plugin, () -> this.openSell(player, n4 - 1))).build());
        }
        if (n4 < n3 - 1) {
            arrayList.add(ActionButton.builder((Component)Component.text((String)"\u4e0b\u4e00\u9875 \u00bb")).action(Util.click(this.plugin, () -> this.openSell(player, n4 + 1))).build());
        }
        ActionButton actionButton = ActionButton.builder((Component)Component.text((String)"\u00ab \u8fd4\u56de\u5546\u5e97")).action(Util.click(this.plugin, () -> this.openMain(player))).build();
        Dialog dialog = Dialog.create(registryBuilderFactory -> {
            DialogRegistryEntry.Builder builder = ((DialogRegistryEntry.Builder)registryBuilderFactory.empty()).base(DialogBase.builder((Component)Component.text((String)("\u267b \u56de\u6536\u7ad9 " + (n4 + 1) + "/" + n3 + " \u9875(\u53ef\u56de\u6536 " + list.size() + " \u79cd)"))).canCloseWithEscape(true).build()).type((DialogType)DialogType.multiAction((List)arrayList, (ActionButton)actionButton, (int)2));
        });
        player.showDialog((DialogLike)dialog);
    }

    private void recycleAll(Player player, Db.ShopItem shopItem, int n) {
        if (this.plugin.eco == null) {
            Util.err(player, "\u7ecf\u6d4e\u7cfb\u7edf\u4e0d\u53ef\u7528");
            return;
        }
        if (shopItem.sell() <= 0.0) {
            Util.err(player, "\u8be5\u7269\u54c1\u4e0d\u56de\u6536");
            return;
        }
        ItemStack itemStack = shopItem.item().clone();
        itemStack.setAmount(1);
        PlayerInventory playerInventory = player.getInventory();
        int n2 = 0;
        int n3 = 0;
        while (n3 < 36) {
            ItemStack itemStack2 = playerInventory.getItem(n3);
            if (itemStack2 != null && itemStack2.isSimilar(itemStack)) {
                n2 += itemStack2.getAmount();
            }
            ++n3;
        }
        if (n2 <= 0) {
            Util.err(player, "\u80cc\u5305\u91cc\u6ca1\u6709\u53ef\u56de\u6536\u7684 " + Util.plainName(shopItem.item()));
            this.openSell(player, n);
            return;
        }
        n3 = n2;
        int n4 = 0;
        while (n4 < 36 && n3 > 0) {
            ItemStack itemStack3 = playerInventory.getItem(n4);
            if (itemStack3 != null && itemStack3.isSimilar(itemStack)) {
                n3 -= itemStack3.getAmount();
                playerInventory.setItem(n4, null);
            }
            ++n4;
        }
        double d = shopItem.sell() * (double)n2;
        EconomyResponse economyResponse = this.plugin.eco.depositPlayer((OfflinePlayer)player, d);
        if (!economyResponse.transactionSuccess()) {
            this.give(player, itemStack, n2);
            Util.err(player, "\u5165\u8d26\u5931\u8d25,\u7269\u54c1\u5df2\u9000\u56de:" + economyResponse.errorMessage);
            return;
        }
        Util.ok(player, "\u56de\u6536 " + Util.plainName(shopItem.item()) + " \u00d7" + n2 + ",\u83b7\u5f97 \u00a5" + Util.money(d));
        this.openSell(player, n);
    }

    private /* synthetic */ boolean lambda$11(String string, String string2, Db.ShopItem shopItem) {
        return string.equals(shopItem.category()) && this.matches(shopItem, string2);
    }

    private static /* synthetic */ void lambda$19(String string, List list, int n, int n2, List list2, ActionButton actionButton, RegistryBuilderFactory registryBuilderFactory) {
        DialogRegistryEntry.Builder builder = ((DialogRegistryEntry.Builder)registryBuilderFactory.empty()).base(DialogBase.builder((Component)Component.text((String)("\u68ee\u5c7f\u5546\u5e97 - " + string + "(" + list.size() + " \u79cd \u00b7 \u7b2c " + (n + 1) + "/" + n2 + " \u9875)"))).canCloseWithEscape(true).build()).type((DialogType)DialogType.multiAction((List)list2, (ActionButton)actionButton, (int)2));
    }

    private static /* synthetic */ void lambda$22(String string, List list, List list2, RegistryBuilderFactory registryBuilderFactory) {
        DialogRegistryEntry.Builder builder = ((DialogRegistryEntry.Builder)registryBuilderFactory.empty()).base(DialogBase.builder((Component)Component.text((String)string)).inputs(list).canCloseWithEscape(true).build()).type((DialogType)DialogType.multiAction((List)list2, null, (int)2));
    }
}

