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
import javax.swing.event.RowSorterEvent;


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

    public Tags(final IBurpExtenderCallbacks callbacks, final String tagName, JTabbedPane panel) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        this.tagName = tagName;
        this.payloads = new ArrayList<>();
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
                logTable.getColumnModel().getColumn(2).setPreferredWidth(200);
                logTable.getColumnModel().getColumn(3).setPreferredWidth(200);
                logTable.getColumnModel().getColumn(4).setPreferredWidth(1000);
                logTable.getColumnModel().getColumn(5).setPreferredWidth(200);
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

    public  void setBackgroundColor(Color color)
    {
        JTable table = logTable;
        try {
            DefaultTableCellRenderer tcr = new DefaultTableCellRenderer() {

                public Component getTableCellRendererComponent(JTable table,
                                                               Object value, boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    final LogEntry logEntry =Tags.this.log.get(Tags.this.logTable.convertRowIndexToModel(row));
                    if (payloads.contains(logEntry.payload)) {
                        setBackground(color);
                        setForeground(new Color(0xFF, 0xFF, 0xFF));
                    } else {
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
        } catch (Exception ex) {
            ex.printStackTrace();
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
        return 6;
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
    public synchronized void addEntry(LogEntry logEntry) {
        this.log.add(logEntry);
    }
    public int add(final URL url, final String status, final String response_len,final String payload, final String pointName,final IHttpRequestResponse requestResponse) {
        synchronized (this.log) {
            final Date d = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final String startTime = sdf.format(d);
            final int id = this.log.size();
            addEntry(new LogEntry(String.valueOf(id),url, status,response_len,payload, pointName,requestResponse));
            this.fireTableRowsInserted(id, id);
            return id;
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
        final IHttpRequestResponse requestResponse;

        LogEntry(final String id,final URL url, final String status,final String response_len,final String payload,  final String pointName,final IHttpRequestResponse requestResponse) {
            this.id = id;
            this.url = url;
            this.status = status;
            this.response_len = response_len;
            if(!payload.isEmpty())
            {
                this.payload = payload;
            }
            this.pointName = pointName;
            this.requestResponse = requestResponse;
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
                clearLog();
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







