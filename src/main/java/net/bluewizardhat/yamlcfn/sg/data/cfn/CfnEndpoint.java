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

import lombok.EqualsAndHashCode;
import lombok.Value;

public abstract class CfnEndpoint {
	@Value
	@EqualsAndHashCode(callSuper = false)
	public static class CidrEndpoint extends CfnEndpoint {
		private CfnValue value;
		@Override
		public String getType() { return "CIDR"; }
		@Override
		public boolean isInline() { return true; }
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	public static class SgEndpoint extends CfnEndpoint {
		private CfnValue value;
		private boolean inline;
		@Override
		public String getType() { return "SG"; }
	}

	public abstract String getType();
	public abstract CfnValue getValue();
	public abstract boolean isInline();
}
