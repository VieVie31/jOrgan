/*
 * jOrgan - Java Virtual Organ
 * Copyright (C) 2003 Sven Meier
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jorgan.swing.wizard;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jorgan.swing.*;

/**
 * A wizard like dialog.
 */
public class WizardDialog extends StandardDialog {

  /**
   * The resource bundle.
   */
  protected static ResourceBundle resources = ResourceBundle.getBundle("jorgan.swing.resources");

  private Action previousAction = new PreviousAction();
  private Action nextAction     = new NextAction();
  private Action finishAction   = new FinishAction();
  private Action cancelAction   = new CancelAction();
  
  private WizardListener listener = new InternalWizardListener();
  
  private Wizard wizard;
  
  /**
   * Constructor.
   */
  public WizardDialog(Frame owner) {
    super(owner);

    setTitle(resources.getString("wizard.title"));

    addAction(previousAction);
    addAction(nextAction);
    addAction(finishAction, true);
    addAction(cancelAction);
    
    setWizard(new BasicWizard());       
  }

  public void setWizard(Wizard wizard) {
    if (this.wizard != null) {
      this.wizard.removeWizardListener(listener);
    }
    
    this.wizard = wizard;
    
    if (this.wizard != null) {
      this.wizard.addWizardListener(listener);

      listener.wizardChanged();
    }
  }
  
  private class PreviousAction extends AbstractAction {

    public PreviousAction() {
      putValue(Action.NAME, resources.getString("wizard.previous"));
    }

    public void actionPerformed(ActionEvent ev) {
      wizard.previous();
    }
  }

  private class NextAction extends AbstractAction {

    public NextAction() {
      putValue(Action.NAME, resources.getString("wizard.next"));
    }

    public void actionPerformed(ActionEvent ev) {
      wizard.next();
    }
  }

  private class FinishAction extends AbstractAction {

    public FinishAction() {
      putValue(Action.NAME, resources.getString("wizard.finish"));
    }

    public void actionPerformed(ActionEvent ev) {
      wizard.finish();
    }
  }

  private class CancelAction extends AbstractAction {

    public CancelAction() {
      putValue(Action.NAME, resources.getString("wizard.cancel"));
    }

    public void actionPerformed(ActionEvent ev) {
      setVisible(false);
    }
  }
  
  private class InternalWizardListener implements WizardListener {
    public void wizardChanged() {
      Page current = wizard.getCurrentPage();
      if (current == null) {
        setContent(null);
        previousAction.setEnabled(false);
        nextAction    .setEnabled(false);
        finishAction  .setEnabled(false);
      } else {
        JComponent component = current.getComponent();
        if (component == null) {
          setContent(null);
          setDescription(null);
        } else {
          if (!component.equals(getContent())) {
            setContent(component);
          }
          setDescription(current.getDescription());
        }
        previousAction.setEnabled(wizard.hasPrevious() && wizard.getCurrentPage().allowsPrevious());
        nextAction    .setEnabled(wizard.hasNext()     && wizard.getCurrentPage().allowsNext());
        finishAction  .setEnabled(wizard.allowsFinish());
      }
    }
     
    public void wizardCanceled() {
      setVisible(false);
    }
    
    public void wizardFinished() {
      setVisible(false);
    }
  }
}