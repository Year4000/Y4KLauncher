/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010, 2011 Albert Pham <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.mclauncher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import com.sk89q.mclauncher.config.Configuration;
import com.sk89q.mclauncher.config.Def;
import com.sk89q.mclauncher.config.LauncherOptions;
import com.sk89q.mclauncher.util.UIUtil;

/**
 * Main launcher GUI frame.
 * 
 * @author sk89q
 */
public class LauncherFrame extends JFrame {

    private static final long serialVersionUID = 4122023031876609883L;
    private static final int PAD = 17;
    private JList<?> configurationList;
    private JComboBox<String> userText;
    private JTextField passText;
    JCheckBox rememberPass;
    private JButton playBtn;
    private JButton optionsBtn;
    private JPanel buttonsPanel;
    private LauncherOptions options;
    private TaskWorker worker = new TaskWorker();
    private Color bgColor = null;
    private Color fgColor = Color.DARK_GRAY;
    private Font font = new Font("Arial", Font.BOLD, 12);

    /**
     * Construct the launcher.
     */
    public LauncherFrame() {
        setTitle("Year4000's Launcher");
        setSize(750, 450);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); 
        UIUtil.setLookAndFeel();
        UIUtil.setIconImage(this, "/resources/icon.png");

        options = Launcher.getInstance().getOptions();

        buildUI();
        
        setLocationRelativeTo(null);

        // Setup
        setConfiguration(options.getStartupConfiguration());
        populateIdentities();
        setLastUsername();


