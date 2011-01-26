package pipe.gui.action;

import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import pipe.dataLayer.Arc;
import pipe.dataLayer.TransportArc;
import pipe.gui.CreateGui;

public class EditGroupAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6000849375126934761L;
	private Arc myArc;
	
	public EditGroupAction(Container contentPane, Arc a) {
		myArc = a;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		String currentInterval = ""+((TransportArc)myArc).getGroupNr();
		String input = JOptionPane.showInputDialog(
				"Group:", currentInterval);

		if ( input == null ) {
			return;		// do nothing if the user clicks "Cancel"
		}

		CreateGui.getView().getUndoManager().addNewEdit( ( (TransportArc)myArc ).setGroupNr( Integer.parseInt(input) ) );

	}
}