package com.hoatv.sql.repository.proxy;

import com.hoatv.fwk.common.services.CheckedConsumer;
import com.hoatv.fwk.common.services.CheckedSupplier;
import com.hoatv.fwk.common.ultilities.InstanceUtils;
import com.hoatv.sql.annotations.*;
import com.hoatv.sql.annotations.Set;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepoProxyFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepoProxyFactory.class);

    private RepoProxyFactory() {

    }

    public static <T extends GenericRepository> T getRepositoryProxyInstance(Class<T> kInterface) {
        return getRepositoryProxyInstance(kInterface, new Properties());
    }

    @SuppressWarnings("unchecked")
    public static <T extends GenericRepository> T getRepositoryProxyInstance(Class<T> kInterface, Properties properties) {
        ClassLoader classLoader = kInterface.getClassLoader();
        ConnectionManager connectionManager = new ConnectionManager();
        return (T) Proxy.newProxyInstance(
                classLoader, new Class[]{kInterface}, new DefaultInvocationHandler(connectionManager, properties, kInterface));
    }

    private record DefaultInvocationHandler(ConnectionManager connectionManager, Properties configuration,
                                            Class<? extends GenericRepository> genericRepository) implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] objects) throws Throwable {

            Database database = genericRepository.getAnnotation(Database.class);
            Connection connection = connectionManager.getConnection(database, configuration);

            Select annotation = method.getAnnotation(Select.class);
            String selectClause = annotation == null ? "" : annotation.value();
            Update updateAnnotation = method.getAnnotation(Update.class);
            String updateClause = updateAnnotation == null ? "" : updateAnnotation.value();
            Set setAnnotation = method.getAnnotation(Set.class);
            String setClause = setAnnotation == null ? "" : setAnnotation.value();
            From fromAnnotation = method.getAnnotation(From.class);
            String fromClause = fromAnnotation == null ? "" : fromAnnotation.value();
            Where whereAnnotation = method.getAnnotation(Where.class);
            String whereClause = whereAnnotation == null ? "" : whereAnnotation.value();
            Order orderAnnotation = method.getAnnotation(Order.class);
            String orderClause = orderAnnotation == null ? "" : orderAnnotation.value();
            NativeQuery nativeQueryAnnotation = method.getAnnotation(NativeQuery.class);
            String queryClause = nativeQueryAnnotation == null ? "" : nativeQueryAnnotation.value();

            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Optional<Class<?>> outputClassOptional = Optional.empty();
            for (int parameterIndex = 0; parameterIndex < objects.length; parameterIndex++) {
                Object paramValue = objects[parameterIndex];

                if (paramValue instanceof Class<?> klass) {
                    outputClassOptional = Optional.of(klass);
                    continue;
                }

                Annotation[] indexParamAnnotations = parameterAnnotations[parameterIndex];
                Optional<Param> clauseParamOp = Arrays.stream(indexParamAnnotations)
                        .filter(Param.class::isInstance)
                        .map(Param.class::cast)
                        .findFirst();
                if (clauseParamOp.isPresent()) {
                    String paramName = clauseParamOp.get().value();

                    if (StringUtils.isNotEmpty(queryClause)) {
                        queryClause = replaceParamPlaceholder(queryClause, paramName, paramValue);
                    }

                    if (StringUtils.isNotEmpty(whereClause)) {
                        whereClause = replaceParamPlaceholder(whereClause, paramName, paramValue);
                    }

                    if (StringUtils.isNotEmpty(setClause)) {
                        setClause = replaceParamPlaceholder(setClause, paramName, paramValue);
                    }
                }
            }

            if (StringUtils.isNotEmpty(queryClause)) {
                return executeQuery(connection, queryClause, outputClassOptional);
            }

            if (StringUtils.isNotEmpty(selectClause)) {
                String selectQuery = getSelectQuery(selectClause, fromClause, whereClause, orderClause);
                return executeQuery(connection, selectQuery, outputClassOptional);
            }
            String updateQuery = getUpdateQuery(updateClause, setClause, whereClause);
            return executeQuery(connection, updateQuery, outputClassOptional);
        }

        @SuppressWarnings("all")
        private static List<Object> executeQuery(Connection connection, String queryString, Optional<Class<?>> outputClassOptional) throws SQLException {
            try (Statement statement = connection.createStatement()) {
                LOGGER.info("Execute SQL query {}", queryString);
                statement.execute(queryString);
                ResultSet resultSet = statement.getResultSet();

                List<Object> outputList = new ArrayList<>();

                while (resultSet.next()) {
                    if (outputClassOptional.isPresent()) {
                        Object dto = InstanceUtils.newInstance(outputClassOptional.get());
                        List<Field> fields = Arrays.asList(outputClassOptional.get().getDeclaredFields());
                        for (Field field : fields) {
                            field.setAccessible(true);
                        }

                        for (Field field : fields) {
                            Col col = field.getAnnotation(Col.class);
                            if (Objects.nonNull(col)) {
                                String name = col.name();
                                CheckedSupplier<String> valueSupplier = () -> resultSet.getString(name);
                                String value = valueSupplier.get();
                                CheckedConsumer<Object> objectCheckedConsumer = input -> field.set(input, field.getType().getConstructor(String.class).newInstance(value));
                                objectCheckedConsumer.accept(dto);
                            }
                        }
                        outputList.add(dto);
                    }
                }
                return outputList;
            }
        }

        private static String getUpdateQuery(String updateClause, String setClause, String whereClause) {
            String statementFormat = "UPDATE %s SET %s";
            String query = String.format(statementFormat, updateClause, setClause);
            if (StringUtils.isNotEmpty(whereClause)) {
                query = String.format("%s WHERE %s", query, whereClause);
            }
            query = query.concat(";");
            return query;
        }

        private static String getSelectQuery(String selectClause, String fromClause, String whereClause,
                                             String orderClause) {
            String statementFormat = "SELECT %s FROM %s";
            String query = String.format(statementFormat, selectClause, fromClause);
            if (StringUtils.isNotEmpty(whereClause)) {
                query = String.format("%s WHERE %s", query, whereClause);
            }
            if (StringUtils.isNotEmpty(orderClause)) {
                query = String.format("%s ORDER %s", query, orderClause);
            }
            query = query.concat(";");
            return query;
        }

        private static String replaceParamPlaceholder(String inputString, String paramName, Object param) {
            return inputString.replace(String.format("{%s}", paramName), String.valueOf(param));
        }
    }

    @Setter
    private static class ConnectionManager {

        private Connection connection;

        public Connection getConnection(Database database, Properties configuration) {
            if (Objects.isNull(connection)) {
                String databaseURL = checkThenGetFromProperties(database.url(), configuration);
                String username = checkThenGetFromProperties(database.username(), configuration);
                String password = checkThenGetFromProperties(database.password(), configuration);
                CheckedSupplier<Connection> connectionCheckedSupplier = () -> DriverManager.getConnection(databaseURL, username, password);
                setConnection(connectionCheckedSupplier.get());
            }
            return connection;
        }

        private String checkThenGetFromProperties(String input, Properties properties) {
            Pattern pattern = Pattern.compile("^(\\{)([\\w \\.]+)(\\})$");
            Matcher matcher = pattern.matcher(input);
            if (matcher.matches()) {
                String propertyName = matcher.group(2);
                return input.replace("{".concat(propertyName).concat("}"), properties.getProperty(propertyName));
            }
            return input;
        }
    }
}
