package com.markes96.frame.main.panel;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import com.markes96.frame.main.MainFrame;
import com.markes96.frame.main.constrain.GridConstraints;
import com.markes96.utils.FrameUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MouseClickPanel extends JPanel implements ActionListener {

  // ============================================================================================
  // Definicion, inicializacion y configuracion de los elementos del panel
  // ============================================================================================

  // Elementos
  private final String mouseClickCheckLable = "Mouse click";
  private final JCheckBox mouseClickCheck = FrameUtils.getNewCheckBox("on/off");

  private final String clickPerSecLable = "Click/sec";
  private final JTextField clickPerSec = FrameUtils.getNewNumberField(1, 1, 1000);

  // Paneles
  // Crea los paneles
  final JPanel mouseClickCheckPanel =
      FrameUtils.getComponentPlusLabel(this.mouseClickCheckLable, this.mouseClickCheck);
  final JPanel clickPerSecPanel =
      FrameUtils.getComponentPlusLabel(this.clickPerSecLable, this.clickPerSec);

  {
    // añade los listeners
    this.mouseClickCheck.addActionListener(this);

    // Añade los paneles
    this.add(this.mouseClickCheckPanel);

    this.setLayout(new GridLayout(1, 1, 0, 10));
  }

  public static Boolean isMouseClickChecked() {
    return getInstance().mouseClickCheck.isSelected();
  }

  public static Integer getClickPerSec() {
    return new Integer(getInstance().clickPerSec.getText());
  }

  // ============================================================================================
  // Acciones de los elementos del panel
  // ============================================================================================

  @Override
  public void actionPerformed(final ActionEvent e) {

    if (isMouseClickChecked()) {
      this.setOn();
    } else {
      this.setOff();
    }

    MainFrame.resize();
    ButtonPanel.actualize();
  }

  private void setOn() {
    this.add(this.clickPerSecPanel);
    this.setLayout(new GridLayout(2, 1, 0, 10));
  }

  private void setOff() {
    this.remove(this.clickPerSecPanel);
    this.setLayout(new GridLayout(1, 1, 0, 10));
  }


  // ============================================================================================
  // Declaracion del patron singleton
  // ============================================================================================

  private static MouseClickPanel instance;

  public static MouseClickPanel getInstance() {
    if (Objects.isNull(instance)) {
      instance = new MouseClickPanel();
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
