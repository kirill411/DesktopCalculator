package calculator;

import javax.swing.*;

abstract class CalculatorButtons {

    private static final String[] buttonNames = {
            "Parentheses ()", "CancelEntry CE", "Clear C", "Delete Del",
            "PowerTwo x²", "PowerY xʸ", "SquareRoot √", "Divide \u00F7",
            "Seven 7", "Eight 8", "Nine 9", "Multiply \u00D7",
            "Four 4", "Five 5", "Six 6", "Subtract -",
            "One 1", "Two 2", "Three 3", "Add \u002B",
             "PlusMinus ±", "Zero 0", "Dot .", "Equals ="

    };

    static void addButtons(JPanel panel) {

        for (String s : buttonNames) {
            String[] namePair = s.split(" ");
            var button = new JButton(namePair[1]);
            button.setName(namePair[0]);
            button.addActionListener(e -> Calculator.actionParser(button.getText()));
            panel.add(button);
        }
    }
}
