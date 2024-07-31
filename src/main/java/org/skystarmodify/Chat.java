package org.skystarmodify;

import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.function.UnaryOperator;

public class Chat {
    public static Component createAsItemTag(ItemStack item){
        item = item.clone();
        item.setAmount(1);
        String translationKey = item.translationKey();

        HoverEvent<HoverEvent.ShowItem> onHover = Bukkit.getServer().getItemFactory().asHoverEvent(
                item,
                UnaryOperator.identity()
        );

        Component text;
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            text = item.getItemMeta().displayName();
        } else {
            text = Component.translatable(translationKey);
        }

        assert text != null;
        return Component.text()
                .append(MiniMessage.miniMessage().deserialize("<gray>[</gray>"))
                .append(text)
                .append(MiniMessage.miniMessage().deserialize("<gray>]</gray>"))
                .hoverEvent(onHover)
                .build();
    }
}
