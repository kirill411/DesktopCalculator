package calculator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

public class Calculator extends JFrame {

    private static boolean refreshLabels = false;
    private static boolean autoCloseParentheses = false;
    private static boolean error = false;

    static int leftParentheses = 0;
    static int rightParentheses = 0;

    private static final JLabel RESULT_LABEL = new JLabel("0");
    private static final JLabel EQUATION_LABEL = new JLabel("");

    public Calculator() {
        super("Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 450);
        setResizable(false);
        setLayout(null);

        initComponents();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {

        RESULT_LABEL.setName("ResultLabel");
        RESULT_LABEL.setFont(new Font("Arial", Font.BOLD, 40));
        RESULT_LABEL.setBounds(10, 20, 330, 40);
        RESULT_LABEL.setHorizontalAlignment(SwingConstants.RIGHT);
        add(RESULT_LABEL);

        EQUATION_LABEL.setName("EquationLabel");
        EQUATION_LABEL.setFont(new Font("Arial", Font.BOLD, 12));
        EQUATION_LABEL.setBounds(10, 90, 330, 12);
        EQUATION_LABEL.setHorizontalAlignment(SwingConstants.RIGHT);
        add(EQUATION_LABEL);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBounds(5, 125, 340, 275);
        buttonPanel.setLayout(new GridLayout(6, 4, 1, 1));
        CalculatorButtons.addButtons(buttonPanel);
        add(buttonPanel);
    }

    static void actionParser(String buttonText) {
        if (refreshLabels) {
            RESULT_LABEL.setText("0");
            EQUATION_LABEL.setText("");
            refreshLabels = false;
        }
        String eqText = EQUATION_LABEL.getText();
        char lastChar = eqText.isBlank() ? ' '  : eqText.charAt(eqText.length() - 1);

        switch (buttonText) {
            case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".":
                if (error) {
                    break;
                }
                EQUATION_LABEL.setText(eqText + buttonText);
                break;
            case "()":
                EQUATION_LABEL.setText(addParentheses(eqText));
                break;
            case "C":
                RESULT_LABEL.setText("0");
                EQUATION_LABEL.setText("");
                autoCloseParentheses = false;
                error = false;
                break;
            case "Del", "CE":
                EQUATION_LABEL.setText(deleteLastChar(eqText));
                error = false;
                break;
            case "=":
                if (!Character.isDigit(lastChar) && lastChar != ')' || eqText.equals("(-√(9))")) {
                    error = true;
                    break;
                }
                try {
                    RESULT_LABEL.setText(getResult(eqText));
                } catch (Exception e) {
                    error = true;
                    System.err.println(e.getMessage());
                }
                refreshLabels = true;
                break;
            case "+", "-", "×", "÷":
                if (autoCloseParentheses && leftParentheses != rightParentheses) {
                    eqText += ")";
                    autoCloseParentheses = false;
                }
                EQUATION_LABEL.setText(addOperator(eqText, buttonText));
                break;
            case "√":
                EQUATION_LABEL.setText(eqText + "√(");
                break;
            case "x²","xʸ":
                EQUATION_LABEL.setText(addExponentiation(eqText, buttonText));
                break;
            case "±":
                EQUATION_LABEL.setText(negate(eqText));
                break;
        }
        countParentheses();
        EQUATION_LABEL.setForeground(error ? Color.RED.darker() : Color.BLACK);
    }

    private static String getResult(String equation) {
        String[] array = infixToPostfix(equation);
        Stack<String> stack = new Stack<>();

        for (String s : array) {
            if (s.matches("[-÷×+^]")) {
                if (s.equals("-") && stack.size() == 1) {
                    double n = -Double.parseDouble(stack.pop());
                    stack.push(n + "");
                    continue;
                }
                double b = Double.parseDouble(stack.pop());
                double a = Double.parseDouble(stack.pop());
                stack.push(evaluate(a, b, s));
            } else if (s.equals("√")) {
                double a = Double.parseDouble(stack.pop());
                stack.push(Math.sqrt(a) + "");
            } else {
                stack.push(s);
            }
        }
        return stack.pop().replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    private static String[] infixToPostfix(String s) {
        StringBuilder result = new StringBuilder();
        Deque<Character> stack = new ArrayDeque<>();

        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                result.append(c);
            } else if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                result.append(" ");
                while (!stack.isEmpty()
                        && stack.peek() != '(') {
                    result.append(stack.peek());
                    result.append(" ");
                    stack.pop();
                }
                stack.pop();
            } else {
                result.append(" ");
                while (!stack.isEmpty()
                        && getPrecedence(c) <= getPrecedence(stack.peek())) {
                    result.append(stack.peek());
                    result.append(" ");
                    stack.pop();
                }
                stack.push(c);
            }
        }
        result.append(" ");

        while (!stack.isEmpty()) {
            if (stack.peek() == '(' || stack.peek() == ')') {
                stack.pop();
            }
            result.append(stack.peek());
            result.append(" ");
            stack.pop();
        }
        return result.toString().trim().split("\\s+");
    }

