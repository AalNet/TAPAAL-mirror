package dk.aau.cs.petrinet.colors;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NormalArc;
import pipe.dataLayer.Place;
import pipe.dataLayer.TAPNInhibitorArc;
import pipe.dataLayer.TimedArc;
import pipe.dataLayer.TransportArc;
import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.dataLayer.colors.ColoredToken;
import pipe.dataLayer.colors.ColoredTransportArc;
import pipe.dataLayer.colors.IntOrConstantRange;
import dk.aau.cs.petrinet.PipeTapnToAauTapnTransformer;
import dk.aau.cs.petrinet.TAPNTransition;

public class ColoredPipeTapnToColoredAauTapnTransformer extends
PipeTapnToAauTapnTransformer {

	public ColoredPipeTapnToColoredAauTapnTransformer() {
		super();
	}

	@Override
	protected void reset(DataLayer model, int capacity){
		this.appModel = model;
		this.capacity = capacity;
		this.aAUPetriNet = new ColoredTAPN();
	}
	
	@Override
	protected void transformPlace(Place place) {
		ColoredTimedPlace ctp = (ColoredTimedPlace)place;
		
		ColorSet colorInvariant = transformColorGuard(ctp.getColorInvariant());
		ColoredTimeInvariant timeInvariant = new ColoredTimeInvariant(ctp.getTimeInvariant().toStringWithoutConstants());
		
		ColoredPlace tapnPlace = new ColoredPlace(ctp.getName(), timeInvariant, colorInvariant);

		for(ColoredToken token : ctp.getColoredTokens()){
			dk.aau.cs.petrinet.colors.ColoredToken newToken = 
				new dk.aau.cs.petrinet.colors.ColoredToken(tapnPlace,token.getColor().getValue());
			tapnPlace.addColoredToken(newToken);
		}

		aAUPetriNet.addPlace(tapnPlace);
		PlaceTransitionObjectBookKeeper.put(place, tapnPlace);
	}

	@Override
	protected void transformTransportArc(TransportArc arc) throws Exception {
		if(arc.getSource() instanceof Place){
			ColoredTransportArc cta = (ColoredTransportArc)arc;

			ColoredInterval timeGuard = new ColoredInterval(cta.getTimeGuard().toStringWithoutConstants());
			ColorSet colorGuard = transformColorGuard(cta.getColorGuard());
			Preservation preserves = Preservation.valueOf(cta.getPreservation().toString());
			int outputValue = cta.getOutputValue().getValue();
			
			dk.aau.cs.petrinet.colors.ColoredTransportArc tarc = 
				new dk.aau.cs.petrinet.colors.ColoredTransportArc(
						(ColoredPlace)PlaceTransitionObjectBookKeeper.get(cta.getSource()),
						(TAPNTransition)PlaceTransitionObjectBookKeeper.get(cta.getTarget()),
						(ColoredPlace)PlaceTransitionObjectBookKeeper.get(cta.getConnectedTo().getTarget()),
						timeGuard,
						colorGuard,
						preserves,
						outputValue);
			aAUPetriNet.addArc(tarc);
		}
	}

	private ColorSet transformColorGuard(pipe.dataLayer.colors.ColorSet colorSet) {
		ColorSet colorGuard = new ColorSet();

		for(IntOrConstantRange range : colorSet.getRanges()){
			IntegerRange newRange = new IntegerRange(range.toStringWithoutConstants());
			colorGuard.addRange(newRange);
		}
		return colorGuard;
	}
	
	@Override
	protected void transformInhibitorArc(TAPNInhibitorArc arc) throws Exception {
		pipe.dataLayer.colors.ColoredInhibitorArc cia = (pipe.dataLayer.colors.ColoredInhibitorArc)arc;
		
		ColoredInterval timeGuard = new ColoredInterval(cia.getTimeGuard().toStringWithoutConstants());
		ColorSet colorGuard = transformColorGuard(cia.getColorGuard());
		
		ColoredInhibitorArc newArc = new ColoredInhibitorArc(
				(ColoredPlace)PlaceTransitionObjectBookKeeper.get(arc.getSource()),
				(TAPNTransition)PlaceTransitionObjectBookKeeper.get(arc.getTarget()),
				timeGuard,
				colorGuard);
		
		aAUPetriNet.add(newArc);
	}
	
	@Override
	protected void transformTimedArc(TimedArc arc) throws Exception {
		pipe.dataLayer.colors.ColoredInputArc cia = (pipe.dataLayer.colors.ColoredInputArc)arc;
		
		ColoredInterval timeGuard = new ColoredInterval(cia.getTimeGuard().toStringWithoutConstants());
		ColorSet colorGuard = transformColorGuard(cia.getColorGuard());
		
		ColoredInputArc newArc = new ColoredInputArc(
				(ColoredPlace)PlaceTransitionObjectBookKeeper.get(arc.getSource()),
				(TAPNTransition)PlaceTransitionObjectBookKeeper.get(arc.getTarget()),
				timeGuard,
				colorGuard);
		
		aAUPetriNet.add(newArc);
	}
	
	@Override
	protected void transformNormalArc(NormalArc arc) throws Exception {
		pipe.dataLayer.colors.ColoredOutputArc coa = (pipe.dataLayer.colors.ColoredOutputArc)arc;
		
		ColoredOutputArc newArc = new ColoredOutputArc(
				(TAPNTransition)PlaceTransitionObjectBookKeeper.get(arc.getSource()),
				(ColoredPlace)PlaceTransitionObjectBookKeeper.get(arc.getTarget()),
				coa.getOutputValue().getValue());
		
		aAUPetriNet.add(newArc);
	}
}