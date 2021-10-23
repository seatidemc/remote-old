package top.seatide.remote.Utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import top.seatide.remote.Main;

public class X {
    public static Main plugin;
    public static boolean commandResult;

    public static void init(Main plug) {
        plugin = plug;
    }

    /**
     * 安全地从 JSON 对象中获取字符串
     * 
     * @param j   JSON 对象
     * @param key 字符串所在的键名
     * @return 如果存在则返回字符串，不存在则返回空字符串 {@code ""}
     */
    public static String getStringSafe(JSONObject j, String key) {
        try {
            return j.getString(key);
        } catch (JSONException e) {
            return "";
        }
    }

    /**
     * 安全地从 JSON 对象中获取字符串
     * 
     * @param j   JSON 对象
     * @param key 字符串所在的键名
     * @return 如果存在则返回字符串，不存在则返回 {@code null}。
     */
    public static String getStringSafeNull(JSONObject j, String key) {
        try {
            return j.getString(key);
        } catch (JSONException e) {
            return null;
        }
    }

    public static TemporalAmount temporal(String feString) {
        if (Character.isUpperCase(feString.charAt(feString.length() - 1))) {
            return Period.parse("P" + feString);
        } else {
            return Duration.parse("PT" + feString);
        }
    }

    public static boolean isPD(String pd) {
        return Pattern.matches("\\d+(h|m|s|D|M|W|Y)", pd);
    }

    public static Date getDateByPD(String pd) {
        var a = temporal(pd);
        var loc = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault());
        loc.plus(a);
        return Date.from(loc.atZone(ZoneId.systemDefault()).toInstant());
    }
}
