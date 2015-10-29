package eu.fbk.dkm.premon.util;

/**
 * Created by alessio on 29/10/15.
 */

public abstract class PremonResource {
	protected String fileName;
	protected String type;
	protected String lemma;

	protected Object main;

	public String getFileName() {
		return fileName;
	}

	public String getType() {
		return type;
	}

	public String getLemma() {
		return lemma;
	}

}
