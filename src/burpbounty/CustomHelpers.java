package burpbounty;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Date;
import java.util.Map;
import java.util.Random;

public class CustomHelpers {
    /**
     * 获取精确到秒的时间戳
     * @param date
     * @return int
     */
    public static int getSecondTimestamp(Date date){
        if (null == date) {
            return 0;
        }
        String timestamp = String.valueOf(date.getTime()/1000);
        return Integer.valueOf(timestamp);
    }

    /**
     * 随机取若干个字符
     * @param number
     * @return
     */
    public static String randomStr(int number) {
        StringBuffer s = new StringBuffer();
        char[] stringArray = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
                'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
                'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6',
                '7', '8', '9'};
        Random random = new Random();
        for (int i = 0; i < number; i++){
            char num = stringArray[random.nextInt(stringArray.length)];
            s.append(num);
        }
        return s.toString();
    }

    /**
     *
     * @param str
     * @return boolean
     */
    public static boolean isJson(String str) {
        boolean result = false;
        if (str != null && !str.isEmpty()) {
            str = str.trim();
            if (str.startsWith("{") && str.endsWith("}")) {
                result = true;
            } else if (str.startsWith("[") && str.endsWith("]")) {
                result = true;
            }
        }
        return result;
    }
/*    private static void iterator(JsonObject jsonObject) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonElement value = entry.getValue();
            String key = entry.getKey();
            if (value.isJsonArray()) {
                JsonArray jsonArray = value.getAsJsonArray();
                // 数组长度为0时将其处理,防止Gson转换异常
                if (jsonArray.size() == 0) {
                    entry.setValue(null);
                } else {
                    for (JsonElement o : jsonArray) {
                        JsonObject asJsonObject = o.getAsJsonObject();
                        iterator(asJsonObject);
                    }
                }
            }
            if (value.isJsonObject()) {
                JsonObject asJsonObject = value.getAsJsonObject();
                iterator(asJsonObject);
            }
        }
    }*/

    public static JsonObject UpdateJsonValue(String Findkey,String payload,JsonObject jsonObject) {
        JsonObject ret = new JsonObject();
        ret = jsonObject;
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonElement value = entry.getValue();


            if(value.isJsonPrimitive())
            {

                String key = entry.getKey();
                JsonElement v = entry.getValue();
                System.out.println(key);
                System.out.println(v);
                if(key.equals(Findkey))
                {
                    //JsonElement jsonElement = JsonObject.get();
                    JsonPrimitive a = new JsonPrimitive(payload);
                    entry.setValue(a);
                    //System.out.println(jsonObject);
                    return ret;
                }
                System.out.println(v.getAsString().equals("123456789012"));
            }
            if (value.isJsonArray()) {
                JsonArray jsonArray = value.getAsJsonArray();
                // 数组长度为0时将其处理,防止Gson转换异常
                if (jsonArray.size() == 0) {
                    entry.setValue(null);
                } else {
                    for (JsonElement o : jsonArray) {
                        JsonObject asJsonObject = o.getAsJsonObject();
                        asJsonObject = UpdateJsonValue(Findkey,payload,asJsonObject);
                    }
                }
            }
            if (value.isJsonObject()) {
                JsonObject asJsonObject = value.getAsJsonObject();
                asJsonObject = UpdateJsonValue(Findkey,payload,asJsonObject);

            }
        }
        return  ret;
    }

}
