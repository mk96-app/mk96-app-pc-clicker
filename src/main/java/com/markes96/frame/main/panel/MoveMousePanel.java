package com.markes96.frame.main.panel;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import com.markes96.constant.MoveMouseType;
import com.markes96.frame.main.MainFrame;
import com.markes96.frame.main.constrain.GridConstraints;
import com.markes96.utils.FrameUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MoveMousePanel extends JPanel implements ActionListener {

  // ============================================================================================
  // Definicion, inicializacion y configuracion de los elementos del panel
  // ============================================================================================

  // Elementos
  private final String moveMouseCheckLable = "Move mouse";
  private final JCheckBox moveMouseCheck = FrameUtils.getNewCheckBox("on/off");

  private final String moveTypeLable = "Move Type";
  private final JComboBox<MoveMouseType> moveType = FrameUtils.getNewComboBox(MoveMouseType.RANDOM,
      MoveMouseType.CIRCLE, MoveMouseType.POLYGON, MoveMouseType.REBOUND);

  private final String nVertexLable = "Vertex";
  private final JTextField nVertex = FrameUtils.getNewNumberField(3, 2, 300);

  private final String radiusLable = "Radius";
  private final JTextField radius = FrameUtils.getNewNumberField(10, 1, 100);

  // Paneles
  final JPanel mouseMovePanel =
      FrameUtils.getComponentPlusLabel(this.moveMouseCheckLable, this.moveMouseCheck);
  final JPanel moveTypePanel = FrameUtils.getComponentPlusLabel(this.moveTypeLable, this.moveType);
  final JPanel nVertexPanel = FrameUtils.getComponentPlusLabel(this.nVertexLable, this.nVertex);
  final JPanel radiusPanel = FrameUtils.getComponentPlusLabel(this.radiusLable, this.radius);

  {
    // añade los listeners
    this.moveMouseCheck.addActionListener(this);
    this.moveType.addActionListener(this);

    // Añade los paneles
    this.add(this.mouseMovePanel);
    this.setLayout(new GridLayout(1, 1, 0, 10));
  }

  public static Boolean isMoveMouseChecked() {
    return getInstance().moveMouseCheck.isSelected();
  }

  public static MoveMouseType getMoveType() {
    return (MoveMouseType) getInstance().moveType.getSelectedItem();
  }

  public static Integer getNVertex() {
    return new Integer(getInstance().nVertex.getText());
  }

  public static Integer getRadius() {
    return new Integer(getInstance().radius.getText());
  }

  // ============================================================================================
  // Acciones de los elementos del panel
  // ============================================================================================

  @Override
  public void actionPerformed(final ActionEvent e) {

    if (isMoveMouseChecked()) {
      this.setOn();
    } else {
      this.setOff();
    }

    MainFrame.resize();
    ButtonPanel.actualize();
  }

  private void setOn() {
    this.add(this.moveTypePanel);
    this.setVisibilities();
  }

  private void setVisibilities() {
    this.remove(this.nVertexPanel);
    this.remove(this.radiusPanel);

    switch (getMoveType()) {
      case CIRCLE:
        this.add(this.radiusPanel);
        this.setLayout(new GridLayout(3, 1, 0, 10));
        break;
      case POLYGON:
        this.add(this.nVertexPanel);
        this.add(this.radiusPanel);
        this.setLayout(new GridLayout(4, 1, 0, 10));
        break;
      case RANDOM:
      case REBOUND:
        this.setLayout(new GridLayout(2, 1, 0, 10));
        break;
    }
  }

  private void setOff() {

    this.remove(this.moveTypePanel);
    this.remove(this.nVertexPanel);
    this.remove(this.radiusPanel);

    this.setLayout(new GridLayout(1, 1, 0, 10));
  }


  // ============================================================================================
  // Declaracion del patron singleton
  // ============================================================================================

  private static MoveMousePanel instance;

  public static MoveMousePanel getInstance() {
    if (Objects.isNull(instance)) {
      instance = new MoveMousePanel();
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
