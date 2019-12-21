import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@Documented
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@interface CustomizedAnnotation
{
    String function() default "default descriptor";
    int revision() default 1;
}

public abstract class AnnotationParser
{
    private static Set<Class> statistics = new HashSet<Class>();
    public static void parseAnnotation(Class subject)
    {
        if (!statistics.contains(subject))
        {
            statistics.add(subject);
            Method[] method = subject.getMethods();
            for (Method element : method)
                if (element.isAnnotationPresent(CustomizedAnnotation.class))
                {
                    CustomizedAnnotation specifiedAnnotation = element.getAnnotation(CustomizedAnnotation.class);
                    System.out.println("class: " + subject.getName() +"\nmethod: " + element.getName() + "\nfunction: " + specifiedAnnotation.function() + "\nrevision: " + specifiedAnnotation.revision() + "\n");
                }
        }
    }
}