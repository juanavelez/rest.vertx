package com.zandero.rest;

import com.zandero.rest.annotation.*;
import com.zandero.rest.data.RouteDefinition;
import com.zandero.utils.Assert;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Collects all JAX-RS annotations to be transformed into routes
 */
public final class AnnotationProcessor {

	private static final List<Class<? extends Annotation>> REST_ANNOTATIONS = Arrays.asList(Path.class,
	                                                                                        HttpMethod.class,
	                                                                                        GET.class,
	                                                                                        POST.class,
	                                                                                        PUT.class,
	                                                                                        DELETE.class,
	                                                                                        PATCH.class,
	                                                                                        OPTIONS.class,
	                                                                                        TRACE.class,
	                                                                                        CONNECT.class,
	                                                                                        HEAD.class);

	private static final List<Class<? extends Annotation>> METHOD_ANNOTATIONS = Arrays.asList(Consumes.class,
	                                                                                          Produces.class,
	                                                                                          CatchWith.class,
	                                                                                          RequestReader.class,
	                                                                                          ResponseWriter.class,
	                                                                                          RouteOrder.class,
	                                                                                          SuppressCheck.class);

	private static final List<Class<? extends Annotation>> PARAM_ANNOTATIONS = Arrays.asList(RequestReader.class,
																							 PathParam.class,
	                                                                                         QueryParam.class,
	                                                                                         FormParam.class,
	                                                                                         HeaderParam.class,
	                                                                                         CookieParam.class,
	                                                                                         Context.class);
	private static List<Class<? extends Annotation>> ALL_ANNOTATIONS;

	static {
		ALL_ANNOTATIONS = new ArrayList<>();
		ALL_ANNOTATIONS.addAll(REST_ANNOTATIONS);
		ALL_ANNOTATIONS.addAll(METHOD_ANNOTATIONS);
	}

	private AnnotationProcessor() {
		// hide constructor
	}

	/**
	 * Checks class for JAX-RS annotations and returns a list of route definitions to build routes upon
	 *
	 * @param clazz to be checked
	 * @return list of definitions or empty list if none present
	 */
	public static Map<RouteDefinition, Method> get(Class clazz) {

		Assert.notNull(clazz, "Missing class with JAX-RS annotations!");

		// base
		RouteDefinition root = new RouteDefinition(clazz);

		Map<Method, Set<Annotation>> methodMap = collectMethodAnnotations(clazz, ALL_ANNOTATIONS);

		// go over methods ...
		Map<RouteDefinition, Method> output = new LinkedHashMap<>();
		for (Method method : methodMap.keySet()) {

			if (isRestMethod(methodMap.get(method))) { // Path must be present

				try {
					RouteDefinition definition = new RouteDefinition(root, methodMap.get(method).toArray(new Annotation[]{}));
					definition.setArguments(method);

					// check route path is not null
					Assert.notNullOrEmptyTrimmed(definition.getRoutePath(), "Missing route @Path!");

					output.put(definition, method);

				}
				catch (IllegalArgumentException e) {

					throw new IllegalArgumentException(clazz + "." + method.getName() + "() - " + e.getMessage());
				}
			}
		}

		return output;
	}

	/**
	 * A Rest method can have a Path and must have GET, POST ...
	 *
	 * @param methodAnnotations annotations of method
	 * @return true if REST method, false otherwise
	 */
	private static boolean isRestMethod(Set<Annotation> methodAnnotations) {

		for (Class<? extends Annotation> item : REST_ANNOTATIONS) {
			for (Annotation annotation : methodAnnotations) {
				if (annotation.annotationType().equals(item)) {
					return true;
				}
			}
		}

		return false;
	}


	/**
	 * Tries to find class with given annotation ... class it's interface or parent class
	 *
	 * @param clazz      to search
	 * @param annotation to search for
	 * @return found class with annotation or null if no class with given annotation could be found
	 */
	public static Class getClassWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
		if (clazz.isAnnotationPresent(annotation)) {
			return clazz;
		}

		for (Class inter : clazz.getInterfaces()) {
			if (inter.isAnnotationPresent(annotation)) {
				return inter;
			}
		}

		Class superClass = clazz.getSuperclass();
		if (superClass != Object.class && superClass != null) {
			return getClassWithAnnotation(superClass, annotation);
		}

		return null;
	}

	public static List<Method> collectMethods(Class<?> clazz) {

		List<Method> methods = new LinkedList<>(Arrays.asList(clazz.getMethods()));

		for (Class inter : clazz.getInterfaces()) {
			List<Method> interfaceMethods = collectMethods(inter);
			methods.addAll(interfaceMethods);
		}

		Class superClass = clazz.getSuperclass();
		if (superClass != Object.class && superClass != null) {
			List<Method> superClassMethods = collectMethods(superClass);
			methods.addAll(superClassMethods);
		}

		return methods;
	}

	public static Map<Method, Set<Annotation>> collectMethodAnnotations(Class<?> clazz, List<Class<? extends Annotation>> annotations) {

		List<Method> methods = collectMethods(clazz);

		Map<String, Set<Annotation>> collected = new LinkedHashMap<>();
		for (Method method : methods) {

			for (Class<? extends Annotation> annotation : annotations) {

				if (method.isAnnotationPresent(annotation)) {
					Annotation ann = method.getAnnotation(annotation);
					add(collected, method, ann);
				}
			}
		}

		// join annotations by method
		Map<Method, Set<Annotation>> out = new LinkedHashMap<>();
		for (String name: collected.keySet()) {

			methods.stream().filter(m -> m.getName().equals(name))
			       .findFirst()
			       .ifPresent(method -> out.put(method, collected.get(name)));
		}

		return out;
	}

	private static void add(Map<String, Set<Annotation>> out, Method method, Annotation ann) {

		if (!out.containsKey(method.getName())) {
			out.put(method.getName(), new LinkedHashSet<>());
		}

		Set<Annotation> set = out.get(method.getName());
		boolean found = false;
		for (Annotation annotation: set) {
			if (ann.annotationType().equals(annotation.annotationType())) {
				found = true;
			}
		}

		if (!found) {
			set.add(ann);
		}
	}
}
