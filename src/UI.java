import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

public class UI  extends JPanel implements ActionListener, ListSelectionListener {
    JButton openButton, convertButton, deleteButton;
    JTextArea log;
    JFileChooser fc;

    JList<FileListElement> fileList;
    DefaultListModel<FileListElement> listModel;

    JRadioButton pictureToFileRadio;
    JRadioButton fileToPictureRadio;

    JScrollPane logScrollPane;
    JScrollPane fileListScrollPane;

    ArrayList<FileListElement> files = new ArrayList<>();

    static boolean isConverting = false;

    public UI(){
        super(new BorderLayout());

        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        logScrollPane = new JScrollPane(log);

        listModel = new DefaultListModel<>();

        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        fileList.addListSelectionListener(this);
        fileList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof FileListElement){
                    if (((FileListElement) value).error) {
                        c.setBackground( Color.red );
                    } else {
                        c.setBackground( Color.white );
                    }
                }


                if (isSelected){
                    c.setBackground(new Color(54, 94, 179, 255));
                }

                return c;
            }
        });
        fileListScrollPane = new JScrollPane(fileList);

        //Create a file chooser
        fc = new JFileChooser();

        //Select Files and Folders
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        //create Open Button
        openButton = new JButton("Open a File/Folder");
        openButton.addActionListener(this);

        //create convert Button
        convertButton = new JButton("Convert");
        convertButton.addActionListener(this);
        convertButton.setEnabled(false);

        //create convert Button
        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(this);
        deleteButton.setEnabled(false);

        //Radio Buttons
        fileToPictureRadio = new JRadioButton("File To Picture");
        pictureToFileRadio = new JRadioButton("Picture To File");
        fileToPictureRadio.setSelected(true);

        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(fileToPictureRadio);
        group.add(pictureToFileRadio);

        JPanel radidoButtonPannel = new JPanel(new GridLayout(0,1));
        radidoButtonPannel.add(fileToPictureRadio);
        radidoButtonPannel.add(pictureToFileRadio);

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);
        buttonPanel.add(convertButton);
        buttonPanel.add(radidoButtonPannel);
        buttonPanel.add(deleteButton);


        JPanel logAndSelecter = new JPanel(new GridLayout(0,2));
        logAndSelecter.add(logScrollPane);
        logAndSelecter.add(fileListScrollPane);

        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logAndSelecter, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(UI.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (file.isDirectory()){
                    try {
                        for (File oFile : file.listFiles()){
                            if (oFile.isFile()){
                                addFile(new FileListElement(oFile,false));
                            }
                        }
                    } catch (Exception exception) {
                        addLog("Folder has no Files\n");
                        exception.printStackTrace();
                    }
                }else {
                    addFile(new FileListElement(file,false));
                }
            } else {
                addLog("Open command cancelled by user.\n");
            }
            log.setCaretPosition(log.getDocument().getLength());

            //Handle save button action.
        } else if (e.getSource() == convertButton) {
            if (files.size() > 0) {
                if (!isConverting){
                    isConverting = true;
                    convertButton.setEnabled(false);
                    openButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    fileList.setSelectedIndex(-1);
                    String[] filePaths = new String[files.size()];
                    for (int i = 0; i < files.size(); i++) {
                        filePaths[i] = files.get(i).file.getAbsolutePath();
                    }

                    UI ui = this;
                    if (fileToPictureRadio.isSelected()){
                        new Thread(() -> new File2Pic(filePaths, ui)).start();
                    }else {
                        new Thread(() -> new Pic2File(filePaths,ui)).start();
                    }
                }else {
                    showMessage("Converting in Process!");
                }
            }else {
                showMessage("You first have to add a Folder or File");
            }
        } else if (e.getSource() == deleteButton) {
            int index = fileList.getSelectedIndex();
            if (index != -1){
                addLog("Closing: " + files.get(index).file.getName() + "\n");
                deleteItem(index);
            }
        }
    }

    private void deleteItem(int index){
        files.remove(index);
        listModel.remove(index);
    }

    private void addFile(FileListElement fileListElement){
        files.add(fileListElement);
        listModel.addElement(new FileListElement(fileListElement.file,false));
        //This is where a real application would open the file.
        addLog("Opening: " + fileListElement.file.getName() + "\n");
        convertButton.setEnabled(true);
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = UI.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public void addLog(String message){
        log.append(message);
        JScrollBar vertical = logScrollPane.getVerticalScrollBar();
        vertical.setValue( vertical.getMaximum() );
    }

    public void finishedFile(File file, boolean successful){
        int index = -1;
        for (int i = 0; i < files.size(); i++){
            if (files.get(i).file.getAbsolutePath().equals(file.getAbsolutePath())){
                index = i;
                break;
            }
        }
        if (index != -1){
            if (successful){
                deleteItem(index);
            }else {
                listModel.get(index).error = true;
                fileList.updateUI();
                addLog("Aborted: \n"+file.getAbsolutePath());
            }
        }else {
            addLog("Didnt found file in list\n");
        }
    }

    public void finished(){
        isConverting = false;
        openButton.setEnabled(true);
        deleteButton.setEnabled(files.size() > 0);
        convertButton.setEnabled(files.size() > 0);
        fileList.setSelectedIndex(-1);
        addLog("Finished converting\n\n\n");
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    public static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("File2Picture");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        //Add content to the window.
        frame.add(new UI());

        //Display the window.
        frame.pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dimension = new Dimension(screen.width/2,(int)(screen.height/1.5));
        frame.setSize(dimension);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isConverting) {
                    String[] ObjButtons = {"Yes","No"};
                    int PromptResult = JOptionPane.showOptionDialog(null,
                            "Are you sure you want to exit?", "Converting in Process",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                            ObjButtons,ObjButtons[1]);
                    if(PromptResult==0)
                    {
                        System.exit(0);
                    }
                } else {
                    System.exit(0);
                }
            }
        });
        try {
            frame.setIconImage(Toolkit.getDefaultToolkit().getImage(UI.class.getResource("/filetopicture.png")));
        } catch (Exception ignored) {}
        frame.setVisible(true);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (!isConverting) {
                if (fileList.getSelectedIndex() == -1) {
                    //No selection
                    deleteButton.setEnabled(false);
                } else {
                    //Selection
                    deleteButton.setEnabled(true);;
                }
            } else {
                fileList.setSelectedIndex(-1);
                deleteButton.setEnabled(false);
            }
        }
    }

    private void showMessage(String message){
        JOptionPane.showMessageDialog(null,message);
    }

}