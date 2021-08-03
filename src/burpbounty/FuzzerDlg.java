package burpbounty;

import burp.IBurpExtenderCallbacks;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JTabbedPane;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 配置窗口类，负责显示配置窗口，处理窗口消息
 */
public class FuzzerDlg extends JFrame {
    private final JPanel mainPanel = new JPanel();
    private final JPanel centerPanel = new JPanel();
    private final JPanel topPanel = new JPanel();;
    public  JTabbedPane TablePanel = new JTabbedPane();
    private Tags tagui;
    private IBurpExtenderCallbacks callbacks;
    public ThreadPoolExecutor executor;
    private final JTextField filerText = new JTextField(20);
    private final JButton btStop = new JButton("Stop Fuzz");
    private final JButton btCancel = new JButton("Cancel");



    public FuzzerDlg(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        initGUI();
        initEvent();
        initValue();
        this.setTitle("BurpBountyPlus Fuzzer");
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Config.get_threadpool_threadnum());

    }

    /**
     * 初始化UI
     */
    public Tags get_dlg_tags(){
        return this.tagui;
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
        topPanel.add(btStop, gbc);


        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(topPanel,BorderLayout.NORTH);
        mainPanel.add(TablePanel,BorderLayout.CENTER);


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

                   tagui.filerTable(value);

                }
            }
            public void keyReleased(KeyEvent e) {
            }
            public void keyTyped(KeyEvent e) {
            }
        });
        btCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //FuzzerDlg.this.dispose();
            }
        });

        btStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    executor.shutdownNow();

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