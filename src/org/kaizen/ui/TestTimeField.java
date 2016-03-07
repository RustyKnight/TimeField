
package org.kaizen.ui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author shane
 */
public class TestTimeField {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		new TestTimeField();
	}
	
	public TestTimeField() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
					ex.printStackTrace();
				}
				
				JPanel content = new JPanel();
				content.setBorder(new EmptyBorder(10, 10, 10, 10));
				
				JFrame frame = new JFrame("Testing");
				frame.setContentPane(content);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLayout(new GridBagLayout());
				
				
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.insets = new Insets(2, 2, 2, 2);
				frame.add(new JLabel("Time: "), gbc);
				gbc.gridx++;
				frame.add(new TimeField(), gbc);
				gbc.gridx = 0;
				gbc.gridy++;
				frame.add(new JLabel("Duration: "), gbc);
				gbc.gridx++;
				frame.add(new DurationField(), gbc);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}
	
}
