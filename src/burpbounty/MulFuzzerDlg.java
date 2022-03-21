package burpbounty;

import burp.IBurpExtenderCallbacks;
import burp.IHttpRequestResponse;

import com.google.gson.JsonArray;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 配置窗口类，负责显示配置窗口，处理窗口消息
 */
public class MulFuzzerDlg extends JFrame {
    private final JPanel mainPanel = new JPanel();
    private final JPanel centerPanel = new JPanel();
    private final JPanel topPanel = new JPanel();;
    public  JTabbedPane TablePanel = new JTabbedPane();
    public static JTextArea inputTextArea;
    public static JTextArea outputTextArea;
    private Tags tagui;
    private IBurpExtenderCallbacks callbacks;
    public ThreadPoolExecutor executor;
    private final JTextField filerText = new JTextField(20);
    private final JButton btStart = new JButton("Start Fuzz");
    private final JButton btStop = new JButton("Stop Fuzz");
    private final JButton btShowIssue = new JButton("Show Issue");
    private boolean isShowIssue = true;
    private IHttpRequestResponse baseRequestResponse;
    private int[] selectedIndex;
    private JsonArray activeProfiles;
    private BurpBountyExtension bbe;
    private String DefalutProfile;


    public MulFuzzerDlg(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.DefalutProfile = "[{\"Name\":\"Payloads\",\"Enabled\":true,\"Scanner\":1,\"Author\":\"test\",\"Payloads\":[\"123456\"],\"Encoder\":[],\"UrlEncode\":false,\"CharsToUrlEncode\":\"\",\"Grep\":[],\"Tags\":[\"All\"],\"PayloadResponse\":false,\"NotResponse\":false,\"TimeOut\":\"\",\"isTime\":false,\"contentLength\":\"\",\"iscontentLength\":false,\"CaseSensitive\":false,\"ExcludeHTTP\":false,\"OnlyHTTP\":false,\"IsContentType\":false,\"ContentType\":\"\",\"NegativeCT\":false,\"IsResponseCode\":false,\"ResponseCode\":\"\",\"NegativeRC\":false,\"isurlextension\":false,\"NegativeUrlExtension\":false,\"MatchType\":0,\"RedirType\":0,\"MaxRedir\":0,\"payloadPosition\":2,\"payloadsFile\":\"\",\"grepsFile\":\"\",\"IssueName\":\"\",\"IssueSeverity\":\"\",\"IssueConfidence\":\"\",\"IssueDetail\":\"\",\"RemediationDetail\":\"\",\"IssueBackground\":\"\",\"RemediationBackground\":\"\",\"Header\":[],\"VariationAttributes\":[],\"Scantype\":0,\"pathDiscovery\":false}]";
        initGUI();
        initEvent();
        initValue();
        this.setTitle("BurpBountyPlus Fuzzer by flystart");
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Config.get_threadpool_threadnum());

    }

    /**
     * 初始化UI
     */
    public Tags get_dlg_tags(){
        return this.tagui;
    }

    public void setScanParams(BurpBountyExtension bbe,IHttpRequestResponse baseRequestResponse, int[] selectedIndex,JsonArray allprofiles)
    {
        this.activeProfiles = allprofiles;
        this.baseRequestResponse = baseRequestResponse;
        this.selectedIndex = selectedIndex;
        this.bbe = bbe;
    }
    public void fuzzer_scan(String url,JsonArray activeProfiles){

        this.bbe.doMultiFuzzerScan(baseRequestResponse,selectedIndex,activeProfiles,activeProfiles,this.tagui,this.executor,url);
    }
    private void initGUI(){
        centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        tagui =new Tags(callbacks, "Fuzzer", TablePanel);
        centerPanel.add(TablePanel);
        topPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.weighty = 1;

        topPanel.add(new JLabel(" Filter: "), gbc);

        gbc.gridx = 1;
        gbc.weightx = 99.0;
        topPanel.add(filerText, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        topPanel.add(btShowIssue, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0;
        topPanel.add(btStart, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0;
        topPanel.add(btStop, gbc);

        JSplitPane CenterSplitPane = new JSplitPane();//中间的大模块，一分为二
        CenterSplitPane.setResizeWeight(0.2);

        JSplitPane LeftOfCenter = new JSplitPane();
        LeftOfCenter.setResizeWeight(0.5);
        CenterSplitPane.setLeftComponent(LeftOfCenter);

        JSplitPane RightOfCenter = new JSplitPane();
       // RightOfCenter.setOrientation(JSplitPane.VERTICAL_SPLIT);
        RightOfCenter.setResizeWeight(0.5);
        CenterSplitPane.setRightComponent(RightOfCenter);

        JScrollPane oneFourthPanel = new JScrollPane();

        inputTextArea = new JTextArea();
        inputTextArea.setLineWrap(true);
        oneFourthPanel.setViewportView(inputTextArea);

        Border blackline = BorderFactory.createLineBorder(Color.black);

        JLabel lblNewLabel_1 = new JLabel("Urls:");
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel_1.setBorder(blackline);
        oneFourthPanel.setColumnHeaderView(lblNewLabel_1);


        LeftOfCenter.setLayout(new BorderLayout());
        LeftOfCenter.add(oneFourthPanel,BorderLayout.CENTER);
        RightOfCenter.setLayout(new BorderLayout());
        RightOfCenter.add(TablePanel,BorderLayout.CENTER);


        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(topPanel,BorderLayout.NORTH);
        mainPanel.add(CenterSplitPane,BorderLayout.CENTER);


        Dimension screensize=Toolkit.getDefaultToolkit().getScreenSize();
        //this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(screensize.width/6*5,screensize.height-40);
        this.setVisible(true);
        this.add(mainPanel);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                //do something
                try {
                    executor.shutdownNow();

                } catch (Exception ex) {
                    callbacks.printError(ex.getMessage());
                }
            }});

        //使配置窗口自动适应控件大小，防止部分控件无法显示
        //this.pack();
        //居中显示配置窗口
        this.setBounds(screensize.width/2-this.getWidth()/2,screensize.height/2-this.getHeight()/2,this.getWidth(),this.getHeight());
    }


    /**
     * 初始化事件
     */
    private void initEvent(){

        filerText.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String value = filerText.getText();
                    tagui.setFilterText(value);
                    tagui.filerTable();

                }
            }
            public void keyReleased(KeyEvent e) {
            }
            public void keyTyped(KeyEvent e) {
            }
        });
        btShowIssue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //FuzzerDlg.this.dispose();
                if(isShowIssue == true)
                {
                    filerText.setText("H");
                    tagui.setFilterText("H");
                    tagui.filerTableColor();
                    isShowIssue = false;
                }else {
                    filerText.setText("");
                    tagui.setFilterText("");
                    tagui.filerTable();
                    isShowIssue = true;
                }
            }
        });

        btStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(new Runnable() {
                    public void run() {

                        try {

                            List<String> urls = Arrays.asList(inputTextArea.getText().replaceAll("\r\n", "\n").split("\n"));
                            for(String url:urls)
                            {
                                if (url.substring(0,4).toLowerCase().startsWith("http")) {
                                    fuzzer_scan(url, activeProfiles);
                                }
                            }

                            /*
                            List<String> payloads = Arrays.asList(outputTextArea.getText().replaceAll("\r\n", "\n").split("\n"));

                            if(payloads.size()>0)
                            {   String tmp = String.join("\",\"", payloads);
                                DefalutProfile = DefalutProfile.replaceAll("123456",tmp);
                                JsonParser jsonParser=new JsonParser();
                                JsonArray jsonArray=jsonParser.parse(DefalutProfile).getAsJsonArray();
                                for(String url:urls)
                                {
                                    if (url.substring(0,4).toLowerCase().startsWith("http")) {
                                        fuzzer_scan(url, jsonArray);
                                    }
                                }

                            }
                            */
                        }catch (Exception ex) {
                            callbacks.printError(ex.getMessage());
                        }
                    }
                });
            }
        });

        btStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //executor.shutdownNow();
                    Config.set_run_status(true);

                } catch (Exception ex) {
                    callbacks.printError(ex.getMessage());
                }
            }
        });
    }


    /**
     * 为控件赋值
     */
    public void initValue(){

    }
}