package org.kaizen.ui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Proto type of a time based field.
 * 
 * The user should get real time validation based on the configuration of the field
 * and the values they are entering (hours &gt; 0 and &lt; 23 for 24 clocks, 
 * &gt; 0 &lt; 13 for 12 hour clocks, minutes &gt;= 0 and &lt; 60, etc)
 * 
 * The field should also provide easy navigation, including automatically moving
 * to the next time component, tab, keyboard arrows and separators (ie ":" between
 * the hours an minutes)
 * 
 * The intention is to make it as fast as possible for the user to enter values
 * 
 * This is the original prototype and should be update to use the AbstractTimeField
 * 
 * Consideration should also be given to allow it to display seconds and
 * milliseconds, including the localised separators
 */
public class TimeField extends javax.swing.JPanel {

  // The time of day...
  public enum TimeOfDay {

    AM,
    PM
  }
  private HourDocumentFilter hourDocumentFilter;
  private MinuteDocumentFilter minDocumentFilter;
  private HourKeyHandler hourKeyHandler;
  private MinuteKeyHandler minuteKeyHandler;
  private HourFocusHandler hourFocusHandler;
  private MinuteFocusHandler minuteFocusHandler;
  private boolean use24HourClock;
  private ActionHandler actionHandler;

  /**
   * Creates new form TimeField
   */
  public TimeField() {
    initComponents();
    pnlFields.setBorder(new CompoundBorder(UIManager.getBorder("TextField.border"),new EmptyBorder(0, 2, 0, 2)));

    set24HourClock(false);
    setTime(new Date());
    fldHour.addKeyListener(new HourKeyHandler());
  }

  @Override
  public void addNotify() {
    super.addNotify();
    // Add all the required functionality to make this thing work...
    ((AbstractDocument) fldHour.getDocument()).setDocumentFilter(getHourDocumentFilter());
    ((AbstractDocument) fldMin.getDocument()).setDocumentFilter(getMinuteDocumentFilter());
    fldHour.addFocusListener(getHourFocusHandler());
    fldMin.addFocusListener(getMinuteFocusHandler());
    fldHour.addKeyListener(getHourKeyHandler());
    fldMin.addKeyListener(getMinuteKeyHandler());
    fldHour.addActionListener(getActionHandler());
    fldMin.addActionListener(getActionHandler());
    cmbTimeOfDay.addActionListener(getActionHandler());
  }

  @Override
  public void removeNotify() {
    // Clean up our listeners...
    ((AbstractDocument) fldHour.getDocument()).setDocumentFilter(null);
    ((AbstractDocument) fldMin.getDocument()).setDocumentFilter(null);
    fldHour.removeFocusListener(getHourFocusHandler());
    fldMin.removeFocusListener(getMinuteFocusHandler());
    fldHour.removeKeyListener(getHourKeyHandler());
    fldMin.removeKeyListener(getMinuteKeyHandler());
    fldHour.removeActionListener(getActionHandler());
    fldMin.removeActionListener(getActionHandler());
    cmbTimeOfDay.removeActionListener(getActionHandler());
    super.removeNotify();
  }

  /**
   * Adds an action listener to the component. Actions are fired when the user
   * presses the enter key
   *
   * @param listener
   */
  public void addActionListener(ActionListener listener) {
    listenerList.add(ActionListener.class, listener);
  }

  public void removeActionListener(ActionListener listener) {
    listenerList.remove(ActionListener.class, listener);
  }

  /**
   * Returns the field that is acting as the hour editor
   *
   * @return
   */
  public JTextField getHourEditor() {
    return fldHour;
  }

  /**
   * Returns the field that is acting as the minute editor
   *
   * @return
   */
  public JTextField getMinuteEditor() {
    return fldMin;
  }

  /**
   * Returns the combo box that provides the time of day selection
   *
   * @return
   */
  public JComboBox getTimeOfDayEditor() {
    return cmbTimeOfDay;
  }

  /**
   * Returns the internal action handler. This handler monitors actions on the
   * individual components and merges them into one.
   *
   * @return
   */
  protected ActionHandler getActionHandler() {
    if (actionHandler == null) {
      actionHandler = new ActionHandler();
    }
    return actionHandler;
  }

