package top.seatide.remote.Utils;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public final class FakePlayer implements ConsoleCommandSender {
    public String finalResponse = "";

    @Override
    public boolean isPermissionSet(String name) {

        return true;
    }

    @Override
    public boolean isPermissionSet(Permission perm) {

        return true;
    }

    @Override
    public boolean hasPermission(String name) {

        return true;
    }

    @Override
    public boolean hasPermission(Permission perm) {

        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {

        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {

        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {

        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {

        return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
    }

    @Override
    public void recalculatePermissions() {

    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {

        return null;
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {

    }

    @Override
    public void sendMessage(String message) {
    }

    @Override
    public void sendMessage(String... messages) {
    }

    @Override
    public void sendMessage(UUID sender, String message) {

    }

    @Override
    public void sendMessage(UUID sender, String... messages) {
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public String getName() {
        return "Fake";
    }

    @Override
    public Spigot spigot() {

        return null;
    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void acceptConversationInput(String input) {

    }

    @Override
    public boolean beginConversation(Conversation conversation) {

        return false;
    }

    @Override
    public void abandonConversation(Conversation conversation) {

    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {

    }

    @Override
    public void sendRawMessage(String message) {
        this.finalResponse = message;
    }

    @Override
    public void sendRawMessage(UUID sender, String message) {
        this.finalResponse = message;
    }
}
