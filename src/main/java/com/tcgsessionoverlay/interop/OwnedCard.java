package com.tcgsessionoverlay.interop;

import lombok.Getter;

@Getter
public final class OwnedCard
{
	private final String cardName;
	private final long pulledAt;
	private final boolean foil;
	private final boolean beta;

	public OwnedCard(String cardName, long pulledAt, boolean foil, boolean beta)
	{
		this.cardName = cardName;
		this.pulledAt = pulledAt;
		this.foil = foil;
		this.beta = beta;
	}
}
