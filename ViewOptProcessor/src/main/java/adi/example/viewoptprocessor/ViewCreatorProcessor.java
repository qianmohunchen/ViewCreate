package adi.example.viewoptprocessor;

import com.google.auto.service.AutoService;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import adi.example.viewoptannotation.ViewOptHost;

@AutoService(Processor.class)
public class ViewCreatorProcessor extends AbstractProcessor {

    Messager messager;
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
         messager = processingEnvironment.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set=new HashSet<>();
        set.add(ViewOptHost.class.getCanonicalName());
        return set;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ViewOptHost.class);
        for(Element element:elements){
            TypeElement classElement= (TypeElement) element;
            ViewCreatorClassGenerator viewCreatorClassGenerator=   new ViewCreatorClassGenerator(processingEnv, classElement, messager);
            viewCreatorClassGenerator.generateClassFile();
        }
        return true;
    }
}
