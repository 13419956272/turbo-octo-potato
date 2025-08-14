package hello;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class jisuanqiapp {
    private static JTextField displayField;
    private static String currentInput = "";
    private static double result = 0;
    private static String lastOperator = "";
    private static boolean newInput = true;
    private static boolean hasDecimal = false;

    public static void main(String[] args) {
        String[][] buttontext = {
                {"7", "8", "9", "/"},
                {"4", "5", "6", "*"},
                {"1", "2", "3", "-"},
                {"0", ".", "=", "+"}
        };

        JFrame jf = new JFrame("功能计算器");
        jf.setSize(300, 400);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setLayout(new BorderLayout(5, 5));

        // 创建显示面板
        displayField = new JTextField("0");
        displayField.setFont(new Font("Arial", Font.BOLD, 24));
        displayField.setEditable(false);
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        jf.add(displayField, BorderLayout.PAGE_START);

        // 创建AC/CE按钮面板
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton acButton = new JButton("AC");
        acButton.setBackground(new Color(255, 102, 102));
        acButton.setForeground(Color.WHITE);
        acButton.addActionListener(e -> {
            // 全清功能
            currentInput = "";
            result = 0;
            lastOperator = "";
            newInput = true;
            hasDecimal = false;
            displayField.setText("0");
        });

        JButton ceButton = new JButton("CE");
        ceButton.setBackground(new Color(255, 153, 51));
        ceButton.setForeground(Color.WHITE);
        ceButton.addActionListener(e -> {
            // 清除当前输入
            if (!currentInput.isEmpty()) {
                char lastChar = currentInput.charAt(currentInput.length() - 1);
                if (lastChar == '.') hasDecimal = false;

                currentInput = currentInput.substring(0, currentInput.length() - 1);

                if (currentInput.isEmpty()) {
                    displayField.setText("0");
                    newInput = true;
                } else {
                    displayField.setText(currentInput);
                }
            }
        });

        topPanel.add(acButton);
        topPanel.add(ceButton);
        jf.add(topPanel, BorderLayout.SOUTH);

        // 创建主按钮面板
        JPanel jp = new JPanel();
        jp.setLayout(new GridLayout(4, 4, 5, 5));

        // 创建按钮并添加事件监听器
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                JButton button = new JButton(buttontext[i][j]);

                // 设置操作符按钮样式
                if ("/*-+".contains(buttontext[i][j])) {
                    button.setBackground(new Color(102, 153, 255));
                    button.setForeground(Color.WHITE);
                } else if ("=".equals(buttontext[i][j])) {
                    button.setBackground(new Color(76, 175, 80)); // 绿色
                    button.setForeground(Color.WHITE);
                }

                // 添加事件监听器
                button.addActionListener(new ButtonClickListener(buttontext[i][j]));
                jp.add(button);
            }
        }

        jf.add(jp, BorderLayout.CENTER);
        jf.setVisible(true);
    }

    // 按钮点击监听器
    static class ButtonClickListener implements ActionListener {
        private final String buttonText;

        public ButtonClickListener(String text) {
            this.buttonText = text;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (buttonText) {
                case "0": case "1": case "2": case "3": case "4":
                case "5": case "6": case "7": case "8": case "9":
                    handleNumberInput(buttonText);
                    break;
                case ".":
                    handleDecimalPoint();
                    break;
                case "+": case "-": case "*": case "/":
                    handleOperator(buttonText);
                    break;
                case "=":
                    calculateResult();
                    break;
            }
        }

        private void handleNumberInput(String digit) {
            if (newInput) {
                currentInput = digit;
                newInput = false;
            } else {
                // 避免前导零
                if (currentInput.equals("0")) {
                    currentInput = digit;
                } else {
                    currentInput += digit;
                }
            }
            displayField.setText(currentInput);
        }

        private void handleDecimalPoint() {
            if (newInput) {
                currentInput = "0.";
                newInput = false;
                hasDecimal = true;
            } else if (!hasDecimal) {
                currentInput += ".";
                hasDecimal = true;
            }
            displayField.setText(currentInput);
        }

        private void handleOperator(String operator) {
            if (!currentInput.isEmpty()) {
                // 如果有待计算的操作
                if (!lastOperator.isEmpty()) {
                    calculateResult();
                } else {
                    result = Double.parseDouble(currentInput);
                }

                lastOperator = operator;
                newInput = true;
                hasDecimal = false;
            }
        }

        private void calculateResult() {
            if (lastOperator.isEmpty() || currentInput.isEmpty()) return;

            double currentValue = Double.parseDouble(currentInput);

            switch (lastOperator) {
                case "+":
                    result += currentValue;
                    break;
                case "-":
                    result -= currentValue;
                    break;
                case "*":
                    result *= currentValue;
                    break;
                case "/":
                    if (currentValue == 0) {
                        displayField.setText("错误: 除零");
                        clearState();
                        return;
                    }
                    result /= currentValue;
                    break;
            }

            // 格式化结果
            String resultText;
            if (result == (long) result) {
                resultText = String.format("%d", (long) result);
            } else {
                resultText = String.format("%.6f", result);
                // 移除多余的零
                resultText = resultText.replaceAll("0*$", "").replaceAll("\\.$", "");
            }

            displayField.setText(resultText);
            currentInput = resultText;
            lastOperator = "";
            newInput = true;
        }

        private void clearState() {
            currentInput = "";
            result = 0;
            lastOperator = "";
            newInput = true;
            hasDecimal = false;
        }
    }
}