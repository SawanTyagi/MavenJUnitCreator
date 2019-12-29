
package creator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.mockito.Mockito;

import ch.qos.logback.core.net.SyslogOutputStream;

/**
 *
 * @author sawtyagi
 *
 */
public class JUnitCreator {

	/**
	 * @param args
	 * @throws SecurityException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws SecurityException, ClassNotFoundException, IOException {
		if (args.length > 0) {
			final String filePath = args[0];
			String[] split = filePath.split("src\\\\main\\\\java");

			final String pathOfTestClass = split[0] + "src\\test\\java" + split[1];
			String fileNameWithPackage = split[1].replaceAll("\\\\", ".");
			fileNameWithPackage = fileNameWithPackage.substring(1, fileNameWithPackage.length() - 5);// Remove first dot
																										// and .java
																										// extension

			split = fileNameWithPackage.split("\\.");

			final String PATH = pathOfTestClass.substring(0, pathOfTestClass.lastIndexOf("\\"));

			final File directory = new File(PATH);
			if (!directory.exists()) {
				directory.mkdirs();
			}

			final String testFileName = pathOfTestClass.substring(pathOfTestClass.lastIndexOf("\\"),
					pathOfTestClass.length() - 5) + "Test.java";
			final File junitJavaFile = new File(PATH + testFileName);
			junitJavaFile.createNewFile();

			final FileWriter fileWriter = new FileWriter(junitJavaFile);

			fileWriter.write(
					"package " + fileNameWithPackage.substring(0, fileNameWithPackage.lastIndexOf(".")) + ";\n\n");

			final Field[] allFields = Class.forName(fileNameWithPackage).getDeclaredFields();

			final Method[] allMethods = Class.forName(fileNameWithPackage).getDeclaredMethods();

			JUnitCreator.createImportStatements(fileWriter, allFields, fileNameWithPackage);

			fileWriter.append("\n");

			final String name = junitJavaFile.getName();

			JUnitCreator.createClassSignature(fileWriter, name);

			JUnitCreator.createFields(fileWriter, allFields, name);

			JUnitCreator.createTestMethods(fileWriter, allMethods, name);

			JUnitCreator.createAffecterMockMethod(fileWriter, allFields, fileNameWithPackage, filePath);

			JUnitCreator.closeClassSignature(fileWriter);

			fileWriter.close();

		}
	}

	/**
	 * Creates empty test methods for all public methods.
	 * 
	 * @param name
	 * @param allMethods
	 * @param fileWriter
	 */
	private static void createTestMethods(FileWriter fileWriter, Method[] allMethods, String name) {

		Arrays.asList(allMethods).stream().filter(method -> method.getModifiers() != 2).forEach(method -> {
			try {
				fileWriter.append("@Test\n");
				fileWriter.append("public void " + method.getName() + "Test(){\n}\n");
			} catch (final IOException e) {
			}
		});

	}

