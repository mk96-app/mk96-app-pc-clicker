package com.markes96.frame.main;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.util.Objects;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import com.markes96.frame.main.constrain.BottomConstraint;
import com.markes96.frame.main.panel.ButtonPanel;
import com.markes96.frame.main.panel.MouseClickPanel;
import com.markes96.frame.main.panel.MoveMousePanel;
import com.markes96.frame.main.panel.TimePanel;

public class MainFrame extends JFrame {

  private MainFrame() {
    super("MK96 PC Clicker");
  }

  {
    // Panel pricipal
    final JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());

    mainPanel.add(MoveMousePanel.getInstance(), MoveMousePanel.gc);
    mainPanel.add(new JSeparator(), new BottomConstraint(30));

    mainPanel.add(MouseClickPanel.getInstance(), MouseClickPanel.gc);
    mainPanel.add(new JSeparator(), new BottomConstraint(35));

    mainPanel.add(TimePanel.getInstance(), TimePanel.gc);
    mainPanel.add(new JSeparator(), new BottomConstraint(30));

    mainPanel.add(ButtonPanel.getInstance(), ButtonPanel.gc);


    // Frame principal
    this.setResizable(false);
    this.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
    this.add(mainPanel);
    this.pack();

    this.setLocationRelativeTo(null);
    this.setVisible(true);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public static void resize() {
    getInstance().pack();
  }

  private static MainFrame instance;

  public static MainFrame getInstance() {

    if (Objects.isNull(instance)) {
      instance = new MainFrame();
    }

    return instance;
  }
}
