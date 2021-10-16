package top.seatide.remote;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.ess3.api.IEssentials;
import top.seatide.remote.Utils.Files;
import top.seatide.remote.Utils.LogUtil;
import top.seatide.remote.Web.API;
import top.seatide.remote.Web.Request;

public final class Main extends JavaPlugin {
    public API api;
    public ProtocolManager man;
    public IEssentials ess;

    @Override
    public void onEnable() {
        Request.setPlugin(this);
        api = new API();
        Files.init(this);
        try {
            api.startServer();
            LogUtil.success("HTTP 服务器已在端口 " + api.getServerPort() + " 开启。");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("无法启动 HTTP 服务器。");
        }
        man = ProtocolLibrary.getProtocolManager();
        man.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.CHAT) {
            @Override
            public void onPacketSending(PacketEvent e) {
                System.out.println("Listening packet event.");
                var pack = e.getPacket();
                var msg = pack.getStrings().read(0);
                System.out.println(msg);
            }
        });
        man.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.CHAT) {
            @Override
            public void onPacketReceiving(PacketEvent e) {
                System.out.println("Listening packet event.");
                var pack = e.getPacket();
                var msg = pack.getStrings().read(0);
                System.out.println(msg);
            }
        });
        // ess = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
        // System.out.println(ess.getTimer().getAverageTPS());
    }

    @Override
    public void onDisable() {
        api.stopServer();
        LogUtil.info("关闭 HTTP 服务器中...");
    }
}