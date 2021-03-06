package io.github.kits;

import io.github.kits.json.Json;
import io.github.kits.json.JsonPath;
import io.github.kits.log.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * JDK序列化和反序列化的封装
 *
 * @author whimthen
 * @version 1.0.0
 * @since 1.0.0
 */
public class Serializes {

	private static final String JSON_SERIALIZE_TYPE = "T";
	private static final String JSON_SERIALIZE_VALUE = "V";

	/**
	 * 序列化对象
	 *
	 * @param object Object
	 * @return bytes
	 */
	public static byte[] serialize(Object object) {
		if (!(object instanceof Serializable)) {
			throw new IllegalArgumentException("object must be implement Serializable");
		}
		ObjectOutputStream    objectOutputStream    = null;
		ByteArrayOutputStream byteArrayOutputStream = null;
		byte[]                result;
		try {
			byteArrayOutputStream = new ByteArrayOutputStream();
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(object);
			result = byteArrayOutputStream.toByteArray();
		} catch (Exception e) {
			result = null;
		} finally {
			if (objectOutputStream != null) {
				try {
					objectOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (byteArrayOutputStream != null) {
				try {
					byteArrayOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * 反序列化
	 *
	 * @param bytes bytes
	 * @return Object
	 */
	public static Object unserialize(byte[] bytes) {
		ByteArrayInputStream byteArrayInputStream = null;
		Object               result;
		try {
			byteArrayInputStream = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(byteArrayInputStream);
			result = ois.readObject();
		} catch (Exception e) {
			result = null;
		} finally {
			if (byteArrayInputStream != null) {
				try {
					byteArrayInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public static byte[] jsonSerialize(Object object) {
		if (Objects.isNull(object)) {
			return null;
		}
		Class<?> aClass = object.getClass();
		return Json.toJson(Maps.asMap(JSON_SERIALIZE_TYPE, aClass.getName(),
			JSON_SERIALIZE_VALUE, object)).getBytes(StandardCharsets.UTF_8);
	}

	public static <T> T jsonUnSerialize(byte[] bytes) {
		String jsonString = new String(bytes, StandardCharsets.UTF_8);
		try {
			JsonPath jsonPath = Json.jsonPath(jsonString);
			String className = jsonPath.get(JSON_SERIALIZE_TYPE, String.class);
			Class<?> aClass = Envs.forName(className);
			@SuppressWarnings("unchecked")
			T t = (T) jsonPath.get(JSON_SERIALIZE_VALUE, aClass);
			return t;
		} catch (Exception e) {
			Logger.error("Json UnSerialize error", e);
			return null;
		}
	}

}
