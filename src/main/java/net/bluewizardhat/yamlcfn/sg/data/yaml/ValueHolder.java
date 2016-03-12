package net.bluewizardhat.yamlcfn.sg.data.yaml;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Value;

public abstract class ValueHolder {
	@Value
	@EqualsAndHashCode(callSuper = false)
	public static class StringValue extends ValueHolder {
		private String value;
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	public static class Join extends ValueHolder {
		private String delimiter;
		private List<String> values;
	}

	public static ValueHolder of(String value) {
		return new StringValue(value);
	}

	public static ValueHolder of(String delimiter, List<String> values) {
		return new Join(delimiter, values);
	}

}
