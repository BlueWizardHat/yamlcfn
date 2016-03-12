package net.bluewizardhat.yamlcfn.sg.data.cfn;

import lombok.EqualsAndHashCode;
import lombok.Value;

public abstract class CfnEndpoint {
	@Value
	@EqualsAndHashCode(callSuper = false)
	public static class CidrEndpoint extends CfnEndpoint {
		private CfnValue.StringValue value;
		@Override
		public String getType() { return "CIDR"; }
		@Override
		public boolean isInternal() { return true; }
		public CidrEndpoint(String cidr) {
			value = new CfnValue.StringValue(cidr);
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	public static class SgEndpoint extends CfnEndpoint {
		private CfnValue value;
		private boolean internal;
		@Override
		public String getType() { return "SG"; }
	}

	public abstract String getType();
	public abstract boolean isInternal();
}
