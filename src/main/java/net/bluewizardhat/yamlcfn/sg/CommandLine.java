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
			List<String> inputFileNames = new ArrayList<>();
			String outputFileName = null;
			for (String arg : args) {
				if ("--debug".equals(arg)) {
					setLoglevel(Level.DEBUG);
					log.debug("DEBUG loglevel enabled");
				} else if ("--trace".equals(arg)) {
					setLoglevel(Level.TRACE);
					log.trace("TRACE loglevel enabled");
				} else {
					if (outputFileName != null) {
						inputFileNames.add(outputFileName);
					}
					outputFileName = arg;
				}
			}

			log.debug("inputFiles: {}", inputFileNames);
			log.debug("outputFile: {}", outputFileName);

			if (inputFileNames.isEmpty() || outputFileName == null) {
				System.out.println("Usage: java -jar yamlcfn-1.0-all.jar [--debug|--trace] input1 [input2..inputn] output");
				return;
			} else {
				File outputFile = new File(outputFileName);
				if (inputFileNames.size() > 1 && !outputFile.isDirectory()) {
					System.err.println("When converting several files output must be a directory!");
					return;
				}

				for (String inputFileName : inputFileNames) {
					File inputFile = new File(inputFileName);
					File actualOutputFile = outputFile;
					if (outputFile.isDirectory()) {
						String actualOutputName = inputFile.getName().replaceAll("\\.ya?ml$", "") + ".json";
						actualOutputFile = new File(outputFile, actualOutputName);
					}

					log.info("Converting '{}' -> '{}'", inputFile.getPath(), actualOutputFile.getPath());
					try (InputStream in = new FileInputStream(inputFile);
						OutputStream out = new FileOutputStream(actualOutputFile)) {
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
