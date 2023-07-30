package org.skystarmodify.commands;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Warps;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.skystarmodify.LangResource;
import org.skystarmodify.SkyStarModify;

import java.math.BigDecimal;

public class buywarp {
    private LangResource lang;
    public buywarp(){
        SkyStarModify ssm = (SkyStarModify) Bukkit.getServer().getPluginManager().getPlugin("SkyStarModify");
        this.lang = ssm.langRes;

        Essentials ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
        Warps warps = ess.getWarps();
        String cmdName = this.getClass().getName();
        new CommandAPICommand("buywarp")
                .withArguments(new GreedyStringArgument(lang.lang.wrapName))
                .withPermission(CommandPermission.NONE)
                .executes((sender, args) -> {
                    String warpName = (String)args.get(0);
                    if (warps.isWarp(warpName)){
                        sender.sendMessage(lang.lang.warpAlreadyExist);
                        return;
                    }
                    User essSender = ess.getUser((Player)sender);
                    BigDecimal playerMoney = essSender.getMoney();
                    BigDecimal warpPrice = BigDecimal.valueOf((int)(ssm.config.get("warpPrice")));
                    if (playerMoney.compareTo(warpPrice) < 0){
                        sender.sendMessage(lang.lang.warpMoneyNotEnough);
                        return;
                    }
                    try {
                        if (warpName.contains(" ")){
                            throw new Exception("awa");
                        }
                        warps.setWarp(warpName,((Player)sender).getLocation());
                        ess.getUser((Player)sender).setMoney(playerMoney.subtract(warpPrice));
                        sender.sendMessage(lang.lang.warpCreateSuccessfully.replaceAll("%warpName%",warpName));
                    }
                    catch (MaxMoneyException ignored) {}
                    catch (Exception e) {
                        sender.sendMessage(lang.lang.warpUnknownError);
                    }
                })
                .register();
    }
}
