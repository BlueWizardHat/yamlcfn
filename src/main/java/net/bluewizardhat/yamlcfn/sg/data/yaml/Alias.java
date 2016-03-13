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

import java.util.Map;

import lombok.Value;

@Value
public class Alias {
	public enum AliasType {
		CIDR,
		PORTS,
		SECURITYGROUP;

		public static AliasType from(String str) {
			return AliasType.valueOf(str.toUpperCase());
		}
	}

	public final static Alias world = new Alias("world", AliasType.CIDR, "0.0.0.0/0");

	private String name;
	private AliasType type;
	private String value;

	public static void addAllAliases(Map<String, Alias> aliases) {
		aliases.put(world.getName(), world);
	}

}