  /**
   * Returns the hour key listener
   *
   * @return
   */
  protected HourKeyHandler getHourKeyHandler() {
    if (hourKeyHandler == null) {
      hourKeyHandler = new HourKeyHandler();
    }
    return hourKeyHandler;
  }

  /**
   * Returns the minute key listener
   *
   * @return
   */
  protected MinuteKeyHandler getMinuteKeyHandler() {
    if (minuteKeyHandler == null) {
      minuteKeyHandler = new MinuteKeyHandler();
    }
    return minuteKeyHandler;
  }

  /**
   * Returns the document filter used to filter the hour field
   *
   * @return
   */
  protected DocumentFilter getHourDocumentFilter() {
    if (hourDocumentFilter == null) {
      hourDocumentFilter = new HourDocumentFilter();
    }
    return hourDocumentFilter;
  }

  /**
   * Returns the document filter user to filter the minute field
   *
   * @return
   */
  protected DocumentFilter getMinuteDocumentFilter() {
    if (minDocumentFilter == null) {
      minDocumentFilter = new MinuteDocumentFilter();
    }
    return minDocumentFilter;
  }

  /**
   * Returns the focus listener used to monitor the hour field
   *
   * @return
   */
  protected HourFocusHandler getHourFocusHandler() {
    if (hourFocusHandler == null) {
      hourFocusHandler = new HourFocusHandler();
    }
    return hourFocusHandler;
  }

  /**
   * Used the focus listener used to monitor the minute field
   *
   * @return
   */
  protected MinuteFocusHandler getMinuteFocusHandler() {
    if (minuteFocusHandler == null) {
      minuteFocusHandler = new MinuteFocusHandler();
    }
    return minuteFocusHandler;
  }

  /**
   * Sets the time based on the supplied date
   *
   * @param date
   */
  public void setTime(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int hour = cal.get(Calendar.HOUR);
    int min = cal.get(Calendar.MINUTE);
    int dayPart = cal.get(Calendar.AM_PM);

    TimeOfDay timeOfDay = TimeOfDay.AM;
    switch (dayPart) {
      case Calendar.PM:
        timeOfDay = TimeOfDay.PM;
        break;
    }

    setTime(hour, min, timeOfDay);
  }

  /**
   * Sets the time based on a 24 hour clock. The field does not need to be in 24
   * hour mode to use this method, the method will automatically correct the
   * hour appropriately.
   *
   * @param hour
   * @param min
   */
  public void setTime(int hour, int min) {
    hour = correctHour(hour);
    min = correctMinute(min);

    TimeOfDay timeOfDay = TimeOfDay.AM;
    if (hour >= 12) {
      timeOfDay = TimeOfDay.PM;
    }

    setTime(hour, min, timeOfDay);
  }

  /**
   * Corrects the minute value to make sure it is within allowable ranges.
   *
   * For example, if you pass in 90 the method, it will automatically correct
   * the value to 30, discard the overflow.
   *
   * This will not effect the hour value...although this might be worth
   * consideration in the future
   *
   * @param min
   * @return
   */
  protected int correctMinute(int min) {
    // Make sure the value is positive.
    // If we were interested in altering the hour value as well, we wouldn't
    // want to do this...
    if (min < 0) {
      min += (min * -2);
    }

    // Correct the minute value....
    if (min > 59) {
      // How many hours fit into this value
      float part = min / 60f;
      part = (float) (part - Math.floor(part)); // Get remainder
      min = (int) (60 * part); // Calculate the number of minutes...
    }
    return min;
  }

  /**
   * Basically, this method will attempt to correct the hour value and bring the
   * it into range of a single day.
   *
   * We are basically going to try and figure out how many parts of the day that
   * the hour falls in and make it equal to a single day...
   *
   * That is, if the hour is 35, it's actually 1.458... days, which is roughly 1
   * day and 11 hours. We are only interested in the 11 hours, cause the date is
   * irrelevant to us
   *
   * @param hour
   * @return
   */
  protected int correctHour(int hour) {
    if (hour < 0) {
      hour += (hour * -2);
    }

    if (hour > 23) {
      float part = hour / 24f;
      part = (float) (part - Math.floor(part));
      hour = (int) (24 * part);
    }
    return hour;
  }

