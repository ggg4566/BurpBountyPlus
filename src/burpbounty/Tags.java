package burpbounty;

import javax.swing.table.*;
import java.awt.*;
import java.net.URL;
import java.util.*;
import java.text.*;
import java.util.List;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


import burp.*;


public class Tags extends AbstractTableModel implements ITab, IMessageEditorController
{
    public IBurpExtenderCallbacks callbacks;
    public IExtensionHelpers helpers;
    private JSplitPane splitPane;
    private IMessageEditor requestViewer;
    private IMessageEditor responseViewer;
    public Table logTable;
    public final List<LogEntry> log = new ArrayList<LogEntry>();;
    private IHttpRequestResponse currentlyDisplayedItem;
    private String tagName;
    private JScrollPane UscrollPane;
    private JSplitPane HjSplitPane;
    private JTabbedPane Ltable;
    private JTabbedPane Rtable;
    public  List<String> payloads;
    private Map<String, LightColor>  ProfileColors;
    public BurpBountyExtension bbe;
    private boolean IsLight;
    private String filterText="";

    public Tags(final IBurpExtenderCallbacks callbacks, final String tagName, JTabbedPane panel) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        this.tagName = tagName;
        this.payloads = new ArrayList<>();
        ProfileColors =  new HashMap<String, LightColor>();
        IsLight = true;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Tags.this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                Tags.this.logTable = new Table(Tags.this);
                /*
                RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(Tags.this);
                Tags.this.logTable.setRowSorter(sorter);
                */

                TableRowSorter<TableModel> rowSorter = new TableRowSorter(Tags.this);
                Tags.this.logTable.setRowSorter(rowSorter);
                rowSorter.setComparator(0, new Comparator()
                {
                    @Override
                    public int compare(Object arg0, Object arg1)
                    {
                        try{
                            int a = Integer.parseInt(arg0.toString());
                            int b = Integer.parseInt(arg1.toString());
                            return a-b;
                        }catch(NumberFormatException e){
                            return 0;
                        }
                    }
                });


                final JScrollPane scrollPane = new JScrollPane(Tags.this.logTable);

                HjSplitPane = new JSplitPane();

                Ltable = new JTabbedPane();
                Tags.this.requestViewer = Tags.this.callbacks.createMessageEditor(Tags.this,false);
                Ltable.addTab("Request",Tags.this.requestViewer.getComponent());



                Rtable = new JTabbedPane();
                Tags.this.responseViewer = Tags.this.callbacks.createMessageEditor(Tags.this,false);
                Rtable.addTab("Response",Tags.this.responseViewer.getComponent());


                HjSplitPane.setLeftComponent(Ltable);
                HjSplitPane.setRightComponent(Rtable);

                HjSplitPane.setDividerLocation(500);


                Tags.this.splitPane.setTopComponent(scrollPane);
                Tags.this.splitPane.setBottomComponent(HjSplitPane);

                Tags.this.callbacks.customizeUiComponent(splitPane);

                panel.add(tagName, splitPane);

