package com.markes96.frame.wait;

import java.awt.FlowLayout;
import java.util.Objects;
import javax.swing.JFrame;
import javax.swing.JLabel;
import com.markes96.constant.FrameConstant;
import com.markes96.utils.FrameUtils;

public class PlaceMouseFrame extends JFrame {

  private PlaceMouseFrame() {
    super("PlaceMouse");
  }

  private static final String GET_READY = "Get ready => ";
  private static final String GO = "GO!!!";
  private static final String PLACE_MOUSE = "Place the mouse";

  private final JLabel waitlabel = FrameUtils.getNewLabel(GET_READY, FrameConstant.DISPLAYS_FONT);
  private final JLabel setMouse = FrameUtils.getNewLabel(PLACE_MOUSE, FrameConstant.DISPLAYS_FONT);

  {
    this.add(this.waitlabel);
    this.add(this.setMouse);

    this.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
    this.setResizable(false);
    this.setUndecorated(true);
    this.setAlwaysOnTop(true);
    this.setLocation(20, 20);
    this.setSize(200, 80);
  }

  public void setTime(final int time) {
    this.waitlabel.setText((time < 1) ? GO : GET_READY + time);
  }

  private static PlaceMouseFrame instance;

  public static PlaceMouseFrame getInstance() {

    if (Objects.isNull(instance)) {
      instance = new PlaceMouseFrame();
    }

    return instance;
  }
}
