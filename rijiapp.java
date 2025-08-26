package hello;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class rijiapp {
    private static int currentDeleteIndex = -1;

    // 文件路径常量
    private static final String DIARY_FILE = "C:\\Users\\24505\\IdeaProjects\\untitled1\\src\\hello\\riji"; // 日记数据文件
    private static final String USER_FILE = "C:\\Users\\24505\\IdeaProjects\\untitled1\\src\\hello\\user";   // 用户数据文件

    // 界面组件
    private static DefaultListModel<String> listModel;    // 日记列表数据模型
    private static JList<String> diaryList;               // 日记列表组件
    private static JPanel cardPanel;                      // 卡片布局主面板
    private static CardLayout cardLayout;                 // 卡片布局管理器

    // 输入组件（需要在多个方法中访问）
    private static JTextField titleField;                // 日记标题输入框
    private static JTextArea contentArea;                 // 日记内容输入区
    private static JSpinner dateSpinner;                  // 日期选择器
    private static JSpinner timeSpinner;                  // 时间选择器

    private static JTextField titleField1;                // 日记标题输入框
    private static JTextArea contentArea1;                 // 日记内容输入区
    private static JSpinner dateSpinner1;                  // 日期选择器
    private static JSpinner timeSpinner1;

    // 当前编辑的日记索引
    private static int currentEditIndex = -1;

    public static void main(String[] args) {
        JFrame frame = new JFrame("日记管理系统");
        frame.setSize(700, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        listModel = new DefaultListModel<>();
        diaryList = new JList<>(listModel);
        diaryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        diaryList.setFixedCellHeight(30);
        diaryList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        diaryList.setFocusable(true); // 关键修复：允许获取焦点

        // 初始化卡片布局
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        /* ========== 面板1：登录面板 ========== */
        JPanel loginPanel = createLoginPanel();
        cardPanel.add(loginPanel, "login");

        /* ========== 面板2：主功能菜单面板 ========== */
        JPanel mainPanel = createMainPanel();
        cardPanel.add(mainPanel, "main");

        /* ========== 面板3：创建日记面板 ========== */
        JPanel createPanel = createDiaryPanel();
        cardPanel.add(createPanel, "create");

        /* ========== 面板4：查看/管理日记面板 ========== */
        listModel = new DefaultListModel<>();
        loadDiaries();
        JPanel viewPanel = createViewPanel();
        cardPanel.add(viewPanel, "view");

        // 面板5：修改日记面板
        JPanel updatePanel = createUpdatePanel();
        cardPanel.add(updatePanel, "update");

        // 面板6：编辑日记面板
        JPanel editPanel = createEditPanel();
        cardPanel.add(editPanel, "edit");

        frame.add(cardPanel);
        frame.setVisible(true);
    }

    /**
     * 创建登录面板
     * 包含：用户名/密码输入框、登录/注册按钮
     */
    private static JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 标题标签
        JLabel titleLabel = new JLabel("日记管理系统", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 25, 112));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // 用户名输入
        JTextField username = new JTextField(20);
        username.setBorder(createInputBorder());
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("用户名:"), gbc);
        gbc.gridx = 1;
        panel.add(username, gbc);

        // 密码输入
        JPasswordField password = new JPasswordField(20);
        password.setBorder(createInputBorder());
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("密码:"), gbc);
        gbc.gridx = 1;
        panel.add(password, gbc);

        // 按钮
        JButton loginBtn = createStyledButton("登录", new Color(135, 206, 250));
        JButton registerBtn = createStyledButton("注册", new Color(152, 251, 152));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        // 事件处理
        loginBtn.addActionListener(e -> {
            if (authenticate(username.getText(), new String(password.getPassword()))) {
                JOptionPane.showMessageDialog(null, "登录成功！");
                cardLayout.show(cardPanel, "main");
            } else {
                JOptionPane.showMessageDialog(null, "用户名或密码错误");
            }
        });

        registerBtn.addActionListener(e -> {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE, true))) {
                bw.write(username.getText() + "=" + new String(password.getPassword()));
                bw.newLine();
                JOptionPane.showMessageDialog(null, "注册成功");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "注册失败: " + ex.getMessage());
            }
        });

        return panel;
    }

    /**
     * 创建主功能菜单面板
     * 包含：创建日记、查看日记、修改日记、删除日记、退出系统按钮
     */
    private static JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 功能按钮
        JButton createBtn = createStyledButton("创建日记", new Color(135, 206, 250));
        JButton viewBtn = createStyledButton("查看日记", new Color(144, 238, 144));
        JButton updateBtn = createStyledButton("修改日记", new Color(255, 215, 0));
        JButton deleteBtn = createStyledButton("删除日记", new Color(255, 182, 193));
        JButton exitBtn = createStyledButton("退出系统", new Color(220, 20, 60));

        // 统一按钮尺寸
        Dimension btnSize = new Dimension(200, 50);
        createBtn.setPreferredSize(btnSize);
        viewBtn.setPreferredSize(btnSize);
        updateBtn.setPreferredSize(btnSize);
        deleteBtn.setPreferredSize(btnSize);
        exitBtn.setPreferredSize(btnSize);

        // 添加按钮到面板
        gbc.gridx = 0;
        gbc.gridy = 0; panel.add(createBtn, gbc);
        gbc.gridy = 1; panel.add(viewBtn, gbc);
        gbc.gridy = 2; panel.add(updateBtn, gbc);
        gbc.gridy = 3; panel.add(deleteBtn, gbc);
        gbc.gridy = 4; panel.add(exitBtn, gbc);

        // 事件处理
        createBtn.addActionListener(e -> cardLayout.show(cardPanel, "create"));
        viewBtn.addActionListener(e -> {
            loadDiaries();
            cardLayout.show(cardPanel, "view");
        });
        updateBtn.addActionListener(e -> {
            loadDiaries();
            cardLayout.show(cardPanel, "update");
        });
        deleteBtn.addActionListener(e -> {
            loadDiaries();
            cardLayout.show(cardPanel, "view");
        });
        exitBtn.addActionListener(e -> System.exit(0));

        return panel;
    }

    /**
     * 创建日记编辑面板
     * 包含：标题输入、内容编辑、日期时间选择、保存/取消按钮
     */
    private static JPanel createDiaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 输入面板
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(new Color(240, 248, 255));

        // 标题输入区
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(new Color(240, 248, 255));
        titleField = new JTextField(25);
        titleField.setBorder(createInputBorder());
        titlePanel.add(new JLabel("标题:"));
        titlePanel.add(titleField);

        // 内容输入区
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(240, 248, 255));
        contentArea = new JTextArea(10, 30);
        contentArea.setLineWrap(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("内容:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        contentPanel.add(contentScroll, BorderLayout.CENTER);

        // 日期时间选择区
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.setBackground(new Color(121, 248, 255));

        // 日期选择器
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date());

        // 时间选择器
        timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setValue(new Date());

        timePanel.add(new JLabel("日期:"));
        timePanel.add(dateSpinner);
        timePanel.add(Box.createHorizontalStrut(20));
        timePanel.add(new JLabel("时间:"));
        timePanel.add(timeSpinner);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 248, 255));
        JButton saveBtn = createStyledButton("保存", new Color(144, 238, 144));
        JButton cancelBtn = createStyledButton("取消", new Color(255, 182, 193));

        buttonPanel.add(saveBtn);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(cancelBtn);

        // 组装输入面板
        inputPanel.add(titlePanel);
        inputPanel.add(Box.createVerticalStrut(15));
        inputPanel.add(contentPanel);
        inputPanel.add(Box.createVerticalStrut(15));
        inputPanel.add(timePanel);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(buttonPanel);

        panel.add(inputPanel, BorderLayout.CENTER);

        // 事件处理
        saveBtn.addActionListener(e -> saveDiary());
        cancelBtn.addActionListener(e -> {
            titleField.setText("");
            contentArea.setText("");
            cardLayout.show(cardPanel, "main");
        });

        return panel;
    }

    /**
     * 创建日记查看/管理面板
     * 包含：日记列表、返回/删除按钮
     */
    private static JPanel createViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 日记列表 - 使用全局实例
        diaryList.setModel(listModel); // 确保使用最新模型
        JScrollPane scrollPane = new JScrollPane(diaryList);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(176, 196, 222)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 248, 255));
        JButton backBtn = createStyledButton("返回", new Color(135, 206, 250));
        JButton deleteBtn = createStyledButton("删除", new Color(255, 182, 193));

        buttonPanel.add(backBtn);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(deleteBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // 事件处理
        backBtn.addActionListener(e -> cardLayout.show(cardPanel, "main"));
        deleteBtn.addActionListener(e -> {
            // 确保列表获得焦点
            diaryList.requestFocusInWindow();
            deleteSelectedDiary();
        });

        return panel;
    }

    /**
     * 创建修改日记面板
     * 包含：日记列表、返回/修改按钮
     */
    private static JPanel createUpdatePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 日记列表
        diaryList = new JList<>(listModel);
        diaryList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        diaryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        diaryList.setFixedCellHeight(30);
        diaryList.setBackground(Color.WHITE);
        diaryList.setFocusable(true);

        JScrollPane scrollPane = new JScrollPane(diaryList);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(176, 196, 222)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 248, 255));
        JButton backBtn = createStyledButton("返回", new Color(135, 206, 250));
        JButton editBtn = createStyledButton("修改", new Color(255, 215, 0));

        buttonPanel.add(backBtn);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(editBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // 事件处理
        backBtn.addActionListener(e -> cardLayout.show(cardPanel, "main"));
        editBtn.addActionListener(e -> {
            int index = diaryList.getSelectedIndex();
            if (index >= 0) {
                currentEditIndex = index;
                String selectedDiary = listModel.getElementAt(index);
                loadDiaryForEdit(selectedDiary);
                cardLayout.show(cardPanel, "edit");
            } else {
                JOptionPane.showMessageDialog(null, "请先选择要修改的日记");
            }
        });

        return panel;
    }

    /**
     * 创建编辑日记面板
     * 包含：标题输入、内容编辑、日期时间选择、保存修改/取消按钮
     */
    private static JPanel createEditPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 输入面板
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(new Color(240, 248, 255));

        // 标题输入区
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(new Color(240, 248, 255));
        titleField1 = new JTextField(25);
        titleField1.setBorder(createInputBorder());
        titlePanel.add(new JLabel("标题:"));
        titlePanel.add(titleField1);

        // 内容输入区
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(240, 248, 255));
        contentArea1 = new JTextArea(10, 30);
        contentArea1.setLineWrap(true);
        JScrollPane contentScroll = new JScrollPane(contentArea1);
        contentScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("内容:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        contentPanel.add(contentScroll, BorderLayout.CENTER);

        // 日期时间选择区
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.setBackground(new Color(240, 248, 255));

        // 日期选择器
        dateSpinner1 = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner1, "yyyy-MM-dd");
        dateSpinner1.setEditor(dateEditor);
        dateSpinner1.setValue(new Date());

        // 时间选择器
        timeSpinner1 = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner1, "HH:mm");
        timeSpinner1.setEditor(timeEditor);
        timeSpinner1.setValue(new Date());

        timePanel.add(new JLabel("日期:"));
        timePanel.add(dateSpinner1);
        timePanel.add(Box.createHorizontalStrut(20));
        timePanel.add(new JLabel("时间:"));
        timePanel.add(timeSpinner1);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 248, 255));
        JButton saveBtn = createStyledButton("保存修改", new Color(144, 238, 144));
        JButton cancelBtn = createStyledButton("取消", new Color(255, 182, 193));

        buttonPanel.add(saveBtn);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(cancelBtn);

        // 组装输入面板
        inputPanel.add(titlePanel);
        inputPanel.add(Box.createVerticalStrut(15));
        inputPanel.add(contentPanel);
        inputPanel.add(Box.createVerticalStrut(15));
        inputPanel.add(timePanel);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(buttonPanel);

        // 将输入面板添加到主面板
        panel.add(inputPanel, BorderLayout.CENTER);

        // 事件处理
        saveBtn.addActionListener(e -> updateDiary());
        cancelBtn.addActionListener(e -> cardLayout.show(cardPanel, "update"));

        return panel;
    }
    /**
     * 加载日记到编辑面板
     */
    private static void loadDiaryForEdit(String diary) {
        // 解析日记内容
        String[] parts = diary.split("->");
        if (parts.length >= 3) {
            // 提取各部分内容
            String timePart = parts[0].replace("时间:", "").trim();
            String titlePart = parts[1].replace("标题:", "").trim();
            String contentPart = parts[2].replace("内容:", "").trim();

            // 设置到编辑面板
            titleField1.setText(titlePart);
            contentArea1.setText(contentPart);

            try {
                // 解析日期时间
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = format.parse(timePart);
                dateSpinner1.setValue(date);
                timeSpinner1.setValue(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新日记
     */
    private static void updateDiary() {
        if (currentEditIndex == -1) {
            JOptionPane.showMessageDialog(null, "未选择要修改的日记");
            return;
        }

        // 获取编辑后的内容
        String title = titleField1.getText();
        String content = contentArea1.getText();
        Date date = (Date) dateSpinner1.getValue();
        Date time = (Date) timeSpinner1.getValue();

        // 合并日期和时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timestamp = format.format(calendar.getTime());

        String newDiary = String.format("时间:%s->标题:%s ->内容:%s",
                timestamp, title, content);

        // 更新列表模型
        listModel.set(currentEditIndex, newDiary);

        // 更新文件
        updateDiaryInFile(currentEditIndex, newDiary);

        JOptionPane.showMessageDialog(null, "日记修改成功");
        cardLayout.show(cardPanel, "view");
    }

    /**
     * 更新文件中的日记
     */
    private static void updateDiaryInFile(int index, String newDiary) {
        try {
            // 读取所有行
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(DIARY_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            }

            // 替换指定行的内容
            if (index >= 0 && index < lines.size()) {
                lines.set(index, newDiary);
            }

            // 重新写入文件
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(DIARY_FILE))) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "更新日记失败: " + e.getMessage());
        }
    }

    /**
     * 创建带样式的按钮
     */
    private static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }

    /**
     * 创建输入框边框样式
     */
    private static Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(176, 196, 222)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );
    }

    /**
     * 保存日记到文件
     */
    private static void saveDiary() {
        String title = titleField.getText();
        String content = contentArea.getText();
        Date date = (Date) dateSpinner.getValue();
        Date time = (Date) timeSpinner.getValue();

        // 合并日期和时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timestamp = format.format(calendar.getTime());

        String diaryEntry = String.format("时间:%s->标题:%s ->内容:%s",
                timestamp, title, content);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DIARY_FILE, true))) {
            bw.write(diaryEntry);
            bw.newLine();
            JOptionPane.showMessageDialog(null, "日记保存成功");
            titleField.setText("");
            contentArea.setText("");
            cardLayout.show(cardPanel, "main");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "保存失败: " + ex.getMessage());
        }
    }

    /**
     * 删除选中的日记
     */
    private static void deleteSelectedDiary() {
        // 获取当前选中的索引
        int index = diaryList.getSelectedIndex();
        System.out.println("删除操作 - 选中索引: " + index);

        if (index >= 0) {
            // 保存当前删除索引
            currentDeleteIndex = index;

            String diary = listModel.getElementAt(index);
            int choice = JOptionPane.showConfirmDialog(null,
                    "确定要删除这条日记吗？", "确认删除",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                // 从文件删除
                deleteDiaryFromFile(diary);

                // 从模型删除
                listModel.remove(index);

                // 刷新列表选择状态
                if (listModel.size() > 0) {
                    // 自动选择下一项或前一项
                    int newIndex = (index < listModel.size()) ? index : listModel.size() - 1;
                    diaryList.setSelectedIndex(newIndex);
                }

                JOptionPane.showMessageDialog(null, "日记删除成功");
            }
        } else {
            JOptionPane.showMessageDialog(null, "请先选择要删除的日记");
        }
    }

    /**
     * 从文件删除日记
     */
    private static void deleteDiaryFromFile(String diary) {
        try {
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(DIARY_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // 使用trim()避免空格问题
                    if (!line.trim().equals(diary.trim())) {
                        lines.add(line);
                    }
                }
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(DIARY_FILE))) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "删除失败: " + e.getMessage());
        }
    }
    /**
     * 用户认证
     */
    private static boolean authenticate(String user, String pass) {
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2 && parts[0].equals(user) && parts[1].equals(pass)) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * 从文件加载日记
     */
    private static void loadDiaries() {
        listModel.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(DIARY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                listModel.addElement(line);
            }
        } catch (IOException e) {
            try {
                new File(DIARY_FILE).createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
