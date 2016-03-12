package net.bluewizardhat.yamlcfn.sg.data.yaml;

import lombok.Value;

@Value
public class Tag {
	private String key;
	private ValueHolder value;
}
