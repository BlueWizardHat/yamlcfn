package net.bluewizardhat.yamlcfn.sg.data.cfn;


@lombok.Value
public class CfnTag {
	private String key;
	private CfnValue value;
}
