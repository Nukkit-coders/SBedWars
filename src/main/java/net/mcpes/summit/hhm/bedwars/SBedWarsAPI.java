package net.mcpes.summit.hhm.bedwars;

import cn.nukkit.Player;
import cn.nukkit.level.Location;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author hhm
 * @date 2017/7/13
 * @since SBedWars
 */

public interface SBedWarsAPI {
    static SBedWarsAPI getInstance() {
        return SBedWars.getInstance();
    }

    void loadAllRoom();

    boolean loadRoom(int id);

    int getFreeID();

    void loadWorlds();

    void loadWorld(String worldName) throws IOException;

    void broadcastMessage(CopyOnWriteArrayList<Player> p, String msg);

    void broadcastTitle(CopyOnWriteArrayList<Player> p, int fadeIn, int stay, int fadeOut, String msg, String twoMsg);

    void broadcastSound(CopyOnWriteArrayList<Player> p, int type);

    void broadcastTip(CopyOnWriteArrayList<Player> p, String msg);

    void broadcastSpeak(CopyOnWriteArrayList<Player> p, String pn, String msg);

    void broadcastTeamSpeak(CopyOnWriteArrayList<String> p, String pn, String msg);

    void broadcastTeamMessage(CopyOnWriteArrayList<String> p, String msg);

    int isPlayerGaming(String name);

    int isSign(String pos);

    void addSign(String pos, int id);

    void resetSign(int id, Location pos);

    void delSign(String pos, int id);

    void registerComposes();
}