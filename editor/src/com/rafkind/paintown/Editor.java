package com.rafkind.paintown;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;

import java.util.List;

import com.rafkind.paintown.exception.LoadException;

import com.rafkind.paintown.level.Level;
import com.rafkind.paintown.level.Block;
import com.rafkind.paintown.level.Thing;
import javax.swing.filechooser.FileFilter;

import org.swixml.SwingEngine;

public class Editor extends JFrame {

	private Image levelImage;

	public Editor(){
		this.setSize( 900, 500 );

		JMenuBar menuBar = new JMenuBar();
		JMenu menuProgram = new JMenu( "Program" );
		menuBar.add( menuProgram );
		JMenu menuLevel = new JMenu( "Level" );
		menuBar.add( menuLevel );
		JMenuItem quit = new JMenuItem( "Quit" );
		menuProgram.add( quit );
		final Lambda0 closeHook = new Lambda0(){
			public Object invoke(){
				System.exit( 0 );
				return null;
			}
		};
		JMenuItem loadLevel = new JMenuItem( "Open Level" );
		menuLevel.add( loadLevel );
		JMenuItem saveLevel = new JMenuItem( "Save Level" );
		menuLevel.add( saveLevel );
		menuLevel.setMnemonic( KeyEvent.VK_L );
		saveLevel.setMnemonic( KeyEvent.VK_S );
		loadLevel.setMnemonic( KeyEvent.VK_O );

		/*
		levelImage = new BufferedImage( 1000, 300, BufferedImage.TYPE_INT_RGB );
		Graphics g = levelImage.getGraphics();
		g.setColor( new Color( 64, 192, 54 ) );
		g.fillRect( 10, 10, 200, 200 );
		*/

		quit.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent event ){
				closeHook.invoke_();
			}
		});

		final SwingEngine engine = new SwingEngine( "main.xml" );
		this.getContentPane().add( (JPanel) engine.getRootComponent() );

		final Level level = new Level();
		
		final JPanel viewContainer = (JPanel) engine.find( "view" );
		final JScrollPane viewScroll = new JScrollPane( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
		final JPanel view = new JPanel(){

			public Dimension getPreferredSize(){
				return level.getSize();
			}

			protected void paintComponent( Graphics g ){
					// System.out.println( "Clear a rect from 0, " + level.getHeight() + " to " + level.getWidth() + ", " + (v.getVisibleAmount() - level.getHeight()) );
				JScrollBar h = viewScroll.getHorizontalScrollBar();
				JScrollBar v = viewScroll.getVerticalScrollBar();
				g.setColor( new Color( 255, 255, 255 ) );
				g.clearRect( 0, 0, (int) level.getWidth(), v.getVisibleAmount() );
				level.render( (Graphics2D) g, h.getValue(), 0, h.getVisibleAmount(), v.getVisibleAmount() );
				// System.out.println( "Visible vertical: " + v.getVisibleAmount() );
			}
		};
		viewScroll.setPreferredSize( new Dimension( 200, 200 ) );
		viewScroll.setViewportView( view );
		viewScroll.getHorizontalScrollBar().setBackground( new Color( 128, 255, 0 ) );

		class Mouser extends MouseMotionAdapter implements MouseInputListener {
			Thing selected = null;
			double dx, dy;
			double sx, sy;
			Popup currentPopup;

			public void mouseDragged( MouseEvent event ){
				
				if ( selected != null ){
					// System.out.println( "sx,sy: " + sx + ", " + sy + " ex,ey: " + (event.getX() / 2) + ", " + (event.getY() / 2) + " dx, dy: " + dx + ", " + dy );
					level.moveThing( selected, (int)(sx + event.getX() / level.getScale() - dx), (int)(sy + event.getY() / level.getScale() - dy) );
					view.repaint();
				}
			}

			private boolean leftClick( MouseEvent event ){
				return event.getButton() == MouseEvent.BUTTON1;
			}
			
			private boolean rightClick( MouseEvent event ){
				return event.getButton() == MouseEvent.BUTTON3;
			}

			private void selectThing( MouseEvent event ){
				Thing t = findThingAt( event );
				Block has = null;
				for ( Iterator it = level.getBlocks().iterator(); it.hasNext(); ){
					Block b = (Block) it.next();
					b.setHighlight( false );
					if ( t != null && b.hasThing( t ) ){
						has = b;
					 }
				}

				if ( has != null ){
					has.setHighlight( true );
					view.repaint();
				}

				if ( selected == null && t != null ){
					// selected = findThingAt( event );
					selected = t;
					if ( selected != null ){
						sx = selected.getX();
						sy = selected.getY() + level.getMinZ();
						// System.out.println( "Y: " + selected.getY() + " minZ: " + level.getMinZ() );
						dx = event.getX() / level.getScale();
						dy = event.getY() / level.getScale();
						// System.out.println( "Found: " + selected + " at " + event.getX() + " " + event.getY() );
					}
				}
				if ( selected != null && event.getClickCount() == 2 ){
					System.out.println( "Properties of " + selected );
				}
			}
			
			public void mousePressed( MouseEvent event ){
				if ( leftClick( event ) ){
					selectThing( event );
				} else if ( rightClick( event ) ){
					JButton button = new JButton( "Hello" );
					if ( currentPopup != null ){
						currentPopup.hide();
					}
					final Popup p = PopupFactory.getSharedInstance().getPopup( Editor.this, button, event.getX() + viewScroll.getX(), event.getY() + viewScroll.getY() );
					button.addActionListener( new AbstractAction(){
						public void actionPerformed( ActionEvent event ){
							p.hide();
						}
					});
					currentPopup = p;
					p.show();
				}
			}
			
			public void mouseExited( MouseEvent event ){
				if ( selected != null ){
					selected = null;
					view.repaint();
				}
			}

			private Thing findThingAt( MouseEvent event ){
				return level.findThing( (int)(event.getX() / level.getScale()), (int)(event.getY() / level.getScale()) );
			}

			public void mouseClicked( MouseEvent event ){
			}
			
			public void mouseEntered( MouseEvent event ){
			}
			
			public void mouseReleased( MouseEvent event ){
				if ( selected != null ){
					selected = null;
					view.repaint();
				}
			}
		}

		Mouser m = new Mouser();

		view.addMouseMotionListener( m );
		view.addMouseListener( m );

		/*
		view.addMouseMotionListener( new MouseMotionAdapter(){
			Thing selected = null;
			public void mouseDragged( MouseEvent event ){
				if ( selected == null ){
					selected = level.findThing( event.getX(), event.getY() );
				}
				if ( selected != null ){
					level.moveThing( selected, event.getX(), event.getY() );
				}
				// view.repaint();
				// System.out.println( "Mouse dragged at " + event.getX() + " " + event.getY() );
			}
		});

		view.addMouseListener( new MouseInputAdapter(){
			public void mousePressed( MouseEvent event ){
				// System.out.println( "Mouse pushed at " + event.getX() + " " + event.getY() );
			}

			public void mouseClicked( MouseEvent event ){
				// System.out.println( "Mouse clicked at " + event.getX() + " " + event.getY() );
			}

			public void mouseReleased( MouseEvent event ){
			}
		});
		*/

		/*
		class BlockModel implements ListModel {
			private List objects;
			private List enabled;
			public BlockModel(){
				objects = new ArrayList();
				enabled = new ArrayList();
			}
		}
		*/

		JTabbedPane tabbed = (JTabbedPane) engine.find( "tabbed" );
		final Box holder = Box.createVerticalBox();
		final Box blocks = Box.createVerticalBox();
		holder.add( new JScrollPane( blocks ) );

		holder.add( new JSeparator() );

		class ObjectList implements ListModel {
			private List listeners;
			private List things;
			public ObjectList(){
				listeners = new ArrayList();
				things = new ArrayList();
			}

			public void setBlock( Block b ){
				this.things = b.getThings();

				ListDataEvent event = new ListDataEvent( this, ListDataEvent.CONTENTS_CHANGED, 0, 999999 );
				for ( Iterator it = listeners.iterator(); it.hasNext(); ){
						  ListDataListener l = (ListDataListener) it.next();
						  l.contentsChanged( event );
				}
			}

			public void addListDataListener( ListDataListener l ){
				listeners.add( l );
			}

			public Object getElementAt( int index ){
				return this.things.get( index );
			}

			public int getSize(){
				return this.things.size();
			}

			public void removeListDataListener( ListDataListener l ){
				this.listeners.remove( l );
			}
		}

		final ObjectList objectList = new ObjectList();
		final JList currentObjects = new JList( objectList );
		holder.add( new JLabel( "Objects" ) );
		holder.add( new JScrollPane( currentObjects ) );
		holder.add( Box.createVerticalGlue() );

		tabbed.add( "Blocks", holder );

		final JList objects = new JList();
		tabbed.add( "Objects", objects );
		
		/*
		viewScroll.getHorizontalScrollBar().addAdjustmentListener( new AdjustmentListener(){
			public void adjustmentValueChanged( AdjustmentEvent e ){
				viewScroll.repaint();
			}
		});
		viewScroll.getVerticalScrollBar().addAdjustmentListener( new AdjustmentListener(){
			public void adjustmentValueChanged( AdjustmentEvent e ){
				viewScroll.repaint();
			}
		});
		*/

		/*
		JButton render = (JButton) engine.find( "render" );
		render.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent event ){
				view.repaint();
			}
		});
		*/

		GridBagLayout layout = new GridBagLayout();
		viewContainer.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		
		constraints.weightx = 1;
		constraints.weighty = 1;
		layout.setConstraints( viewScroll, constraints );
		view.setBorder( BorderFactory.createLineBorder( new Color( 255, 0, 0 ) ) );
		viewContainer.add( viewScroll );

		/*
		final JList list = (JList) engine.find( "files" );
		final DirectoryModel model = new DirectoryModel( "data" );
		list.setModel( model );
		*/

		saveLevel.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent event ){
				Token t = level.toToken();
				System.out.println( t );
			}
		});

		loadLevel.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent event ){
				JFileChooser chooser = new JFileChooser( new File( "." ) );	
				chooser.setFileFilter( new FileFilter(){
					public boolean accept( File f ){
						return f.isDirectory() || f.getName().endsWith( ".txt" );
					}

					public String getDescription(){
						return "Level files";
					}
				});

				// chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
				int returnVal = chooser.showOpenDialog( Editor.this );
				if ( returnVal == JFileChooser.APPROVE_OPTION ){
					final File f = chooser.getSelectedFile();
					try{
						level.load( f );
						/*
						JLabel label = (JLabel) engine.find( "file" );
						label.setText( "Filename: " + f.getName() );
						*/
						blocks.removeAll();
						int n = 1;
						int total = 0;
						for ( Iterator it = level.getBlocks().iterator(); it.hasNext(); ){
							final Block block = (Block) it.next();
							Box stuff = Box.createHorizontalBox();
							JCheckBox check = new JCheckBox( new AbstractAction(){
								public void actionPerformed( ActionEvent event ){
									JCheckBox c = (JCheckBox) event.getSource();
									block.setEnabled( c.isSelected() );
									view.revalidate();
									viewScroll.repaint();
								}
							});

							check.setSelected( true );
							stuff.add( check );
							JButton button = new JButton( "Block " + n + " : " + block.getLength() );
							button.addActionListener( new AbstractAction(){
								public void actionPerformed( ActionEvent event ){
									objectList.setBlock( block );
								}
							});
							stuff.add( button );
							total += block.getLength();
							stuff.add( Box.createHorizontalGlue() );
							blocks.add( stuff );
							n += 1;
						}
						blocks.add( new JLabel( "Total length " + total ) );

						blocks.repaint();
						view.revalidate();
						viewScroll.repaint();
					} catch ( LoadException le ){
						System.out.println( "Could not load " + f.getName() );
						le.printStackTrace();
					}
				}
			}
		});

		JPanel scroll = (JPanel) engine.find( "scroll" );
		final JScrollBar scrolly = new JScrollBar( JScrollBar.HORIZONTAL, 10, 0, 1, 10 );
		final JLabel scale = (JLabel) engine.find( "scale" );
		scroll.add( scrolly );
		scrolly.addAdjustmentListener( new AdjustmentListener(){
			public void adjustmentValueChanged( AdjustmentEvent e ){
				level.setScale( (double) e.getValue() * 2.0 / scrolly.getMaximum() );
				scale.setText( "Scale: " + level.getScale() );
				view.revalidate();
				viewScroll.repaint();
			}
		});

		this.setJMenuBar( menuBar );
		this.addWindowListener( new CloseHook( closeHook ) );
	}

	public static void main( String[] args ){

		final Editor editor = new Editor();
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					editor.setVisible( true );
				}
			}
		);
	}
}
