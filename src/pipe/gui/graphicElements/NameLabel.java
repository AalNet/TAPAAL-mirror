package pipe.gui.graphicElements;

import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import pipe.gui.Pipe;
import pipe.gui.Translatable;
import pipe.gui.Zoomable;
import pipe.gui.Zoomer;

/**
 * This class is for the labels of PN Objects
 * 
 * @version 1.0
 * @author Camilla Clifford
 */
public class NameLabel extends JTextArea implements Translatable,
		Zoomable {

	private static final long serialVersionUID = 5167510420195429773L;
	private int positionX;
	private int positionY;
	private String name = "";
	private String text = "";

	private Font font = new Font(Pipe.LABEL_FONT, Font.BOLD,
			Pipe.LABEL_DEFAULT_FONT_SIZE);

	public NameLabel(int zoom) {
		this("");
		setFont(getFont().deriveFont(Zoomer.getZoomedValue((float) Pipe.LABEL_DEFAULT_FONT_SIZE, zoom)));
	}

	public NameLabel(String nameInput) {
		super(nameInput);
		name = nameInput;
		setFont(font);
		setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));
		setEditable(false);
		setFocusable(false);
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder());
		setBackground(Pipe.BACKGROUND_COLOR);

		//When redrawing a textarea added to a scroll pane, default behaviour is to move scroll pane to the location
        //of the lable. (https://docs.oracle.com/javase/7/docs/api/javax/swing/text/JTextComponent.html)
        //Behaviour disabled to avoid jumping, see bug https://bugs.launchpad.net/tapaal/+bug/1828782
		DefaultCaret c = new DefaultCaret();
		c.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		setCaret(c);

	}

	public void setPosition(int x, int y) {
		positionX = x;
		positionY = y;
		updatePosition();
	}

	public void updateSize() {
		// To get round Java bug #4352983 I have to expand the size a bit
		setSize((int) (getPreferredSize().width * 1.2),
				(int) (getPreferredSize().height * 1.2));
		updatePosition();
	}

	public void updatePosition() {
		setLocation(positionX - getPreferredSize().width, positionY	- Pipe.NAMELABEL_OFFSET);
	}

	public void translate(int x, int y) {
		setPosition(positionX + x, positionY + y);
	}

	public double getYPosition() {
		return positionY;
	}

	public double getXPosition() {
		return positionX;
	}

	@Override
	public void setName(String nameInput) {
		name = nameInput;
		setText(text);
		updateSize();
	}

	@Override
	public void setText(String s) {
		text = s;
		if (name != null) {
			super.setText(name + s);
		} else {
			super.setText(s);
		}
		updateSize();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getText() {
		return text;
	}

	public void zoomUpdate(int value) {
		setFont(getFont().deriveFont(Zoomer.getZoomedValue((float) Pipe.LABEL_DEFAULT_FONT_SIZE, value)));
		updateSize();
	}
	
	public void displayName(boolean display){
		if(display && name != null && text != null){
			super.setText(name + text);
		} else if(!display && text != null){
			super.setText(text);
		}
	}
	
	public int getLayerOffset() {
		return Pipe.NAMELABEL_LAYER_OFFSET;
	}
}
