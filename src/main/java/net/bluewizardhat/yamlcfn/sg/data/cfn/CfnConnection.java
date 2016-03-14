/*
 * Copyright (C) 2016 BlueWizardHat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bluewizardhat.yamlcfn.sg.data.cfn;

import lombok.Data;
import lombok.Getter;
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

	@Getter(lazy = true)
	private final String name = generateName();

	private String generateName() {
		if (from instanceof SgEndpoint && to instanceof SgEndpoint) {
			SgEndpoint sgFrom = (SgEndpoint) from;
			SgEndpoint sgTo = (SgEndpoint) to;
			if (sgFrom.getValue() instanceof RefValue && sgTo.getValue() instanceof RefValue) {
				if (sgFrom.getValue().toString().equals(sgTo.getValue().toString())) {
					return shortname(sgFrom.getValue()) + ports() + "Self";
				} else {
					return shortname(sgFrom.getValue()) + "To" + shortname(sgTo.getValue()) + ports();
				}
			}
		}
		if (to instanceof SgEndpoint) {
			return to.getValue().getValue() + " From " + from.getValue().getValue() + " " + ports();
		}
		if (from instanceof SgEndpoint) {
			return from.getValue().getValue() + " To " + to.getValue().getValue() + " " + ports();
		}
		throw new IllegalArgumentException("Connection without an sg");
	}

	private String shortname(CfnValue value) {
		return value.getValue().replace("SecurityGroup", "");
	}

	private String ports() {
		if (fromPort == toPort || toPort < 0) {
			return protocol.getName() + fromPort;
		} else {
			return protocol.getName() + fromPort + "To" + toPort;
		}
	}
}
