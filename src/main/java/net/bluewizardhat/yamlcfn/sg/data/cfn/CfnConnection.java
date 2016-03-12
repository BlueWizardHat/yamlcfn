package net.bluewizardhat.yamlcfn.sg.data.cfn;

import lombok.Data;
import lombok.experimental.Accessors;
import net.bluewizardhat.yamlcfn.sg.data.cfn.CfnEndpoint.SgEndpoint;
import net.bluewizardhat.yamlcfn.sg.data.cfn.CfnValue.RefValue;
import net.bluewizardhat.yamlcfn.sg.data.yaml.Protocol;

@Data
@Accessors(chain = true)
public class CfnConnection {
	private CfnEndpoint from;
	private CfnEndpoint to;
	private Protocol protocol;
	private int fromPort;
	private int toPort;

	public String getName () {
		if (from instanceof SgEndpoint && to instanceof SgEndpoint) {
			boolean self = false;
			SgEndpoint sgFrom = (SgEndpoint) from;
			SgEndpoint sgTo = (SgEndpoint) to;
			if (sgFrom.getValue() instanceof RefValue && sgTo.getValue() instanceof RefValue) {
				self = sgFrom.getValue().toString().equals(sgTo.getValue().toString());
				if (self) {
					return shortname(sgFrom.getValue()) + ports() + "Self";
				} else {
					return shortname(sgFrom.getValue()) + "To" + shortname(sgTo.getValue()) + ports();
				}
			}
		}
		throw new IllegalArgumentException("Error trying to name non sg-to-sg connection");
	}

	private String shortname(CfnValue value) {
		RefValue refv = (RefValue) value;
		return refv.getValue().replace("SecurityGroup", "");
	}

	private String ports() {
		if (fromPort == toPort || toPort < 0) {
			return protocol.getName() + fromPort;
		} else {
			return protocol.getName() + fromPort + "To" + toPort;
		}
	}
}
