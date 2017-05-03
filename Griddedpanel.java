import javax.swing.*;
import java.awt.*;

public class GriddedPanel extends JPanel
{
	private GridBagConstraints constraints;
	public GriddedPanel()
	{
		this( new Insets( 2, 2, 2, 2 ) );
	}
	public GriddedPanel( Insets insets )
	{
		super( new GridBagLayout() );
		constraints = new GridBagConstraints();
		constraints.insets = insets;
	}
	public void addComponent( JComponent component, int row, int col,
							  int width, int height, int anchor, int fill )
	{
	   constraints.gridx = col;
	   constraints.gridy = row;
	   constraints.gridwidth = width;
	   constraints.gridheight = height;
	   constraints.anchor = anchor;
	   switch( fill )
	   {
		   case GridBagConstraints.HORIZONTAL:
			   constraints.weightx = 1.0;
			   constraints.weighty = 0.0;
			   break;
		   case GridBagConstraints.VERTICAL:
			   constraints.weighty = 1.0;
			   constraints.weightx = 0.0;
			   break;
		   case GridBagConstraints.BOTH:
			   constraints.weightx = 1.0;
			   constraints.weighty = 1.0;
			   break;
		   case GridBagConstraints.NONE:
				constraints.weightx = 0.0;
			   	constraints.weighty = 0.0;
			   	break;
		   default:
			   break;
	   }
	   
	   constraints.fill = fill;
	   add( component, constraints );
	}
}
	
