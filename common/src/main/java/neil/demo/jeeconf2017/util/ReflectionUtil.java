package neil.demo.jeeconf2017.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

/**
 * <P>Reflection based helper code to dynamically work
 * out how to display domain objects.
 * </P>
 */
@Slf4j
public class ReflectionUtil {

	/**
	 * <P>Find all fields in the domain object to be
	 * column headers in an HTML table.
	 * </P>
	 * 
	 * @param klass The domain object
	 * @return The column names, alphabetic order
	 */
	public static Set<String> getColumns(Class<?> klass) {
		Set<String> columns = new TreeSet<>();
        for (Field field : klass.getDeclaredFields()) {
        	if (!Modifier.isStatic(field.getModifiers())) {
                columns.add(field.getName());
        	}
        }
        return columns;
	}
	
	/**
	 * <P>Reflection based util to dump a list of Java objects into a format
	 * for an HTML table. One row per item. One column per item's fields.
	 * </P>
	 * 
	 * @param collection A list of items of the specified class
	 * @param klass The class of the items in the collection, all the same
	 * @return A list of name value pairs
	 */
	public static List<Map<String, String>> getData(Collection<?> collection, Class<?> klass) {
        List<Map<String, String>> data = new ArrayList<>();
        
        collection.stream().forEach(item -> {
            Map<String, String> datum = new TreeMap<>();

            for (Field field : klass.getDeclaredFields()) {
            	if (!Modifier.isStatic(field.getModifiers())) {
            		field.setAccessible(true);
            		try {
            			datum.put(field.getName(), field.get(item).toString());
            		} catch (Exception e) {
            			log.error("getData", e);
            			datum.put(field.getName(), "?");
            		}
            	}
            }
            data.add(datum);
        });

        return data;
	}
}
