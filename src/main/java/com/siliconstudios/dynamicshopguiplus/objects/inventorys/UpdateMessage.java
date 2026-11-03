

package com.siliconstudios.dynamicshopguiplus.objects.inventorys;

import java.util.function.Consumer;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
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

public class UpdateMessage {
    private ConversationFactory factory;
    private Conversation conv;
    private final Player player;
    private final Consumer<String> onComplete;

    public UpdateMessage(Player p, Consumer<String> onComplete) {
        this.onComplete = onComplete;
        this.player = p;
        this.startConversation(p);
    }

    private void startConversation(Player p) {
        this.factory = new ConversationFactory((Plugin)DynamicShopGUIPlus.getInstance());
        this.conv = this.factory.withFirstPrompt((Prompt)new StringPrompt(){

            public String getPromptText(ConversationContext c) {
                UpdateMessage.this.player.sendTitle("\u00a7bEnter update message", "", 5, 80, 5);
                return "\u00a7aEnter a update message of the changes made in this update, maximum 128 characters.";
            }

            public Prompt acceptInput(ConversationContext c, String s) {
                c.setSessionData((Object)"result", (Object)s);
                return Prompt.END_OF_CONVERSATION;
            }
        }).addConversationAbandonedListener((ConversationAbandonedListener)new ConvAbandon()).withEscapeSequence("exit").withLocalEcho(false).withTimeout(60).withPrefix((ConversationPrefix)new ConvPrefix()).buildConversation((Conversable)p);
        this.conv.begin();
    }

    private final class ConvAbandon
    implements ConversationAbandonedListener {
        private ConvAbandon() {
        }

        public void conversationAbandoned(ConversationAbandonedEvent event) {
            Player p = (Player)event.getContext().getForWhom();
            if (event.gracefulExit()) {
                String result = (String)event.getContext().getSessionData((Object)"result");
                if (result.length() < 16) {
                    SendMessage.chatToPlayer(p, "\u00a7cMessage must be longer as 16 characters, please write a descriptive explanation of your update");
                    UpdateMessage.this.startConversation(p);
                    return;
                }
                if (result.length() > 128) {
                    SendMessage.chatToPlayer(p, "\u00a7cMessage cannot be longer as 128 characters, please write a descriptive explanation of your update");
                    UpdateMessage.this.startConversation(p);
                    return;
                }
                UpdateMessage.this.onComplete.accept(result);
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

