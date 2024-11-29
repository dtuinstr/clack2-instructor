package clack.endpoint;

import clack.cipher.CipherManager;
import clack.cipher.CipherNameEnum;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class ClientGUI
{
    //
    // Static constants
    //
    private static final int TEXT_ROWS = 10;
    private static final int TEXT_COLS = 60;
    private static final int KEY_COLS = 20;
    private static final int HOST_COLS = 30;
    private static final int PORT_COLS = 5;
    private static final int USERNAME_COLS = 20;
    private static final Dimension BUTTON_SIZE = new Dimension(200, 30);
    private static final Box.Filler BUTTON_SPACING =
            (Box.Filler) Box.createRigidArea(new Dimension(40, 15));
    private static final Box.Filler SMALL_SPACING =
            (Box.Filler) Box.createRigidArea(new Dimension(10, 10));
    private static final Border ETCHED_BORDER =
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

    //
    // Object fields
    //

    // Bottom-level GUI components (that hold values of importance).
    private final String[] cipherNames;
    private final JComboBox<String> cipherNameCombo;
    private final JCheckBox cipherEnableCheckBox;
    private final JTextField cipherKeyField;
    private final JTextField hostnameField;
    private final JTextField portField;
    private final JTextField usernameField;
    private final JTextField textEntryField;

    // Top-level GUI component
    private final JFrame frame;

    // Other
    private final CipherManager cipherManager;
    // When testing, one-time pad needs a copy of the cipher manager to
    // simulate the other end's pad, so pads stay in sync.
    private final boolean TESTING = true;
    private final CipherManager cipherManagerOther;

    /**
     * Constructs client GUI. Sets up GUI and wires up functionality.
     * GUI schematic:
     * <pre>
     *
     *         +---------+ +-------------------------------+
     *         | Options | |  Connection                   |
     *         |         | +-------------------------------+
     *         |---------| |                               |
     *         |         | |  Conversation / Log           |
     *         | Control | |  (w/ scroll decorations)      |
     *         | (Btns)  | |                               |
     *         |         | |                               |
     *         |         | |                               |
     *         |         | |                               |
     *         |         | |-------------------------------|
     *         |         | |  Text Entry                   |
     *         +---------+ +-------------------------------+
     *          leftPanel             mainPanel
     *
     * </pre>
     *
     */
    public ClientGUI()
    {
        //
        // Create bottom-level GUI components.
        //
        cipherNames = CipherNameEnum.asStringArray();
        cipherNameCombo = new JComboBox<>(cipherNames);
        cipherNameCombo.setSelectedIndex(0);
        cipherEnableCheckBox = new JCheckBox();
        cipherEnableCheckBox.setEnabled(false);
        cipherKeyField = new JTextField(KEY_COLS);
        cipherKeyField.setText("THEKEY");
        hostnameField = new JTextField(HOST_COLS);
        portField = new JTextField(PORT_COLS);
        usernameField = new JTextField(USERNAME_COLS);
        textEntryField = new JTextField(TEXT_COLS);
        // Place each in its own panel with a label (an "item")
        JPanel cipherNameItem = createItem("Cipher Name", cipherNameCombo);
        JPanel cipherEnabledItem = createItem("Enabled", cipherEnableCheckBox);
        JPanel cipherKeyItem = createItem("Cipher Key", cipherKeyField);
        JPanel hostnameItem = createItem("Host Name", hostnameField);
        JPanel portItem = createItem("Port Number", portField);
        JPanel usernameItem = createItem("Username", usernameField);
        JPanel textEntryItem = createItem("Message Text", textEntryField);

        // Buttons
        JButton clearBtn = new JButton("Clear Conversation");
        JButton helpBtn = new JButton("Help");
        JButton listusersBtn = new JButton("List Users");
        JButton loginBtn = new JButton("Login");
        JButton logoutBtn = new JButton("Logout");
        JButton sendfileBtn = new JButton("Send File");
        JButton connectBtn = new JButton("Connect");
        JButton disconnectBtn = new JButton("Disconnect");
        JButton[] allBtns = {
                clearBtn, helpBtn, listusersBtn,
                loginBtn, logoutBtn, sendfileBtn,
                connectBtn, disconnectBtn
        };
        for (JButton btn : allBtns) {
            btn.setPreferredSize(BUTTON_SIZE);
            btn.setMinimumSize(BUTTON_SIZE);
            btn.setMaximumSize(BUTTON_SIZE);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setEnabled(false);
        }

        //
        // Create intermediate-level panels
        //

        // Cipher Options
        JPanel cipherNameEnabledItem = createBoxPanel(
                BoxLayout.LINE_AXIS, cipherNameItem, cipherEnabledItem);
        cipherNameEnabledItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        cipherKeyItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel cipherOptionPanel = createBoxPanel(BoxLayout.PAGE_AXIS,
                cipherNameEnabledItem, cipherKeyItem);
        cipherOptionPanel.setBorder(ETCHED_BORDER);

        // Control Buttons
        JPanel controlButtonPanel = new JPanel();
        controlButtonPanel.setLayout(
                new BoxLayout(controlButtonPanel, BoxLayout.PAGE_AXIS));
        controlButtonPanel.setBorder(ETCHED_BORDER);
        JButton[] controlBtns = {
                clearBtn, helpBtn, listusersBtn,
                loginBtn, logoutBtn, sendfileBtn
        };
        for (JButton btn : controlBtns) {
            controlButtonPanel.add(Box.createVerticalGlue());
            controlButtonPanel.add(BUTTON_SPACING);
            controlButtonPanel.add(btn);
        }
        controlButtonPanel.add(Box.createVerticalGlue());
        controlButtonPanel.add(BUTTON_SPACING);

        // Connection Panel
        JPanel hostInfo =
                createBoxPanel(BoxLayout.LINE_AXIS, hostnameItem, portItem);
        JPanel cnxSettings =
                createBoxPanel(BoxLayout.PAGE_AXIS, hostInfo, usernameItem);
        JPanel cnxBtns =
                createBoxPanel(BoxLayout.LINE_AXIS, connectBtn, disconnectBtn);
        JPanel connectionPanel =
                createBoxPanel(BoxLayout.PAGE_AXIS, cnxSettings, cnxBtns);
        connectionPanel.setBorder(ETCHED_BORDER);

        // Conversation Panel
        JTextArea conversationArea = new JTextArea(TEXT_ROWS, TEXT_COLS);
        JScrollPane caScrollPane = new JScrollPane(conversationArea);
        conversationArea.setEditable(false);
        conversationArea.setLineWrap(true);
        JPanel conversationPanel = new JPanel();
        // Use center of border layout so conversationArea expands
        conversationPanel.setLayout(new BorderLayout());
        conversationPanel.add(caScrollPane, BorderLayout.CENTER);

        // TextEntry Panel
        // Not much to do; use textEntryItem. Just set its border.
        textEntryItem.setBorder(ETCHED_BORDER);

        //
        // Put it all together.
        //
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        //leftPanel.setBorder(ETCHED_BORDER);
        leftPanel.add(cipherOptionPanel, BorderLayout.NORTH);
        leftPanel.add(controlButtonPanel, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(connectionPanel, BorderLayout.NORTH);
        mainPanel.add(conversationPanel, BorderLayout.CENTER);
        mainPanel.add(textEntryItem, BorderLayout.SOUTH);

        frame = new JFrame("Clack");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(
                new BoxLayout(frame.getContentPane(), BoxLayout.LINE_AXIS));
        frame.add(leftPanel);
        frame.add(mainPanel);

        // Additional component initialization.
        cipherEnableCheckBox.setEnabled(true);
        cipherEnableCheckBox.setSelected(false);
        clearBtn.setEnabled(true);
        connectBtn.setEnabled(true);

        // Initialize cipher managers
        cipherManager = new CipherManager();
        setCipherManagerToOptions(cipherManager);
        cipherManagerOther = new CipherManager();
        setCipherManagerToOptions(cipherManagerOther);

        //
        // Actions
        //
        cipherEnableCheckBox.addActionListener(event -> {
            if (cipherEnableCheckBox.isSelected()) {
                setCipherManagerToOptions(cipherManager);
                setCipherManagerToOptions(cipherManagerOther);
                disableAll(cipherNameCombo, cipherKeyField);
            } else {
                enableAll(cipherNameCombo, cipherKeyField);
            }
        });
        clearBtn.addActionListener(event ->
                conversationArea.setText(""));
        helpBtn.addActionListener(event ->
                conversationArea.append("Help: clicked\n"));
        listusersBtn.addActionListener( event ->
                conversationArea.append("List Users: clicked\n"));
        loginBtn.addActionListener(event -> {
            String result = JOptionPane.showInputDialog(
                    frame,
                    "Enter password for " + usernameField.getText(),
                    "Enter password",
                    JOptionPane.PLAIN_MESSAGE
            );
            if (result == null) {
                result = "Log In: clicked Cancel\n";
            } else {
                result = "Log In: clicked OK: username='"
                        + usernameField.getText()
                        + "', password='" + result + "'\n";
            }
            conversationArea.append(result);
        });
        logoutBtn.addActionListener(event -> {
            String[] options = {"Yes", "Cancel"};
            int result = JOptionPane.showOptionDialog(
                    frame,
                    "Are you sure you want to log out?",
                    "Confirm log out",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[1]
            );
            if (result == 0) {
                conversationArea.append("Log Out: clicked OK\n");
            } else {
                conversationArea.append("Log Out: clicked Cancel\n");
            }
            conversationArea.append("" + result);
        });
        textEntryField.addActionListener(event -> {
            String text = textEntryField.getText();
            String response = "> " + text;
            if (cipherEnableCheckBox.isSelected()) {
                // response w/ encryption
                String prepText = cipherManager.prep(text);
                String cipherText = cipherManager.encrypt(prepText);
                String decrypted;
                if (TESTING && cipherManager.getCipherName() ==
                        CipherNameEnum.PSEUDO_ONE_TIME_PAD) {
                    decrypted = cipherManagerOther.decrypt(cipherText);
                } else {
                    decrypted = cipherManager.decrypt(cipherText);
                }
                response =
                        "Clear > " + text + "\n"
                                + "Prepped > " + prepText + "\n"
                                + "Encrypted > " + cipherText + "\n"
                                + "Decrypted > " + decrypted + "\n";
            }
            conversationArea.append(response);
            textEntryField.setText("");
        });
        connectBtn.addActionListener( event -> {
            if (hostnameField.getText().isEmpty()
                    || portField.getText().isEmpty()
                    || usernameField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Please fill in hostname, port, and username",
                        "Need full info",
                        JOptionPane.WARNING_MESSAGE);
            } else { // connect
                disconnectBtn.setEnabled(true);
                disableAll(hostnameField, portField, usernameField);
                enableAll(controlBtns);
                connectBtn.setEnabled(false);
            }
        });
        disconnectBtn.addActionListener( event -> {
            disconnectBtn.setEnabled(false);
            enableAll(hostnameField, portField, usernameField);
            disableAll(controlBtns);
            clearBtn.setEnabled(true);
            connectBtn.setEnabled(true);
        });
        sendfileBtn.addActionListener( event -> {
            JFileChooser fc = new JFileChooser();
            int result = fc.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                String filePath = fc.getSelectedFile().getPath();
                conversationArea.append("Send File: clicked OK: file="
                                + filePath + '\n');
            } else {
                conversationArea.append("Send File: clicked Cancel" + '\n');
            }
        });

        // pack it and go
        frame.pack();
        frame.setVisible(true);
    }

    private void setCipherManagerToOptions(CipherManager cm)
    {
        String cipherName = String.valueOf(cipherNameCombo.getSelectedItem());
        String key = String.valueOf(cipherKeyField.getText());
        try {
            cm.setEnabled(cipherEnableCheckBox.isSelected());
            cm.setKey(key);
            cm.setCipher(cipherName);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Problem with " + cipherName + ": "
                            + e.getMessage()
                            + ". Please try a different cipher or key.",
                    "Cipher Problem",JOptionPane.ERROR_MESSAGE
            );
            cipherEnableCheckBox.setSelected(false);
            enableAll(cipherNameCombo, cipherKeyField);
        }
    }

    /**
     * Calls setEnable(true) on all components in arg list.
     * @param components components to enable.
     */
    private void enableAll(Component... components) {
        for (Component c : components) {
            c.setEnabled(true);
        }
    }

    /**
     * Calls setEnable(false) on all components in arg list.
     * @param components components to disable.
     */
    private void disableAll(Component... components) {
        for (Component c : components) {
            c.setEnabled(false);
        }
    }

    /**
     * Creates a JPanel containing a label and a component,
     * with the label to the left of the component, and the JPanel
     * using a BoxLayout.
     * @param label the label to use
     * @param jc the component to label
     * @return a JPanel containing the label and component.
     */
    private JPanel createItem(String label, JComponent jc) {
        return createBoxPanel(BoxLayout.LINE_AXIS,
                new JLabel(label),
                jc);
    }

    /**
     * Creates a JPanel with a BoxLayout and containing one or more
     * components, with a small filler and glue between them.
     *
     * @param axis one of the BoxLayout.*_AXIS constants.
     * @param jcs the components
     * @return a JPanel containing components
     */
    private JPanel createBoxPanel(int axis, JComponent... jcs) {
        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, axis));
        jp.add(SMALL_SPACING);
        jp.add(Box.createGlue());
        for (JComponent jc : jcs) {
            jp.add(jc);
            jp.add(SMALL_SPACING);
            jp.add(Box.createGlue());
        }
        return jp;
    }
}
