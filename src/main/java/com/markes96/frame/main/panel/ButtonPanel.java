package com.markes96.frame.main.panel;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import com.markes96.constant.FrameConstant;
import com.markes96.flag.Flags;
import com.markes96.frame.main.MainFrame;
import com.markes96.frame.main.constrain.GridConstraints;
import com.markes96.utils.FieldUtils;
import com.markes96.utils.FrameUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ButtonPanel extends JPanel implements ActionListener {

  // ============================================================================================
  // Definicion, inicializacion y configuracion de los elementos del panel
  // ============================================================================================

  private final JButton buttonStart = FrameUtils.getNewButtom(FrameConstant.START_BUTTOM);
  private final JButton buttonInfo = FrameUtils.getNewButtom(FrameConstant.INFO_BUTTOM);
  private final JButton buttonClose = FrameUtils.getNewButtom(FrameConstant.CLOSE_BUTTOM);

  {
    // Añade los listeners
    this.buttonStart.addActionListener(this);
    this.buttonInfo.addActionListener(this);
    this.buttonClose.addActionListener(this);

    // añade los botones buttoms
    this.add(this.buttonStart);
    this.add(this.buttonInfo);
    this.add(this.buttonClose);

    this.buttonStart.setEnabled(false);
    this.setLayout(new GridLayout(1, 3, 5, 0));
  }


  // ============================================================================================
  // Acciones de los elementos del panel
  // ============================================================================================

  public static void actualize() {
    getInstance().buttonStart
        .setEnabled(MoveMousePanel.isMoveMouseChecked() || MouseClickPanel.isMouseClickChecked());
  }

  @Override
  public void actionPerformed(final ActionEvent event) {

    switch (event.getActionCommand()) {
      case FrameConstant.START_BUTTOM:
        this.actionButtomStart();
        break;

      case FrameConstant.INFO_BUTTOM:
        this.actionButtomInfo();
        break;

      case FrameConstant.CLOSE_BUTTOM:
        this.actionButtomClose();
        break;
    }

  }

  private void actionButtomStart() {
    Flags.on = true;
  }

  private void actionButtomInfo() {
    try {
      final String infoMessage =
          FieldUtils.loadFileFromRelativePath(String.class, "files", "ProgramInfo.txt");

      final JLabel label = FrameUtils.getNewLabel(infoMessage);
      label.setFont(FrameConstant.INFO_FONT);
      JOptionPane.showMessageDialog(MainFrame.getInstance(), label);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void actionButtomClose() {
    System.exit(0);
  }


  // ============================================================================================
  // Declaracion del patron singleton
  // ============================================================================================

  private static ButtonPanel instance;

  public static ButtonPanel getInstance() {

    if (Objects.isNull(instance)) {
      instance = new ButtonPanel();
    }

    return instance;
  }


  // ============================================================================================
  // Configuracion para la injeccion del elemento
  // ============================================================================================

  public static final GridConstraints gc = new GridConstraints();

  static {
    gc.insets = new Insets(0, 0, 5, 0);
  }

}
