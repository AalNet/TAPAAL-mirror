package dk.aau.cs.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import com.sun.org.apache.xpath.internal.operations.And;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetType;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.AnimationController;
import pipe.gui.AnimationHistoryComponent;
import pipe.gui.Animator;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Zoomer;
import pipe.gui.widgets.ConstantsPane;
import pipe.gui.widgets.JSplitPaneFix;
import pipe.gui.widgets.QueryPane;
import dk.aau.cs.gui.components.EnabledTransitionsList;
import dk.aau.cs.gui.components.MultiSplitPane;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.util.Require;

public class TabContent extends JSplitPane {
	private static final long serialVersionUID = -648006317150905097L;

	protected TimedArcPetriNetNetwork tapnNetwork = new TimedArcPetriNetNetwork();
	protected HashMap<TimedArcPetriNet, DataLayer> guiModels = new HashMap<TimedArcPetriNet, DataLayer>();
	protected HashMap<TimedArcPetriNet, Zoomer> zoomLevels = new HashMap<TimedArcPetriNet, Zoomer>();
	protected JScrollPane drawingSurfaceScroller;
	protected DrawingSurfaceImpl drawingSurface;
	protected File appFile;

	// Normal mode
	JPanel editorLeftPane;
	QueryPane queries;
	ConstantsPane constantsPanel;
	TemplateExplorer templateExplorer;
	MultiSplitPane editorSplitPane;

	// / Animation
	protected AnimationHistoryComponent animBox;
	protected AnimationController animControlerBox;	
	protected JScrollPane animationHistoryScrollPane;
	protected JScrollPane animationControllerScrollPane;
	protected AnimationHistoryComponent abstractAnimationPane = null;
	protected JPanel animationControlsPanel;
	protected EnabledTransitionsList enabledTransitionsList;

	protected JPanel animatorLeftPane;
	protected JSplitPane animationHistorySplitter;
	protected SharedPlacesAndTransitionsPanel sharedPTPanel;
	
	protected MultiSplitPane animatorSplitPane;
	
	public TabContent() { 
		for (TimedArcPetriNet net: tapnNetwork.allTemplates()){
			guiModels.put(net, new DataLayer());
			zoomLevels.put(net, new Zoomer());
		}

		drawingSurface = new DrawingSurfaceImpl(new DataLayer(), this);
		drawingSurfaceScroller = new JScrollPane(drawingSurface);
		// make it less bad on XP
		drawingSurfaceScroller.setBorder(new BevelBorder(BevelBorder.LOWERED));
		drawingSurfaceScroller.setWheelScrollingEnabled(true);

		createEditorLeftPane();

		this.setOrientation(HORIZONTAL_SPLIT);
		this.setLeftComponent(editorLeftPane);
		this.setRightComponent(drawingSurfaceScroller);

		this.setContinuousLayout(true);
		this.setOneTouchExpandable(true);
		this.setBorder(null); // avoid multiple borders
		this.setDividerSize(8);	

	}
	
	public void createEditorLeftPane() {
		editorLeftPane = new JPanel(new GridBagLayout());
		editorLeftPane.setPreferredSize(new Dimension(300, 100)); // height is ignored because the component is stretched
		editorLeftPane.setMinimumSize(new Dimension(300, 100));
		boolean enableAddButton = getModel() == null ? true : !getModel().netType().equals(NetType.UNTIMED);
		
		constantsPanel = new ConstantsPane(enableAddButton, this);
		queries = new QueryPane(new ArrayList<TAPNQuery>(), this);
		templateExplorer = new TemplateExplorer(this);
		sharedPTPanel = new SharedPlacesAndTransitionsPanel(this);
		
		editorSplitPane = new MultiSplitPane();
		editorSplitPane.add(templateExplorer, 0);
		editorSplitPane.add(sharedPTPanel, 1);
		editorSplitPane.add(queries, 2);
		editorSplitPane.add(constantsPanel, 3);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		editorLeftPane.add(editorSplitPane, gbc);
	}

	public void updateConstantsList() {
		constantsPanel.showConstants();
	}
	public void updateQueryList() {
		queries.updateQueryButtons();
		queries.repaint();
	}

	public DataLayer getModel() {
		return drawingSurface.getGuiModel();
	}

	public void setDrawingSurface(DrawingSurfaceImpl drawingSurface) {
		this.drawingSurface = drawingSurface;
	}

	public File getFile() {
		return appFile;
	}

	public void setFile(File file) {
		appFile = file;
	}

