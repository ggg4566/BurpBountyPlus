/*
Copyright 2018 Eduardo Garcia Melia <wagiro@gmail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package burpbounty;

import burp.*;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import sun.nio.cs.ext.MacArabic;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import javax.management.ListenerNotFoundException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ThreadPoolExecutor;


public class BurpBountyExtension implements IBurpExtender, ITab, IScannerCheck,IExtensionStateListener, IScannerInsertionPointProvider, IContextMenuFactory{

    public BurpBountyExtension burpExt;
    public static IBurpExtenderCallbacks callbacks;
    public IExtensionHelpers helpers;
    List<IBurpCollaboratorClientContext> CollaboratorClientContext;
    private JScrollPane optionsTab;
    private BurpBountyGui panel;
    ProfilesProperties issue;
    BurpCollaboratorThread BurpCollaborator;
    BurpCollaboratorThread bct;
    CollaboratorData burpCollaboratorData;
    List<byte[]> responses;
    List<String> params;
    Gson gson;
    int scanner;
    JsonArray profiles;
    FuzzerDlg dlg;
    private ExecutorService executorService;
    public Tags tagui;
    public CustomHelpers customTools;
    List<String> IgnoreParams =new ArrayList<String>();

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        this.burpExt = this;
        callbacks.setExtensionName("BurpBountyPlus");
        callbacks.registerContextMenuFactory(this);
        executorService = Executors.newSingleThreadExecutor();
        responses = new ArrayList();
        params = new ArrayList();
        gson = new Gson();
        callbacks.registerScannerCheck(this);
        callbacks.registerExtensionStateListener(this);
        callbacks.registerScannerInsertionPointProvider(this);
        CollaboratorClientContext = new ArrayList();
        burpCollaboratorData = new CollaboratorData(helpers);
        Config.init_ignore_param();
        bct = new BurpCollaboratorThread(callbacks, burpCollaboratorData);
        bct.start();


        SwingUtilities.invokeLater(() -> {
            panel = new BurpBountyGui(this);
            optionsTab = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            optionsTab.setPreferredSize(new Dimension(600, 600));
            optionsTab.getVerticalScrollBar().setUnitIncrement(20);

            callbacks.addSuiteTab(this);

            callbacks.printOutput("- BurpBountyPlus v1.0");
            callbacks.printOutput("- For bugs please on the official github: https://github.com/wagiro/BurpBounty/");
            callbacks.printOutput("- For bugs please on the official github: https://github.com/ggg4566/BurpBountyPlus");
            callbacks.printOutput("- Created by Eduardo Garcia Melia <wagiro@gmail.com>,flystart <root@flystart.org>");
        });

    }

    public JsonArray getProfiles() {
        FileReader fr;

        try {
            JsonArray data = new JsonArray();
            File f = new File(panel.profiles_directory);
            if (f.exists() && f.isDirectory()) {
                for (File file : f.listFiles()) {
                    if (file.getName().endsWith(".bb")) {
                        fr = new FileReader(file.getAbsolutePath());
                        JsonReader json = new JsonReader((fr));
                        JsonParser parser = new JsonParser();
                        data.addAll(parser.parse(json).getAsJsonArray());
                        fr.close();
                    }

                }
            }
            return data;
        } catch (Exception e) {
            callbacks.printError("BurpBountyGui line 1823:" + e.getMessage());
            return null;
        }
    }

    @Override
    public void extensionUnloaded() {
        bct.doStop();
        callbacks.printOutput("- Burp Bounty extension was unloaded");
    }

    @Override
    public List<IScannerInsertionPoint> getInsertionPoints(IHttpRequestResponse baseRequestResponse) {
        List<IScannerInsertionPoint> insertionPoints = new ArrayList();

        try {
            IRequestInfo request = helpers.analyzeRequest(baseRequestResponse);

            if (request.getMethod().equals("GET")) {
                String url = request.getUrl().getHost();
                byte[] match = helpers.stringToBytes("/");
                byte[] req = baseRequestResponse.getRequest();
                int len = helpers.bytesToString(baseRequestResponse.getRequest()).indexOf("HTTP");
                int beginAt = 0;

                while (beginAt < len) {
                    beginAt = helpers.indexOf(req, match, false, beginAt, len);
                    if (beginAt == -1) {
                        break;
                    }
                    if (!params.contains(url + ":p4r4m" + beginAt)) {
                        insertionPoints.add(helpers.makeScannerInsertionPoint("p4r4m" + beginAt, baseRequestResponse.getRequest(), beginAt, helpers.bytesToString(baseRequestResponse.getRequest()).indexOf(" HTTP")));
                        params.add(url + ":p4r4m" + beginAt);
                    }
                    beginAt += match.length;
                }
            }
        } catch (NullPointerException e) {
            return insertionPoints;
        }
        return insertionPoints;
    }

    public void SubmitParamJson(JsonObject jsonObject,byte[] request,List<IScannerInsertionPoint> ScanInsertPoint, String ParamName,String ParamValue,byte type,byte INS)
    {

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonElement value = entry.getValue();

            if(value.isJsonPrimitive())
            {

                String key = entry.getKey();
                JsonElement v = entry.getValue();
                System.out.println(key);
                System.out.println(v);

                ScanInsertPoint.add(new InsertionPoint(burpExt, request, key, v.getAsString(), type,INS,ParamName,ParamValue));
            }
            if (value.isJsonArray()) {
                JsonArray jsonArray = value.getAsJsonArray();
                // 数组长度为0时将其处理,防止Gson转换异常
                if (jsonArray.size() == 0) {
                    entry.setValue(null);
                } else {
                    for (JsonElement o : jsonArray) {
                        JsonObject asJsonObject = o.getAsJsonObject();
                        SubmitParamJson(asJsonObject,request,ScanInsertPoint, ParamName,ParamValue,type,INS);
                    }
                }
            }
            if (value.isJsonObject()) {
                JsonObject asJsonObject = value.getAsJsonObject();
                SubmitParamJson(asJsonObject,request,ScanInsertPoint, ParamName,ParamValue,type,INS);

            }
        }

    }
    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {

        List<JMenuItem> menus_list = new ArrayList();
        List<String> GetParam = new ArrayList<>();
        List<String> PostParam = new ArrayList<>();
        List<String> CustomFuzzItem = new ArrayList<>();
        Map<String, String>  GetParamDics =  new HashMap<String, String>();
        Map<String, String>  PostParamDics =  new HashMap<String, String>();

        Config.set_run_status(false);
        String[] array= Config.get_ignore_param().split("\n");
        IgnoreParams = Arrays.asList(array);
        Integer len = IgnoreParams.size();
        for(Integer i=0;i<len;i++)
        {
            String v = IgnoreParams.get(i);
            v = v.trim().replaceAll("(\\s|\\t)", "");
            IgnoreParams.set(i,v);
        }
        final IHttpRequestResponse[] messages = invocation.getSelectedMessages();
        if(messages.length<=0)
        {
            callbacks.printError("IHttpRequestResponse getSelectedMessages is Empty!");
            return new ArrayList<>();
        }
        byte[] request = messages[0].getRequest();
        for (final IHttpRequestResponse message : messages) {
            IRequestInfo analyzeRequest = helpers.analyzeRequest(message);
            List<IParameter> paraList = analyzeRequest.getParameters();
            for (IParameter para : paraList) {
                String key = para.getName();
                String value = para.getValue();
                byte type = para.getType();
                if(type == 0)
                {
                    GetParam.add(key);
                    GetParamDics.put(key,value);
                }
                if(type == 1)
                {
                    PostParam.add(key);
                    PostParamDics.put(key,value);
                }
            }
        }


        JMenu MainMenu = new JMenu("Send to Bounty");
        JMenu GetMenu = new JMenu("Scan GET Param");
        JMenu PostMenu = new JMenu("Scan POST Param");
        JMenuItem i1 = new JMenuItem("Scan All Param");
        JMenuItem config = new JMenuItem("Config");
        JMenuItem stopScan = new JMenuItem("Stop Scan");
        JMenuItem fuzzScan = new JMenuItem("Fuzzer Scan");
        JMenu FuzzGetMenu = new JMenu("Fuzz GET Param");
        JMenu FuzzPostMenu = new JMenu("Fuzz POST Param");
        JMenu ComstomFuzzMenu = new JMenu("Insert Fuzz Scan");
        List<IScannerInsertionPoint> ScanInsertPoint = new ArrayList<IScannerInsertionPoint>();

        JsonArray allprofiles = getProfiles();
        JsonArray activeprofiles = new JsonArray();

        Global.InitProfileColor(allprofiles);

        try {
            for (int i = 0; i < allprofiles.size(); i++) {
                Object idata = allprofiles.get(i);
                issue = gson.fromJson(idata.toString(), ProfilesProperties.class);
                scanner = issue.getScanner();
                String profile=issue.getName();
                //String testName = issue.getIssueName();
                CustomFuzzItem.add(profile);
                if (scanner == 1 && issue.getEnabled()) {
                    activeprofiles.add(allprofiles.get(i));
                }
            }
            if (activeprofiles.size() == 0) {
                return null;
            }

        } catch (Exception ex) {
            callbacks.printError("BurpBountyExtension line 286: " + ex.getMessage());
        }


        i1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (final IHttpRequestResponse message : messages) {
                    executorService.submit(new Runnable() {
                      public void run() {
                            //callbacks.printOutput("Run");
                            IRequestInfo analyzeRequest = helpers.analyzeRequest(message);
                            List<IParameter> paraList = analyzeRequest.getParameters();
                            byte[] request = message.getRequest();
                            for (IParameter para : paraList){
                                String key = para.getName();
                                if(IgnoreParams.contains(key))
                                {
                                    continue;
                                }
                                String value = para.getValue();
                                byte type = para.getType();

                                /*for (int i = 0; i < activeprofiles.size(); i++) {
                                    Object idata = activeprofiles.get(i);
                                    ProfilesProperties profile_property = gson.fromJson(idata.toString(), ProfilesProperties.class);
                                    List<Integer> insertionPointType =profile_property.getInsertionPointType();
                                    if(insertionPointType.contains(type&0xff))
                                    {
                                        ScanInsertPoint.add(new InsertionPoint(burpExt,message.getRequest(),key,value,type));
                                    }
                                }*/
                                if(type == 0 || type == 1) // param or body
                                {
                                    byte INS = type;
                                    if (!value.isEmpty()&&customTools.isJson(helpers.urlDecode(value))) {
                                        byte retype = 6;
                                        try{
                                            JsonObject jsonObject = new JsonParser().parse(value).getAsJsonObject();
                                            SubmitParamJson(jsonObject, request, ScanInsertPoint, key, value, retype, INS);
                                        } catch (Exception e){
                                            callbacks.printError(e.getMessage());
                                            ScanInsertPoint.add(new InsertionPoint(burpExt, request, key, value, type, INS, "", ""));
                                        }
                                    } else {
                                        ScanInsertPoint.add(new InsertionPoint(burpExt, message.getRequest(), key, value, type,INS,"",""));
                                    }
                                }
                                if(type == 6)
                                {
                                    byte INS= 6;
                                    ScanInsertPoint.add(new InsertionPoint(burpExt,message.getRequest(),key,value,type,INS,"",""));
                                }

                            }
                            //doActiveScan(message,a);
                          int nSize = ScanInsertPoint.size();
                        if(nSize >0) {
                            for (IScannerInsertionPoint Point : ScanInsertPoint) {

                                if(Config.get_run_status()==true)
                                {
                                    break;
                                }
                                List<IScanIssue> issues = doMyScan(message, Point, allprofiles, activeprofiles);
                            }
                        }

                        }
                    });



                }
            }
        });

        stopScan.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                Config.set_run_status(true);
            }
        });

        config.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                try {
                    ConfigDlg dlg = new ConfigDlg();
                    dlg.setVisible(true);
                    BurpExtender.callbacks.customizeUiComponent(dlg);

                }catch (Exception e){
                    callbacks.printError(e.getMessage());
                }
            }
        });
        fuzzScan.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {

                try {
                    dlg = new FuzzerDlg(callbacks);
                }catch (Exception e){
                    callbacks.printError(e.getMessage());
                }
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                for (final IHttpRequestResponse message : messages) {
                    executorService.submit(new Runnable() {
                        public void run() {
                            //callbacks.printOutput("Run");
                            IRequestInfo analyzeRequest = helpers.analyzeRequest(message);
                            List<IParameter> paraList = analyzeRequest.getParameters();
                            byte[] request = message.getRequest();
                            for (IParameter para : paraList){
                                String key = para.getName();
                                if(IgnoreParams.contains(key))
                                {
                                    continue;
                                }
                                String value = para.getValue();
                                byte type = para.getType();

                                /*for (int i = 0; i < activeprofiles.size(); i++) {
                                    Object idata = activeprofiles.get(i);
                                    ProfilesProperties profile_property = gson.fromJson(idata.toString(), ProfilesProperties.class);
                                    List<Integer> insertionPointType =profile_property.getInsertionPointType();
                                    if(insertionPointType.contains(type&0xff))
                                    {
                                        ScanInsertPoint.add(new InsertionPoint(burpExt,message.getRequest(),key,value,type));
                                    }
                                }*/
                                if(type == 0 || type == 1) // param or body
                                {
                                    byte INS = type;
                                    if (!value.isEmpty()&&customTools.isJson(helpers.urlDecode(value))) {
                                        byte retype = 6;
                                        try{
                                            JsonObject jsonObject = new JsonParser().parse(value).getAsJsonObject();
                                            SubmitParamJson(jsonObject, request, ScanInsertPoint, key, value, retype, INS);
                                        } catch (Exception e){
                                            callbacks.printError(e.getMessage());
                                            ScanInsertPoint.add(new InsertionPoint(burpExt, request, key, value, type, INS, "", ""));
                                        }

                                    } else {
                                        ScanInsertPoint.add(new InsertionPoint(burpExt, message.getRequest(), key, value, type,INS,"",""));
                                    }
                                }
                                if(type == 6)
                                {
                                    byte INS= 6;
                                    ScanInsertPoint.add(new InsertionPoint(burpExt,message.getRequest(),key,value,type,INS,"",""));
                                }

                            }
                            //doActiveScan(message,a);
                            int nSize = ScanInsertPoint.size();
                            if(nSize >0) {
                                for (IScannerInsertionPoint Point : ScanInsertPoint) {

                                    if(Config.get_run_status()==true)
                                    {
                                        break;
                                    }
                                    Tags tag = dlg.get_dlg_tags();
                                    List<IScanIssue> issues = doFuzzerScan(message, Point, allprofiles, activeprofiles,tag,dlg.executor);
                                }
                            }
                        // waitfor end
                            //callbacks.printOutput("thread end!");
                        }
                    });



                }
            }
        });

        for(String ProfileName: CustomFuzzItem)
        {
            JMenuItem tmp = new JMenuItem(ProfileName);
            ComstomFuzzMenu.add(tmp);
            tmp.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent arg0) {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.submit(new Runnable() {
                        public void run() {
                            try {
                                dlg = new FuzzerDlg(callbacks);
                            }catch (Exception e){
                                callbacks.printError(e.getMessage());
                            }

                            try {
                                // do something
                                String key = arg0.getActionCommand();
                                Map<String, JsonElement>  FuzzProfile =  new HashMap<String,JsonElement>();
                                JsonArray comstomprilfe =  new JsonArray();
                                for (int i = 0; i < allprofiles.size(); i++) {
                                    Object idata = allprofiles.get(i);
                                    issue = gson.fromJson(idata.toString(), ProfilesProperties.class);
                                    String profile=issue.getName();
                                    FuzzProfile.put(profile,allprofiles.get(i));

                                }
                                JsonElement value = FuzzProfile.get(key);
                                comstomprilfe.add(value);
                                Tags tag = dlg.get_dlg_tags();
                                int[] selectedIndex = invocation.getSelectionBounds();
                                doCostomFuzzerScan(messages[0],selectedIndex,allprofiles,comstomprilfe,tag,dlg.executor);
                                //List<IScanIssue> issues = doFuzzerScan(messages[0], Point, allprofiles, comstomprilfe,tag,dlg.executor);

                            }catch (Exception e){
                                callbacks.printError(e.getMessage());
                            }
                        }
                    });

                }
            });
        }

        if(!GetParam.isEmpty())
        {
            for(String key: GetParam)
            {
                JMenuItem tmp = new JMenuItem(key);
                JMenuItem GetFuzzerItem = new JMenuItem(key);
                GetMenu.add(tmp);
                FuzzGetMenu.add(GetFuzzerItem);
                tmp.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent arg0) {
                        executorService.submit(new Runnable() {
                            public void run() {
                                try {
                                    // do something
                                    String key = arg0.getActionCommand();
                                    String value = "";
                                    value = GetParamDics.get(key);
                                    byte type = 0;
                                    if(type == 0 || type == 1) // param or body
                                    {
                                        byte INS = type;
                                        if (!value.isEmpty()&&customTools.isJson(helpers.urlDecode(value))) {
                                            byte retype = 6;
                                            try{
                                                JsonObject jsonObject = new JsonParser().parse(value).getAsJsonObject();
                                                SubmitParamJson(jsonObject, request, ScanInsertPoint, key, value, retype, INS);
                                            } catch (Exception e){
                                                callbacks.printError(e.getMessage());
                                                ScanInsertPoint.add(new InsertionPoint(burpExt, request, key, value, type, INS, "", ""));
                                            }

                                        } else {
                                            ScanInsertPoint.add(new InsertionPoint(burpExt, request, key, value, type,INS,"",""));
                                        }
                                    }
                                    if(type == 6)
                                    {
                                        byte INS= 6;
                                        ScanInsertPoint.add(new InsertionPoint(burpExt,request,key,value,type,INS,"",""));
                                    }
                                    if(ScanInsertPoint.size()>0)
                                    {
                                        for (IScannerInsertionPoint Point : ScanInsertPoint) {
                                            if(Config.get_run_status()==true)
                                            {
                                                break;
                                            }
                                            List<IScanIssue> issues = doMyScan(messages[0], Point, allprofiles, activeprofiles);
                                        }
                                    }

                                }catch (Exception e){
                                    callbacks.printError(e.getMessage());
                                }
                            }
                        });

                    }
                });

                GetFuzzerItem.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent arg0) {
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.submit(new Runnable() {
                            public void run() {
                                try {
                                    dlg = new FuzzerDlg(callbacks);
                                }catch (Exception e){
                                    callbacks.printError(e.getMessage());
                                }

                                try {
                                    // do something
                                    String key = arg0.getActionCommand();
                                    String value = "";
                                    value = GetParamDics.get(key);
                                    byte type = 0;
                                    if(type == 0 || type == 1) // param or body
                                    {
                                        byte INS = type;
                                        if (!value.isEmpty()&&customTools.isJson(helpers.urlDecode(value))) {
                                            byte retype = 6;
                                            try{
                                                JsonObject jsonObject = new JsonParser().parse(value).getAsJsonObject();
                                                SubmitParamJson(jsonObject, request, ScanInsertPoint, key, value, retype, INS);
                                            } catch (Exception e){
                                                callbacks.printError(e.getMessage());
                                                ScanInsertPoint.add(new InsertionPoint(burpExt, request, key, value, type, INS, "", ""));
                                            }

                                        } else {
                                            ScanInsertPoint.add(new InsertionPoint(burpExt, request, key, value, type,INS,"",""));
                                        }
                                    }
                                    if(type == 6)
                                    {
                                        byte INS= 6;
                                        ScanInsertPoint.add(new InsertionPoint(burpExt,request,key,value,type,INS,"",""));
                                    }
                                    if(ScanInsertPoint.size()>0)
                                    {
                                        for (IScannerInsertionPoint Point : ScanInsertPoint) {
                                            if(Config.get_run_status()==true)
                                            {
                                                break;
                                            }
                                            Tags tag = dlg.get_dlg_tags();
                                            List<IScanIssue> issues = doFuzzerScan(messages[0], Point, allprofiles, activeprofiles,tag,dlg.executor);
                                        }
                                    }

                                }catch (Exception e){
                                    callbacks.printError(e.getMessage());
                                }
                            }
                        });

                    }
                });
            }
        }
        if(!PostParam.isEmpty())
        {
            for(String key: PostParam)
            {
                JMenuItem tmp = new JMenuItem(key);
                JMenuItem PostFuzzerItem = new JMenuItem(key);
                PostMenu.add(tmp);
                FuzzPostMenu.add(PostFuzzerItem);
                tmp.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent arg0) {
                        executorService.submit(new Runnable() {
                            public void run() {
                                try {
                                    // do something
                                    String key = arg0.getActionCommand();
                                    String value = "";
                                    value = PostParamDics.get(key);
                                    byte type = 1;
                                    if(type == 0 || type == 1) // param or body
                                    {
                                        byte INS = type;

                                        if (!value.isEmpty()&&customTools.isJson(helpers.urlDecode(value))) {
                                            byte retype = 6;
                                            try{
                                                JsonObject jsonObject = new JsonParser().parse(value).getAsJsonObject();
                                                SubmitParamJson(jsonObject, request, ScanInsertPoint, key, value, retype, INS);
                                            } catch (Exception e){
                                                callbacks.printError(e.getMessage());
                                                ScanInsertPoint.add(new InsertionPoint(burpExt, request, key, value, type, INS, "", ""));
                                            }

                                        } else {
                                            ScanInsertPoint.add(new InsertionPoint(burpExt, request, key, value, type, INS, "", ""));
                                        }

                                    }
                                    if(type == 6)
                                    {
                                        byte INS= 6;
                                        ScanInsertPoint.add(new InsertionPoint(burpExt,request,key,value,type,INS,"",""));
                                    }
                                    if(ScanInsertPoint.size()>0)
                                    {
                                        for (IScannerInsertionPoint Point : ScanInsertPoint) {
                                            if(Config.get_run_status()==true)
                                            {
                                                break;
                                            }
                                            List<IScanIssue> issues = doMyScan(messages[0], Point, allprofiles, activeprofiles);
                                        }
                                    }

                                }catch (Exception e){
                                    callbacks.printError(e.getMessage());
                                }
                            }
                        });
                    }
                });

                PostFuzzerItem.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent arg0) {
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.submit(new Runnable() {
                            public void run() {
                                try {
                                    dlg = new FuzzerDlg(callbacks);
                                }catch (Exception e){
                                    callbacks.printError(e.getMessage());
                                }

                                try {
                                    // do something
                                    String key = arg0.getActionCommand();
                                    String value = "";
                                    value = PostParamDics.get(key);
                                    byte type = 1;
                                    if(type == 0 || type == 1) // param or body
                                    {
                                        byte INS = type;

                                        if (!value.isEmpty()&&customTools.isJson(helpers.urlDecode(value))) {
                                            byte retype = 6;
                                            try{
                                                JsonObject jsonObject = new JsonParser().parse(value).getAsJsonObject();
                                                SubmitParamJson(jsonObject, request, ScanInsertPoint, key, value, retype, INS);
                                            } catch (Exception e){
                                                callbacks.printError(e.getMessage());
                                                ScanInsertPoint.add(new InsertionPoint(burpExt, request, key, value, type, INS, "", ""));
                                            }
                                        } else {
                                            ScanInsertPoint.add(new InsertionPoint(burpExt, request, key, value, type, INS, "", ""));
                                        }

                                    }
                                    if(type == 6)
                                    {
                                        byte INS= 6;
                                        ScanInsertPoint.add(new InsertionPoint(burpExt,request,key,value,type,INS,"",""));
                                    }
                                    if(ScanInsertPoint.size()>0)
                                    {
                                        for (IScannerInsertionPoint Point : ScanInsertPoint) {
                                            if(Config.get_run_status()==true)
                                            {
                                                break;
                                            }
                                            Tags tag = dlg.get_dlg_tags();
                                            List<IScanIssue> issues = doFuzzerScan(messages[0], Point, allprofiles, activeprofiles,tag,dlg.executor);
                                        }
                                    }

                                }catch (Exception e){
                                    callbacks.printError(e.getMessage());
                                }
                            }
                        });
                    }
                });
            }
        }

        MainMenu.add(i1);

        MainMenu.add(GetMenu);
        MainMenu.add(PostMenu);
        MainMenu.addSeparator();
        MainMenu.add(fuzzScan);
        MainMenu.add(FuzzGetMenu);
        MainMenu.add(FuzzPostMenu);
        MainMenu.add(ComstomFuzzMenu);

        MainMenu.addSeparator();
        MainMenu.add(stopScan);
        MainMenu.addSeparator();
        MainMenu.add(config);
        menus_list.add(MainMenu);
        return menus_list;

    }

    @Override
    public List<IScanIssue> doActiveScan(IHttpRequestResponse baseRequestResponse, IScannerInsertionPoint insertionPoint) {
        JsonArray allprofiles = getProfiles();
        JsonArray activeprofiles = new JsonArray();
        params = new ArrayList();
        String paramName =insertionPoint.getInsertionPointName();
        String param = insertionPoint.getBaseValue();
        try {
            for (int i = 0; i < allprofiles.size(); i++) {
                Object idata = allprofiles.get(i);
                issue = gson.fromJson(idata.toString(), ProfilesProperties.class);
                scanner = issue.getScanner();

                if (scanner == 1 && issue.getEnabled() && issue.getInsertionPointType().contains(insertionPoint.getInsertionPointType() & 0xFF)) {
                    activeprofiles.add(allprofiles.get(i));
                }

            }
            if (activeprofiles.size() == 0) {
                return null;
            }

            GenericScan as = new GenericScan(this, callbacks, burpCollaboratorData, panel.getProfilesFilename(), allprofiles);

            IBurpCollaboratorClientContext CollaboratorClientContext2 = callbacks.createBurpCollaboratorClientContext();
            burpCollaboratorData.setCollaboratorClientContext(CollaboratorClientContext2);
            String bchost = CollaboratorClientContext2.generatePayload(true);
            return as.runAScan(baseRequestResponse, insertionPoint, activeprofiles, bchost);
        } catch (Exception ex) {
            callbacks.printError("BurpBountyExtension line 174: " + ex.getMessage());
        }
        return null;
    }

    public void doCostomFuzzerScan(IHttpRequestResponse baseRequestResponse, int[] selectedIndex,JsonArray allprofiles,JsonArray activeprofiles,Tags tag,ThreadPoolExecutor threadpool) {
        try {
            byte[] request = baseRequestResponse.getRequest();
            CustomInsertFuzz as = new CustomInsertFuzz(this, callbacks, panel.getProfilesFilename(), allprofiles, tag,threadpool);
            as.runAScan(baseRequestResponse, activeprofiles,request,selectedIndex);
        } catch (Exception ex) {
            if(ex== null || ex.getMessage() == null) {

            } else {
                callbacks.printError("BurpBountyExtension line 800: " + ex.getMessage());
            }
        }
    }


    public List<IScanIssue> doFuzzerScan(IHttpRequestResponse baseRequestResponse, IScannerInsertionPoint insertionPoint,JsonArray allprofiles,JsonArray activeprofiles,Tags tag,ThreadPoolExecutor threadpool) {
        List<IScanIssue> ret = null;
        try {

            Fuzzer as = new Fuzzer(this, callbacks, burpCollaboratorData, panel.getProfilesFilename(), allprofiles, tag,threadpool);
            IBurpCollaboratorClientContext CollaboratorClientContext2 = callbacks.createBurpCollaboratorClientContext();
            burpCollaboratorData.setCollaboratorClientContext(CollaboratorClientContext2);
            String bchost = CollaboratorClientContext2.generatePayload(true);
            ret= as.runAScan(baseRequestResponse, insertionPoint, activeprofiles, bchost);
        } catch (Exception ex) {
            if(ex== null || ex.getMessage() == null) {

            } else {
                callbacks.printError("BurpBountyExtension line 279: " + ex.getMessage());
            }
        }
        return ret;
    }

    public List<IScanIssue> doMyScan(IHttpRequestResponse baseRequestResponse, IScannerInsertionPoint insertionPoint,JsonArray allprofiles,JsonArray activeprofiles) {
        List<IScanIssue> ret = null;
        try {

            FGenericScan as = new FGenericScan(this, callbacks, burpCollaboratorData, panel.getProfilesFilename(), allprofiles, tagui);

            IBurpCollaboratorClientContext CollaboratorClientContext2 = callbacks.createBurpCollaboratorClientContext();
            burpCollaboratorData.setCollaboratorClientContext(CollaboratorClientContext2);
            String bchost = CollaboratorClientContext2.generatePayload(true);
            ret= as.runAScan(baseRequestResponse, insertionPoint, activeprofiles, bchost);
        } catch (Exception ex) {
            if(ex== null || ex.getMessage() == null) {

            } else {
                callbacks.printError("BurpBountyExtension line 279: " + ex.getMessage());
            }
        }
        return ret;
    }

    @Override
    public List<IScanIssue> doPassiveScan(IHttpRequestResponse baseRequestResponse) {
        JsonArray allprofiles = getProfiles();
        JsonArray passiveresprofiles = new JsonArray();
        JsonArray passivereqprofiles = new JsonArray();
        List<IScanIssue> issues = new ArrayList();

        for (int i = 0; i < allprofiles.size(); i++) {
            Object idata = allprofiles.get(i);
            issue = gson.fromJson(idata.toString(), ProfilesProperties.class);
            scanner = issue.getScanner();
            if (issue.getEnabled() && scanner == 2) {
                passiveresprofiles.add(allprofiles.get(i));
            } else if (issue.getEnabled() && scanner == 3) {
                passivereqprofiles.add(allprofiles.get(i));
            }
        }

        if (passiveresprofiles.size() > 0) {
            GenericScan ps = new GenericScan(this, callbacks, burpCollaboratorData, panel.getProfilesFilename(), allprofiles);

            try {
                IBurpCollaboratorClientContext CollaboratorClientContext2 = callbacks.createBurpCollaboratorClientContext();
                burpCollaboratorData.setCollaboratorClientContext(CollaboratorClientContext2);
                String bchost = CollaboratorClientContext2.generatePayload(true);
                issues.addAll(ps.runResPScan(baseRequestResponse, passiveresprofiles, bchost));
            } catch (Exception ex) {
                callbacks.printError("BurpBountyExtension line 219: " + ex.getMessage());
            }
        }

        if (passivereqprofiles.size() > 0) {
            GenericScan ps = new GenericScan(this, callbacks, burpCollaboratorData, panel.getProfilesFilename(), allprofiles);

            try {
                IBurpCollaboratorClientContext CollaboratorClientContext2 = callbacks.createBurpCollaboratorClientContext();
                burpCollaboratorData.setCollaboratorClientContext(CollaboratorClientContext2);
                String bchost = CollaboratorClientContext2.generatePayload(true);
                issues.addAll(ps.runReqPScan(baseRequestResponse, passivereqprofiles, bchost));
            } catch (Exception ex) {
                callbacks.printError("BurpBountyExtension line 229: " + ex.getMessage());
            }
        }
        return issues;
    }

    @Override
    public int consolidateDuplicateIssues(IScanIssue existingIssue, IScanIssue newIssue) {
        if (existingIssue.getIssueName().equals(newIssue.getIssueName())) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public String getTabCaption() {
        return "BurpBountyPlus";
    }

    @Override
    public Component getUiComponent() {
        //return optionsTab;
        return panel;
    }
}
