package dk.aau.cs.model.petriNet;

import dk.aau.cs.util.Require;

public class OutputArc {
	private Transition source;
	private Place destination;

	public OutputArc(Transition source, Place destination){
		Require.that(source != null, "An arc must have a non-null source transition");
		Require.that(destination != null, "An arc must have a non-null destination place");
		
		this.source = source;
		this.destination = destination;
	}
	
	public Transition destination() {
		return source;
	}

	public Place source() {
		return destination;
	}
}
