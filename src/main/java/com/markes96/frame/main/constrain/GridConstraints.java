package com.markes96.frame.main.constrain;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GridConstraints extends GridBagConstraints {

  public static GridConstraints gc = new GridConstraints();

  static {
    gc.insets = new Insets(0, 0, 10, 0);
  }

  {
    super.gridwidth = GridBagConstraints.REMAINDER;
    super.fill = GridBagConstraints.HORIZONTAL;
  }

}
