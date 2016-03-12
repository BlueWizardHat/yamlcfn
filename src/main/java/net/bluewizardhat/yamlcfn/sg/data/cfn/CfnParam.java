package net.bluewizardhat.yamlcfn.sg.data.cfn;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CfnParam {
	private String name;
	private String defaultValue;
	private String description;
	private String type;
}
