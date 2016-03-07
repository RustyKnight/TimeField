package org.kaizen.ui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Allows for the entry of a duration of time, where the number of hours is
 * unconstrained, but the minutes and seconds still conform to the basic 
 * requirements for their values
 */
public class DurationField extends AbstractTimeField {

	private HourDocumentFilter hourDocumentFilter;

	private JTextField secondField;
	private DocumentFilter secondDocumentFilter;
	private FocusListener secondFocusHandler;

	public DurationField() {
		setDuration(0, 0, 0);
	}
	
	public void setDuration(int hour, int minute, int seconds) {
		setHour(hour);
		setMinute(minute);
		setSeconds(seconds);
	}

	public long getDuration() throws NumberFormatException {

		int hour = getHour();
		int minute = getMinute();
		int second = getSecond();

		return (hour * 60 * 60 * 1000)
						+ (minute * 60 * 1000)
						+ (second * 1000);

	}
	
	@Override
	public void setHour(int hour) {
		setFieldValue(getHourField(), hour, 3);
	}
	
	public void setSeconds(int seconds) {
		setFieldValue(getSecondField(), seconds, 2);
	}

	public int getSecond() throws NumberFormatException {
		return Integer.parseInt(getSecondField().getText());
	}

	@Override
	protected JTextField createHourField() {
		JTextField field = super.createHourField();
		field.setColumns(4);
		return field;
	}
	
	@Override
	protected int getHourFocusForwardLength() {
		return -1;
	}
	
	protected int getSecondFocusForwardLength() {
		return 2;
	}

	protected JTextField createSecondField() {
		JTextField secondField = new JTextField(2);
		((AbstractDocument) secondField.getDocument()).setDocumentFilter(getSecondDocumentFilter());
		secondField.addFocusListener(getSecondFocusHandler());
		secondField.setHorizontalAlignment(JTextField.RIGHT);
		secondField.getDocument().addDocumentListener(new MoveFocusForwardHandler(secondField, getMinuteFocusForwardLength()));
		return secondField;
	}

	@Override
	protected JComponent[] getTimeFields() {
		return new JComponent[]{
			getHourField(),
			new JLabel(":"),
			getMinuteField(),
			new JLabel("."),
			getSecondField()
		};
	}

	@Override
	protected List<JComponent> initFocusTransveralOrder() {
		List<JComponent> order = super.initFocusTransveralOrder();
		order.add(getSecondField());
		return order;
	}

	@Override
	protected void installKeyBindings() {
		super.installKeyBindings();

		JTextField minuteField = getMinuteField();
		installKeyBinding(
						minuteField,
						JComponent.WHEN_FOCUSED,
						KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0),
						"toSecondField",
						new MoveFocusForward());

		installKeyBinding(
						minuteField,
						JComponent.WHEN_FOCUSED,
						KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
						"afterMinuteArrow",
						new MoveFieldFocusForward(minuteField, true));
		installKeyBinding(
						minuteField,
						JComponent.WHEN_FOCUSED,
						KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0),
						"afterMinuteKeyPad",
						new MoveFieldFocusForward(minuteField, false));

		JTextField secondField = getMinuteField();
		installKeyBinding(
						secondField,
						JComponent.WHEN_FOCUSED,
						KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
						"beforeSecond",
						new MoveFieldFocusBackward(minuteField, true));
		installKeyBinding(
						secondField,
						JComponent.WHEN_FOCUSED,
						KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0),
						"beforeSecond",
						new MoveFieldFocusBackward(secondField, false));
	}

	public JTextField getSecondField() {
		if (secondField == null) {
			secondField = createSecondField();
		}
		return secondField;
	}

	@Override
	protected DocumentFilter getHourDocumentFilter() {
		if (hourDocumentFilter == null) {
			hourDocumentFilter = new HourDocumentFilter();
		}
		return hourDocumentFilter;
	}

	protected DocumentFilter getSecondDocumentFilter() {
		if (secondDocumentFilter == null) {
			secondDocumentFilter = new SecondDocumentFilter();
		}
		return secondDocumentFilter;
	}

	/**
	 * Returns the focus listener used to monitor the hour field
	 *
	 * @return
	 */
	protected FocusListener getSecondFocusHandler() {
		if (secondFocusHandler == null) {
			secondFocusHandler = new SecondFocusHandler();
		}
		return secondFocusHandler;
	}

	protected void secondFieldLostFocus(FocusEvent evt) {
		String text = getSecondField().getText();
		if (text.length() < 2) {
			text = pad(text, 2);
			getSecondField().setText(text);
		}
	}

	/**
	 * Hour field focus handler. This watches for focus lost events a automatically pads the field with a leading "0" if the field is only 1 character in length
	 */
	protected class SecondFocusHandler extends AbstractFocusHandler {

		@Override
		public void focusLost(FocusEvent e) {
			secondFieldLostFocus(e);
		}
	}

	protected class HourDocumentFilter extends DocumentFilter {

		@Override
		public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
			System.out.println("insert: offset = " + offset + "; text = " + text);
			super.insertString(fb, offset, text, attr);
		}

		@Override
		public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {

			try {
				// We convert the value here to make sure it's a number...
				int value = Integer.parseInt(text);
				super.replace(fb, offset, length, text, attrs);
			} catch (NumberFormatException exp) {
			}
		}
	}

	protected class SecondDocumentFilter extends DocumentFilter {

		@Override
		public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
			super.insertString(fb, offset, text, attr);
		}

		@Override
		public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {

			try {
				boolean isAcceptable = false;

				// How long is the text been added
				int strLength = text.length();
				// Convert the value to an integer now and save us the hassel
				int value = Integer.parseInt(text);

				// If the length is only 1, probably a new character has been added
				if (strLength == 1) {
					// The valid range of values we can accept
					int upperRange = 9;
					int lowerRange = 0;

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
				}

			} catch (NumberFormatException exp) {
			}
		}
	}

}
