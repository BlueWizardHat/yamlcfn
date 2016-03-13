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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public abstract class NamedElement {
	private final String name;
	private final String cfnName;

	public NamedElement(String name) {
		this.name = name;
		cfnName = cfnName(name);
	}

	private String cfnName(String name) {
		String cfnName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		if (name.matches(".*[:_-].*")) {
			StringBuilder b = new StringBuilder(name.length());
			boolean upperNext = true;
			for (char ch : name.toCharArray()) {
				if (ch == ':' || ch == '-' || ch == '_') {
					upperNext = true;
					continue;
				}
				b.append(upperNext ? Character.toUpperCase(ch) : ch);
				upperNext = false;
			}
			cfnName = b.toString();
		}

		return postfix(cfnName);
	}

	protected String postfix(String cfnName) {
		return cfnName;
	}
}
