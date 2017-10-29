package de.zaunkoenigweg.rspio.audio;

public interface PlaybackIndicator {

	public void playing();
	public void stopped();

	public static PlaybackIndicator ignoring() {
		return new PlaybackIndicator() {
			
			@Override
			public void stopped() {
			}
			
			@Override
			public void playing() {
			}
		};
	}

}
