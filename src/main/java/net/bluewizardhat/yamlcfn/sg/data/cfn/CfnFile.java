package net.bluewizardhat.yamlcfn.sg.data.cfn;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CfnFile {
	private String description;
	private List<CfnParam> parameters;
	private List<CfnSecurityGroup> securitygroups;
	private List<CfnConnection> ingress = new ArrayList<>();
	private List<CfnConnection> egress = new ArrayList<>();
}
