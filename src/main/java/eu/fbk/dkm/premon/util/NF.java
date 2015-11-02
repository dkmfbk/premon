package eu.fbk.dkm.premon.util;

import javax.annotation.Nullable;

/**
 * Created by alessio on 09/10/15.
 */

public class NF {
	private String n;
	private String f;

	public static final String AGENT = "a";
	public static final String MOD = "mod";

	public NF(@Nullable String n, @Nullable String f) {

		// This is about n

		if (n != null && n.length() > 0) {

				/*
				Property 'n' should be a number, 'm' or 'a'
				- number: is the arg-N (usually arg-0 is the agent)
				- 'a': arg-0 is not the agent
				- 'm': see property 'f'

				Note: when n='a' => f=''
				*/

			// Bug: hunker-v.xml
			if (n.toLowerCase().equals("dir")) {
				n = "m";
				f = "dir";
			}

			// Bug: go-v.xml
			if (n.toLowerCase().equals("argm-adv")) {
				n = "m";
				f = "adv";
			}
		}
		else {
			n = null;
		}

		if (n != null) {
			n = n.toLowerCase();
		}

		// This is about f

		if (f == null || f.length() <= 0) {
			f = null;
		}

		if (f != null) {
			f = f.toLowerCase();
			if (f.equals("str")) {
				f = null;
			}
		}

		this.n = n;
		this.f = f;
	}

	public String getArgName() {
		if (n != null) {
			if (n.equals("a")) {
				return AGENT;
			}
			else if (n.equals("m")) {
				if (f != null) {
					return f;
				}
				else {
					//todo: if n='m' the attribute f should be populated, but it's not always true (see abandon-v.xml)
					return MOD;
				}
			}
			else {
				return n;
			}
		}

		return f;
	}

	public String getN() {
		return n;
	}

	public String getF() {
		return f;
	}

	@Override
	public String toString() {
		return "NF{" +
				"n='" + n + '\'' +
				", f='" + f + '\'' +
				'}';
	}
}