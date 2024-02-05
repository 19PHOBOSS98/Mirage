package net.phoboss.mirage.utility;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;

public interface BookSettingsUtility {
    static ListTag readPages(ItemStack bookStack){
        if (!bookStack.isEmpty() && bookStack.hasTag()) {
            CompoundTag bookNbt = bookStack.getTag();
            if(bookNbt.contains("pages")) {
                return bookNbt.getList("pages", 8).copy();
            }
        }
        return new ListTag();
    }
    static JsonObject parsePages(ListTag pagesNbt) throws Exception{
        if(pagesNbt.size()<1){
            throw new Exception("The book is empty: ");
        }
        String pagesStr = "{";
        for(int i=0; i<pagesNbt.size(); ++i) {
            pagesStr = pagesStr + pagesNbt.getString(i);
        }
        pagesStr = pagesStr + "}";

        try {
            return JsonParser.parseString(pagesStr).getAsJsonObject();
        }catch (Exception e){
            throw new Exception("Might need to recheck your book: "+e.getLocalizedMessage(),e);
        }
    }



    static JsonObject createNewBook(JsonObject settingsJSON, Book book) throws Exception{
        JsonObject bookJSON = new Gson().toJsonTree(book).getAsJsonObject();

        for (Map.Entry<String, JsonElement> setting : settingsJSON.entrySet()) {
            String settingName = setting.getKey();
            if(!bookJSON.has(settingName)) {
                throw new Exception("Unrecognized Setting: " + settingName);
            }
            JsonElement oldValue = bookJSON.get(settingName);
            JsonElement newValue = setting.getValue();
            if(oldValue.getClass() != newValue.getClass()){
                throw new Exception("Invalid Entry: " + settingName + ":" + newValue);
            }
            bookJSON.add(settingName,newValue);
        }
        return bookJSON;
    }

    default void executeBookProtocol(ItemStack bookStack,
                                             BlockEntity blockEntity,
                                             Book bookSettings,boolean override) throws Exception{
        ListTag pagesNbt;
        try {
            pagesNbt = readPages(bookStack);
        }catch(Exception e){
            //ErrorResponse.onError(world,pos,player,"can't find pages...");
            throw new Exception("can't find pages...",e);
        }

        if(pagesNbt.isEmpty()){
            throw new Exception("The book is empty...");
        }

        try {
            JsonObject newSettings = parsePages(pagesNbt);
            customJSONParsingValidation(newSettings,override);
            implementBookSettings(blockEntity,createNewBook(newSettings,bookSettings),override);
        }catch(Exception e){
            throw new Exception(e.getMessage(),e);
        }
    }

    default void customJSONParsingValidation(JsonObject settingsJSON,boolean overide) throws Exception{

    }
    default void implementBookSettings(BlockEntity blockEntity, JsonObject newSettings,boolean override) throws Exception{
    }

}
