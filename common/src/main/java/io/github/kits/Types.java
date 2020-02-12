package io.github.kits;

import io.github.kits.configuration.TypeFunctionConfig;
import io.github.kits.enums.FunctionType;
import io.github.kits.exception.TypeException;
import io.github.kits.json.Json;
import io.github.kits.log.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static io.github.kits.enums.FunctionType.ARRAY;
import static io.github.kits.enums.FunctionType.BASIC;
import static io.github.kits.enums.FunctionType.BIG_DECIMAL;
import static io.github.kits.enums.FunctionType.BOOLEAN;
import static io.github.kits.enums.FunctionType.CLASS;
import static io.github.kits.enums.FunctionType.COLLECTION;
import static io.github.kits.enums.FunctionType.DATE;
import static io.github.kits.enums.FunctionType.ENUM;
import static io.github.kits.enums.FunctionType.MAP;
import static io.github.kits.enums.FunctionType.NUMBER;
import static io.github.kits.enums.FunctionType.OBJECT;
import static io.github.kits.enums.FunctionType.STRING;
import static io.github.kits.enums.FunctionType.SYSTEM;

/**
 * @author whimthen
 * @version 1.0.0
 * @since 1.0.0
 */
public class Types {

	public static final String[] basicArrName = {"[S", "[I", "[J", "[F", "[D", "[Z"};

	public static <T, R> R function(T object, TypeFunctionConfig<T, R> config) {
		if (Objects.isNull(object)) {
			throw new TypeException("Class Type is null!");
		}
		FunctionType functionType = OBJECT;
		if (object instanceof Collection) {
			functionType = COLLECTION;
		} else if (object instanceof Map) {
			functionType = MAP;
		} else if (object instanceof Date) {
			functionType = DATE;
		} else if (object instanceof BigDecimal) {
			functionType = BIG_DECIMAL;
		} else if (object.getClass().isArray()) {
			functionType = ARRAY;
		} else if (object instanceof Class) {
			functionType = CLASS;
		} else if (object instanceof Boolean) {
			functionType = BOOLEAN;
		} else if (object instanceof Number && !(object instanceof Byte)) {
			functionType = NUMBER;
		} else if (Envs.isBasicType(object.getClass())) {
			functionType = BASIC;
		} else if (Envs.isSystemType(object.getClass())) {
			functionType = SYSTEM;
		} else if (object instanceof Enum) {
			functionType = ENUM;
		} else if (object instanceof CharSequence || object instanceof Character) {
			functionType = STRING;
		}
		return apply(functionType, config, object);
	}

	private static <T, R> R apply(FunctionType type, TypeFunctionConfig<T, R> config, T object) {
		return FunctionType.getFunction(type, config)
						   .map(function -> function.apply(object))
						   .orElse(null);
	}

	public static <R> R type(Object object, Class<R> target) {
		if (Objects.isNull(target)) {
			throw new TypeException("Target class is null!");
		}
		if (Objects.isNull(object)) {
			return null;
		}
		R r = null;
		if (Collection.class.isAssignableFrom(target)) {
			if (List.class.isAssignableFrom(target)) {
				ArrayList<Object> objects = new ArrayList<>();
				objects.add(object);
				r = (R) objects;
			} else if (Set.class.isAssignableFrom(target)) {
				HashSet<Object> objects = new HashSet<>();
				objects.add(object);
				r = (R) objects;
			}
		} else if (Map.class.isAssignableFrom(target)) {
			HashMap<Object, Object> maps = new HashMap<>();
			maps.put("", object);
			r = (R) maps;
		} else if (Date.class.isAssignableFrom(target)) {
			if (object instanceof Date) {
				r = (R) object;
			} else {
				r = (R) DateTimes.get(object.toString());
			}
		} else if (BigDecimal.class.isAssignableFrom(target)) {
			if (object instanceof BigDecimal) {
				r = (R) object;
			} else {
				r = (R) new BigDecimal(object.toString());
			}
		} else if (target.isArray()) {
			r = array(object, target);
		} else if (Class.class.isAssignableFrom(target)) {

		} else if (Boolean.class.isAssignableFrom(target)) {
			if (object instanceof Boolean) {
				r = (R) object;
			} else {
				boolean b = false;
				if (Strings.isBoolean(object.toString())) {
					b = Boolean.parseBoolean(object.toString());
				}
				r = (R) Boolean.valueOf(b);
			}
		} else if (Number.class.isAssignableFrom(target) && !(Byte.class.isAssignableFrom(target))) {

		}
//		else if (Envs.isBasicType(target)) {
//
//		} else if (Envs.isSystemType(target)) {
//
//		}
		else if (Enum.class.isAssignableFrom(target)) {

		} else if (CharSequence.class.isAssignableFrom(target) || Character.class.isAssignableFrom(target)) {
			r = (R) object.toString();
		} else {
			r = (R) object;
		}
		return r;
	}

	/**
	 * 构建新数组，并将object的值放入新数组中
	 *
	 * @param object 对象
	 * @param tClass 需要转换的目标对象类型
	 * @param <T>    范型
	 * @return 新构建的数组
	 */
	private static <T> T array(Object object, Class<T> tClass) {
		Object arr = null;
		if (Objects.isNull(object)) {
			arr = Array.newInstance(tClass.getComponentType(), 0);
		} else if (object.getClass().isArray() || object instanceof Collection) {
			if (object instanceof Collection) {
				object = ((Collection) object).toArray();
			}
			int length = Array.getLength(object);
			arr = Array.newInstance(tClass.getComponentType(), length);
			for (int i = 0; i < length; i++) {
				Array.set(arr, i, Array.get(object, i));
			}
		} else {
			Class<?> singleArrType = tClass.getComponentType();
			if (!tClass.getComponentType().isPrimitive()) {
				String name = tClass.getName();
				if (Strings.isNotBlack(name)) {
					name = name.substring(2);
					name = name.substring(0, name.length() - 1);
				}
				singleArrType = Envs.forName(name);
				if (!singleArrType.isAssignableFrom(object.getClass())) {
					throw new TypeException("array element type mismatch: " + object.getClass().getName()
												+ " for array type: " + tClass.getName());
				}
			}
			arr = Array.newInstance(singleArrType, 1);
			Array.set(arr, 0, object);
		}
		return (T) arr;
	}

	private static <T> T object(Object object, Class<T> tClass) {
		return null;
	}

}
