package org.skystarmodify;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.logging.Logger;

public class LangResource {

    public class dict {
        public String wrapName;
        public String price;


        public String warpAlreadyExist;
        public String warpMoneyNotEnough;
        public String warpCreateSuccessfully;
        public String warpUnknownError;


        public String error;
        public String skShopHeader;
        public String skShopDetails;
        public String skShopMessageType;
        public String skShopActionType;
        public String skShopSinglePrice;
        public String skShopSinglePriceError;
        public String skShopTotal;
        public String skShopUpstore;
        public String skUpstoreFsError;
        public String skShopItemName;
        public String skUpstoreSinglePriceError;
        public String skShopTradeType;
        public String buy;
        public String sell;
        public String item;
        public String amount;
        public String player;
        public String total;
        public String singlePrice;
        public String skShopItemDNEError;
        public String skShopItemFsError;
        public String skShopMoneyNotEnoughError;
        public String skShopAmount;
        public String skShopBal;
        public String skShopBackpackFullError;
        public String skShopDiscordTitle;
        public String skShopDiscordContent;
        public String skShopDiscordFooter;
        public String skShopTypeError;
        public String skShopItemNotEnoughError;
        public String skShopCmdArgError;
        public String skPlayerOnlineTimeSystemTitle;
        public String skYesterdayPlayersOnlineTimeData;
        public String skTeam;
        public String skTime;

    }
    public LangResource.dict lang = new LangResource.dict();

    public LangResource(JsonObject fromLangFile){
        Logger console = Bukkit.getLogger();
        Class<?> langClass = lang.getClass();

        for (Map.Entry<String, JsonElement> entry : fromLangFile.entrySet()){

            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (!value.isJsonPrimitive()){
                continue;
            }
            if (!value.getAsJsonPrimitive().isString()){
                continue;
            }

            try {
                LangResource.dict.class.getField(entry.getKey()).set(lang, value.getAsString());
            } catch (Exception ignored) {
            }
        }
    }
}