	/** Creates a new animationHistory text area, and returns a reference to it */
	private void createAnimationHistory() {
		animBox = new AnimationHistoryComponent();
		animBox.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)){
					int selected = animBox.getSelectedIndex();
					int clicked = animBox.locationToIndex(e.getPoint());
					if(clicked != -1){
						int steps = clicked - selected;
						Animator anim = CreateGui.getAnimator();
						if(steps < 0){
							for(int i = 0; i < Math.abs(steps); i++){
								animBox.stepBackwards();
								anim.stepBack();
								CreateGui.getAnimationController().setAnimationButtonsEnabled();
							}
						}else{
							for(int i = 0; i < Math.abs(steps); i++){
								animBox.stepForward();
								anim.stepForward();
								CreateGui.getAnimationController().setAnimationButtonsEnabled();
							}
						}
					}
				}
			}
		});
		animationHistoryScrollPane = new JScrollPane(animBox);
		animationHistoryScrollPane.setBorder(BorderFactory
				.createCompoundBorder(BorderFactory
						.createTitledBorder("Simulation History"),
						BorderFactory.createEmptyBorder(3, 3, 3, 3)));
	}
	
	public void switchToAnimationComponents() {
		
		//saveSplitPaneDividerlocationsEditor();
		
		if(animBox == null) createAnimationHistory();
		if(animControlerBox == null) createAnimationController();
		if(enabledTransitionsList == null) createEnabledTransitionsList();
		
		animatorLeftPane = new JPanel(new GridBagLayout());
		animatorLeftPane.setPreferredSize(animControlerBox.getPreferredSize()); // height is ignored because the component is stretched
		animatorLeftPane.setMinimumSize(animControlerBox.getMinimumSize());
		templateExplorer.switchToAnimationMode();
		
		animatorSplitPane = new MultiSplitPane();
		animatorSplitPane.add(templateExplorer, 0);
		animatorSplitPane.add(enabledTransitionsList, 1);
		
		animationControlsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		animationControlsPanel.add(animControlerBox, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		animationControlsPanel.add(animationHistoryScrollPane, gbc);
		
		animatorSplitPane.add(animationControlsPanel, 2);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		animatorLeftPane.add(animatorSplitPane, gbc);
		this.setLeftComponent(animatorLeftPane);
		
	}
	
	public void switchToEditorComponents() {
		templateExplorer.switchToEditorMode();
		
		//TODO
		/*
		//It's necessary to use this method as otherwise the dividerlocation is first updated on resizing the window
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				editorTopSplitPane.setLeftComponent(templateExplorer);
			}
		});
		*/
		this.setLeftComponent(editorLeftPane);
		
		//setSplitPaneDividerLocationEditor();
		
		drawingSurface.repaintAll();
	}

	public AnimationHistoryComponent getUntimedAnimationHistory() {
		return abstractAnimationPane;
	}

	public AnimationController getAnimationController() {
		return animControlerBox;
	}

	public void addAbstractAnimationPane() {
		animationControlsPanel.remove(animationHistoryScrollPane);
		abstractAnimationPane = new AnimationHistoryComponent();

		JScrollPane untimedAnimationHistoryScrollPane = new JScrollPane(abstractAnimationPane);
		untimedAnimationHistoryScrollPane.setBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createTitledBorder("Untimed Trace"),
						BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		animationHistorySplitter = new JSplitPaneFix(JSplitPane.HORIZONTAL_SPLIT, animationHistoryScrollPane, untimedAnimationHistoryScrollPane);

		animationHistorySplitter.setContinuousLayout(true);
		animationHistorySplitter.setOneTouchExpandable(true);
		animationHistorySplitter.setBorder(null); // avoid multiple borders
		animationHistorySplitter.setDividerSize(8);
		animationHistorySplitter.setDividerLocation(0.5);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		animationControlsPanel.add(animationHistorySplitter, gbc);
	}

	public void removeAbstractAnimationPane() {
		animationControlsPanel.remove(animationHistorySplitter);
		abstractAnimationPane = null;
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		animationControlsPanel.add(animationHistoryScrollPane, gbc);
		this.repaint();
		
	}

	private void createAnimationController() {
		animControlerBox = new AnimationController();

		animationControllerScrollPane = new JScrollPane(animControlerBox);
		animationControllerScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		animControlerBox.requestFocus(true);
	}

	public AnimationHistoryComponent getAnimationHistory() {
		return animBox;
	}
	
	private void createEnabledTransitionsList(){
		enabledTransitionsList = new EnabledTransitionsList();
		
		enabledTransitionsList.setBorder(BorderFactory
		.createCompoundBorder(BorderFactory
				.createTitledBorder("Enabled Transitions"),
				BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		enabledTransitionsList.setToolTipText("List of currently enabled transitions (double click a transition to fire it)");
	}
	
	public EnabledTransitionsList getFireabletransitionsList(){
		return enabledTransitionsList;
	}

	public JScrollPane drawingSurfaceScrollPane() {
		return drawingSurfaceScroller;
	}

	public TimedArcPetriNetNetwork network() {
		return tapnNetwork;
	}

	public DrawingSurfaceImpl drawingSurface() {
		return drawingSurface;
	}

	public Iterable<Template> allTemplates() {
		ArrayList<Template> list = new ArrayList<Template>();
		for (TimedArcPetriNet net : tapnNetwork.allTemplates()) {
			list.add(new Template(net, guiModels.get(net), zoomLevels.get(net)));
		}
		return list;
	}
	
	public Iterable<Template> activeTemplates() {
		ArrayList<Template> list = new ArrayList<Template>();
		for (TimedArcPetriNet net : tapnNetwork.activeTemplates()) {
			list.add(new Template(net, guiModels.get(net), zoomLevels.get(net)));
		}
		return list;
	}
	
	public int numberOfActiveTemplates() {
		int count = 0;
		for(TimedArcPetriNet net : tapnNetwork.activeTemplates()) {
			if(net.isActive())
				count++;
		}
		return count;
	}

	public void addTemplate(Template template) {
		tapnNetwork.add(template.model());
		guiModels.put(template.model(), template.guiModel());
		zoomLevels.put(template.model(), template.zoomer());
		templateExplorer.updateTemplateList();
	}
	
	public void addGuiModel(TimedArcPetriNet net, DataLayer guiModel){
		guiModels.put(net, guiModel);
	}
	
	public void removeTemplate(Template template) {
		tapnNetwork.remove(template.model());
		guiModels.remove(template.model());
		zoomLevels.remove(template.model());
		templateExplorer.updateTemplateList();
	}

	public Template currentTemplate() {
		return templateExplorer.selectedModel();
	}

	public void setCurrentTemplate(Template template) {
		drawingSurface.setModel(template.guiModel(), template.model(), template.zoomer());
	}

	public Iterable<TAPNQuery> queries() {
		return queries.getQueries();
	}

	public void setQueries(Iterable<TAPNQuery> queries) {
		this.queries.setQueries(queries);

	}

	public void removeQuery(TAPNQuery queryToRemove) {
		queries.removeQuery(queryToRemove);
	}
	
	public void addQuery(TAPNQuery query){
		queries.addQuery(query);
	}

	public void setConstants(Iterable<Constant> constants) {
		tapnNetwork.setConstants(constants);
		//constantsPanel.showConstants();
	}

	public void setupNameGeneratorsFromTemplates(Iterable<Template> templates) {
		drawingSurface.setupNameGeneratorsFromTemplates(templates);
	}

	public void setNetwork(TimedArcPetriNetNetwork network, Collection<Template> templates) {
		Require.that(network != null, "network cannot be null");
		tapnNetwork = network;
		
		guiModels.clear();
		for(Template template : templates){
			addGuiModel(template.model(), template.guiModel());
			zoomLevels.put(template.model(), template.zoomer());
		}

		sharedPTPanel.setNetwork(network);
		templateExplorer.updateTemplateList();
		
		constantsPanel.setNetwork(tapnNetwork);
	}

	public void swapTemplates(int currentIndex, int newIndex) {
		tapnNetwork.swapTemplates(currentIndex, newIndex);
	}
	
	public TimedArcPetriNet[] sortTemplates() {
		return tapnNetwork.sortTemplates();
	}
	
	public void undoSort(TimedArcPetriNet[] l){
		tapnNetwork.undoSort(l);
	}

	public void swapConstants(int currentIndex, int newIndex) {
		tapnNetwork.swapConstants(currentIndex, newIndex);
		
	}
	
	public Constant[] sortConstants(){
		return tapnNetwork.sortConstants();
	}
	
	public void undoSort(Constant[] oldOrder) {
		tapnNetwork.undoSort(oldOrder);
	}
	
	public void showComponents(boolean enable){
		if(enable){
			editorSplitPane.add(templateExplorer, 0);
			editorSplitPane.add(sharedPTPanel, 1);
			
			if(animatorSplitPane != null){
				animatorSplitPane.add(templateExplorer, 0);
			}
		} else {
			editorSplitPane.remove(0);
			editorSplitPane.remove(1);
			if(animatorSplitPane != null){
				animatorSplitPane.remove(0);
			}
		}
		//TODO
		/*
		editorTopSplitPane.setVisible(enable);
		editorTopSplitPane.setDividerLocation(dividerlocationEditorTopSplitPane);
		editorOuterSplitPane.setDividerLocation(dividerlocationEditorOuterSplitPane);
		if(animatorTopSplitPane!=null){
			animatorTopSplitPane.setDividerLocation(dividerlocationEditorTopSplitPane);
			updateTopSplitPanel();
		}
		*/
	}
	public void showQueries(boolean enable){
		if(enable){
			editorSplitPane.add(queries, 2);
		} else {
			editorSplitPane.remove(2);
		}
	}
	public void showConstantsPanel(boolean enable){
		if(enable){
			editorSplitPane.add(constantsPanel, 3);
		} else {
			editorSplitPane.remove(3);
		}
	}
	public void showEnabledTransitionsList(boolean enable){
		if(enable){
			animatorSplitPane.add(enabledTransitionsList, 1);
		} else {
			animatorSplitPane.remove(1);
		}
	}
	
	public void selectFirstElements(){
		templateExplorer.selectFirst();
		queries.selectFirst();
		constantsPanel.selectFirst();
		
	}	
}
