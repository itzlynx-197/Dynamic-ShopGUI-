

package com.siliconstudios.dynamicshopguiplus.objects.inventorys;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ShopInventory;
import com.siliconstudios.dynamicshopguiplus.objects.layouts.SimpleCard;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CustomizeLayout
extends ShopInventory {
    private ConversationFactory factory;
    private Conversation conv;
    private final SimpleCard card = new SimpleCard();
    private final Pattern namePattern = Pattern.compile("[a-zA-Z0-9]+");
    private final DynamicShopGUIPlus plugin = DynamicShopGUIPlus.getInstance();
    private final ArrayList<String> files = new ArrayList();
    private final Player player;
    private final Consumer<SimpleCard> onComplete;
    private final Prompt prompt = new StringPrompt(){

        public String getPromptText(ConversationContext c) {
            switch (Input.valueOf((String)c.getSessionData((Object)"input")).ordinal()) {
                case 1: {
                    CustomizeLayout.this.player.sendTitle("\u00a7bEnter layout title", "", 5, 80, 5);
                    return "\u00a7aEnter a title for this layout.\n\u00a7aExample: ModernLayout";
                }
                case 2: {
                    CustomizeLayout.this.player.sendTitle("\u00a7bEnter description", "", 5, 80, 5);
                    return "\u00a7aEnter a description for this layout.\n\u00a7aExample: Modern shop layout with almost every in-game item, designed for 1.21 survival based servers.";
                }
                case 3: {
                    CustomizeLayout.this.player.sendTitle("\u00a7bEnter tags", "", 5, 80, 5);
                    return "\u00a7aTags can be used to easily identify this layout, use the format of \u00a7f#<tag>\u00a7a, use commas(',') to specify multiple tags.\n\u00a7aExample: #modern, #allItems, #cheap";
                }
            }
            return null;
        }

        public Prompt acceptInput(ConversationContext c, String s) {
            c.setSessionData((Object)"result", (Object)s);
            return Prompt.END_OF_CONVERSATION;
        }
    };

    public CustomizeLayout(Player p, Consumer<SimpleCard> onComplete) {
        this.onComplete = onComplete;
        this.player = p;
        this.startConversation(p, Input.TITLE);
    }

    private void startConversation(Player p, Input input) {
        this.factory = new ConversationFactory((Plugin)DynamicShopGUIPlus.getInstance());
        this.conv = this.factory.withFirstPrompt(this.prompt).addConversationAbandonedListener((ConversationAbandonedListener)new ConvAbandon()).withEscapeSequence("exit").withLocalEcho(false).withTimeout(60).withPrefix((ConversationPrefix)new ConvPrefix()).buildConversation((Conversable)p);
        this.conv.getContext().setSessionData((Object)"input", (Object)input.name());
        this.conv.begin();
    }

    private static enum Input {
        GUIDELINES,
        TITLE,
        DESC,
        TAGS;

    }

    private final class ConvAbandon
    implements ConversationAbandonedListener {
        private ConvAbandon() {
        }

        public void conversationAbandoned(ConversationAbandonedEvent event) {
            Player p = (Player)event.getContext().getForWhom();
            if (event.gracefulExit()) {
                String result = (String)event.getContext().getSessionData((Object)"result");
                switch (Input.valueOf((String)event.getContext().getSessionData((Object)"input")).ordinal()) {
                    case 1: {
                        if (result.length() < 6) {
                            SendMessage.chatToPlayer(p, "\u00a7cTitle must be longer as 6 characters");
                            CustomizeLayout.this.startConversation(p, Input.TITLE);
                        } else if (result.length() > 32) {
                            SendMessage.chatToPlayer(p, "\u00a7cInvalid title, title cannot be longer as 32 characters");
                            CustomizeLayout.this.startConversation(p, Input.TITLE);
                        } else {
                            CustomizeLayout.this.card.setTitle(result);
                            CustomizeLayout.this.startConversation(p, Input.DESC);
                        }
                        return;
                    }
                    case 2: {
                        if (result.length() < 60) {
                            SendMessage.chatToPlayer(p, "\u00a7cDescription must be longer as 60 characters, please write a descriptive explanation of your layout");
                            CustomizeLayout.this.startConversation(p, Input.DESC);
                            return;
                        }
                        CustomizeLayout.this.card.setDescription(result);
                        CustomizeLayout.this.startConversation(p, Input.TAGS);
                        return;
                    }
                    case 3: {
                        ArrayList<String> tags = new ArrayList<String>();
                        if (!result.equalsIgnoreCase("skip")) {
                            for (String tag : result.replace(" ", "").split(",")) {
                                if (tag.startsWith("#")) {
                                    tag = tag.substring(1);
                                }
                                if (!CustomizeLayout.this.namePattern.matcher(tag).matches()) {
                                    SendMessage.chatToPlayer(p, "\u00a7c\u00a7lInvalid tag for " + tag + ", contains invalid characters");
                                    CustomizeLayout.this.startConversation(p, Input.TAGS);
                                    return;
                                }
                                if (tag.length() > 10) {
                                    SendMessage.chatToPlayer(p, "\u00a7c\u00a7lInvalid tag for " + tag + ", tag cannot be longer as 10 characters");
                                    CustomizeLayout.this.startConversation(p, Input.TAGS);
                                    return;
                                }
                                tags.add(tag);
                            }
                        } else {
                            SendMessage.chatToPlayer(p, "\u00a7c\u00a7lAtleast 1 tag is required!");
                            CustomizeLayout.this.startConversation(p, Input.TAGS);
                            return;
                        }
                        CustomizeLayout.this.card.setTags(tags);
                    }
                }
                CustomizeLayout.this.onComplete.accept(CustomizeLayout.this.card);
            } else {
                SendMessage.chatToPlayer(p, "\u00a7cExited the conversation.");
            }
        }
    }

    private final class ConvPrefix
    implements ConversationPrefix {
        private ConvPrefix() {
        }

        public String getPrefix(ConversationContext context) {
            return Lang.SHOP_PREFIX.get().getLegacy() + ChatColor.RESET + " ";
        }
    }
}

