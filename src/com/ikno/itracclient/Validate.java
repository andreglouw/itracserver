package com.ikno.itracclient;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public class Validate {

	public static class Numeric implements VerifyListener {
		public void verifyText(VerifyEvent e) {
			if (e.text.length() <= 1) {
				int c = (int)e.character;
				if (!(('0' <= e.character && e.character <= '9') || e.character == '-' || e.character == '+' || e.character == '\b' || c == 127 || c == 0)) {
					e.doit = false;
				}
			} else {
				try {
					Integer.parseInt(e.text);
				} catch (Exception exc) {
					e.doit = false;
				}
			}
		}
	}
	public static class Floating implements VerifyListener {
		public void verifyText(VerifyEvent e) {
			if (e.text.length() <= 1) {
				int c = (int)e.character;
				if (!(('0' <= e.character && e.character <= '9') || e.character == '.' || e.character == '-' || e.character == '+' || e.character == '\b' || c == 127 || c == 0)) {
					e.doit = false;
				}
			} else {
				try {
					Double.parseDouble(e.text);
				} catch (Exception exc) {
					e.doit = false;
				}
			}
		}
	}
}
