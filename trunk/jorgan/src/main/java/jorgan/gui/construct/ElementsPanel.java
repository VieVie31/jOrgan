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
package jorgan.gui.construct;

import java.util.*;
import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import spin.Spin;
import swingx.docking.DockedPanel;
import swingx.list.AbstractDnDListModel;
import swingx.list.DnDList;

import jorgan.disposition.*;
import jorgan.disposition.event.*;
import jorgan.gui.OrganSession;
import jorgan.gui.event.*;
import jorgan.play.event.*;

/**
 * Panel shows all elements.
 */
public class ElementsPanel extends DockedPanel {

  protected static final ResourceBundle resources = ResourceBundle.getBundle("jorgan.gui.resources");

  private static final Icon sortNameIcon =
    new ImageIcon(ElementsPanel.class.getResource("/jorgan/gui/img/sortName.gif"));

  private static final Icon sortTypeIcon =
    new ImageIcon(ElementsPanel.class.getResource("/jorgan/gui/img/sortType.gif"));
  
  /**
   * Icon used for indication an element.
   */
  private static final Icon elementIcon =
    new ImageIcon(ElementsPanel.class.getResource("/jorgan/gui/img/element.gif"));

  /**
   * Icon used for indication of a warning.
   */
  private static final Icon warningIcon =
    new ImageIcon(ElementsPanel.class.getResource("/jorgan/gui/img/elementWarning.gif"));

  /**
   * Icon used for indication of an error.
   */
  private static final Icon errorIcon =
    new ImageIcon(ElementsPanel.class.getResource("/jorgan/gui/img/elementError.gif"));

  /**
   * The edited organ.
   */
  private OrganSession session;

  /**
   * The handler of selection changes.
   */
  private SelectionHandler selectionHandler = new SelectionHandler();

  private AddAction addAction = new AddAction();
  private RemoveAction removeAction = new RemoveAction();

  private DnDList list = new DnDList();
  
  private JToggleButton sortNameButton = new JToggleButton(sortNameIcon);
  
  private JToggleButton sortTypeButton = new JToggleButton(sortTypeIcon);
  
  private ElementsModel elementsModel = new ElementsModel();
  
  private List elements = new ArrayList();
  
