

package com.siliconstudios.dynamicshopguiplus.files;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.files.lang.TranslatableComponent;
import com.siliconstudios.dynamicshopguiplus.files.lang.TranslatableRaw;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import com.siliconstudios.dynamicshopguiplus.util.ConfigUtil;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ConfigLoadException;
import com.siliconstudios.dynamicshopguiplus.util.meta.BukkitMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public enum Lang {
    UPDATING_SHOP_SETTINGS("Updating Shop settings..."),
    CREATING_LATEST_SHOPS("Creating default shops for the latest version because there was no existing layout found matching server version %version%, please use a supported server version."),
    DEFAULT_SHOPS_CREATED("Default shops layout compatible with %version% created at %path%"),
    LOADED_SHOP_CONFIGURATIONS("Completed loading %total% shop configs from /shops/"),
    DEFAULT_SECTIONS_CREATED("Default sections layout created at %path%"),
    LOADED_SECTION_CONFIGURATIONS("Completed loading %total% section configs from /sections/"),
    CREATING_NEW_SHOP_CONFIG("Creating a new shops config for '%section%' because no shops config was found while there is a sections config. Not removed correctly?"),
    CREATING_NEW_SECTION_CONFIG("Creating a new sections config for '%section%' because no sections config was found while there is a shops config. Not removed correctly?"),
    DISABLED_DUE_NO_VAULT("Disabled due to no Vault dependency found, please install Vault."),
    DISABLED_DUE_NO_ECONOMY("Disabled due to no economy plugin was found, please install a plugin such as EssentialsX!"),
    DISCOUNTS_FEATURE_UNAVAILABLE("Discounts feature cannot be enabled because no permissions plugin was found."),
    UPDATE_AVAILABLE("There is an update available for DynamicShopGUIPlus, you are running %plugin_version% but found %latest_version%."),
    DONE("Done - Took %millis%ms to complete"),
    UPDATING_SECTIONS_CONFIG("Updating sections config..."),
    DEBUG_MODE_ENABLED("Debug mode is enabled."),
    ECONOMY_PROVIDER_NULL("EconomyProvider is null."),
    LANGUAGE_FILE_USING("Using %languagefile% as language file."),
    CANNOT_FIND_LANGUAGE_FILE("Can not find language file!"),
    MINECRAFT_VERSION_USING("Using minecraft version %version%..."),
    LOADING_ITEMS("Loading all items..."),
    COULD_NOT_FIND_VALID_VERSION("Could not find a valid implementation for this server version. This will cause the plugin to not have a version to use and result in lots of items/methods not working. Please use a supported minecraft version."),
    UPDATING_CONFIGS("Updating the configurations because a newer version has been found..."),
    CREATING_BACKUP("Creating a backup from the old %fileName%, the file can be found in the backups folder inside the DynamicShopGUIPlus plugin's folder."),
    ENABLED_PLUGIN_HOOK("Enabled %plugin% hook..."),
    FAILED_PLUGIN_INTEGRATION("Failed to hook into %plugin%, plugin is disabled or not found..."),
    LOADING_SEASON_MODIFIERS("Loading season modifiers from config..."),
    LOADED_SEASON_MODIFIERS("Completed loading %total% modifier(s) for season '%season%' in world '%world%'"),
    NO_SEASON_MODIFIERS_FOUND("No season modifiers found for season %season% in config"),
    SEASON_CHANGE("Detected season change in world '%world%', loading modifiers for season %season%..."),
    ECONOMY_PROVIDERS_LOADED("Completed loading %total% economy provider(s) for all %shops% shop sections."),
    SECTION_ECONOMY_PROVIDER_LOADED("Using '%economy%' as default economy for shop '%shop%'"),
    LOADED_SHOP_STAND_MODULE("Completed loading %count% shop stand(s) from stands.json"),
    SAVING_SHOP_STANDS("Saving %count% shop stands to 'stands.json'..."),
    LOADING_LANGUAGE_FILES("Loading language files from LanguageFiles folder..."),
    FAILED_TO_LOAD_LANGUAGE_FILE("Failed to load language file '%fileName%', skipping..."),
    FAILED_TO_GET_LOCALE("Failed to get language for file name '%fileName%', skipping..."),
    LOADED_LANGUAGE_FILES("Successfully loaded %amount% language files from /LanguageFiles/ folder"),
    FAILED_TO_LOAD_LANGUAGE_FILES("Failed to load per player languages because LanguageFiles folder cannot be found"),
    ERROR_LOADING_BEDROCK_ICONS("Failed to load bedrock item icons with reason: %reason%"),
    CANNOT_GET_GEYSER_PALETTE("Could not get the latest Geyser item palette, icons for items in bedrock forms will not load!"),
    SUCCESSFULLY_LOADED_BEDROCK_ICONS("Successfully loaded %amount% item icon ids for bedrock forms"),
    LOADING_ITEM_IDS_FROM_GEYSER("Loading bedrock item ids from installed Geyser instance(%instanceVersion%)..."),
    ERROR_LOADING_ENCHANTMENT_COSTS("Failed to load enchantment variations because the enchant-costs.yml config cannot be read"),
    ENCHANTMENT_COSTS_LOADED("Successfully loaded %amount% enchantment costs from enchant-costs.yml"),
    SHOP_PREFIX("&9&lShop &8&l>> "),
    CONSOLE_PREFIX("&8[&3DynamicShopGUIPlus&8]&r"),
    INVENTORY_MAIN_SHOP_TITLE("&8&lServer Shop"),
    PROFILE("&aProfile"),
    NAME("&6Name"),
    MONEY("&6Money"),
    LEVEL("&6Level"),
    INVENTORY_SELLGUI_TITLE("&8&lSellGUI"),
    INVENTORY_BUYSTACKS_TITLE("&8Choose &lamount of stacks"),
    INVENTORY_HOWMUCHSELL_TITLE("&8Choose &lsell amount"),
    INVENTORY_HOWMUCHBUY_TITLE("&8Choose &lbuy amount"),
    PLUS_ONE("&b+1"),
    MIN_ONE("&5-1"),
    PLUS_SIXTEEN("&b+16"),
    MIN_SIXTEEN("&5-16"),
    PLUS_THIRTY_TWO("&b+32"),
    MIN_THIRTY_TWO("&5-32"),
    SELL("&aClick to sell"),
    SELL_ALL("&cClick to sell all"),
    BUY("&aClick to purchase"),
    BUY_STACKS("&cClick to buy stacks"),
    MAX_AMOUNT_REACHED("&cMax amount reached"),
    MIN_AMOUNT_REACHED("&cMin amount reached"),
    SELECTED_AMOUNT("&aAmount"),
    PREVIOUS_PAGE("&dPrevious Page"),
    NEXT_PAGE("&bNext Page"),
    CURRENT_PAGE("&7Page: %page%/%pages%"),
    SHOPSTAND_BUY_SCREEN_TITLE("&8Buying &a&l> &r&8%item%"),
    SHOPSTAND_SELL_SCREEN_TITLE("&8Selling &c&l> &r&8%item%"),
    SHOPSTAND_CLICK_TO_BUY("&aClick to buy %amount%"),
    SHOPSTAND_CLICK_TO_SELL("&aClick to sell %amount%"),
    SHOPSTAND_TOTAL_PRICE("&cTotal price: &7%price%"),
    DEFAULT_SHOP_STAND_ITEM_NAME("&eShopStand"),
    DEFAULT_SHOP_STAND_ITEM_LORE("&fPlace this item down to create\n&fa new &6ShopStand &fat the placed location\n&ffor shop item &c%item%"),
    SHOP_STANDS_MANAGEMENT_INVENTORY_TITLE("&8&lStand management"),
    SHOP_STAND_INFO_ITEM_NAME("&6&lShop stand info:"),
    SHOP_STAND_INFO_ITEM_LORE("&aStand ID: &f%id%\n&aLocation: &f%location%\n&aShop item: &f%item%\n&aStand type: &f%type%\n&aIs loaded: &f%loaded%"),
    FORCE_RELOAD_STAND_ITEM_NAME("&e&lForce reload stand"),
    FORCE_RELOAD_STAND_ITEM_LORE("&fClicking this will reload the shop stand\n&fwhich will re-create the displayitem and stand."),
    DESTROY_STAND_ITEM_NAME("&4&lDestroy shop stand"),
    DESTROY_STAND_ITEM_LORE("&eClick to destroy this shop stand"),
    RELOADED_SHOP_STAND("&aForcefully reloaded shop stand with ID %id%"),
    SHOP_STAND_BROWSE_INVENTORY_TITLE("&8&lStand list"),
    SHOP_STAND_LOCATION_FORMAT("World '%world%', x%pos_x%, y%pos_y%, z%pos_z%"),
    CREATED_NEW_SHOP_STAND("&aSuccessfully created new ShopStand for &f%item%"),
    ERROR_CREATING_NEW_SHOP_STAND("&cFailed to create a new shop stand: %reason%"),
    CANCEL("&4Cancel"),
    BACK("&4Back"),
    LEFT_CLICK_BUY("\n &a&lBuy Price\n &a\u25a0 &f%buyPrice%"),
    SEASONAL_BUY_PRICE("\n &a&lBuy Price\n &a\u25a0 &f&m%buyPrice%&r &f%seasonalBuyPrice%"),
    DISCOUNTED_BUY_PRICE("\n &a&lBuy Price\n &a\u25a0 &f&m%buyPrice%&r &f%discountedPrice%"),
    RIGHT_CLICK_SELL("\n &c&lSell Price\n &c\u25a0 &f%sellPrice%"),
    SEASONAL_SELL_PRICE("\n &c&lSell Price\n &c\u25a0 &f&m%sellPrice%&r &f%seasonalSellPrice%"),
    MULTIPLIED_SELL_PRICE("\n &c&lSell Price\n &c\u25a0 &f&m%sellPrice%&r &f%multipliedPrice%"),
    MIDDLE_CLICK_SELL_ALL("&bMiddle Click: Sell All"),
    SHIFT_RIGHT_CLICK_SELL_ALL("&bShift + Right Click: Sell All"),
    ITEM_LOCATION_IN_SHOP("&8Section: %shopsection%, Index: %itemLoc%"),
    ITEM_CANNOT_BE_BOUGHT("\n &a&lBuy Price\n &a\u25a0 &fCannot be bought"),
    ITEM_CANNOT_BE_SOLD("\n &c&lSell Price\n &c\u25a0 &fCannot be sold"),
    ITEM_SEASONAL_PRICE("&e&lSeasonal price %season-icon%"),
    DECORATION_LORE("\n &e&lUsage:"),
    DECORATION_LORE_BUY("&e \u255a&e&l\u00bb &fLeft click to buy"),
    DECORATION_LORE_SELL_ALL("&e \u255a&e&l\u00bb &fShift + Right Click to sell all"),
    DECORATION_LORE_SELL("&e \u255a&e&l\u00bb &fRight click to sell"),
    DECORATION_LORE_VIEW("&e \u255a&e&l\u00bb &fClick to view\n "),
    TIME_REQUIREMENT_LORE("&c&lCan only be purchased between\n&c%minTime% &land &c%maxTime% &lingame time."),
    QUEST_REQUIREMENT_LORE("&c&lRequires to complete quest &b&l%questName%"),
    REGION_REQUIREMENT_LORE("&c&lRequires to be inside region &b&l%regionName%"),
    DISCORD_SRV_PLAYER_TRANSACTION("%player_name% %bought/sold% %amount% x %items% for %price%"),
    SELL_CONFIRMATION_MULTIPLE_ITEMS("&aYou have successfully sold &1%amount% &aitems for &c%amounttopay%"),
    SELL_CONFIRMATION("&aYou have successfully sold &1%amount% x &b%material% &afor &c%amounttopay%"),
    PAY_CONFIRMATION("&aYou have successfully bought &1%amount% x &b%material% &afor &c%amounttopay%"),
    NO_ITEM_TO_BE_SOLD("&cYou do not have any of this item inside your inventory."),
    CANNOT_PURCHASE_ITEM("&cThis item cannot be purchased."),
    CANNOT_SELL_ITEM("&cThis item cannot be sold."),
    PLAYER_MADE_TRANSACTION("%playername% %bought/sold% %amount% x %material% for %amountofmoney% with the %buy/sell-method%."),
    PLAYER_MADE_TRANSACTION_MULTIPLE_ITEMS("%playername% %bought/sold% %items% for %amountofmoney% with the %buy/sell-method%."),
    TRANSACTION_FAILED_NO_SPACE("%playername% tried to make a transaction of %amount% x %material% for %amountofmoney% but the transaction failed because there was not enough space inside the inventory."),
    TIME_REQUIREMENT_NOT_MET("&cThis item can only be bought/sold between &f%minTime% &cand &f%maxTime%&c ingame time, current time is &f%progress%&c. Come back shortly!"),
    QUEST_REQUIREMENT_NOT_MET("&cThis item can only be bought/sold when you completed the quest &b&l%questName%&c, come back once you completed it"),
    REGION_REQUIREMENT_NOT_MET("&cYou may only buy/sell this item inside the region %regionName%"),
    BUY_SCREEN("buy screen"),
    SELL_SCREEN("sell screen"),
    BUYSTACKS_SCREEN("buy stacks screen"),
    SELLALL_SCREEN("sell all screen"),
    SELLALL_COMMAND("sell all command"),
    SELLGUI_SCREEN("sell gui"),
    QUICK_BUY("quick buy screen"),
    QUICK_SELL("quick sell screen"),
    SHOPSTAND_BUY_SCREEN("shopstand buy screen"),
    SHOPSTAND_SELL_SCREEN("shopstand sell screen"),
    AUTO_SELL_CHEST("SellChest"),
    BOUGHT("bought"),
    SOLD("sold"),
    TRANSACTION_SCREEN_TOTAL_PRICE("&3%price%"),
    TRANSACTION_SCREEN_TOTAL_AMOUNT("&5x %amount%"),
    ON_SCREEN_TITLE_SOLD("&a+%price%"),
    ON_SCREEN_TITLE_BOUGHT("&c-%price%"),
    CURRENCYSYMBOL("$"),
    SYMBOL_PRICING_FORMAT("%symbol%%price%"),
    NAMED_PRICING_FORMAT("%price% %currency-format%"),
    XP_CURRENCY_NAME_SINGULAR("XP point"),
    XP_CURRENCY_NAME_PLURAL("XP points"),
    COMMAND_DISABLED_IN_WORLD("&cThis command is disabled inside this world."),
    EDITSHOP_ADD_ITEM_SUBCOMMAND_DESC("&aAdd a item to the shop."),
    EDITSHOP_EDIT_ITEM_SUBCOMMAND_DESC("&aEdit an existing item in the shop."),
    EDITSHOP_REMOVE_ITEM_SUBCOMMAND_DESC("&aDelete a item from the shop."),
    EDITSHOP_ADD_HAND_ITEM_SUBCOMMAND_DESC("&aAdd a item from you hand to the shop."),
    EDITSHOP_IMPORT_SUBCOMMAND_DESC("&aImports files/settings from another location or plugin."),
    EDITSHOP_ADD_SECTION_SUBCOMMAND_DESC("&aAdd a shop section to the shop."),
    EDITSHOP_EDIT_SECTION_SUBCOMMAND_DESC("&aEdit an existing shop section."),
    EDITSHOP_REMOVE_SECTION_SUBCOMMAND_DESC("&aRemoves an existing shop section from the shop."),
    EDITSHOP_MIGRATE_SUBCOMMAND_DESC("&aAllows to migrate data from another location or plugin."),
    EDITSHOP_SHOPSTANDS_SUBCOMMAND_DESC("&aManage existing or create new shop stands"),
    EDITSHOP_SHOPSTANDS_GIVE_SUBCOMMAND_DESC("&aGives a new shop stand item to place down"),
    EDITSHOP_SHOPSTANDS_DESTROY_SUBCOMMAND_DESC("&aDestroys a shop stand at its current location from its ID"),
    EDITSHOP_SHOPSTANDS_BROWSE_SUBCOMMAND_DESC("&aOpens a GUI with all existing shop stands"),
    EDITSHOP_ADD_ITEM_SUBCOMMAND_SYNTAX("&a/editshop additem <section> <material> <buy price> <sell price> [displayname]"),
    EDITSHOP_EDIT_ITEM_SUBCOMMAND_SYNTAX("&a/editshop edititem <section> <index> <action> <key> <value>"),
    EDITSHOP_REMOVE_ITEM_SUBCOMMAND_SYNTAX("&a/editshop deleteitem <section> <index>"),
    EDITSHOP_ADD_HAND_ITEM_SUBCOMMAND_SYNTAX("&a/editshop addhanditem <section> <buy price> <sell price>"),
    EDITSHOP_IMPORT_SUBCOMMAND_SYNTAX("&a/editshop import <plugin> <file>"),
    EDITSHOP_ADD_SECTION_SUBCOMMAND_SYNTAX("&a/editshop addsection <section> <material> <displayname> <place>"),
    EDITSHOP_EDIT_SECTION_SUBCOMMAND_SYNTAX("&a/editshop editsection <section> <action> <key> <value>"),
    EDITSHOP_REMOVE_SECTION_SUBCOMMAND_SYNTAX("&a/editshop deletesection <section>"),
    EDITSHOP_MIGRATE_SUBCOMMAND_SYNTAX("&a/editshop migrate <plugin> <image> [flags]"),
    EDITSHOP_SHOPSTANDS_SUBCOMMAND_SYNTAX("&a/editshop shopstands <give/destroy> ..."),
    EDITSHOP_SHOPSTANDS_GIVE_SUBCOMMAND_SYNTAX("&a/editshop shopstands give <standType> <section> <index>"),
    EDITSHOP_SHOPSTANDS_DESTROY_SUBCOMMAND_SYNTAX("&a/eshop shopstands destroy <standID>"),
    EDITSHOP_SHOPSTANDS_BROWSE_SUBCOMMAND_SYNTAX("&a/eshop shopstands browse"),
    EDITSHOP_ADDING_ITEM("&aAdding new item to shop %shop%..."),
    EDITSHOP_ADD_ITEM_SUCCESSFUL("&aSuccessfully added the item to shop %shop% under %itemPath%."),
    EDITSHOP_ADDING_SECTION("&aAdding new section config for shop %section%..."),
    EDITSHOP_ADD_SECTION_SUCCESSFUL("&aNew section config created at %path%."),
    EDITSHOP_EDITING_ITEM("&aUpdating the item..."),
    EDITSHOP_EDITING_SECTION("&aUpdating section..."),
    EDITSHOP_EDIT_ITEM_SUCCESSFUL("&aSuccessfully updated '%itemPath%'."),
    EDITSHOP_REMOVING_ITEM("&aRemoving the item from the shop..."),
    EDITSHOP_REMOVE_ITEM_SUCCESSFUL("&aSuccessfully removed '%itemPath%' from the shop."),
    EDITSHOP_REMOVING_SECTION("&aRemoving the section from the shop..."),
    EDITSHOP_REMOVED_SECTION("&a&aSuccessfully removed '%itemPath%' from shop."),
    EDITSHOP_RELOAD_SHOP_TO_SEE_CHANGES("&aUse command /sreload to load the changes inside the shop."),
    FILE_REPLACED("&aThe old %fileName% has been replaced with the new file, the old file can be found inside the backups folder if you messed up something."),
    SHOP_STAND_ITEM_RECEIVED("&aYou were given &c%amount%x &aShopStand for shop item &f%item%"),
    SECTION_DOES_NOT_CONTAIN_ITEM("&cSection '%shopsection%' does not contain the item '%itemLoc%'."),
    NO_VALID_ACTION("&cNot a valid action '%action%'. \nAvailable actions: %actions%."),
    NO_VALID_KEY("&cNot a valid key '%key%'. \nAvailable keys: %keys%."),
    EDIT_KEY_UNAVAILABLE("&cCannot edit the '%key%' of this item as it is a action item"),
    ACTION_NOT_VALID_ON_KEY("&cCannot %action% the '%key%' key."),
    EDIT_ITEM_MATERIAL_INVALID("&cThe material from the item you are trying to edit is invalid. '%material%'"),
    KEY_ALREADY_DEFINED("&cThe '%key%' key is already defined on the item, you cannot add this twice."),
    KEY_IS_NOT_FOUND("&cThe '%key%' key cannot be removed from the item because it is not found."),
    NO_VALID_SLOT_FOR_ITEM("&cThe item could not be placed at slot '%slot%'. The slot needs to be 1 - %maxShopSize%."),
    PLUGIN_NOT_FOUND("&cCannot find a plugin specified with that name, please check if we do support this plugin."),
    FILE_NOT_FOUND("&cCannot find a file with name %fileName% inside the plugin folder of %pluginName%."),
    SLOT_ALREADY_IN_USE("&cThe slot for this section is already taken.\nAll available slots: %available-slots%"),
    SLOT_NEEDS_TO_BE_NUMBER("&cThe slot of the item in the main menu needs to be a number."),
    INVALID_SHOP_STAND_TYPE("&cInvalid shop stand type, valid values: %values%"),
    UNKNOWN_SHOP_STAND_ID("&cNo shop stand found with ID &f%id%"),
    STAND_TYPE_NOT_SUPPORTED("&cShop stand type &f%type% &cis not supported in the current minecraft version"),
    CONFIGS_RELOADED("&aAll configs reloaded!"),
    SELLALL_COMMAND_USAGES("&aCommand usage(s):"),
    SELLALL_INVENTORY_COMMAND_USAGE("&7/sellall inventory &f- &7Sells all items in your inventory"),
    SELLALL_HAND_COMMAND_USAGE("&7/sellall hand &8[amount] &f- &7Sells all items in your hand"),
    SELLALL_ITEM_COMMAND_USAGE("&7/sellall <item> &8[amount] &f- &7Sells all items in your inventory matching the given item name"),
    SHOP_ITEM_GIVEN("&aYou were given &c%amount% &ax &6%material%"),
    SHOP_ITEM_RECEIVED("&aYou received &c%amount% &ax shop item from section &f%section% &aand item index &f%itemIndex%"),
    MINIMUM_AMOUNT("&cThe minimum amount to pay a player is %minimumamount%."),
    PAYMENT_COMPLETE("&aPayment complete."),
    PLAYER_RECEIVED_MONEY("&aYou received &c%amounttogive% &afrom &2%playername%&a!"),
    NEED_PLAYER_AND_AMOUNT("&9You need to give a player and a amount of money."),
    NEED_AMOUNT("&9You need to give a amount of money."),
    PAYCOMMAND_EXAMPLE("&9/pay <player> <amount>"),
    GIVEMONEYCOMMAND_EXAMPLE("&9/givemoney <player> <amount>"),
    GIVEMONEY_COMPLETE("&aGivemoney complete."),
    CHECK_BALANCE("&aYou currently have &b%amountofmoney%&a."),
    CHECK_BALANCE_OTHER_PLAYER("&b%playername% &acurrently has &c%amountofmoney%&a."),
    DEFAULT_SPAWNER_NAME("&9&l%spawner-type% &rSpawner"),
    SPAWNER_BROKE_AND_APPLIED_TO_INVENTORY("&aYou successfully mined a &9%spawner-type%&a, the item is applied to your inventory."),
    NEED_SILK_TOUCH("&cYou need to use an silk touch pickaxe to mine this!"),
    JOIN_MESSAGE("&2Welcome, your balance is &c%balance%!"),
    LEVEL_EVENT_MESSAGE("&2[Levels] &5Here you go, &c%amount% &5has been added to your balance!"),
    DISPLAYNAME_NULL("Could not get displayname."),
    ITEM_NAME_NULL("Could not get the name of the item."),
    NEED_ITEM_MATERIAL("Can not create item with a blank material value."),
    ITEM_MATERIAL_NULL("Could not get the item material. If you think this is a bug, please report it to our discord support server which you can find at the plugin page. %material%"),
    ITEM_ENCHANTMENT_NULL("Could not get the item enchantment."),
    NEED_ITEM_ENCHANTMENT("Can not create enchantment with a blank value."),
    ITEM_ENCHANTMENT_NOT_SUPPORTED("This enchantment is not supported in this minecraft version."),
    ITEM_ENCHANTMENT_STRENGTH_NULL("Could not get the strength of the enchantment."),
    ENCHANTMENT_CANNOT_APPLY_TO_ITEM("To apply an enchantment to the item, the material needs to be enchanted book or a tool that fits the enchantment."),
    ITEM_SPAWNERTYPE_NULL("Could not get the spawner type of the item."),
    NEED_ITEM_SPAWNERTYPE("Can not create spawner with a empty spawner-type."),
    MATERIAL_NEEDS_TO_BE_SPAWNER("To apply a Spawner-Type(Entity-Type) to the item, the material of the item needs to be a spawner."),
    ITEM_POTIONTYPE_NULL("Could not get the potion type of the item."),
    NEED_ITEM_POTIONTYPE("Can not create a potiontype with a blank value."),
    POTIONTYPE_NOT_SUPPORTED("This potion type is not supported or doesn't exist."),
    MATERIAL_NEEDS_TO_BE_POTION("To apply an potion effect, the material needs to be a kind of a potion or a tipped arrow."),
    MATERIAL_NEEDS_TO_BE_LEATHER_ARMOR("To apply an color to a armor piece, the material needs to be leather armor."),
    CANT_BIND_MULTIPLE_APPLYMENTS("Can not create an item with multiple applyments like enchantments/spawner types or potion effect types."),
    ITEM_ERROR("&cItem Error, look in the console."),
    MATERIAL_NOT_SUPPORTED("This item material is not supported in this minecraft version."),
    ITEM_NEEDS_BUY_AND_SELL_VALUE("Every item in the shop needs to have a buy/sell value."),
    SELL_VALUE_WRONG("The sell value needs to be a decimal or a number."),
    BUY_VALUE_WRONG("The buy value needs to be a decimal or a number."),
    OPTION_TYPE_CHANGED("Please change option 'type' to option 'spawnertype' to create a spawner in the shop. The name of the option has changed in version 2.2 for clarity."),
    OPTION_META_CHANGED("Please change option 'meta' to option 'displayname' to create a item with a displayname in the shop. The name of the option has changed in version 2.2 for clarity."),
    CANNOT_DISPLAY_SHOPSECTION("Cannot create shop section '%shopsection%' because the slot of the item needs to be in a range from 0 - 45."),
    NEED_RECIPE_MATERIAL("Cannot create the recipe material with a blank value."),
    CANNOT_GET_RECIPE_MATERIAL("Cannot create the recipe material, please check the material name for any spelling mistakes and if it exists."),
    RECIPE_MATERIAL_NOT_SUPPORTED("The given recipe material is not supported in this minecraft version."),
    MATERIAL_NEEDS_TO_BE_BOOK("To apply an recipe to the item, the material of the item needs to be a KnowLedge book."),
    NEED_SKULLOWNER("Cannot create the PlayerHead without a specified player (Skull Owner)."),
    MATERIAL_NEEDS_TO_BE_SKULL("To apply an skull texture (Skull Owner) to the item, the material of the item needs to be a Player Head."),
    INVALID_ITEM_REQUIREMENT_TYPE("Invalid item requirement type for '%type%'"),
    INVALID_ITEM_REQUIREMENT("Invalid item requirement for %requirement%"),
    INVALID_SPAWN_POTENTIAL_WEIGHT("Invalid weight for spawn potential, should be a number"),
    INVALID_SPAWN_POTENTIAL("Invalid entry for spawn potential '%spawn_potential%', should be format of '<entityType>:<weight>'"),
    INVALID_MATERIAL_FOR_ITEM_OPTION("Material of item should be %material% to set %item-option%"),
    INVALID_OMINOUS_BOTTLE_STRENGTH("Invalid strength for ominous bottle, should be between 1 and 5, but got %strength%"),
    ERROR_SETTING_ITEM_OPTION("Failed to set the %item-option% for item with error:"),
    INVALID_ITEM_ACTION("Invalid item action for '%action%'"),
    CANNOT_DO_THAT_ACTION_ITEM("&cCannot do that for a action item"),
    ITEMS_PATH_IN_SHOPS_CONFIG("Item path in shops.yml: %location%"),
    ITEMS_PATH_IN_SECTIONS_CONFIG("Item path in sections.yml: %location%"),
    ITEMS_PATH_IN_CONFIG("Item path in config.yml: %location%"),
    ITEMS_LOCATION_IN_SHOPS("In shops config /shops/%section%.yml at location: %location%"),
    ITEMS_LOCATION_IN_SECTIONS("In sections config /sections/%section%.yml at location: %location%"),
    SPAWNER_COMPATIBILITY_ENABLED("Spawner provider set to %provider% in config"),
    SPAWNER_PROVIDER_FOUND("%provider% found, integrating..."),
    SPAWNER_PROVIDER_NOT_FOUND("%provider% was not found"),
    FAILED_SPAWNER_PROVIDER_INTEGRATION("Failed to integrate with %provider%"),
    USING_EXTERNAL_SPAWNERS("Using %provider% as external spawner provider..."),
    MINEABLESPAWNERS_COMPATIBILITY_ENABLED("Spawner provider set to MineableSpawners in config"),
    MINEABLESPAWNERS_FOUND("MineableSpawners found, integrating..."),
    MINEABLESPAWNERS_NOT_FOUND("MineableSpawners was not found"),
    COULD_NOT_INTEGRATE_WITH_MINEABLESPAWNERS("Could not integrate with MineableSpawners"),
    USING_MINEABLESPAWNERS_SPAWNERS("Using MineableSpawners as spawner provider..."),
    ROSESTACKER_COMPATIBILITY_ENABLED("Spawner provider set to RoseStacker in config"),
    ROSESTACKER_NOT_FOUND("RoseStacker was not found"),
    COULD_NOT_INTEGRATE_WITH_ROSESTACKER("Could not integrate with RoseStacker"),
    ROSESTACKER_FOUND("RoseStacker found, integrating..."),
    USING_ROSESTACKER_SPAWNERS("Using RoseStacker as spawner provider..."),
    ULTIMATESTACKER_COMPATIBILITY_ENABLED("Spawner provider set to UltimateStacker in config"),
    ULTIMATESTACKER_NOT_FOUND("UltimateStacker was not found"),
    COULD_NOT_INTEGRATE_WITH_ULTIMATESTACKER("Could not integrate with UltimateStacker"),
    ULTIMATESTACKER_FOUND("UltimateStacker found, integrating..."),
    USING_ULTIMATESTACKER_SPAWNERS("Using UltimateStacker as spawner provider..."),
    WILDSTACKER_COMPATIBILITY_ENABLED("Spawner provider set to WildStacker in config"),
    WILDSTACKER_NOT_FOUND("WildStacker was not found."),
    WILDSTACKER_FOUND("WildStacker found, integrating..."),
    COULD_NOT_INTEGRATE_WITH_WILDSTACKER("Could not integrate with WildStacker"),
    USING_WILDSTACKER_SPAWNERS("Using WildStacker as spawner provider."),
    SILKSPAWNERS_COMPATIBILITY_ENABLED("SilkSpawners compatibility has been enabled in config."),
    SILKSPAWNERS_NOT_FOUND("SilkSpawners not found, using DynamicShopGUIPlus spawners..."),
    SILKSPAWNERS_FOUND("SilkSpawners found, integrating..."),
    COULD_NOT_INTEGRATE_WITH_SILKSPAWNERS("Could not integrate with SilkSpawners."),
    USING_SILKSPAWNERS_SPAWNERS("Using SilkSpawners spawners..."),
    USING_DEFAULT_SPAWNERS("Using plugin default spawners..."),
    SPAWNER_PROVIDER_DISABLED("Spawner provider disabled in config, disabling..."),
    CANNOT_FIND_DISCOUNTED_SECTION("Cannot add discounts for section '%section%', either the shop section is disabled or not found."),
    DISABLING_PLUGIN("Disabling the plugin..."),
    NOT_SPAWN_ABLE("This entity type is not spawn able. '%spawnertype%'"),
    REAL_PLAYER("You need to be a real player to use that command."),
    PLAYER_NOT_ONLINE("&cThat player does not exist or is not online."),
    ITEM_NULL("&cThe item does not exist."),
    ITEM_CANNOT_BE_SOLD_TO_SERVER("&cThe item (%material%) cannot be sold to the server because the item has a error inside the shop. Please report this issue to server admins or the owner."),
    CANNOT_SELL_AIR("&cYou cannot sell air..."),
    NO_PERMISSIONS("&cYou do not have permissions for that!"),
    NO_PERMISSIONS_TO_OPEN_SHOP("&cYou do not have the required permission to open this shop section."),
    CANNOT_DO_THAT("&4You can not do that!!"),
    MORE_SPACE_NEEDED("&cPlease make more space inside your inventory and then try again."),
    COULD_NOT_STORE_ALL_ITEMS("&c%amount% item(s) couldn't be added to your inventory because it is full."),
    NO_ITEM_FOUND("&cNo items found which can be sold."),
    NOT_ENOUGH_ITEMS_TO_SELL("&cIt is required to sell atleast %min-sell%, you only have %quantity%"),
    NOT_ENOUGH_ITEMS_TO_BUY("&cIt is required to buy atleast %min-buy%"),
    NOT_ENOUGH_SPACE_INSIDE_INVENTORY("&4You do not have enough space in your inventory, dropping the remaining items on ground."),
    CANCELING_TRANSACTION_NO_SPACE("&4You do not have enough space in your inventory, canceling transaction. \nPlease make more room inside your inventory and try again."),
    INSUFFICIENT_MONEY("&cYou do not have enough %currency%."),
    NO_ITEMS_IN_SECTION("Can not open shop section %shopsection% because there are no items found!"),
    COULD_NOT_SAVE_CONFIG("Could not save %fileName% config."),
    RGB_COLOR_FORMATTED_WRONG("RGB colors must be formatted as: '0x000000', '#000000' or '000000' at %path%."),
    NO_SHOP_FOUND("&cCan not find a shop section called by that name!"),
    NO_VALID_AMOUNT("&cNot a valid amount"),
    NO_SHOP_PAGE_FOUND("&cNo shop page found for %page%"),
    RESTART_PLUGIN("Can not open the shop section, try restarting the plugin or join the discord server for support."),
    COULD_NOT_CHECK_FOR_UPDATES("Could not check for updates."),
    ERROR_OCCURRED_WHILE_RELOADING("&4A error has occurred while reloading the plugin, please look inside the console for any error message."),
    INVALID_ITEM_TYPE("&cInvalid item type for %type%"),
    CANNOT_ENTER_SHOP_BANNED_GAMEMODE("&cYou cannot buy/sell items while being in %gamemode%."),
    CANNOT_USE_COMMAND_BANNED_GAMEMODE("&cYou cannot use this command while being in %gamemode%."),
    EXCEPTION_LOADING_FILL_ITEM("A exception occurred while loading shop fill-Item for section '%section%'."),
    CANNOT_LOAD_SEASON_MODIFIER("Failed to load season modifier for '%path%' because there was no loaded section/item found."),
    CANNOT_LOAD_SEASON_MODIFIER_ACTION_ITEM("Cannot load season modifier for '%path%' as the shop item contains a action"),
    INVALID_SEASON_MODIFIER("Invalid modifier found at '%path%', it needs to be a valid percent between -100 and 100 like '-75%', '+25%', ... Got %current%"),
    CANNOT_LOAD_ECONOMY_PROVIDER("Failed to load economy provider for shop '%shop%' with reason '%reason%', using default..."),
    CANNOT_FIND_ECONOMY_PROVIDER("Could not find a economy provider for shop '%shop%' such as '%type%', using default..."),
    CANNOT_FIND_DEFAULT_ECONOMY_PROVIDER("Could not find the economy type specified inside the config.yml such as '%economy%', trying to automatically find supported economy type..."),
    CANNOT_LOAD_DEFAULT_ECONOMY_PROVIDER("Failed to load default economy provider with reason '%reason%', trying to automatically find supported economy provider"),
    REMOVED_SHOP_STAND("&cRemoved shop stand from location %location%"),
    SHOP_STAND_LOAD_FAILED("Failed to load shop stand with ID '%id%': %reason%"),
    SHOP_STAND_UNLOAD_FAILED("Failed to unload shop stand with ID '%id%': %reason%"),
    SHOP_STAND_INSIDE_UNLOADED_CHUNK("Shop stand is inside a unloaded chunk"),
    ITEM_FROM_SHOP_STAND_NOT_FOUND("Cannot find shop item from shop stand '%item%'"),
    BLOCK_FROM_SHOP_STAND_NOT_FOUND("Block type from stand at location %location% does not match with cached block type of '%type%'. Possibly removed from world manually but not from plugin?"),
    INVALID_SHOP_STAND_WORLD("Invalid stand location for world '%world%', does not exist for shop stand ID %id%"),
    INVALID_SHOP_STAND_FORMAT("Invalid stand location with x%pos_x%, y%pos_y%, z%pos_z% for shop stand ID %id%"),
    ERROR_SAVING_SHOP_STANDS("Failed to save shop stands to 'stands.json'"),
    ERROR_LOADING_SHOP_STANDS("Failed to load shop stands from 'stands.json'"),
    ERROR_LOADING_SHOP_STAND("Failed to load shop stand('%stand%') from stands.json with reason: %reason%"),
    ERROR_CREATING_STANDS_STORAGE("Failed to create 'stands.json' storage file"),
    INVALID_STAND_TYPE("Invalid shop stand type for %type%"),
    SHOP_STANDS_MODULE_NOT_ENABLED("Shop stands module not enabled"),
    CANNOT_OPEN_SHOP_STAND("Failed to open shop stand for player %player%: ShopItem(%item%) does not exist!"),
    INVALID_CLICK_TYPE("Invalid click type for %click-type% with entry %click-mapping%, using default..."),
    INVALID_CLICK_ACTION("Invalid click action for %click-action% with entry %click-mapping%, using default...");

    private String def;
    private static final DynamicShopGUIPlus plugin;
    private static final boolean supportsComponents;
    private static Locale defaultLang;
    private static boolean perPlayerLanguages;
    private static final Map<Locale, LangCache> langCache;

    private Lang(String def) {
        this.def = def;
    }

    public void reload() {
        if (!langCache.isEmpty()) {
            langCache.clear();
        }
        Lang.plugin.startupReload.checkIfLanguageFilesExist();
        boolean found = true;
        String fileName = ConfigManager.badYaml != null ? "lang-en.yml" : ConfigManager.getConfig().getString("language-file", "lang-en.yml");
        try {
            defaultLang = Locale.forLanguageTag(fileName.split("-")[1].split("\\.")[0].replace("_", "-"));
        }
        catch (ArrayIndexOutOfBoundsException e) {
            found = false;
        }
        if (!found || defaultLang.getLanguage().isEmpty()) {
            SendMessage.errorMessage("Failed to get language for file name '%fileName%', using English as default...".replace("%fileName%", fileName));
            defaultLang = Locale.ENGLISH;
        }
        try {
            langCache.put(defaultLang, new LangCache(fileName));
            SendMessage.infoMessage("Using " + fileName + " as language file");
        }
        catch (ConfigLoadException e) {
            SendMessage.errorMessage("Failed to load language file '%fileName%' from config.yml, using English as default...".replace("%fileName%", fileName));
            try {
                defaultLang = Locale.ENGLISH;
                langCache.put(defaultLang, new LangCache("lang-en.yml"));
            }
            catch (ConfigLoadException ex) {
                SendMessage.errorMessage("Failed to load default lang-en.yml, this is a bug!");
            }
        }
        if (ConfigManager.badYaml == null && ConfigManager.getConfig().getBoolean("per-player-languages", true)) {
            SendMessage.infoMessage(LOADING_LANGUAGE_FILES.get());
            File languageFolder = new File(plugin.getDataFolder() + File.separator + "LanguageFiles");
            if (languageFolder.exists() && languageFolder.isDirectory()) {
                perPlayerLanguages = true;
                for (File file : languageFolder.listFiles()) {
                    if (file.getName().equals(fileName)) continue;
                    Locale locale = null;
                    try {
                        locale = Locale.forLanguageTag(file.getName().split("-")[1].split("\\.")[0].replace("_", "-"));
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        found = false;
                    }
                    if (!found || locale == null || locale.getLanguage().isEmpty()) {
                        SendMessage.errorMessage(FAILED_TO_GET_LOCALE.get().replace("%fileName%", file.getName()));
                        continue;
                    }
                    try {
                        langCache.put(locale, new LangCache(file));
                    }
                    catch (ConfigLoadException e) {
                        SendMessage.errorMessage(FAILED_TO_LOAD_LANGUAGE_FILE.get().replace("%fileName%", file.getName()));
                    }
                }
                SendMessage.infoMessage(LOADED_LANGUAGE_FILES.get().replace("%amount%", String.valueOf(langCache.size())));
            } else {
                SendMessage.errorMessage(FAILED_TO_LOAD_LANGUAGE_FILES.get());
            }
        }
    }

    public String getKey() {
        return this.name().toLowerCase(Locale.ENGLISH).replace("_", "-").replace("0", ",");
    }

    public void clearMessages() {
        if (!langCache.isEmpty()) {
            langCache.clear();
        }
    }

    public Translatable get(Player p) {
        if (perPlayerLanguages) {
            return this.get(UserManager.getUser(p).getLocale());
        }
        return this.get();
    }

    public Translatable get(Locale locale) {
        return langCache.getOrDefault(locale, langCache.get(defaultLang)).get(this.getKey(), this.def);
    }

    public Translatable get() {
        return langCache.get(defaultLang).get(this.getKey(), this.def);
    }

    public static Translatable getOrNull(String key) {
        return langCache.get(defaultLang).getOrNull(key);
    }

    public static Translatable getOrNull(String key, Locale locale) {
        return langCache.getOrDefault(locale, langCache.get(defaultLang)).getOrNull(key);
    }

    public static Translatable getOrDefault(Lang key, String extra, Locale locale) {
        String lang = key.getKey() + extra;
        Translatable prefer = Lang.getOrNull(lang, locale);
        return prefer != null ? prefer : key.get(locale);
    }

    public static Set<Locale> getLocales() {
        return langCache.keySet();
    }

    public static Locale getDefaultLocale() {
        return defaultLang;
    }

    public List<Translatable> getMultiple() {
        if (supportsComponents) {
            return Arrays.stream(this.getRaw().split("\\\\n|\\n")).map(Lang::fromConfig).collect(Collectors.toList());
        }
        return this.get().getLines();
    }

    public String getRaw() {
        return langCache.get(defaultLang).getRaw(this.getKey(), this.def);
    }

    public String getRaw(Locale locale) {
        return langCache.getOrDefault(locale, langCache.get(defaultLang)).getRaw(this.getKey(), this.def);
    }

    public static Translatable fromConfig(String s) {
        if (plugin.getMetaUtils() instanceof BukkitMeta) {
            return new TranslatableRaw(s);
        }
        return new TranslatableComponent(s);
    }

    private static FileConfiguration getLangFile(File file) {
        if (!file.exists()) {
            return null;
        }
        YamlConfiguration c = plugin.loadConfiguration(file, file.getName());
        if (c == null) {
            return null;
        }
        try {
            plugin.saveResource("LanguageFiles" + File.separator + file.getName(), true);
            YamlConfiguration conf = plugin.loadConfiguration(file, file.getName());
            if (conf != null) {
                for (String str : c.getKeys(false)) {
                    conf.set(str, c.get(str));
                }
                ConfigUtil.save((FileConfiguration)conf, file);
                return conf;
            }
            ConfigUtil.save((FileConfiguration)c, file);
            return c;
        }
        catch (Exception e) {
            SendMessage.logDebugMessage("Failed to automatically update the language file for " + file.getName());
            return plugin.loadConfiguration(file, file.getName());
        }
    }

    static {
        plugin = DynamicShopGUIPlus.getInstance();
        supportsComponents = !(DynamicShopGUIPlus.getInstance().getMetaUtils() instanceof BukkitMeta);
        langCache = new HashMap<Locale, LangCache>();
    }

    private static final class LangCache {
        private final FileConfiguration config;
        private final HashMap<String, Translatable> cache = new HashMap();

        public LangCache(String fileName) throws ConfigLoadException {
            this.config = Lang.getLangFile(new File(plugin.getDataFolder() + File.separator + "LanguageFiles", fileName));
            if (this.config == null) {
                throw new ConfigLoadException("Can not find or load language file '" + fileName + "'");
            }
        }

        public LangCache(File file) throws ConfigLoadException {
            this.config = Lang.getLangFile(file);
            if (this.config == null) {
                throw new ConfigLoadException("Can not find or load language file '" + file.getName() + "'");
            }
        }

        public Translatable get(String key, String def) {
            Translatable value;
            if (this.cache.containsKey(key)) {
                return this.cache.get(key);
            }
            try {
                value = Lang.fromConfig(this.config.getString(key, def));
            }
            catch (NullPointerException npe) {
                value = Lang.fromConfig(def);
            }
            this.cache.put(key, value);
            return value;
        }

        public Translatable getOrNull(String key) {
            if (this.cache.containsKey(key)) {
                return this.cache.get(key);
            }
            String s = this.config.getString(key);
            if (s == null) {
                return null;
            }
            Translatable value = Lang.fromConfig(s);
            this.cache.put(key, value);
            return value;
        }

        public String getRaw(String key, String def) {
            try {
                return this.config.getString(key, def);
            }
            catch (NullPointerException ignored) {
                return def;
            }
        }
    }
}