                logTable.getColumnModel().getColumn(0).setPreferredWidth(50);
                logTable.getColumnModel().getColumn(1).setPreferredWidth(1000);
                logTable.getColumnModel().getColumn(2).setPreferredWidth(150);
                logTable.getColumnModel().getColumn(3).setPreferredWidth(200);
                logTable.getColumnModel().getColumn(4).setPreferredWidth(900);
                logTable.getColumnModel().getColumn(5).setPreferredWidth(200);
                logTable.getColumnModel().getColumn(6).setPreferredWidth(350);
            }
        });
    }

    public void setBurpBountyExtension(BurpBountyExtension bbe)
    {
        this.bbe = bbe;
    }
    public LightColor GetProfileColor(String key)
    {
        LightColor ret = Global.ProfileColors.get(key);
        return ret;
    }

    public List getRowFiler(String query)
    {
        List<String> ret = new ArrayList();
        try {
            int RowLines = getRowCount();
            for (int i = 0; i < RowLines; i++) {
                final LogEntry logEntry =this.log.get(i);
                String responseText = new String(logEntry.requestResponse.getResponse());

                if(responseText.toUpperCase().contains(query.toUpperCase()))
                {
                    ret.add(logEntry.id);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public List getRowFiler()
    {
        List<String> ret = new ArrayList();
        try {
            int RowLines = getRowCount();
            for (int i = 0; i < RowLines; i++) {
                final LogEntry logEntry =this.log.get(i);
                if(logEntry.isIsssue == true)
                {
                    ret.add(logEntry.id);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public void filerTableColor()
    {
            TableRowSorter<TableModel> rowSorter = new TableRowSorter(Tags.this);
            Tags.this.logTable.setRowSorter(rowSorter);
            ArrayList<RowFilter<Object, Number>> response_filter_values = new ArrayList<RowFilter<Object, Number>>();
            List<String> showRowIndex = getRowFiler();
            if(!showRowIndex.isEmpty()) {
                RowFilter<Object, Number> filter = new RowFilter<Object, Number>() {
                    @Override
                    public boolean include(RowFilter.Entry entry) {
                        return showRowIndex.contains(entry.getValue(0));
                    }
                };
                response_filter_values.add(filter);
                rowSorter.setRowFilter(RowFilter.orFilter(response_filter_values));
            }
    }

    public void setFilterText(String text)
    {
        this.filterText = text;
    }

    public void filerTable()
    {
        String query = this.filterText;
        TableRowSorter<TableModel> rowSorter = new TableRowSorter(Tags.this);
        Tags.this.logTable.setRowSorter(rowSorter);
        if(query.isEmpty())
        {
            rowSorter.setRowFilter(null);
        }else
        {
            ArrayList<RowFilter<Object,Object>> status_filter_values = new ArrayList<RowFilter<Object,Object>>();
            ArrayList<RowFilter<Object,Number>> response_filter_values = new ArrayList<RowFilter<Object,Number>>();
            status_filter_values.add(RowFilter.regexFilter("^2", 2));
            status_filter_values.add(RowFilter.regexFilter("^3", 2));
            status_filter_values.add(RowFilter.regexFilter("^4", 2));
            status_filter_values.add(RowFilter.regexFilter("^5", 2));

            List<String> showRowIndex =getRowFiler(query);
            RowFilter<Object, Number> filter = new RowFilter<Object, Number>() {
                @Override
                public boolean include(RowFilter.Entry entry) {
                    return showRowIndex.contains(entry.getValue(0));
                }
            };

            response_filter_values.add(filter);
            RowFilter<Object,Object> status_filter;
            RowFilter<Object,Number> response_filter;

            status_filter= RowFilter.orFilter(status_filter_values);
            response_filter = RowFilter.orFilter(response_filter_values);
            ArrayList<RowFilter<Object, Object>> andFilters = new ArrayList<RowFilter<Object, Object>>();
            andFilters.add(status_filter);
            //andFilters.add(response_filter);
            rowSorter.setRowFilter(RowFilter.orFilter(response_filter_values));
        }

        rowSorter.setComparator(0, new Comparator()
        {
            @Override
            public int compare(Object arg0, Object arg1)
            {
                try{
                    int a = Integer.parseInt(arg0.toString());
                    int b = Integer.parseInt(arg1.toString());
                    return a-b;
                }catch(NumberFormatException e){
                    return 0;
                }
            }
        });
    }
    @Override
    public String getTabCaption() {
        return this.tagName;
    }
    public synchronized void clear_find_payloads() {
        this.payloads.clear();
    }

    public  boolean RegexQuery(String query,String BodyString)
    {
        boolean ret= false;
        Pattern p;
        Matcher m;
        try {
            p = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
            m = p.matcher(BodyString);
            if(m.find())
            {
                ret = true;
            }
        } catch (PatternSyntaxException pse) {
            Tags.this.callbacks.printError("Grep Match line 216 Incorrect regex: " + pse.getPattern());
        }
        return ret;
    }



    public void setRowColor()
    {
        JTable table = logTable;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    DefaultTableCellRenderer tcr = new DefaultTableCellRenderer() {

                        public Component getTableCellRendererComponent(JTable table,
                                                                       Object value, boolean isSelected, boolean hasFocus,
                                                                       int row, int column) {
                            LogEntry logEntry = Tags.this.log.get(Tags.this.logTable.convertRowIndexToModel(row));

                            String ProfileName = logEntry.profileName;
                            LightColor ShowColor = GetProfileColor(ProfileName);
                            int matchtype = ShowColor.matchtype;

                            if (!ShowColor.greps.isEmpty()&&Integer.parseInt(logEntry.response_len)>0) {
                                String responseText = new String(logEntry.requestResponse.getResponse());
                                Boolean isFind = false;
                                for (String query : ShowColor.greps) {
                                    String[] tokens = query.split(",", 3);
                                    if(matchtype == 2) {// regex match
                                        if(Tags.this.RegexQuery(tokens[2],responseText))
                                        {
                                            isFind = true;
                                            logEntry.isIsssue =true;
                                            break;

                                        }
                                    }else{// Simple String
                                        if (responseText.toUpperCase().contains(tokens[2].toUpperCase())) {
                                            isFind = true;
                                            logEntry.isIsssue =true;
                                            break;
                                        }
                                    }

                                }


                                if (isFind) {
                                    setBackground(ShowColor.RowColor.get(1));
                                    setForeground(ShowColor.RowColor.get(0));
                                } else {
                                    setBackground(new Color(0xFF, 0xFF, 0xFF));
                                    setForeground(new Color(0, 0, 0));
                                }
                            }else
                            {
                                setBackground(new Color(0xFF, 0xFF, 0xFF));
                               setForeground(new Color(0,0,0));
                            }
                            return super.getTableCellRendererComponent(table, value,
                                    isSelected, hasFocus, row, column);
                        }
                    };
                    int columnCount = table.getColumnCount();
                    for (int i = 0; i < columnCount; i++) {
                        table.getColumn(table.getColumnName(i)).setCellRenderer(tcr);
                    }
                } catch (
                        Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }
    public  void setBackgroundColor(Color color)
    {
        JTable table = logTable;
//        try {
//            DefaultTableCellRenderer tcr = new DefaultTableCellRenderer() {
//
//                public Component getTableCellRendererComponent(JTable table,
//                                                               Object value, boolean isSelected, boolean hasFocus,
//                                                               int row, int column) {
//                    final LogEntry logEntry =Tags.this.log.get(Tags.this.logTable.convertRowIndexToModel(row));
//                    if (payloads.contains(logEntry.payload)) {
//                        setBackground(color);
//                        setForeground(new Color(0xFF, 0xFF, 0xFF));
//                    } else {
//                        setBackground(new Color(0, 102, 255));
//                        setForeground(new Color(0xFF, 0xFF, 0xFF));
//                    }
//                    return super.getTableCellRendererComponent(table, value,
//                            isSelected, hasFocus, row, column);
//                }
//            };
//            int columnCount = table.getColumnCount();
//            for (int i = 0; i < columnCount; i++) {
//                table.getColumn(table.getColumnName(i)).setCellRenderer(tcr);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        if(IsLight ==true){
            try {
                DefaultTableCellRenderer tcr = new DefaultTableCellRenderer() {

                    public Component getTableCellRendererComponent(JTable table,
                                                                   Object value, boolean isSelected, boolean hasFocus,
                                                                   int row, int column) {
                        final LogEntry logEntry = Tags.this.log.get(Tags.this.logTable.convertRowIndexToModel(row));

                        String ProfileName = logEntry.profileName;
                        LightColor ShowColor = GetProfileColor(ProfileName);
                        int matchtype = ShowColor.matchtype;
                        if (!ShowColor.greps.isEmpty()) {
                            String responseText = new String(logEntry.requestResponse.getResponse());
                            Boolean isFind = false;
                            for (String query : ShowColor.greps) {
                                String[] tokens = query.split(",", 3);
                                if(matchtype == 2) {// regex match
                                    isFind = Tags.this.RegexQuery(tokens[2],responseText);
                                }else{// Simple String
                                    if (responseText.toUpperCase().contains(tokens[2].toUpperCase())) {
                                        isFind = true;
                                        break;
                                    }
                                }

                            }

                            if (isFind) {
                                setBackground(ShowColor.RowColor.get(1));
                                setForeground(ShowColor.RowColor.get(0));
                            } else {
                                setBackground(new Color(0, 102, 255));
                                setForeground(new Color(0xFF, 0xFF, 0xFF));
                            }
                        }else
                        {
                            setBackground(new Color(0, 102, 255));
                            setForeground(new Color(0xFF, 0xFF, 0xFF));
                        }
                        return super.getTableCellRendererComponent(table, value,
                                isSelected, hasFocus, row, column);
                    }
                };
                int columnCount = table.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    table.getColumn(table.getColumnName(i)).setCellRenderer(tcr);
                }
            } catch (
                    Exception ex) {
                ex.printStackTrace();
            }
            IsLight = false;
        }
    }
    public  void setOneRowBackgroundColor(int rowIndex,
                                          Color color) {
        JTable table = logTable;
        try {
            DefaultTableCellRenderer tcr = new DefaultTableCellRenderer() {

                public Component getTableCellRendererComponent(JTable table,
                                                               Object value, boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    if (rowIndex == row) {
                        setBackground(color);
                        setForeground(new Color(0x00, 0xFD, 0x9D));
                    } else {
                        setBackground(Color.BLACK);
                        setForeground(new Color(0x00, 0xFD, 0x9D));
                    }
                    return super.getTableCellRendererComponent(table, value,
                            isSelected, hasFocus, row, column);
                }
            };
            int columnCount = table.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                table.getColumn(table.getColumnName(i)).setCellRenderer(tcr);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Component getUiComponent() {
        return this.splitPane;
    }

    @Override
    public int getRowCount() {
        return this.log.size();
    }

    public int getRowIndexByValue(int startIndex,String value)
    {
        int findCows = 0;
        int RowLines = getRowCount();
        if(startIndex>RowLines)
        {
            return findCows;
        }
        for(int i=startIndex;i<RowLines;i++)
        {
            String v = logTable.getValueAt(i,4).toString();
            if(v.equals(value))
            {
                findCows =i;
                break;
            }
        }
        return findCows;
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public String getColumnName(final int column) {
        switch (column) {
            case 0:
            {
                return "ID";
            }
            case 1: {
                return "Url";
            }
            case 2: {
                return "Status";
            }
            case 3: {
                return "Response Length";
            }
            case 4: {
                return "Payload";
            }
            case 5: {
                return "Param";
            }
            case 6: {
                return "Profile";
            }
            default: {
                return "";
            }
        }
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        Class returnValue;
        if ((columnIndex >= 0) && (columnIndex < getColumnCount())) {

            returnValue = getValueAt(0, columnIndex).getClass();
        } else {
            returnValue = Object.class;
        }
        return returnValue;
    }

    @Override
    public String getValueAt(final int rowIndex, final int columnIndex) {
        final LogEntry logEntry = this.log.get(rowIndex);
        switch (columnIndex) {
            case 0: {
                return logEntry.id;
            }
            case 1: {
                return logEntry.url.toString();
            }
            case 2: {
                return logEntry.status;
            }
            case 3: {
                return logEntry.response_len;
            }
            case 4: {
                return logEntry.payload;
            }
            case 5: {
                return logEntry.pointName;
            }
            case 6: {
                return logEntry.profileName;
            }
            default: {
                return "";
            }
        }
    }

    @Override
    public byte[] getRequest() {
        return this.currentlyDisplayedItem.getRequest();
    }

    @Override
    public byte[] getResponse() {
        return this.currentlyDisplayedItem.getResponse();
    }

    @Override
    public IHttpService getHttpService() {
        return this.currentlyDisplayedItem.getHttpService();
    }
    public void addEntry(LogEntry logEntry) {
        this.log.add(logEntry);
    }
    public int add(final URL url, final String status, final String response_len,final String payload, final String pointName,final String profileName,final IHttpRequestResponse requestResponse) {
        synchronized (this.log) {
            final Date d = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final String startTime = sdf.format(d);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    final int id = log.size();
                    addEntry(new LogEntry(String.valueOf(id),url, status,response_len,payload, pointName,profileName,requestResponse));
                    fireTableRowsInserted(id, id);
                    if(filterText =="H"){
                        filerTableColor();
                    }else{
                        filerTable();
                    }

                }
            });
            return 0;
        }
    }

    /*public int save(final int id, final String extensionMethod, final String encryptMethod, final String requestMethod, final String url, final String statusCode, final String issue, final IHttpRequestResponse requestResponse) {
        final TablesData dataEntry = this.Udatas.get(id);
        final String startTime = dataEntry.startTime;
        final Date d = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String endTime = sdf.format(d);
        synchronized (this.Udatas) {
            this.Udatas.set(id, new TablesData(id, extensionMethod, encryptMethod, requestMethod, url, statusCode, issue, requestResponse, startTime, endTime));
            this.fireTableRowsUpdated(id, id);
            return id;
        }
    }*/

    private static class LogEntry
    {
        final URL url;
        String status;
        String id;
        String response_len;
        String payload="";
        String pointName;
        String profileName;
        public boolean isIsssue;
        final IHttpRequestResponse requestResponse;

        LogEntry(final String id,final URL url, final String status,final String response_len,final String payload,  final String pointName, String profileName,final IHttpRequestResponse requestResponse) {
            this.id = id;
            this.url = url;
            this.status = status;
            this.response_len = response_len;
            this.profileName = profileName;
            if(!payload.isEmpty())
            {
                this.payload = payload;
            }
            this.pointName = pointName;
            this.requestResponse = requestResponse;
            this.isIsssue = false;
        }
    }

    private class Table extends JTable
    {

        public JPopupMenu m_popupMenu;
        public Table(final TableModel tableModel) {
            super(tableModel);
            m_popupMenu = new JPopupMenu();
            JMenuItem ClearMenItem = new JMenuItem();
            ClearMenItem.setText("Clear log");
            ClearMenItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    //do sime
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            clearLog();
                        }});

                callbacks.printOutput("clear log");
                }
            });
            m_popupMenu.add(ClearMenItem);
            registerListeners();
        }

        @Override
        public void changeSelection(final int row, final int col, final boolean toggle, final boolean extend) {

            //final LogEntry logEntry = Tags.this.log.get(row);
            final LogEntry logEntry =Tags.this.log.get(convertRowIndexToModel(row));
            Tags.this.requestViewer.setMessage(logEntry.requestResponse.getRequest(), true);
            Tags.this.responseViewer.setMessage(logEntry.requestResponse.getResponse(), false);
            Tags.this.currentlyDisplayedItem = logEntry.requestResponse;
            super.changeSelection(row, col, toggle, extend);
        }
        public void clearLog()
        {
            if(Tags.this.log.size()>0)
            {
                Tags.this.log.clear();
            }
            fireTableDataChanged();
        }

        private void registerListeners()
        {
            this.addMouseListener( new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onMouseEvent(e);
                }

                @Override
                public void mouseReleased( MouseEvent e ){
                    onMouseEvent(e);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    onMouseEvent(e);
                }

                private void onMouseEvent(MouseEvent e){
                    if ( SwingUtilities.isRightMouseButton(e)){
                        Point p = e.getPoint();
                        int rowAtPoint = rowAtPoint(p);
                        if(rowAtPoint == -1) return;

                        m_popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
        }


    }
}







