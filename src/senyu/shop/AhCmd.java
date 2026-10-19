/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.dialog.Dialog
 *  io.papermc.paper.registry.data.dialog.ActionButton
 *  io.papermc.paper.registry.data.dialog.DialogBase
 *  io.papermc.paper.registry.data.dialog.DialogRegistryEntry$Builder
 *  io.papermc.paper.registry.data.dialog.body.DialogBody
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
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package senyu.shop;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
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
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import senyu.shop.Db;
import senyu.shop.SenyuShopPlugin;
import senyu.shop.Util;

public class AhCmd
implements CommandExecutor,
TabCompleter {
    private final SenyuShopPlugin plugin;

    public AhCmd(SenyuShopPlugin senyuShopPlugin) {
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
        if (stringArray.length > 0 && stringArray[0].equalsIgnoreCase("sell")) {
            if (stringArray.length < 2) {
                Util.err(player, "\u7528\u6cd5: /ah sell <\u5355\u4ef7>");
                return true;
            }
            try {
                this.listHand(player, Double.parseDouble(stringArray[1]));
            }
            catch (NumberFormatException numberFormatException) {
                Util.err(player, "\u4ef7\u683c\u5fc5\u987b\u662f\u6570\u5b57,\u4f8b\u5982 /ah sell 100");
            }
            return true;
        }
        if (stringArray.length > 0 && stringArray[0].equalsIgnoreCase("my")) {
            this.openMine(player);
            return true;
        }
        this.openBrowse(player, 0);
        return true;
    }

    public List<String> onTabComplete(CommandSender commandSender, Command command, String string, String[] stringArray) {
        if (stringArray.length == 1) {
            ArrayList<String> arrayList = new ArrayList<String>();
            if ("sell".startsWith(stringArray[0].toLowerCase())) {
                arrayList.add("sell");
            }
            if ("my".startsWith(stringArray[0].toLowerCase())) {
                arrayList.add("my");
            }
            return arrayList;
        }
        return List.of();
    }

    public void openBrowse(Player player, int n) {
        int n2 = Math.max(0, n);
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            List<Db.Listing> list;
            try {
                this.plugin.db.sweepExpired();
                list = this.plugin.db.openListings(this.plugin.pageSize + 1, n2 * this.plugin.pageSize);
            }
            catch (Exception exception) {
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u8bfb\u53d6\u62cd\u5356\u884c\u5931\u8d25"));
                return;
            }
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.showBrowse(player, n2, list));
        });
    }

    private void showBrowse(Player player, int n, List<Db.Listing> list) {
        if (!player.isOnline()) {
            return;
        }
        boolean bl = list.size() > this.plugin.pageSize;
        List<Db.Listing> list2 = list.subList(0, Math.min(list.size(), this.plugin.pageSize));
        if (list2.isEmpty() && n == 0) {
            Util.msg(player, "\u62cd\u5356\u884c\u662f\u7a7a\u7684,\u7528 /ah sell <\u4ef7\u683c> \u4e0a\u67b6\u624b\u4e0a\u7684\u7269\u54c1", NamedTextColor.YELLOW);
            return;
        }
        ArrayList<ActionButton> arrayList = new ArrayList<ActionButton>();
        for (Db.Listing listing2 : list2) {
            boolean bl2 = player.getUniqueId().toString().equals(listing2.seller());
            String string = (bl2 ? "\u4f60\u7684\u62cd\u5356 \u00b7 \u70b9\u51fb\u53d6\u56de" : "\u70b9\u51fb\u8d2d\u4e70") + " \u00b7 \u00a5" + Util.money(listing2.price()) + " \u00b7 \u5356\u5bb6 " + listing2.sellerName() + " \u00b7 \u5269\u4f59 " + Util.money(Math.max(0.0, (double)(listing2.expire() - System.currentTimeMillis()) / 3600000.0)) + " \u5c0f\u65f6";
            arrayList.add(ActionButton.builder((Component)Util.name(listing2.item(), NamedTextColor.WHITE)).tooltip((Component)Component.text((String)string, (TextColor)NamedTextColor.GRAY)).action(Util.click(this.plugin, () -> this.openDetail(player, listing2, n))).build());
        }
        if (n > 0) {
            int n2 = n - 1;
            arrayList.add(Util.button(this.plugin, "\u25c0 \u4e0a\u4e00\u9875", () -> this.openBrowse(player, n2)));
        }
        if (bl) {
            int n3 = n + 1;
            arrayList.add(Util.button(this.plugin, "\u4e0b\u4e00\u9875 \u25b6", () -> this.openBrowse(player, n3)));
        }
        arrayList.add(Util.button(this.plugin, "\u6211\u7684\u62cd\u5356", () -> this.openMine(player)));
        arrayList.add(Util.button(this.plugin, "去官方商店", () -> this.plugin.openShopFor(player)));
        Dialog dialog = Dialog.create(registryBuilderFactory -> {
            DialogRegistryEntry.Builder builder = ((DialogRegistryEntry.Builder)registryBuilderFactory.empty()).base(DialogBase.builder((Component)Component.text((String)("\u68ee\u5c7f\u62cd\u5356\u884c \u00b7 \u7b2c " + (n + 1) + " \u9875"))).canCloseWithEscape(true).build()).type((DialogType)DialogType.multiAction((List)arrayList, null, (int)2));
        });
        player.showDialog((DialogLike)dialog);
    }

    private void openDetail(Player player, Db.Listing listing, int n) {
        if (!player.isOnline()) {
            return;
        }
        boolean bl = player.getUniqueId().toString().equals(listing.seller());
        ArrayList<DialogBody> arrayList = new ArrayList<DialogBody>();
        arrayList.add(DialogBody.item((ItemStack)listing.item()).build());
        arrayList.add(DialogBody.plainMessage((Component)Component.text((String)("\u4ef7\u683c \u00a5" + Util.money(listing.price()) + " \u00b7 \u5356\u5bb6 " + listing.sellerName() + " \u00b7 \u5269\u4f59 " + Util.money(Math.max(0.0, (double)(listing.expire() - System.currentTimeMillis()) / 3600000.0)) + " \u5c0f\u65f6"), (TextColor)NamedTextColor.GOLD)));
        ArrayList<ActionButton> arrayList2 = new ArrayList<ActionButton>();
        if (bl) {
            arrayList2.add(Util.button(this.plugin, "\u53d6\u6d88\u62cd\u5356\u5e76\u53d6\u56de", () -> this.takeBack(player, listing, n)));
        } else {
            arrayList2.add(Util.button(this.plugin, "\u786e\u8ba4\u8d2d\u4e70(\u00a5" + Util.money(listing.price()) + ")", () -> this.confirmBuy(player, listing)));
        }
        int n2 = n;
        arrayList2.add(Util.button(this.plugin, "\u8fd4\u56de\u5217\u8868", () -> this.openBrowse(player, n2)));
        Dialog dialog = Dialog.create(registryBuilderFactory -> {
            DialogRegistryEntry.Builder builder = ((DialogRegistryEntry.Builder)registryBuilderFactory.empty()).base(DialogBase.builder((Component)Component.text((String)("\u62cd\u5356\u8be6\u60c5 - " + Util.plainName(listing.item())))).body(arrayList).canCloseWithEscape(true).build()).type((DialogType)DialogType.multiAction((List)arrayList2, null, (int)2));
        });
        player.showDialog((DialogLike)dialog);
    }

    private void confirmBuy(Player player, Db.Listing listing) {
        if (this.plugin.eco == null) {
            Util.err(player, "经济系统不可用");
            return;
        }
        double price = listing.price();
        if (this.plugin.eco.getBalance((OfflinePlayer)player) < price) {
            Util.err(player, "余额不足(需要 ¥" + Util.money(price) + ")");
            return;
        }
        EconomyResponse resp = this.plugin.eco.withdrawPlayer((OfflinePlayer)player, price);
        if (!resp.transactionSuccess()) {
            Util.err(player, "扣款失败:" + resp.errorMessage);
            return;
        }

        boolean sold;
        try {
            sold = this.plugin.db.markSold(listing.id(), player.getUniqueId().toString(), player.getName());
        }
        catch (Exception exception) {
            this.plugin.eco.depositPlayer((OfflinePlayer)player, price);
            Util.err(player, "购买失败(数据错误),已退款");
            return;
        }
        if (!sold) {
            this.plugin.eco.depositPlayer((OfflinePlayer)player, price);
            Util.err(player, "手慢了,这件拍卖已被别人买走或下架,已退款");
            return;
        }

        OfflinePlayer seller = Bukkit.getOfflinePlayer((UUID)UUID.fromString(listing.seller()));
        ItemStack sellingItem = listing.item().clone();
        int total = Math.max(1, sellingItem.getAmount());

        /* 先交付物品;没放进去的部分按比例退款,剩余物品退回卖家 */
        double deliveredPrice = price;
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(sellingItem);
        int delivered = total;
        for (ItemStack leftoverItem : leftover.values()) {
            delivered -= leftoverItem.getAmount();
        }
        if (delivered <= 0) {
            this.plugin.eco.depositPlayer((OfflinePlayer)player, price);
            this.returnRemainderToSeller(listing, sellingItem);
            try {
                this.plugin.db.cancelListing(listing.id(), listing.seller());
            }
            catch (Exception ignored) { }
            Util.err(player, "背包已满,已全额退款,物品退回卖家");
            return;
        }
        if (delivered < total) {
            deliveredPrice = Math.round(price * (double)delivered / (double)total * 100.0) / 100.0;
            double refund = Math.round((price - deliveredPrice) * 100.0) / 100.0;
            if (refund > 0.0) {
                this.plugin.eco.depositPlayer((OfflinePlayer)player, refund);
            }
            ItemStack restItem = sellingItem.clone();
            restItem.setAmount(total - delivered);
            this.returnRemainderToSeller(listing, restItem);
            Util.err(player, "背包空间不足:本次只交付 " + delivered + " 件,已退还 ¥" + Util.money(refund));
        }

        /* 卖家货款:按其成交时的余额档累进收税,税金直接回收出经济系统(销毁防通胀) */
        double sellerBalance = this.plugin.eco.getBalance(seller);
        int taxPct = this.plugin.taxPercentFor(sellerBalance);
        double tax = this.plugin.taxAmount(deliveredPrice, sellerBalance);
        double net = Math.round((deliveredPrice - tax) * 100.0) / 100.0;
        EconomyResponse pay = this.plugin.eco.depositPlayer(seller, net);
        if (!pay.transactionSuccess()) {
            this.plugin.eco.depositPlayer((OfflinePlayer)player, deliveredPrice);
            Util.err(player, "卖家收款异常,已全额退款;物品请你自行保管,请联系管理员处理");
            return;
        }

        Util.ok(player, "已购买 " + Util.plainName(listing.item()) + " ×" + delivered + ",花费 ¥" + Util.money(deliveredPrice));
        Player sellerPlayer = seller.getPlayer();
        if (sellerPlayer != null) {
            if (tax > 0.0) {
                Util.ok(sellerPlayer, "你的拍卖被 " + player.getName() + " 买走了:成交 ¥" + Util.money(deliveredPrice)
                    + ",按你当前余额档累进税 " + taxPct + "% 收 ¥" + Util.money(tax) + ",实收 ¥" + Util.money(net));
            } else {
                Util.ok(sellerPlayer, "你的拍卖被 " + player.getName() + " 买走了,货款 ¥" + Util.money(net) + " 已到账");
            }
        }
    }

    /* 把未能交付的部分退回卖家:在线退背包,离线则按原价重新上架 */
    private void returnRemainderToSeller(Db.Listing listing, ItemStack stack) {
        try {
            Player sellerOnline = Bukkit.getPlayer((UUID)UUID.fromString(listing.seller()));
            if (sellerOnline != null) {
                sellerOnline.getInventory().addItem(stack);
                Util.msg(sellerOnline, "买家背包放不下,有部分物品退回了你的背包", NamedTextColor.YELLOW);
            } else {
                long now = System.currentTimeMillis();
                this.plugin.db.addListing(listing.seller(), listing.sellerName(), stack, listing.price(), now, now + (long)this.plugin.expireDays * 86400000L);
            }
        }
        catch (Exception ignored) { }
    }

    private void takeBack(Player player, Db.Listing listing, int n) {
        boolean bl;
        try {
            bl = this.plugin.db.cancelListing(listing.id(), player.getUniqueId().toString());
        }
        catch (Exception exception) {
            bl = false;
        }
        if (!bl) {
            Util.err(player, "\u53d6\u56de\u5931\u8d25(\u53ef\u80fd\u5df2\u552e\u51fa\u6216\u5df2\u8fc7\u671f)");
            this.openMine(player);
            return;
        }
        player.getInventory().addItem(new ItemStack[]{listing.item().clone()});
        Util.ok(player, "\u5df2\u53d6\u6d88\u62cd\u5356," + Util.plainName(listing.item()) + " \u5df2\u9000\u56de\u80cc\u5305");
        this.openMine(player);
    }

    private void openMine(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            List<Db.Listing> list;
            try {
                list = this.plugin.db.myListings(player.getUniqueId().toString());
            }
            catch (Exception exception) {
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> Util.err(player, "\u8bfb\u53d6\u5931\u8d25"));
                return;
            }
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }
                if (list.isEmpty()) {
                    Util.msg(player, "\u4f60\u8fd8\u6ca1\u6709\u62cd\u5356\u8bb0\u5f55,\u7528 /ah sell <\u4ef7\u683c> \u4e0a\u67b6\u624b\u4e0a\u7684\u7269\u54c1", NamedTextColor.YELLOW);
                    return;
                }
                ArrayList<ActionButton> arrayList = new ArrayList<ActionButton>();
                for (Db.Listing listing2 : list) {
                    Object object = "open".equals(listing2.status()) ? "\u5728\u552e\u4e2d \u00b7 \u70b9\u51fb\u53d6\u6d88\u5e76\u53d6\u56de" : "\u5df2\u552e\u51fa \u00b7 \u4e70\u5bb6 " + (listing2.buyerName() == null ? "?" : listing2.buyerName()) + " \u00b7 \u8d27\u6b3e\u5df2\u6309\u6863\u4f4d\u7a0e\u540e\u5230\u8d26";
                    arrayList.add(ActionButton.builder((Component)Util.name(listing2.item(), NamedTextColor.WHITE)).tooltip((Component)Component.text((String)object, (TextColor)NamedTextColor.GRAY)).action(Util.click(this.plugin, () -> {
                        if ("open".equals(listing2.status())) {
                            this.takeBack(player, listing2, 0);
                        } else {
                            Util.err(player, "\u8be5\u62cd\u5356\u5df2\u552e\u51fa,\u8d27\u6b3e\u5df2\u6309\u6863\u4f4d\u7a0e\u540e\u5230\u8d26");
                        }
                    })).build());
                }
                arrayList.add(Util.button(this.plugin, "\u8fd4\u56de\u62cd\u5356\u884c", () -> this.openBrowse(player, 0)));
                Dialog dialog = Dialog.create(registryBuilderFactory -> {
                    DialogRegistryEntry.Builder builder = ((DialogRegistryEntry.Builder)registryBuilderFactory.empty()).base(DialogBase.builder((Component)Component.text((String)("\u6211\u7684\u62cd\u5356(" + list.size() + ")"))).canCloseWithEscape(true).build()).type((DialogType)DialogType.multiAction((List)arrayList, null, (int)2));
                });
                player.showDialog((DialogLike)dialog);
            });
        });
    }

    private void listHand(Player player, double d) {
        if (this.plugin.eco == null) {
            Util.err(player, "\u7ecf\u6d4e\u7cfb\u7edf\u4e0d\u53ef\u7528");
            return;
        }
        if (d <= 0.0) {
            Util.err(player, "\u4ef7\u683c\u5fc5\u987b\u5927\u4e8e 0");
            return;
        }
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.isEmpty() || itemStack.getType().isAir()) {
            Util.err(player, "\u624b\u4e0a\u6ca1\u6709\u7269\u54c1,\u628a\u8981\u62cd\u5356\u7684\u4e1c\u897f\u62ff\u5728\u624b\u91cc\u518d\u8f93 /ah sell <\u4ef7\u683c>");
            return;
        }
        long l = System.currentTimeMillis();
        long l2 = l + (long)this.plugin.expireDays * 86400000L;
        try {
            int n = this.plugin.db.myOpenCount(player.getUniqueId().toString());
            if (n >= this.plugin.maxListings) {
                Util.err(player, "\u4f60\u7684\u5728\u552e\u62cd\u5356\u5df2\u8fbe\u4e0a\u9650(" + this.plugin.maxListings + " \u4ef6),\u5148\u53bb /ah my \u53d6\u56de\u4e00\u4e9b\u5427");
                return;
            }
            this.plugin.db.addListing(player.getUniqueId().toString(), player.getName(), itemStack, d, l, l2);
        }
        catch (Exception exception) {
            Util.err(player, "\u4e0a\u67b6\u5931\u8d25:" + exception.getMessage());
            return;
        }
        player.getInventory().setItemInMainHand(null);
        Util.ok(player, "\u5df2\u4e0a\u67b6 " + Util.plainName(itemStack) + " \u00d71,\u5355\u4ef7 \u00a5" + Util.money(d) + "," + this.plugin.expireDays + " \u5929\u5185\u6709\u6548\u3002\u53ef\u5728 /ah my \u53d6\u56de");
        if (this.plugin.eco != null) {
            int pct = this.plugin.taxPercentFor(this.plugin.eco.getBalance((OfflinePlayer)player));
            if (pct > 0) {
                Util.msg(player, "提示:成交时按你当时的余额档累进税 " + pct + "%,税金回收出服务器(防通胀)", NamedTextColor.GRAY);
            }
        }
    }
}

