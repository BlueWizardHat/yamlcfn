package net.bluewizardhat.yamlcfn.sg.data.yaml;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public abstract class NamedElement {
	private final String name;
	private final String cfnName;

	public NamedElement(String name) {
		this.name = name;
		cfnName = cfnName(name);
	}

	private String cfnName(String name) {
		String cfnName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		if (name.matches(".*[:_-].*")) {
			StringBuilder b = new StringBuilder(name.length());
			boolean upperNext = true;
			for (char ch : name.toCharArray()) {
				if (ch == ':' || ch == '-' || ch == '_') {
					upperNext = true;
					continue;
				}
				b.append(upperNext ? Character.toUpperCase(ch) : ch);
				upperNext = false;
			}
			cfnName = b.toString();
		}

		return postfix(cfnName);
	}

	protected String postfix(String cfnName) {
		return cfnName;
	}
}
