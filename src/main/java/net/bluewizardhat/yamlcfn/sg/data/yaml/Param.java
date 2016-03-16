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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Param extends NamedElement {
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public enum ParamType {
		CIDR("String"),
		SECURITYGROUP("AWS::EC2::SecurityGroup::Id"),
		STRING("String"),
		VPC("AWS::EC2::VPC::Id");

		@Getter
		private final String cfnType;

		public static ParamType from(String str) {
			return ParamType.valueOf(str.toUpperCase());
		}
	}

	private String description;
	private ParamType type;
	private String defaultValue;

	public Param(String name, String description, ParamType type, String defaultValue) {
		super(name);
		this.description = description;
		this.type = type;
		this.defaultValue = defaultValue;
	}
}