    private static String evaluate(double a, double b, String operator) {
        if (operator.equals("÷") && b == 0) {
            throw new ArithmeticException();
        }
        double result = switch (operator) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "×" -> a * b;
            case "÷" -> a / b;
            case "^" -> Math.pow(a, b);
            default -> 0;
        };
        return result + "";
    }

    private static String deleteLastChar(String text) {
        if (text.isEmpty()) {
            return "";
        }
        return text.substring(0, text.length() - 1);
    }

    private static String addParentheses(String text) {
        if (text.isEmpty()) {
            return "(";
        }
        char c = text.charAt(text.length() - 1);
        if (c == '('
                || leftParentheses == rightParentheses
                || String.valueOf(c).matches("[-+×÷]")) {
            return text + "(";
        } else {
            autoCloseParentheses = false;
            return text + ")";
        }
    }

    private static String addExponentiation(String eqText, String buttonText) {
        if (eqText.isEmpty()) {
            return "";
        }
        if (buttonText.equals("x²")) {
            return eqText + "^(2)";
        } else {
            autoCloseParentheses = true;
            return eqText + "^(";
        }
    }

    private static String negate(String text) {
        if (text.startsWith("(-")) {
            return text.substring(2);
        } else {
            return "(-" + text;
        }
    }

    private static String addOperator(String text, String buttonText) {
        if (text.isEmpty()) {
            return "";
        }
        text = numberFormatCorrection(text);
        if (Character.isDigit(text.charAt(text.length() - 1)) || text.charAt(text.length() - 1) == ')') {
            return text + buttonText;
        } else {
            return text.substring(0, text.length() - 1) + buttonText;
        }
    }

    private static String numberFormatCorrection(String s) {
        String[] eqArray = s.split("(?=\\d|.)(?<=[-÷×+])|(?<=\\d|.)(?=[-÷×+])");
        String lastNum = eqArray[eqArray.length - 1];
        if (lastNum.startsWith(".")) {
            eqArray[eqArray.length - 1] = "0" + lastNum;
        } else if (lastNum.endsWith(".")) {
            eqArray[eqArray.length - 1] = lastNum + "0";
        } else if (lastNum.matches("\\d+\\.0*$")) {
            lastNum = lastNum.replaceAll("0*$", "").replaceAll("\\.$", "");
            eqArray[eqArray.length - 1] = lastNum;
        }
        return String.join("", eqArray);
    }

    private static int getPrecedence(char c) {
        return switch (c) {
            case '-', '+' -> 1;
            case '÷','×' -> 2;
            case '^', '√' -> 3;
            default -> 0;
        };
    }

    private static void countParentheses() {
        leftParentheses = 0;
        rightParentheses = 0;
        for (char c : EQUATION_LABEL.getText().toCharArray()) {
            if (c == '(') {
                leftParentheses++;
            }
            if (c == ')') {
                rightParentheses++;
            }
        }
    }
}
