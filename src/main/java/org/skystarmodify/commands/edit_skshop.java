package org.skystarmodify.commands;

import com.earth2me.essentials.Essentials;
import com.google.gson.JsonObject;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.FloatArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.skystarmodify.*;
import net.md_5.bungee.api.chat.*;
import org.skystarmodify.exceptions.PriceException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;

public class edit_skshop {
    private LangResource langRes;
    private File skshopData = Files.pluginFileConstruct("skshopData");
    public edit_skshop() throws FileSystemException{
        SkyStarModify ssm = (SkyStarModify) Bukkit.getServer().getPluginManager().getPlugin("SkyStarModify");
        this.langRes = ssm.langRes;

        if (!skshopData.isDirectory()){
            skshopData.delete();
            if (!skshopData.mkdir()){
                throw new FileSystemException(skshopData.toPath().toUri().toString());
            }
        }

        new CommandAPICommand("edit_skshop")
                .withPermission(CommandPermission.OP)
                .withSubcommand(new CommandAPICommand("buy")
                        .withArguments(new FloatArgument(langRes.lang.price))
                        .executes((sender,args) -> {
                            sender.sendMessage(langRes.lang.skShopHeader);
                            sender.sendMessage(langRes.lang.skShopActionType+langRes.lang.skShopUpstore);
                            ItemStack handItem = ((Player)sender).getInventory().getItemInMainHand();
                            Float price = (Float) args.get(0);
                            try{
                                if (price == null) throw new PriceException("Price must not be null");
                                SkShop.upstoreBuy(handItem, price);
                            }
                            catch (PriceException ignored) {
                                sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skShopSinglePriceError);
                                return;
                            }
                            catch (IOException e){
                                sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skUpstoreFsError);
                                return;
                            }

                            sender.sendMessage(langRes.lang.skShopTradeType+langRes.lang.buy);
                            sender.sendMessage(Component.text(langRes.lang.skShopItemName).append(Chat.createAsItemTag(handItem)));
                            sender.sendMessage(langRes.lang.skShopSinglePrice+price);

                        })
                )
                .withSubcommand(new CommandAPICommand("sell")
                        .withArguments(new FloatArgument(langRes.lang.price))
                        .executes((sender,args) -> {
                            sender.sendMessage(langRes.lang.skShopHeader);
                            sender.sendMessage(langRes.lang.skShopActionType+langRes.lang.skShopUpstore);
                            ItemStack handItem = ((Player)sender).getInventory().getItemInMainHand();
                            Float price = (Float) args.get(0);
                            try{
                                if (price == null) throw new PriceException("Price must not be null");
                                SkShop.upstoreSell(handItem, price);
                            }
                            catch (PriceException ignored) {
                                sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skShopSinglePriceError);
                                return;
                            }
                            catch (IOException e){
                                sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skUpstoreFsError);
                                return;
                            }

                            sender.sendMessage(langRes.lang.skShopTradeType+langRes.lang.sell);
                            sender.sendMessage(Component.text(langRes.lang.skShopItemName).append(Chat.createAsItemTag(handItem)));
                            sender.sendMessage(langRes.lang.skShopSinglePrice+price);
                        })
                )
                .register();
    }
}
