package ru.standardsolutions.dams.publisher.common;

import ru.standardsolutions.dams.publisher.common.database.DbType;

public interface Database {

    DbType type();

    String version();
}