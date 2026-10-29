package com.markes96.frame.display;

import java.awt.FlowLayout;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import javax.swing.JFrame;
import javax.swing.JLabel;
import com.markes96.constant.FrameConstant;
import com.markes96.flag.Flags;
import com.markes96.utils.FrameUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DisplayFrame extends JFrame {

  private static final String ESC_CLOSE = "Press [Esc] key to close ";
  private static final String X_STOP = "Press [X] key to stop ";
  private static final String P_PAUSE = "Press [P] key to Pause ";
  private static final String TIME = "Time => ";
  private static final String RUNNING = "Program [RUNNING]";
  private static final String PAUSED = "Program [PAUSED]";


  private final JLabel escLabel = FrameUtils.getNewLabel(ESC_CLOSE, FrameConstant.DISPLAYS_FONT);
  private final JLabel xLabel = FrameUtils.getNewLabel(X_STOP, FrameConstant.DISPLAYS_FONT);
  private final JLabel pLabel = FrameUtils.getNewLabel(P_PAUSE, FrameConstant.DISPLAYS_FONT);
  private final JLabel timeLabel = FrameUtils.getNewLabel(TIME, FrameConstant.DISPLAYS_FONT);
  private final JLabel stateLabel = FrameUtils.getNewLabel(RUNNING, FrameConstant.DISPLAYS_FONT);

  {

    this.add(this.escLabel);
    this.add(this.xLabel);
    this.add(this.pLabel);
    this.add(this.timeLabel);
    this.add(this.stateLabel);

    this.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
    this.setResizable(false);
    this.setUndecorated(true);
    this.setAlwaysOnTop(true);
    this.setLocation(20, 20);
    this.setSize(220, 185);
  }


  @Getter
  private Instant initInstant;
  private Instant pausedInstant;

  public void init() {
    this.initInstant = Instant.now();
    this.pausedInstant = null;
    this.setVisible(true);
  }

  public void pause() {
    this.pausedInstant = Instant.now();
  }

  public void play() {
    if (Objects.nonNull(this.pausedInstant)) {
      final Duration pauseDuration = FrameUtils.getDuration(this.pausedInstant);
      this.initInstant = this.initInstant.plus(pauseDuration);
      this.pausedInstant = null;
    }
  }

  public void actualize() {
    final Duration duration = FrameUtils.getDuration(this.initInstant);

    final StringBuilder sb = new StringBuilder(TIME);

    final long hour = duration.toHours();
    final long mins = duration.toMinutes() - (duration.toHours() * 60);
    final long seconds = duration.getSeconds() - (duration.toMinutes() * 60);

    if (hour != 0) {
      sb.append(hour);
      sb.append("h ");
    }

    if (mins != 0) {
      sb.append(mins);
      sb.append("m ");
    }

    sb.append(seconds);
    sb.append("s");

    this.timeLabel.setText(sb.toString());
  }

  public void checkIfsPaused() {
    this.stateLabel.setText(Flags.paused ? PAUSED : RUNNING);
  }

  private static DisplayFrame instance;

  public static DisplayFrame getInstance() {

    if (Objects.isNull(instance)) {
      instance = new DisplayFrame();
    }

    return instance;
  }
}