	/**
	 * @param fileWriter
	 * @param allFields
	 * @param fileName
	 * @param filePath
	 */
	private static void createAffecterMockMethod(final FileWriter fileWriter, final Field[] allFields,
			final String fileName, final String filePath) {
		final Map<Field, Set<String>> mapOfFieldAndMethodCalls = getMapOfFieldAndMethodCalls(allFields, filePath);

		StringBuilder methodProtoType = new StringBuilder("\nprivate void affecterMock(");
		StringBuilder methodBody = new StringBuilder();
		final Random random = new Random();
		for (Iterator<Entry<Field, Set<String>>> iterator = mapOfFieldAndMethodCalls.entrySet().iterator(); iterator
				.hasNext();) {
			Entry<Field, Set<String>> entry = iterator.next();
			Field field = entry.getKey();
			List<Method> methods = Arrays.asList(field.getType().getMethods());
			entry.getValue().stream().forEach(calledMethodName -> {
				Optional<Method> optionalMethod = methods.stream()
						.filter(method -> calledMethodName.split("\\.")[1].equals(method.getName())).findFirst();
				if (optionalMethod.isPresent()) {
					Class returnType = optionalMethod.get().getReturnType();
					String returnTypeString = returnType.getCanonicalName();
					Parameter[] parameters = optionalMethod.get().getParameters();
					
					if (!"void".equals(returnTypeString)) {
						methodBodyForNonVoid(methodProtoType, methodBody, random, field, optionalMethod, returnTypeString,
								parameters);
					} else {
						methodBodyForVoid(methodBody, field, optionalMethod, parameters);
					}
				}
			});
		}
		if ((methodProtoType.charAt(methodProtoType.length() - 1) + "").equals(",")) {
			methodProtoType.deleteCharAt(methodProtoType.length() - 1);
		}

		try {
			fileWriter.append(methodProtoType).append("){").append(methodBody).append("\n}");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param allFields
	 * @param filePath
	 * @return Map<Field, Set<String>>
	 */
	private static Map<Field, Set<String>> getMapOfFieldAndMethodCalls(final Field[] allFields, final String filePath) {
		final File file = new File(filePath);
		final Set<String> methodCalls = new HashSet<>();
		final Map<Field, Set<String>> mapOfFieldAndMethodCalls = new HashMap<>();
		Arrays.asList(allFields).stream().forEach(field -> {
			final String fieldName = field.getName();
			try {
				final BufferedReader br = new BufferedReader(new FileReader(file));
				String st;
				StringBuilder builder = new StringBuilder();
				while ((st = br.readLine()) != null) {
					if (!st.contains(";")) {
						builder.append(st);
					} else {
						builder.append(st);
						final String oneJavaLine = builder.toString().trim().replaceAll("\\s+", "");
						final String fieldNameWithDot = fieldName + ".";
						if (oneJavaLine.contains(fieldNameWithDot)) {
							final String methodcall = oneJavaLine.substring(oneJavaLine.indexOf(fieldNameWithDot),
									oneJavaLine.lastIndexOf(")") + 1);
							methodCalls.add(methodcall.substring(0, methodcall.indexOf("(")));
						}
						builder = new StringBuilder();
					}
				}
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
			mapOfFieldAndMethodCalls.put(field, methodCalls);
		});
		return mapOfFieldAndMethodCalls;
	}

	/**
	 * @param methodBody
	 * @param field
	 * @param optionalMethod
	 * @param parameters
	 */
	private static void methodBodyForVoid(StringBuilder methodBody, Field field, Optional<Method> optionalMethod,
			Parameter[] parameters) {
		methodBody.append("Mockito.doNothing().when(").append(field.getName()).append(").")
				.append(optionalMethod.get().getName()).append("(");
		for (int i = 0; i < parameters.length; i++) {
			methodBody.append("Matchers.any(),");
		}
		if ((methodBody.charAt(methodBody.length() - 1) + "").equals(",")) {
			methodBody.deleteCharAt(methodBody.length() - 1);
		}
		methodBody.append(");");
	}

	/**
	 * @param methodProtoType
	 * @param methodBody
	 * @param random
	 * @param field
	 * @param optionalMethod
	 * @param returnTypeString
	 * @param parameters
	 */
	private static void methodBodyForNonVoid(StringBuilder methodProtoType, StringBuilder methodBody, final Random random,
			Field field, Optional<Method> optionalMethod, String returnTypeString, Parameter[] parameters) {
		String[] split = returnTypeString.split("\\.");
		StringBuilder returnVarName = new StringBuilder().append("returnVar")
				.append(split[split.length - 1]).append(random.nextInt(100));
		methodBody.append("Mockito.when(").append(field.getName()).append(".")
				.append(optionalMethod.get().getName()).append("(");
		
		for (int i = 0; i < parameters.length; i++) {
			methodBody.append("Matchers.any(),");
		}
		if ((methodBody.charAt(methodBody.length() - 1) + "").equals(",")) {
			methodBody.deleteCharAt(methodBody.length() - 1);
		}
		methodBody.append(")).thenReturn(").append(returnVarName)
		.append(");");
		methodProtoType.append(split[split.length - 1]).append(" ").append(returnVarName).append(",");
	}

	private static void closeClassSignature(final FileWriter fileWriter) throws IOException {
		fileWriter.append("}");

	}

	/**
	 * @param fileWriter
	 * @param allFields
	 * @param name
	 * @throws IOException
	 */
	private static void createFields(final FileWriter fileWriter, final Field[] allFields, final String name)
			throws IOException {
		final String[] split = name.split("\\.");
		final String mockFileName = split[split.length - 2];
		fileWriter.append("\n@InjectMocks\nprivate " + mockFileName.substring(0, mockFileName.length() - 4) + " "
				+ mockFileName.substring(0, mockFileName.length() - 4) + ";\n");
		Arrays.asList(allFields).stream().forEach(field -> {
			try {
				final String[] substrings = field.getType().toString().split("\\.");
				fileWriter.append("@Mock\n");
				fileWriter.append("private " + substrings[substrings.length - 1] + " " + field.getName() + ";\n");
			} catch (final IOException e) {
			}
		});
	}

	/**
	 * @param fileWriter
	 * @param name
	 * @throws IOException
	 */
	private static void createClassSignature(final FileWriter fileWriter, final String name) throws IOException {
		fileWriter.append("@RunWith(PowerMockRunner.class)\n@PowerMockIgnore(\"javax.management.*\")");
		final String[] split = name.split("\\.");
		fileWriter.append("\npublic class " + split[split.length - 2] + "{\n");

	}

	/**
	 * @param fileWriter
	 * @param allFields
	 * @param fileName
	 * @throws IOException
	 */
	private static void createImportStatements(final FileWriter fileWriter, final Field[] allFields,
			final String fileName) throws IOException {

		fileWriter.append("import " + fileName + ";\n");
		fileWriter.append(
				"import org.mockito.InjectMocks;" + "\nimport org.mockito.Mock;" + "\nimport org.junit.runner.RunWith;"
						+ "\nimport org.powermock.core.classloader.annotations.PowerMockIgnore;"
						+ "\nimport org.powermock.modules.junit4.PowerMockRunner;" + "\nimport org.junit.Test;"
						+ "\nimport org.mockito.Mockito;\n"
						+ "\nimport org.mockito.Matchers;\n");
		Arrays.asList(allFields).stream().forEach(field -> {
			try {
				fileWriter.append("import " + field.getType().toString().split(" ")[1] + ";\n"); // Remove class prefix
																									// from field type
																									// name
																									// by using
																									// sub-string
			} catch (final IOException e) {
			}
		});

	}
}
