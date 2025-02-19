package main.java.mylombok;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SupportedAnnotationTypes("main.java.mylombok.MyGetter")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class MyGetterProcessor extends AbstractProcessor {

    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(MyGetter.class)) {
            ElementKind kind = element.getKind();

            if (!getSupportedKinds().contains(kind)) {
                continue;
            }

            if (getSupportedClasses().contains(kind)) {
                TypeElement classElement = (TypeElement) element;
                generateGettersForType(classElement);
            }

            if (getSupportedFields().contains(kind)) {
                generateGetterForField(element);
            }
        }
        return true;
    }

    private List<ElementKind> getSupportedKinds() {
        return Stream.of(getSupportedClasses(), getSupportedFields())
                .flatMap(List::stream)
                .toList();
    }

    private List<ElementKind> getSupportedClasses() {
        return List.of(ElementKind.CLASS, ElementKind.ENUM, ElementKind.INTERFACE);
    }

    private List<ElementKind> getSupportedFields() {
        return List.of(ElementKind.FIELD, ElementKind.ENUM_CONSTANT);
    }

    private void generateGettersForType(TypeElement classElement) {
        String className = classElement.getSimpleName().toString();
        String packageName = processingEnv.getElementUtils().getPackageOf(classElement).toString();

        String fields = classElement.getEnclosedElements().stream()
                .filter(e -> getSupportedFields().contains(e.getKind()))
                .map(this::generateFieldDeclaration)
                .collect(Collectors.joining("\n"));

        String getters = classElement.getEnclosedElements().stream()
                .filter(e -> getSupportedFields().contains(e.getKind()))
                .map(this::generateGetterMethod)
                .collect(Collectors.joining("\n\n"));

        String generatedClass = String.format(
                "package %s;\n\npublic class %sGenerated {\n\n%s\n\n%s\n}",
                packageName, className, fields, getters
        );

        writeGeneratedFile(packageName, className + "Generated", generatedClass);
    }

    private void generateGetterForField(Element fieldElement) {
        String className = fieldElement.getEnclosingElement().getSimpleName().toString() + "FieldGetter";
        String packageName = processingEnv.getElementUtils().getPackageOf(fieldElement).toString();

        String field = generateFieldDeclaration(fieldElement);
        String getter = generateGetterMethod(fieldElement);

        String generatedClass = String.format(
                "package %s;\n\npublic class %sGenerated {\n\n%s\n\n%s\n}",
                packageName, className, field, getter
        );

        writeGeneratedFile(packageName, className + "Generated", generatedClass);
    }

    private String generateFieldDeclaration(Element field) {
        String fieldName = field.getSimpleName().toString();
        String fieldType = field.asType().toString();
        return String.format("  private %s %s;", fieldType, fieldName);
    }

    private String generateGetterMethod(Element field) {
        String fieldName = field.getSimpleName().toString();
        String fieldType = field.asType().toString();
        String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        return String.format("  public %s %s() { return this.%s; }",
                fieldType, methodName, fieldName);
    }

    private void writeGeneratedFile(String packageName, String className, String content) {
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + className);
            try (Writer writer = file.openWriter()) {
                writer.write(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
