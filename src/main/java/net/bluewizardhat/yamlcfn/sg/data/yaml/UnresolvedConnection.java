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

package net.bluewizardhat.yamlcfn.sg.data.yaml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

public abstract class UnresolvedConnection {
	@Getter
	@ToString
	@AllArgsConstructor
	public static class Ref extends UnresolvedConnection {
		private final String ref;
	}

	@Getter
	@ToString
	public static class RefWithPort extends Ref {
		private final Protocol protocol;
		private final PortSpec portSpec;

		public RefWithPort(String ref, Protocol protocol, PortSpec portSpec) {
			super(ref);
			this.protocol = protocol;
			this.portSpec = portSpec;
		}
	}

	public abstract String getRef();
}
