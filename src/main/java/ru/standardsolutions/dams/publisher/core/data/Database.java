package ru.standardsolutions.dams.publisher.core.data;

import ru.standardsolutions.dams.publisher.core.data.database.DbType;

public interface Database {

    DbType type();

    String version();
}