package org.clockworx.vampire.event;

import org.clockworx.vampire.entity.VampirePlayer;

public class EventVampirePlayerVampireChange extends AbstractVampireEvent {
	
	protected final boolean vampire;

	
	public EventVampirePlayerVampireChange(boolean vampire, VampirePlayer vampirePlayer) {
		super(vampirePlayer);
		this.vampire = vampire;
	}
	
	public boolean isVampire() {
		return this.vampire;
	}
}