  /**
   * Sets the time value for this field...
   *
   * @param hour
   * @param min
   * @param timeOfDay
   */
  public void setTime(int hour, int min, TimeOfDay timeOfDay) {
    hour = correctHour(hour);
    min = correctMinute(min);

    // Now that we have a correct hour value, we need to know if it will
    // actually fit within the correct part of the day...

    switch (timeOfDay) {
      case AM:
        cmbTimeOfDay.setSelectedIndex(0);
        break;
      case PM:
        cmbTimeOfDay.setSelectedIndex(1);
        break;
    }

    if (!is24HourClock()) {
      if (hour > 12) {
        hour -= 12;
      }
    } else {
      if (hour < 12 && timeOfDay.equals(TimeOfDay.PM)) {
        hour += 12;
      }
    }

    fldHour.setText(pad(Integer.toString(hour), 2));
    fldMin.setText(pad(Integer.toString(min), 2));
  }

  public int getHour() {
    return Integer.parseInt(getHourEditor().getText());
  }

  public int getMinute() {
    return Integer.parseInt(getMinuteEditor().getText());
  }

  public TimeOfDay getTimeOfDay() {
    TimeOfDay tod = null;
    switch (cmbTimeOfDay.getSelectedIndex()) {
      case 0:
        tod = TimeOfDay.AM;
        break;
      case 1:
        tod = TimeOfDay.PM;
        break;
    }
    return tod;
  }

  /**
   * Sets if we should be using 24 or 12 hour clock. This basically configures
   * the time of day field and the validation ranges of the various fields
   *
   * @param value
   */
  public void set24HourClock(boolean value) {
    if (value != use24HourClock) {

      use24HourClock = value;
      cmbTimeOfDay.setVisible(!use24HourClock);

      if (cmbTimeOfDay.getSelectedIndex() == 1) {
        setTime(getHour() + 12, getMinute(), getTimeOfDay());
      }

      invalidate();
      firePropertyChange("24HourClock", !use24HourClock, value);
    }
  }

