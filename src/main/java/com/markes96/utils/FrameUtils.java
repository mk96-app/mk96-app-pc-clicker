package com.markes96.utils;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import com.markes96.constant.FrameConstant;
import com.markes96.frame.main.MainFrame;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FrameUtils {

  public JButton getNewButtom(final String text) {
    return new JButton(text);
  }

  public JLabel getNewLabel(final String text) {
    return getNewLabel(text, null);
  }

  public JLabel getNewLabel(final String text, final Font font) {
    final JLabel label = new JLabel(text);
    if (Objects.nonNull(font)) {
      label.setFont(font);
    }
    return label;

  }

  public JTextField getNewTextField(final String text) {
    return new JTextField(text);
  }

  public <T> JComboBox<T> getNewComboBox(final T... values) {
    return new JComboBox<>(values);
  }

  public JCheckBox getNewCheckBox(final String text) {
    return new JCheckBox(text);
  }

  public JPanel getComponentPlusLabel(final String lableText, final JComponent componen) {
    final JPanel panel = new JPanel(new GridLayout(0, 2, 10, 0));
    final JLabel label = getNewLabel(lableText);
    panel.add(label);
    panel.add(componen);
    return panel;
  }

  public JTextField getNewNumberField() {
    return getNewNumberField(null);
  }

  public JTextField getNewNumberField(final Integer defaultValue) {
    return getNewNumberField(defaultValue, FrameConstant.MAX_NUMBER_INPUT_VALUE);
  }

  public JTextField getNewNumberField(final Integer defaultValue, final Integer maxValue) {
    return getNewNumberField(defaultValue, 0, maxValue);
  }

  public JTextField getNewNumberField(final Integer defaultValue, final Integer minValue,
      final Integer maxValue) {

    final String defaultValueString = Objects.nonNull(defaultValue) ? defaultValue.toString() : "";
    final JTextField numberField = new JTextField(defaultValueString);

    numberField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(final FocusEvent event) {
        if (numberField.getText().trim().isEmpty()) {
          numberField.setText(defaultValueString);
        } else {
          try {
            final Integer value = new Integer(numberField.getText());
            if (value < minValue) {
              numberField.setText(minValue.toString());
            }
          } catch (final NumberFormatException e) {
          }
        }
      }
    });

    numberField.addKeyListener(new KeyAdapter() {

      @Override
      public void keyReleased(final KeyEvent event) {
        if (StringUtils.isNotEmpty(numberField.getText())) {
          try {
            final Integer value = new Integer(numberField.getText());
            if (value > maxValue) {
              numberField.setText(maxValue.toString());
            }
          } catch (final NumberFormatException exception) {
            final String message =
                "Error input number format: " + numberField.getText() + "\nOnly numbers permited";
            JOptionPane.showMessageDialog(MainFrame.getInstance(), message);
            numberField.setText(defaultValueString);
          }
        }
      }
    });

    return numberField;
  }

  public Duration getDuration(final Instant initTime) {
    return Duration.between(initTime, Instant.now());
  }

}
