package burpbounty;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Global {
    public static Map<String, LightColor>  ProfileColors =  new HashMap<String,LightColor>();
    public static void InitProfileColor(JsonArray allprofiles)
    {
        List AllProfileColor = new ArrayList<>();
        List ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0xff,0xff,0x0ff));
        ColorNode.add(new Color(0xFF,0,0));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0xff,0xff,0xff));
        ColorNode.add(new Color(0x99,0x66,0xff));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0xff,0xff,0xff));
        ColorNode.add(new Color(0x30,0xe1,0xcd));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0xff,0xff,0xff));
        ColorNode.add(new Color(0xBC,0xE3,0x4E));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0xff,0xff,0xff));
        ColorNode.add(new Color(0x1A,0xE3,0x11));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0xff,0xff,0xff));
        ColorNode.add(new Color(0xDD,0x47,0x9e));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0x00,0x00,0x00));
        ColorNode.add(new Color(0xE9,0xC5,0x9E));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0xff,0xff,0xff));
        ColorNode.add(new Color(0xD8,0x62,0xE3));

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0x00,0x00,0x00));
        ColorNode.add(new Color(0xD8,0xE1,0x6B));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0x00,0x00,0x00));
        ColorNode.add(new Color(0xE9,0x68,0x3E));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0x00,0x00,0x00));
        ColorNode.add(new Color(0x5E,0xE0,0xD4));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0x00,0x00,0x00));
        ColorNode.add(new Color(0xE9,0xFB,0xD4));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0x00,0x00,0x00));
        ColorNode.add(new Color(0x3F,0xAD,0xD4));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0x00,0x00,0xf1));
        ColorNode.add(new Color(0x13,0xF0,0xff));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0x00,0x00,0x00));
        ColorNode.add(new Color(0xEF,0xC5,0xE9));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0x00,0x00,0x00));
        ColorNode.add(new Color(0x8C,0xD1,0xAD));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0x00,0x00,0x00));
        ColorNode.add(new Color(0xED,0xD1,0x6A));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0xff,0xff,0xff));
        ColorNode.add(new Color(0xED,0x25,0x6A));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0xff,0xff,0xff));
        ColorNode.add(new Color(0x86,0x25,0x98));
        AllProfileColor.add(ColorNode);

        ColorNode = new ArrayList<>();
        ColorNode.add(new Color(0xff,0xff,0xff));
        ColorNode.add(new Color(0x86,0xBF,0x35));
        AllProfileColor.add(ColorNode);
        for (int i = 0; i < allprofiles.size(); i++) {
            Object idata = allprofiles.get(i);
            Gson gson = new Gson();
            ProfilesProperties issue = gson.fromJson(idata.toString(), ProfilesProperties.class);
            String name = issue.getName();
            List<String> greps = issue.getGreps();
            LightColor rowColor = new LightColor();
            rowColor.ProfileName = name;
            rowColor.greps = greps;
            if(i>AllProfileColor.size()-1)
            {
                rowColor.RowColor = (List<Color>) AllProfileColor.get(0);
            }else
            {
                rowColor.RowColor = (List<Color>) AllProfileColor.get(i);
            }
            Global.ProfileColors.put(name,rowColor);
        }
    }

    public static Map<String, LightColor> getProfileColors()
    {
        return Global.ProfileColors;
    }
}
