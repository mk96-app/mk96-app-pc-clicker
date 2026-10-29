package com.markes96.frame.main.constrain;

import java.awt.GridBagConstraints;
import java.awt.Insets;

public class BottomConstraint extends GridConstraints {

  public BottomConstraint(final int bottom) {
    super.gridwidth = GridBagConstraints.REMAINDER;
    super.fill = GridBagConstraints.HORIZONTAL;
    super.insets = new Insets(0, 0, bottom, 0);
  }

}
