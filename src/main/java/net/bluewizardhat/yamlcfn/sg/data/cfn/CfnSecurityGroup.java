package net.bluewizardhat.yamlcfn.sg.data.cfn;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CfnSecurityGroup {
	private String name;
	private String description;
	private List<CfnConnection> inbound = new ArrayList<>();
	private List<CfnConnection> outbound = new ArrayList<>();
	private List<CfnTag> tags;
	private CfnValue vpcId;
}
