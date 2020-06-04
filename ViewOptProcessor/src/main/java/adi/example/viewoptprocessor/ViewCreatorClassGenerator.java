package adi.example.viewoptprocessor;

import com.squareup.javapoet.JavaFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class ViewCreatorClassGenerator {

    ProcessingEnvironment mProcessingEnv;
    TypeElement mClassElement;
    Messager mMessager;

    private String mPackageName;
    private String mClassName;
    private static final String sProxyInterfaceName = "IViewCreator";

    public ViewCreatorClassGenerator(ProcessingEnvironment processingEnv, TypeElement classElement, Messager messager) {
        mProcessingEnv = processingEnv;
        mClassElement = classElement;
        mMessager = messager;
        PackageElement packageOf = processingEnv.getElementUtils().getPackageOf(classElement);
        mPackageName = packageOf.getQualifiedName().toString();
        int packageLen = mPackageName.length() + 1;
        String className = classElement.getQualifiedName().toString().substring(packageLen).replace('.', '$');
        println("classElement =" + classElement.getQualifiedName().toString());
        mClassName = className + "proxy";
        println("mPackageName=" + mPackageName + " className=" + className);

    }

    private void println(String mes) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, mes);
    }

    public void generateClassFile() {
        Writer writer = null;
        try {
            JavaFileObject sourceFile = mProcessingEnv.getFiler().createSourceFile(mClassName, mClassElement);
            String classPath = sourceFile.toUri().getPath();
            println("classPath=" + classPath);
            String buildDirStr = "/app/build/";
            String buildDirFullPath = classPath.substring(0, classPath.indexOf(buildDirStr) + buildDirStr.length());
            File customViewFile = new File(buildDirFullPath + "tmp_custom_views/custom_view_final.txt");
            if (!customViewFile.exists()) {
                return;
            }
            HashSet<String> customViewClassNameSet = new HashSet<>();
            putClassListData(customViewClassNameSet, customViewFile);
            writer = sourceFile.openWriter();
            String generateClassInfoStr = generateClassInfoStr(customViewClassNameSet);
            writer.write(generateClassInfoStr);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String generateClassInfoStr(HashSet<String> customViewClassNameSet) {
        StringBuffer sb = new StringBuffer();
        sb.append("package " + mPackageName + "; \n\n")
                .append("import cn.segi.viewcreate.*;\n")
                .append("import android.content.Context;\n")
                .append("import android.util.AttributeSet;\n")
                .append("import android.view.*;\n")
                .append("import android.widget.*;\n")
                .append("import android.webkit.*;\n")
                .append("import android.app.*;\n\n")
                .append("public class " + mClassName + " implements " + sProxyInterfaceName)
                .append(" {\n");

        generateMethodStr(sb, customViewClassNameSet);
        sb.append('\n');

        sb.append("}\n");
        return sb.toString();
    }

    private void generateMethodStr(StringBuffer builder, HashSet<String> customViewClassNameSet) {
        builder.append("@Override\n ");
        builder.append("public View createView(String name, Context context, AttributeSet attrs ) {\n");


        builder.append("    switch(name){\n");

        for (String className : customViewClassNameSet) {
            if (className == null || className.trim().length() == 0) {
                continue;
            }
            builder.append("        case \"" + className + "\" :\n");
            builder.append("        return new " + className + "(context,attrs);\n");
        }

        builder.append("    }\n");

        builder.append("    return null;\n");
        builder.append("  }\n");
    }

    private void putClassListData(HashSet<String> customViewClassNameSet, File customViewFile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(customViewFile));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() <= 0) {
                    continue;
                }
                println(line);
                customViewClassNameSet.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