  /**
   * Returns if this is using a 24 or 12 hour clock
   *
   * @return
   */
  public boolean is24HourClock() {
    return use24HourClock;
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    cmbTimeOfDay = new javax.swing.JComboBox();
    pnlFields = new javax.swing.JPanel();
    lblSeperator = new javax.swing.JLabel();
    fldHour = new javax.swing.JTextField();
    fldMin = new javax.swing.JTextField();

    addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        doFocusGained(evt);
      }
    });
    setLayout(new java.awt.GridBagLayout());

    cmbTimeOfDay.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"am", "pm"}));
    cmbTimeOfDay.setBorder(null);
    cmbTimeOfDay.setEditor(null);
    cmbTimeOfDay.setOpaque(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
    add(cmbTimeOfDay, gridBagConstraints);

    pnlFields.setBackground(new java.awt.Color(255, 255, 255));
    pnlFields.setLayout(new java.awt.GridBagLayout());

    lblSeperator.setText(":");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
    pnlFields.add(lblSeperator, gridBagConstraints);

    fldHour.setBorder(null);
    fldHour.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    pnlFields.add(fldHour, gridBagConstraints);

    fldMin.setBorder(null);
    fldMin.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    pnlFields.add(fldMin, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    add(pnlFields, gridBagConstraints);
  }// </editor-fold>                        

  private void doFocusGained(java.awt.event.FocusEvent evt) {
    fldHour.requestFocus();
  }
  // Variables declaration - do not modify                     
  private javax.swing.JComboBox cmbTimeOfDay;
  private javax.swing.JTextField fldHour;
  private javax.swing.JTextField fldMin;
  private javax.swing.JLabel lblSeperator;
  private javax.swing.JPanel pnlFields;
  // End of variables declaration                   

  /**
   * Moves the focus forward to the next field.
   *
   * This is used to provide "automatic" focus movement
   */
  protected void moveFocusForward() {
    if (fldHour.hasFocus()) {
      fldMin.requestFocus();
    } else if (fldMin.hasFocus()) {
      cmbTimeOfDay.requestFocus();
    }
  }

  /**
   * Moves the focus backwards to the previous field.
   *
   * This is used to provide "automatic" focus movement
   */
  protected void moveFocusBackward() {
    if (fldMin.hasFocus()) {
      fldHour.requestFocus();
    } else if (cmbTimeOfDay.hasFocus()) {
      fldMin.requestFocus();
    }
  }

  /**
   * Fires the action performed event to all registered listeners
   *
   * @param evt
   */
  protected void fireActionPerformed(ActionEvent evt) {
    List<ActionListener> lstListeners = Arrays.asList(listenerList.getListeners(ActionListener.class));
    if (!lstListeners.isEmpty()) {
      Collections.reverse(lstListeners);
      for (ActionListener listener : lstListeners) {
        listener.actionPerformed(evt);
      }
    }
  }

  /**
   * Hour key handler, used to monitor "special" keys for the hour field.
   *
   * This looks for the user pressing the ":" key and the right arrow key in
   * order to perform special navigation
   */
  protected class HourKeyHandler extends KeyAdapter {

    @Override
    public void keyPressed(KeyEvent e) {
      boolean numLock = false;
      try {
        // Get the state of the nums lock
        numLock = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
      } catch (Exception exp) {
      }

      // Move focus forward if the user presses the ":"
      if (e.getKeyCode() == KeyEvent.VK_SEMICOLON && e.isShiftDown()) {
        moveFocusForward();
        // Move focus forward if the user pressed the left arrow key
      } else if ((e.getKeyCode() == KeyEvent.VK_NUMPAD6 && !numLock) || e.getKeyCode() == KeyEvent.VK_RIGHT) {
        // If we are in the last edit position
        if (fldHour.getCaretPosition() >= 2) {
          moveFocusForward();
          // Or we are in the first edit position and the field only contains a single character
        } else if (fldHour.getText().trim().length() == 1 && fldHour.getCaretPosition() == 1) {
          moveFocusForward();
        }
      }
    }
  }

  /**
   * Minute key handler, used to monitor "special" keys for the hour field.
   *
   * This looks for the user pressing the left arrow key in order to perform
   * special navigation
   */
  protected class MinuteKeyHandler extends KeyAdapter {

    @Override
    public void keyPressed(KeyEvent e) {
      boolean numLock = false;
      try {
        numLock = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
      } catch (Exception exp) {
      }

      if ((e.getKeyCode() == KeyEvent.VK_NUMPAD4 && !numLock) || e.getKeyCode() == KeyEvent.VK_LEFT) {
        // Only want to move backwards if we are at the first edit position
        if (fldMin.getCaretPosition() == 0) {
          moveFocusBackward();
        }
      }
    }
  }

  /**
   * Hour field focus handler. This watches for focus lost events a
   * automatically pads the field with a leading "0" if the field is only 1
   * character in length
   */
  protected class HourFocusHandler extends FocusAdapter {

    @Override
    public void focusLost(FocusEvent e) {
      String text = fldHour.getText();
      if (text.length() < 2) {
        text = pad(text, 2);
        fldHour.setText(text);
      }
    }
  }

  /**
   * Minute field focus handler, watches for focus lost events and automatically
   * adds a "0" to the end of the field if it is only 1 character in length
   */
  protected class MinuteFocusHandler extends FocusAdapter {

    @Override
    public void focusLost(FocusEvent e) {
      String text = fldMin.getText();
      if (text.length() < 2) {
        fldMin.setText(text + "0");
      }
    }
  }

  /**
   * The document filter used to filter the hour field.
   */
  protected class HourDocumentFilter extends DocumentFilter {

    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
      System.out.println("insert: offset = " + offset + "; text = " + text);
      super.insertString(fb, offset, text, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {

      try {
        boolean isAcceptable = false;
        boolean passOnFocus = false;

        int strLength = text.length();
        // We convert the value here to make sure it's a number...
        int value = Integer.parseInt(text);

        // If the length of the string been replaced is only 1 character
        if (strLength == 1) {
          // If we are at the start of the editing position
          if (offset == 0) {
            // What clock type are we using...
            if (!is24HourClock()) {
              // only accept 0 or 1...
              if (value <= 1) {
                isAcceptable = true;
              }
            } else if (value <= 2) {
              isAcceptable = true;
            }
            // If we are at the second editing position
          } else if (offset == 1) {
            // Get the preceeding value, should be 0, 1 or 2
            String upperPart = fb.getDocument().getText(0, 1);
            // Convert the value to an int
            int upperValue = Integer.parseInt(upperPart);

            // The acceptable range of values for the given position
            int lowerRange = 0;
            int upperRange = 9;

            // Which clock are we using
            if (is24HourClock()) {
              // If the first value is 2, we can only accept values from 0-3 (20-23)
              if (upperValue == 2) {
                upperRange = 3;
              }
            } else {
              // 12 hour clock
              // If the first value is 1, we can only accept values from 0-2 (10-12)
              if (upperValue == 1) {
                upperRange = 2;
              }
            }

            // Is the value within accpetable range...
            if (value >= lowerRange && value <= upperRange) {
              isAcceptable = true;
            }

            // Pass on focus (only if the value is accepted)
            passOnFocus = true;
          }
        } else {
          // First, we need to trim the value down to a maximum of 2 characters

          // Need to know at what offest...
          // 2 - offset..
          // offset == 0, length = 2 - offset = 2
          // offset == 1, length = 2 - offset = 1
          strLength = 2 - offset;
          String timeText = text.substring(offset, strLength);
          value = Integer.parseInt(timeText);
          // this will only work if we are using a 24 hour clock
          if (value >= 0 && value <= 23) {
            while (value > 12 && is24HourClock()) {
              value -= 12;
            }

            // Pad out the text if required
            text = pad(value, 2);
            isAcceptable = true;
          }
        }

        if (isAcceptable) {
          super.replace(fb, offset, length, text, attrs);
          if (passOnFocus) {
            moveFocusForward();
          }
        }
      } catch (NumberFormatException exp) {
      }
    }
  }

  /**
   * The document filter used to filter the minute field.
   */
  protected class MinuteDocumentFilter extends DocumentFilter {

    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
      System.out.println("insert: offset = " + offset + "; text = " + text);
      super.insertString(fb, offset, text, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {

      try {
        boolean isAcceptable = false;
        boolean passOnFocus = false;

        // How long is the text been added
        int strLength = text.length();
        // Convert the value to an integer now and save us the hassel
        int value = Integer.parseInt(text);

        // If the length is only 1, probably a new character has been added
        if (strLength == 1) {
          // The valid range of values we can accept
          int upperRange = 9;
          int lowerRange = 0;
          if (offset == 0) {
            // If we are at the first edit position, we can only accept values
            // from 0-5 (50 minutes that is)
            upperRange = 5;
          } else if (offset == 1) {
            // Second edit position...
            // Every thing is valid here...
            // We want to pass on focus if the clock is in 12 hour mode
            passOnFocus = !is24HourClock();
          }

          // Is the value acceptable..
          if (value >= lowerRange && value <= upperRange) {
            isAcceptable = true;
          }
        } else {
          // Basically, we are going to trim the value down to at max 2 characters

          // Need to know at what offest...
          // 2 - offset..
          // offset == 0, length = 2 - offset = 2
          // offset == 1, length = 2 - offset = 1
          strLength = 2 - offset;
          String timeText = text.substring(offset, strLength);
          value = Integer.parseInt(timeText);
          if (value >= 0 && value <= 59) {
            // Pad out the value as required
            text = pad(value, 2);
            isAcceptable = true;
          }
        }

        if (isAcceptable) {
          super.replace(fb, offset, length, text, attrs);
          if (passOnFocus) {
            moveFocusForward();
          }
        }

      } catch (NumberFormatException exp) {
      }
    }
  }

  /**
   * This is a simple "pass" on action handler...
   */
  protected class ActionHandler implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      ActionEvent evt = new ActionEvent(TimeField.this, e.getID(), e.getActionCommand(), e.getModifiers());

      fireActionPerformed(evt);
    }
  }

  public static String pad(long lValue, int iMinLength) {
    return pad(Long.toString(lValue), 2);
  }

  public static String pad(int iValue, int iMinLength) {
    return pad(Integer.toString(iValue), iMinLength);
  }

  public static String pad(String sValue, int iMinLength) {
    StringBuilder sb = new StringBuilder(iMinLength);
    sb.append(sValue);
    while (sb.length() < iMinLength) {
      sb.insert(0, "0");
    }
    return sb.toString();
  }
}