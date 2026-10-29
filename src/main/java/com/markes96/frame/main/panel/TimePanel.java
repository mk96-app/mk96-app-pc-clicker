package com.markes96.frame.main.panel;

import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Objects;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import com.markes96.constant.MoveMouseType;
import com.markes96.frame.main.constrain.GridConstraints;
import com.markes96.utils.FrameUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimePanel extends JPanel {

  // ============================================================================================
  // Definicion, inicializacion y configuracion de los elementos del panel
  // ============================================================================================

  private final String moveMouseCheckLable = "Move mouse";
  private final JCheckBox moveMouseCheck = FrameUtils.getNewCheckBox("on/off");

  private final String moveTypeLable = "Move Type";
  private final JComboBox<MoveMouseType> moveType = FrameUtils.getNewComboBox(MoveMouseType.RANDOM,
      MoveMouseType.CIRCLE, MoveMouseType.POLYGON, MoveMouseType.REBOUND);

  private final String timeLable = "Time (min)";
  private final JTextField inputTime = FrameUtils.getNewNumberField();

  {
    // Crea el panel completo
    final JPanel timePanel = FrameUtils.getComponentPlusLabel(this.timeLable, this.inputTime);

    // Añade el panel
    this.add(timePanel);

    this.setLayout(new GridLayout(1, 1, 0, 0));
  }

  public static Integer getInputTime() {
    try {
      return new Integer(getInstance().inputTime.getText());
    } catch (final NumberFormatException e) {
      return -1;
    }
  }


  // ============================================================================================
  // Declaracion del patron singleton
  // ============================================================================================

  private static TimePanel instance;

  public static TimePanel getInstance() {
    if (Objects.isNull(instance)) {
      instance = new TimePanel();
    }
    return instance;
  }


  // ============================================================================================
  // Configuracion para la injeccion del elemento
  // ============================================================================================

  public static final GridConstraints gc = new GridConstraints();

  static {
    gc.insets = new Insets(0, 0, 10, 0);
  }

}
