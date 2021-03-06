
package creator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

/**
 *
 * @author sawtyagi
 *
 */
public class JUnitCreator {

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

            Arrays.asList(allFields).stream().forEach(field -> System.out.println(field.getType()));

            JUnitCreator.createImportStatements(fileWriter, allFields, fileNameWithPackage);

            fileWriter.append("\n");

            final String name = junitJavaFile.getName();

            JUnitCreator.createClassSignature(fileWriter, name);

            JUnitCreator.createFields(fileWriter, allFields, name);

            JUnitCreator.createAffecterMockMethod(fileWriter, allFields, fileNameWithPackage, filePath);

            JUnitCreator.closeClassSignature(fileWriter);

            fileWriter.close();

        }
    }

    private static void createAffecterMockMethod(final FileWriter fileWriter, final Field[] allFields,
            final String fileName, final String filePath) {
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
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            mapOfFieldAndMethodCalls.put(field, methodCalls);
        });

        final Map<String, List<String>> fieldNameAndMethodMap = new HashMap<>();
        methodCalls.stream().forEach(entry -> {
            final String key = entry.split("\\.")[0];
            if (fieldNameAndMethodMap.containsKey(key)) {
                if (CollectionUtils.isNotEmpty(fieldNameAndMethodMap.get(key))) {
                    fieldNameAndMethodMap.get(key).add(entry.split("\\.")[1]);
                } else {
                    final List<String> listOfMethods = new ArrayList<>();
                    listOfMethods.add(entry.split("\\.")[1]);
                    fieldNameAndMethodMap.put(key, listOfMethods);
                }
            }
        });
        methodCalls.stream().forEach(System.out::println);
    }

    private static void closeClassSignature(final FileWriter fileWriter) throws IOException {
        fileWriter.append("}");

    }

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

    private static void createClassSignature(final FileWriter fileWriter, final String name) throws IOException {
        fileWriter.append("@RunWith(PowerMockRunner.class)\n@PowerMockIgnore(\"javax.management.*\")");
        final String[] split = name.split("\\.");
        fileWriter.append("\npublic class " + split[split.length - 2] + "{\n");

    }

    private static void createImportStatements(final FileWriter fileWriter, final Field[] allFields,
            final String fileName) throws IOException {

        fileWriter.append("import " + fileName + ";\n");
        fileWriter.append(
                "import org.mockito.InjectMocks;\nimport org.mockito.Mock;\nimport org.junit.runner.RunWith;\nimport org.powermock.core.classloader.annotations.PowerMockIgnore;\nimport org.powermock.modules.junit4.PowerMockRunner;\n");
        Arrays.asList(allFields).stream().forEach(field -> {
            try {
                fileWriter.append("import " + field.getType().toString().split(" ")[1] + ";\n"); // Remove class prefix
                                                                                                 // from field type name
                                                                                                 // by using sub-string
            } catch (final IOException e) {
            }
        });

    }
}