  /**
   * Create a tree panel.
   */
  public ElementsPanel() {
    
    addTool(addAction);
    
    addTool(removeAction);
    
    addToolSeparator();
    
    ButtonGroup sortGroup = new ButtonGroup();
    sortNameButton.getModel().setGroup(sortGroup);
    sortNameButton.setSelected(true);
    sortNameButton.setToolTipText(resources.getString("sort.name"));
    sortNameButton.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setOrgan(session);
      }
    });
    addTool(sortNameButton);
    
    sortTypeButton.getModel().setGroup(sortGroup);
    sortTypeButton.setToolTipText(resources.getString("sort.type"));
    sortTypeButton.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setOrgan(session);
      }
    });
    addTool(sortTypeButton);

    list.setModel(elementsModel);
    list.setCellRenderer(new ElementListCellRenderer());
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    list.addListSelectionListener(selectionHandler);
    
    setScrollableBody(list, true, false);
  }
   
  public OrganSession getOrgan() {
    return session;
  }
  
  /**
   * Set the organ to be edited.
   *
   * @param organ organ to be edited
   */
  public void setOrgan(OrganSession session) {
    
    if (this.session != null) {
      this.session.getOrgan().removeOrganListener((OrganListener)Spin.over(elementsModel));
      this.session.getPlay().removePlayerListener((PlayListener)Spin.over(elementsModel));
      this.session.getSelectionModel().removeSelectionListener(selectionHandler);

      int removed = elements.size();     
      elements = new ArrayList();
      elementsModel.fireRemoved(removed);
    }

    this.session = session;

    if (this.session != null) {
      this.session.getOrgan().addOrganListener((OrganListener)Spin.over(elementsModel));
      this.session.getPlay().addPlayerListener((PlayListener)Spin.over(elementsModel));
      this.session.getSelectionModel().addSelectionListener(selectionHandler);
  
      elements = new ArrayList(this.session.getOrgan().getElements());

      if (sortNameButton.isSelected()) {
        Collections.sort(elements, new ElementComparator(true));
      } else if (sortTypeButton.isSelected()) {
        Collections.sort(elements, new ElementComparator(false));
      }

      elementsModel.fireAdded(elements.size());
    }
    
    removeAction.update();      
  }
  
  /**
   * The handler of selections.
   */
  private class SelectionHandler implements ElementSelectionListener, ListSelectionListener {

    private boolean updatingSelection = false;
    
    public void selectionChanged(ElementSelectionEvent ev) {
      if (!updatingSelection) {
        updatingSelection = true;
          
        list.clearSelection();
       
        java.util.List selectedElements = session.getSelectionModel().getSelectedElements();    
        for (int e = 0; e < selectedElements.size(); e++) {
          Element element = (Element)selectedElements.get(e);
     
          int index = elements.indexOf(element);
          if (index != -1) {
            list.addSelectionInterval(index, index);
         
            if (e == 0) {
              list.scrollRectToVisible(list.getCellBounds(index, index));
            }
          }
        }

        updatingSelection = false;
      }
      
      removeAction.update();
    }
        
    public void valueChanged(ListSelectionEvent e) {
      if (!e.getValueIsAdjusting() && !updatingSelection) {
        updatingSelection = true;

        Object[] values = list.getSelectedValues();
        if (values.length == 1) {
          session.getSelectionModel().setSelectedElement((Element)values[0]);
        }else {
          session.getSelectionModel().setSelectedElements(Arrays.asList(values));
        }
        
        updatingSelection = false;
      }

      removeAction.update();
    }
  }

  /**
   * Note that <em>Spin</em> ensures that the organListener methods are called
   * on the EDT, although a change in the organ might be triggered by a change
   * on a MIDI thread.
   */
  private class ElementsModel extends AbstractDnDListModel implements PlayListener, OrganListener {

    public Object getElementAt(int index) {
      return elements.get(index);
    }

    public int indexOf(Object element) {
        return elements.indexOf(element);
    }
    
    public int getSize() {
      return elements.size();
    }

    public int getDropActions(Transferable transferable, int index) {
        return 0;
    }
    
    protected void insertElementAt(Object element, int index) {
    }

    protected void removeElement(Object element) {
    }

    public void outputProduced() { }
    
    public void inputAccepted() { }

    public void playerAdded(PlayEvent ev) { }
  
    public void playerRemoved(PlayEvent ev) { }

    public void problemAdded(PlayEvent ev) {
      updateProblem(ev);
    }
    
    public void problemRemoved(PlayEvent ev) {
      updateProblem(ev);
    }

    private void updateProblem(PlayEvent ev) {
      
      Element element = ev.getElement();
      int index = elements.indexOf(element);
      
      fireContentsChanged(this, index, index);
    }

    public void elementChanged(final OrganEvent event) {
      Element element = event.getElement();
      int index = elements.indexOf(element);
      
      fireContentsChanged(this, index, index);
    }

    public void elementAdded(OrganEvent event) {
      elements.add(event.getElement());
      
      int index = elements.size() - 1;
      fireIntervalAdded(this, index, index);
      
      Collections.sort(elements, new ElementComparator(sortNameButton.isSelected()));
      fireContentsChanged(this, 0, index);
      
      selectionHandler.selectionChanged(null);
    }

    public void elementRemoved(OrganEvent event) {
      int index = elements.indexOf(event.getElement());
      
      elements.remove(event.getElement());
      
      fireIntervalRemoved(this, index, index);
    }
    
    public void referenceAdded(OrganEvent event) {
    }

    public void referenceChanged(OrganEvent event) {
    }
    
    public void referenceRemoved(OrganEvent event) {
    }
     
    public void fireRemoved(int count) {
      
      if (count > 0) {
        fireIntervalRemoved(this, 0, count - 1);
      }   
    }
    
    public void fireAdded(int count) {
      if (count > 0) {
        fireIntervalAdded(this, 0, count - 1);   
      }   
    }
  }
  
  public class ElementListCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

      Element element = (Element)value;

      String name = ElementUtils.getElementAndTypeName(element, sortNameButton.isSelected());

      super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
      
      setIcon(elementIcon);
      
      if (session != null) {
        if (session.getPlay().hasErrors(element)) {
          setIcon(errorIcon);
        } else if (session.getPlay().hasWarnings(element)) {
          setIcon(warningIcon);
        }
      }
      
      return this;
    }
  }
    
  private class AddAction extends AbstractAction  {

    public AddAction() {
      putValue(Action.NAME             , resources.getString("construct.action.element.add.name"));
      putValue(Action.SHORT_DESCRIPTION, resources.getString("construct.action.element.add.description"));
      putValue(Action.SMALL_ICON       , new ImageIcon(ElementsPanel.class.getResource("/jorgan/gui/img/add.gif")));
   }

    public void actionPerformed(ActionEvent ev) {
        if (session != null) {
          Element prototype = null;
          if (session.getSelectionModel().getSelectionCount() == 1) {
            prototype = session.getSelectionModel().getSelectedElement();
          }
          CreateElementWizard.showInDialog((JFrame)SwingUtilities.getWindowAncestor(ElementsPanel.this), session.getOrgan(), prototype);
        }
    }
  }
  
  private class RemoveAction extends AbstractAction  {

    public RemoveAction() {
      putValue(Action.NAME             , resources.getString("construct.action.element.remove.name"));
      putValue(Action.SHORT_DESCRIPTION, resources.getString("construct.action.element.remove.description"));
      putValue(Action.SMALL_ICON, new ImageIcon(ElementsPanel.class.getResource("/jorgan/gui/img/remove.gif")));
   }

    public void actionPerformed(ActionEvent ev) {
      List selectedElements = session.getSelectionModel().getSelectedElements();
        
      for (int e = selectedElements.size() - 1; e >= 0; e--) {
        session.getOrgan().removeElement((Element)selectedElements.get(e));
      }
    }
    
    public void update() {
      setEnabled(session != null && session.getSelectionModel().isElementSelected());
    }
  }
}