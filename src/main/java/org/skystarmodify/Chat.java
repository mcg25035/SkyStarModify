package org.skystarmodify;

import net.md_5.bungee.api.chat.*;
import org.bukkit.inventory.ItemStack;
import org.itemutils.ItemUtils;

public class Chat {
    public static BaseComponent[] createAsItemTag(ItemStack item){
        TranslatableComponent component = new TranslatableComponent();
        component.setTranslate(ItemUtils.getDescriptionID(item));

        BaseComponent[] hoverEventCompoents = new BaseComponent[]{
                new TextComponent(ItemUtils.ItemToData(item))
        };

        ComponentBuilder builder = new ComponentBuilder();
        return builder.append(component).event(new HoverEvent(HoverEvent.Action.SHOW_ITEM,hoverEventCompoents))
                .append("").event((HoverEvent)null).create();

    }
}
