package net.bluewizardhat.yamlcfn.sg.data.cfn;

import java.util.List;

import lombok.EqualsAndHashCode;

public abstract class CfnValue {
	@lombok.Value
	@EqualsAndHashCode(callSuper = false)
	public static class StringValue extends CfnValue {
		private String value;
		@Override
		public String getType() { return "STRING"; }
	}

	@lombok.Value
	@EqualsAndHashCode(callSuper = false)
	public static class RefValue extends CfnValue {
		private String value;
		@Override
		public String getType() { return "REF"; }
	}

	@lombok.Value
	@EqualsAndHashCode(callSuper = false)
	public static class JoinValue extends CfnValue {
		private String delimiter;
		private List<CfnValue> elements;
		@Override
		public String getType() { return "JOIN"; }
	}

	public abstract String getType();
}