        // Focus initial item
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (getInputUsername().length() > 0
                        && getInputPassword().length() > 0) {
                    playBtn.requestFocusInWindow();
                } else if (getInputUsername().length() > 0) {
                    passText.requestFocusInWindow();
                } else {
                    userText.requestFocusInWindow();
                }
            }
        });
    }

    /**
     * Sets the configuration to use.
     * 
     * @param configuration
     *            configuration
     */
    public void setConfiguration(Configuration configuration) {
        if (!configuration.getBaseDir().isDirectory()) {
            UIUtil.showError(
                    this,
                    "Misconfigured configuration",
                    "The last selected configuration is broken. Switching to the default...");
            configuration = options.getConfigurations().getDefault();
        }
        
        ListModel<?> model = configurationList.getModel();
        if (configurationList.getSelectedValue() != configuration) {
            for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i) == configuration) {
                    configurationList.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    /**
     * Get the currently selected workspace.
     * 
     * @return workspace
     */
    public Configuration getWorkspace() {
        Configuration configuration = (Configuration) configurationList.getSelectedValue();
        
        // Switch to default if the current one is broken
        if (configuration == null || !configuration.getBaseDir().isDirectory()) {
            configuration = options.getConfigurations().getDefault();
            setConfiguration(configuration);
        }
        
        return configuration;
    }

    public String getInputUsername() {
        Object selectedName = userText.getSelectedItem();
        return selectedName != null ? selectedName.toString() : "";
    }

    public String getInputPassword() {
        return passText.getText();
    }

    public void setLogin(String username, String password) {
        ((JTextComponent) userText.getEditor().getEditorComponent())
                .setText(username);
        if (passText != null) {
            passText.setText(password);
        }
    }

    //Open the options window.
    private OptionsDialog openOptions() {
        return openOptions(0);
    }

    //Open the options window.
    private OptionsDialog openOptions(int index) {
        OptionsDialog dialog = new OptionsDialog(this, getWorkspace(), options, index);
        dialog.setVisible(true);
        return dialog;
    }
    
    private void showNews(JLayeredPane newsPanel) {
        final LauncherFrame self = this;
        newsPanel.setLayout(new NewsLayoutManager());
        newsPanel.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, 0));
        newsPanel.setBackground(bgColor);
        newsPanel.setForeground(fgColor);
        newsPanel.setOpaque(true);
        JEditorPane newsView = new JEditorPane();
        newsView.setEditable(false);
        newsView.setBorder(BorderFactory.createEmptyBorder());
        newsView.setFont(font);
        newsView.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    UIUtil.openURL(e.getURL(), self);
                }
            }
        });
        JScrollPane newsScroll = new JScrollPane(newsView);
        newsPanel.add(newsScroll, new Integer(1));
        JProgressBar newsProgress = new JProgressBar();
        newsProgress.setIndeterminate(true);
        newsPanel.add(newsProgress, new Integer(2));
        NewsFetcher.update(newsView, newsProgress);
    }

    /**
     * Build the UI.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        JLayeredPane newsPanel = new JLayeredPane();
        newsPanel.setBackground(bgColor);
        newsPanel.setForeground(fgColor);
        newsPanel.setOpaque(true);
        showNews(newsPanel);
        add(newsPanel, BorderLayout.CENTER);
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        add(leftPanel, BorderLayout.LINE_END);

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 3, 3, 0));
        playBtn = new JButton("Play");
        optionsBtn = new JButton("Options");
        buttonsPanel.add(playBtn);
        buttonsPanel.add(optionsBtn);
        buttonsPanel.setBackground(bgColor);
        buttonsPanel.setForeground(fgColor);
        playBtn.setFont(font);
        optionsBtn.setFont(font);
        playBtn.setForeground(fgColor);
        optionsBtn.setForeground(fgColor);

        JPanel root = new JPanel();
        root.setBorder(BorderFactory.createEmptyBorder(0, PAD, PAD, PAD));
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.add(createLoginPanel());
        root.add(buttonsPanel);
        root.setBackground(bgColor);
        root.setForeground(fgColor);
        leftPanel.add(root, BorderLayout.SOUTH);

        JPanel configurationsPanel = new JPanel();
        configurationsPanel.setLayout(new BorderLayout(0, 0));
        configurationsPanel.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
        configurationsPanel.setBackground(bgColor);
        configurationsPanel.setForeground(fgColor);
        configurationList = new JList(options.getConfigurations());
        configurationList.setCellRenderer(new ConfigurationCellRenderer());
        configurationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configurationList.setFont(font);
        
        JScrollPane configScroll = new JScrollPane(configurationList);
        configurationsPanel.add(configScroll, BorderLayout.CENTER);
        leftPanel.add(configurationsPanel, BorderLayout.CENTER);

        // Add listener
        playBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                launch();
            }
        });

        configurationList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                setConfiguration((Configuration) ((JList<?>) e.getSource()).getSelectedValue());
            }
        });

        optionsBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                openOptions();
            }
        });
        
    }

    /**
     * Create the login panel.
     * 
     * @return panel
     */
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        panel.setBackground(bgColor);
        panel.setForeground(fgColor);

        GridBagConstraints fieldC = new GridBagConstraints();
        fieldC.fill = GridBagConstraints.HORIZONTAL;
        fieldC.weightx = 1.0;
        fieldC.gridwidth = GridBagConstraints.REMAINDER;
        fieldC.insets = new Insets(2, 1, 2, 1);

        GridBagConstraints labelC = (GridBagConstraints) fieldC.clone();
        labelC.weightx = 0.0;
        labelC.gridwidth = 1;
        labelC.insets = new Insets(1, 1, 1, 10);

        GridBagConstraints checkboxC = (GridBagConstraints) fieldC.clone();
        checkboxC.insets = new Insets(5, 2, 1, 2);

        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);

        JLabel userLabel = new JLabel("Username:", SwingConstants.RIGHT);
        JLabel passLabel = new JLabel("Password:", SwingConstants.RIGHT);

        userText = new JComboBox<String>();
        userText.setEditable(true);
        passText = new JPasswordField(20);
        userLabel.setLabelFor(userText);
        passLabel.setLabelFor(passText);
        layout.setConstraints(userText, fieldC);
        layout.setConstraints(passText, fieldC);

        rememberPass = new JCheckBox("Remember my password");
        rememberPass.setBorder(null);
        rememberPass.setBackground(null);
        rememberPass.setForeground(fgColor);
        rememberPass.setFont(font);

        userLabel.setForeground(fgColor);
        userLabel.setFont(font);
        passLabel.setForeground(fgColor);
        passLabel.setFont(font);

        panel.add(userLabel, labelC);
        panel.add(userText, fieldC);
        panel.add(passLabel, labelC);
        panel.add(passText, fieldC);
        panel.add(rememberPass, checkboxC);
        panel.add(buttonsPanel);
        
        userText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadSavedPassword();
            }
        });

        userText.getEditor().getEditorComponent()
                .addKeyListener(new KeyAdapter() {
                    
                    public void keyReleased(KeyEvent e) {
                        Object text = userText.getSelectedItem();
                        if (text == null)
                            return;
                        if (options.getSavedPassword((String) text) != null) {
                            passText.setText("");
                            rememberPass.setSelected(false);
                        }

                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            launch();
                        }
                    }
                });

        userText.getEditor().getEditorComponent()
                .addMouseListener(new MouseAdapter() {
                    
                    public void mousePressed(MouseEvent e) {
                        maybeShowPopup(e);
                    }

                    
                    public void mouseReleased(MouseEvent e) {
                        maybeShowPopup(e);
                    }

                    private void maybeShowPopup(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            popupIdentityMenu(e.getComponent(), e.getX(),
                                    e.getY());
                        }
                    }
                });

        passText.addKeyListener(new KeyAdapter() {
            
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    launch();
                }
            }
        });

        return panel;
    }


    private void popupIdentityMenu(Component component, int x, int y) {
        final LauncherFrame self = this;

        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;

        final String username = getInputUsername();

        if (options.getSavedPassword(username) != null) {
            menuItem = new JMenuItem("Forget '" + username
                    + "' and its password");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    options.forgetIdentity(username);
                    if (username.equals(options.getLastUsername())) {
                        options.setLastUsername(null);
                    }
                    populateIdentities();
                    options.save();
                }
            });
            popup.add(menuItem);
        }

        menuItem = new JMenuItem("Forget all saved passwords...");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane
                        .showConfirmDialog(
                                self,
                                "Are you sure that you want to forget all saved passwords?",
                                "Forget passwords", JOptionPane.YES_NO_OPTION) == 0) {
                    options.forgetAllIdentities();
                    options.setLastUsername(null);
                    populateIdentities();
                    options.save();
                }
            }
        });
        popup.add(menuItem);

        popup.show(component, x, y);
    }

    /**
     * Update the drop down list of saved user/pass combinations.
     */
    private void populateIdentities() {
        Object selectedName = userText.getSelectedItem();
        String password = passText.getText();
        boolean remember = rememberPass.isSelected();

        userText.removeAllItems();

        for (String name : options.getSavedUsernames()) {
            userText.addItem(name);
        }

        userText.setSelectedItem(selectedName);
        passText.setText(password);
        rememberPass.setSelected(remember);
    }

    /**
     * Set the username field to the last saved username.
     */
    private void setLastUsername() {
        if (options.getLastUsername() != null) {
            userText.setSelectedItem(options.getLastUsername());
        }

        loadSavedPassword();
    }

    /**
     * Load the saved password for the current entered username.
     */
    private void loadSavedPassword() {
        LauncherOptions options = Launcher.getInstance().getOptions();

        Object selected = userText.getSelectedItem();
        if (selected != null) {
            String password = options.getSavedPassword(selected.toString());
            if (password != null) {
                passText.setText(password);
                rememberPass.setSelected(true);
            }
        }
    }

    /**
     * Launch the game.
     * 
     * @param autoConnect address to try auto-connecting to
     * @param test set test mode
     */
    public void launch() {
        if (worker.isAlive())
            return;

        Object selectedName = userText.getSelectedItem();

        if (selectedName == null) {
            JOptionPane.showMessageDialog(this, "A username must be entered.",
                    "No username", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (passText.getText().trim().length() == 0 && !options.getSettings().getBool(Def.LAUNCHER_ALLOW_OFFLINE_NAME, false)) {
            JOptionPane.showMessageDialog(this, "A password must be entered.",
                    "No password", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = selectedName.toString();
        String password = passText.getText();
        boolean remember = rememberPass.isSelected();

        // Save the identity
        if (remember && !options.getSettings().getBool(Def.LAUNCHER_ALLOW_OFFLINE_NAME, false)) {
            options.saveIdentity(username, password);
            options.setLastUsername(username);
        } else {
            options.forgetIdentity(username);
            options.setLastUsername(username);
        }

        options.setLastConfigName(getWorkspace().getId());
        options.save();

        // Want to update the GUI
        populateIdentities();
        LaunchTask task = new LaunchTask(this, getWorkspace(), username, password);
        task.setForceUpdate(options.getSettings().getBool(Def.LAUNCHER_GAMEUPDATE, false));
        task.setPlayOffline(options.getSettings().getBool(Def.LAUNCHER_ALLOW_OFFLINE_NAME, false));
        task.setShowConsole(options.getSettings().getBool(Def.LAUNCHER_LAUNCH_CONSOLE, false));
        options.getSettings().set(Def.LAUNCHER_GAMEUPDATE, false);
        options.getSettings().set(Def.LAUNCHER_ALLOW_OFFLINE_NAME, false);
        options.getSettings().set(Def.LAUNCHER_LAUNCH_CONSOLE, false);
        options.save();

        worker = Task.startWorker(this, task);
    }
}