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

package net.bluewizardhat.yamlcfn.sg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandLine {
	public static void main(String[] args) {
		try {
			List<String> sourceFileNames = new ArrayList<>();
			String targetFileName = null;
			for (String arg : args) {
				if ("--debug".equals(arg)) {
					setLoglevel(Level.DEBUG);
					log.debug("DEBUG loglevel enabled");
				} else if ("--trace".equals(arg)) {
					setLoglevel(Level.TRACE);
					log.trace("TRACE loglevel enabled");
				} else {
					if (targetFileName != null) {
						sourceFileNames.add(targetFileName);
					}
					targetFileName = arg;
				}
			}

			log.debug("sourceFiles: {}", sourceFileNames);
			log.debug("targetFile: {}", targetFileName);

			if (sourceFileNames.isEmpty() || targetFileName == null) {
				System.out.println("Usage: java -jar yamlcfn-1.0-all.jar [--debug|--trace] source-1 [source-2..source-n] target");
				return;
			} else {
				File targetFile = new File(targetFileName);
				if (sourceFileNames.size() > 1 && !targetFile.isDirectory()) {
					System.err.println("When converting several files target must be a directory!");
					return;
				}

				for (String sourceFileName : sourceFileNames) {
					File sourceFile = new File(sourceFileName);
					File actualtargetFile = targetFile;
					if (targetFile.isDirectory()) {
						String actualOutputName = sourceFile.getName().replaceAll("\\.ya?ml$", "") + ".json";
						actualtargetFile = new File(targetFile, actualOutputName);
					}

					log.info("Converting '{}' -> '{}'", sourceFile.getPath(), actualtargetFile.getPath());
					try (InputStream in = new FileInputStream(sourceFile);
						OutputStream out = new FileOutputStream(actualtargetFile)) {
						YamlCfn.convert(in, out);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void setLoglevel(Level level) {
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		context.getConfiguration().getRootLogger().setLevel(level);
		context.updateLoggers();
	}
}
