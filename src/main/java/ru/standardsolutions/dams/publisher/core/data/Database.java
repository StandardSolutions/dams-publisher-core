package ru.standardsolutions.dams.publisher.core.data;

import ru.standardsolutions.dams.publisher.core.data.database.DbInfo;

public interface Database {

    DbInfo info();

    <T> T execute(DatabaseOperation<T> operation);

    void execute(DatabaseVoidOperation operation);
}
