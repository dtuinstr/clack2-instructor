package clack.endpoint;

import clack.cipher.CipherManager;
import clack.cipher.CipherNameEnum;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import static javax.swing.BoxLayout.*;

public class ClientGUI
{
    //
    // Static constants
    //
    private static final int TEXT_ROWS = 10;
    private static final int TEXT_COLS = 50;
    private static final int KEY_COLS = 20;
    private static final int HOST_COLS = 30;
    private static final int PORT_COLS = 5;
    private static final int USERNAME_COLS = 20;
    private static final Dimension BUTTON_SIZE = new Dimension(180, 30);
    private static final Box.Filler BUTTON_SPACING =
            (Box.Filler) Box.createRigidArea(new Dimension(40, 15));
    private static final Border BORDER =
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
    private static final Border EMPTY_BORDER =
            BorderFactory.createEmptyBorder(3, 3, 3, 3);

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
    // Login dialog
    private final JDialog loginDialog;
    private final JTextField ldUsernameField;
    private final JTextField ldPasswordField;

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
        // Place each in its own panel with a label.
        JPanel cipherNameItem = createItem("Cipher Name", cipherNameCombo);
        JPanel cipherKeyItem = createItem("Cipher Key", cipherKeyField);
        JPanel cipherEnabledItem = createItem("Enabled", cipherEnableCheckBox);
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
        cipherNameItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        cipherKeyItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        cipherEnabledItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel cipherInnerOptionPanel = createBoxPanel(Y_AXIS,
                cipherNameItem,
                cipherKeyItem,
                cipherEnabledItem);
        cipherInnerOptionPanel.setBorder(EMPTY_BORDER);
        JPanel cipherOptionPanel = new JPanel(new BorderLayout());
        cipherOptionPanel.add(cipherInnerOptionPanel, BorderLayout.WEST);
        cipherOptionPanel.setBorder(BORDER);

        // Control Buttons
        JPanel controlButtonPanel = new JPanel();
        controlButtonPanel.setLayout(
                new BoxLayout(controlButtonPanel, BoxLayout.Y_AXIS));
        controlButtonPanel.setBorder(BORDER);
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
        hostnameItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        portItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel cnxBtns =
                createBoxPanel(X_AXIS, connectBtn, disconnectBtn);
        cnxBtns.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel connectionInnerPanel = createBoxPanel(Y_AXIS,
                hostnameItem,
                portItem,
                usernameItem,
                cnxBtns);
        connectionInnerPanel.setBorder(EMPTY_BORDER);
        JPanel connectionPanel = new JPanel(new BorderLayout());
        connectionPanel.add(connectionInnerPanel, BorderLayout.WEST);
        connectionPanel.setBorder(BORDER);

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
        JPanel textEntryPanel = createBoxPanel(BoxLayout.Y_AXIS);
        textEntryItem.setBorder(EMPTY_BORDER);
        textEntryPanel.add(textEntryItem);
        textEntryPanel.setBorder(BORDER);

        //
        // Put it all together.
        //
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(cipherOptionPanel, BorderLayout.NORTH);
        leftPanel.add(controlButtonPanel, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(connectionPanel, BorderLayout.NORTH);
        mainPanel.add(conversationPanel, BorderLayout.CENTER);
        mainPanel.add(textEntryPanel, BorderLayout.SOUTH);

        frame = new JFrame("Clack");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(mainPanel, BorderLayout.CENTER);

        //
        // Additional initializations
        //
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
        // Create login dialog (but don't show yet)
        //
        loginDialog = new JDialog(frame, "Login", true);
        loginDialog.setLayout(new BoxLayout(loginDialog.getContentPane(),
                BoxLayout.Y_AXIS));
        ldUsernameField = new JTextField(USERNAME_COLS);
        JPanel ldUsernameItem = createItem("Username", ldUsernameField);
        ldPasswordField = new JTextField(USERNAME_COLS);
        JPanel ldPasswordItem = createItem("Password", ldPasswordField);
        JButton ldOKBtn = new JButton("OK");
        JButton ldCancelBtn = new JButton("Cancel");
        JPanel ldButtons =
                createBoxPanel(X_AXIS, ldOKBtn, ldCancelBtn);
        ldButtons.setBorder(EMPTY_BORDER);
        JPanel loginPanel = createBoxPanel(Y_AXIS,
                ldUsernameItem,
                ldPasswordItem,ldButtons);
        loginPanel.setBorder(EMPTY_BORDER);
        loginDialog.add(loginPanel);
        loginDialog.pack();
        Dimension ldDimension =
                new Dimension(loginDialog.getWidth(), loginDialog.getHeight());
        loginDialog.setMaximumSize(ldDimension);
        loginDialog.setMinimumSize(ldDimension);
        loginDialog.setPreferredSize(ldDimension);
        loginDialog.setResizable(false);
        loginDialog.setVisible(false);

        ldOKBtn.addActionListener(event -> {
            conversationArea.append("Log In: clicked OK: "
                    + "username=" + ldUsernameField.getText()
                    + ", password=" + ldPasswordField.getText() + "\n");
            loginDialog.setVisible(false);
        });

        ldCancelBtn.addActionListener(event -> {
            conversationArea.append("Log In: clicked Cancel: "
                    + "username=" + ldUsernameField.getText()
                    + ", password=" + ldPasswordField.getText() + "\n");
            loginDialog.setVisible(false);
        });


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
            ldUsernameField.setText(usernameField.getText());
            loginDialog.setVisible(true);
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
            String response = "> " + text + "\n";
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
     * Creates a JPanel containing a label and a component, with the
     * label to the left of the component, and the JPanel using a
     * BoxLayout. Component's border is set to BORDER.
     * @param labelStr the label to use
     * @param jc the component to label
     * @return a JPanel containing the label and component.
     */
    private JPanel createItem(String labelStr, JComponent jc) {
        JLabel label = new JLabel(labelStr);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        jc.setBorder(BORDER);
        label.setAlignmentY(Component.TOP_ALIGNMENT);
        jc.setAlignmentY(Component.TOP_ALIGNMENT);
        JPanel jcPanel = new JPanel();
        jcPanel.add(jc);
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.add(label, BorderLayout.WEST);
        itemPanel.add(jcPanel, BorderLayout.CENTER);
        return itemPanel;
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
        Border border;
        if (axis == X_AXIS || axis == LINE_AXIS) {
            border = BorderFactory.createEmptyBorder(0, 3, 0, 3);
        } else {
            border = BorderFactory.createEmptyBorder(3, 0, 3, 0);
        }
        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, axis));
        jp.add(Box.createGlue());
        for (JComponent jc : jcs) {
            jc.setBorder(border);
            jp.add(jc);
            jp.add(Box.createGlue());
        }
        return jp;
    }
}
