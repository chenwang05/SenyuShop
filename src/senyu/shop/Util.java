/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.dialog.DialogResponseView
 *  io.papermc.paper.registry.data.dialog.ActionButton
 *  io.papermc.paper.registry.data.dialog.action.DialogAction
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.event.ClickCallback$Options
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package senyu.shop;

import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import senyu.shop.SenyuShopPlugin;

public final class Util {
    private Util() {
    }

    public static String money(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d)) {
            return String.valueOf((long)d);
        }
        return String.format("%.2f", d);
    }

    public static String plainName(ItemStack itemStack) {
        return PlainTextComponentSerializer.plainText().serialize(itemStack.effectiveName());
    }

    public static Component name(ItemStack itemStack, NamedTextColor namedTextColor) {
        Component component = itemStack.effectiveName();
        if (component.color() == null) {
            component = component.color((TextColor)namedTextColor);
        }
        return component;
    }

    public static DialogAction click(SenyuShopPlugin senyuShopPlugin, Runnable runnable) {
        ClickCallback.Options options = (ClickCallback.Options)ClickCallback.Options.builder().build();
        return DialogAction.customClick((dialogResponseView, audience) -> {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTask((Plugin)senyuShopPlugin, runnable);
        }, (ClickCallback.Options)options);
    }

    public static ActionButton button(SenyuShopPlugin senyuShopPlugin, String string, Runnable runnable) {
        return ActionButton.builder((Component)Component.text((String)string)).action(Util.click(senyuShopPlugin, runnable)).build();
    }

    public static void msg(Player player, String string, NamedTextColor namedTextColor) {
        if (player.isOnline()) {
            player.sendMessage((Component)Component.text((String)string, (TextColor)namedTextColor));
        }
    }

    public static void err(Player player, String string) {
        Util.msg(player, string, NamedTextColor.RED);
    }

    public static void ok(Player player, String string) {
        Util.msg(player, string, NamedTextColor.GREEN);
    }

    public static DialogAction clickInput(SenyuShopPlugin senyuShopPlugin, Consumer<DialogResponseView> consumer) {
        ClickCallback.Options options = (ClickCallback.Options)ClickCallback.Options.builder().build();
        return DialogAction.customClick((dialogResponseView, audience) -> {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTask((Plugin)senyuShopPlugin, () -> consumer.accept(dialogResponseView));
        }, (ClickCallback.Options)options);
    }

    public static ActionButton inputButton(SenyuShopPlugin senyuShopPlugin, String string, Consumer<DialogResponseView> consumer) {
        return ActionButton.builder((Component)Component.text((String)string)).action(Util.clickInput(senyuShopPlugin, consumer)).build();
    }
}

