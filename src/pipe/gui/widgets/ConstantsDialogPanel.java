package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.DocumentFilter;
import javax.swing.text.InternationalFormatter;
import javax.swing.text.NumberFormatter;
import javax.swing.text.DocumentFilter.FilterBypass;

import pipe.gui.CreateGui;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

/*
 * LeftConstantsPane.java
 *
 * Created on 08-10-2009, 13:51:42
 */

/**
 * 
 * @author Morten Jacobsen
 */
public class ConstantsDialogPanel extends javax.swing.JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6734583459331431789L;
	private JRootPane rootPane;
	private TimedArcPetriNetNetwork model;
	private int lowerBound;
	private int upperBound;

	JPanel valueTextFieldPane;
	JTextField valueTextField;
	JPanel nameTextFieldPane;
	JTextField nameTextField;
	Dimension size;
	JLabel nameLabel;  
	JLabel valueLabel; 	
	JPanel container;	

	private String oldName;

	public ConstantsDialogPanel() {
		initComponents();		
	}

	public ConstantsDialogPanel(JRootPane pane, TimedArcPetriNetNetwork model) {
		initComponents();
		rootPane = pane;
		this.model = model;		
		
		oldName = "";
		nameTextField.setText(oldName);		
	}

	public ConstantsDialogPanel(JRootPane pane, TimedArcPetriNetNetwork model,
			Constant constant) {
		rootPane = pane;
		this.model = model;
				
		oldName = constant.name();
		lowerBound = constant.lowerBound();
		upperBound = constant.upperBound();		 
				
		initComponents();
		nameTextField.setText(oldName);
		valueTextField.setText(((Integer)constant.value()).toString());	
	}
	
	private boolean stringIsNumber(String text) {
		if (Pattern.matches("^([1-9]([0-9])*)?|0$",text) && 
			!valueTextField.getText().trim().equals(""))
			return true;
		return false;
	}

	public void showDialog() {
		Integer constantWasConfirmed = new Integer(2); //cancel = 2, ok = 0, close window = -1		
		constantWasConfirmed = JOptionPane.showConfirmDialog(
				null, container, "Edit Constant",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (constantWasConfirmed == 2 )
			return;
		else if (constantWasConfirmed == -1 )
			return;
		else if (constantWasConfirmed == 0)
		{			
			String newName = nameTextField.getText();

			if (!Pattern.matches("[a-zA-Z]([\\_a-zA-Z0-9])*", newName)) {
				System.err
				.println("Acceptable names for constants are defined by the regular expression:\n[a-zA-Z][_a-zA-Z]*");
				JOptionPane
				.showMessageDialog(
						CreateGui.getApp(),
						"Acceptable names for constants are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
						"Error", JOptionPane.ERROR_MESSAGE);
				showDialog();
				return;
			}

			if (newName.trim().isEmpty()) {
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"You must specify a name.", "Missing name",
						JOptionPane.ERROR_MESSAGE);
				showDialog();
				return;				
			} else {												
				if (!stringIsNumber(valueTextField.getText().trim())) {
					System.err
					.println("Acceptable values are defined by the regular expression:\n^([1-9]([0-9])*)?|0$");
					JOptionPane
					.showMessageDialog(
							CreateGui.getApp(),
							"Acceptable values are defined by the regular expression:\n([1-9]([0-9])*)?|0$",
							"Error", JOptionPane.ERROR_MESSAGE);
					showDialog();
					return;
				}
				
				int val = Integer.parseInt(valueTextField.getText().trim());
				
				if (!oldName.equals("")) {
					if (!oldName.equals(newName)
							&& model.isConstantNameUsed(newName)) {
						JOptionPane
						.showMessageDialog(
								CreateGui.getApp(),
								"There is already another constant with the same name.\n\n"
								+ "Choose a different name for the constant.",
								"Error", JOptionPane.ERROR_MESSAGE);
						showDialog();
						return;
					}
					//Kyrke - This is messy, but a quck fix for bug #815487			
					//Check that the value is within the allowed bounds
					if (!( lowerBound <= val && val <= upperBound )){
						JOptionPane.showMessageDialog(
								CreateGui.getApp(),
								"The specified value is invalid for the current net.\n"
								+ "Updating the constant to the specified value invalidates the guard\n"
								+ "on one or more arcs.",
								"Constant value invalid for current net",
								JOptionPane.ERROR_MESSAGE);
						showDialog();
						return;
					}
					Command edit = model.updateConstant(oldName, new Constant(
							newName, val));
					if (edit == null) {
						JOptionPane
						.showMessageDialog(
								CreateGui.getApp(),
								"The specified value is invalid for the current net.\n"
								+ "Updating the constant to the specified value invalidates the guard\n"
								+ "on one or more arcs.",
								"Constant value invalid for current net",
								JOptionPane.ERROR_MESSAGE);
						showDialog();
						return;
					} else {
						CreateGui.getCurrentTab().drawingSurface().getUndoManager()
						.addNewEdit(edit);
						CreateGui.getCurrentTab().drawingSurface().repaintAll();
					}
				} else {
					Command edit = model.addConstant(newName, val);
					if (edit == null) {
						JOptionPane
						.showMessageDialog(
								CreateGui.getApp(),
								"A constant with the specified name already exists.",
								"Constant exists",
								JOptionPane.ERROR_MESSAGE);
						showDialog();
						return;
					} else
						CreateGui.getView().getUndoManager().addNewEdit(edit);
				}
				model.buildConstraints();
			}
		}
	}
	
	private void initComponents() {		
		container = new JPanel();
		container.setLayout(new GridBagLayout());
		size = new Dimension(250, 25);
		
		valueTextField = new javax.swing.JTextField();	
		valueTextField.setPreferredSize(size);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 0, 2, 0);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		container.add(valueTextField,gbc);

		nameTextField = new javax.swing.JTextField();	
		nameTextField.setPreferredSize(size);
		nameTextField.addAncestorListener(new RequestFocusListener());

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		container.add(nameTextField,gbc);

		nameLabel = new JLabel(); 
		nameLabel.setText("Name: ");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		container.add(nameLabel,gbc);

		valueLabel = new javax.swing.JLabel(); 
		valueLabel.setText("Value: ");
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 0, 2, 0);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		container.add(valueLabel,gbc);
	}
}

