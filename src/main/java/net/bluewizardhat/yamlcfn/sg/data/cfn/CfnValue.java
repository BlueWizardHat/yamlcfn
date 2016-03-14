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
		@Override
		public String getValue() { throw new UnsupportedOperationException(); }
	}

	public abstract String getType();
	public abstract String getValue();
}
